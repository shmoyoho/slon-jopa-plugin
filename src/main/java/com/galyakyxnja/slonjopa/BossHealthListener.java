package com.galyakyxnja.slonjopa;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.ZombieVillager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class BossHealthListener implements Listener {
    
    @EventHandler
    public void onBossDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ZombieVillager)) {
            return;
        }
        
        ZombieVillager boss = (ZombieVillager) event.getEntity();
        Component customName = boss.customName();
        
        if (customName == null || !customName.toString().contains("Хозяин Жопы")) {
            return;
        }
        
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            BossManager.checkBerserkMode(boss);
        }, 1L);
    }
}