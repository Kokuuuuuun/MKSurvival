package com.mk.mksurvival.listeners.items;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;

import com.mk.mksurvival.gui.items.ItemQualityGUI;
import com.mk.mksurvival.managers.items.ItemQualityManager;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener completo para todos los eventos relacionados con el sistema de calidad de ítems
 * Maneja aplicación automática, interacciones, GUI y efectos especiales
 */
public class ItemQualityListener implements Listener {

    private final MKSurvival plugin;
    private final Random random;

    public ItemQualityListener(MKSurvival plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    // ==================== EVENTOS DE GUI ====================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // Verificar si es una GUI de calidad de ítems
        if (event.getInventory().getHolder() instanceof ItemQualityGUI) {
            ItemQualityGUI gui = (ItemQualityGUI) event
                .getInventory()
                .getHolder();
            gui.handleClick(event);
        }

        // Manejar interacciones especiales con ítems de calidad
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Verificar combinación de cristales
        if (isCrystalCombination(clicked, cursor)) {
            handleCrystalCombination(event, player, clicked, cursor);
        }
    }

    // ==================== EVENTOS DE OBTENCIÓN DE ÍTEMS ====================

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Aplicar calidad a ítems dropeados por minería
        if (shouldApplyQualityToMiningDrop(event.getBlock().getType())) {
            ItemQualityManager qualityManager = plugin.getItemQualityManager();

            // Calcular factor de suerte basado en herramientas y habilidades
            double luckFactor = calculateLuckFactor(player);

            // Aplicar calidad con probabilidad
            if (
                random.nextDouble() <
                getQualityDropChance(event.getBlock().getType())
            ) {
                ItemQualityManager.ItemQuality quality =
                    ItemQualityManager.getWeightedRandomQuality(luckFactor);

                // Aplicar a los drops
                event
                    .getBlock()
                    .getDrops(player.getInventory().getItemInMainHand())
                    .forEach(drop -> {
                        ItemStack qualityItem = qualityManager.applyQuality(
                            drop,
                            quality
                        );

                        // Notificar al jugador si es calidad alta
                        if (
                            quality.ordinal() >=
                            ItemQualityManager.ItemQuality.RARE.ordinal()
                        ) {
                            MessageUtils.sendMessage(player,
                                "<gold>[Calidad] <dark_green>¡Encontraste " +
                                    quality.getColorCode() +
                                    quality.getDisplayName() +
                                    " <dark_green>" +
                                    qualityItem.getType().name().toLowerCase() +
                                    "!"
                            );
                            player.playSound(
                                player.getLocation(),
                                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                1.0f,
                                1.5f
                            );
                        }
                    });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        Player killer = event.getEntity().getKiller();

        // Aplicar calidad a drops de mobs
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        double luckFactor = calculateLuckFactor(killer);

        // Procesar cada drop
        event
            .getDrops()
            .replaceAll(drop -> {
                if (shouldApplyQualityToMobDrop(drop.getType())) {
                    double dropChance = getMobDropQualityChance(
                        event.getEntity().getType()
                    );

                    if (random.nextDouble() < dropChance) {
                        ItemQualityManager.ItemQuality quality =
                            ItemQualityManager.getWeightedRandomQuality(
                                luckFactor
                            );
                        ItemStack qualityItem = qualityManager.applyQuality(
                            drop,
                            quality
                        );

                        // Efectos especiales para drops épicos o superiores
                        if (
                            quality.ordinal() >=
                            ItemQualityManager.ItemQuality.EPIC.ordinal()
                        ) {
                            MessageUtils.sendMessage(killer,
                                "<gold>[Calidad] <dark_purple>¡Drop épico! " +
                                    quality.getColorCode() +
                                    quality.getDisplayName() +
                                    " <dark_purple>" +
                                    drop.getType().name().toLowerCase() +
                                    "!"
                            );
                            killer.playSound(
                                killer.getLocation(),
                                Sound.ENTITY_PLAYER_LEVELUP,
                                1.0f,
                                1.2f
                            );

                            // Efectos de partículas
                            killer
                                .getWorld()
                                .spawnParticle(
                                    Particle.FIREWORK,
                                    event.getEntity().getLocation(),
                                    20,
                                    1,
                                    1,
                                    1,
                                    0.1
                                );
                        }

                        return qualityItem;
                    }
                }
                return drop;
            });
    }

    // ==================== EVENTOS DE INTERACCIÓN ====================

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        // Verificar si es la herramienta de calidad
        if (isQualityTool(item)) {
            handleQualityToolUse(event, player, item);
            return;
        }

        // Verificar si es un cristal de mejora
        if (isEnhancementCrystal(item)) {
            handleCrystalUse(event, player, item);
            return;
        }

        // Efectos especiales para ítems de alta calidad
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        if (qualityManager.hasQuality(item)) {
            ItemQualityManager.ItemQuality quality =
                qualityManager.getItemQuality(item);

            // Efectos especiales según la calidad
            if (
                quality.ordinal() >=
                ItemQualityManager.ItemQuality.LEGENDARY.ordinal()
            ) {
                // Efectos de partículas para ítems legendarios+
                player
                    .getWorld()
                    .spawnParticle(
                        org.bukkit.Particle.ENCHANTED_HIT,
                        player.getLocation().add(0, 1, 0),
                        5,
                        0.5,
                        0.5,
                        0.5,
                        0.1
                    );
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        if (newItem == null) return;

        ItemQualityManager qualityManager = plugin.getItemQualityManager();

        if (qualityManager.hasQuality(newItem)) {
            ItemQualityManager.ItemQuality quality =
                qualityManager.getItemQuality(newItem);

            // Mostrar información en la action bar para ítems de calidad
            if (
                quality.ordinal() >=
                ItemQualityManager.ItemQuality.UNCOMMON.ordinal()
            ) {
                String message = MessageUtils.parse((
                    quality.getColorCode() +
                    quality.getSymbol() +
                    " " +
                    quality.getDisplayName() +
                    " <gray>| Poder: <light_purple>" +
                    String.format(
                        "%.1f",
                        qualityManager.getTotalItemPower(newItem)
                    )));

                player
                    .spigot()
                    .sendMessage(
                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent(message)
                    );

                // Sonido sutil para ítems épicos+
                if (
                    quality.ordinal() >=
                    ItemQualityManager.ItemQuality.EPIC.ordinal()
                ) {
                    player.playSound(
                        player.getLocation(),
                        Sound.BLOCK_NOTE_BLOCK_CHIME,
                        0.3f,
                        2.0f
                    );
                }
            }
        }
    }

    // ==================== EVENTOS DE MUERTE ====================

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Verificar ítems de calidad que se van a dropear
        event
            .getDrops()
            .forEach(drop -> {
                ItemQualityManager qualityManager =
                    plugin.getItemQualityManager();

                if (qualityManager.hasQuality(drop)) {
                    ItemQualityManager.ItemQuality quality =
                        qualityManager.getItemQuality(drop);

                    // Los ítems de calidad alta tienen probabilidad de no dropearse
                    double keepChance = getKeepChanceOnDeath(quality);

                    if (random.nextDouble() < keepChance) {
                        // Devolver el ítem al inventario del jugador (será devuelto al respawn)
                        player.getInventory().addItem(drop);
                        event.getDrops().remove(drop);

                        MessageUtils.sendMessage(player,
                            "<green>[Calidad] Tu " +
                                quality.getColorCode() +
                                quality.getDisplayName() +
                                " <green>" +
                                drop.getType().name().toLowerCase() +
                                " fue protegido!"
                        );
                    }
                }
            });
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        ItemQualityManager qualityManager = plugin.getItemQualityManager();

        if (qualityManager.hasQuality(droppedItem)) {
            ItemQualityManager.ItemQuality quality =
                qualityManager.getItemQuality(droppedItem);

            // Advertencia para ítems valiosos
            if (
                quality.ordinal() >=
                ItemQualityManager.ItemQuality.EPIC.ordinal()
            ) {
                Player player = event.getPlayer();
                MessageUtils.sendMessage(player,
                    "<red>[Calidad] <dark_red>¡Cuidado! Has dropeado un ítem " +
                        quality.getColorCode() +
                        quality.getDisplayName() +
                        "<dark_red>!"
                );
                player.playSound(
                    player.getLocation(),
                    Sound.ENTITY_VILLAGER_NO,
                    1.0f,
                    1.0f
                );
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private boolean isCrystalCombination(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;

        // Verificar si es ítem + cristal o cristal + ítem
        return (
            (canReceiveQuality(item1) && isEnhancementCrystal(item2)) ||
            (isEnhancementCrystal(item1) && canReceiveQuality(item2))
        );
    }

    private boolean handleCrystalCombination(
        InventoryClickEvent event,
        Player player,
        ItemStack item,
        ItemStack crystal
    ) {
        event.setCancelled(true);

        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        // Corregir la lógica para identificar correctamente el ítem objetivo y el cristal
        ItemStack targetItem = isEnhancementCrystal(item) ? crystal : item;
        ItemStack crystalItem = isEnhancementCrystal(item) ? item : crystal;
        
        // Verificar que el target puede recibir calidad
        if (!canReceiveQuality(targetItem)) {
            MessageUtils.sendMessage(player, "<red>[Calidad] Este ítem no puede recibir calidad.");
            return false;
        }

        if (qualityManager.applyCrystal(player, targetItem, crystalItem)) {
            // Remover cristal del cursor/slot
            if (event.getCursor().equals(crystalItem)) {
                event.setCursor(null);
            } else {
                event.setCurrentItem(null);
            }

            MessageUtils.sendMessage(player, "<light_purple>[Calidad] ¡Cristal aplicado exitosamente!");
            player.playSound(
                player.getLocation(),
                Sound.BLOCK_ENCHANTMENT_TABLE_USE,
                1.0f,
                2.0f
            );
        }
        return false;
    }

    private boolean isQualityTool(ItemStack item) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return (
            meta.hasDisplayName() &&
            meta.getDisplayName().contains("Herramienta de Calidad")
        );
    }

    private boolean isEnhancementCrystal(ItemStack item) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return (
            meta.hasDisplayName() &&
            meta.getDisplayName().contains("Cristal de Mejora")
        );
    }

    private void handleQualityToolUse(
        PlayerInteractEvent event,
        Player player,
        ItemStack tool
    ) {
        event.setCancelled(true);

        // Abrir GUI principal de calidad
        ItemQualityGUI.openMainGUI(player);
        player.playSound(
            player.getLocation(),
            Sound.UI_BUTTON_CLICK,
            1.0f,
            1.0f
        );
    }

    private void handleCrystalUse(
        PlayerInteractEvent event,
        Player player,
        ItemStack crystal
    ) {
        event.setCancelled(true);

        MessageUtils.sendMessage(player,
            "<light_purple>[Calidad] Combina este cristal con un ítem para aplicar mejoras!"
        );
        MessageUtils.sendMessage(player,
            "<gray>Tip: Arrastra el cristal sobre un ítem en tu inventario"
        );
    }

    private boolean shouldApplyQualityToMiningDrop(Material blockType) {
        return switch (blockType) {
            case
                IRON_ORE,
                GOLD_ORE,
                DIAMOND_ORE,
                EMERALD_ORE,
                COAL_ORE,
                REDSTONE_ORE,
                LAPIS_ORE,
                NETHER_GOLD_ORE,
                NETHER_QUARTZ_ORE,
                ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }

    private boolean shouldApplyQualityToMobDrop(Material itemType) {
        return switch (itemType) {
            case
                LEATHER,
                BEEF,
                PORKCHOP,
                CHICKEN,
                MUTTON,
                RABBIT,
                BONE,
                STRING,
                GUNPOWDER,
                SPIDER_EYE,
                ENDER_PEARL,
                BLAZE_ROD,
                GHAST_TEAR,
                IRON_INGOT,
                GOLD_INGOT -> true;
            default -> itemType.name().contains("SWORD")
                || itemType.name().contains("BOW")
                || itemType.name().contains("HELMET")
                || itemType.name().contains("CHESTPLATE")
                || itemType.name().contains("LEGGINGS")
                || itemType.name().contains("BOOTS");
        };
    }

    // Método eliminado por duplicidad

    private double getMobDropQualityChance(
        org.bukkit.entity.EntityType entityType
    ) {
        return switch (entityType) {
            case ENDER_DRAGON -> 1.0;
            case WITHER -> 0.9;
            case ELDER_GUARDIAN -> 0.8;
            case WARDEN -> 0.85;
            case PIGLIN_BRUTE, HOGLIN -> 0.4;
            case BLAZE, GHAST -> 0.3;
            case ENDERMAN, CREEPER -> 0.2;
            case ZOMBIE, SKELETON, SPIDER -> 0.15;
            default -> 0.1;
        };
    }

    private double calculateLuckFactor(Player player) {
        double baseLuck = 1.0;

        // Factor de herramienta con Fortuna
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (
            tool.hasItemMeta() &&
            tool
                .getItemMeta()
                .hasEnchant(
                    Enchantment.LOOTING
                )
        ) {
            int fortuneLevel = tool
                .getItemMeta()
                .getEnchantLevel(
                    Enchantment.LOOTING
                );
            baseLuck += fortuneLevel * 0.2;
        }

        // Factor de calidad de la herramienta
        ItemQualityManager qualityManager = plugin.getItemQualityManager();
        if (qualityManager.hasQuality(tool)) {
            ItemQualityManager.ItemQuality quality =
                qualityManager.getItemQuality(tool);
            baseLuck += quality.getStatMultiplier() * 0.1;
        }

        // Factor de nivel del jugador (si hay sistema de skills)
        baseLuck += player.getLevel() * 0.01;

        return Math.min(baseLuck, 3.0); // Cap at 3.0x
    }

    private double getKeepChanceOnDeath(
        ItemQualityManager.ItemQuality quality
    ) {
        return switch (quality) {
            case POOR -> 0.0;
            case COMMON -> 0.05;
            case UNCOMMON -> 0.1;
            case RARE -> 0.2;
            case EPIC -> 0.35;
            case LEGENDARY -> 0.5;
            case MYTHIC -> 0.65;
            case DIVINE -> 0.8;
            case COSMIC -> 0.95;
            default -> 0.0;
        };
    }

    // ==================== EVENTOS ESPECIALES ====================

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        ItemQualityManager qualityManager = plugin.getItemQualityManager();

        if (qualityManager.hasQuality(item)) {
            ItemQualityManager.ItemQuality quality =
                qualityManager.getItemQuality(item);

            // Notificar sobre ítems de calidad alta
            if (
                quality.ordinal() >=
                ItemQualityManager.ItemQuality.EPIC.ordinal()
            ) {
                MessageUtils.sendMessage(player,
                    "<gold>[Calidad] <green>¡Recogiste " +
                        quality.getColorCode() +
                        quality.getDisplayName() +
                        " <green>" +
                        item.getType().name().toLowerCase() +
                        "!"
                );
                player.playSound(
                    player.getLocation(),
                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                    1.0f,
                    1.5f
                );
            }
        }
    }

    /**
     * Evento personalizado para aplicar calidad automáticamente a ítems craftados
     */
    @EventHandler
    public void onCraftItem(org.bukkit.event.inventory.CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();

        // Verificar si el resultado puede tener calidad
        if (canHaveQuality(result.getType())) {
            ItemQualityManager qualityManager = plugin.getItemQualityManager();

            // Calcular probabilidad basada en los materiales usados
            double qualityChance = calculateCraftQualityChance(event);

            if (random.nextDouble() < qualityChance) {
                double luckFactor = calculateLuckFactor(player);
                ItemQualityManager.ItemQuality quality =
                    ItemQualityManager.getWeightedRandomQuality(
                        luckFactor * 0.5
                    );

                // Aplicar calidad al resultado
                ItemStack qualityResult = qualityManager.applyQuality(
                    result.clone(),
                    quality
                );
                event.setCurrentItem(qualityResult);

                // Notificar si es calidad notable
                if (
                    quality.ordinal() >=
                    ItemQualityManager.ItemQuality.UNCOMMON.ordinal()
                ) {
                    MessageUtils.sendMessage(player,
                        "<gold>[Calidad] <green>¡Crafteaste " +
                            quality.getColorCode() +
                            quality.getDisplayName() +
                            " <green>" +
                            result.getType().name().toLowerCase() +
                            "!"
                    );
                    player.playSound(
                        player.getLocation(),
                        Sound.BLOCK_ENCHANTMENT_TABLE_USE,
                        0.7f,
                        1.2f
                    );
                }
            }
        }
    }

    private boolean canHaveQuality(Material material) {
        return (
            material.name().contains("SWORD") ||
            material.name().contains("AXE") ||
            material.name().contains("PICKAXE") ||
            material.name().contains("SHOVEL") ||
            material.name().contains("HOE") ||
            material.name().contains("BOW") ||
            material.name().contains("CROSSBOW") ||
            material.name().contains("HELMET") ||
            material.name().contains("CHESTPLATE") ||
            material.name().contains("LEGGINGS") ||
            material.name().contains("BOOTS") ||
            material.name().contains("SHIELD")
        );
    }

    private double calculateCraftQualityChance(
        org.bukkit.event.inventory.CraftItemEvent event
    ) {
        double baseChance = 0.1; // 10% base chance

        // Verificar si se usan materiales de calidad en el craft
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null) {
                ItemQualityManager qualityManager =
                    plugin.getItemQualityManager();
                ItemQualityManager.ItemQuality quality =
                    qualityManager.getItemQuality(ingredient);
                if (quality != null) {
                    baseChance += quality.getStatMultiplier() * 0.05;
                }
            }
        }

        return Math.min(baseChance, 0.5); // Max 50% chance
    }

    // ==================== MÉTODOS AUXILIARES ====================

    // Método eliminado por duplicidad

    // Método eliminado por duplicidad

    // Método eliminado por duplicidad - calculateLuckFactor ya está definido en otra parte del archivo

    // Método eliminado por duplicidad

    // Método eliminado por duplicidad - ya existe otro getMobDropQualityChance con org.bukkit.entity.EntityType

    // Reimplementación del método getQualityDropChance
    private double getQualityDropChance(Material material) {
        return switch (material) {
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> 0.75;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 0.85;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> 0.60;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> 0.45;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> 0.35;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> 0.25;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> 0.30;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> 0.40;
            case NETHER_QUARTZ_ORE -> 0.35;
            case ANCIENT_DEBRIS -> 0.90;
            default -> 0.10;
        };
    }

    private boolean isQualityCrystal(ItemStack item) {
        if (
            item == null || item.getType() != Material.PRISMARINE_CRYSTALS
        ) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        String displayName = meta.getDisplayName();
        return displayName.contains("Cristal de ");
    }

    private boolean canReceiveQuality(ItemStack item) {
        if (item == null) return false;

        return (
            item.getType().name().contains("SWORD") ||
            item.getType().name().contains("AXE") ||
            item.getType().name().contains("PICKAXE") ||
            item.getType().name().contains("SHOVEL") ||
            item.getType().name().contains("HOE") ||
            item.getType().name().contains("HELMET") ||
            item.getType().name().contains("CHESTPLATE") ||
            item.getType().name().contains("LEGGINGS") ||
            item.getType().name().contains("BOOTS")
        );
    }

    // Método eliminado por duplicidad

    private ItemQualityManager.ItemQuality extractQualityFromCrystal(
        ItemStack crystal
    ) {
        if (!isQualityCrystal(crystal)) return null;

        String displayName = crystal.getItemMeta().getDisplayName();

        for (ItemQualityManager.ItemQuality quality : ItemQualityManager.ItemQuality.values()) {
            if (displayName.contains(quality.getDisplayName())) {
                return quality;
            }
        }

        return null;
    }

    /**
     * Limpia recursos cuando se deshabilita el plugin
     */
    public void cleanup() {
        // Limpiar cualquier tarea programada o cache si es necesario
    }
}
