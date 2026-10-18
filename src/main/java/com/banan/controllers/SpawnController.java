package com.banan.controllers;

import com.banan.core.ElysiumController;
import com.banan.modules.Rule;
import com.banan.modules.Spawn.SpawnRule;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.ArrayList;
import java.util.List;

public class SpawnController implements Listener {

    private boolean isEnabled = false;
    private final List<Rule> rules = new ArrayList<>();

    private SpawnController() {
        rules.add(new SpawnRule());
    }

    public void enable() {
        if (isEnabled) return;
        isEnabled = true;
        
        for (Rule rule : rules) {
            rule.setIsActive(true);
        }
        
        Bukkit.getPluginManager().registerEvents(this, ElysiumController.getInstance());
        Bukkit.getLogger().info("[Elysium] SpawnController включен.");
        
        // Сохраняем конфиг ТОЛЬКО если это ручной вызов (из команды)
        // Для этого мы добавим простой флаг
        if (!isLoading) {
            ElysiumController.getInstance().getConfig().set("spawn-control.enabled", true);
            ElysiumController.getInstance().saveConfig();
        }
    }

    public void disable() {
        if (!isEnabled) return;
        isEnabled = false;
        
        for (Rule rule : rules) {
            rule.setIsActive(false);
        }
        
        HandlerList.unregisterAll(this);
        Bukkit.getLogger().info("[Elysium] SpawnController выключен.");
        
        // Убираем отсюда сохранение конфига!
        // Конфиг будет сохраняться только в команде.
    }

    // Добавляем флаг "загрузки"
    private boolean isLoading = false;
    
    public void setLoading(boolean loading) {
        this.isLoading = loading;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isEnabled) return;

        String worldName = event.getLocation().getWorld().getName();
        List<String> enabledWorlds = ElysiumController.getInstance().getConfig().getStringList("spawn-control.enabled-worlds");

        if (!enabledWorlds.contains(worldName)) return; 

        // МАГИЯ: Принудительно разрешаем спавнеры (ломаем проверку света)
        if (event.getSpawnReason() == SpawnReason.SPAWNER) {
            event.setCancelled(false);
        }

        // Прогоняем через правила
        boolean shouldCancel = false;
        for (Rule rule : rules) {
            rule.handleEvent(event);
        }

        for (Rule rule : rules) {
            if (!rule.isEventApplied()) { 
                shouldCancel = true;
                break;
            }
        }

        if (shouldCancel) {
            event.setCancelled(true);
        }
    }

    private static class SingletonHolder {
        protected static final SpawnController instance = new SpawnController();
    }

    public static SpawnController getInstance() {
        return SingletonHolder.instance;
    }
}