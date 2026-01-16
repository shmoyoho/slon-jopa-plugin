package com.galyakyxnja.slonjopa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.HashMap;
import java.util.Map;

public class JopaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем, что команду вводит игрок
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду можно использовать только в игре!");
            return true;
        }

        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();

        // Подсчитываем, какие части есть у игрока
        Map<SlonPart, Integer> partsCount = new HashMap<>();
        for (SlonPart part : SlonPart.values()) {
            partsCount.put(part, 0);
        }

        // Проходим по всему инвентарю игрока
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.hasItemMeta()) {
                String displayName = item.getItemMeta().getDisplayName();
                for (SlonPart part : SlonPart.values()) {
                    if (displayName.contains(part.getDisplayName())) {
                        partsCount.put(part, partsCount.get(part) + item.getAmount());
                    }
                }
            }
        }

        // Проверяем, есть ли хотя бы по одной каждой части
        boolean hasAllParts = true;
        for (SlonPart part : SlonPart.values()) {
            if (partsCount.get(part) < 1) {
                hasAllParts = false;
                break;
            }
        }

        if (!hasAllParts) {
            // У игрока не все части
            player.sendMessage("§c§lСобери все 6 частей слона!");
            player.sendMessage("§eКуски падают из угля с шансом 5%.");
            player.sendMessage("§6Нужные части: Ухо, Шкура, Кость, Нога, Хобот, Жопа.");
            return true;
        }

        // У игрока есть все части — удаляем по одному экземпляру каждой
        for (SlonPart part : SlonPart.values()) {
            int toRemove = 1;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.hasItemMeta() && 
                    item.getItemMeta().getDisplayName().contains(part.getDisplayName())) {
                    int amount = item.getAmount();
                    if (amount > toRemove) {
                        item.setAmount(amount - toRemove);
                        toRemove = 0;
                    } else {
                        toRemove -= amount;
                        inventory.remove(item);
                    }
                    if (toRemove == 0) break;
                }
            }
        }

        player.sendMessage("§a§l✓ Все части собраны! Призываю Хозяина Жопы...");
        
        // Спавним босса
        spawnBoss(player);
        return true;
    }

    private void spawnBoss(Player player) {
        // Вызываем метод из BossManager (создадим в следующем шаге)
        BossManager.spawnBoss(player.getLocation());
        player.sendMessage("§c§l⚠ Приготовься к битве! Хозяин Жопы появился рядом!");
    }
}