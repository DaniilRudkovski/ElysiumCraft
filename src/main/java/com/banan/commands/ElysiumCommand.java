package com.banan.commands;

import com.banan.core.ElysiumController;
import com.banan.controllers.SpawnController;
import com.banan.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            sender.sendMessage("Эта команда только для игроков.");
            return true;
        }

        // 1. Если ввели просто /elysium (без аргументов) -> показываем ГЛАВНОЕ меню
        if (args.length == 0) {
            player.sendMessage(Component.text("=== Elysium Controller ===").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("Нажми на модуль, чтобы настроить:").color(NamedTextColor.GRAY));
            MessageUtils.sendClickableButton(player, "<yellow>[ Контроль спавна ]</yellow>", "Нажми, чтобы открыть настройки спавна", "/elysium menu spawn");
            return true;
        }

        // 2. Если ввели /elysium menu spawn -> показываем МЕНЮ СПАВНА
        if (args[0].equalsIgnoreCase("menu") && args.length > 1 && args[1].equalsIgnoreCase("spawn")) {
            player.sendMessage(Component.text("=== Настройки спавна ===").color(NamedTextColor.GOLD));
            
            SpawnController spawnController = plugin.getSpawnController();
            boolean isEnabled = spawnController.isEnabled();
            
            String toggleText = isEnabled ? "<red>[ ВЫКЛЮЧИТЬ ]</red>" : "<green>[ ВКЛЮЧИТЬ ]</green>";
            String toggleHover = isEnabled ? "Выключить модуль" : "Включить модуль";
            
            MessageUtils.sendClickableButton(player, toggleText, toggleHover, "/elysium spawn");
            MessageUtils.sendClickableButton(player, "<yellow>[ Настроить правила ]</yellow>", "Нажми, чтобы настроить правила", "/elysium menu spawn rules");
            return true;
        }

        // 3. Если ввели /elysium spawn (прямое включение/выключение)
        if (args[0].equalsIgnoreCase("spawn")) {
            SpawnController spawnController = plugin.getSpawnController();
            
            if (!spawnController.isEnabled()) {
                spawnController.enable();
                player.sendMessage(Component.text("Модуль контроля спавна ВКЛЮЧЕН.").color(NamedTextColor.GREEN));
            } else {
                spawnController.disable();
                player.sendMessage(Component.text("Модуль контроля спавна ВЫКЛЮЧЕН.").color(NamedTextColor.RED));
            }
            return true;
        }

        // 4. Если ввели что-то неизвестное
        player.sendMessage(Component.text("Неизвестная команда.").color(NamedTextColor.RED));
        return true;
    }
}