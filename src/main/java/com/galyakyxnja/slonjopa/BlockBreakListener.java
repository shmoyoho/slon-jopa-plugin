package com.galyakyxnja.slonjopa;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Random;

public class BlockBreakListener implements Listener {
    private final Random random = new Random();
    private final int TOTAL_WEIGHT = 31; // Сумма всех chanceWeight (10+8+6+4+2+1=31)

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Проверяем, что сломан уголь
        if (event.getBlock().getType() == Material.COAL_ORE) {
            // Шанс 5% на выпадение любой части
            if (random.nextInt(100) < 5) {
                // Определяем, какая именно часть выпадет
                int roll = random.nextInt(TOTAL_WEIGHT);
                int currentWeight = 0;

                for (SlonPart part : SlonPart.values()) {
                    currentWeight += part.getChanceWeight();
                    if (roll < currentWeight) {
                        // Выдаём часть
                        ItemStack partItem = part.getItem();
                        event.getPlayer().getWorld().dropItemNaturally(
                            event.getBlock().getLocation().add(0.5, 0.5, 0.5), 
                            partItem
                        );
                        event.getPlayer().sendMessage("§6§l✨ Вы нашли: " + part.getDisplayName() + "!");
                        break;
                    }
                }
            }
        }
    }
}