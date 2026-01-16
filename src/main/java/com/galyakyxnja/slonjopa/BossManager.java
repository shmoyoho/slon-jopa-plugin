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
        // Спавним зомби-жителя (не горит на солнце)
        ZombieVillager boss = (ZombieVillager) location.getWorld().spawnEntity(location, EntityType.ZOMBIE_VILLAGER);
        
        // Настройка босса из config.yml
        double maxHealth = Main.getInstance().getConfig().getDouble("boss.max-health", 250.0);
        double attackDamage = Main.getInstance().getConfig().getDouble("boss.attack-damage", 2.0);
        String bossName = Main.getInstance().getConfig().getString("boss.name", "Хозяин Жопы");
        
        // Устанавливаем точное здоровье через атрибут (работает для зомби)
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        boss.setHealth(maxHealth);
        
        // Устанавливаем слабый урон
        boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(attackDamage);
        
        // Устанавливаем имя
        boss.customName(Component.text("§c§l" + bossName));
        boss.setCustomNameVisible(true);
        
        // Настраиваем внешний вид как свинью
        customizeAppearance(boss);
        
        // Делаем взрослым и неисчезающим
        boss.setAdult();
        boss.setRemoveWhenFarAway(false);
        boss.setAI(true);
        
        // Эффекты
        boss.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0)); // Немного быстрее
        
        // Отключаем превращение на солнце
        boss.setConversionTime(-1);
        
        // Сообщение о появлении
        broadcastBossSpawn(location, bossName);
    }
    
    private static void customizeAppearance(ZombieVillager boss) {
        // 1. Одеваем в розовую кожаную броню (цвет свиньи)
        EntityEquipment equipment = boss.getEquipment();
        if (equipment != null) {
            // Розовый шлем
            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
            helmetMeta.setColor(Color.fromRGB(255, 192, 203)); // Розовый
            helmet.setItemMeta(helmetMeta);
            equipment.setHelmet(helmet);
            
            // Розовый нагрудник
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
            chestplateMeta.setColor(Color.fromRGB(255, 182, 193)); // Светло-розовый
            chestplate.setItemMeta(chestplateMeta);
            equipment.setChestplate(chestplate);
            
            // Делаем броню неснимаемой
            equipment.setHelmetDropChance(0.0f);
            equipment.setChestplateDropChance(0.0f);
        }
        
        // 2. Делаем его "толстым" (больше хитбокс)
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1)); // Медленнее
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