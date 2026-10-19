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

            if (!type.equals("natural") && !type.equals("spawner") && !type.equals("trial")) {
                player.sendMessage(ChatColor.RED + "Неизвестный тип. Доступно: natural, spawner, trial.");
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