package com.mk.mksurvival.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static Component color(String message) {
        return MessageUtils.parse(message);
    }

    public static List<Component> color(List<String> messages) {
        List<Component> coloredMessages = new ArrayList<>();
        for (String message : messages) {
            coloredMessages.add(MessageUtils.parse(message));
        }
        return coloredMessages;
    }

    public static void sendMessage(Player player, Component message) {
        MessageUtils.sendMessage(player, message);
    }

    public static void sendMessage(Player player, String message) {
        MessageUtils.sendMessage(player, message);
    }

    public static void sendTitle(Player player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        MessageUtils.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    public static String formatNumber(double number) {
        if (number >= 1000000000) {
            return String.format("%.2fB", number / 1000000000);
        } else if (number >= 1000000) {
            return String.format("%.2fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.2fK", number / 1000);
        } else {
            return String.format("%.2f", number);
        }
    }

    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
}