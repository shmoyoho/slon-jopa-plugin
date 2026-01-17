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
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;
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
        player.sendMessage(Component.text("§a§l✓ Легенда оживает! Призываю Хозяина Жопы в радиусе 50 блоков..."));
        
        // Генерируем случайную точку в радиусе 30-50 блоков
        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();
        Random random = new Random();
        
        // Случайный угол и расстояние (30-50 блоков)
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 30 + random.nextDouble() * 20; // От 30 до 50 блоков
        
        // Вычисляем координаты
        double x = playerLoc.getX() + Math.cos(angle) * distance;
        double z = playerLoc.getZ() + Math.sin(angle) * distance;
        
        // Находим безопасную Y-координату
        int y = world.getHighestBlockYAt((int) x, (int) z) + 1;
        
        Location spawnLocation = new Location(world, x, y, z);
        
        // Проверка на воду/лаву
        if (spawnLocation.getBlock().getType() == Material.WATER || 
            spawnLocation.getBlock().getType() == Material.LAVA ||
            spawnLocation.getBlock().getType() == Material.LAVA_CAULDRON) {
            // Ищем другое место
            for (int i = 0; i < 5; i++) {
                angle = random.nextDouble() * 2 * Math.PI;
                distance = 30 + random.nextDouble() * 20;
                x = playerLoc.getX() + Math.cos(angle) * distance;
                z = playerLoc.getZ() + Math.sin(angle) * distance;
                y = world.getHighestBlockYAt((int) x, (int) z) + 1;
                spawnLocation = new Location(world, x, y, z);
                
                if (spawnLocation.getBlock().getType() != Material.WATER && 
                    spawnLocation.getBlock().getType() != Material.LAVA &&
                    spawnLocation.getBlock().getType() != Material.LAVA_CAULDRON) {
                    break;
                }
            }
        }
        
        // Сообщение игроку о координатах
        player.sendMessage(Component.text("§6Босс появится на координатах: §eX:" + (int)x + " Y:" + y + " Z:" + (int)z));
        
        // Спавним босса
        BossManager.spawnBoss(spawnLocation);
        
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
    player.sendMessage(Component.text("§6║      §e§lЛЕГЕНДА О СЛОНЕ         §6║")
        .decorate(TextDecoration.BOLD));

    
    player.sendMessage(Component.text("§7Давным-давно великий §bМагический Слон§7 странствовал")
        .color(TextColor.color(170, 170, 170)));
    player.sendMessage(Component.text("§7по этим землям. После великой битвы он рассыпался")
        .color(TextColor.color(170, 170, 170)));
    player.sendMessage(Component.text("§7на 6 магических частей, которые были поглощены")
        .color(TextColor.color(170, 170, 170)));
    player.sendMessage(Component.text("§7разными рудами земли..."));
    player.sendMessage(Component.text(""));
    
    player.sendMessage(Component.text("§6§l✨ ЦЕЛЬ КВЕСТА:")
        .decorate(TextDecoration.BOLD));
    player.sendMessage(Component.text("§71. §eДобывайте разные руды §7- каждая часть выпадает из своей руды")
        .color(TextColor.color(170, 170, 170)));
    
    // Детальная информация о дропе
    player.sendMessage(Component.text("§72. §eЧасти выпадают из руд с шансом §a3%§7:"));
    
    for (SlonPart part : SlonPart.values()) {
        String oreName = getOreDisplayName(part.getSourceOre());
        String rarityColor = part == SlonPart.JOPA ? "§c" : 
                           part.getChanceWeight() <= 2 ? "§6" : "§a";
        String rarityText = part == SlonPart.JOPA ? "§c(ОЧЕНЬ РЕДКО) " : 
                           part.getChanceWeight() <= 2 ? "§6(Редко) " : "§a(Обычно) ";
        
        player.sendMessage(Component.text("   §8• " + oreName + " §7→ " + 
            rarityColor + part.getDisplayName() + " §7- " + rarityText));
    }
    
    player.sendMessage(Component.text("§7   §8ℹ §7Работает для обычных и глубокосланцевых руд"));
    player.sendMessage(Component.text(""));
    
    player.sendMessage(Component.text("§73. §eПризовите Хозяина Жопы §7командой §6/jopa")
        .color(TextColor.color(170, 170, 170)));
    player.sendMessage(Component.text("   §8• §7Босс появится §eв радиусе 30-50 блоков§7 от вас")
        .color(TextColor.color(170, 170, 170)));
    player.sendMessage(Component.text("   §8• §7Всем игрокам придёт §cуведомление в чат§7!")
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
    player.sendMessage(Component.text("§8• §cПоявляется в радиусе §e30-50 блоков §cот призывающего"));
    player.sendMessage(Component.text(""));
    
    player.sendMessage(Component.text("§6§l📊 ВАША СТАТИСТИКА:")
        .decorate(TextDecoration.BOLD));
    player.sendMessage(Component.text("§7Собрано частей: §e" + countParts(player.getInventory()) + "§7/6"));
    player.sendMessage(Component.text(""));
    player.sendMessage(Component.text("§7Автор плагина: §dGalyaKyxnya §7для сервера §6'Жопа Слона'")
        .color(TextColor.color(170, 170, 170)));
    player.sendMessage(Component.text(""));
}

private String getOreDisplayName(Material ore) {
    switch (ore) {
        case COAL_ORE: return "§8Угольная руда";
        case IRON_ORE: return "§fЖелезная руда";
        case GOLD_ORE: return "§6Золотая руда";
        case COPPER_ORE: return "§6Медная руда";
        case EMERALD_ORE: return "§aИзумрудная руда";
        case DIAMOND_ORE: return "§bАлмазная руда";
        default: return ore.toString();
    }
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