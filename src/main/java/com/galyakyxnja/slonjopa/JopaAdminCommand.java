package com.galyakyxnja.slonjopa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JopaAdminCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверка прав
        if (!sender.hasPermission("slonjopa.admin")) {
            sender.sendMessage(Component.text("§cУ вас нет прав на эту команду!"));
            return true;
        }
        
        // Проверка аргументов
        if (args.length < 2) {
            sender.sendMessage(Component.text("§6Использование: /jopas <игрок> <часть>"));
            sender.sendMessage(Component.text("§eДоступные части: " + getAllPartsString()));
            return true;
        }
        
        Player targetPlayer = org.bukkit.Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("§cИгрок " + args[0] + " не найден или оффлайн!"));
            return true;
        }
        
        // Получаем часть слона
        SlonPart part = SlonPart.fromString(args[1]);
        if (part == null) {
            sender.sendMessage(Component.text("§cНеизвестная часть! Доступные:"));
            sender.sendMessage(Component.text("§e" + getAllPartsString()));
            return true;
        }
        
        // Выдаём часть
        ItemStack item = part.getItem();
        targetPlayer.getInventory().addItem(item);
        
        // Сообщения
        sender.sendMessage(Component.text("§aВыдал " + part.getDisplayName() + " игроку " + targetPlayer.getName()));
        targetPlayer.sendMessage(Component.text("§eВы получили " + part.getDisplayName() + " от администратора!"));
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Автодополнение имён игроков
            List<String> playerNames = new ArrayList<>();
            for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        } else if (args.length == 2) {
            // Автодополнение частей слона
            List<String> parts = new ArrayList<>();
            for (SlonPart part : SlonPart.values()) {
                parts.add(part.name().toLowerCase());
                parts.add(part.getDisplayName().toLowerCase());
            }
            return parts;
        }
        return new ArrayList<>();
    }
    
    private String getAllPartsString() {
        StringBuilder sb = new StringBuilder();
        for (SlonPart part : SlonPart.values()) {
            sb.append(part.name().toLowerCase()).append(" (").append(part.getDisplayName()).append("), ");
        }
        return sb.substring(0, sb.length() - 2); // Убираем последнюю запятую
    }
}