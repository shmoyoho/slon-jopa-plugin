package com.galyakyxnja.slonjopa;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.Color;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class BossManager {
    
    public static void spawnBoss(Location location) {
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
        
        Component message1 = Component.text("§4§lВНИМАНИЕ! §c" + bossName + " появился на координатах");
        Component message2 = Component.text("§cX: §e" + x + " §cY: §e" + y + " §cZ: §e" + z);
        Component message3 = Component.text("§6Сразите его, чтобы получить группу §aarchitect§6!");
        
        world.getNearbyEntities(location, 50, 50, 50).forEach(entity -> {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.sendMessage(message1);
                player.sendMessage(message2);
                player.sendMessage(message3);
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
        
        // 7. ЗВУК (опционально, можно убрать если ошибка)
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