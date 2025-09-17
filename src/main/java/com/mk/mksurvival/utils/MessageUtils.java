package com.mk.mksurvival.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer legacySectionSerializer =
            LegacyComponentSerializer.legacySection();

    public static String parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        
        // Convertir códigos legacy (&c, §c) a MiniMessage antes de parsear
        String processedMessage = message;
        if (message.contains("&") || message.contains("§")) {
            processedMessage = legacyToMiniMessage(message);
        }
        
        return miniMessage.deserialize("<!italic>" + processedMessage);
    }

    public static @NotNull Component parse(String message, Player player) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String parsedMessage = message;
        // Verificar si PlaceholderAPI está disponible
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            parsedMessage = PlaceholderAPI.setPlaceholders(player, message);
        }
        
        // Convertir códigos legacy (&c, §c) a MiniMessage antes de parsear
        if (parsedMessage.contains("&") || parsedMessage.contains("§")) {
            parsedMessage = legacyToMiniMessage(parsedMessage);
        }
        
        return miniMessage.deserialize("<!italic>" + parsedMessage);
    }

    public static List<Component> parseList(List<String> messages) {
        return messages.stream()
                .map(MessageUtils::parse)
                .collect(Collectors.toList());
    }

    public static List<Component> parseList(List<String> messages, Player player) {
        return messages.stream()
                .map(message -> parse(message, player))
                .collect(Collectors.toList());
    }

    public static String legacyToMiniMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        String converted = convertLegacyColors(convertSectionColors(message));
        Component component = legacySerializer.deserialize(converted);
        return miniMessage.serialize(component);
    }

    public static String colorize(String message) {
        return miniMessage.serialize(parse(message));
    }

    public static String toLegacy(Component component) {
        return legacySectionSerializer.serialize(component);
    }

    public static String toLegacyString(Component component) {
        return legacySerializer.serialize(component);
    }

    public static String stripColor(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        Component component = parse(message);
        return toLegacy(component).replaceAll("§[0-9a-fk-or]", "");
    }

    public static boolean canUseColors(Player player) {
        return player.hasPermission("mksurvival.chat.colors");
    }

    public static String processChatMessage(Player player, String message) {
        if (canUseColors(player)) {
            String miniMessageFormat = legacyToMiniMessage(message);
            return miniMessage.serialize(parse(miniMessageFormat, player));
        } else {
            String cleanMessage = stripLegacyColors(message);
            // Verificar si PlaceholderAPI está disponible
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                return PlaceholderAPI.setPlaceholders(player, cleanMessage);
            } else {
                return cleanMessage;
            }
        }
    }

    private static String stripLegacyColors(String message) {
        return message.replaceAll("&[0-9a-fk-or]", "").replaceAll("§[0-9a-fk-or]", "");
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sendMessage(sender, parse(message));
    }

    public static void sendMessage(Player player, String message) {
        if (message == null || message.isEmpty()) return;
        sendMessage(player, parse(message, player));
    }
    
    public static void sendMessage(Player player, Component component) {
        if (component == null) return;
        Audience audience = (Audience) player;
        audience.sendMessage(component);
    }

    public static void sendMessage(CommandSender sender, Component component) {
        if (component == null) return;
        Audience audience = (Audience) sender;
        audience.sendMessage(component);
    }

    public static void sendMessages(Player player, List<String> messages) {
        messages.forEach(message -> sendMessage(player, message));
    }

    public static void sendMessages(CommandSender sender, List<String> messages) {
        messages.forEach(message -> sendMessage(sender, message));
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        sendTitle(player, 
            title != null ? parse(title, player) : Component.empty(),
            subtitle != null ? parse(subtitle, player) : Component.empty(),
            fadeIn, stay, fadeOut
        );
    }
    
    public static void sendTitle(Player player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        Audience audience = (Audience) player;
        
        Title.Times times = Title.Times.times(
            Duration.ofMillis(fadeIn * 50), // Convert ticks to milliseconds
            Duration.ofMillis(stay * 50),
            Duration.ofMillis(fadeOut * 50)
        );
        
        Title titleObj = Title.title(
            title != null ? title : Component.empty(),
            subtitle != null ? subtitle : Component.empty(),
            times
        );
        audience.showTitle(titleObj);
    }

    private static String convertLegacyColors(String message) {
        return message
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
    }

    private static String convertSectionColors(String message) {
        return message
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§k", "<obfuscated>")
                .replace("§l", "<bold>")
                .replace("§m", "<strikethrough>")
                .replace("§n", "<underlined>")
                .replace("§o", "<italic>")
                .replace("§r", "<reset>");
    }

    public static String stripColors(String questName) {
        return questName;
    }
    
    /**
     * Convierte códigos de color legacy (&c, §c) a formato que Minecraft puede mostrar
     * @param message El mensaje con códigos de color legacy
     * @return El mensaje con colores convertidos para display
     */
    public static String translateColorCodes(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return message.replace('&', '§');
    }
    
    /**
     * Convierte MiniMessage a legacy color codes para GUI/scoreboards
     * @param message El mensaje con formato MiniMessage
     * @return El mensaje con códigos legacy (§)
     */
    public static String toMinecraftFormat(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        // Si ya contiene códigos legacy, convertir & a §
        if (message.contains("&") && !message.contains("<")) {
            return translateColorCodes(message);
        }
        
        // Si contiene MiniMessage, convertir a legacy
        if (message.contains("<") && message.contains(">")) {
            try {
                Component component = parse(message);
                return toLegacy(component);
            } catch (Exception e) {
                // Fallback: conversión manual
                return message
                    .replace("<black>", "§0")
                    .replace("<dark_blue>", "§1")
                    .replace("<dark_green>", "§2")
                    .replace("<dark_aqua>", "§3")
                    .replace("<dark_red>", "§4")
                    .replace("<dark_purple>", "§5")
                    .replace("<gold>", "§6")
                    .replace("<gray>", "§7")
                    .replace("<dark_gray>", "§8")
                    .replace("<blue>", "§9")
                    .replace("<green>", "§a")
                    .replace("<aqua>", "§b")
                    .replace("<red>", "§c")
                    .replace("<light_purple>", "§d")
                    .replace("<yellow>", "§e")
                    .replace("<white>", "§f")
                    .replace("<bold>", "§l")
                    .replace("<italic>", "§o")
                    .replace("<underlined>", "§n")
                    .replace("<strikethrough>", "§m")
                    .replace("<obfuscated>", "§k")
                    .replace("<reset>", "§r");
            }
        }
        
        // Si no tiene códigos especiales, devolver tal como está
        return message;
    }
}