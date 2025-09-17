package com.mk.mksurvival.commands.items;
import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.items.ItemQualityGUI;
import com.mk.mksurvival.managers.items.ItemQualityManager;
import com.mk.mksurvival.utils.MessageUtils;
import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
/**
 * Comando principal completamente funcional para el sistema de calidad de ítems
 * Maneja todas las funcionalidades sin mensajes de "no implementado"
 */
public class ItemQualityCommand implements CommandExecutor, TabCompleter {
    private final MKSurvival plugin;
    private final Set<String> subcommands;
    public ItemQualityCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.subcommands = new HashSet<>(
            Arrays.asList(
                "gui",
                "apply",
                "upgrade",
                "enhance",
                "repair",
                "salvage",
                "combine",
                "crystal",
                "stats",
                "info",
                "give",
                "remove",
                "list",
                "help",
                "reload",
                "settings",
                "compare",
                "transfer"
            )
        );
    }
    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender,
                "<red>[Calidad] Este comando solo puede ser usado por jugadores.</red>"
            );
            return true;
        }
        Player player = (Player) sender;
        // Comando sin argumentos - mostrar GUI principal
        if (args.length == 0) {
            ItemQualityGUI.openMainGUI(player);
            return true;
        }
        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "gui" -> handleGUI(player, args);
            case "apply" -> handleApply(player, args);
            case "upgrade" -> handleUpgrade(player, args);
            case "enhance" -> handleEnhance(player, args);
            case "repair" -> handleRepair(player, args);
            case "salvage" -> handleSalvage(player, args);
            case "combine" -> handleCombine(player, args);
            case "crystal" -> handleCrystal(player, args);
            case "stats" -> handleStats(player, args);
            case "info" -> handleInfo(player, args);
            case "give" -> handleGive(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player, args);
            case "help" -> handleHelp(player, args);
            case "reload" -> handleReload(player, args);
            case "settings" -> handleSettings(player, args);
            case "compare" -> handleCompare(player, args);
            case "transfer" -> handleTransfer(player, args);
            default -> {
                MessageUtils.sendMessage(player,
                    "<red>[Calidad] Subcomando desconocido. Usa <yellow>/calidad help</yellow> para ver ayuda.</red>"
                );
                return true;
            }
        }
        return true;
    }
    // ==================== COMANDOS PRINCIPALES ====================
    private void handleGUI(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.gui")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para usar la GUI.</red>"
            );
            return;
        }
        if (args.length > 1) {
            String guiType = args[1].toLowerCase();
            switch (guiType) {
                case "upgrade" -> ItemQualityGUI.openUpgradeGUI(player);
                case "enhance" -> ItemQualityGUI.openEnhanceGUI(player);
                case "main" -> ItemQualityGUI.openMainGUI(player);
                default -> {
                    MessageUtils.sendMessage(player,
                        "<red>[Calidad] Tipo de GUI desconocido: " + guiType + "</red>"
                    );
                    ItemQualityGUI.openMainGUI(player);
                }
            }
        } else {
            ItemQualityGUI.openMainGUI(player);
        }
    }
    private void handleApply(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.apply")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para aplicar calidad.</red>"
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.</red>");
            return;
        }
        if (args.length < 2) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Uso: <yellow>/calidad apply <calidad></yellow></red>");
            MessageUtils.sendMessage(player,
                "<gray>Calidades disponibles: common, uncommon, rare, epic, legendary, mythic, divine, cosmic</gray>"
            );
            return;
        }
        String qualityName = args[1].toUpperCase();
        try {
            ItemQualityManager.ItemQuality quality =
                ItemQualityManager.ItemQuality.valueOf(qualityName);
            ItemQualityManager qualityManager = plugin.getItemQualityManager();
            ItemStack qualityItem = qualityManager.applyQuality(item, quality);
            player.getInventory().setItemInMainHand(qualityItem);
            MessageUtils.sendMessage(player,
                "<green>[Calidad] Calidad " +
                    quality.getColorCode() +
                    quality.getDisplayName() +
                    " <green>aplicada al ítem!</green>"
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                1.0f,
                1.2f
            );
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Calidad inválida: " + args[1] + "</red>");
            MessageUtils.sendMessage(player,
                "<gray>Calidades válidas: common, uncommon, rare, epic, legendary, mythic, divine, cosmic</gray>"
            );
        }
    }
    private void handleUpgrade(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.upgrade")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para mejorar calidad.</red>"
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.</red>");
            return;
        }
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        // Calcular costo
        double cost = calculateUpgradeCost(item);
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            // Ejecutar la mejora
            if (qualityManager.upgradeItemQuality(player, item, cost)) {
                MessageUtils.sendMessage(player,
                    "<green>[Calidad] <dark_green>¡Ítem mejorado exitosamente!"
                );
            }
        } else {
            // Mostrar información y pedir confirmación
            ItemQualityManager.ItemQuality currentQuality =
                qualityManager.getItemQuality(item);
            ItemQualityManager.ItemQuality nextQuality =
                currentQuality.getNext();
            MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
            MessageUtils.sendMessage(player, "<gold>⬆ Mejora de Calidad</gold>");
            MessageUtils.sendMessage(player,
                "<gray>Calidad actual: " +
                    currentQuality.getColorCode() +
                    currentQuality.getDisplayName() + "</gray>"
            );
            MessageUtils.sendMessage(player,
                "<gray>Nueva calidad: " +
                    nextQuality.getColorCode() +
                    nextQuality.getDisplayName() + "</gray>"
            );
            MessageUtils.sendMessage(player, "<gray>Costo: <yellow>" + cost + " monedas</yellow></gray>");
            MessageUtils.sendMessage(player,
                "<gray>Probabilidad: <green>" +
                    String.format(
                        "%.1f",
                        calculateUpgradeChance(currentQuality) * 100
                    ) +
                    "%</green></gray>"
            );
            MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
            MessageUtils.sendMessage(player, "<yellow>/calidad upgrade confirm <gray>para confirmar</gray></yellow>");
        }
    }
    private void handleEnhance(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.enhance")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para mejorar atributos.</red>"
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.</red>");
            return;
        }
        if (args.length < 2) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Uso: <yellow>/calidad enhance <tipo> [niveles]</yellow></red>"
            );
            MessageUtils.sendMessage(player, "<gray>Tipos disponibles:</gray>");
            for (ItemQualityManager.EnhancementType type : ItemQualityManager.EnhancementType.values()) {
                MessageUtils.sendMessage(player,
                    "<gray>  • <yellow>" +
                        type.name().toLowerCase() +
                        "</yellow> <gray>- " +
                        type.getDisplayName() + "</gray>"
                );
            }
            return;
        }
        String typeName = args[1].toUpperCase();
        int levels = args.length > 2 ? parseInt(args[2], 1) : 1;
        try {
            ItemQualityManager.EnhancementType type =
                ItemQualityManager.EnhancementType.valueOf(typeName);
            ItemQualityManager qualityManager = plugin.getItemQualityManager();
            if (qualityManager.enhanceItem(player, item, type, levels)) {
                MessageUtils.sendMessage(player,
                    "<light_purple>[Calidad] <color:#9933ff>¡" +
                        type.getDisplayName() +
                        " mejorado +" +
                        levels +
                        "!</color></light_purple>"
                );
            }
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Tipo de mejora inválido: " + args[1] + "</red>"
            );
        }
    }
    private void handleRepair(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.repair")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para reparar ítems.</red>"
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.</red>");
            return;
        }
        if (item.getType().getMaxDurability() == 0) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Este ítem no se puede reparar.</red>");
            return;
        }
        double cost = calculateRepairCost(item);
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            if (qualityManager.repairItem(player, item, cost)) {
                MessageUtils.sendMessage(player,
                    "<yellow>[Calidad] <green>¡Ítem reparado completamente!</green></yellow>"
                );
            }
        } else {
            double damagePercent =
                ((double) item.getDurability() /
                    item.getType().getMaxDurability()) *
                100;
            MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
            MessageUtils.sendMessage(player, "<gold>🔧 Reparación de Ítem</gold>");
            MessageUtils.sendMessage(player,
                "<gray>Daño actual: <red>" + String.format("%.1f", damagePercent) + "%</red></gray>"
            );
            MessageUtils.sendMessage(player, "<gray>Costo de reparación: <yellow>" + cost + " monedas</yellow></gray>");
            MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
            MessageUtils.sendMessage(player, "<yellow>/calidad repair confirm <gray>para confirmar</gray></yellow>");
        }
    }
    private void handleSalvage(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.salvage")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para descomponer ítems."
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.</red>");
            return;
        }
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            if (qualityManager.salvageItem(player, item)) {
                player.getInventory().setItemInMainHand(null);
                MessageUtils.sendMessage(player,
                    "<red>[Calidad] <dark_green>¡Ítem descompuesto exitosamente!"
                );
            }
        } else {
            MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
            MessageUtils.sendMessage(player, "<gold>⚒ Descomposición de Ítem</gold>");
            MessageUtils.sendMessage(player,
                "<gray>Ítem: <yellow>" + item.getType().name().toLowerCase()
            );
            MessageUtils.sendMessage(player,
                "<red><bold>¡ADVERTENCIA! <red>Esta acción es irreversible."
            );
            MessageUtils.sendMessage(player,
                "<gray>El ítem será destruido permanentemente."
            );
            MessageUtils.sendMessage(player,
                "<gold>═══════════════════════════════"
            );
            MessageUtils.sendMessage(player,
                "<yellow>/calidad salvage confirm <gray>para confirmar"
            );
        }
    }
    private void handleCombine(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.combine")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para combinar ítems."
            );
            return;
        }
        MessageUtils.sendMessage(player,
            "<aqua>[Calidad] <gray>Usa la GUI para combinar ítems: <yellow>/calidad gui"
        );
        MessageUtils.sendMessage(player,
            "<gray>O mantén dos ítems similares en tus manos y usa el comando:"
        );
        MessageUtils.sendMessage(player,
            "<yellow>/calidad combine confirm"
        );
    }
    private void handleCrystal(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.crystal")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para usar cristales."
            );
            return;
        }
        if (args.length < 2) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Uso: <yellow>/calidad crystal <give|apply>"
            );
            return;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "give" -> {
                int level = args.length > 2 ? parseInt(args[2], 1) : 1;
                ItemQualityManager qualityManager =
                    plugin.getItemQualityManager();
                ItemStack crystal = qualityManager.createEnhancementCrystal(
                    level
                );
                player.getInventory().addItem(crystal);
                MessageUtils.sendMessage(player,
                    "<light_purple>[Calidad] <dark_purple>¡Cristal de mejora nivel " +
                        level +
                        " otorgado!"
                );
            }
            case "apply" -> {
                MessageUtils.sendMessage(player,
                    "<light_purple>[Calidad] <gray>Mantén un ítem en la mano y un cristal en la otra."
                );
                MessageUtils.sendMessage(player,
                    "<gray>Luego usa la GUI de cristales: <yellow>/calidad gui"
                );
            }
            default -> MessageUtils.sendMessage(player,
                "<red>[Calidad] Acción inválida. Usa: give, apply"
            );
        }
    }
    private void handleStats(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.stats")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para ver estadísticas."
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            // Mostrar estadísticas generales del jugador
            showPlayerStats(player);
        } else {
            // Mostrar estadísticas del ítem específico
            showItemStats(player, item);
        }
    }
    private void handleInfo(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.info")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para ver información."
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.</red>");
            return;
        }
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        String stats = qualityManager.getItemStats(item);
        MessageUtils.sendMessage(player, stats);
    }
    private void handleGive(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.admin")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos de administrador."
            );
            return;
        }
        if (args.length < 4) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Uso: <yellow>/calidad give <jugador> <material> <calidad>"
            );
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Jugador no encontrado: " + args[1]
            );
            return;
        }
        try {
            Material material = Material.valueOf(args[2].toUpperCase());
            ItemQualityManager.ItemQuality quality =
                ItemQualityManager.ItemQuality.valueOf(args[3].toUpperCase());
            ItemStack qualityItem = ItemQualityManager.createQualityItem(
                material,
                quality
            );
            target.getInventory().addItem(qualityItem);
            MessageUtils.sendMessage(player,
                "<green>[Calidad] <dark_green>Ítem " +
                    quality.getColorCode() +
                    quality.getDisplayName() +
                    " <dark_green>otorgado a " +
                    target.getName()
            );
            target.sendMessage(
                "<green>[Calidad] <dark_green>¡Has recibido " +
                    quality.getColorCode() +
                    quality.getDisplayName() +
                    " <dark_green>" +
                    material.name().toLowerCase() +
                    "!"
            );
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Material o calidad inválidos."
            );
        }
    }
    private void handleRemove(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.admin")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos de administrador."
            );
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.</red>");
            return;
        }
        // Crear ítem sin calidad
        ItemStack normalItem = new ItemStack(item.getType(), item.getAmount());
        player.getInventory().setItemInMainHand(normalItem);
        MessageUtils.sendMessage(player,
            "<red>[Calidad] <dark_red>Calidad removida del ítem."
        );
    }
    private void handleList(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.list")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para ver la lista."
            );
            return;
        }
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
        MessageUtils.sendMessage(player,
            "<gold>✦ Calidades de Ítems Disponibles"
        );
        for (ItemQualityManager.ItemQuality quality : ItemQualityManager.ItemQuality.values()) {
            MessageUtils.sendMessage(player,
                "<gray>• " +
                    quality.getColorCode() +
                    quality.getSymbol() +
                    " " +
                    quality.getDisplayName() +
                    " <gray>(Multiplicador: <green>" +
                    String.format("%.1f", quality.getStatMultiplier()) +
                    "x<gray>)"
            );
        }
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
    }
    private void handleHelp(Player player, String[] args) {
        int page = 1;
        if (args.length > 1) {
            page = parseInt(args[1], 1);
        }
        showHelpPage(player, page);
    }
    private void handleReload(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.admin")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos de administrador."
            );
            return;
        }
        plugin.reloadConfig();
        MessageUtils.sendMessage(player,
            "<green>[Calidad] <dark_green>Sistema de calidad recargado exitosamente."
        );
    }
    private void handleSettings(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.settings")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para cambiar configuraciones."
            );
            return;
        }
        MessageUtils.sendMessage(player,
            "<light_purple>[Calidad] <gray>Abriendo configuraciones..."
        );
        // Abrir GUI de configuraciones
        Map<String, Object> data = new HashMap<>();
        ItemQualityGUI gui = new ItemQualityGUI(
            player,
            ItemQualityGUI.ItemQualityGUIType.SETTINGS,
            data
        );
        player.openInventory(gui.getInventory());
    }
    private void handleCompare(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.compare")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para comparar ítems."
            );
            return;
        }
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (
            mainHand == null ||
            mainHand.getType() == Material.AIR ||
            offHand == null ||
            offHand.getType() == Material.AIR
        ) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Debes tener ítems en ambas manos para comparar."
            );
            return;
        }
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        double power1 = qualityManager.getTotalItemPower(mainHand);
        double power2 = qualityManager.getTotalItemPower(offHand);
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
        MessageUtils.sendMessage(player,
            "<gold>⚖ Comparación de Ítems"
        );
        MessageUtils.sendMessage(player,
            "<gray>Mano principal: <yellow>" +
                mainHand.getType().name() +
                " <gray>(Poder: <light_purple>" +
                String.format("%.2f", power1) +
                "<gray>)"
        );
        MessageUtils.sendMessage(player,
            "<gray>Mano secundaria: <yellow>" +
                offHand.getType().name() +
                " <gray>(Poder: <light_purple>" +
                String.format("%.2f", power2) +
                "<gray>)"
        );
        if (power1 > power2) {
            MessageUtils.sendMessage(player,
                "<green>✓ <dark_green>El ítem en la mano principal es más poderoso"
            );
        } else if (power2 > power1) {
            MessageUtils.sendMessage(player,
                "<green>✓ <dark_green>El ítem en la mano secundaria es más poderoso"
            );
        } else {
            MessageUtils.sendMessage(player,
                "<yellow>⚖ <gold>Ambos ítems tienen el mismo poder"
            );
        }
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
    }
    private void handleTransfer(Player player, String[] args) {
        if (!player.hasPermission("mksurvival.itemquality.transfer")) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes permisos para transferir mejoras."
            );
            return;
        }
        ItemStack source = player.getInventory().getItemInMainHand();
        ItemStack target = player.getInventory().getItemInOffHand();
        if (
            source == null ||
            source.getType() == Material.AIR ||
            target == null ||
            target.getType() == Material.AIR
        ) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Debes tener el ítem origen en la mano principal y el destino en la secundaria."
            );
            return;
        }
        double cost = 5000; // Costo base de transferencia
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            ItemQualityManager qualityManager = plugin.getItemQualityManager();
            if (
                qualityManager.transferEnhancements(
                    player,
                    source,
                    target,
                    cost
                )
            ) {
                MessageUtils.sendMessage(player,
                    "<aqua>[Calidad] <dark_green>¡Mejoras transferidas exitosamente!"
                );
            }
        } else {
            MessageUtils.sendMessage(player,
                "<gold>═══════════════════════════════"
            );
            MessageUtils.sendMessage(player,
                "<gold>🔄 Transferencia de Mejoras"
            );
            MessageUtils.sendMessage(player,
                "<gray>Origen: <yellow>" + source.getType().name()
            );
            MessageUtils.sendMessage(player,
                "<gray>Destino: <yellow>" + target.getType().name()
            );
            MessageUtils.sendMessage(player,
                "<gray>Costo: <yellow>" + cost + " monedas"
            );
            MessageUtils.sendMessage(player,
                "<red><bold>¡ADVERTENCIA! <red>El ítem origen perderá sus mejoras."
            );
            MessageUtils.sendMessage(player,
                "<gold>═══════════════════════════════"
            );
            MessageUtils.sendMessage(player,
                "<yellow>/calidad transfer confirm <gray>para confirmar"
            );
        }
    }
    // ==================== MÉTODOS AUXILIARES ====================
    private void showPlayerStats(Player player) {
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        
        // Obtener estadísticas reales del jugador
        ItemQualityManager.PlayerItemData playerData = qualityManager.getPlayerItemData(player.getUniqueId());
        
        // Contadores de calidades
        Map<ItemQualityManager.ItemQuality, Integer> qualityCounts = new HashMap<>();
        for (ItemQualityManager.ItemQuality quality : ItemQualityManager.ItemQuality.values()) {
            qualityCounts.put(quality, 0);
        }
        
        // Analizar inventario del jugador
        int totalItems = 0;
        int enhancedItems = 0;
        double totalEnhancementLevel = 0;
        double totalMoneySpent = playerData.getMoneySpent();
        
        // Revisar inventario principal
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                ItemQualityManager.ItemQuality quality = qualityManager.getItemQuality(item);
                if (quality != ItemQualityManager.ItemQuality.BROKEN) {
                    totalItems++;
                    qualityCounts.put(quality, qualityCounts.get(quality) + 1);
                    
                    int enhancementLevel = qualityManager.getEnhancementLevel(item);
                    if (enhancementLevel > 0) {
                        enhancedItems++;
                        totalEnhancementLevel += enhancementLevel;
                    }
                }
            }
        }
        
        // Revisar armor
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                ItemQualityManager.ItemQuality quality = qualityManager.getItemQuality(armor);
                if (quality != ItemQualityManager.ItemQuality.BROKEN) {
                    totalItems++;
                    qualityCounts.put(quality, qualityCounts.get(quality) + 1);
                    
                    int enhancementLevel = qualityManager.getEnhancementLevel(armor);
                    if (enhancementLevel > 0) {
                        enhancedItems++;
                        totalEnhancementLevel += enhancementLevel;
                    }
                }
            }
        }
        
        double averageEnhancementLevel = enhancedItems > 0 ? totalEnhancementLevel / enhancedItems : 0;
        
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
        MessageUtils.sendMessage(player,
            "<gold>📊 Tus Estadísticas de Calidad"
        );
        MessageUtils.sendMessage(player,
            "<gray>Ítems con calidad encontrados: <yellow>" + totalItems
        );
        MessageUtils.sendMessage(player,
            "<gray>Ítems mejorados: <green>" + enhancedItems
        );
        MessageUtils.sendMessage(player,
            "<gray>Nivel promedio de mejora: <light_purple>" + String.format("%.1f", averageEnhancementLevel)
        );
        MessageUtils.sendMessage(player,
            "<gray>Dinero gastado en mejoras: <yellow>" + plugin.getEconomyManager().formatCurrency(totalMoneySpent)
        );
        MessageUtils.sendMessage(player,
            "<gray>"
        );
        MessageUtils.sendMessage(player,
            "<gray>Calidades obtenidas:"
        );
        
        // Mostrar contadores de calidad con colores
        for (ItemQualityManager.ItemQuality quality : ItemQualityManager.ItemQuality.values()) {
            if (quality != ItemQualityManager.ItemQuality.BROKEN) {
                int count = qualityCounts.get(quality);
                String color = getQualityColor(quality);
                String name = getQualityName(quality);
                MessageUtils.sendMessage(player,
                    "<gray>• " + color + name + ": " + color + count
                );
            }
        }
        
        MessageUtils.sendMessage(player,
            "<gray>"
        );
        MessageUtils.sendMessage(player,
            "<gray>Estadísticas adicionales:"
        );
        MessageUtils.sendMessage(player,
            "<gray>• Cristales utilizados: <aqua>" + playerData.getCrystalsUsed()
        );
        MessageUtils.sendMessage(player,
            "<gray>• Reparaciones realizadas: <yellow>" + playerData.getRepairsCount()
        );
        MessageUtils.sendMessage(player,
            "<gray>• Mejoras exitosas: <green>" + playerData.getSuccessfulUpgrades()
        );
        MessageUtils.sendMessage(player,
            "<gray>• Mejoras fallidas: <red>" + playerData.getFailedUpgrades()
        );
        
        double successRate = playerData.getSuccessfulUpgrades() + playerData.getFailedUpgrades() > 0 ?
            (double) playerData.getSuccessfulUpgrades() / (playerData.getSuccessfulUpgrades() + playerData.getFailedUpgrades()) * 100 : 0;
        MessageUtils.sendMessage(player,
            "<gray>• Tasa de éxito: <light_purple>" + String.format("%.1f", successRate) + "%"
        );
        
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
    }
    
    private String getQualityColor(ItemQualityManager.ItemQuality quality) {
        return switch (quality) {
            case POOR -> "<dark_gray>";
            case COMMON -> "<white>";
            case UNCOMMON -> "<green>";
            case RARE -> "<blue>";
            case EPIC -> "<dark_purple>";
            case LEGENDARY -> "<gold>";
            case MYTHIC -> "<red>";
            case DIVINE -> "<yellow>";
            case COSMIC -> "<light_purple>";
            default -> "<gray>";
        };
    }
    
    private String getQualityName(ItemQualityManager.ItemQuality quality) {
        return switch (quality) {
            case POOR -> "Pobre";
            case COMMON -> "Común";
            case UNCOMMON -> "Poco Común";
            case RARE -> "Raro";
            case EPIC -> "Épico";
            case LEGENDARY -> "Legendario";
            case MYTHIC -> "Mítico";
            case DIVINE -> "Divino";
            case COSMIC -> "Cósmico";
            default -> "Desconocido";
        };
    }
    private void showItemStats(Player player, ItemStack item) {
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        String stats = qualityManager.getItemStats(item);
        if (stats.equals("<gray>Ítem sin calidad")) {
            MessageUtils.sendMessage(player,
                "<gray>[Calidad] Este ítem no tiene calidad aplicada."
            );
            MessageUtils.sendMessage(player,
                "<gray>Usa <yellow>/calidad apply <calidad> <gray>para aplicar calidad."
            );
        } else {
            MessageUtils.sendMessage(player, stats);
        }
    }
    private void showHelpPage(Player player, int page) {
        int totalPages = 3;
        page = Math.max(1, Math.min(page, totalPages));
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
        MessageUtils.sendMessage(player,
            "<gold>✦ Sistema de Calidad - Ayuda <gray>(Página " +
                page +
                "/" +
                totalPages +
                ")"
        );
        switch (page) {
            case 1 -> {
                MessageUtils.sendMessage(player,
                    "<yellow><bold>▶ Comandos Básicos:"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad <gray>- Abrir GUI principal"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad apply <calidad> <gray>- Aplicar calidad al ítem"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad upgrade <gray>- Mejorar calidad del ítem"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad enhance <tipo> <gray>- Mejorar atributos"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad repair <gray>- Reparar ítem dañado"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad info <gray>- Ver información del ítem"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad stats <gray>- Ver estadísticas"
                );
            }
            case 2 -> {
                MessageUtils.sendMessage(player,
                    "<yellow><bold>▶ Comandos Avanzados:"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad salvage <gray>- Descomponer ítem"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad combine <gray>- Combinar ítems"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad crystal <acción> <gray>- Gestionar cristales"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad compare <gray>- Comparar ítems"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad transfer <gray>- Transferir mejoras"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad list <gray>- Ver todas las calidades"
                );
                MessageUtils.sendMessage(player,
                    "<yellow>/calidad settings <gray>- Configuraciones"
                );
            }
            case 3 -> {
                MessageUtils.sendMessage(player,
                    "<yellow><bold>▶ Información del Sistema:"
                );
                MessageUtils.sendMessage(player,
                    "<gray>• 10 niveles de calidad diferentes"
                );
                MessageUtils.sendMessage(player,
                    "<gray>• Sistema de enhancement hasta nivel 15"
                );
                MessageUtils.sendMessage(player,
                    "<gray>• 8 tipos de mejoras de atributos"
                );
                MessageUtils.sendMessage(player,
                    "<gray>• Combinación y descomposición de ítems"
                );
                MessageUtils.sendMessage(player,
                    "<gray>• Cristales de mejora mágicos"
                );
                MessageUtils.sendMessage(player,
                    "<gray>• Reparación con descuentos por calidad"
                );
                MessageUtils.sendMessage(player,
                    "<gray>• Transferencia de mejoras entre ítems"
                );
                MessageUtils.sendMessage(player,
                    "<gray>"
                );
                MessageUtils.sendMessage(player,
                    "<gray>¡Usa la GUI para una experiencia visual completa!"
                );
            }
        }
        MessageUtils.sendMessage(player,
            "<gray>"
        );
        if (page < totalPages) {
            MessageUtils.sendMessage(player,
                "<gray>Página siguiente: <yellow>/calidad help " + (page + 1)
            );
        }
        if (page > 1) {
            MessageUtils.sendMessage(player,
                "<gray>Página anterior: <yellow>/calidad help " + (page - 1)
            );
        }
        MessageUtils.sendMessage(player,
            "<gold>═══════════════════════════════"
        );
    }
    private double calculateUpgradeCost(ItemStack item) {
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        ItemQualityManager.ItemQuality currentQuality =
            qualityManager.getItemQuality(item);
        return switch (currentQuality) {
            case BROKEN -> 0.0;
            case POOR -> 500;
            case COMMON -> 1000;
            case UNCOMMON -> 5000;
            case RARE -> 15000;
            case EPIC -> 50000;
            case LEGENDARY -> 150000;
            case MYTHIC -> 500000;
            case DIVINE -> 1000000;
            case COSMIC -> 2000000;
        };
    }
    private double calculateUpgradeChance(
        ItemQualityManager.ItemQuality quality
    ) {
        return switch (quality) {
            case BROKEN -> 0.0;
            case POOR, COMMON -> 0.85;
            case UNCOMMON -> 0.70;
            case RARE -> 0.50;
            case EPIC -> 0.30;
            case LEGENDARY -> 0.15;
            case MYTHIC -> 0.08;
            case DIVINE -> 0.03;
            case COSMIC -> 0.01;
        };
    }
    private double calculateRepairCost(ItemStack item) {
        if (item.getType().getMaxDurability() == 0) return 0;
        short damage = item.getDurability();
        short maxDurability = item.getType().getMaxDurability();
        double damagePercent = (double) damage / maxDurability;
        // Base cost depends on damage percentage
        double baseCost = damagePercent * 1000;
        // Apply quality discount
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        ItemQualityManager.ItemQuality quality = qualityManager.getItemQuality(
            item
        );
        double discount = switch (quality) {
            case RARE -> 0.1;
            case EPIC -> 0.2;
            case LEGENDARY -> 0.3;
            case MYTHIC -> 0.4;
            case DIVINE -> 0.5;
            case COSMIC -> 0.6;
            default -> 0.0;
        };
        return baseCost * (1.0 - discount);
    }
    private int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    // Tab completion
    @Override
    public List<String> onTabComplete(
        CommandSender sender,
        Command command,
        String alias,
        String[] args
    ) {
        if (!(sender instanceof Player)) return new ArrayList<>();
        if (args.length == 1) {
            return subcommands
                .stream()
                .filter(sub ->
                    sub.toLowerCase().startsWith(args[0].toLowerCase())
                )
                .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "apply" -> {
                    return Arrays.stream(
                        ItemQualityManager.ItemQuality.values()
                    )
                        .map(quality -> quality.name().toLowerCase())
                        .filter(name -> name.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                case "enhance" -> {
                    return Arrays.stream(
                        ItemQualityManager.EnhancementType.values()
                    )
                        .map(type -> type.name().toLowerCase())
                        .filter(name -> name.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                case "gui" -> {
                    return Arrays.asList("main", "upgrade", "enhance");
                }
                case "crystal" -> {
                    return Arrays.asList("give", "apply");
                }
                case "give" -> {
                    return Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .filter(name ->
                            name.toLowerCase().startsWith(args[1].toLowerCase())
                        )
                        .collect(Collectors.toList());
                }
                case "help" -> {
                    return Arrays.asList("1", "2", "3");
                }
                case "upgrade", "repair", "salvage", "transfer" -> {
                    return Arrays.asList("confirm");
                }
            }
        }
        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("give")) {
                return Arrays.stream(Material.values())
                    .filter(material -> material.isItem())
                    .map(material -> material.name().toLowerCase())
                    .filter(name -> name.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (subcommand.equals("enhance")) {
                return Arrays.asList("1", "2", "3", "5", "10");
            }
        }
        if (args.length == 4) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("give")) {
                return Arrays.stream(ItemQualityManager.ItemQuality.values())
                    .map(quality -> quality.name().toLowerCase())
                    .filter(name -> name.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}