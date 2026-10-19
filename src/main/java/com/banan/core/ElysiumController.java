package com.banan.core;

import com.banan.commands.ElysiumCommand;
import com.banan.commands.ElysiumTabCompleter;
import com.banan.controllers.SpawnController;
import org.bukkit.plugin.java.JavaPlugin;

public class ElysiumController extends JavaPlugin {

    private static ElysiumController instance;
    private SpawnController spawnController;

        @Override
    public void onEnable() {
        instance = this;
        getLogger().info("=== Запуск Elysium Controller ===");
        
        saveDefaultConfig();
        spawnController = SpawnController.getInstance();
        
        // Читаем конфиг. Если там true — включаем модуль.
        if (getConfig().getBoolean("spawn-control.enabled", false)) {
            getLogger().info("[Elysium] Модуль спавна был включен в конфиге. Запускаем...");
            spawnController.enable();
        }

        registerCommands();
        getLogger().info("=== Elysium Controller успешно загружен! ===");
    }

    @Override
    public void onDisable() {
        if (spawnController != null) {
            spawnController.disable();
        }
        instance = null;
        getLogger().info("Elysium Controller выключен.");
    }

    private void registerCommands() {
        if (this.getCommand("elysium") != null) {
            this.getCommand("elysium").setExecutor(new ElysiumCommand(this));
            this.getCommand("elysium").setTabCompleter(new ElysiumTabCompleter());
        } else {
            getLogger().warning("Ошибка! Команда /elysium не найдена в plugin.yml!");
        }
    }

    public SpawnController getSpawnController() {
        return spawnController;
    }

    public static ElysiumController getInstance() {
        return instance;
    }
}