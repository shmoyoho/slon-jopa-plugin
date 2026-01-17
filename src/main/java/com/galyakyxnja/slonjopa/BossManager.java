package com.galyakyxnja.slonjopa;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Color;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class BossManager {
    
    public static void spawnBoss(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        // ПРОВЕРКА 1: чтобы не спавнить внутри блока
        if (location.getBlock().getType().isSolid()) {
            // Ищем безопасное место выше
            for (int i = 1; i <= 5; i++) {
                Location checkLoc = location.clone().add(0, i, 0);
                if (!checkLoc.getBlock().getType().isSolid() && 
                    checkLoc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                    location = checkLoc;
                    break;
                }
            }
        }
        
        // ПРОВЕРКА 2: чтобы не спавнить в воде/лаве
        if (location.getBlock().getType() == Material.WATER || 
            location.getBlock().getType() == Material.LAVA ||
            location.getBlock().getType() == Material.LAVA_CAULDRON) {
            // Ищем ближайшую безопасную поверхность
            Location safeLocation = findSafeLocation(location);
            if (safeLocation != null) {
                location = safeLocation;
            } else {
                // Если не нашли безопасное место, спавним на самом высоком блоке
                location = world.getHighestBlockAt(location).getLocation().add(0, 1, 0);
            }
        }
        
        // ПРОВЕРКА 3: минимальная высота (не ниже 0)
        if (location.getY() < 0) {
            location.setY(world.getHighestBlockYAt(location) + 1);
        }
        
        // ПРОВЕРКА 4: чтобы не спавнить в пустоте (The End) или небе (The End)
        if (location.getY() > world.getMaxHeight() - 10) {
            location.setY(world.getHighestBlockYAt(location) + 1);
        }
        
        ZombieVillager boss = (ZombieVillager) location.getWorld().spawnEntity(location, EntityType.ZOMBIE_VILLAGER);
        
        double maxHealth = Main.getInstance().getConfig().getDouble("boss.max-health", 250.0);
        double attackDamage = Main.getInstance().getConfig().getDouble("boss.attack-damage", 2.0);
        String bossName = Main.getInstance().getConfig().getString("boss.name", "Хозяин Жопы");
        
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        boss.setHealth(maxHealth);
        boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(attackDamage);
        
        boss.customName(Component.text("§c§l" + bossName));
        boss.setCustomNameVisible(true);
        
        customizeAppearance(boss);
        
        boss.setAdult();
        boss.setRemoveWhenFarAway(false);
        boss.setAI(true);
        boss.setConversionTime(-1);
        
        boss.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1));
        boss.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.25); // ×1.25 скорости
        
        broadcastBossSpawn(location, bossName);
        
        // АКТИВИРУЕМ ИИ БОССА
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            BossAI.activateBossAI(boss);
        }, 20L); // Через 1 секунду после спавна
    }
    
    private static Location findSafeLocation(Location start) {
        World world = start.getWorld();
        int startX = start.getBlockX();
        int startY = start.getBlockY();
        int startZ = start.getBlockZ();
        
        // Ищем в радиусе 5 блоков
        for (int radius = 1; radius <= 5; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -2; y <= 2; y++) {
                        Location checkLoc = new Location(world, startX + x, startY + y, startZ + z);
                        Material blockType = checkLoc.getBlock().getType();
                        Material aboveType = checkLoc.clone().add(0, 1, 0).getBlock().getType();
                        
                        // Проверяем, что блок под ногами твердый и над головой воздух
                        if (blockType.isSolid() && blockType != Material.LAVA && 
                            blockType != Material.WATER && aboveType.isAir()) {
                            return checkLoc.clone().add(0, 1, 0); // Ставим на блок выше
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private static void customizeAppearance(ZombieVillager boss) {
        EntityEquipment equipment = boss.getEquipment();
        if (equipment != null) {
            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
            helmetMeta.setColor(Color.fromRGB(255, 192, 203));
            helmet.setItemMeta(helmetMeta);
            equipment.setHelmet(helmet);
            
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
            chestplateMeta.setColor(Color.fromRGB(255, 182, 193));
            chestplate.setItemMeta(chestplateMeta);
            equipment.setChestplate(chestplate);
            
            equipment.setHelmetDropChance(0.0f);
            equipment.setChestplateDropChance(0.0f);
        }
    }
    
    private static void broadcastBossSpawn(Location location, String bossName) {
        World world = location.getWorld();
        int x = (int) location.getX();
        int y = (int) location.getY();
        int z = (int) location.getZ();
        
        // 1 СООБЩЕНИЕ вместо 3
        Component globalMessage = Component.text("§4§l⚠ §cЛегендарный " + bossName + " §4§lпоявился на свет! Координаты: §eX:" + x + " Y:" + y + " Z:" + z + " §7| Сразите его, чтобы получить группу §aarchitect§7!");
        
        // Отправляем во ВЕСЬ сервер
        Bukkit.broadcast(globalMessage);
        
        // Дополнительно: сообщение игрокам в радиусе 100 блоков (с эффектами)
        Component localMessage = Component.text("§4§lВы чувствуете дрожь земли под ногами... монстр рядом!");
        
        world.getNearbyEntities(location, 100, 100, 100).forEach(entity -> {
            if (entity instanceof Player) {
                Player nearbyPlayer = (Player) entity;
                
                // Эффекты для близких игроков
                if (nearbyPlayer.getLocation().distance(location) < 50) {
                    nearbyPlayer.playSound(nearbyPlayer.getLocation(), 
                        org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
                    nearbyPlayer.sendMessage(localMessage);
                }
            }
        });
    }
    
    public static void checkBerserkMode(org.bukkit.entity.LivingEntity boss) {
        if (!(boss instanceof ZombieVillager)) return;
        
        Component customName = boss.customName();
        if (customName == null || !customName.toString().contains("Хозяин Жопы")) {
            return;
        }
        
        double maxHealth = boss.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = boss.getHealth();
        
        if (currentHealth <= maxHealth * 0.5 && !boss.hasPotionEffect(PotionEffectType.STRENGTH)) {
            // 1. УСИЛЕНИЕ УРОНА
            boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
            
            // 2. УВЕЛИЧЕНИЕ СКОРОСТИ В 2 РАЗА (ПРАВИЛЬНЫЙ АТРИБУТ)
            boss.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.4); // ×2 скорости
            
            // 3. УБИРАЕМ ЗАМЕДЛЕНИЕ
            boss.removePotionEffect(PotionEffectType.SLOWNESS);
            
            // 4. ДОБАВЛЯЕМ ЭФФЕКТ СКОРОСТИ
            boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
            
            // 5. СООБЩЕНИЕ
            Component message = Component.text("§4§l⚠ ХОЗЯИН ЖОПЫ ВПАЛ В ЯРОСТЬ! СТАЛ БЫСТРЕЕ И СИЛЬНЕЕ!");
            boss.getWorld().getNearbyEntities(boss.getLocation(), 30, 30, 30).forEach(entity -> {
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage(message);
                }
            });
            
            // 6. ВИЗУАЛЬНЫЕ ЭФФЕКТЫ
            for (int i = 0; i < 15; i++) {
                boss.getWorld().spawnParticle(
                    org.bukkit.Particle.FLAME,
                    boss.getLocation().add(
                        Math.random() * 2 - 1,
                        1 + Math.random() * 2,
                        Math.random() * 2 - 1
                    ),
                    1
                );
            }
            
            // 7. ЗВУК
            try {
                boss.getWorld().playSound(
                    boss.getLocation(),
                    org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL,
                    1.0f,
                    0.8f
                );
            } catch (Exception e) {
                // Игнорируем ошибки со звуком
            }
        }
    }
    
    public static void onBossDeath(Location location, String killerName) {
        Player player = Bukkit.getPlayer(killerName);
        if (player != null && player.isOnline()) {
            String rewardGroup = Main.getInstance().getConfig().getString("reward-group", "architect");
            LuckPermsIntegration.addGroupToPlayer(player, rewardGroup);
            
            Component broadcastMsg = Component.text("§6§l🎉 " + killerName + 
                " победил Хозяина Жопы и получил группу " + rewardGroup + "!");
            Bukkit.broadcast(broadcastMsg);
        }
        
        Main.getInstance().getLogger().info("Босс убит игроком: " + killerName);
    }
}