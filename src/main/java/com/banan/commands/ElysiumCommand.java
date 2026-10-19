package com.banan.commands;

import com.banan.core.ElysiumController;
import com.banan.controllers.SpawnController;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ElysiumCommand implements CommandExecutor {

    private final ElysiumController plugin;

    public ElysiumCommand(ElysiumController plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== Elysium Controller ===");
            player.sendMessage(ChatColor.YELLOW + "/elysium spawn enable" + ChatColor.GRAY + " - включить модуль");
            player.sendMessage(ChatColor.YELLOW + "/elysium spawn disable" + ChatColor.GRAY + " - выключить модуль");
            player.sendMessage(ChatColor.YELLOW + "/elysium spawn <мир> init" + ChatColor.GRAY + " - создать настройки");
            player.sendMessage(ChatColor.YELLOW + "/elysium spawn <мир> <natural/spawner/trial> <on/off>");
            return true;
        }

        // --- Команда регистрации спавнеров ---
        if (args[0].equalsIgnoreCase("functions") && args.length > 1 && args[1].equalsIgnoreCase("recalcspawners")) {
            org.bukkit.World world = player.getWorld();
            int radius = 32; // Ищем в радиусе 32 блоков
            
            int cx = player.getLocation().getBlockX() >> 4;
            int cz = player.getLocation().getBlockZ() >> 4;
            int chunksRadius = radius >> 4;
            
            java.util.List<String> spawnerList = new java.util.ArrayList<>();
            int found = 0;
            
            for (int x = cx - chunksRadius; x <= cx + chunksRadius; x++) {
                for (int z = cz - chunksRadius; z <= cz + chunksRadius; z++) {
                    if (!world.isChunkLoaded(x, z)) continue;
                    
                    for (org.bukkit.block.BlockState state : world.getChunkAt(x, z).getTileEntities()) {
                        if (state instanceof org.bukkit.block.CreatureSpawner) {
                            String locStr = state.getX() + "," + state.getY() + "," + state.getZ();
                            spawnerList.add(locStr);
                            found++;
                        }
                    }
                }
            }
            
            if (found == 0) {
                player.sendMessage(ChatColor.RED + "Спавнеры не найдены в радиусе " + radius + " блоков.");
                return true;
            }
            
            // Сохраняем список в конфиг
            plugin.getConfig().set("spawn-control.custom-spawner-list." + world.getName(), spawnerList);
            plugin.saveConfig();
            plugin.getSpawnController().reloadCustomSpawners();
            
            player.sendMessage(ChatColor.GREEN + "Зарегистрировано спавнеров: " + found + " в мире " + world.getName());
            return true;
        }

        if (args[0].equalsIgnoreCase("functions") && args.length > 3 && args[1].equalsIgnoreCase("customspawnerset")) {
            String setting = args[2].toLowerCase();
            String valueStr = args[3];
            
            try {
                int value = Integer.parseInt(valueStr);
                
                if (setting.equals("delay")) {
                    plugin.getConfig().set("spawn-control.settings.delay", value);
                    plugin.saveConfig();
                    // Перезапускаем таймер с новой задержкой (для этого вызовем reload)
                    plugin.getSpawnController().reloadTimer();
                    player.sendMessage(ChatColor.GREEN + "Задержка спавна установлена на " + value + " тиков.");
                    return true;
                }
                
                if (setting.equals("radius")) {
                    plugin.getConfig().set("spawn-control.settings.radius", value);
                    plugin.saveConfig();
                    // Перезапускаем таймер с новым радиусом (для этого вызовем reload)
                    plugin.getSpawnController().reloadTimer();
                    player.sendMessage(ChatColor.GREEN + "Радиус спавна установлен на " + value + " блоков.");
                    return true;
                }
                
                player.sendMessage(ChatColor.RED + "Неизвестная настройка. Доступно: delay, radius.");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Введи число!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            SpawnController sc = plugin.getSpawnController();
            
            // --- Управление модулем (enable/disable) ---
            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("enable")) {
                    sc.enable();
                    plugin.getConfig().set("spawn-control.enabled", true);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Модуль контроля спавна ВКЛЮЧЕН.");
                    return true;
                }
                if (args[1].equalsIgnoreCase("disable")) {
                    sc.disable();
                    plugin.getConfig().set("spawn-control.enabled", false);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.RED + "Модуль контроля спавна ВЫКЛЮЧЕН.");
                    return true;
                }
            }

            // --- Управление мирами ---
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Используй: /elysium spawn <init|enable|disable|мир>");
                return true;
            }

            String worldName = args[1];
            String type = args[2].toLowerCase();
            String basePath = "spawn-control.worlds." + worldName;

            // 0. Удаление мира из конфига
            if (type.equals("remove")) {
                if (!plugin.getConfig().contains(basePath)) {
                    player.sendMessage(ChatColor.RED + "Мира " + worldName + " нет в конфиге.");
                    return true;
                }
                
                plugin.getConfig().set(basePath, null); // Удаляем всю ветку мира
                plugin.saveConfig();
                
                player.sendMessage(ChatColor.GREEN + "Мир " + worldName + " полностью удален из конфига.");
                return true;
            }

            // 1. Инициализация
            if (type.equals("init")) {
                if (plugin.getConfig().contains(basePath)) {
                    player.sendMessage(ChatColor.RED + "Мир " + worldName + " уже есть в конфиге.");
                    return true;
                }
                
                plugin.getConfig().set(basePath + ".natural", false);
                plugin.getConfig().set(basePath + ".spawner", false);
                plugin.getConfig().set(basePath + ".trial", false);
                plugin.saveConfig();
                
                player.sendMessage(ChatColor.GREEN + "Мир " + worldName + " добавлен в конфиг (все настройки выключены).");
                return true;
            }

            // 2. Настройка параметров
            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "Используй: /elysium spawn <мир> <тип> <on/off>");
                return true;
            }

            String state = args[3].toLowerCase();

            if (!type.equals("natural") && !type.equals("spawner") && !type.equals("trial") && !type.equals("customspawn")) {
                player.sendMessage(ChatColor.RED + "Неизвестный тип. Доступно: natural, spawner, trial, customspawn.");
                return true;
            }

            if (!state.equals("on") && !state.equals("off")) {
                player.sendMessage(ChatColor.RED + "Используй on или off.");
                return true;
            }

            boolean isOn = state.equals("on");
            String path = basePath + "." + type;
            
            if (!plugin.getConfig().contains(basePath)) {
                plugin.getConfig().set(basePath + ".natural", false);
                plugin.getConfig().set(basePath + ".spawner", false);
                plugin.getConfig().set(basePath + ".trial", false);
                player.sendMessage(ChatColor.YELLOW + "Мир " + worldName + " не был найден. Автоматически добавлен.");
            }

            plugin.getConfig().set(path, isOn);
            plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + "Настройка " + type + " для мира " + worldName + " теперь " + state.toUpperCase());
            return true;
        }

        player.sendMessage(ChatColor.RED + "Неизвестная команда.");
        return true;
    }
}