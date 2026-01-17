package com.galyakyxnja.slonjopa;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import java.util.Arrays;

public enum SlonPart {
    EAR("Ухо слона", Material.PINK_WOOL, 10, Material.COAL_ORE),
    SKIN("Шкура слона", Material.LEATHER, 8, Material.IRON_ORE),
    BONE("Кость слона", Material.BONE, 6, Material.GOLD_ORE),
    FOOT("Нога слона", Material.MUTTON, 4, Material.COPPER_ORE),
    TRUNK("Хобот слона", Material.CARROT_ON_A_STICK, 2, Material.EMERALD_ORE),
    JOPA("Жопа слона", Material.GHAST_TEAR, 1, Material.DIAMOND_ORE); // Самый редкий

    private final String displayName;
    private final Material material;
    private final int chanceWeight;
    private final Material sourceOre;

    SlonPart(String displayName, Material material, int chanceWeight, Material sourceOre) {
        this.displayName = displayName;
        this.material = material;
        this.chanceWeight = chanceWeight;
        this.sourceOre = sourceOre;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Обновлённый лор с информацией об источнике
        String oreName = getOreDisplayName(sourceOre);
        meta.displayName(Component.text("§e" + displayName));
        meta.lore(Arrays.asList(
                Component.text("§7Часть мифического слона"),
                Component.text("§6Собери все 6 частей!"),
                Component.text("§8Источник: " + oreName),
                Component.text("§8Вес выпадения: " + chanceWeight)
        ));
        item.setItemMeta(meta);
        return item;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getChanceWeight() {
        return chanceWeight;
    }

    public Material getSourceOre() {
        return sourceOre;
    }

    public static SlonPart fromString(String name) {
        for (SlonPart part : values()) {
            if (part.name().equalsIgnoreCase(name) || 
                part.getDisplayName().equalsIgnoreCase(name)) {
                return part;
            }
        }
        return null;
    }
    
    private String getOreDisplayName(Material ore) {
        switch (ore) {
            case COAL_ORE: return "§7Угольная руда";
            case IRON_ORE: return "§fЖелезная руда";
            case GOLD_ORE: return "§6Золотая руда";
            case COPPER_ORE: return "§6Медная руда";
            case EMERALD_ORE: return "§aИзумрудная руда";
            case DIAMOND_ORE: return "§bАлмазная руда";
            default: return ore.toString();
        }
    }
}