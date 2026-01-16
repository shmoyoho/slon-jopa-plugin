package com.galyakyxnja.slonjopa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsIntegration {
    
    public static boolean setupLuckPerms() {
        // Просто проверяем, установлен ли LuckPerms
        boolean hasLuckPerms = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
        if (hasLuckPerms) {
            Main.getInstance().getLogger().info("LuckPerms найден, будет использоваться консольная команда.");
        } else {
            Main.getInstance().getLogger().info("LuckPerms не найден. Права выдаваться не будут.");
        }
        return hasLuckPerms;
    }
    
    public static void addGroupToPlayer(Player player, String groupName) {
        // Исполняем команду от имени консоли
        String command = "lp user " + player.getName() + " parent add " + groupName;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        
        // Сообщение игроку
        player.sendMessage("§a§l✓ Вы получили группу §e" + groupName + "§a через LuckPerms!");
        player.sendMessage("§6Теперь у вас есть права архитектора на сервере!");
        
        // Лог в консоль сервера
        Main.getInstance().getLogger().info("Выполнена команда: " + command);
    }
}