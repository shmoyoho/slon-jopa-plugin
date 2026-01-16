package com.galyakyxnja.slonjopa;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    // Статическая ссылка на плагин для доступа из других классов
    private static Main instance;

   @Override
public void onEnable() {
    instance = this;
    getLogger().info("§aПлагин 'Жопа Слона' включен! Создатель: GalyaKyxnya");

    // Инициализация LuckPerms
    if (!LuckPermsIntegration.setupLuckPerms()) {
        getLogger().warning("Интеграция с LuckPerms отключена.");
    }

    // Регистрация команд
    this.getCommand("jopa").setExecutor(new JopaCommand());
    this.getCommand("jopas").setExecutor(new JopaAdminCommand());

    // Регистрация слушателей событий
    getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
    getServer().getPluginManager().registerEvents(new BossDeathListener(), this);
    getServer().getPluginManager().registerEvents(new BossDamageListener(), this); // ← НОВЫЙ СЛУШАТЕЛЬ

    // Сохранение конфига по умолчанию
    saveDefaultConfig();
}

    @Override
    public void onDisable() {
        getLogger().info("§cПлагин 'Жопа Слона' выключен.");
    }

    public static Main getInstance() {
        return instance;
    }
}