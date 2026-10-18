package com.banan.core;

import com.banan.commands.ElysiumCommand;
import com.banan.controllers.SpawnController;
import org.bukkit.plugin.java.JavaPlugin;

public class ElysiumController extends JavaPlugin {

    // Ссылка на единственный экземпляр плагина
    private static ElysiumController instance;

    private SpawnController spawnController;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("=== Запуск Elysium Controller ===");
        
        saveDefaultConfig();
        spawnController = SpawnController.getInstance();
        
        if (getConfig().getBoolean("spawn-control.enabled", false)) {
            getLogger().info("[Elysium] Загрузка состояния из config.yml...");
            
            // Включаем "режим загрузки", чтобы не перезаписывать файл
            spawnController.setLoading(true);
            spawnController.enable();
            spawnController.setLoading(false); // Выключаем режим загрузки
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
            getLogger().info("Команды (Commands) зарегистрированы.");
        } else {
            getLogger().warning("Ошибка! Команда /elysium не найдена в plugin.yml!");
        }
    }

    public SpawnController getSpawnController() {
        return spawnController;
    }

    // --- Singleton ---
    public static ElysiumController getInstance() {
        return instance;
    }
}