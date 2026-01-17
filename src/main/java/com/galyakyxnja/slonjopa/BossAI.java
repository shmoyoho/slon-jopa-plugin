package com.galyakyxnja.slonjopa;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossAI {
    
    private static final Map<UUID, BossData> activeBosses = new HashMap<>();
    private static JavaPlugin plugin;
    
    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    // Класс для хранения данных босса
    private static class BossData {
        ZombieVillager boss;
        long lastMeleeAttack = 0;
        long lastRangedAttack = 0;
        int taskId = -1;
        
        BossData(ZombieVillager boss) {
            this.boss = boss;
        }
    }
    
    // Активируем ИИ для босса
    public static void activateBossAI(ZombieVillager boss) {
        if (activeBosses.containsKey(boss.getUniqueId())) {
            return; // ИИ уже активирован
        }
        
        BossData data = new BossData(boss);
        activeBosses.put(boss.getUniqueId(), data);
        
        // Запускаем задачу ИИ каждые 2 тика (0.1 секунды)
        data.taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || boss.isDead()) {
                    deactivateBossAI(boss);
                    cancel();
                    return;
                }
                
                updateBossAI(boss);
            }
        }.runTaskTimer(plugin, 0L, 2L).getTaskId();
        
        plugin.getLogger().info("Активирован ИИ для босса: " + boss.getName());
    }
    
    // Деактивируем ИИ
    public static void deactivateBossAI(ZombieVillager boss) {
        BossData data = activeBosses.remove(boss.getUniqueId());
        if (data != null && data.taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(data.taskId);
        }
    }
    
    // Основная логика ИИ
    private static void updateBossAI(ZombieVillager boss) {
        BossData data = activeBosses.get(boss.getUniqueId());
        if (data == null) return;
        
        LivingEntity target = boss.getTarget();
        
        // Если нет цели, ищем ближайшего игрока в радиусе 20 блоков
        if (target == null || !(target instanceof Player) || target.isDead()) {
            target = findNearestPlayer(boss, 20);
            if (target instanceof Player) {
                boss.setTarget(target);
            }
        }
        
        if (!(target instanceof Player)) {
            return; // Нет подходящей цели
        }
        
        Player player = (Player) target;
        double distance = boss.getLocation().distance(player.getLocation());
        
        long currentTime = System.currentTimeMillis();
        
        // БЛИЖНЯЯ АТАКА (когда игрок в радиусе 3 блоков)
        if (distance <= 3.0) {
            // Атакуем каждые 1.5 секунды (1500 мс)
            if (currentTime - data.lastMeleeAttack >= 1500) {
                performMeleeAttack(boss, player);
                data.lastMeleeAttack = currentTime;
            }
        }
        
        // ДАЛЬНЯЯ АТАКА (когда игрок дальше 8 блоков)
        if (distance > 8.0) {
            // Стреляем каждые 3 секунды (3000 мс)
            if (currentTime - data.lastRangedAttack >= 3000) {
                performRangedAttack(boss, player);
                data.lastRangedAttack = currentTime;
            }
        }
        
        // Если в режиме берсерка - ускоряем атаки
        if (boss.hasPotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH)) {
            // В режиме берсерка атаки в 1.5 раза чаще
            if (distance <= 3.0 && currentTime - data.lastMeleeAttack >= 1000) {
                performMeleeAttack(boss, player);
                data.lastMeleeAttack = currentTime;
            }
            
            if (distance > 8.0 && currentTime - data.lastRangedAttack >= 2000) {
                performRangedAttack(boss, player);
                data.lastRangedAttack = currentTime;
            }
        }
    }
    
    // Ближняя атака (удар рукой)
    private static void performMeleeAttack(ZombieVillager boss, Player player) {
        double damage = Main.getInstance().getConfig().getDouble("boss.melee-damage", 10.0);
        
        // Наносим урон
        player.damage(damage, boss);
        
        // Эффекты удара
        player.playSound(player.getLocation(), 
            org.bukkit.Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 
            1.0f, 0.8f);
        
        // Частицы удара
        Location hitLocation = player.getLocation().add(0, 1, 0);
        boss.getWorld().spawnParticle(
            org.bukkit.Particle.CRIT,
            hitLocation,
            10,
            0.3, 0.3, 0.3,
            0.1
        );
        
        // Отбрасывание игрока
        Vector knockback = player.getLocation().toVector()
            .subtract(boss.getLocation().toVector())
            .normalize()
            .multiply(0.5)
            .setY(0.3);
        player.setVelocity(knockback);
        
        // Сообщение игроку (если не спамить)
        if (System.currentTimeMillis() % 5000 < 100) {
            player.sendMessage("§cБосс ударил вас в ближнем бою!");
        }
    }
    
    // Дальняя атака (фаербол)
    private static void performRangedAttack(ZombieVillager boss, Player player) {
        Location bossLoc = boss.getLocation().add(0, 1.5, 0);
        Location targetLoc = player.getLocation().add(0, 1, 0);
        
        // Направление к игроку
        Vector direction = targetLoc.toVector()
            .subtract(bossLoc.toVector())
            .normalize();
        
        // Создаем фаербол
        Fireball fireball = boss.getWorld().spawn(bossLoc, Fireball.class);
        fireball.setShooter(boss);
        fireball.setDirection(direction);
        fireball.setYield(1.5f); // Мощность взрыва
        fireball.setIsIncendiary(true); // Поджигает блоки
        
        // Настраиваем скорость
        fireball.setVelocity(direction.multiply(1.5));
        
        // Кастомное имя для фаербола
        fireball.setCustomName("§cОгненный шар Хозяина Жопы");
        fireball.setCustomNameVisible(false);
        
        // Звук выстрела
        boss.getWorld().playSound(
            bossLoc,
            org.bukkit.Sound.ENTITY_BLAZE_SHOOT,
            1.0f,
            0.8f
        );
        
        // Частицы при выстреле
        boss.getWorld().spawnParticle(
            org.bukkit.Particle.FLAME,
            bossLoc,
            15,
            0.2, 0.2, 0.2,
            0.05
        );
        
        // Сообщение игроку
        if (System.currentTimeMillis() % 5000 < 100) {
            player.sendMessage("§6Босс запустил в вас огненный шар!");
        }
        
        // Если в режиме берсерка - пускаем 3 фаербола веером
        if (boss.hasPotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH)) {
            for (int i = -1; i <= 1; i++) {
                if (i == 0) continue; // Пропускаем центральный (уже создан)
                
                Vector fanDirection = direction.clone()
                    .rotateAroundY(Math.toRadians(15 * i));
                
                Fireball fanFireball = boss.getWorld().spawn(bossLoc, Fireball.class);
                fanFireball.setShooter(boss);
                fanFireball.setDirection(fanDirection);
                fanFireball.setYield(1.0f);
                fanFireball.setIsIncendiary(true);
                fanFireball.setVelocity(fanDirection.multiply(1.5));
            }
        }
    }
    
    // Поиск ближайшего игрока
    private static Player findNearestPlayer(LivingEntity entity, double radius) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Player player : entity.getWorld().getPlayers()) {
            if (player.isDead() || !player.isValid()) continue;
            
            double distance = entity.getLocation().distance(player.getLocation());
            if (distance <= radius && distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        
        return nearest;
    }
    
    // Получить данные босса (для отладки)
    public static boolean isBossActive(UUID bossId) {
        return activeBosses.containsKey(bossId);
    }
}