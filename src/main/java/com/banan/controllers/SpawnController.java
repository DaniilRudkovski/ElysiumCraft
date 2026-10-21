package com.banan.controllers;

import com.banan.core.ElysiumController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnController implements Listener {

    private boolean isEnabled = false;
    private BukkitTask spawnerTask;

    // Кэш: Мир -> Список координат спавнеров
    private final Map<String, List<Location>> customSpawners = new HashMap<>();

    private SpawnController() {
    }

    public void enable() {
        if (isEnabled) return;
        isEnabled = true;
        
        Bukkit.getPluginManager().registerEvents(this, ElysiumController.getInstance());
        Bukkit.getLogger().info("[Elysium] SpawnController включен.");

        loadCustomSpawners();
        startTimer();
    }

    // Метод для запуска таймера
    private void startTimer() {
        if (spawnerTask != null) {
            spawnerTask.cancel();
        }

        int delay = ElysiumController.getInstance().getConfig().getInt("spawn-control.settings.delay", 160);
        int radius = ElysiumController.getInstance().getConfig().getInt("spawn-control.settings.radius", 10);
        int radiusSq = radius * radius;

        spawnerTask = Bukkit.getScheduler().runTaskTimer(ElysiumController.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String worldName = player.getWorld().getName();

                if (!ElysiumController.getInstance().getConfig().getBoolean("spawn-control.worlds." + worldName + ".customspawn", false)) {
                    continue;
                }

                List<Location> spawners = customSpawners.get(worldName);
                if (spawners == null || spawners.isEmpty()) continue;

                for (Location loc : spawners) {
                    if (loc.distanceSquared(player.getLocation()) <= radiusSq) {
                        Block block = loc.getBlock();
                        if (block.getType() == Material.SPAWNER && block.getState() instanceof CreatureSpawner spawner) {
                            EntityType type = spawner.getSpawnedType();
                            if (type != null) {
                                // --- ПРОВЕРКА ЛИМИТА ---
                                int limitmobs = ElysiumController.getInstance().getConfig().getInt("spawn-control.settings.limitmobs", 10);
                                int limitRadius = ElysiumController.getInstance().getConfig().getInt("spawn-control.settings.limitradius", 8);
                                
                                // Считаем мобов этого типа в радиусе limitRadius блоков от спавнера
                                int nearbyCount = 0;
                                for (Entity entity : loc.getWorld().getNearbyEntities(loc, limitRadius, limitRadius, limitRadius)) {
                                    if (entity.getType() == type) {
                                        nearbyCount++;
                                    }
                                }

                                // Если мобов больше лимита — пропускаем спавн
                                if (nearbyCount >= limitmobs) {
                                    continue; 
                                }
                                // -----------------------

                                // СПАВНИМ НА 1 БЛОК ВЫШЕ (чтобы не застревали)
                                Location spawnLoc = loc.clone().add(0, 1, 0);
                                loc.getWorld().spawn(spawnLoc, type.getEntityClass(), SpawnReason.CUSTOM);
                            }
                        }
                    }
                }
            }
        }, 0L, delay);
    }

    // Метод для перезапуска таймера (при смене delay)
    public void reloadTimer() {
        if (isEnabled) {
            startTimer();
            Bukkit.getLogger().info("[Elysium] Таймер спавна перезапущен.");
        }
    }

    public void reloadCustomSpawners() {
        customSpawners.clear();
        loadCustomSpawners();
        Bukkit.getLogger().info("[Elysium] Список спавнеров перезагружен в память.");
    }

    private void loadCustomSpawners() {
        customSpawners.clear();
        for (World world : Bukkit.getWorlds()) {
            List<String> list = ElysiumController.getInstance().getConfig()
                    .getStringList("spawn-control.custom-spawner-list." + world.getName());
            
            if (list != null) {
                List<Location> locs = new ArrayList<>();
                for (String s : list) {
                    String[] parts = s.split(",");
                    if (parts.length == 3) {
                        try {
                            locs.add(new Location(world, Integer.parseInt(parts[0]) + 0.5, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) + 0.5));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (!locs.isEmpty()) {
                    customSpawners.put(world.getName(), locs);
                }
            }
        }
        Bukkit.getLogger().info("[Elysium] Загружено кастомных спавнеров: " + customSpawners.values().stream().mapToInt(List::size).sum());
    }

    public void disable() {
        if (!isEnabled) return;
        isEnabled = false;
        
        if (spawnerTask != null) {
            spawnerTask.cancel();
            spawnerTask = null;
        }
        
        customSpawners.clear();
        HandlerList.unregisterAll(this);
        Bukkit.getLogger().info("[Elysium] SpawnController выключен.");
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isEnabled) return;

        String worldName = event.getLocation().getWorld().getName();
        
        if (!ElysiumController.getInstance().getConfig().contains("spawn-control.worlds." + worldName)) {
            return; 
        }

        SpawnReason reason = event.getSpawnReason();

        if (reason == SpawnReason.NATURAL) {
            boolean naturalEnabled = ElysiumController.getInstance().getConfig()
                    .getBoolean("spawn-control.worlds." + worldName + ".natural", false);
            if (!naturalEnabled) event.setCancelled(true);
        }

        if (reason == SpawnReason.SPAWNER) {
            boolean spawnerEnabled = ElysiumController.getInstance().getConfig()
                    .getBoolean("spawn-control.worlds." + worldName + ".spawner", false);
            if (!spawnerEnabled) event.setCancelled(true);
        }

        if (reason == SpawnReason.TRIAL_SPAWNER) {
            boolean trialEnabled = ElysiumController.getInstance().getConfig()
                    .getBoolean("spawn-control.worlds." + worldName + ".trial", false);
            if (!trialEnabled) event.setCancelled(true);
        }
    }

    private static class SingletonHolder {
        protected static final SpawnController instance = new SpawnController();
    }

    public static SpawnController getInstance() {
        return SingletonHolder.instance;
    }
}