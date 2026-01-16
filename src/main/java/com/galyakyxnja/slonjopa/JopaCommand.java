package com.galyakyxnja.slonjopa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;

public class JopaCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("§cЭту команду можно использовать только в игре!"));
            return true;
        }

        Player player = (Player) sender;
        
        // Если игрок ввёл /jopa help или просто /jopa без частей
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendQuestInfo(player);
            return true;
        }
        
        // Проверка инвентаря
        if (!hasAllParts(player)) {
            sendMissingPartsMessage(player);
            sendQuestInfo(player); // Показываем информацию о квесте
            return true;
        }
        
        // У игрока есть все части
        removePartsFromInventory(player);
        
        // Призыв босса
        player.sendMessage(Component.text("§a§l✓ Легенда оживает! Призываю Хозяина Жопы..."));
        BossManager.spawnBoss(player.getLocation());
        
        return true;
    }
    
    private boolean hasAllParts(Player player) {
        PlayerInventory inventory = player.getInventory();
        Map<SlonPart, Integer> partsCount = new HashMap<>();
        
        for (SlonPart part : SlonPart.values()) {
            partsCount.put(part, 0);
        }
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();
                for (SlonPart part : SlonPart.values()) {
                    if (displayName.contains(part.getDisplayName())) {
                        partsCount.put(part, partsCount.get(part) + item.getAmount());
                    }
                }
            }
        }
        
        for (SlonPart part : SlonPart.values()) {
            if (partsCount.get(part) < 1) {
                return false;
            }
        }
        return true;
    }
    
    private void removePartsFromInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        
        for (SlonPart part : SlonPart.values()) {
            int toRemove = 1;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
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
    }
    
    private void sendMissingPartsMessage(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<String> missingParts = new ArrayList<>();
        
        for (SlonPart part : SlonPart.values()) {
            boolean hasPart = false;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                    item.getItemMeta().getDisplayName().contains(part.getDisplayName())) {
                    hasPart = true;
                    break;
                }
            }
            if (!hasPart) {
                missingParts.add(part.getDisplayName());
            }
        }
        
        // Сообщение о недостающих частях
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("§c§l✗ Вы ещё не собрали все части Слона!")
            .color(TextColor.color(255, 85, 85))
            .decorate(TextDecoration.BOLD));
        
        if (!missingParts.isEmpty()) {
            player.sendMessage(Component.text("§7Не хватает: §e" + String.join("§7, §e", missingParts)));
        }
        
        player.sendMessage(Component.text("§7У вас есть: §e" + countParts(inventory) + "§7/6 частей"));
        player.sendMessage(Component.text("§6Используйте §e/jopa help §6для информации о квесте"));
    }
    
    private int countParts(PlayerInventory inventory) {
        int count = 0;
        for (SlonPart part : SlonPart.values()) {
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                    item.getItemMeta().getDisplayName().contains(part.getDisplayName())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    private void sendQuestInfo(Player player) {
        // КРАСИВОЕ ОФОРМЛЕНИЕ КВЕСТА
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("§6╔══════════════════════════════════╗")
            .decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("§6║      §e§lЛЕГЕНДА О СЛОНЕ       §6║")
            .decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("§6╚══════════════════════════════════╝"));
        player.sendMessage(Component.text(""));
        
        player.sendMessage(Component.text("§7Давным-давно великий §bМагический Слон§7 странствовал")
            .color(TextColor.color(170, 170, 170)));
        player.sendMessage(Component.text("§7по этим землям. После великой битвы он рассыпался")
            .color(TextColor.color(170, 170, 170)));
        player.sendMessage(Component.text("§7на 6 магических частей, которые были поглощены")
            .color(TextColor.color(170, 170, 170)));
        player.sendMessage(Component.text("§7самой землёй..."));
        player.sendMessage(Component.text(""));
        
        player.sendMessage(Component.text("§6§l✨ ЦЕЛЬ КВЕСТА:")
            .decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("§71. §eДобывайте уголь §7- с шансом §a5%§7 выпадают части")
            .color(TextColor.color(170, 170, 170)));
        player.sendMessage(Component.text("§72. §eСоберите все 6 частей§7:"));
        
        // Список частей с редкостью
        for (SlonPart part : SlonPart.values()) {
            String rarityColor = part == SlonPart.JOPA ? "§c" : "§e";
            String rarityText = part == SlonPart.JOPA ? "§c(ОЧЕНЬ РЕДКО) " : 
                               part.getChanceWeight() <= 2 ? "§6(Редко) " : "§a(Обычно) ";
            player.sendMessage(Component.text("   §8• " + rarityColor + part.getDisplayName() + 
                " §7- " + rarityText));
        }
        
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("§73. §eПризовите Хозяина Жопы §7командой §6/jopa")
            .color(TextColor.color(170, 170, 170)));
        player.sendMessage(Component.text("§74. §eПобедите босса§7 и получите:")
            .color(TextColor.color(170, 170, 170)));
        player.sendMessage(Component.text("   §8• §aГруппу Architect §7(права на сервере)"));
        player.sendMessage(Component.text("   §8• §e20 уровней опыта"));
        player.sendMessage(Component.text("   §8• §cСлаву победителя!"));
        player.sendMessage(Component.text(""));
        
        player.sendMessage(Component.text("§6§l⚔ ОСОБЕННОСТИ БОССА:")
            .decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("§8• §c250 HP §7- огромное здоровье"));
        player.sendMessage(Component.text("§8• §cОтражает 30% урона §7обратно атакующему"));
        player.sendMessage(Component.text("§8• §cРежим Ярости §7- при 50% HP урон ×2, скорость ×2"));
        player.sendMessage(Component.text(""));
        
        player.sendMessage(Component.text("§6§l📊 ВАША СТАТИСТИКА:")
            .decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("§7Собрано частей: §e" + countParts(player.getInventory()) + "§7/6"));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("§7Автор квеста: §dGalyaKyxnya §7для сервера §6'Жопа Слона'")
            .color(TextColor.color(170, 170, 170)));
        player.sendMessage(Component.text(""));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("help");
            return completions;
        }
        return new ArrayList<>();
    }
}