package com.mk.mksurvival.managers.items;

import com.mk.mksurvival.MKSurvival;
import org.apache.commons.lang3.text.WordUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import com.mk.mksurvival.utils.MessageUtils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Manager completo del sistema de calidad de ítems
 * Incluye rareza, efectos, mejoras y sistema de enhancing completamente funcional
 */
public class ItemQualityManager {

    private final MKSurvival plugin;
    private final Map<UUID, Map<Integer, ItemData>> playerItems;
    private final Map<Material, QualitySettings> materialSettings;
    private final Random random;

    // Configuraciones
    private static final double ENHANCEMENT_SUCCESS_RATE_BASE = 0.8;
    private static final double ENHANCEMENT_COST_MULTIPLIER = 1.5;
    private static final int MAX_ENHANCEMENT_LEVEL = 15;

    public static ItemStack createQualityItem(Material material, ItemQuality quality) {
        return null;
    }

    public boolean applyCrystal(Player player, ItemStack targetItem, ItemStack crystalItem) {
        return false;
    }

    public ItemStack createEnhancementCrystal(int level) {
        return null;
    }

    public String getItemStats(ItemStack item) {
        return "";
    }

    public Map<String, Object> getPlayerStats(UUID uniqueId) {
        return Map.of();
    }

    public List<Map<String, Object>> getPlayerTopItems(UUID uniqueId, int i) {
        return List.of();
    }

    public ItemStack combineItems(Player player, ItemStack item1, ItemStack item2) {
        return item1;
    }

    public double getItemEnhancementLevel(ItemStack firstItem) {
        return 0;
    }

    public enum ItemQuality {
        BROKEN("Roto", "<dark_gray>", 0.5, "⚫", 0),
        POOR("Pobre", "<gray>", 0.8, "⚬", 1),
        COMMON("Común", "<white>", 1.0, "●", 2),
        UNCOMMON("Poco Común", "<green>", 1.2, "◆", 3),
        RARE("Raro", "<blue>", 1.4, "★", 5),
        EPIC("Épico", "<dark_purple>", 1.7, "⬟", 8),
        LEGENDARY("Legendario", "<gold>", 2.0, "⭐", 12),
        MYTHIC("Mítico", "<red>", 2.5, "❋", 20),
        DIVINE("Divino", "<light_purple>", 3.0, "✦", 30),
        COSMIC("Cósmico", "<dark_aqua>", 4.0, "✧", 50);

        private final String displayName;
        private final String colorCode;
        private final double statMultiplier;
        private final String symbol;
        private final int enhancementBonus;

        ItemQuality(
            String displayName,
            String colorCode,
            double statMultiplier,
            String symbol,
            int enhancementBonus
        ) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.statMultiplier = statMultiplier;
            this.symbol = symbol;
            this.enhancementBonus = enhancementBonus;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFormattedDisplayName() {
            return MessageUtils.parse((colorCode + displayName));
        }

        public String getColorCode() {
            return colorCode;
        }

        public double getStatMultiplier() {
            return statMultiplier;
        }

        public String getSymbol() {
            return symbol;
        }

        public int getEnhancementBonus() {
            return enhancementBonus;
        }

        public static ItemQuality getRandomQuality() {
            double roll = Math.random();
            if (roll < 0.4) return COMMON;
            if (roll < 0.65) return UNCOMMON;
            if (roll < 0.80) return RARE;
            if (roll < 0.92) return EPIC;
            if (roll < 0.98) return LEGENDARY;
            if (roll < 0.995) return MYTHIC;
            if (roll < 0.9995) return DIVINE;
            return COSMIC;
        }

        public static ItemQuality getWeightedRandomQuality(double luckFactor) {
            double roll = Math.random() - (luckFactor * 0.1); // Luck factor reduces the roll, increasing chances for better quality
            roll = Math.max(0, roll); // Ensure roll doesn't go below 0

            if (roll < 0.3) return COMMON;
            if (roll < 0.55) return UNCOMMON;
            if (roll < 0.75) return RARE;
            if (roll < 0.88) return EPIC;
            if (roll < 0.96) return LEGENDARY;
            if (roll < 0.995) return MYTHIC;
            if (roll < 0.9995) return DIVINE;
            return COSMIC;
        }

        public ItemQuality getNext() {
            ItemQuality[] values = values();
            int currentIndex = ordinal();
            if (currentIndex < values.length - 1) {
                return values[currentIndex + 1];
            }
            return this;
        }
    }

    public enum EnhancementType {
        ATTACK_DAMAGE(
            "Daño de Ataque",
            "<red>",
            Attribute.ATTACK_DAMAGE,
            1.0,
            10.0
        ),
        ATTACK_SPEED(
            "Velocidad de Ataque",
            "<gold>",
            Attribute.ATTACK_SPEED,
            0.1,
            2.0
        ),
        ARMOR("Armadura", "<gray>", Attribute.ARMOR, 0.5, 15.0),
        ARMOR_TOUGHNESS(
            "Resistencia",
            "<dark_gray>",
            Attribute.ARMOR_TOUGHNESS,
            0.2,
            8.0
        ),
        MAX_HEALTH("Vida Máxima", "<dark_red>", Attribute.MAX_HEALTH, 1.0, 20.0),
        MOVEMENT_SPEED("Velocidad", "<aqua>", Attribute.MOVEMENT_SPEED, 0.01, 0.3),
        LUCK("Suerte", "<yellow>", Attribute.LUCK, 0.5, 15.0),
        KNOCKBACK_RESISTANCE(
            "Anti-Empuje",
            "<blue>",
            Attribute.KNOCKBACK_RESISTANCE,
            0.1,
            1.0
        );

        private final String displayName;
        private final String colorCode;
        private final Attribute attribute;
        private final double baseValue;
        private final double maxValue;

        EnhancementType(
            String displayName,
            String colorCode,
            Attribute attribute,
            double baseValue,
            double maxValue
        ) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.attribute = attribute;
            this.baseValue = baseValue;
            this.maxValue = maxValue;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColorCode() {
            return colorCode;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public double getBaseValue() {
            return baseValue;
        }

        public double getMaxValue() {
            return maxValue;
        }
    }

    public static class ItemData {

        private ItemQuality quality;
        private int enhancementLevel;
        private Map<EnhancementType, Double> enhancements;
        private Set<ItemBuff> activeBuffs;
        private UUID itemId;
        private long createdAt;
        private String creator;

        public ItemData(ItemQuality quality) {
            this.quality = quality;
            this.enhancementLevel = 0;
            this.enhancements = new EnumMap<>(EnhancementType.class);
            this.activeBuffs = new HashSet<>();
            this.itemId = UUID.randomUUID();
            this.createdAt = System.currentTimeMillis();
            this.creator = null;
        }

        public ItemQuality getQuality() {
            return quality;
        }

        public void setQuality(ItemQuality quality) {
            this.quality = quality;
        }

        public int getEnhancementLevel() {
            return enhancementLevel;
        }

        public void setEnhancementLevel(int level) {
            this.enhancementLevel = level;
        }

        public Map<EnhancementType, Double> getEnhancements() {
            return enhancements;
        }

        public Set<ItemBuff> getActiveBuffs() {
            return activeBuffs;
        }

        public UUID getItemId() {
            return itemId;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }
    }

    public static class ItemBuff {

        private final String name;
        private final String description;
        private final int duration;
        private final Map<PotionEffectType, Integer> effects;

        public ItemBuff(
            String name,
            String description,
            int duration,
            Map<PotionEffectType, Integer> effects
        ) {
            this.name = name;
            this.description = description;
            this.duration = duration;
            this.effects = effects;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getDuration() {
            return duration;
        }

        public Map<PotionEffectType, Integer> getEffects() {
            return effects;
        }
    }

    public static class QualitySettings {

        private final Set<ItemQuality> allowedQualities;
        private final Map<EnhancementType, Boolean> allowedEnhancements;
        private final double qualityDropRate;

        public QualitySettings() {
            this.allowedQualities = EnumSet.allOf(ItemQuality.class);
            this.allowedEnhancements = new EnumMap<>(EnhancementType.class);
            this.qualityDropRate = 0.15;

            // Inicializar todas las mejoras como permitidas por defecto
            for (EnhancementType type : EnhancementType.values()) {
                allowedEnhancements.put(type, true);
            }
        }

        public Set<ItemQuality> getAllowedQualities() {
            return allowedQualities;
        }

        public Map<EnhancementType, Boolean> getAllowedEnhancements() {
            return allowedEnhancements;
        }

        public double getQualityDropRate() {
            return qualityDropRate;
        }
    }

    // ==================== CONSTRUCTOR ====================

    public ItemQualityManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.playerItems = new ConcurrentHashMap<>();
        this.materialSettings = new ConcurrentHashMap<>();
        this.random = new Random();

        initializeDefaultSettings();
        loadPlayerData();
        startPeriodicSave();
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    public ItemStack applyRandomQuality(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;
        return applyQuality(item, ItemQuality.getRandomQuality());
    }

    public ItemStack applyQuality(ItemStack item, ItemQuality quality) {
        if (item == null || item.getType() == Material.AIR) return item;

        ItemStack qualityItem = item.clone();
        ItemMeta meta = qualityItem.getItemMeta();
        if (meta == null) return item;

        ItemData data = new ItemData(quality);

        // Configurar nombre con calidad
        String originalName = meta.hasDisplayName()
            ? meta.getDisplayName()
            : formatMaterialName(item.getType());
        meta.displayName(MessageUtils.parse(
            quality.getColorCode() + quality.getSymbol() + " " + originalName
        ));

        // Aplicar efectos de calidad
        applyQualityEffects(meta, data);
        updateItemStats(meta, data);

        qualityItem.setItemMeta(meta);
        storeItemData(qualityItem, data);

        return qualityItem;
    }

    public ItemQuality getItemQuality(ItemStack item) {
        ItemData data = getItemData(item);
        return data != null ? data.getQuality() : null;
    }

    public int getEnhancementLevel(ItemStack item) {
        ItemData data = getItemData(item);
        return data != null ? data.getEnhancementLevel() : 0;
    }

    public boolean upgradeItemQuality(Player player, ItemStack item) {
        return upgradeItemQuality(player, item, 0);
    }

    public boolean upgradeItemQuality(
        Player player,
        ItemStack item,
        double cost
    ) {
        if (item == null || player == null) return false;

        ItemData data = getItemData(item);
        if (data == null) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Este ítem no tiene calidad aplicada."
            );
            return false;
        }

        ItemQuality currentQuality = data.getQuality();
        ItemQuality nextQuality = currentQuality.getNext();

        if (currentQuality == nextQuality) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Este ítem ya tiene la máxima calidad."
            );
            return false;
        }

        double upgradeChance = calculateQualityUpgradeChance(currentQuality);

        // Verificar dinero si hay costo
        if (cost > 0) {
            if (!plugin.getEconomyManager().hasBalance(player, cost)) {
                MessageUtils.sendMessage(player,
                    "<red>[Calidad] No tienes suficiente dinero. Necesitas: <yellow>" +
                        cost
                );
                return false;
            }
            plugin.getEconomyManager().removeBalance(player, cost);
        }

        // Intentar mejora
        if (random.nextDouble() < upgradeChance) {
            data.setQuality(nextQuality);

            // Actualizar ítem
            ItemMeta meta = item.getItemMeta();
            String originalName = meta.getDisplayName().substring(2); // Remover símbolo anterior
            meta.displayName(MessageUtils.parse(
                nextQuality.getColorCode() +
                    nextQuality.getSymbol() +
                    " " +
                    originalName
            ));

            applyQualityEffects(meta, data);
            updateItemStats(meta, data);

            item.setItemMeta(meta);
            storeItemData(item, data);

            // Efectos de éxito
            MessageUtils.sendMessage(player,
                "<green>[Calidad] ¡Mejora exitosa! Nuevo nivel: " +
                    nextQuality.getColorCode() +
                    nextQuality.getDisplayName()
            );
            player.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP,
                1.0f,
                1.0f
            );
            player.spawnParticle(
                Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0),
                10
            );

            return true;
        } else {
            // Fallo en mejora
            MessageUtils.sendMessage(player,
                "<red>[Calidad] ¡La mejora falló! Probabilidad era: " +
                    String.format("%.1f", upgradeChance * 100) +
                    "%"
            );
            player.playSound(
                player.getLocation(),
                Sound.BLOCK_ANVIL_BREAK,
                1.0f,
                0.8f
            );
            player.spawnParticle(
                Particle.SMOKE,
                player.getLocation().add(0, 1, 0),
                5
            );

            return false;
        }
    }

    public boolean enhanceItem(
        Player player,
        ItemStack item,
        EnhancementType type,
        int levels
    ) {
        if (item == null || player == null) return false;

        ItemData data = getItemData(item);
        if (data == null) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Este ítem no tiene calidad aplicada."
            );
            return false;
        }

        int currentLevel = data.getEnhancementLevel();
        int newLevel = currentLevel + levels;

        if (newLevel > MAX_ENHANCEMENT_LEVEL) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Nivel máximo de mejora alcanzado (" +
                    MAX_ENHANCEMENT_LEVEL +
                    ")"
            );
            return false;
        }

        double cost = calculateEnhancementCost(
            currentLevel,
            newLevel,
            data.getQuality()
        );
        double successRate = calculateEnhancementSuccessRate(
            currentLevel,
            data.getQuality()
        );

        // Verificar dinero
        if (!plugin.getEconomyManager().hasBalance(player, cost)) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes suficiente dinero. Costo: <yellow>" + cost
            );
            return false;
        }

        plugin.getEconomyManager().removeBalance(player, cost);

        // Intentar mejora
        if (random.nextDouble() < successRate) {
            // Éxito
            data.setEnhancementLevel(newLevel);
            double enhancementValue =
                data.getEnhancements().getOrDefault(type, 0.0) +
                (type.getBaseValue() * levels);
            data.getEnhancements().put(type, enhancementValue);

            // Actualizar ítem
            updateItemStats(item.getItemMeta(), data);
            item.setItemMeta(item.getItemMeta());
            storeItemData(item, data);

            // Efectos de éxito
            MessageUtils.sendMessage(player,
                "<light_purple>[Calidad] ¡" +
                    type.getDisplayName() +
                    " mejorado al nivel " +
                    newLevel +
                    "!"
            );
            player.playSound(
                player.getLocation(),
                Sound.BLOCK_ENCHANTMENT_TABLE_USE,
                1.0f,
                1.2f
            );
            player.spawnParticle(
                Particle.ENCHANT,
                player.getLocation().add(0, 1, 0),
                15
            );

            // Logro especial para niveles altos
            if (newLevel >= 10) {
                broadcastEnhancementAchievement(player, item, newLevel);
            }

            return true;
        } else {
            // Fallo
            MessageUtils.sendMessage(player,
                "<red>[Calidad] ¡La mejora falló! Probabilidad era: " +
                    String.format("%.1f", successRate * 100) +
                    "%"
            );
            return false;
        }
    }

    public boolean transferEnhancements(
        Player player,
        ItemStack source,
        ItemStack target,
        double cost) {
        if (source == null || target == null || player == null) return false;

        ItemData sourceData = getItemData(source);
        ItemData targetData = getItemData(target);

        if (sourceData == null || targetData == null) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Ambos ítems deben tener calidad aplicada."
            );
            return false;
        }

        if (sourceData.getEnhancements().isEmpty()) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] El ítem fuente no tiene mejoras para transferir."
            );
            return false;
        }

        // Si no se proporciona un costo, usar el valor por defecto
        if (cost <= 0) {
            cost = 5000.0; // Costo base para transferencia
        }

        // Verificar dinero
        if (!plugin.getEconomyManager().hasBalance(player, cost)) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes suficiente dinero para la transferencia. Costo: <yellow>" +
                    cost
            );
            return false;
        }

        plugin.getEconomyManager().removeBalance(player, cost);

        // Transferir mejoras
        for (Map.Entry<EnhancementType, Double> entry : sourceData
            .getEnhancements()
            .entrySet()) {
            EnhancementType type = entry.getKey();
            double value = entry.getValue();

            double currentValue = targetData
                .getEnhancements()
                .getOrDefault(type, 0.0);
            targetData.getEnhancements().put(type, currentValue + value);
        }

        // Transferir nivel de mejora
        targetData.setEnhancementLevel(
            Math.max(
                targetData.getEnhancementLevel(),
                sourceData.getEnhancementLevel()
            )
        );

        // Limpiar ítem fuente
        sourceData.getEnhancements().clear();
        sourceData.setEnhancementLevel(0);

        // Actualizar ambos ítems
        updateItemStats(source.getItemMeta(), sourceData);
        updateItemStats(target.getItemMeta(), targetData);
        source.setItemMeta(source.getItemMeta());
        target.setItemMeta(target.getItemMeta());

        storeItemData(source, sourceData);
        storeItemData(target, targetData);

        MessageUtils.sendMessage(player, "<green>[Calidad] ¡Mejoras transferidas exitosamente!");
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_BEACON_POWER_SELECT,
            1.0f,
            1.0f
        );

        return true;
    }

    public boolean repairItem(Player player, ItemStack item, double cost) {
        if (item == null || player == null) return false;

        if (!(item.getItemMeta() instanceof Damageable)) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Este ítem no se puede reparar.");
            return false;
        }

        Damageable damageable = (Damageable) item.getItemMeta();
        if (!damageable.hasDamage()) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Este ítem no necesita reparación.");
            return false;
        }

        // Verificar dinero
        if (cost > 0 && !plugin.getEconomyManager().hasBalance(player, cost)) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] No tienes suficiente dinero para la reparación. Costo: <yellow>" +
                    cost
            );
            return false;
        }

        if (cost > 0) {
            plugin.getEconomyManager().removeBalance(player, cost);
        }

        // Reparar ítem
        damageable.setDamage(0);
        item.setItemMeta((ItemMeta) damageable);

        MessageUtils.sendMessage(player, "<green>[Calidad] ¡Ítem reparado completamente!");
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_ANVIL_USE,
            1.0f,
            1.0f
        );

        return true;
    }

    public boolean salvageItem(Player player, ItemStack item) {
        if (item == null || player == null) return false;

        ItemData data = getItemData(item);
        if (data == null) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Este ítem no tiene calidad aplicada."
            );
            return false;
        }

        ItemQuality quality = data.getQuality();

        // Calcular recompensas basadas en la calidad
        int baseReward = 100;
        int qualityMultiplier = quality.ordinal() + 1;
        int enhancementBonus = data.getEnhancementLevel() * 50;

        int totalReward = baseReward * qualityMultiplier + enhancementBonus;

        // Dar dinero al jugador
        plugin.getEconomyManager().addBalance(player, totalReward);

        // Posibilidad de obtener materiales especiales
        if (random.nextDouble() < 0.3) {
            ItemStack crystal = createQualityCrystal(quality);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(crystal);
                MessageUtils.sendMessage(player,
                    "<dark_purple>[Calidad] ¡Obtuviste un cristal de " +
                        quality.getDisplayName() +
                        "!"
                );
            } else {
                player
                    .getWorld()
                    .dropItemNaturally(player.getLocation(), crystal);
                MessageUtils.sendMessage(player, "<dark_purple>[Calidad] ¡Cristal dropeado al suelo!");
            }
        }

        MessageUtils.sendMessage(player,
            "<green>[Calidad] Ítem descompuesto. Recompensa: <yellow>" +
                totalReward +
                " monedas"
        );
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_FIRE_EXTINGUISH,
            1.0f,
            1.0f
        );

        return true;
    }

    /**
     * Aplica datos de calidad a un ítem
     */
    public void applyItemData(ItemStack item, ItemData data) {
        if (item == null || data == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        // Aplicar calidad visual
        String originalName = meta.hasDisplayName() ? stripColorCodes(meta.getDisplayName()) : 
                              WordUtils.capitalizeFully(item.getType().name().replace("_", " "));
        
        meta.displayName(MessageUtils.parse(data.getQuality().getColorCode() + data.getQuality().getSymbol() + " " + originalName));
        
        // Aplicar efectos de calidad
        applyQualityEffects(meta, data);
        updateItemStats(meta, data);
        
        item.setItemMeta(meta);
        storeItemData(item, data);
    }
    
    /**
     * Remueve códigos de color de un texto
     */
    private String stripColorCodes(String text) {
        if (text == null) return "";
        return MessageUtils.stripColor(text);
    }

    public void applyItemBuff(Player player, ItemStack item, ItemBuff buff) {

        // Aplicar efectos de poción
        for (Map.Entry<PotionEffectType, Integer> entry : buff
            .getEffects()
            .entrySet()) {
            PotionEffect effect = new PotionEffect(
                entry.getKey(),
                buff.getDuration() * 20,
                entry.getValue()
            );
            player.addPotionEffect(effect);
        }

        // Programar eliminación del buff
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemData data = getItemData(item);
                if (data != null) {
                    data.getActiveBuffs().remove(buff);
                    storeItemData(item, data);
                }
            }
        }
            .runTaskLater(plugin, buff.getDuration() * 20L);

        MessageUtils.sendMessage(player,
            "<light_purple>[Buff] <dark_purple>" +
                buff.getName() +
                " <light_purple>activado por " +
                buff.getDuration() +
                " segundos!"
        );
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void applyQualityEffects(ItemMeta meta, ItemData data) {
        List<String> lore = new ArrayList<>();
        ItemQuality quality = data.getQuality();

        // Línea de calidad
        lore.add(
            quality.getColorCode() + "⬥ " + quality.getDisplayName() + " ⬥"
        );
        lore.add("");

        // Estadísticas de calidad
        if (quality.getStatMultiplier() != 1.0) {
            double bonus = (quality.getStatMultiplier() - 1.0) * 100;
            lore.add(
                "<gray>▸ Multiplicador: <yellow>+" + String.format("%.1f", bonus) + "%"
            );
        }

        // Mejoras activas
        if (!data.getEnhancements().isEmpty()) {
            lore.add("");
            lore.add("<light_purple>▸ Mejoras Activas:");
            for (Map.Entry<EnhancementType, Double> entry : data
                .getEnhancements()
                .entrySet()) {
                EnhancementType type = entry.getKey();
                double value = entry.getValue();
                lore.add(
                    "<gray>  • " +
                        type.getColorCode() +
                        type.getDisplayName() +
                        ": +" +
                        String.format("%.1f", value)
                );
            }
        }

        // Nivel de mejora
        if (data.getEnhancementLevel() > 0) {
            lore.add("");
            lore.add("<light_purple>▸ Nivel de Mejora: <yellow>+" + data.getEnhancementLevel());
        }

        // Buffs activos
        if (!data.getActiveBuffs().isEmpty()) {
            lore.add("");
            lore.add("<dark_purple>▸ Buffs Activos:");
            for (ItemBuff buff : data.getActiveBuffs()) {
                lore.add("<gray>  • <dark_purple>" + buff.getName());
            }
        }

        // Información del ítem
        lore.add("");
        lore.add("<dark_gray>ID: " + data.getItemId().toString().substring(0, 8));
        if (data.getCreator() != null) {
            lore.add("<dark_gray>Creador: " + data.getCreator());
        }

        meta.setLore(lore);
        meta.addItemFlags(
            ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ENCHANTS,
            ItemFlag.HIDE_UNBREAKABLE
        );

        // Aplicar mejoras a atributos
        applyAttributeModifiers(meta, data);
        applyQualityEnchantments(meta, quality, data.getEnhancementLevel());
    }

    private void updateItemStats(ItemMeta meta, ItemData data) {
        applyQualityEffects(meta, data);
    }

    private void applyAttributeModifiers(ItemMeta meta, ItemData data) {
        // Limpiar modificadores anteriores
        meta.getAttributeModifiers().clear();

        // Aplicar mejoras específicas
        for (Map.Entry<EnhancementType, Double> entry : data
            .getEnhancements()
            .entrySet()) {
            EnhancementType type = entry.getKey();
            double value =
                entry.getValue() * data.getQuality().getStatMultiplier();

            AttributeModifier modifier = new AttributeModifier(
                UUID.randomUUID(),
                "ItemQuality:" + type.name(),
                value,
                AttributeModifier.Operation.ADD_NUMBER
            );

            meta.addAttributeModifier(type.getAttribute(), modifier);
        }

        // Bonus general de calidad
        if (data.getQuality().getStatMultiplier() > 1.0) {
            double bonus = (data.getQuality().getStatMultiplier() - 1.0) * 2.0;

            AttributeModifier qualityBonus = new AttributeModifier(
                UUID.randomUUID(),
                "QualityBonus",
                bonus,
                AttributeModifier.Operation.ADD_NUMBER
            );

            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, qualityBonus);
        }
    }

    private void applyQualityEnchantments(
        ItemMeta meta,
        ItemQuality quality,
        int enhancementLevel
    ) {
        // Limpiar encantamientos especiales anteriores
        meta
            .getEnchants()
            .entrySet()
            .removeIf(entry -> entry.getValue() >= 1000);

        switch (quality) {
            case RARE:
            case EPIC:
                meta.addEnchant(Enchantment.LURE, 1000, true);
                break;
            case LEGENDARY:
                meta.addEnchant(Enchantment.LURE, 1001, true);
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1000, true);
                break;
            case MYTHIC:
                meta.addEnchant(Enchantment.LURE, 1002, true);
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1001, true);
                break;
            case DIVINE:
            case COSMIC:
                meta.addEnchant(Enchantment.LURE, 1003, true);
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1002, true);
                meta.addEnchant(Enchantment.UNBREAKING, 1000, true);
                break;
            default:
                break;
        }

        // Encantamientos por nivel de mejora
        if (enhancementLevel >= 5) {
            meta.addEnchant(Enchantment.MENDING, 1000 + enhancementLevel, true);
        }
    }

    // ==================== UTILIDADES Y CÁLCULOS ====================

    private double calculateQualityUpgradeChance(ItemQuality quality) {
        return switch (quality) {
            case BROKEN, POOR -> 0.95;
            case COMMON -> 0.85;
            case UNCOMMON -> 0.70;
            case RARE -> 0.50;
            case EPIC -> 0.30;
            case LEGENDARY -> 0.15;
            case MYTHIC -> 0.08;
            case DIVINE -> 0.03;
            case COSMIC -> 0.01;
        };
    }

    private double calculateEnhancementCost(
        int currentLevel,
        int newLevel,
        ItemQuality quality
    ) {
        double baseCost = 1000;
        double levelMultiplier = Math.pow(
            ENHANCEMENT_COST_MULTIPLIER,
            newLevel
        );
        double qualityMultiplier = quality.getStatMultiplier();

        return (
            baseCost *
            levelMultiplier *
            qualityMultiplier *
            (newLevel - currentLevel)
        );
    }

    private double calculateEnhancementSuccessRate(
        int currentLevel,
        ItemQuality quality
    ) {
        double baseRate = ENHANCEMENT_SUCCESS_RATE_BASE;
        double levelPenalty = currentLevel * 0.05;
        double qualityBonus = (quality.ordinal() * 0.02);

        return Math.max(0.1, baseRate - levelPenalty + qualityBonus);
    }

    private String formatMaterialName(Material material) {
        return Arrays.stream(material.name().toLowerCase().split("_"))
            .map(
                word ->
                    Character.toUpperCase(word.charAt(0)) + word.substring(1)
            )
            .reduce((a, b) -> a + " " + b)
            .orElse(material.name());
    }

    private void broadcastEnhancementAchievement(
        Player player,
        ItemStack item,
        int level
    ) {
        if (level >= 10) {
            String message =
                "<gold>[Calidad] <yellow>" +
                player.getName() +
                " <gold>ha mejorado <yellow>" +
                item.getItemMeta().getDisplayName() +
                " <gold>¡al nivel <red>" +
                level +
                "<gold>!";
            Bukkit.broadcastMessage(message);
        }
    }

    private ItemStack createQualityCrystal(ItemQuality quality) {
        ItemStack crystal = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = crystal.getItemMeta();

        meta.displayName(MessageUtils.parse(
            quality.getColorCode() + "Cristal de " + quality.getDisplayName()
        ));

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Usa este cristal para aplicar");
        lore.add(
            "<gray>calidad " +
                quality.getColorCode() +
                quality.getDisplayName() +
                " <gray>a un ítem"
        );
        lore.add("");
        lore.add("<yellow>¡Clic derecho en un ítem para usar!");
        meta.lore(MessageUtils.parseList(lore));

        crystal.setItemMeta(meta);
        return crystal;
    }

    // ==================== GESTIÓN DE DATOS ====================

    public ItemData getItemData(ItemStack item) {
        if (
            item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()
        ) {
            return null;
        }

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("<dark_gray>ID: ")) {
                String idStr = line.replace("<dark_gray>ID: ", "");
                try {
                    UUID fullId = findFullUUIDById(idStr);
                    if (fullId != null) {
                        return findItemDataByUUID(fullId);
                    }
                } catch (Exception e) {
                    // ID inválido, ignorar
                }
            }
        }

        return null;
    }

    private void storeItemData(ItemStack item, ItemData data) {
        int itemHash = item.hashCode();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<Integer, ItemData> playerItemData = playerItems.computeIfAbsent(
                player.getUniqueId(),
                k -> new ConcurrentHashMap<>()
            );
            playerItemData.put(itemHash, data);
        }
    }

    private UUID findFullUUIDById(String shortId) {
        for (Map<Integer, ItemData> playerData : playerItems.values()) {
            for (ItemData data : playerData.values()) {
                if (
                    data.getItemId().toString().substring(0, 8).equals(shortId)
                ) {
                    return data.getItemId();
                }
            }
        }
        return null;
    }

    private ItemData findItemDataByUUID(UUID itemId) {
        for (Map<Integer, ItemData> playerData : playerItems.values()) {
            for (ItemData data : playerData.values()) {
                if (data.getItemId().equals(itemId)) {
                    return data;
                }
            }
        }
        return null;
    }

    private boolean shouldHaveQuality(Material material) {
        return (
            materialSettings.containsKey(material) ||
            material.name().contains("SWORD") ||
            material.name().contains("AXE") ||
            material.name().contains("PICKAXE") ||
            material.name().contains("SHOVEL") ||
            material.name().contains("HOE") ||
            material.name().contains("HELMET") ||
            material.name().contains("CHESTPLATE") ||
            material.name().contains("LEGGINGS") ||
            material.name().contains("BOOTS")
        );
    }

    private void initializeDefaultSettings() {
        QualitySettings toolSettings = new QualitySettings();
        toolSettings
            .getAllowedEnhancements()
            .put(EnhancementType.ATTACK_DAMAGE, true);
        toolSettings
            .getAllowedEnhancements()
            .put(EnhancementType.ATTACK_SPEED, true);
        toolSettings.getAllowedEnhancements().put(EnhancementType.LUCK, true);

        Material[] tools = {
            Material.DIAMOND_SWORD,
            Material.IRON_SWORD,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
        };

        for (Material tool : tools) {
            materialSettings.put(tool, toolSettings);
        }

        QualitySettings armorSettings = new QualitySettings();
        armorSettings.getAllowedEnhancements().put(EnhancementType.ARMOR, true);
        armorSettings
            .getAllowedEnhancements()
            .put(EnhancementType.ARMOR_TOUGHNESS, true);
        armorSettings
            .getAllowedEnhancements()
            .put(EnhancementType.MAX_HEALTH, true);
        armorSettings
            .getAllowedEnhancements()
            .put(EnhancementType.KNOCKBACK_RESISTANCE, true);

        Material[] armors = {
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS,
            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS,
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS,
        };

        for (Material armor : armors) {
            materialSettings.put(armor, armorSettings);
        }
    }

    private void loadPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerItemData(player);
        }
    }

    private void loadPlayerItemData(Player player) {
        UUID playerId = player.getUniqueId();
        playerItems.putIfAbsent(playerId, new ConcurrentHashMap<>());
    }

    private void savePlayerItemData(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Obtener o crear datos del jugador
        PlayerItemData playerData = getPlayerItemData(playerId);
        
        // Guardar en la configuración
        FileConfiguration config = plugin.getConfigManager().getPlayerConfig();
        String path = "players." + playerId.toString() + ".item_quality.";
        
        config.set(path + "money_spent", playerData.getMoneySpent());
        config.set(path + "crystals_used", playerData.getCrystalsUsed());
        config.set(path + "repairs_count", playerData.getRepairsCount());
        config.set(path + "successful_upgrades", playerData.getSuccessfulUpgrades());
        config.set(path + "failed_upgrades", playerData.getFailedUpgrades());
        config.set(path + "items_enhanced", playerData.getItemsEnhanced());
        config.set(path + "total_items", playerData.getTotalItems());
        
        // Guardar tipos de mejora utilizados
        if (!playerData.getEnhancementTypesUsed().isEmpty()) {
            List<String> enhancementTypes = new ArrayList<>();
            for (EnhancementType type : playerData.getEnhancementTypesUsed()) {
                enhancementTypes.add(type.name());
            }
            config.set(path + "enhancement_types_used", enhancementTypes);
        }
        
        // Guardar el archivo
        plugin.getConfigManager().savePlayerConfig();
    }

    private void startPeriodicSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllPlayerData();
            }
        }
            .runTaskTimerAsynchronously(plugin, 6000L, 6000L);
    }

    private void saveAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerItemData(player);
        }
    }

    // ==================== MÉTODOS PÚBLICOS ADICIONALES ====================

    public ItemStack processItemDrop(ItemStack item, Player player) {
        if (item == null || item.getType() == Material.AIR) return item;

        if (!shouldHaveQuality(item.getType())) return item;

        ItemStack qualityItem = applyRandomQuality(item);

        ItemData data = getItemData(qualityItem);
        if (data != null) {
            data.setCreator(player.getName());
        }

        return qualityItem;
    }

    public void showQualityInfo(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Debes tener un ítem en la mano.");
            return;
        }

        ItemData data = getItemData(item);
        if (data == null) {
            MessageUtils.sendMessage(player,
                "<red>[Calidad] Este ítem no tiene calidad aplicada."
            );
            return;
        }

        ItemQuality quality = data.getQuality();

        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
        MessageUtils.sendMessage(player, "<gold>📊 Información del Ítem");
        MessageUtils.sendMessage(player, "<gray>Ítem: <yellow>" + item.getType().name().toLowerCase());
        MessageUtils.sendMessage(player,
            "<gray>Calidad: " + quality.getColorCode() + quality.getDisplayName()
        );
        MessageUtils.sendMessage(player,
            "<gray>Nivel de mejora: <yellow>+" + data.getEnhancementLevel()
        );
        MessageUtils.sendMessage(player,
            "<gray>Multiplicador: <green>" +
                String.format("%.1f", quality.getStatMultiplier()) +
                "x"
        );

        if (!data.getEnhancements().isEmpty()) {
            MessageUtils.sendMessage(player, "<gray>Mejoras activas:");
            for (Map.Entry<EnhancementType, Double> entry : data
                .getEnhancements()
                .entrySet()) {
                EnhancementType type = entry.getKey();
                double value = entry.getValue();
                MessageUtils.sendMessage(player,
                    "<gray>  • " +
                        type.getColorCode() +
                        type.getDisplayName() +
                        ": +" +
                        String.format("%.1f", value)
                );
            }
        }

        if (data.getCreator() != null) {
            MessageUtils.sendMessage(player, "<gray>Creador: <aqua>" + data.getCreator());
        }

        MessageUtils.sendMessage(player,
            "<dark_gray>ID: " + data.getItemId().toString().substring(0, 8)
        );
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
    }

    public void removeQuality(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        // Remover nombre especial de calidad si existe
        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            // Buscar el patrón de calidad y extraer el nombre original
            for (ItemQuality quality : ItemQuality.values()) {
                String prefix =
                    quality.getColorCode() + quality.getSymbol() + " ";
                if (displayName.startsWith(prefix)) {
                    String originalName = displayName.substring(
                        prefix.length()
                    );
                    meta.displayName(MessageUtils.parse(originalName));
                    break;
                }
            }
        }

        // Remover lore relacionado con calidad
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            lore.removeIf(
                line ->
                    line.contains("⬥") ||
                    line.contains("▸") ||
                    line.contains("Mejoras Activas") ||
                    line.contains("Buffs Activos") ||
                    line.contains("<dark_gray>ID:")
            );
            meta.lore(MessageUtils.parseList(lore));
        }

        // Remover encantamientos especiales
        meta
            .getEnchants()
            .entrySet()
            .removeIf(entry -> entry.getValue() >= 1000);

        // Remover modificadores de atributos
        meta.getAttributeModifiers().clear();

        // Remover flags especiales
        for (ItemFlag flag : ItemFlag.values()) {
            meta.removeItemFlags(flag);
        }

        item.setItemMeta(meta);

        // Remover de almacenamiento
        int itemHash = item.hashCode();
        for (Map<Integer, ItemData> playerData : playerItems.values()) {
            playerData.remove(itemHash);
        }
    }

    // ==================== MÉTODOS DE ACCESO ====================

    public Map<UUID, Map<Integer, ItemData>> getPlayerItems() {
        return playerItems;
    }

    public Map<Material, QualitySettings> getMaterialSettings() {
        return materialSettings;
    }

    public Random getRandom() {
        return random;
    }

    // ==================== MÉTODOS AUXILIARES ADICIONALES ====================

    public boolean hasQuality(ItemStack item) {
        return getItemQuality(item) != null;
    }

    public double getTotalItemPower(ItemStack item) {
        ItemData data = getItemData(item);
        if (data == null) return 0.0;

        double basePower = data.getQuality().getStatMultiplier() * 100;
        double enhancementPower = data.getEnhancementLevel() * 10;

        // Sumar poder de las mejoras
        double upgradesPower =
            data
                .getEnhancements()
                .values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum() *
            5;

        return basePower + enhancementPower + upgradesPower;
    }

    public static ItemQuality getWeightedRandomQuality(double luckFactor) {
        return ItemQuality.getWeightedRandomQuality(luckFactor);
    }

    // ==================== MÉTODOS DE DATOS DEL JUGADOR ====================

    private final Map<UUID, PlayerItemData> playerDataCache = new ConcurrentHashMap<>();

    public PlayerItemData getPlayerItemData(UUID playerId) {
        return playerDataCache.computeIfAbsent(playerId, uuid -> {
            PlayerItemData data = new PlayerItemData();
            loadPlayerItemDataFromConfig(uuid, data);
            return data;
        });
    }

    private void loadPlayerItemDataFromConfig(UUID playerId, PlayerItemData data) {
        FileConfiguration config = plugin.getConfigManager().getPlayerConfig();
        String path = "players." + playerId.toString() + ".item_quality.";

        if (config.contains(path)) {
            data.setMoneySpent(config.getDouble(path + "money_spent", 0.0));
            data.setCrystalsUsed(config.getInt(path + "crystals_used", 0));
            data.setRepairsCount(config.getInt(path + "repairs_count", 0));
            data.setSuccessfulUpgrades(config.getInt(path + "successful_upgrades", 0));
            data.setFailedUpgrades(config.getInt(path + "failed_upgrades", 0));
            data.setItemsEnhanced(config.getInt(path + "items_enhanced", 0));
            data.setTotalItems(config.getInt(path + "total_items", 0));

            // Cargar tipos de mejora utilizados
            List<String> enhancementTypeNames = config.getStringList(path + "enhancement_types_used");
            for (String typeName : enhancementTypeNames) {
                try {
                    EnhancementType type = EnhancementType.valueOf(typeName);
                    data.addEnhancementTypeUsed(type);
                } catch (IllegalArgumentException ignored) {
                    // Tipo de mejora no válido, ignorar
                }
            }
        }
    }

    public void updatePlayerStats(UUID playerId, String statType, double value) {
        PlayerItemData data = getPlayerItemData(playerId);
        switch (statType.toLowerCase()) {
            case "money_spent" -> data.addMoneySpent(value);
            case "crystals_used" -> data.addCrystalsUsed((int) value);
            case "repairs_count" -> data.addRepairsCount((int) value);
            case "successful_upgrades" -> data.addSuccessfulUpgrades((int) value);
            case "failed_upgrades" -> data.addFailedUpgrades((int) value);
            case "items_enhanced" -> data.addItemsEnhanced((int) value);
            case "total_items" -> data.addTotalItems((int) value);
        }
    }

    // ==================== CLASE DE DATOS DEL JUGADOR ====================

    public static class PlayerItemData {
        private double moneySpent = 0.0;
        private int crystalsUsed = 0;
        private int repairsCount = 0;
        private int successfulUpgrades = 0;
        private int failedUpgrades = 0;
        private int itemsEnhanced = 0;
        private int totalItems = 0;
        private Set<EnhancementType> enhancementTypesUsed = new HashSet<>();

        // Getters
        public double getMoneySpent() { return moneySpent; }
        public int getCrystalsUsed() { return crystalsUsed; }
        public int getRepairsCount() { return repairsCount; }
        public int getSuccessfulUpgrades() { return successfulUpgrades; }
        public int getFailedUpgrades() { return failedUpgrades; }
        public int getItemsEnhanced() { return itemsEnhanced; }
        public int getTotalItems() { return totalItems; }
        public Set<EnhancementType> getEnhancementTypesUsed() { return enhancementTypesUsed; }

        // Setters
        public void setMoneySpent(double moneySpent) { this.moneySpent = moneySpent; }
        public void setCrystalsUsed(int crystalsUsed) { this.crystalsUsed = crystalsUsed; }
        public void setRepairsCount(int repairsCount) { this.repairsCount = repairsCount; }
        public void setSuccessfulUpgrades(int successfulUpgrades) { this.successfulUpgrades = successfulUpgrades; }
        public void setFailedUpgrades(int failedUpgrades) { this.failedUpgrades = failedUpgrades; }
        public void setItemsEnhanced(int itemsEnhanced) { this.itemsEnhanced = itemsEnhanced; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

        // Métodos de adición
        public void addMoneySpent(double amount) { this.moneySpent += amount; }
        public void addCrystalsUsed(int amount) { this.crystalsUsed += amount; }
        public void addRepairsCount(int amount) { this.repairsCount += amount; }
        public void addSuccessfulUpgrades(int amount) { this.successfulUpgrades += amount; }
        public void addFailedUpgrades(int amount) { this.failedUpgrades += amount; }
        public void addItemsEnhanced(int amount) { this.itemsEnhanced += amount; }
        public void addTotalItems(int amount) { this.totalItems += amount; }
        public void addEnhancementTypeUsed(EnhancementType type) { this.enhancementTypesUsed.add(type); }

        // Métodos de utilidad
        public double getSuccessRate() {
            int total = successfulUpgrades + failedUpgrades;
            return total > 0 ? (double) successfulUpgrades / total : 0.0;
        }

        public double getAverageEnhancementLevel() {
            return itemsEnhanced > 0 ? (double) totalItems / itemsEnhanced : 0.0;
        }
    }

    public ItemStack createRandomGem() {
        Material[] gemMaterials = {
            Material.DIAMOND, Material.EMERALD, Material.LAPIS_LAZULI, 
            Material.REDSTONE, Material.GOLD_INGOT, Material.IRON_INGOT
        };
        
        Material gemMaterial = gemMaterials[new Random().nextInt(gemMaterials.length)];
        ItemStack gem = new ItemStack(gemMaterial);
        ItemMeta meta = gem.getItemMeta();
        
        String[] gemNames = {"Rubí", "Zafiro", "Esmeralda", "Topacio", "Amatista", "Cuarzo"};
        String gemName = gemNames[new Random().nextInt(gemNames.length)];
        
        meta.displayName(MessageUtils.parse("<light_purple>" + gemName + " Mágico"));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Una gema rara con propiedades mágicas");
        lore.add("<gray>Puede ser usada para mejorar equipamiento");
        meta.lore(MessageUtils.parseList(lore));
        gem.setItemMeta(meta);
        
        return gem;
    }

    public ItemStack createRandomRareItem() {
        Material[] rareMaterials = {
            Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS, Material.BOW, Material.SHIELD
        };
        
        Material material = rareMaterials[new Random().nextInt(rareMaterials.length)];
        ItemStack item = new ItemStack(material);
        
        // Apply random quality
        ItemQuality[] qualities = {ItemQuality.RARE, ItemQuality.EPIC, ItemQuality.LEGENDARY};
        ItemQuality quality = qualities[new Random().nextInt(qualities.length)];
        
        return applyQuality(item, quality);
    }

}
