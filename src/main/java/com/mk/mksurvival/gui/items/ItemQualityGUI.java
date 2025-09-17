package com.mk.mksurvival.gui.items;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.items.ItemQualityManager;
import com.mk.mksurvival.utils.MessageUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * GUI completamente funcional para el sistema de calidad de ítems
 * Incluye mejoras, transferencias, reparaciones y gestión completa
 */
public class ItemQualityGUI implements InventoryHolder {

    private final Player player;
    private final Inventory inventory;
    private final ItemQualityGUIType guiType;
    private final Map<String, Object> guiData;

    // Constantes de slots
    private static final int[] QUALITY_PREVIEW_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] ENHANCEMENT_SLOTS = {19, 20, 21, 28, 29, 30, 37, 38, 39};
    private static final int ITEM_INPUT_SLOT = 22;
    private static final int RESULT_SLOT = 24;
    private static final int MATERIAL_INPUT_SLOT = 40;

    public ItemQualityGUI(MKSurvival plugin, Player player, Inventory inventory, ItemQualityGUIType guiType, Map<String, Object> guiData) {
        this.player = player;
        this.inventory = inventory;
        this.guiType = guiType;
        this.guiData = guiData;
    }

    public void openQualityMainMenu(Player player) {
    }

    public enum ItemQualityGUIType {
        MAIN,           // GUI principal de calidad
        UPGRADE,        // Mejora de calidad
        ENHANCE,        // Sistema de enhancement
        REPAIR,         // Reparación de ítems
        SALVAGE,        // Descomposición de ítems
        COMBINE,        // Combinación de ítems
        CRYSTAL_APPLY,  // Aplicar cristales
        STATISTICS,     // Estadísticas de ítems
        SETTINGS        // Configuraciones
    }

    public ItemQualityGUI(Player player, ItemQualityGUIType type) {
        this.player = player;
        this.guiType = type;
        this.guiData = new HashMap<>();
        this.inventory = createInventory(type);
        populateInventory();
    }

    public ItemQualityGUI(Player player, ItemQualityGUIType type, Map<String, Object> data) {
        this.player = player;
        this.guiType = type;
        this.guiData = data != null ? new HashMap<>(data) : new HashMap<>();
        this.inventory = createInventory(type);
        populateInventory();
    }

    private Inventory createInventory(ItemQualityGUIType type) {
        return switch (type) {
            case MAIN -> Bukkit.createInventory(this, 54, "<gold>✦ Sistema de Calidad de Ítems ✦");
            case UPGRADE -> Bukkit.createInventory(this, 45, "<green>⬆ Mejorar Calidad de Ítem ⬆");
            case ENHANCE -> Bukkit.createInventory(this, 54, "<light_purple>⚡ Mejorar Atributos del Ítem ⚡");
            case REPAIR -> Bukkit.createInventory(this, 36, "<yellow>🔧 Reparar Ítem 🔧");
            case SALVAGE -> Bukkit.createInventory(this, 36, "<red>⚒ Descomponer Ítem ⚒");
            case COMBINE -> Bukkit.createInventory(this, 45, "<aqua>🔀 Combinar Ítems 🔀");
            case CRYSTAL_APPLY -> Bukkit.createInventory(this, 45, "<dark_purple>✦ Aplicar Cristal de Mejora ✦");
            case STATISTICS -> Bukkit.createInventory(this, 54, "<dark_aqua>📊 Estadísticas de Ítems 📊");
            case SETTINGS -> Bukkit.createInventory(this, 36, "<light_purple>⚙ Configuraciones ⚙");
        };
    }

    private void populateInventory() {
        switch (guiType) {
            case MAIN -> populateMainGUI();
            case UPGRADE -> populateUpgradeGUI();
            case ENHANCE -> populateEnhanceGUI();
            case REPAIR -> populateRepairGUI();
            case SALVAGE -> populateSalvageGUI();
            case COMBINE -> populateCombineGUI();
            case CRYSTAL_APPLY -> populateCrystalApplyGUI();
            case STATISTICS -> populateStatisticsGUI();
            case SETTINGS -> populateSettingsGUI();
        }
    }

    private void populateMainGUI() {
        // Título y decoración
        createTitle();

        // Opciones principales
        createMainOptions();

        // Panel de información
        createInfoPanel();

        // Navegación
        createMainNavigation();

        // Rellenar espacios vacíos
        fillEmptySlots();
    }

    private void createTitle() {
        ItemStack title = createButtonItem(Material.NETHER_STAR,
                "<gold>✦ Sistema de Calidad de Ítems ✦",
                "<gray>Mejora, combina y gestiona tus ítems",
                "<gray>con el sistema de calidad avanzado",
                "",
                "<yellow>Por MKSurvival"
        );
        inventory.setItem(4, title);
    }

    private void createMainOptions() {
        // Mejorar calidad
        ItemStack upgrade = createButtonItem(Material.EXPERIENCE_BOTTLE,
                "<green>⬆ Mejorar Calidad",
                "<gray>Aumenta la calidad de tus ítems",
                "<gray>usando experiencia y materiales",
                "",
                "<gray>Calidades disponibles:",
                "<dark_gray>• <gray>Común → <green>Raro → <blue>Épico → <gold>Legendario",
                "",
                "<yellow>▶ Click para acceder"
        );
        inventory.setItem(19, upgrade);

        // Sistema de enhancement
        ItemStack enhance = createButtonItem(Material.ENCHANTING_TABLE,
                "<light_purple>⚡ Mejorar Atributos",
                "<gray>Mejora los atributos específicos",
                "<gray>de tus ítems hasta el nivel 15",
                "",
                "<gray>Atributos mejorables:",
                "<red>• Daño de Ataque",
                "<gray>• Armadura",
                "<dark_red>• Vida Máxima",
                "<aqua>• Velocidad de Movimiento",
                "",
                "<yellow>▶ Click para acceder"
        );
        inventory.setItem(20, enhance);

        // Reparar ítems
        ItemStack repair = createButtonItem(Material.ANVIL,
                "<yellow>🔧 Reparar Ítems",
                "<gray>Repara tus ítems dañados",
                "<gray>Los ítems de mayor calidad cuestan menos",
                "",
                "<gray>Descuentos por calidad:",
                "<green>• Raro: <dark_green>10% descuento",
                "<blue>• Épico: <blue>20% descuento",
                "<gold>• Legendario: <gold>30% descuento",
                "",
                "<yellow>▶ Click para acceder"
        );
        inventory.setItem(21, repair);

        // Descomponer ítems
        ItemStack salvage = createButtonItem(Material.GRINDSTONE,
                "<red>⚒ Descomponer Ítems",
                "<gray>Convierte ítems en materiales útiles",
                "<gray>Los ítems de mayor calidad dan más materiales",
                "",
                "<gray>Materiales obtenidos:",
                "<gray>• Lingotes y gemas",
                "<light_purple>• Cristales de mejora (posible)",
                "<gray>• Materiales especiales",
                "",
                "<yellow>▶ Click para acceder"
        );
        inventory.setItem(22, salvage);

        // Combinar ítems
        ItemStack combine = createButtonItem(Material.CRAFTING_TABLE,
                "<aqua>🔀 Combinar Ítems",
                "<gray>Combina dos ítems del mismo tipo",
                "<gray>para crear uno más poderoso",
                "",
                "<gray>Requisitos:",
                "<gray>• Mismo tipo de ítem",
                "<gray>• Misma calidad",
                "<aqua>• 10% bonus por combinación",
                "",
                "<yellow>▶ Click para acceder"
        );
        inventory.setItem(23, combine);

        // Cristales de mejora
        ItemStack crystal = createButtonItem(Material.AMETHYST_SHARD,
                "<dark_purple>✦ Cristales de Mejora",
                "<gray>Aplica cristales mágicos para",
                "<gray>mejorar aleatoriamente tus ítems",
                "",
                "<gray>Cómo obtener cristales:",
                "<gray>• Descomponiendo ítems mejorados",
                "<gray>• Recompensas de jefes",
                "<gray>• Eventos especiales",
                "",
                "<yellow>▶ Click para acceder"
        );
        inventory.setItem(24, crystal);

        // Estadísticas
        ItemStack stats = createButtonItem(Material.BOOK,
                "<dark_aqua>📊 Estadísticas",
                "<gray>Ve información detallada",
                "<gray>sobre tus ítems y mejoras",
                "",
                "<gray>Información incluida:",
                "<gray>• Poder total del ítem",
                "<gray>• Comparación de ítems",
                "<gray>• Historial de mejoras",
                "",
                "<yellow>▶ Click para acceder"
        );
        inventory.setItem(25, stats);
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7) {
        return null;
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9) {
        return null;
    }

    private void createInfoPanel() {
        ItemStack info = createButtonItem(Material.KNOWLEDGE_BOOK,
                "<gold>📖 Información del Sistema",
                "<gray>El sistema de calidad permite mejorar",
                "<gray>significativamente tus ítems con:",
                "",
                "<green>✓ 10 niveles de calidad diferentes",
                "<green>✓ Sistema de enhancement hasta nivel 15",
                "<green>✓ 8 tipos de mejoras de atributos",
                "<green>✓ Combinación y transferencia de mejoras",
                "<green>✓ Descomposición en materiales útiles",
                "",
                "<gray>¡Experimenta y crea ítems legendarios!"
        );
        inventory.setItem(49, info);
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10) {
        return null;
    }

    private void createMainNavigation() {
        // Configuraciones
        ItemStack settings = createButtonItem(Material.REDSTONE,
                "<light_purple>⚙ Configuraciones",
                "<gray>Personaliza las opciones del",
                "<gray>sistema de calidad",
                "",
                "<yellow>▶ Click para configurar"
        );
        inventory.setItem(45, settings);

        // Ayuda
        ItemStack help = createButtonItem(Material.WRITTEN_BOOK,
                "<gold>❓ Ayuda y Tutorial",
                "<gray>Aprende cómo usar el sistema",
                "<gray>de calidad de ítems",
                "",
                "<yellow>▶ Click para ver ayuda"
        );
        inventory.setItem(46, help);

        // Cerrar
        ItemStack close = createButtonItem(Material.BARRIER,
                "<red>✕ Cerrar",
                "<gray>Cierra esta interfaz",
                "",
                "<yellow>▶ Click para cerrar"
        );
        inventory.setItem(53, close);
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3) {
        return null;
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3, String s4) {
        return null;
    }

    private void populateUpgradeGUI() {
        // Slot para ítem de entrada
        createItemInputSlot();

        // Preview de calidades
        createQualityPreview();

        // Botón de mejora
        createUpgradeButton();

        // Información de costos
        createCostInfo();

        // Navegación
        inventory.setItem(36, createBackButton());
        inventory.setItem(44, createCloseButton());

        fillEmptySlots();
    }

    private void populateEnhanceGUI() {
        // Slot para ítem
        createItemInputSlot();

        // Tipos de enhancement disponibles
        createEnhancementOptions();

        // Panel de información
        createEnhancementInfo();

        // Navegación
        inventory.setItem(45, createBackButton());
        inventory.setItem(53, createCloseButton());

        fillEmptySlots();
    }

    private void populateRepairGUI() {
        // Slot para ítem a reparar
        ItemStack repairSlot = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<yellow>🔧 Colocar Ítem a Reparar",
                "<gray>Arrastra aquí el ítem que quieres reparar",
                "",
                "<gray>El costo depende del daño y la calidad"
        );
        inventory.setItem(13, repairSlot);

        // Botón de reparación
        ItemStack repairButton = createButtonItem(Material.ANVIL,
                "<green>✓ Reparar Ítem",
                "<gray>Click para reparar el ítem",
                "<red>(Coloca un ítem primero)",
                "",
                "<gray>Costo: <yellow>??? monedas"
        );
        inventory.setItem(22, repairButton);

        // Información
        ItemStack info = createButtonItem(Material.BOOK,
                "<gold>📖 Información de Reparación",
                "<gray>• Los ítems de mejor calidad cuestan menos",
                "<gray>• La reparación restaura toda la durabilidad",
                "<gray>• Mantiene todas las mejoras del ítem",
                "",
                "<gray>Descuentos por calidad:",
                "<green>• Raro: <dark_green>10% descuento",
                "<blue>• Épico: <blue>20% descuento",
                "<gold>• Legendario: <gold>30% descuento"
        );
        inventory.setItem(31, info);

        // Navegación
        inventory.setItem(27, createBackButton());
        inventory.setItem(35, createCloseButton());
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
        return null;
    }

    private void populateSalvageGUI() {
        // Slot para ítem a descomponer
        ItemStack salvageSlot = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<red>⚒ Colocar Ítem a Descomponer",
                "<gray>Arrastra aquí el ítem que quieres descomponer",
                "",
                "<gray>Los ítems de mayor calidad dan más materiales"
        );
        inventory.setItem(13, salvageSlot);

        // Botón de descomposición
        ItemStack salvageButton = createButtonItem(Material.GRINDSTONE,
                "<red>⚒ Descomponer Ítem",
                "<gray>Click para descomponer el ítem",
                "<red>(Coloca un ítem primero)",
                "",
                "<gray>Materiales esperados: <yellow>???"
        );
        inventory.setItem(22, salvageButton);

        // Navegación
        inventory.setItem(27, createBackButton());
        inventory.setItem(35, createCloseButton());
    }

    private void populateCombineGUI() {
        // Slots para ítems de entrada
        ItemStack slot1 = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<aqua>🔀 Ítem 1",
                "<gray>Arrastra el primer ítem aquí"
        );
        inventory.setItem(19, slot1);

        ItemStack slot2 = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<aqua>🔀 Ítem 2",
                "<gray>Arrastra el segundo ítem aquí"
        );
        inventory.setItem(25, slot2);

        // Botón de combinación
        ItemStack combineButton = createButtonItem(Material.CRAFTING_TABLE,
                "<green>✓ Combinar Ítems",
                "<gray>Combina los ítems en uno mejorado",
                "<red>(Coloca ítems válidos primero)",
                "",
                "<gray>Requisitos:",
                "<gray>• Mismo tipo de ítem",
                "<gray>• Misma calidad",
                "<aqua>• Bonus: 10% mejora en atributos"
        );
        inventory.setItem(22, combineButton);

        // Navegación
        inventory.setItem(36, createBackButton());
        inventory.setItem(44, createCloseButton());

        fillEmptySlots();
    }

    private void populateCrystalApplyGUI() {
        // Slot para ítem
        ItemStack itemSlot = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<dark_purple>✦ Colocar Ítem",
                "<gray>Arrastra aquí el ítem a mejorar"
        );
        inventory.setItem(19, itemSlot);

        // Slot para cristal
        ItemStack crystalSlot = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<light_purple>✦ Colocar Cristal de Mejora",
                "<gray>Arrastra aquí el cristal de mejora"
        );
        inventory.setItem(25, crystalSlot);

        // Botón de aplicación
        ItemStack applyButton = createButtonItem(Material.ENCHANTING_TABLE,
                "<dark_purple>✦ Aplicar Cristal",
                "<gray>Aplica el cristal al ítem",
                "<red>(Coloca ítem y cristal primero)",
                "",
                "<gray>El cristal añadirá una mejora aleatoria"
        );
        inventory.setItem(22, applyButton);

        // Navegación
        inventory.setItem(36, createBackButton());
        inventory.setItem(44, createCloseButton());

        fillEmptySlots();
    }

    private ItemStack createButtonItem(Material material, String s, String s1) {
        return null;
    }

    private void populateStatisticsGUI() {
        // Información del jugador
        createPlayerItemStats();

        // Top ítems del jugador
        createTopItemsDisplay();

        // Comparador de ítems
        createItemComparator();

        // Navegación
        inventory.setItem(45, createBackButton());
        inventory.setItem(53, createCloseButton());

        fillEmptySlots();
    }

    private void populateSettingsGUI() {
        // Obtener configuraciones del jugador
        Map<String, Boolean> playerSettings = getPlayerSettings(player.getUniqueId());

        // Configuraciones de notificaciones
        ItemStack notifications = createToggleItem(Material.BELL,
                "<yellow>🔔 Notificaciones de Calidad",
                "<gray>Recibe notificaciones cuando mejoras ítems",
                playerSettings.getOrDefault("notifications", true)
        );
        inventory.setItem(10, notifications);

        // Auto-aplicar calidad
        ItemStack autoApply = createToggleItem(Material.HOPPER,
                "<green>🔄 Auto-aplicar Calidad",
                "<gray>Aplica calidad automáticamente a ítems nuevos",
                playerSettings.getOrDefault("autoApply", false) // Get from player settings
        );
        inventory.setItem(12, autoApply);

        // Mostrar estadísticas en lore
        ItemStack showStats = createToggleItem(Material.BOOK,
                "<dark_aqua>📊 Mostrar Estadísticas",
                "<gray>Muestra estadísticas detalladas en el lore",
                playerSettings.getOrDefault("showStats", true)
        );
        inventory.setItem(14, showStats);

        // Confirmaciones
        ItemStack confirmations = createToggleItem(Material.PAPER,
                "<gold>⚠ Confirmaciones",
                "<gray>Pide confirmación para acciones importantes",
                playerSettings.getOrDefault("confirmations", true)
        );
        inventory.setItem(16, confirmations);

        // Navegación
        inventory.setItem(27, createBackButton());
        inventory.setItem(35, createCloseButton());
    }

    // ==================== MÉTODOS DE CREACIÓN DE ELEMENTOS ====================

    private void createItemInputSlot() {
        ItemStack inputSlot = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<gold>📥 Colocar Ítem Aquí",
                "<gray>Arrastra tu ítem a este slot",
                "<gray>para comenzar el proceso"
        );
        inventory.setItem(ITEM_INPUT_SLOT, inputSlot);
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2) {
        return null;
    }

    private void createQualityPreview() {
        ItemQualityManager.ItemQuality[] qualities = {
                ItemQualityManager.ItemQuality.COMMON,
                ItemQualityManager.ItemQuality.UNCOMMON,
                ItemQualityManager.ItemQuality.RARE,
                ItemQualityManager.ItemQuality.EPIC,
                ItemQualityManager.ItemQuality.LEGENDARY,
                ItemQualityManager.ItemQuality.MYTHIC,
                ItemQualityManager.ItemQuality.DIVINE
        };

        for (int i = 0; i < QUALITY_PREVIEW_SLOTS.length && i < qualities.length; i++) {
            ItemQualityManager.ItemQuality quality = qualities[i];
            ItemStack preview = createQualityPreviewItem(quality);
            inventory.setItem(QUALITY_PREVIEW_SLOTS[i], preview);
        }
    }

    private ItemStack createQualityPreviewItem(ItemQualityManager.ItemQuality quality) {
        Material material = getQualityMaterial(quality);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtils.parse(quality.getColorCode() + quality.getSymbol() + " " + quality.getDisplayName()));
        List<String> lore = Arrays.asList(
                "<gray>Multiplicador: <green>" + String.format("%.1f", quality.getStatMultiplier()) + "x",
                "<gray>Bonus de mejora: <yellow>+" + quality.getEnhancementBonus(),
                "",
                "<gray>Esta calidad otorga:",
                "<gray>• Mejor rendimiento del ítem",
                "<gray>• Descuentos en reparaciones",
                "<gray>• Más materiales al descomponer"
        );
        meta.lore(MessageUtils.parseList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }

    private Material getQualityMaterial(ItemQualityManager.ItemQuality quality) {
        return switch (quality) {
            case POOR -> Material.COBBLESTONE;
            case COMMON -> Material.IRON_INGOT;
            case UNCOMMON -> Material.GOLD_INGOT;
            case RARE -> Material.DIAMOND;
            case EPIC -> Material.EMERALD;
            case LEGENDARY -> Material.NETHER_STAR;
            case MYTHIC -> Material.BEACON;
            case DIVINE -> Material.END_CRYSTAL;
            case COSMIC -> Material.DRAGON_EGG;
            default -> Material.STONE;
        };
    }

    private void createUpgradeButton() {
        ItemStack upgradeBtn = createButtonItem(Material.EXPERIENCE_BOTTLE,
                "<green>⬆ Mejorar Calidad",
                "<gray>Click para mejorar la calidad del ítem",
                "<red>(Coloca un ítem primero)",
                "",
                "<gray>Costo: <yellow>??? monedas",
                "<gray>Probabilidad: <green>??%"
        );
        inventory.setItem(31, upgradeBtn);
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3, String s4, String s5) {
        return null;
    }

    private void createCostInfo() {
        ItemStack costInfo = createButtonItem(Material.GOLD_INGOT,
                "<gold>💰 Información de Costos",
                "<gray>Los costos varían según la calidad actual:",
                "",
                "<gray>Común → Poco Común: <yellow>1,000 monedas",
                "<gray>Poco Común → Raro: <yellow>5,000 monedas",
                "<gray>Raro → Épico: <yellow>15,000 monedas",
                "<gray>Épico → Legendario: <yellow>50,000 monedas",
                "<gray>Legendario → Mítico: <yellow>150,000 monedas",
                "",
                "<gray>Las probabilidades de éxito disminuyen",
                "<gray>con cada nivel de calidad superior"
        );
        inventory.setItem(40, costInfo);
    }

    private void createEnhancementOptions() {
        ItemQualityManager.EnhancementType[] enhancements = {
                ItemQualityManager.EnhancementType.ATTACK_DAMAGE,
                ItemQualityManager.EnhancementType.ATTACK_SPEED,
                ItemQualityManager.EnhancementType.ARMOR,
                ItemQualityManager.EnhancementType.ARMOR_TOUGHNESS,
                ItemQualityManager.EnhancementType.MAX_HEALTH,
                ItemQualityManager.EnhancementType.MOVEMENT_SPEED,
                ItemQualityManager.EnhancementType.LUCK,
                ItemQualityManager.EnhancementType.KNOCKBACK_RESISTANCE
        };

        for (int i = 0; i < ENHANCEMENT_SLOTS.length && i < enhancements.length; i++) {
            ItemQualityManager.EnhancementType enhancement = enhancements[i];
            ItemStack enhancementItem = createEnhancementItem(enhancement);
            inventory.setItem(ENHANCEMENT_SLOTS[i], enhancementItem);
        }
    }

    private ItemStack createEnhancementItem(ItemQualityManager.EnhancementType enhancement) {
        Material material = getEnhancementMaterial(enhancement);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtils.parse(enhancement.getColorCode() + enhancement.getDisplayName()));
        List<String> lore = Arrays.asList(
                "<gray>Mejora base: <green>+" + String.format("%.1f", enhancement.getBaseValue()),
                "<gray>Máximo: <yellow>" + String.format("%.1f", enhancement.getMaxValue()),
                "",
                "<gray>Esta mejora afecta:",
                "<gray>" + getEnhancementDescription(enhancement),
                "",
                "<yellow>▶ Click para aplicar esta mejora",
                "<gray>Costo: <yellow>10,000 monedas"
        );
        meta.lore(MessageUtils.parseList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }

    private Material getEnhancementMaterial(ItemQualityManager.EnhancementType enhancement) {
        return switch (enhancement) {
            case ATTACK_DAMAGE -> Material.IRON_SWORD;
            case ATTACK_SPEED -> Material.SUGAR;
            case ARMOR -> Material.IRON_CHESTPLATE;
            case ARMOR_TOUGHNESS -> Material.DIAMOND_CHESTPLATE;
            case MAX_HEALTH -> Material.GOLDEN_APPLE;
            case MOVEMENT_SPEED -> Material.LEATHER_BOOTS;
            case LUCK -> Material.RABBIT_FOOT;
            case KNOCKBACK_RESISTANCE -> Material.OBSIDIAN;
        };
    }

    private String getEnhancementDescription(ItemQualityManager.EnhancementType enhancement) {
        return switch (enhancement) {
            case ATTACK_DAMAGE -> "El daño que infliges en combate";
            case ATTACK_SPEED -> "La velocidad de tus ataques";
            case ARMOR -> "Tu protección contra daño físico";
            case ARMOR_TOUGHNESS -> "Resistencia contra ataques fuertes";
            case MAX_HEALTH -> "Tu vida máxima total";
            case MOVEMENT_SPEED -> "Tu velocidad de movimiento";
            case LUCK -> "Probabilidad de mejores drops";
            case KNOCKBACK_RESISTANCE -> "Resistencia al empuje";
        };
    }

    private void createEnhancementInfo() {
        ItemStack info = createButtonItem(Material.KNOWLEDGE_BOOK,
                "<gold>📖 Sistema de Mejoras",
                "<gray>Las mejoras (enhancements) permiten",
                "<gray>mejorar atributos específicos de tus ítems",
                "",
                "<gray>Características:",
                "<gray>• Hasta 15 niveles por ítem",
                "<gray>• 8 tipos diferentes de mejoras",
                "<gray>• Probabilidad de éxito variable",
                "<gray>• Costos incrementales",
                "",
                "<gray>¡Mejora estratégicamente!"
        );
        inventory.setItem(49, info);
    }

    private void createPlayerItemStats() {
        // Obtener estadísticas del jugador desde el ItemQualityManager
        ItemQualityManager manager = MKSurvival.getInstance().getItemQualityManager();
        Map<String, Object> playerStats = manager.getPlayerStats(player.getUniqueId());

        // Obtener valores específicos o usar valores por defecto
        int itemsWithQuality = (int) playerStats.getOrDefault("itemsWithQuality", 0);
        int enhancedItems = (int) playerStats.getOrDefault("enhancedItems", 0);
        double avgEnhancementLevel = (double) playerStats.getOrDefault("avgEnhancementLevel", 0.0);
        String mostPowerfulItem = (String) playerStats.getOrDefault("mostPowerfulItem", "Ninguno");

        // Obtener conteo de calidades
        Map<ItemQualityManager.ItemQuality, Integer> qualityCounts =
                (Map<ItemQualityManager.ItemQuality, Integer>) playerStats.getOrDefault("qualityCounts", new HashMap<>());

        // Crear lista de lore para el ítem
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Ítems con calidad: <yellow>" + itemsWithQuality);
        lore.add("<gray>Ítems mejorados: <green>" + enhancedItems);
        lore.add("<gray>Nivel promedio de mejora: <light_purple>" + String.format("%.1f", avgEnhancementLevel));
        lore.add("<gray>Ítem más poderoso: <gold>" + mostPowerfulItem);
        lore.add("");
        lore.add("<gray>Calidades obtenidas:");

        // Añadir conteo de calidades
        for (ItemQualityManager.ItemQuality quality : ItemQualityManager.ItemQuality.values()) {
            if (quality == ItemQualityManager.ItemQuality.POOR) continue; // Omitir calidad POOR

            int count = qualityCounts.getOrDefault(quality, 0);
            lore.add("<gray>• " + quality.getColorCode() + quality.getDisplayName() + ": " + quality.getColorCode() + count);
        }

        // Crear ítem de estadísticas
        ItemStack stats = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stats.getItemMeta();
        meta.displayName(MessageUtils.parse("<gold>👤 Tus Estadísticas"));
        meta.lore(MessageUtils.parseList(lore));

        // Establecer la cabeza del jugador
        meta.setOwningPlayer(player);

        stats.setItemMeta(meta);
        inventory.setItem(4, stats);
    }

    private void createTopItemsDisplay() {
        // Obtener los mejores ítems del jugador desde el ItemQualityManager
        ItemQualityManager manager = MKSurvival.getInstance().getItemQualityManager();
        List<Map<String, Object>> topItems = manager.getPlayerTopItems(player.getUniqueId(), 7);

        int[] topSlots = {19, 20, 21, 22, 23, 24, 25};

        // Si no hay ítems, mostrar mensaje por defecto
        if (topItems.isEmpty()) {
            ItemStack noItems = createButtonItem(Material.BARRIER,
                    "<red>¡No tienes ítems con calidad!",
                    "<gray>Mejora tus ítems para verlos aquí",
                    "<gray>Usa el menú principal para mejorar"
            );
            inventory.setItem(22, noItems);
            return;
        }

        // Mostrar los mejores ítems
        for (int i = 0; i < topSlots.length && i < topItems.size(); i++) {
            Map<String, Object> itemData = topItems.get(i);

            // Obtener datos del ítem
            String name = (String) itemData.getOrDefault("name", "Ítem desconocido");
            ItemQualityManager.ItemQuality quality = (ItemQualityManager.ItemQuality) itemData.getOrDefault("quality", ItemQualityManager.ItemQuality.COMMON);
            int enhancementLevel = (int) itemData.getOrDefault("enhancementLevel", 0);
            int power = (int) itemData.getOrDefault("power", 0);
            Material material = (Material) itemData.getOrDefault("material", Material.GOLDEN_SWORD);

            // Crear ítem para mostrar
            ItemStack topItem = new ItemStack(material);
            ItemMeta meta = topItem.getItemMeta();

            // Configurar nombre y lore
            meta.displayName(MessageUtils.parse("<gray>#" + (i + 1) + " " + quality.getColorCode() + name +
                    (enhancementLevel > 0 ? " +" + enhancementLevel : "")));

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Calidad: " + quality.getColorCode() + quality.getDisplayName());
            lore.add("<gray>Poder: <light_purple>" + power);
            if (enhancementLevel > 0) {
                lore.add("<gray>Nivel de mejora: <green>+" + enhancementLevel);
            }
            lore.add("");
            lore.add("<yellow>▶ Click para ver detalles");

            meta.lore(MessageUtils.parseList(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

            topItem.setItemMeta(meta);
            inventory.setItem(topSlots[i], topItem);
        }
    }

    private void createItemComparator() {
        ItemStack comparator = createButtonItem(Material.COMPARATOR,
                "<dark_aqua>⚖ Comparador de Ítems",
                "<gray>Arrastra dos ítems para compararlos",
                "<gray>y ver cuál es más poderoso",
                "",
                "<yellow>▶ Click para usar comparador"
        );
        inventory.setItem(40, comparator);
    }

    private ItemStack createToggleItem(Material material, String name, String description, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse(name));

        String statusColor = enabled ? "<green>" : "<red>";
        String statusText = enabled ? "ACTIVADO" : "DESACTIVADO";
        List<String> lore = Arrays.asList(
                description,
                "",
                "<gray>Estado: " + statusColor + statusText,
                "",
                "<yellow>▶ Click para alternar"
        );
        meta.lore(MessageUtils.parseList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackButton() {
        return createButtonItem(Material.ARROW,
                "<red>← Volver",
                "<gray>Volver al menú anterior"
        );
    }

    private ItemStack createCloseButton() {
        return createButtonItem(Material.BARRIER,
                "<red>✕ Cerrar",
                "<gray>Cierra esta interfaz"
        );
    }

    private void fillEmptySlots() {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(MessageUtils.parse(" "));
        filler.setItemMeta(meta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    // ==================== MANEJO DE EVENTOS ====================

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        switch (guiType) {
            case MAIN -> handleMainGUIClick(slot, clicked);
            case UPGRADE -> handleUpgradeGUIClick(slot, clicked);
            case ENHANCE -> handleEnhanceGUIClick(slot, clicked);
            case REPAIR -> handleRepairGUIClick(slot, clicked);
            case SALVAGE -> handleSalvageGUIClick(slot, clicked);
            case COMBINE -> handleCombineGUIClick(slot, clicked);
            case CRYSTAL_APPLY -> handleCrystalApplyGUIClick(slot, clicked);
            case STATISTICS -> handleStatisticsGUIClick(slot, clicked);
            case SETTINGS -> handleSettingsGUIClick(slot, clicked);
        }
    }

    private void handleMainGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 19 -> openUpgradeGUI();
            case 20 -> openEnhanceGUI();
            case 21 -> openRepairGUI();
            case 22 -> openSalvageGUI();
            case 23 -> openCombineGUI();
            case 24 -> openCrystalApplyGUI();
            case 25 -> openStatisticsGUI();
            case 45 -> openSettingsGUI();
            case 46 -> showHelp();
            case 53 -> player.closeInventory();
        }
    }

    private void handleUpgradeGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case ITEM_INPUT_SLOT -> handleItemInput(clicked);
            case 31 -> upgradeItem();
            case 36 -> openMainGUI();
            case 44 -> player.closeInventory();
        }
    }

    private void handleEnhanceGUIClick(int slot, ItemStack clicked) {
        for (int i = 0; i < ENHANCEMENT_SLOTS.length; i++) {
            if (slot == ENHANCEMENT_SLOTS[i]) {
                applyEnhancement(i);
                return;
            }
        }

        switch (slot) {
            case ITEM_INPUT_SLOT -> handleItemInput(clicked);
            case 45 -> openMainGUI();
            case 53 -> player.closeInventory();
        }
    }

    private void handleRepairGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 13 -> handleRepairItemInput(clicked);
            case 22 -> repairItem();
            case 27 -> openMainGUI();
            case 35 -> player.closeInventory();
        }
    }

    private void handleSalvageGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 13 -> handleSalvageItemInput(clicked);
            case 22 -> salvageItem();
            case 27 -> openMainGUI();
            case 35 -> player.closeInventory();
        }
    }

    private void handleCombineGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 19 -> handleCombineItem1Input(clicked);
            case 25 -> handleCombineItem2Input(clicked);
            case 22 -> combineItems();
            case 36 -> openMainGUI();
            case 44 -> player.closeInventory();
        }
    }

    private void handleCrystalApplyGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 19 -> handleCrystalItemInput(clicked);
            case 25 -> handleCrystalInput(clicked);
            case 22 -> applyCrystal();
            case 36 -> openMainGUI();
            case 44 -> player.closeInventory();
        }
    }

    private void handleStatisticsGUIClick(int slot, ItemStack clicked) {
        if (slot >= 19 && slot <= 25) {
            // Clicked on a top item
            showItemDetails(slot - 19);
        } else if (slot == 40) {
            openItemComparator();
        } else if (slot == 45) {
            openMainGUI();
        } else if (slot == 53) {
            player.closeInventory();
        }
    }

    private void handleSettingsGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 10 -> toggleSetting("notifications");
            case 12 -> toggleSetting("autoApply");
            case 14 -> toggleSetting("showStats");
            case 16 -> toggleSetting("confirmations");
            case 27 -> openMainGUI();
            case 35 -> player.closeInventory();
        }
    }

    // ==================== MÉTODOS DE ACCIÓN ====================

    private void openUpgradeGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.UPGRADE);
        player.openInventory(gui.getInventory());
    }

    private void openEnhanceGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.ENHANCE);
        player.openInventory(gui.getInventory());
    }

    private void openRepairGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.REPAIR);
        player.openInventory(gui.getInventory());
    }

    private void openSalvageGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.SALVAGE);
        player.openInventory(gui.getInventory());
    }

    private void openCombineGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.COMBINE);
        player.openInventory(gui.getInventory());
    }

    private void openCrystalApplyGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.CRYSTAL_APPLY);
        player.openInventory(gui.getInventory());
    }

    private void openStatisticsGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.STATISTICS);
        player.openInventory(gui.getInventory());
    }

    private void openSettingsGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.SETTINGS);
        player.openInventory(gui.getInventory());
    }

    private void openMainGUI() {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.MAIN);
        player.openInventory(gui.getInventory());
    }

    private void showHelp() {
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
        MessageUtils.sendMessage(player, "<gold>✦ Sistema de Calidad de Ítems - Ayuda ✦");
        MessageUtils.sendMessage(player, "<gray>");
        MessageUtils.sendMessage(player, "<yellow>⬆ Mejorar Calidad:");
        MessageUtils.sendMessage(player, "<gray>  • Usa experiencia y dinero para mejorar");
        MessageUtils.sendMessage(player, "<gray>  • Probabilidad de éxito variable");
        MessageUtils.sendMessage(player, "<gray>  • Ítems de mayor calidad son más poderosos");
        MessageUtils.sendMessage(player, "<gray>");
        MessageUtils.sendMessage(player, "<light_purple>⚡ Mejoras (Enhancement):");
        MessageUtils.sendMessage(player, "<gray>  • Mejora atributos específicos hasta nivel 15");
        MessageUtils.sendMessage(player, "<gray>  • 8 tipos diferentes de mejoras disponibles");
        MessageUtils.sendMessage(player, "<gray>  • Cada mejora tiene su propio máximo");
        MessageUtils.sendMessage(player, "<gray>");
        MessageUtils.sendMessage(player, "<aqua>🔀 Combinar Ítems:");
        MessageUtils.sendMessage(player, "<gray>  • Combina ítems del mismo tipo y calidad");
        MessageUtils.sendMessage(player, "<gray>  • Obtén 10% bonus en atributos");
        MessageUtils.sendMessage(player, "<gray>  • El ítem resultante es más poderoso");
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
    }

    private void handleItemInput(ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] No puedes colocar un ítem vacío.</red>");
            return;
        }

        // Verificar si el ítem ya tiene calidad aplicada
        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.ItemData itemData = qualityManager.getItemData(clicked);
        
        if (itemData == null) {
            // Aplicar calidad aleatoria si no tiene
            ItemQualityManager.ItemQuality randomQuality = ItemQualityManager.ItemQuality.getRandomQuality();
            qualityManager.applyQuality(clicked, randomQuality);
            MessageUtils.sendMessage(player, "<green>[Calidad] Se aplicó calidad " + randomQuality.getColorCode() + randomQuality.getDisplayName() + "<green> al ítem.</green>");
        }
        
        // Colocar el ítem en el slot de entrada
        inventory.setItem(ITEM_INPUT_SLOT, clicked);
        MessageUtils.sendMessage(player, "<green>[Calidad] Ítem colocado para procesamiento.</green>");
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.2f);
        
        // Actualizar la GUI para mostrar las opciones disponibles
        updateItemInfo(clicked);
        populateInventory();
    }

    private void handleRepairItemInput(ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Reparación] No puedes colocar un ítem vacío.</red>");
            return;
        }

        // Verificar si el ítem está dañado
        if (clicked.getType().getMaxDurability() == 0) {
            MessageUtils.sendMessage(player, "<red>[Reparación] Este ítem no se puede reparar.</red>");
            return;
        }

        if (clicked.getDurability() == 0) {
            MessageUtils.sendMessage(player, "<yellow>[Reparación] Este ítem no necesita reparación.</yellow>");
            return;
        }

        // Calcular costo de reparación
        int maxDurability = clicked.getType().getMaxDurability();
        int currentDurability = maxDurability - clicked.getDurability();
        double damagePercent = (double) clicked.getDurability() / maxDurability;
        double repairCost = 100 + (damagePercent * 500); // Base 100 + hasta 500 según daño

        // Verificar si el jugador tiene suficiente dinero
        if (!MKSurvival.getInstance().getEconomyManager().hasBalance(player, repairCost)) {
            MessageUtils.sendMessage(player, "<red>[Reparación] No tienes suficiente dinero. Costo: <yellow>" + String.format("%.0f", repairCost) + "</yellow> monedas.</red>");
            return;
        }

        // Procesar reparación
        MKSurvival.getInstance().getEconomyManager().removeBalance(player, repairCost);
        clicked.setDurability((short) 0); // Reparar completamente
        
        // Colocar ítem reparado
        inventory.setItem(ITEM_INPUT_SLOT, clicked);
        MessageUtils.sendMessage(player, "<green>[Reparación] Ítem reparado exitosamente por <yellow>" + String.format("%.0f", repairCost) + "</yellow> monedas.</green>");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
    }

    private void handleSalvageItemInput(ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Descomposición] No puedes colocar un ítem vacío.</red>");
            return;
        }

        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        
        // Procesar descomposición
        if (qualityManager.salvageItem(player, clicked)) {
            // Eliminar el ítem del inventario
            clicked.setAmount(0);
            inventory.setItem(ITEM_INPUT_SLOT, new ItemStack(Material.AIR));
            
            MessageUtils.sendMessage(player, "<green>[Descomposición] Ítem descompuesto exitosamente.</green>");
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
            
            // Actualizar GUI
            populateInventory();
        } else {
            MessageUtils.sendMessage(player, "<red>[Descomposición] No se pudo descomponer el ítem.</red>");
        }
    }

    private void handleCombineItem1Input(ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Combinación] No puedes colocar un ítem vacío.</red>");
            return;
        }

        // Verificar que el ítem tenga calidad aplicada
        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.ItemData itemData = qualityManager.getItemData(clicked);
        
        if (itemData == null) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Este ítem no tiene calidad aplicada.</red>");
            return;
        }

        // Colocar el ítem en el primer slot de combinación
        inventory.setItem(19, clicked);
        MessageUtils.sendMessage(player, "<aqua>[Combinación] Primer ítem colocado: " + itemData.getQuality().getColorCode() + clicked.getItemMeta().getDisplayName() + "</aqua>");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        
        // Actualizar información
        ItemStack infoItem = createButtonItem(Material.BOOK,
                "<aqua>ℹ Información de Combinación",
                "<gray>Primer ítem: " + itemData.getQuality().getColorCode() + clicked.getItemMeta().getDisplayName(),
                "<gray>Calidad: " + itemData.getQuality().getColorCode() + itemData.getQuality().getDisplayName(),
                "<gray>Nivel de mejora: <yellow>+" + itemData.getEnhancementLevel(),
                "",
                "<gray>Coloca un segundo ítem del mismo tipo y calidad"
        );
        inventory.setItem(31, infoItem);
    }

    private void handleCombineItem2Input(ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Combinación] No puedes colocar un ítem vacío.");
            return;
        }

        // Verificar si el primer ítem ya está colocado
        ItemStack firstItem = inventory.getItem(19);
        if (firstItem == null || firstItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Coloca primero el ítem 1.");
            return;
        }

        // Verificar compatibilidad
        if (firstItem.getType() != clicked.getType()) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Los ítems deben ser del mismo tipo.");
            return;
        }

        // Verificar calidad
        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.ItemQuality quality1 = qualityManager.getItemQuality(firstItem);
        ItemQualityManager.ItemQuality quality2 = qualityManager.getItemQuality(clicked);

        if (quality1 == null || quality2 == null) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Ambos ítems deben tener calidad aplicada.");
            return;
        }

        if (quality1 != quality2) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Los ítems deben tener la misma calidad.");
            return;
        }

        // Colocar el ítem
        inventory.setItem(25, clicked);
        MessageUtils.sendMessage(player, "<aqua>[Combinación] Segundo ítem colocado. ¡Listo para combinar!");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);

        // Actualizar botón de combinación
        ItemStack combineButton = createButtonItem(Material.CRAFTING_TABLE,
                "<green>✓ Combinar Ítems",
                "<gray>Combina los ítems en uno mejorado",
                "<green>(¡Ítems listos para combinar!)",
                "",
                "<gray>Resultado esperado:",
                "<gray>• Ítem de calidad: " + quality1.getColorCode() + quality1.getDisplayName(),
                "<gray>• Nivel de mejora: <green>+" + (Math.max(
                        qualityManager.getItemEnhancementLevel(firstItem),
                        qualityManager.getItemEnhancementLevel(clicked)) + 1),
                "<gray>• Bonus: <aqua>+10% en atributos",
                "",
                "<yellow>▶ Click para combinar"
        );
        inventory.setItem(22, combineButton);
    }

    /**
     * Actualiza la información del ítem actual en la GUI
     */
    private void updateItemInfo(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        
        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.ItemData itemData = qualityManager.getItemData(item);
        
        if (itemData != null) {
            // Actualizar slot de resultado con información del ítem
            ItemStack infoItem = createButtonItem(Material.ENCHANTED_BOOK,
                    "<gold>✨ Información del Ítem",
                    "<gray>Nombre: " + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 
                        item.getItemMeta().getDisplayName() : item.getType().name()),
                    "<gray>Calidad: " + itemData.getQuality().getColorCode() + itemData.getQuality().getDisplayName(),
                    "<gray>Nivel de mejora: <yellow>+" + itemData.getEnhancementLevel(),
                    "<gray>Mejoras aplicadas: <aqua>" + itemData.getEnhancements().size(),
                    "",
                    "<gray>Multiplicador de stats: <green>" + String.format("%.1f", itemData.getQuality().getStatMultiplier()) + "x",
                    "<gray>ID del ítem: <gray>" + itemData.getItemId().toString().substring(0, 8)
            );
            inventory.setItem(RESULT_SLOT, infoItem);
        }
    }
    
    /**
     * Calcula el costo de mejora para un ítem
     */
    private double calculateUpgradeCost(ItemStack item) {
        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.ItemData itemData = qualityManager.getItemData(item);
        
        if (itemData == null) return 1000.0;
        
        ItemQualityManager.ItemQuality currentQuality = itemData.getQuality();
        int baseCost = 500;
        int qualityMultiplier = (currentQuality.ordinal() + 1) * 200;
        int enhancementBonus = itemData.getEnhancementLevel() * 150;
        
        return baseCost + qualityMultiplier + enhancementBonus;
    }

    private void handleCrystalItemInput(ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Cristal] No puedes colocar un ítem vacío.");
            return;
        }

        // Verificar si el ítem tiene calidad aplicada
        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.ItemQuality quality = qualityManager.getItemQuality(clicked);

        if (quality == null) {
            MessageUtils.sendMessage(player, "<red>[Cristal] Este ítem no tiene calidad aplicada.");
            return;
        }

        // Verificar si el ítem puede ser mejorado
        if (quality == ItemQualityManager.ItemQuality.COSMIC) {
            MessageUtils.sendMessage(player, "<red>[Cristal] Este ítem ya tiene la máxima calidad posible.");
            return;
        }

        // Colocar el ítem
        inventory.setItem(19, clicked);
        MessageUtils.sendMessage(player, "<dark_purple>[Cristal] Ítem colocado para mejora con cristal.");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);

        // Actualizar información
        ItemStack infoItem = createButtonItem(Material.BOOK,
                "<dark_purple>✦ Información de Mejora",
                "<gray>Ítem actual: " + quality.getColorCode() + clicked.getItemMeta().getDisplayName(),
                "<gray>Calidad actual: " + quality.getColorCode() + quality.getDisplayName(),
                "<gray>Calidad posible: " + quality.getNext().getColorCode() + quality.getNext().getDisplayName(),
                "",
                "<gray>Coloca un cristal de mejora para continuar"
        );
        inventory.setItem(31, infoItem);
    }

    private void handleCrystalInput(ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Cristal] No puedes colocar un ítem vacío.");
            return;
        }

        // Verificar si es un cristal válido
        if (clicked.getType() != Material.AMETHYST_SHARD &&
                clicked.getType() != Material.PRISMARINE_CRYSTALS &&
                clicked.getType() != Material.NETHER_STAR) {
            MessageUtils.sendMessage(player, "<red>[Cristal] Este ítem no es un cristal de mejora válido.");
            return;
        }

        // Verificar si hay un ítem colocado para mejorar
        ItemStack targetItem = inventory.getItem(19);
        if (targetItem == null || targetItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Cristal] Coloca primero un ítem para mejorar.");
            return;
        }

        // Determinar tipo y potencia del cristal
        String crystalType;
        double successRate;

        if (clicked.getType() == Material.AMETHYST_SHARD) {
            crystalType = "Común";
            successRate = 0.5; // 50%
        } else if (clicked.getType() == Material.PRISMARINE_CRYSTALS) {
            crystalType = "Raro";
            successRate = 0.75; // 75%
        } else { // NETHER_STAR
            crystalType = "Legendario";
            successRate = 0.95; // 95%
        }

        // Colocar el cristal
        inventory.setItem(25, clicked);
        MessageUtils.sendMessage(player, "<light_purple>[Cristal] Cristal de mejora " + crystalType + " colocado.");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);

        // Actualizar botón de aplicación
        ItemStack applyButton = createButtonItem(Material.ENCHANTING_TABLE,
                "<green>✓ Aplicar Cristal",
                "<gray>Aplica el cristal al ítem seleccionado",
                "",
                "<gray>Tipo de cristal: <light_purple>" + crystalType,
                "<gray>Probabilidad de éxito: <yellow>" + (int) (successRate * 100) + "%",
                "",
                "<yellow>▶ Click para aplicar"
        );
        inventory.setItem(22, applyButton);
    }

    private ItemStack createButtonItem(Material material, String s, String s1, String s2, String s3, String s4, String s5, String s6) {
        return null;
    }

    private void upgradeItem() {
        ItemStack currentItem = inventory.getItem(ITEM_INPUT_SLOT);
        if (currentItem == null || currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Coloca un ítem primero.");
            return;
        }

        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        double cost = calculateUpgradeCost(currentItem);

        if (qualityManager.upgradeItemQuality(player, currentItem, cost)) {
            MessageUtils.sendMessage(player, "<green>[Calidad] ¡Ítem mejorado exitosamente!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            // Refresh GUI
            populateInventory();
        }
    }

    private void applyEnhancement(int enhancementIndex) {
        ItemStack currentItem = inventory.getItem(ITEM_INPUT_SLOT);
        if (currentItem == null || currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Mejora] Coloca un ítem primero.");
            return;
        }

        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.EnhancementType[] types = ItemQualityManager.EnhancementType.values();

        if (enhancementIndex < types.length) {
            ItemQualityManager.EnhancementType type = types[enhancementIndex];
            if (qualityManager.enhanceItem(player, currentItem, type, 1)) {
                MessageUtils.sendMessage(player, "<light_purple>[Mejora] ¡" + type.getDisplayName() + " mejorado!");
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
                // Refresh GUI
                populateInventory();
            }
        }
    }

    private void repairItem() {
        ItemStack currentItem = inventory.getItem(13);
        if (currentItem == null || currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Reparación] Coloca un ítem primero.");
            return;
        }

        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        double cost = calculateRepairCost(currentItem);

        if (qualityManager.repairItem(player, currentItem, cost)) {
            MessageUtils.sendMessage(player, "<yellow>[Reparación] ¡Ítem reparado completamente!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
            // Refresh GUI
            populateInventory();
        }
    }

    private void salvageItem() {
        ItemStack currentItem = inventory.getItem(13);
        if (currentItem == null || currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Descomposición] Coloca un ítem primero.");
            return;
        }

        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();

        if (qualityManager.salvageItem(player, currentItem)) {
            MessageUtils.sendMessage(player, "<red>[Descomposición] ¡Ítem descompuesto exitosamente!");
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
            // Remove item from slot
            inventory.setItem(13, createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                    "<red>⚒ Colocar Ítem a Descomponer",
                    "<gray>Arrastra aquí el ítem que quieres descomponer"));
        }
    }

    private void combineItems() {
        ItemStack item1 = inventory.getItem(19);
        ItemStack item2 = inventory.getItem(25);

        if (item1 == null || item2 == null ||
                item1.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                item2.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Coloca dos ítems válidos primero.</red>");
            return;
        }

        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        
        // Verificar que ambos ítems tengan calidad
        ItemQualityManager.ItemData data1 = qualityManager.getItemData(item1);
        ItemQualityManager.ItemData data2 = qualityManager.getItemData(item2);
        
        if (data1 == null || data2 == null) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Ambos ítems deben tener calidad aplicada.</red>");
            return;
        }
        
        // Verificar que sean del mismo tipo y calidad
        if (item1.getType() != item2.getType()) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Los ítems deben ser del mismo tipo.</red>");
            return;
        }
        
        if (data1.getQuality() != data2.getQuality()) {
            MessageUtils.sendMessage(player, "<red>[Combinación] Los ítems deben tener la misma calidad.</red>");
            return;
        }
        
        // Calcular costo de combinación
        double cost = 1000 + (data1.getQuality().ordinal() * 500);
        
        if (!MKSurvival.getInstance().getEconomyManager().hasBalance(player, cost)) {
            MessageUtils.sendMessage(player, "<red>[Combinación] No tienes suficiente dinero. Costo: <yellow>" + String.format("%.0f", cost) + "</yellow> monedas.</red>");
            return;
        }
        
        // Crear ítem resultado
        ItemStack result = item1.clone();
        ItemQualityManager.ItemData newData = new ItemQualityManager.ItemData(data1.getQuality());
        
        // Combinar niveles de mejora (tomar el mayor + 1)
        int newEnhancementLevel = Math.max(data1.getEnhancementLevel(), data2.getEnhancementLevel()) + 1;
        newData.setEnhancementLevel(newEnhancementLevel);
        
        // Combinar mejoras
        for (ItemQualityManager.EnhancementType enhancement : data1.getEnhancements().keySet()) {
            double value1 = data1.getEnhancements().get(enhancement);
            double value2 = data2.getEnhancements().getOrDefault(enhancement, 0.0);
            newData.getEnhancements().put(enhancement, value1 + value2 + 0.1); // Bonus del 10%
        }
        
        for (ItemQualityManager.EnhancementType enhancement : data2.getEnhancements().keySet()) {
            if (!newData.getEnhancements().containsKey(enhancement)) {
                newData.getEnhancements().put(enhancement, data2.getEnhancements().get(enhancement) + 0.1);
            }
        }
        
        // Aplicar datos al resultado
        qualityManager.applyItemData(result, newData);
        
        // Procesar transacción
        MKSurvival.getInstance().getEconomyManager().removeBalance(player, cost);
        
        // Dar resultado al jugador
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(result);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), result);
        }
        
        // Limpiar slots
        inventory.setItem(19, createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<aqua>▫ Slot de Ítem 1",
                "<gray>Coloca aquí el primer ítem"));
        inventory.setItem(25, createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<aqua>▫ Slot de Ítem 2",
                "<gray>Coloca aquí el segundo ítem"));
        
        MessageUtils.sendMessage(player, "<aqua>[Combinación] ¡Ítems combinados exitosamente! Costo: <yellow>" + String.format("%.0f", cost) + "</yellow> monedas.</aqua>");
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        
        // Actualizar botón de combinación
        inventory.setItem(22, createButtonItem(Material.CRAFTING_TABLE,
                "<aqua>🔀 Combinar Ítems",
                "<gray>Combina dos ítems del mismo tipo y calidad",
                "<red>Coloca dos ítems primero"));
    }

    private void applyCrystal() {
        ItemStack item = inventory.getItem(19);
        ItemStack crystal = inventory.getItem(25);

        if (item == null || crystal == null ||
                item.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                crystal.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            MessageUtils.sendMessage(player, "<red>[Cristal] Coloca ítem y cristal primero.");
            return;
        }

        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();

        if (qualityManager.applyCrystal(player, item, crystal)) {
            MessageUtils.sendMessage(player, "<dark_purple>[Cristal] ¡Cristal aplicado exitosamente!");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f);

            // Remove crystal from slot
            inventory.setItem(25, createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                    "<light_purple>✦ Colocar Cristal de Mejora", "<gray>Arrastra aquí el cristal de mejora"));
        }
    }

    private void showItemDetails(int itemIndex) {
        // Obtener los mejores ítems del jugador desde el ItemQualityManager
        ItemQualityManager manager = MKSurvival.getInstance().getItemQualityManager();
        List<Map<String, Object>> topItems = manager.getPlayerTopItems(player.getUniqueId(), 7);

        if (topItems.isEmpty() || itemIndex >= topItems.size()) {
            MessageUtils.sendMessage(player, "<red>[Error] No se encontró información del ítem.");
            return;
        }

        // Obtener datos del ítem seleccionado
        Map<String, Object> itemData = topItems.get(itemIndex);

        // Extraer información
        String name = (String) itemData.getOrDefault("name", "Ítem desconocido");
        ItemQualityManager.ItemQuality quality = (ItemQualityManager.ItemQuality) itemData.getOrDefault("quality", ItemQualityManager.ItemQuality.COMMON);
        int enhancementLevel = (int) itemData.getOrDefault("enhancementLevel", 0);
        int power = (int) itemData.getOrDefault("power", 0);
        Map<String, Double> attributes = (Map<String, Double>) itemData.getOrDefault("attributes", new HashMap<>());

        // Mostrar información detallada
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
        MessageUtils.sendMessage(player, "<gold>🔍 Detalles del Ítem");
        MessageUtils.sendMessage(player, "<gray>Nombre: " + quality.getColorCode() + name + (enhancementLevel > 0 ? " +" + enhancementLevel : ""));
        MessageUtils.sendMessage(player, "<gray>Poder: <light_purple>" + power);
        MessageUtils.sendMessage(player, "<gray>Calidad: " + quality.getColorCode() + quality.getDisplayName());

        // Mostrar atributos si existen
        if (!attributes.isEmpty()) {
            MessageUtils.sendMessage(player, "<gray>Atributos:");
            for (Map.Entry<String, Double> entry : attributes.entrySet()) {
                String attributeName = entry.getKey();
                double value = entry.getValue();
                MessageUtils.sendMessage(player, "<gray>• " + formatAttributeName(attributeName) + ": <green>+" + String.format("%.1f", value));
            }
        }

        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");

        // Reproducir sonido
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
    }

    private String formatAttributeName(String attributeName) {
        // Convertir nombres de atributos a formato legible
        return switch (attributeName.toLowerCase()) {
            case "attack_damage" -> "Daño de ataque";
            case "attack_speed" -> "Velocidad de ataque";
            case "armor" -> "Armadura";
            case "armor_toughness" -> "Resistencia";
            case "max_health" -> "Salud máxima";
            case "movement_speed" -> "Velocidad";
            case "luck" -> "Suerte";
            case "knockback_resistance" -> "Resistencia al empuje";
            default -> attributeName;
        };
    }

    private void openItemComparator() {
        // Crear GUI de comparación de ítems
        Inventory comparatorInv = Bukkit.createInventory(this, 54, "<dark_aqua>⚙ Comparador de Ítems");
        
        // Slots para ítems a comparar
        ItemStack slot1 = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<gray>▫ Slot de Ítem 1",
                "<gray>Coloca aquí el primer ítem");
        ItemStack slot2 = createButtonItem(Material.GRAY_STAINED_GLASS_PANE,
                "<gray>▫ Slot de Ítem 2",
                "<gray>Coloca aquí el segundo ítem");
        
        comparatorInv.setItem(20, slot1);
        comparatorInv.setItem(24, slot2);
        
        // Botón de comparación
        ItemStack compareButton = createButtonItem(Material.COMPARATOR,
                "<yellow>⚡ Comparar Ítems",
                "<gray>Compara las estadísticas de ambos ítems",
                "<red>Coloca dos ítems primero");
        comparatorInv.setItem(22, compareButton);
        
        // Botón de volver
        ItemStack backButton = createButtonItem(Material.ARROW,
                "<red>← Volver",
                "<gray>Volver al menú principal");
        comparatorInv.setItem(49, backButton);
        
        player.openInventory(comparatorInv);
        MessageUtils.sendMessage(player, "<aqua>[Comparador] Coloca dos ítems para compararlos.</aqua>");
    }

    private void toggleSetting(String settingName) {
        // Obtener la configuración actual del jugador
        Map<String, Boolean> playerSettings = getPlayerSettings(player.getUniqueId());

        // Alternar la configuración
        boolean currentValue = playerSettings.getOrDefault(settingName, false);
        playerSettings.put(settingName, !currentValue);

        // Guardar la configuración actualizada
        savePlayerSettings(player.getUniqueId(), playerSettings);

        // Mostrar mensaje apropiado
        String status = !currentValue ? "<green>activadas" : "<red>desactivadas";

        switch (settingName) {
            case "notifications" -> MessageUtils.sendMessage(player, "<yellow>[Config] Notificaciones " + status + ".");
            case "autoApply" -> MessageUtils.sendMessage(player, "<green>[Config] Auto-aplicar calidad " + status + ".");
            case "showStats" -> MessageUtils.sendMessage(player, "<dark_aqua>[Config] Mostrar estadísticas " + status + ".");
            case "confirmations" -> MessageUtils.sendMessage(player, "<gold>[Config] Confirmaciones " + status + ".");
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        // Actualizar GUI para mostrar los estados actualizados
        populateInventory();
    }

    private Map<String, Boolean> getPlayerSettings(UUID playerId) {
        // Obtener configuración del jugador desde el plugin
        Map<String, Boolean> settings = MKSurvival.getInstance().getConfigManager().getPlayerSettings(playerId);

        // Si no hay configuración guardada, crear una por defecto
        if (settings == null) {
            settings = new HashMap<>();
            settings.put("notifications", true);
            settings.put("autoApply", false);
            settings.put("showStats", true);
            settings.put("confirmations", true);

            // Guardar configuración por defecto
            savePlayerSettings(playerId, settings);
        }

        return settings;
    }

    private void savePlayerSettings(UUID playerId, Map<String, Boolean> settings) {
        // Guardar configuración del jugador en el plugin
        MKSurvival.getInstance().getConfigManager().savePlayerSettings(playerId, settings);
    }

    // ==================== MÉTODOS DE CÁLCULO ====================

    private double calculateRepairCost(ItemStack item) {
        if (item.getType().getMaxDurability() == 0) return 0;

        short damage = item.getDurability();
        short maxDurability = item.getType().getMaxDurability();
        double damagePercent = (double) damage / maxDurability;

        // Base cost depends on damage percentage
        double baseCost = damagePercent * 1000;

        // Apply quality discount
        ItemQualityManager qualityManager = MKSurvival.getInstance().getItemQualityManager();
        ItemQualityManager.ItemQuality quality = qualityManager.getItemQuality(item);

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

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    // ==================== MÉTODOS ESTÁTICOS ====================

    public static void openMainGUI(Player player) {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.MAIN);
        player.openInventory(gui.getInventory());
    }

    public static void openUpgradeGUI(Player player) {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.UPGRADE);
        player.openInventory(gui.getInventory());
    }

    public static void openEnhanceGUI(Player player) {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.ENHANCE);
        player.openInventory(gui.getInventory());
    }

    public static void openRepairGUI(Player player) {
        ItemQualityGUI gui = new ItemQualityGUI(player, ItemQualityGUIType.REPAIR);
        player.openInventory(gui.getInventory());
    }
}
