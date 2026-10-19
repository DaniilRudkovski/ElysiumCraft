package com.banan.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElysiumTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // 1. Первый аргумент
        if (args.length == 1) {
            completions.add("spawn");
        }

        // 2. Второй аргумент (после /elysium spawn)
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            // Добавляем enable и disable
            completions.addAll(Arrays.asList("enable", "disable"));
            
            // Добавляем имена миров
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                completions.add(world.getName());
            }
        }

        // 3. Третий аргумент (после /elysium spawn <мир>)
        if (args.length == 3 && args[0].equalsIgnoreCase("spawn")) {
            // Если ввели enable или disable, третий аргумент не нужен
            if (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable")) {
                return new ArrayList<>();
            }
            completions.addAll(Arrays.asList("init", "natural", "spawner", "trial"));
        }

        // 4. Четвертый аргумент (после /elysium spawn <мир> <тип>)
        if (args.length == 4 && args[0].equalsIgnoreCase("spawn")) {
            if (!args[2].equalsIgnoreCase("init")) {
                completions.addAll(Arrays.asList("on", "off"));
            }
        }

        // Фильтрация
        String lastArg = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(lastArg));

        return completions;
    }
}