package com.galyakyxnja.slonjopa;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import net.kyori.adventure.text.Component;

public class BossDamageListener implements Listener {
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Проверяем, что бьют нашего босса
        if (!(event.getEntity() instanceof ZombieVillager)) return;
        
        ZombieVillager boss = (ZombieVillager) event.getEntity();
        Component customName = boss.customName();
        
        if (customName == null || !customName.toString().contains("Хозяин Жопы")) {
            return; // Это не наш босс
        }
        
        // Определяем, кто нанёс урон
        Player damager = null;
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                damager = (Player) projectile.getShooter();
            }
        }
        
        if (damager == null) return;
        
        // Получаем процент отражения урона из конфига
        double reflectionPercent = Main.getInstance().getConfig().getDouble("boss.damage-reflection", 0.3);
        
        // Рассчитываем отражённый урон (30% от нанесённого)
        double originalDamage = event.getDamage();
        double reflectedDamage = originalDamage * reflectionPercent;
        
        // Наносим отражённый урон атакующему
        if (reflectedDamage > 0) {
            damager.damage(reflectedDamage, boss);
            
            // Сообщение игроку (раз в 5 секунд, чтобы не спамить)
            if (System.currentTimeMillis() % 5000 < 50) {
                damager.sendMessage(Component.text("§cБосс отражает " + 
                    (int)(reflectionPercent * 100) + "% вашего урона!"));
            }
        }
        
        // Уменьшаем получаемый боссом урон
        double damageReduction = 0.2; // 20% снижение урона
        double finalDamage = originalDamage * (1 - damageReduction);
        
        // УМНОЖАЕМ УРОН БОССА В РЕЖИМЕ БЕРСЕРКА (когда у него ≤50% HP)
        double damageMultiplier = 1.0;
        if (boss.hasPotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH)) {
            damageMultiplier = 2.0; // ×2 урон в режиме берсерка
        }
        
        // Устанавливаем итоговый урон
        event.setDamage(finalDamage * damageMultiplier);
    }
    
    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        // Защита босса от некоторых типов урона
        if (!(event.getEntity() instanceof ZombieVillager)) return;
        
        ZombieVillager boss = (ZombieVillager) event.getEntity();
        Component customName = boss.customName();
        
        if (customName == null || !customName.toString().contains("Хозяин Жопы")) {
            return;
        }
        
        // Защита от падения
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(event.getDamage() * 0.5); // Урон от падения уменьшен на 50%
        }
        
        // Защита от огня
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
            event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
            event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            event.setDamage(event.getDamage() * 0.3); // 70% сопротивления огню
        }
    }
}