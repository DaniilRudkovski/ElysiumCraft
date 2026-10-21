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
        // ПРОВЕРКА ПРАВ
        if (!sender.hasPermission("elysium.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
            return true;
        }

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

        // --- Команды регистрации спавнеров ---
        if (args[0].equalsIgnoreCase("functions") && args.length > 1) {
            
            // Проверяем, что ввели именно recalcspawners
            if (args[1].equalsIgnoreCase("recalcspawners")) {
                
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Используй: /elysium functions recalcspawners <addall/deleteall> <радиус>");
                    return true;
                }

                String action = args[2].toLowerCase();
                org.bukkit.World world = player.getWorld();
                String path = "spawn-control.custom-spawner-list." + world.getName();
                
                // Радиус по умолчанию 64
                int radius = 64;
                if (args.length > 3) {
                    try {
                        radius = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Радиус должен быть числом!");
                        return true;
                    }
                }

                // --- CLEARUNUSED ---
                if (action.equals("clearunused")) {
                    java.util.List<String> spawnerList = new java.util.ArrayList<>(plugin.getConfig().getStringList(path));
                    
                    if (spawnerList.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "База спавнеров пуста.");
                        return true;
                    }

                    int removed = 0;
                    java.util.Iterator<String> iterator = spawnerList.iterator();
                    
                    while (iterator.hasNext()) {
                        String locStr = iterator.next();
                        String[] parts = locStr.split(",");
                        if (parts.length == 3) {
                            try {
                                int x = Integer.parseInt(parts[0]);
                                int y = Integer.parseInt(parts[1]);
                                int z = Integer.parseInt(parts[2]);
                                
                                // Проверяем, существует ли блок по этим координатам и является ли он спавнером
                                org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                                if (block.getType() != org.bukkit.Material.SPAWNER) {
                                    iterator.remove();
                                    removed++;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    if (removed > 0) {
                        plugin.getConfig().set(path, spawnerList);
                        plugin.saveConfig();
                        plugin.getSpawnController().reloadCustomSpawners();
                        player.sendMessage(ChatColor.GREEN + "Очистка завершена. Удалено мертвых спавнеров: " + removed + ". Осталось: " + spawnerList.size());
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Мертвых спавнеров не найдено. В базе: " + spawnerList.size());
                    }
                    return true;
                }

                // --- ADDALL ---
                if (action.equals("addall")) {
                    java.util.List<String> spawnerList = new java.util.ArrayList<>(plugin.getConfig().getStringList(path));
                    
                    int cx = player.getLocation().getBlockX() >> 4;
                    int cz = player.getLocation().getBlockZ() >> 4;
                    int chunksRadius = radius >> 4;
                    
                    int added = 0;
                    int found = 0;

                    for (int x = cx - chunksRadius; x <= cx + chunksRadius; x++) {
                        for (int z = cz - chunksRadius; z <= cz + chunksRadius; z++) {
                            if (!world.isChunkLoaded(x, z)) continue;
                            
                            for (org.bukkit.block.BlockState state : world.getChunkAt(x, z).getTileEntities()) {
                                if (state instanceof org.bukkit.block.CreatureSpawner) {
                                    String locStr = state.getX() + "," + state.getY() + "," + state.getZ();
                                    found++;
                                    
                                    if (!spawnerList.contains(locStr)) {
                                        spawnerList.add(locStr);
                                        added++;
                                    }
                                }
                            }
                        }
                    }

                    plugin.getConfig().set(path, spawnerList);
                    plugin.saveConfig();
                    plugin.getSpawnController().reloadCustomSpawners();
                    
                    player.sendMessage(ChatColor.GREEN + "Найдено: " + found + ". Добавлено новых: " + added + ". Всего в базе: " + spawnerList.size());
                    return true;
                }

                // --- DELETEALL ---
                if (action.equals("deleteall")) {
                    java.util.List<String> spawnerList = new java.util.ArrayList<>(plugin.getConfig().getStringList(path));
                    
                    if (spawnerList.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "База спавнеров пуста.");
                        return true;
                    }

                    int removed = 0;
                    java.util.Iterator<String> iterator = spawnerList.iterator();
                    
                    while (iterator.hasNext()) {
                        String locStr = iterator.next();
                        String[] parts = locStr.split(",");
                        if (parts.length == 3) {
                            try {
                                int x = Integer.parseInt(parts[0]);
                                int y = Integer.parseInt(parts[1]);
                                int z = Integer.parseInt(parts[2]);
                                
                                double dist = player.getLocation().distanceSquared(new org.bukkit.Location(world, x, y, z));
                                
                                if (dist <= (radius * radius)) {
                                    iterator.remove();
                                    removed++;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    plugin.getConfig().set(path, spawnerList);
                    plugin.saveConfig();
                    plugin.getSpawnController().reloadCustomSpawners();
                    
                    player.sendMessage(ChatColor.GREEN + "Удалено спавнеров из базы: " + removed + ". Осталось: " + spawnerList.size());
                    return true;
                }

                player.sendMessage(ChatColor.RED + "Неизвестное действие. Доступно: addall, deleteall.");
                return true;
            }
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

                if (setting.equals("limitmobs")) {
                    plugin.getConfig().set("spawn-control.settings.limitmobs", value);
                    plugin.saveConfig();
                    player.sendMessage(ChatColor.GREEN + "Лимит мобов (limitmobs) установлен на " + value + ".");
                    return true;
                }

                if (setting.equals("limitradius")) {
                    plugin.getConfig().set("spawn-control.settings.limitradius", value);
                    plugin.saveConfig();
                    // Перезапуск не нужен, так как мы читаем это значение каждый тик
                    player.sendMessage(ChatColor.GREEN + "Радиус подсчета мобов (limitradius) установлен на " + value + " блоков.");
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