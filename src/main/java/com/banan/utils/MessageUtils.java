package com.banan.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MessageUtils {

    // UUID больше не используем, чтобы не было ошибок
    public static final UUID MENU_ID = UUID.randomUUID();

    // Отправка обычного текста
    public static void sendMenu(Player player, String... messages) {
        for (String msg : messages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    // Отправка кликабельной кнопки через Bungee API (работает без ошибок)
    public static void sendClickableButton(Player player, String text, String hoverText, String command) {
        String legacyText = ChatColor.translateAlternateColorCodes('&', text);
        String legacyHover = ChatColor.translateAlternateColorCodes('&', hoverText);

        TextComponent button = new TextComponent(TextComponent.fromLegacyText(legacyText));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(legacyHover).create()));
        
        // Отправляем через Bungee API
        player.spigot().sendMessage(button);
    }
}