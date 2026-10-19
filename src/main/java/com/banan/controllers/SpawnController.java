package com.banan.controllers;

import com.banan.core.ElysiumController;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class SpawnController implements Listener {

    private boolean isEnabled = false;

    private SpawnController() {
    }

    public void enable() {
        if (isEnabled) return;
        isEnabled = true;
        
        Bukkit.getPluginManager().registerEvents(this, ElysiumController.getInstance());
        Bukkit.getLogger().info("[Elysium] SpawnController включен.");
    }

    public void disable() {
        if (!isEnabled) return;
        isEnabled = false;
        
        HandlerList.unregisterAll(this);
        Bukkit.getLogger().info("[Elysium] SpawnController выключен.");
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isEnabled) return;

        // Получаем имя мира
        String worldName = event.getLocation().getWorld().getName();
        
        // Проверяем, есть ли вообще настройки для этого мира
        if (!ElysiumController.getInstance().getConfig().contains("spawn-control.worlds." + worldName)) {
            return; 
        }

        SpawnReason reason = event.getSpawnReason();

        // 1. Обычный спавн
        if (reason == SpawnReason.NATURAL) {
            boolean naturalEnabled = ElysiumController.getInstance().getConfig()
                    .getBoolean("spawn-control.worlds." + worldName + ".natural", false);
            
            if (!naturalEnabled) {
                event.setCancelled(true);
            }
        }

        // 2. Спавнеры
        if (reason == SpawnReason.SPAWNER) {
            boolean spawnerEnabled = ElysiumController.getInstance().getConfig()
                    .getBoolean("spawn-control.worlds." + worldName + ".spawner", false);
            
            if (spawnerEnabled) {
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        }

        // 3. Рассадники
        if (reason == SpawnReason.TRIAL_SPAWNER) {
            boolean trialEnabled = ElysiumController.getInstance().getConfig()
                    .getBoolean("spawn-control.worlds." + worldName + ".trial", false);
            
            if (trialEnabled) {
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        }
    }

    private static class SingletonHolder {
        protected static final SpawnController instance = new SpawnController();
    }

    public static SpawnController getInstance() {
        return SingletonHolder.instance;
    }
}