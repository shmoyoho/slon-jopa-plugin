package com.galyakyxnja.slonjopa;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Random;
import java.util.HashSet;

public class BlockBreakListener implements Listener {
    private final Random random = new Random();
    private final int TOTAL_WEIGHT = 31; // Сумма всех chanceWeight (10+8+6+4+2+1=31)
    
    // Множество всех руд, которые могут давать части
    private final HashSet<Material> validOres = new HashSet<>();
    
    public BlockBreakListener() {
        // Заполняем множество валидных руд при создании
        for (SlonPart part : SlonPart.values()) {
            validOres.add(part.getSourceOre());
            // Также добавляем глубокосланцевые версии
            validOres.add(Material.valueOf("DEEPSLATE_" + part.getSourceOre().name()));
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material brokenBlock = event.getBlock().getType();
        
        // Проверяем, является ли блок рудой (обычной или глубокосланцевой)
        if (!validOres.contains(brokenBlock)) {
            return;
        }
        
        // Получаем базовый тип руды (без DEEPSLATE_ префикса)
        Material baseOre = getBaseOreType(brokenBlock);
        
        // Получаем шанс из конфига (по умолчанию 3% вместо 5%)
        double chance = Main.getInstance().getConfig().getDouble("drop-chance", 3.0);
        
        // Проверяем общий шанс дропа
        if (random.nextDouble() * 100 < chance) {
            // Определяем, какая именно часть выпадет
            int roll = random.nextInt(TOTAL_WEIGHT);
            int currentWeight = 0;

            for (SlonPart part : SlonPart.values()) {
                // Проверяем, выпадает ли эта часть из этой руды
                if (part.getSourceOre() != baseOre) continue;
                    
                currentWeight += part.getChanceWeight();
                if (roll < currentWeight) {
                    // Выдаём часть
                    ItemStack partItem = part.getItem();
                    event.getPlayer().getWorld().dropItemNaturally(
                        event.getBlock().getLocation().add(0.5, 0.5, 0.5), 
                        partItem
                    );
                    event.getPlayer().sendMessage("§6§l✨ Вы нашли: " + part.getDisplayName() + " из " + getOreDisplayName(brokenBlock) + "!");
                    break;
                }
            }
        }
    }
    
    private Material getBaseOreType(Material ore) {
        String oreName = ore.name();
        if (oreName.startsWith("DEEPSLATE_")) {
            // Убираем префикс "DEEPSLATE_" чтобы получить базовый тип
            return Material.valueOf(oreName.substring(10));
        }
        return ore;
    }
    
    private String getOreDisplayName(Material ore) {
        switch (ore) {
            case COAL_ORE: return "§7угля";
            case DEEPSLATE_COAL_ORE: return "§7глубокосланцевого угля";
            case IRON_ORE: return "§fжелеза";
            case DEEPSLATE_IRON_ORE: return "§fглубокосланцевого железа";
            case GOLD_ORE: return "§6золота";
            case DEEPSLATE_GOLD_ORE: return "§6глубокосланцевого золота";
            case COPPER_ORE: return "§6меди";
            case DEEPSLATE_COPPER_ORE: return "§6глубокосланцевой меди";
            case EMERALD_ORE: return "§aизумрудов";
            case DEEPSLATE_EMERALD_ORE: return "§aглубокосланцевых изумрудов";
            case DIAMOND_ORE: return "§bалмазов";
            case DEEPSLATE_DIAMOND_ORE: return "§bглубокосланцевых алмазов";
            default: return ore.toString();
        }
    }
}