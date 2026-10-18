package com.banan.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class MessageUtils {

    // Отправляет кликабельную кнопку игроку (используя Adventure API)
    public static void sendClickableButton(Player player, String text, String hoverText, String command) {
        // MiniMessage позволяет использовать цвета прямо в тексте (например, <yellow>)
        // Но так как мы передаем обычные строки с ChatColor, мы переведем их в компоненты
        Component button = MiniMessage.miniMessage().deserialize(text.replace('§', '&'))
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(hoverText.replace('§', '&'))));

        player.sendMessage(button);
    }
}