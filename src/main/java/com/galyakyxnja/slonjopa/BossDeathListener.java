package com.galyakyxnja.slonjopa;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class BossDeathListener implements Listener {
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Проверяем, что умер зомби-житель (наш босс)
        if (!(event.getEntity() instanceof ZombieVillager)) return;
        
        ZombieVillager zombie = (ZombieVillager) event.getEntity();
        Component customName = zombie.customName();
        
        // Проверяем имя босса
        if (customName == null || !customName.toString().contains("Хозяин Жопы")) {
            return; // Это не наш босс
        }
        
        // Получаем игрока-убийцу
        Player killer = zombie.getKiller();
        if (killer == null) return;
        
        // 1. Выдаём опыт (20 уровней)
        killer.giveExpLevels(20);
        
        // 2. Отправляем кастомное сообщение в чат
        Component message = Component.text("@DfvGhy Я " + killer.getName() + "Убил твоего Слона!")
            .color(TextColor.color(255, 170, 0)); // Золотой цвет §6
        
        zombie.getWorld().sendMessage(message);
        
        // 3. Очищаем стандартный дроп зомби
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // 4. Вызываем метод для выдачи прав через LuckPerms
        BossManager.onBossDeath(zombie.getLocation(), killer.getName());
        
        // 5. Фейерверк (опционально)
        try {
            FireworkEffect fireworkEffect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.RED)
                    .withFade(Color.YELLOW)
                    .build();
                    
            Firework firework = zombie.getWorld().spawn(zombie.getLocation(), Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(fireworkEffect);
            meta.setPower(1);
            firework.setFireworkMeta(meta);
        } catch (Exception e) {
            // Игнорируем ошибки с фейерверком, если они есть
            Main.getInstance().getLogger().warning("Не удалось создать фейерверк: " + e.getMessage());
        }
    }
}