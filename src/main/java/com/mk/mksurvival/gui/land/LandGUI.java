package com.mk.mksurvival.gui.land;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.land.LandManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LandGUI implements InventoryHolder {
    private final Player player;
    private final Inventory inventory;

    public LandGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "<gold>✦ Tierras ✦");
        initializeItems();
    }

    private void initializeItems() {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        createPlayerInfoPanel(landManager);
        createLandsPanel(landManager);
        createToolsPanel();
        createNavigationButtons();
    }

    private void createPlayerInfoPanel(LandManager landManager) {
        // Contar solo los terrenos del jugador actual
        int claimCount = 0;
        for (LandManager.LandClaim claim : landManager.getLandClaims().values()) {
            if (claim.getOwnerId().equals(player.getUniqueId())) {
                claimCount++;
            }
        }
        int availableBlocks = landManager.getPlayerClaimBlocks(player);
        ItemStack playerInfo = createSkullItem(player);
        ItemMeta meta = playerInfo.getItemMeta();
        meta.displayName(MessageUtils.parse("<gold>⛏ Terrenos del Jugador"));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Terrenos: <yellow>" + claimCount);
        lore.add("<gray>Bloques Disponibles: <green>" + availableBlocks);
        lore.add("<gray>Límite: <aqua>" + MKSurvival.getInstance().getConfig().getInt("land.max_claims_per_player", 3));
        lore.add("");
        lore.add("<gold>▶ Click para ver estadísticas");
        meta.lore(MessageUtils.parseList(lore));
        playerInfo.setItemMeta(meta);
        inventory.setItem(4, playerInfo);
    }

    private void createLandsPanel(LandManager landManager) {
        int[] landSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 24};
        int landIndex = 0;
        for (LandManager.LandClaim claim : landManager.getLandClaims().values()) {
            if (landIndex >= landSlots.length) break;
            if (claim.getOwnerId().equals(player.getUniqueId())) {
                ItemStack landItem = createLandItem(claim);
                inventory.setItem(landSlots[landIndex], landItem);
                landIndex++;
            }
        }
        while (landIndex < landSlots.length) {
            ItemStack emptySlot = createEmptyLandSlot();
            inventory.setItem(landSlots[landIndex], emptySlot);
            landIndex++;
        }
    }

    private ItemStack createLandItem(LandManager.LandClaim claim) {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<green>⚡ Terreno #" + claim.getId().split("_")[1]));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Tamaño: <yellow>" + calculateSize(claim) + " bloques");
        lore.add("<gray>Ubicación: <aqua>" + formatLocation(claim.getCorner1()));
        lore.add("<gray>Protegido: <green>✓");
        lore.add("");
        lore.add("<gold>▶ Click para gestionar");
        meta.lore(MessageUtils.parseList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEmptyLandSlot() {
        ItemStack item = new ItemStack(Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<gray>Espacio de Terreno Vacío"));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Usa /land claim para");
        lore.add("<gray>reclamar más terrenos");
        meta.lore(MessageUtils.parseList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private void createToolsPanel() {
        // Verificar si el jugador tiene una selección activa
        Location[] selection = MKSurvival.getInstance().getLandSelectionManager().getSelection(player);
        boolean hasSelection = selection != null && selection[0] != null && selection[1] != null;
        
        ItemStack selectionTool;
        if (hasSelection) {
            selectionTool = createButtonItem(Material.GOLDEN_AXE, "<gold>⛒ Herramienta de Selección",
                    "<green>Esquina 1: <white>" + formatLocation(selection[0]),
                    "<green>Esquina 2: <white>" + formatLocation(selection[1]),
                    "<green>Tamaño: <white>" + calculateBlocks(selection[0], selection[1]) + " bloques",
                    "",
                    "<gold>▶ Click para usar /land claim");
        } else {
            selectionTool = createButtonItem(Material.WOODEN_AXE, "<gold>⛒ Herramienta de Selección",
                    "<gray>Selecciona áreas para reclamar",
                    "<red>No hay selección activa",
                    "",
                    "<gold>▶ Click para iniciar selección");
        }
        inventory.setItem(37, selectionTool);

        ItemStack mapButton = createButtonItem(Material.FILLED_MAP,
                "<aqua>🗺 Mapa de Terrenos",
                "<gray>Ver un mapa visual de",
                "<gray>tus terrenos reclamados");
        inventory.setItem(39, mapButton);

        ItemStack protectionInfo = createButtonItem(Material.SHIELD, "<gold>🛡 Protección",
                "<gray>Ver información de protección");
        inventory.setItem(41, protectionInfo);

        ItemStack helpButton = createButtonItem(Material.BOOK,
                "<yellow>? Ayuda de Terrenos",
                "<gray>Click para ver comandos",
                "<gray>y información útil");
        inventory.setItem(46, helpButton);
    }

    private void createNavigationButtons() {
        ItemStack claimButton = createButtonItem(Material.EMERALD, "<gold>⛏ Reclamar",
                "<gray>Reclamar nuevo terreno");
        inventory.setItem(1, claimButton);

        ItemStack manageButton = createButtonItem(Material.REDSTONE, "<gold>⚙ Administrar",
                "<gray>Administrar terrenos");
        inventory.setItem(7, manageButton);

        ItemStack closeButton = createButtonItem(Material.BARRIER,
                "<red>✖ Cerrar",
                "<gray>Cerrar menú de terrenos");
        inventory.setItem(53, closeButton);
    }

    private ItemStack createButtonItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse(name));
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.lore(MessageUtils.parseList(loreList));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSkullItem(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        try {
            if (skull.getItemMeta() instanceof SkullMeta) {
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwningPlayer(player);
                skull.setItemMeta(meta);
            }
        } catch (NoSuchMethodError e) {
            ItemMeta meta = skull.getItemMeta();
            meta.displayName(MessageUtils.parse(player.getName()));
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private int calculateSize(LandManager.LandClaim claim) {
        int x1 = claim.getCorner1().getBlockX();
        int z1 = claim.getCorner1().getBlockZ();
        int x2 = claim.getCorner2().getBlockX();
        int z2 = claim.getCorner2().getBlockZ();
        return Math.abs(x2 - x1) * Math.abs(z2 - z1);
    }

    private int calculateBlocks(Location loc1, Location loc2) {
        int x1 = loc1.getBlockX();
        int z1 = loc1.getBlockZ();
        int x2 = loc2.getBlockX();
        int z2 = loc2.getBlockZ();
        return Math.abs(x2 - x1) * Math.abs(z2 - z1);
    }

    private String formatLocation(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void openMainGUI(Player player) {
        LandGUI gui = new LandGUI(player);
        player.openInventory(gui.getInventory());
    }

    public static void openClaimsGUI(Player player) {
        LandGUI gui = new LandGUI(player);
        player.openInventory(gui.getInventory());
    }

    private void showLandMap(Player player) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        Inventory mapInventory = Bukkit.createInventory(this, 54, "<gold>✦ Mapa de Terrenos ✦");

        // Crear un mapa visual de los terrenos
        List<LandManager.LandClaim> playerClaims = new ArrayList<>();
        for (LandManager.LandClaim claim : landManager.getLandClaims().values()) {
            if (claim.getOwnerId().equals(player.getUniqueId())) {
                playerClaims.add(claim);
            }
        }

        if (playerClaims.isEmpty()) {
            MessageUtils.sendMessage(player,"<red>[Tierras] No tienes terrenos reclamados para mostrar en el mapa.");
            return;
        }

        // Crear un mapa visual con bloques representando los terrenos
        for (int i = 0; i < playerClaims.size() && i < 9; i++) {
            LandManager.LandClaim claim = playerClaims.get(i);
            // Crear un item para representar el terreno en el mapa
            ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
            ItemMeta meta = mapItem.getItemMeta();
            meta.displayName(MessageUtils.parse("<green>⚡ Terreno #" + claim.getId().split("_")[1]));
            List<String> lore = new ArrayList<>();
            lore.add("<gray>Tamaño: <yellow>" + calculateSize(claim) + " bloques");
            lore.add("<gray>Ubicación: <aqua>" + formatLocation(claim.getCorner1()));
            lore.add("<gray>Mundo: <light_purple>" + claim.getCorner1().getWorld().getName());
            lore.add("");
            lore.add("<gold>▶ Click para teletransportarte");
            meta.lore(MessageUtils.parseList(lore));
            mapItem.setItemMeta(meta);
            mapInventory.setItem(10 + i, mapItem);
        }

        // Botón para volver al menú principal
        ItemStack backButton = createButtonItem(Material.ARROW, "<red>← Volver", "<gray>Volver al menú principal");
        mapInventory.setItem(49, backButton);
        player.openInventory(mapInventory);
    }

    private void teleportToLand(Player player, String landId) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        LandManager.LandClaim claim = landManager.getLandClaims().get(landId);

        if (claim == null) {
            MessageUtils.sendMessage(player,"<red>[Tierras] No se encontró el terreno seleccionado.");
            // Volver a abrir el menú de administración después de un tick para evitar errores
            Bukkit.getScheduler().runTaskLater(MKSurvival.getInstance(), () -> showManageLands(player), 1L);
            return;
        }

        // Calcular el centro del terreno para teletransportar al jugador
        Location corner1 = claim.getCorner1();
        Location corner2 = claim.getCorner2();
        double x = (corner1.getX() + corner2.getX()) / 2;
        double z = (corner1.getZ() + corner2.getZ()) / 2;

        // Encontrar el bloque más alto en esa posición para evitar teletransportar dentro de bloques
        int y = corner1.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;
        Location teleportLocation = new Location(corner1.getWorld(), x, y, z);

        player.teleport(teleportLocation);
        MessageUtils.sendMessage(player,"<green>[Tierras] Has sido teletransportado a tu terreno.");
        player.closeInventory();
    }

    private void showHelpInfo(Player player) {
        // Crear un nuevo inventario para mostrar la ayuda
        Inventory helpInventory = Bukkit.createInventory(this, 54, "<gold>✦ Ayuda de Tierras ✦");
        
        // Verificar si el jugador tiene una selección activa
        Location[] selection = MKSurvival.getInstance().getLandSelectionManager().getSelection(player);
        boolean hasSelection = selection != null && selection[0] != null && selection[1] != null;

        // Información general
        ItemStack generalInfo = new ItemStack(Material.BOOK);
        ItemMeta generalMeta = generalInfo.getItemMeta();
        generalMeta.displayName(MessageUtils.parse("<yellow><bold>Sistema de Tierras"));
        List<String> generalLore = new ArrayList<>();
        generalLore.add("<gray>El sistema de tierras te permite proteger");
        generalLore.add("<gray>tus construcciones y recursos de otros jugadores.");
        generalLore.add("<gray>Puedes reclamar terrenos, administrarlos y");
        generalLore.add("<gray>dar acceso a otros jugadores de confianza.");
        generalMeta.lore(MessageUtils.parseList(generalLore));
        generalInfo.setItemMeta(generalMeta);
        helpInventory.setItem(4, generalInfo);

        // Comandos básicos
        ItemStack commandsItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta commandsMeta = commandsItem.getItemMeta();
        commandsMeta.displayName(MessageUtils.parse("<yellow><bold>Comandos Básicos"));
        List<String> commandsLore = new ArrayList<>();
        commandsLore.add("<white>/land <gray>- Abre el menú de tierras");
        commandsLore.add("<white>/land claim <gray>- Reclama el terreno seleccionado");
        commandsLore.add("<white>/land unclaim <gray>- Libera un terreno reclamado");
        commandsLore.add("<white>/land info <gray>- Muestra información del terreno");
        commandsLore.add("<white>/land list <gray>- Lista tus terrenos reclamados");
        commandsLore.add("<white>/land trust <jugador> <gray>- Da acceso a un jugador");
        commandsLore.add("<white>/land untrust <jugador> <gray>- Revoca el acceso");
        commandsLore.add("<white>/land selection <gray>- Inicia la selección de terreno");
        commandsMeta.lore(MessageUtils.parseList(commandsLore));
        commandsItem.setItemMeta(commandsMeta);
        helpInventory.setItem(19, commandsItem);

        // Botón de reclamar
        ItemStack claimButton = new ItemStack(Material.EMERALD);
        ItemMeta claimMeta = claimButton.getItemMeta();
        claimMeta.displayName(MessageUtils.parse("<green>⚡ Reclamar Terreno"));
        List<String> claimLore = new ArrayList<>();
        claimLore.add("<gray>Reclama el área seleccionada");
        claimLore.add("<gray>como tu terreno protegido");
        claimLore.add("");
        if (hasSelection) {
            int size = calculateSelectionSize(selection[0], selection[1]);
            int availableBlocks = MKSurvival.getInstance().getLandManager().getPlayerClaimBlocks(player);
            if (size <= availableBlocks) {
                claimLore.add("<green>✓ Bloques suficientes");
                claimLore.add("<gray>Costo: <yellow>" + size + " bloques");
                claimLore.add("<gray>Disponibles: <green>" + availableBlocks);
            } else {
                claimLore.add("<red>✗ Bloques insuficientes");
                claimLore.add("<gray>Necesitas: <red>" + size + " bloques");
                claimLore.add("<gray>Disponibles: <yellow>" + availableBlocks);
            }
        } else {
            claimLore.add("<red>✗ Selecciona un área primero");
        }
        claimMeta.lore(MessageUtils.parseList(claimLore));
        claimButton.setItemMeta(claimMeta);
        helpInventory.setItem(21, claimButton);

        // Límites y costos
        ItemStack limitsItem = new ItemStack(Material.BOOK);
        ItemMeta limitsMeta = limitsItem.getItemMeta();
        limitsMeta.displayName(MessageUtils.parse("<gold>📋 Límites y Costos"));
        List<String> limitsLore = new ArrayList<>();
        limitsLore.add("<gray>Costo por bloque: <white>1 bloque = 1 bloque reclamado");
        limitsMeta.lore(MessageUtils.parseList(limitsLore));
        limitsItem.setItemMeta(limitsMeta);
        helpInventory.setItem(23, limitsItem);

        // Herramienta de selección
        ItemStack selectionTool = new ItemStack(Material.GOLDEN_SHOVEL);
        ItemMeta selectionMeta = selectionTool.getItemMeta();
        selectionMeta.displayName(MessageUtils.parse("<gold>⚒ Herramienta de Selección"));
        List<String> selectionLore = new ArrayList<>();
        selectionLore.add("<gray>Click derecho para seleccionar");
        selectionLore.add("<gray>esquinas de tu terreno");
        selectionLore.add("");
        if (hasSelection) {
            selectionLore.add("<green>✓ Selección activa");
            selectionLore.add("<gray>Esquina 1: <yellow>" + formatLocation(selection[0]));
            selectionLore.add("<gray>Esquina 2: <yellow>" + formatLocation(selection[1]));
        } else {
            selectionLore.add("<red>✗ Sin selección");
            selectionLore.add("<gray>Usa la herramienta para");
            selectionLore.add("<gray>seleccionar un área");
        }
        selectionMeta.lore(MessageUtils.parseList(selectionLore));
        selectionTool.setItemMeta(selectionMeta);
        helpInventory.setItem(25, selectionTool);

        // Crear inventario de opciones para los elementos que lo necesitan
        Inventory optionsInventory = Bukkit.createInventory(this, 36, "<gold>✦ Opciones de Terreno ✦");
        
        // Jugadores de confianza
        ItemStack trustItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta trustMeta = trustItem.getItemMeta();
        trustMeta.displayName(MessageUtils.parse("<green>👥 Jugadores de Confianza"));
        List<String> trustLore = new ArrayList<>();
        trustLore.add("<gray>Gestiona los jugadores");
        trustLore.add("<gray>de confianza en este terreno.");
        trustMeta.lore(MessageUtils.parseList(trustLore));
        trustItem.setItemMeta(trustMeta);
        optionsInventory.setItem(21, trustItem);

        // Botón de abandonar
        ItemStack abandonButton = new ItemStack(Material.REDSTONE);
        ItemMeta abandonMeta = abandonButton.getItemMeta();
        abandonMeta.displayName(MessageUtils.parse("<red>⚠ Abandonar Terreno"));
        List<String> abandonLore = new ArrayList<>();
        abandonLore.add("<gray>Abandona el terreno donde");
        abandonLore.add("<gray>te encuentras actualmente");
        abandonLore.add("");
        abandonLore.add("<red>⚠ Esta acción es irreversible");
        abandonMeta.lore(MessageUtils.parseList(abandonLore));
        abandonButton.setItemMeta(abandonMeta);
        optionsInventory.setItem(22, abandonButton);

        // Botón para volver al menú de administración
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("<red><bold>Volver a Administrar");
        backItem.setItemMeta(backMeta);
        optionsInventory.setItem(31, backItem);

        // Decoración
        ItemStack decoration = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta decorationMeta = decoration.getItemMeta();
        decorationMeta.setDisplayName(" ");
        decoration.setItemMeta(decorationMeta);
        for (int i = 0; i < 36; i++) {
            if (optionsInventory.getItem(i) == null) {
                optionsInventory.setItem(i, decoration);
            }
        }

        player.openInventory(helpInventory);
    }

    private int calculateSelectionSize(Location location, Location location1) {
        return 0;
    }

    private void showProtectionInfo(Player player) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        Inventory protectionInventory = Bukkit.createInventory(this, 54, "<gold>✦ Protección de Terrenos ✦");

        // Información general de protección
        ItemStack infoItem = createButtonItem(Material.BOOK, "<gold>📚 Información de Protección",
                "<gray>Los terrenos protegen automáticamente contra:",
                "<gray>- Construcción por otros jugadores",
                "<gray>- Uso de bloques interactivos",
                "<gray>- Acceso a cofres y contenedores",
                "<gray>- PvP dentro del terreno",
                "<gray>- Daño a entidades");
        protectionInventory.setItem(4, infoItem);

        // Mostrar los terrenos del jugador y su estado de protección
        List<LandManager.LandClaim> playerClaims = new ArrayList<>();
        for (LandManager.LandClaim claim : landManager.getLandClaims().values()) {
            if (claim.getOwnerId().equals(player.getUniqueId())) {
                playerClaims.add(claim);
            }
        }

        if (playerClaims.isEmpty()) {
            ItemStack noClaimsItem = createButtonItem(Material.BARRIER, "<red>❌ Sin Terrenos",
                    "<gray>No tienes terrenos reclamados",
                    "<gray>Usa /land claim para reclamar un terreno");
            protectionInventory.setItem(22, noClaimsItem);
        } else {
            int slot = 19;
            for (LandManager.LandClaim claim : playerClaims) {
                if (slot > 34) break; // Limitar a 16 terrenos mostrados
                ItemStack claimItem = createButtonItem(Material.SHIELD, "<green>🛡 Terreno #" + claim.getId().split("_")[1],
                        "<gray>Ubicación: <aqua>" + formatLocation(claim.getCorner1()),
                        "<gray>Tamaño: <yellow>" + calculateSize(claim) + " bloques",
                        "<gray>Estado: <green>✓ Protegido",
                        "<gray>Jugadores confiados: <yellow>" + claim.getTrustedPlayers().size(),
                        "",
                        "<gold>▶ Click para ver detalles");
                protectionInventory.setItem(slot, claimItem);
                slot++;
                // Saltar al siguiente fila cada 8 slots
                if ((slot - 19) % 8 == 0) {
                    slot += 1;
                }
            }
        }

        // Botón para visualizar la protección
        ItemStack visualizeButton = createButtonItem(Material.ENDER_EYE, "<gold>👁 Visualizar Protección",
                "<gray>Muestra los límites de tu terreno",
                "<gray>durante 30 segundos",
                "",
                "<gold>▶ Click para activar");
        protectionInventory.setItem(48, visualizeButton);

        // Botón para volver al menú principal
        ItemStack backButton = createButtonItem(Material.ARROW, "<red>← Volver",
                "<gray>Volver al menú principal");
        protectionInventory.setItem(49, backButton);

        player.openInventory(protectionInventory);
    }

    private void showLandProtectionDetails(Player player, String landId) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        LandManager.LandClaim claim = landManager.getLandClaims().get(landId);
        if (claim == null) {
            MessageUtils.sendMessage(player,"<red>[Tierras] No se encontró el terreno seleccionado.");
            return;
        }

        // Verificar si el jugador es el dueño, un jugador confiado o un administrador
        boolean isOwner = claim.getOwnerId().equals(player.getUniqueId());
        boolean isTrusted = claim.getTrustedPlayers().contains(player.getUniqueId());
        boolean isAdmin = player.hasPermission("mksurvival.land.admin");

        if (!isOwner && !isTrusted && !isAdmin) {
            MessageUtils.sendMessage(player,"<red>[Tierras] No tienes permiso para ver los detalles de este terreno.");
            return;
        }

        Inventory detailsInventory = Bukkit.createInventory(this, 54, "<gold>✦ Detalles de Protección ✦");

        // Información del terreno
        ItemStack infoItem = createButtonItem(Material.SHIELD, "<green>🛡 Terreno #" + landId.split("_")[1],
                "<gray>Ubicación: <aqua>" + formatLocation(claim.getCorner1()),
                "<gray>Tamaño: <yellow>" + calculateSize(claim) + " bloques",
                "<gray>Mundo: <light_purple>" + claim.getCorner1().getWorld().getName());
        detailsInventory.setItem(4, infoItem);

        // Protección contra construcción
        ItemStack buildItem = createButtonItem(Material.BRICKS, "<yellow>🏗 Protección contra Construcción",
                "<gray>Estado: <green>✓ Activado",
                "<gray>Solo tú y los jugadores de confianza",
                "<gray>pueden construir en este terreno.");
        detailsInventory.setItem(19, buildItem);

        // Protección contra destrucción
        ItemStack breakItem = createButtonItem(Material.DIAMOND_PICKAXE, "<yellow>⛏ Protección contra Destrucción",
                "<gray>Estado: <green>✓ Activado",
                "<gray>Solo tú y los jugadores de confianza",
                "<gray>pueden romper bloques en este terreno.");
        detailsInventory.setItem(20, breakItem);

        // Protección contra uso de elementos
        ItemStack useItem = createButtonItem(Material.CHEST, "<yellow>🔒 Protección contra Uso",
                "<gray>Estado: <green>✓ Activado",
                "<gray>Solo tú y los jugadores de confianza",
                "<gray>pueden usar elementos en este terreno.");
        detailsInventory.setItem(21, useItem);

        // Protección contra PvP
        ItemStack pvpItem = createButtonItem(Material.IRON_SWORD, "<yellow>⚔ Protección contra PvP",
                "<gray>Estado: <green>✓ Activado",
                "<gray>El PvP está desactivado en este terreno.");
        detailsInventory.setItem(22, pvpItem);

        // Protección contra mobs hostiles
        ItemStack mobItem = createButtonItem(Material.ZOMBIE_HEAD, "<yellow>👹 Protección contra Mobs",
                "<gray>Estado: <green>✓ Activado",
                "<gray>Los mobs hostiles no pueden dañar",
                "<gray>estructuras en este terreno.");
        detailsInventory.setItem(23, mobItem);

        // Protección contra explosiones
        ItemStack explosionItem = createButtonItem(Material.TNT, "<yellow>💥 Protección contra Explosiones",
                "<gray>Estado: <green>✓ Activado",
                "<gray>Las explosiones no dañan los bloques",
                "<gray>en este terreno.");
        detailsInventory.setItem(24, explosionItem);

        // Jugadores de confianza
        List<String> trustedLore = new ArrayList<>();
        trustedLore.add("<gray>Jugadores que pueden interactuar");
        trustedLore.add("<gray>con tu terreno:");
        trustedLore.add("");
        if (claim.getTrustedPlayers().isEmpty()) {
            trustedLore.add("<red>No hay jugadores de confianza");
        } else {
            for (UUID trustedUUID : claim.getTrustedPlayers()) {
                String playerName = Bukkit.getOfflinePlayer(trustedUUID).getName();
                trustedLore.add("<white>- " + playerName);
            }
        }
        ItemStack trustedItem = createButtonItem(Material.PLAYER_HEAD, "<yellow>👥 Jugadores de Confianza", trustedLore.toArray(new String[0]));
        detailsInventory.setItem(31, trustedItem);

        // Botón para volver al menú de protección
        ItemStack backButton = createButtonItem(Material.ARROW, "<red>← Volver",
                "<gray>Volver al menú de protección");
        detailsInventory.setItem(49, backButton);

        player.openInventory(detailsInventory);
    }

    private void visualizeProtection(Player player) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        LandManager.LandClaim claim = null;

        // Buscar el terreno en el que está parado el jugador
        for (LandManager.LandClaim c : landManager.getLandClaims().values()) {
            if (isInside(player.getLocation(), c.getCorner1(), c.getCorner2())) {
                claim = c;
                break;
            }
        }

        if (claim == null) {
            MessageUtils.sendMessage(player,"<red>[Tierras] No estás dentro de ningún terreno protegido.");
            return;
        }

        // Verificar si el terreno pertenece al jugador, si es de confianza o si es administrador
        boolean isOwner = claim.getOwnerId().equals(player.getUniqueId());
        boolean isTrusted = claim.getTrustedPlayers().contains(player.getUniqueId());
        boolean isAdmin = player.hasPermission("mksurvival.land.admin");

        if (!isOwner && !isTrusted && !isAdmin) {
            MessageUtils.sendMessage(player,"<red>[Tierras] No tienes permiso para visualizar este terreno.");
            return;
        }

        // Mostrar partículas en los bordes del terreno
        Location corner1 = claim.getCorner1();
        Location corner2 = claim.getCorner2();

        // Programar tarea para mostrar partículas durante 30 segundos
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 30 || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                // Mostrar partículas en los bordes
                showBorderParticles(player, corner1, corner2);
                count++;
            }
        }.runTaskTimer(MKSurvival.getInstance(), 0L, 20L);

        MessageUtils.sendMessage(player,"<green>[Tierras] Visualizando los límites del terreno durante 30 segundos.");
    }

    private boolean isInside(Location loc, Location corner1, Location corner2) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    private void showBorderParticles(Player player, Location corner1, Location corner2) {
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        World world = corner1.getWorld();

        // Mostrar partículas en las aristas
        for (double x = minX; x <= maxX; x += 0.5) {
            world.spawnParticle(Particle.SMOKE, new Location(world, x, minY, minZ), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, x, minY, maxZ), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, x, maxY, minZ), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, x, maxY, maxZ), 1, new Particle.DustOptions(Color.GREEN, 1));
        }

        for (double z = minZ; z <= maxZ; z += 0.5) {
            world.spawnParticle(Particle.SMOKE, new Location(world, minX, minY, z), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, maxX, minY, z), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, minX, maxY, z), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, maxX, maxY, z), 1, new Particle.DustOptions(Color.GREEN, 1));
        }

        for (double y = minY; y <= maxY; y += 0.5) {
            world.spawnParticle(Particle.SMOKE, new Location(world, minX, y, minZ), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, maxX, y, minZ), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, minX, y, maxZ), 1, new Particle.DustOptions(Color.GREEN, 1));
            world.spawnParticle(Particle.SMOKE, new Location(world, maxX, y, maxZ), 1, new Particle.DustOptions(Color.GREEN, 1));
        }
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();
        String title = event.getView().getTitle();

        // Manejo de clics en el mapa de terrenos
        if (title.equals("<gold>✦ Mapa de Terrenos ✦")) {
            if (slot == 49) { // Botón de volver
                player.closeInventory();
                openMainGUI(player);
                return;
            }
            // Clics en los terrenos del mapa (slots 10-18)
            if (slot >= 10 && slot <= 18) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == Material.FILLED_MAP && clickedItem.hasItemMeta()) {
                    String landId = clickedItem.getItemMeta().getDisplayName().split("#")[1];
                    teleportToLand(player, "land_" + player.getUniqueId() + "_" + landId);
                }
                return;
            }
            return;
        }

        // Manejo de clics en la interfaz de protección
        if (title.equals("<gold>✦ Protección de Terrenos ✦")) {
            if (slot == 49) { // Botón de volver
                player.closeInventory();
                openMainGUI(player);
                return;
            }
            // Botón de visualizar protección
            if (slot == 48) {
                player.closeInventory();
                visualizeProtection(player);
                return;
            }
            // Clics en los terrenos (slots 19-34)
            if (slot >= 19 && slot <= 34) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == Material.SHIELD && clickedItem.hasItemMeta()) {
                    String landId = clickedItem.getItemMeta().getDisplayName().split("#")[1];
                    showLandProtectionDetails(player, "land_" + player.getUniqueId() + "_" + landId);
                }
                return;
            }
            return;
        }

        // Manejo de clics en la interfaz de detalles de protección
        if (title.equals("<gold>✦ Detalles de Protección ✦")) {
            if (slot == 49) { // Botón de volver
                player.closeInventory();
                showProtectionInfo(player);
                return;
            }
            // Botón de visualizar protección
            if (slot == 48) {
                player.closeInventory();
                visualizeProtection(player);
                return;
            }
            return;
        }

        // Manejo de clics en la interfaz de ayuda
        if (title.equals("<gold>✦ Ayuda de Tierras ✦")) {
            if (slot == 49) { // Botón de volver
                player.closeInventory();
                openMainGUI(player);
                return;
            }
            return;
        }

        // Manejo de clics en la interfaz de administración de terrenos
        if (title.equals("<gold>✦ Administrar Terrenos ✦")) {
            if (slot == 49) { // Botón de volver
                player.closeInventory();
                openMainGUI(player);
                return;
            }
            // Botón para reclamar nuevo terreno
            if (slot == 47) {
                player.closeInventory();
                MKSurvival.getInstance().getLandSelectionManager().startSelection(player);
                MessageUtils.sendMessage(player,"<green>[Tierras] Usa el hacha de madera para seleccionar las esquinas de tu terreno.");
                return;
            }
            // Botón para gestionar jugadores de confianza
            if (slot == 48) {
                MessageUtils.sendMessage(player,"<red>[Tierras] Esta función aún no está implementada.");
                return;
            }
            // Verificar si se hizo clic en un terreno (slots 19-43)
            if (slot >= 19 && slot <= 43) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == Material.GRASS_BLOCK && clickedItem.hasItemMeta()) {
                    String displayName = clickedItem.getItemMeta().getDisplayName();
                    if (displayName.startsWith("<green><bold>Terreno #")) {
                        String landId = "land_" + player.getUniqueId() + "_" + displayName.substring(11);

                        // Verificar que el terreno existe antes de continuar
                        LandManager landManager = MKSurvival.getInstance().getLandManager();
                        if (!landManager.getLandClaims().containsKey(landId)) {
                            MessageUtils.sendMessage(player,"<red>[Tierras] Error: No se encontró el terreno seleccionado. Por favor, actualiza el menú.");
                            player.closeInventory();
                            // Volver a abrir el menú de administración después de un tick para evitar errores
                            Bukkit.getScheduler().runTaskLater(MKSurvival.getInstance(), () -> showManageLands(player), 1L);
                            return;
                        }

                        if (event.isLeftClick()) {
                            // Teleportarse al terreno
                            player.closeInventory();
                            teleportToLand(player, landId);
                        } else if (event.isRightClick()) {
                            // Mostrar opciones del terreno
                            player.closeInventory();
                            showLandOptions(player, landId);
                        }
                    }
                }
                return;
            }
            return;
        }

        // Manejo de clics en la interfaz de opciones de terreno
        if (title.startsWith("<gold>✦ Opciones del Terreno #")) {
            // Extraer el ID del terreno del título
            String landIdNumber = title.substring(title.lastIndexOf("#") + 1, title.lastIndexOf(" ✦"));
            String landId = "land_" + player.getUniqueId() + "_" + landIdNumber;

            // Botón para volver al menú de administración
            if (slot == 31) {
                player.closeInventory();
                showManageLands(player);
                return;
            }
            // Botón para teleportarse
            if (slot == 19) {
                player.closeInventory();
                teleportToLand(player, landId);
                return;
            }
            // Botón para visualizar límites
            if (slot == 20) {
                player.closeInventory();
                visualizeProtection(player);
                return;
            }
            // Botón para gestionar jugadores de confianza
            if (slot == 21) {
                player.closeInventory();
                manageTrustedPlayers(player, landId);
                return;
            }
            // Botón para liberar el terreno
            if (slot == 22) {
                player.closeInventory();
                LandManager landManager = MKSurvival.getInstance().getLandManager();
                if (landManager.removeLandClaim(landId)) {
                    MessageUtils.sendMessage(player,"<green>[Tierras] Has liberado el terreno #" + landIdNumber + " correctamente.");
                    showManageLands(player);
                } else {
                    MessageUtils.sendMessage(player,"<red>[Tierras] No se pudo liberar el terreno. Inténtalo de nuevo.");
                }
                return;
            }
            return;
        }

        // Manejo de clics en el menú principal
        // Botón de cerrar
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        // Botón de reclamar
        if (slot == 1) {
            player.closeInventory();
            MKSurvival.getInstance().getLandSelectionManager().startSelection(player);
            return;
        }
        // Botón de administrar
        if (slot == 7) {
            player.closeInventory();
            showManageLands(player);
            return;
        }
        // Botón de ayuda
        if (slot == 46) {
            player.closeInventory();
            showHelpInfo(player);
            return;
        }
        // Herramienta de selección
        if (slot == 37) {
            player.closeInventory();
            MKSurvival.getInstance().getLandSelectionManager().startSelection(player);
            return;
        }
        // Mapa de terrenos
        if (slot == 39) {
            player.closeInventory();
            showLandMap(player);
            return;
        }
        // Protección
        if (slot == 41) {
            player.closeInventory();
            showProtectionInfo(player);
            return;
        }

        // Slots de terrenos
        int[] landSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 24};
        for (int landSlot : landSlots) {
            if (slot == landSlot) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    String landName = clickedItem.getItemMeta().getDisplayName();
                    MessageUtils.sendMessage(player,"<green>[Tierras] Has seleccionado el terreno: " + landName);
                }
                return;
            }
        }
    }

    private void showManageLands(Player player) {
    }

    private void showLandOptions(Player player, String landId) {
    }

    // ==================== MÉTODOS DE GESTIÓN DE CONFIANZA ====================

    private void manageTrustedPlayers(Player player, String landId) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        
        MessageUtils.sendMessage(player, "<gold>══════════════════════════════════");
        MessageUtils.sendMessage(player, "<gold>👥 Gestión de Jugadores de Confianza");
        MessageUtils.sendMessage(player, "<gray>Terreno: <yellow>" + landId);
        MessageUtils.sendMessage(player, "");
        
        // Mostrar jugadores de confianza actuales
        List<String> trustedPlayers = getTrustedPlayersForLand(landId);
        if (trustedPlayers.isEmpty()) {
            MessageUtils.sendMessage(player, "<gray>No hay jugadores de confianza en este terreno.");
        } else {
            MessageUtils.sendMessage(player, "<gray>Jugadores de confianza actuales:");
            for (int i = 0; i < trustedPlayers.size(); i++) {
                String trustedPlayer = trustedPlayers.get(i);
                MessageUtils.sendMessage(player, "<yellow>" + (i + 1) + ". <green>" + trustedPlayer);
            }
        }
        
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "<gray>Comandos disponibles:");
        MessageUtils.sendMessage(player, "<yellow>/land trust add <jugador> <gray>- Añadir jugador de confianza");
        MessageUtils.sendMessage(player, "<yellow>/land trust remove <jugador> <gray>- Remover jugador de confianza");
        MessageUtils.sendMessage(player, "<yellow>/land trust list <gray>- Ver lista de jugadores de confianza");
        MessageUtils.sendMessage(player, "<yellow>/land trust clear <gray>- Remover todos los jugadores de confianza");
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "<gray><bold>NOTA: <gray>Los jugadores de confianza pueden:");
        MessageUtils.sendMessage(player, "<gray>• Construir y destruir en el terreno");
        MessageUtils.sendMessage(player, "<gray>• Usar cofres y otros contenedores");
        MessageUtils.sendMessage(player, "<gray>• Interactuar con puertas y mecanismos");
        MessageUtils.sendMessage(player, "<red>• NO pueden gestionar otros jugadores de confianza");
        MessageUtils.sendMessage(player, "<gold>══════════════════════════════════");
    }
    
    private List<String> getTrustedPlayersForLand(String landId) {
        // Obtener la lista de jugadores de confianza desde el LandManager
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        
        // Por ahora, devolvemos una lista de ejemplo
        // En una implementación real, esto vendría del sistema de almacenamiento del LandManager
        List<String> trustedPlayers = new ArrayList<>();
        
        // Obtener desde la configuración
        org.bukkit.configuration.file.FileConfiguration config = MKSurvival.getInstance().getConfigManager().getLandConfig();
        if (config.contains("lands." + landId + ".trusted_players")) {
            trustedPlayers = config.getStringList("lands." + landId + ".trusted_players");
        }
        
        return trustedPlayers;
    }
    
    public void addTrustedPlayer(String landId, String playerName, Player owner) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        
        List<String> trustedPlayers = getTrustedPlayersForLand(landId);
        
        if (trustedPlayers.contains(playerName)) {
            MessageUtils.sendMessage(owner, "<red>[Tierras] " + playerName + " ya es un jugador de confianza.</red>");
            return;
        }
        
        // Verificar límite de jugadores de confianza (máximo 10)
        if (trustedPlayers.size() >= 10) {
            MessageUtils.sendMessage(owner, "<red>[Tierras] Has alcanzado el límite máximo de jugadores de confianza (10).</red>");
            return;
        }
        
        // Verificar que el jugador existe
        org.bukkit.entity.Player targetPlayer = org.bukkit.Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            // Verificar si el jugador ha jugado antes
            org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            if (!offlinePlayer.hasPlayedBefore()) {
                MessageUtils.sendMessage(owner, "<red>[Tierras] El jugador " + playerName + " no existe o nunca ha jugado en el servidor.</red>");
                return;
            }
        }
        
        // Añadir jugador de confianza
        trustedPlayers.add(playerName);
        
        // Guardar en configuración
        org.bukkit.configuration.file.FileConfiguration config = MKSurvival.getInstance().getConfigManager().getLandConfig();
        config.set("lands." + landId + ".trusted_players", trustedPlayers);
        MKSurvival.getInstance().getConfigManager().saveLandConfig();
        
        MessageUtils.sendMessage(owner, "<green>[Tierras] " + playerName + " ha sido añadido como jugador de confianza.</green>");
        
        // Notificar al jugador si está en línea
        if (targetPlayer != null) {
            MessageUtils.sendMessage(targetPlayer, "<green>[Tierras] " + owner.getName() + " te ha añadido como jugador de confianza en su terreno.</green>");
        }
    }
    
    public void removeTrustedPlayer(String landId, String playerName, Player owner) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        
        List<String> trustedPlayers = getTrustedPlayersForLand(landId);
        
        if (!trustedPlayers.contains(playerName)) {
            MessageUtils.sendMessage(owner, "<red>[Tierras] " + playerName + " no es un jugador de confianza.</red>");
            return;
        }
        
        // Remover jugador de confianza
        trustedPlayers.remove(playerName);
        
        // Guardar en configuración
        org.bukkit.configuration.file.FileConfiguration config = MKSurvival.getInstance().getConfigManager().getLandConfig();
        config.set("lands." + landId + ".trusted_players", trustedPlayers);
        MKSurvival.getInstance().getConfigManager().saveLandConfig();
        
        MessageUtils.sendMessage(owner, "<green>[Tierras] " + playerName + " ha sido removido de los jugadores de confianza.</green>");
        
        // Notificar al jugador si está en línea
        org.bukkit.entity.Player targetPlayer = org.bukkit.Bukkit.getPlayer(playerName);
        if (targetPlayer != null) {
            MessageUtils.sendMessage(targetPlayer, "<red>[Tierras] " + owner.getName() + " te ha removido como jugador de confianza de su terreno.</red>");
        }
    }
    
    public void clearTrustedPlayers(String landId, Player owner) {
        LandManager landManager = MKSurvival.getInstance().getLandManager();
        
        List<String> trustedPlayers = getTrustedPlayersForLand(landId);
        
        if (trustedPlayers.isEmpty()) {
            MessageUtils.sendMessage(owner, "<red>[Tierras] No hay jugadores de confianza para remover.</red>");
            return;
        }
        
        // Notificar a todos los jugadores de confianza que estarán en línea
        for (String playerName : trustedPlayers) {
            org.bukkit.entity.Player targetPlayer = org.bukkit.Bukkit.getPlayer(playerName);
            if (targetPlayer != null) {
                MessageUtils.sendMessage(targetPlayer, "<red>[Tierras] " + owner.getName() + " te ha removido como jugador de confianza de su terreno.</red>");
            }
        }
        
        // Limpiar lista
        org.bukkit.configuration.file.FileConfiguration config = MKSurvival.getInstance().getConfigManager().getLandConfig();
        config.set("lands." + landId + ".trusted_players", new ArrayList<String>());
        MKSurvival.getInstance().getConfigManager().saveLandConfig();
        
        MessageUtils.sendMessage(owner, "<green>[Tierras] Todos los jugadores de confianza han sido removidos.</green>");
    }
}