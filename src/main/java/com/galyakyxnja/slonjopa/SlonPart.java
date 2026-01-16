package com.galyakyxnja.slonjopa;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import java.util.Arrays;

public enum SlonPart {
    EAR("Ухо слона", Material.PINK_WOOL, 10),
    SKIN("Шкура слона", Material.LEATHER, 8),
    BONE("Кость слона", Material.BONE, 6),
    FOOT("Нога слона", Material.MUTTON, 4),
    TRUNK("Хобот слона", Material.CARROT_ON_A_STICK, 2),
    JOPA("Жопа слона", Material.GHAST_TEAR, 1); // Самый редкий

    private final String displayName;
    private final Material material;
    private final int chanceWeight; // Вес для шанса выпадения (чем меньше, тем реже)

    SlonPart(String displayName, Material material, int chanceWeight) {
        this.displayName = displayName;
        this.material = material;
        this.chanceWeight = chanceWeight;
    }

    // Метод для создания предмета (ItemStack)
    public ItemStack getItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e" + displayName));
        meta.lore(Arrays.asList(
                Component.text("§7Часть мифического слона"),
                Component.text("§6Собери все 6 частей!"),
                Component.text("§8Шанс выпадения: " + chanceWeight + "%")
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

    // Получить часть по имени (для админской команды)
    public static SlonPart fromString(String name) {
        for (SlonPart part : values()) {
            if (part.name().equalsIgnoreCase(name) || 
                part.getDisplayName().equalsIgnoreCase(name)) {
                return part;
            }
        }
        return null;
    }
}