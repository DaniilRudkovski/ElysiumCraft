package com.banan.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    public void render(Player player, NodeEnum node) {
        List<String> messages = new ArrayList<>();

        // 1. Заголовок (цвета через &)
        messages.add("&6=== " + node.desc + " ===");

        // 2. Дочерние узлы
        if (node.underNodes != null) {
            for (NodeEnum child : node.underNodes) {
                String command = "/elysium menu " + child.name().toLowerCase();
                String color = (child.underNodes != null) ? "&e" : "&a";

                // Формируем текст кнопки (с квадратными скобками)
                String buttonText = color + "[" + child.desc + "]";

                // Отправляем кнопку (через Bungee API, который мы оставили в MessageUtils)
                MessageUtils.sendClickableButton(player, buttonText, "Нажми, чтобы открыть", command);
                
                // Если это переключатель (тип 2), добавляем кнопку переключения
                if (child.nodeType == 2) {
                    String toggleCommand = "/elysium toggle " + child.name().toLowerCase();
                    MessageUtils.sendClickableButton(player, "&b[ Переключить ]", "Нажми, чтобы изменить", toggleCommand);
                }
            }
        }

        // 3. Кнопка Назад
        if (node.uppernode != null) {
            MessageUtils.sendClickableButton(player, 
                "&c[ Назад ]", 
                "Вернуться в " + node.uppernode.desc, 
                "/elysium menu " + node.uppernode.name().toLowerCase()
            );
        }
    }

    public NodeEnum getNodeByName(String name) {
        for (NodeEnum node : NodeEnum.values()) {
            if (node.name().equalsIgnoreCase(name)) {
                return node;
            }
        }
        return null;
    }
}