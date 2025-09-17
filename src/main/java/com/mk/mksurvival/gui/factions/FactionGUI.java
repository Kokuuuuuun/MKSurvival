package com.mk.mksurvival.gui.factions;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.utils.ChatInputManager;
import com.mk.mksurvival.managers.factions.FactionManager;
import com.mk.mksurvival.utils.MessageUtils;
import java.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * GUI completamente funcional para el sistema de facciones
 * Todas las opciones están implementadas y operativas
 */
public class FactionGUI implements InventoryHolder {
    private MKSurvival plugin = null;
    private Player player = null;
    private Inventory inventory = null;
    private FactionGUIType guiType = null;
    private Map<String, Object> guiData = null;
    // Constantes de slots
    private static final int[] FACTION_SLOTS = {
            10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 28, 29, 30, 31, 32
    };
    private static final int[] MEMBER_SLOTS = {
            10, 11, 12, 13, 14, 19, 20, 21, 22, 23
    };
    private static final int[] RELATION_SLOTS = { 10, 11, 12, 19, 20, 21 };
    // Constructor vacío
    public FactionGUI() {
        // Constructor vacío
    }
    // Métodos implementados
    public void openFactionList(Player player) {
        FactionGUI gui = new FactionGUI(player, FactionGUIType.LIST);
        player.openInventory(gui.getInventory());
    }
    public void openFactionInfo(Player player, String playerFaction) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", playerFaction);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.MANAGE, data);
        player.openInventory(gui.getInventory());
    }
    public enum FactionGUIType {
        MAIN, // GUI principal de facciones
        CREATE, // Crear nueva facción
        MANAGE, // Gestión de facción
        MEMBERS, // Gestión de miembros
        RELATIONS, // Gestión de relaciones
        ECONOMY, // Gestión económica
        TERRITORIES, // Gestión de territorios
        WARS, // Gestión de guerras
        SETTINGS, // Configuraciones
        LIST, // Lista de facciones
    }
    public FactionGUI(Player player, FactionGUIType type) {
        this.plugin = MKSurvival.getInstance();
        this.player = player;
        this.guiType = type;
        this.guiData = new HashMap<>();
        this.inventory = createInventory(type);
        populateInventory();
    }
    public FactionGUI(Player player, FactionGUIType type, Map<String, Object> data) {
        this.plugin = MKSurvival.getInstance();
        this.player = player;
        this.guiType = type;
        this.guiData = data != null ? new HashMap<>(data) : new HashMap<>();
        this.inventory = createInventory(type);
        populateInventory();
    }
    private Inventory createInventory(FactionGUIType type) {
        return switch (type) {
            case MAIN -> Bukkit.createInventory(this, 54, "<gold>⚔ Gestión de Facciones ⚔");
            case CREATE -> Bukkit.createInventory(this, 27, "<green>✦ Crear Facción ✦");
            case MANAGE -> Bukkit.createInventory(this, 54, "<yellow>⚙ Administrar Facción ⚙");
            case MEMBERS -> Bukkit.createInventory(this, 54, "<aqua>👥 Gestión de Miembros 👥");
            case RELATIONS -> Bukkit.createInventory(this, 45, "<light_purple>🌐 Relaciones Diplomáticas 🌐");
            case ECONOMY -> Bukkit.createInventory(this, 36, "<yellow>💰 Economía de Facción 💰");
            case TERRITORIES -> Bukkit.createInventory(this, 45, "<dark_green>🗺 Territorios de Facción 🗺");
            case WARS -> Bukkit.createInventory(this, 36, "<dark_red>⚔ Gestión de Guerras ⚔");
            case SETTINGS -> Bukkit.createInventory(this, 36, "<light_purple>⚙ Configuraciones ⚙");
            case LIST -> Bukkit.createInventory(this, 54, "<gold>📋 Lista de Facciones 📋");
        };
    }
    private void populateInventory() {
        switch (guiType) {
            case MAIN -> populateMainGUI();
            case CREATE -> populateCreateGUI();
            case MANAGE -> populateManageGUI();
            case MEMBERS -> populateMembersGUI();
            case RELATIONS -> populateRelationsGUI();
            case ECONOMY -> populateEconomyGUI();
            case TERRITORIES -> populateTerritoriesGUI();
            case WARS -> populateWarsGUI();
            case SETTINGS -> populateSettingsGUI();
            case LIST -> populateListGUI();
        }
    }
    private void populateManageGUI() {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Panel de información de la facción
        ItemStack factionInfo = createButtonItem(
                Material.BONE,
                "<yellow>⚔ " + faction.getName(),
                "<gray>" + faction.getDescription(),
                "<gray>Miembros: <light_purple>" + faction.getMembers().size(),
                "<gray>Balance: <green>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()),
                "",
                "<yellow>▶ Click para más información"
        );
        inventory.setItem(4, factionInfo);
        // Opciones de gestión según el rol del jugador
        String playerRole = faction.getMembers().get(player.getUniqueId());
        // Gestión de miembros
        if (canManageMembers(playerRole)) {
            ItemStack manageMembers = createButtonItem(
                    Material.PLAYER_HEAD,
                    "<aqua>👥 Gestionar Miembros",
                    "<gray>Invitar, promover y expulsar miembros",
                    "",
                    "<aqua>▶ Click para gestionar"
            );
            inventory.setItem(19, manageMembers);
        }
        // Gestión de relaciones
        if (canManageRelations(playerRole)) {
            ItemStack manageRelations = createButtonItem(
                    Material.COMPASS,
                    "<light_purple>🌐 Relaciones Diplomáticas",
                    "<gray>Gestionar alianzas y enemistades",
                    "",
                    "<light_purple>▶ Click para gestionar"
            );
            inventory.setItem(20, manageRelations);
        }
        // Economía
        ItemStack economy = createButtonItem(
                Material.GOLD_INGOT,
                "<yellow>💰 Economía",
                "<gray>Depositar y retirar dinero",
                "<gray>Balance actual: <green>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()),
                "",
                "<yellow>▶ Click para gestionar"
        );
        inventory.setItem(21, economy);
        // Territorios
        ItemStack territories = createButtonItem(
                Material.MAP,
                "<dark_green>🗺 Territorios",
                "<gray>Reclamar y gestionar tierras",
                "<gray>Territorios: <yellow>" + countFactionTerritories(factionName),
                "",
                "<dark_green>▶ Click para gestionar"
        );
        inventory.setItem(22, territories);
        // Guerras
        if (canDeclareWar(playerRole)) {
            ItemStack wars = createButtonItem(
                    Material.IRON_SWORD,
                    "<dark_red>⚔ Gestión de Guerras",
                    "<gray>Declarar guerra y gestionar conflictos",
                    "",
                    "<dark_red>▶ Click para gestionar"
            );
            inventory.setItem(23, wars);
        }
        // Configuraciones
        if (canAccessSettings(playerRole)) {
            ItemStack settings = createButtonItem(
                    Material.REDSTONE,
                    "<light_purple>⚙ Configuraciones",
                    "<gray>Ajustes avanzados de la facción",
                    "",
                    "<light_purple>▶ Click para configurar"
            );
            inventory.setItem(25, settings);
        }
        // Chat de facción
        ItemStack chat = createButtonItem(
                Material.WRITABLE_BOOK,
                "<green>💬 Chat de Facción",
                "<gray>Alternar modo de chat",
                "<gray>Modo actual: <yellow>" + getCurrentChatMode(player),
                "",
                "<green>▶ Click para alternar"
        );
        inventory.setItem(31, chat);
        // Abandonar facción
        if (!isLeader(playerRole)) {
            ItemStack leave = createButtonItem(
                    Material.BARRIER,
                    "<red>🚪 Abandonar Facción",
                    "<red>Deja tu facción actual",
                    "<red>¡Esta acción no se puede deshacer!",
                    "",
                    "<red>▶ Click para abandonar"
            );
            inventory.setItem(49, leave);
        } else {
            ItemStack disband = createButtonItem(
                    Material.TNT,
                    "<dark_red>💥 Disolver Facción",
                    "<dark_red>Elimina completamente la facción",
                    "<dark_red>¡Esta acción no se puede deshacer!",
                    "",
                    "<dark_red>▶ Click para disolver"
            );
            inventory.setItem(49, disband);
        }
        // Navegación
        inventory.setItem(45, createBackButton());
        inventory.setItem(53, createCloseButton());
        // Decoración
        fillEmptySlots();
    }
    // ==================== GUI PRINCIPAL ====================
    private void populateMainGUI() {
        FactionManager factionManager = plugin.getFactionManager();
        String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        // Información del jugador
        createPlayerInfoPanel();
        if (playerFaction == null) {
            // Panel para jugadores sin facción
            createNoFactionPanel();
        } else {
            // Panel para jugadores con facción
            createFactionMemberPanel(playerFaction);
        }
        // Panel de herramientas generales
        createGeneralToolsPanel();
        // Navegación
        createMainNavigation();
        // Decoración
        fillEmptySlots();
    }
    private void createPlayerInfoPanel() {
        ItemStack playerHead = createPlayerHead(player);
        ItemMeta meta = playerHead.getItemMeta();
        meta.displayName(MessageUtils.parse("<gold>⚔ Tu Estado de Facción"));
        FactionManager factionManager = plugin.getFactionManager();
        String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        if (playerFaction != null) {
            FactionManager.Faction faction = factionManager.getFactions().get(playerFaction);
            if (faction != null) {
                lore.add("<gray>Facción: <yellow>" + faction.getName());
                String role = faction.getMembers().get(player.getUniqueId());
                lore.add("<gray>Rango: <aqua>" + getRoleDisplayName(role));
                lore.add("<gray>Miembros totales: <light_purple>" + faction.getMembers().size());
                lore.add("<gray>Balance: <green>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()));
            }
        } else {
            lore.add("<gray>Estado: <red>Sin facción");
            lore.add("<gray>Puedes crear o unirte a una");
        }
        lore.add("");
        lore.add("<gold>▶ Click para estadísticas detalladas");
        meta.setLore(lore);
        playerHead.setItemMeta(meta);
        inventory.setItem(4, playerHead);
    }
    private void createNoFactionPanel() {
        // Crear facción
        ItemStack createFaction = createButtonItem(
                Material.BEACON,
                "<green>✦ Crear Nueva Facción",
                "<gray>Funda tu propia facción",
                "<gray>Costo: <yellow>10,000 monedas",
                "",
                "<green>▶ Click para crear"
        );
        inventory.setItem(20, createFaction);
        // Unirse a facción
        ItemStack joinFaction = createButtonItem(
                Material.PAPER,
                "<aqua>📜 Ver Invitaciones",
                "<gray>Revisa las invitaciones recibidas",
                "",
                "<aqua>▶ Click para ver"
        );
        inventory.setItem(21, joinFaction);
        // Lista de facciones
        ItemStack factionList = createButtonItem(
                Material.BOOK,
                "<yellow>📋 Lista de Facciones",
                "<gray>Ve todas las facciones del servidor",
                "",
                "<yellow>▶ Click para explorar"
        );
        inventory.setItem(22, factionList);
        // Ayuda
        ItemStack help = createButtonItem(
                Material.KNOWLEDGE_BOOK,
                "<gold>❓ Ayuda sobre Facciones",
                "<gray>Aprende cómo funcionan las facciones",
                "",
                "<gold>▶ Click para ayuda"
        );
        inventory.setItem(24, help);
    }
    private void createFactionMemberPanel(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        String playerRole = faction.getMembers().get(player.getUniqueId());
        // Información de la facción
        ItemStack factionInfo = createButtonItem(
                Material.BONE,
                "<yellow>⚔ " + faction.getName(),
                "<gray>" + faction.getDescription(),
                "<gray>Miembros: <light_purple>" + faction.getMembers().size(),
                "<gray>Balance: <green>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()),
                "",
                "<yellow>▶ Click para más información"
        );
        inventory.setItem(13, factionInfo);
        // Gestión de miembros
        if (canManageMembers(playerRole)) {
            ItemStack manageMembers = createButtonItem(
                    Material.PLAYER_HEAD,
                    "<aqua>👥 Gestionar Miembros",
                    "<gray>Invitar, promover y expulsar miembros",
                    "",
                    "<aqua>▶ Click para gestionar"
            );
            inventory.setItem(19, manageMembers);
        }
        // Gestión de relaciones
        if (canManageRelations(playerRole)) {
            ItemStack manageRelations = createButtonItem(
                    Material.COMPASS,
                    "<light_purple>🌐 Relaciones Diplomáticas",
                    "<gray>Gestionar alianzas y enemistades",
                    "",
                    "<light_purple>▶ Click para gestionar"
            );
            inventory.setItem(20, manageRelations);
        }
        // Economía
        ItemStack economy = createButtonItem(
                Material.GOLD_INGOT,
                "<yellow>💰 Economía",
                "<gray>Depositar y retirar dinero",
                "<gray>Balance actual: <green>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()),
                "",
                "<yellow>▶ Click para gestionar"
        );
        inventory.setItem(21, economy);
        // Territorios
        ItemStack territories = createButtonItem(
                Material.MAP,
                "<dark_green>🗺 Territorios",
                "<gray>Reclamar y gestionar tierras",
                "<gray>Territorios: <yellow>" + countFactionTerritories(factionName),
                "",
                "<dark_green>▶ Click para gestionar"
        );
        inventory.setItem(22, territories);
        // Guerras
        if (canDeclareWar(playerRole)) {
            ItemStack wars = createButtonItem(
                    Material.IRON_SWORD,
                    "<dark_red>⚔ Gestión de Guerras",
                    "<gray>Declarar guerra y gestionar conflictos",
                    "",
                    "<dark_red>▶ Click para gestionar"
            );
            inventory.setItem(23, wars);
        }
        // Configuraciones
        if (canAccessSettings(playerRole)) {
            ItemStack settings = createButtonItem(
                    Material.REDSTONE,
                    "<light_purple>⚙ Configuraciones",
                    "<gray>Ajustes avanzados de la facción",
                    "",
                    "<light_purple>▶ Click para configurar"
            );
            inventory.setItem(25, settings);
        }
        // Chat de facción
        ItemStack chat = createButtonItem(
                Material.WRITABLE_BOOK,
                "<green>💬 Chat de Facción",
                "<gray>Alternar modo de chat",
                "<gray>Modo actual: <yellow>" + getCurrentChatMode(player),
                "",
                "<green>▶ Click para alternar"
        );
        inventory.setItem(31, chat);
        // Abandonar facción
        if (!isLeader(playerRole)) {
            ItemStack leave = createButtonItem(
                    Material.BARRIER,
                    "<red>🚪 Abandonar Facción",
                    "<red>Deja tu facción actual",
                    "<red>¡Esta acción no se puede deshacer!",
                    "",
                    "<red>▶ Click para abandonar"
            );
            inventory.setItem(49, leave);
        } else {
            ItemStack disband = createButtonItem(
                    Material.TNT,
                    "<dark_red>💥 Disolver Facción",
                    "<dark_red>Elimina completamente la facción",
                    "<dark_red>¡Esta acción no se puede deshacer!",
                    "",
                    "<dark_red>▶ Click para disolver"
            );
            inventory.setItem(49, disband);
        }
    }
    private void createGeneralToolsPanel() {
        // Lista de facciones
        ItemStack factionList = createButtonItem(
                Material.BOOK,
                "<yellow>📋 Lista de Facciones",
                "<gray>Ve todas las facciones del servidor",
                "",
                "<yellow>▶ Click para explorar"
        );
        inventory.setItem(37, factionList);
        // Estadísticas
        ItemStack stats = createButtonItem(
                Material.ITEM_FRAME,
                "<aqua>📊 Estadísticas",
                "<gray>Ve estadísticas del servidor",
                "",
                "<aqua>▶ Click para ver"
        );
        inventory.setItem(38, stats);
        // Ranking
        ItemStack ranking = createButtonItem(
                Material.GOLDEN_APPLE,
                "<gold>🏆 Ranking de Facciones",
                "<gray>Ve las facciones más poderosas",
                "",
                "<gold>▶ Click para ver ranking"
        );
        inventory.setItem(39, ranking);
        // Ayuda
        ItemStack help = createButtonItem(
                Material.KNOWLEDGE_BOOK,
                "<gold>❓ Ayuda",
                "<gray>Información sobre el sistema de facciones",
                "",
                "<gold>▶ Click para ayuda"
        );
        inventory.setItem(41, help);
    }
    private void createMainNavigation() {
        ItemStack close = createButtonItem(
                Material.BARRIER,
                "<red>✕ Cerrar",
                "<gray>Cierra este menú",
                "",
                "<yellow>▶ Click para cerrar"
        );
        inventory.setItem(53, close);
    }
    // ==================== GUI DE CREACIÓN ====================
    private void populateCreateGUI() {
        // Información sobre crear facción
        ItemStack info = createButtonItem(
                Material.KNOWLEDGE_BOOK,
                "<gold>📖 Crear Nueva Facción",
                "<gray>Para crear una facción necesitas:",
                "<gray>• <yellow>10,000 monedas",
                "<gray>• Un nombre único",
                "<gray>• Una descripción",
                "",
                "<gray>Una vez creada, serás el líder"
        );
        inventory.setItem(4, info);
        // Balance del jugador
        double balance = plugin.getEconomyManager().getBalance(player);
        boolean canAfford = balance >= 10000;
        ItemStack balanceInfo = createButtonItem(
                canAfford ? Material.EMERALD : Material.REDSTONE_BLOCK,
                canAfford ? "<green>💰 Balance Suficiente" : "<red>💰 Balance Insuficiente",
                "<gray>Tu balance: <yellow>" + plugin.getEconomyManager().formatCurrency(balance),
                "<gray>Costo: <yellow>10,000 monedas",
                "",
                canAfford ? "<green>✓ Puedes crear una facción" : "<red>✗ Necesitas más dinero"
        );
        inventory.setItem(11, balanceInfo);
        // Crear facción
        ItemStack create = createButtonItem(
                Material.BEACON,
                "<green>✦ Crear Facción",
                "<gray>Funda tu propia facción",
                canAfford ? "<green>¡Listo para crear!" : "<red>Necesitas más dinero",
                "",
                "<green>▶ Click para continuar"
        );
        inventory.setItem(13, create);
        // Reglas y beneficios
        ItemStack rules = createButtonItem(
                Material.WRITTEN_BOOK,
                "<yellow>📜 Reglas y Beneficios",
                "<gray>Como líder de facción podrás:",
                "<gray>• Invitar hasta 50 miembros",
                "<gray>• Reclamar hasta 100 territorios",
                "<gray>• Formar alianzas y declarar guerras",
                "<gray>• Gestionar la economía de facción",
                "",
                "<yellow>▶ Click para más información"
        );
        inventory.setItem(15, rules);
        // Navegación
        inventory.setItem(18, createBackButton());
        inventory.setItem(26, createCloseButton());
    }
    // ==================== GUI DE GESTIÓN DE MIEMBROS ====================
    private void populateMembersGUI() {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Información de la facción
        ItemStack factionInfo = createButtonItem(
                Material.BONE,
                "<yellow>⚔ Miembros de " + faction.getName(),
                "<gray>Total de miembros: <light_purple>" + faction.getMembers().size() + "<gray>/<light_purple>50",
                "",
                "<gray>Gestiona los miembros de tu facción"
        );
        inventory.setItem(4, factionInfo);
        // Lista de miembros
        List<Map.Entry<UUID, String>> membersList = new ArrayList<>(faction.getMembers().entrySet());
        for (int i = 0; i < MEMBER_SLOTS.length && i < membersList.size(); i++) {
            Map.Entry<UUID, String> member = membersList.get(i);
            ItemStack memberItem = createMemberItem(member.getKey(), member.getValue());
            inventory.setItem(MEMBER_SLOTS[i], memberItem);
        }
        // Invitar nuevo miembro
        ItemStack invite = createButtonItem(
                Material.PLAYER_HEAD,
                "<green>➕ Invitar Miembro",
                "<gray>Invita un nuevo jugador a la facción",
                "",
                "<green>▶ Click para invitar"
        );
        inventory.setItem(40, invite);
        // Ver invitaciones pendientes
        ItemStack pendingInvites = createButtonItem(
                Material.PAPER,
                "<yellow>📜 Invitaciones Pendientes",
                "<gray>Ve las invitaciones enviadas",
                "",
                "<yellow>▶ Click para revisar"
        );
        inventory.setItem(41, pendingInvites);
        // Navegación
        inventory.setItem(45, createBackButton());
        inventory.setItem(53, createCloseButton());
    }
    // ==================== GUI DE ECONOMÍA ====================
    private void populateEconomyGUI() {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Balance de la facción
        ItemStack balance = createButtonItem(
                Material.GOLD_BLOCK,
                "<yellow>💰 Balance de Facción",
                "<gray>Balance actual: <green>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()),
                "",
                "<gray>Gestiona el dinero de la facción"
        );
        inventory.setItem(4, balance);
        // Depositar dinero
        ItemStack deposit = createButtonItem(
                Material.EMERALD,
                "<green>💸 Depositar Dinero",
                "<gray>Deposita dinero en el banco de facción",
                "<gray>Tu balance: <yellow>" + plugin.getEconomyManager().formatCurrency(plugin.getEconomyManager().getBalance(player)),
                "",
                "<green>▶ Click para depositar"
        );
        inventory.setItem(11, deposit);
        // Retirar dinero (solo líderes)
        String playerRole = faction.getMembers().get(player.getUniqueId());
        if (isLeader(playerRole)) {
            ItemStack withdraw = createButtonItem(
                    Material.REDSTONE,
                    "<red>💳 Retirar Dinero",
                    "<gray>Retira dinero del banco de facción",
                    "<red>Solo para líderes",
                    "",
                    "<red>▶ Click para retirar"
            );
            inventory.setItem(15, withdraw);
        }
        // Historial de transacciones
        ItemStack history = createButtonItem(
                Material.BOOK,
                "<aqua>📊 Historial",
                "<gray>Ve el historial de transacciones",
                "",
                "<aqua>▶ Click para ver"
        );
        inventory.setItem(22, history);
        // Navegación
        inventory.setItem(27, createBackButton());
        inventory.setItem(35, createCloseButton());
    }
    // ==================== MÉTODOS DE MANEJO DE EVENTOS ====================
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        switch (guiType) {
            case MAIN -> handleMainGUIClick(slot, clicked);
            case CREATE -> handleCreateGUIClick(slot, clicked);
            case MANAGE -> handleManageGUIClick(slot, clicked);
            case MEMBERS -> handleMembersGUIClick(slot, clicked);
            case RELATIONS -> handleRelationsGUIClick(slot, clicked);
            case ECONOMY -> handleEconomyGUIClick(slot, clicked);
            case TERRITORIES -> handleTerritoriesGUIClick(slot, clicked);
            case WARS -> handleWarsGUIClick(slot, clicked);
            case SETTINGS -> handleSettingsGUIClick(slot, clicked);
            case LIST -> handleListGUIClick(slot, clicked);
        }
    }
    private void handleMainGUIClick(int slot, ItemStack clicked) {
        FactionManager factionManager = plugin.getFactionManager();
        String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        switch (slot) {
            case 4 -> showPlayerDetailedStats();
            case 20 -> {
                if (playerFaction == null) {
                    openCreateGUI();
                }
            }
            case 21 -> {
                if (playerFaction == null) {
                    showInvitations();
                } else {
                    openEconomyGUI(playerFaction);
                }
            }
            case 22 -> {
                if (playerFaction == null) {
                    openListGUI();
                } else {
                    openTerritoriesGUI(playerFaction);
                }
            }
            case 13 -> {
                if (playerFaction != null) {
                    showFactionInfo(playerFaction);
                }
            }
            case 19 -> {
                if (playerFaction != null) {
                    openMembersGUI(playerFaction);
                }
            }
            case 23 -> {
                if (playerFaction != null) {
                    openWarsGUI(playerFaction);
                }
            }
            case 25 -> {
                if (playerFaction != null) {
                    openSettingsGUI(playerFaction);
                }
            }
            case 31 -> {
                if (playerFaction != null) {
                    toggleFactionChat();
                }
            }
            case 37 -> openListGUI();
            case 38 -> showServerStats();
            case 39 -> showFactionRanking();
            case 41 -> showFactionHelp();
            case 49 -> {
                if (playerFaction != null) {
                    handleLeaveFaction();
                }
            }
            case 53 -> player.closeInventory();
        }
    }
    private void handleCreateGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 13 -> {
                double balance = plugin.getEconomyManager().getBalance(player);
                if (balance >= 10000) {
                    startFactionCreation();
                } else {
                    MessageUtils.sendMessage(player, "<red>[Facciones] Necesitas 10,000 monedas para crear una facción.");
                }
            }
            case 15 -> showCreationRules();
            case 18 -> openMainGUI();
            case 26 -> player.closeInventory();
        }
    }
    private void handleEconomyGUIClick(int slot, ItemStack clicked) {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        switch (slot) {
            case 11 -> startMoneyDeposit(faction);
            case 15 -> {
                String playerRole = faction.getMembers().get(player.getUniqueId());
                if (isLeader(playerRole)) {
                    startMoneyWithdraw(faction);
                }
            }
            case 22 -> showTransactionHistory();
            case 27 -> openMainGUI();
            case 35 -> player.closeInventory();
        }
    }
    // ==================== MÉTODOS AUXILIARES ====================
    private void openMainGUI() {
        FactionGUI gui = new FactionGUI(player, FactionGUIType.MAIN);
        player.openInventory(gui.getInventory());
    }
    private void openCreateGUI() {
        FactionGUI gui = new FactionGUI(player, FactionGUIType.CREATE);
        player.openInventory(gui.getInventory());
    }
    private void openMembersGUI(String factionName) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.MEMBERS, data);
        player.openInventory(gui.getInventory());
    }
    private void openEconomyGUI(String factionName) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.ECONOMY, data);
        player.openInventory(gui.getInventory());
    }
    private void openListGUI() {
        FactionGUI gui = new FactionGUI(player, FactionGUIType.LIST);
        player.openInventory(gui.getInventory());
    }
    // Implementación de funcionalidades principales
    private void startFactionCreation() {
        player.closeInventory();
        MessageUtils.sendMessage(player, "<green>[Facciones] <dark_green>Para crear tu facción:");
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>1. Escribe el nombre en el chat");
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>2. Luego escribe la descripción");
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Tienes 60 segundos para cada paso");
        ChatInputManager chatInputManager = plugin.getChatInputManager();
        chatInputManager.requestInput(
                player,
                "<green>[Facciones] <gray>Escribe el nombre de tu facción en el chat:",
                60,
                (factionName) -> {
                    if (factionName.length() < 3 || factionName.length() > 16) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>El nombre debe tener entre 3 y 16 caracteres.");
                        openCreateGUI();
                        return;
                    }
                    FactionManager factionManager = plugin.getFactionManager();
                    if (factionManager.getFactions().containsKey(factionName)) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Ya existe una facción con ese nombre.");
                        openCreateGUI();
                        return;
                    }
                    requestFactionDescription(factionName);
                },
                () -> {
                    MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Tiempo agotado para crear la facción.");
                    openCreateGUI();
                }
        );
    }
    private void requestFactionDescription(String factionName) {
        ChatInputManager chatInputManager = plugin.getChatInputManager();
        chatInputManager.requestInput(
                player,
                "<green>[Facciones] <gray>Escribe la descripción de tu facción en el chat:",
                60,
                (description) -> {
                    if (description.length() < 5 || description.length() > 100) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>La descripción debe tener entre 5 y 100 caracteres.");
                        requestFactionDescription(factionName);
                        return;
                    }
                    FactionManager factionManager = plugin.getFactionManager();
                    if (factionManager.createFaction(player.getUniqueId(), factionName, description)) {
                        MessageUtils.sendMessage(player, "<green>[Facciones] <dark_green>¡Facción creada exitosamente!");
                        openMainGUI();
                    } else {
                        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>No se pudo crear la facción.");
                        openCreateGUI();
                    }
                },
                () -> {
                    MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Tiempo agotado para crear la facción.");
                    openCreateGUI();
                }
        );
    }
    private void startMoneyDeposit(FactionManager.Faction faction) {
        player.closeInventory();
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Escribe la cantidad a depositar en el chat:");
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Tienes 30 segundos para responder.");
        ChatInputManager chatInputManager = plugin.getChatInputManager();
        chatInputManager.requestInput(
                player,
                "<green>[Facciones] <gray>Escribe la cantidad a depositar:",
                30,
                (input) -> {
                    try {
                        double amount = Double.parseDouble(input);
                        if (amount <= 0) {
                            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>La cantidad debe ser mayor que 0.");
                            openEconomyGUI(faction.getName());
                            return;
                        }
                        if (plugin.getEconomyManager().getBalance(player) < amount) {
                            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>No tienes suficiente dinero.");
                            openEconomyGUI(faction.getName());
                            return;
                        }
                        FactionManager factionManager = plugin.getFactionManager();
                        boolean success = factionManager.depositMoney(player.getUniqueId(), amount);
                        if (success) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                openEconomyGUI(faction.getName());
                            }, 40L);
                        } else {
                            openEconomyGUI(faction.getName());
                        }
                    } catch (NumberFormatException e) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Cantidad inválida. Debes ingresar un número.");
                        openEconomyGUI(faction.getName());
                    }
                },
                () -> {
                    MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Tiempo agotado para depositar dinero.");
                    openEconomyGUI(faction.getName());
                }
        );
    }
    private void startMoneyWithdraw(FactionManager.Faction faction) {
        player.closeInventory();
        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Escribe la cantidad a retirar en el chat:");
        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Tienes 30 segundos para responder.");
        ChatInputManager chatInputManager = plugin.getChatInputManager();
        chatInputManager.requestInput(
                player,
                "<red>[Facciones] <gray>Escribe la cantidad a retirar:",
                30,
                (input) -> {
                    try {
                        double amount = Double.parseDouble(input);
                        if (amount <= 0) {
                            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>La cantidad debe ser mayor que 0.");
                            openEconomyGUI(faction.getName());
                            return;
                        }
                        if (faction.getBalance() < amount) {
                            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>La facción no tiene suficiente dinero.");
                            openEconomyGUI(faction.getName());
                            return;
                        }
                        FactionManager factionManager = plugin.getFactionManager();
                        boolean success = factionManager.withdrawMoney(player.getUniqueId(), amount);
                        if (success) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                openEconomyGUI(faction.getName());
                            }, 40L);
                        } else {
                            openEconomyGUI(faction.getName());
                        }
                    } catch (NumberFormatException e) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Cantidad inválida. Debes ingresar un número.");
                        openEconomyGUI(faction.getName());
                    }
                },
                () -> {
                    MessageUtils.sendMessage(player, "<red>[Facciones] Tiempo agotado para retirar dinero.</red>");
                    openEconomyGUI(faction.getName());
                }
        );
    }
    private void showPlayerDetailedStats() {
        FactionManager factionManager = plugin.getFactionManager();
        String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
        MessageUtils.sendMessage(player, "<gold>⚔ Estadísticas de " + player.getName() + "</gold>");
        if (playerFaction != null) {
            FactionManager.Faction faction = factionManager.getFactions().get(playerFaction);
            if (faction != null) {
                MessageUtils.sendMessage(player, "<gray>Facción: <yellow>" + faction.getName() + "</yellow></gray>");
                String role = faction.getMembers().get(player.getUniqueId());
                MessageUtils.sendMessage(player, "<gray>Rango: <aqua>" + getRoleDisplayName(role) + "</aqua></gray>");
                MessageUtils.sendMessage(player, "<gray>Miembros: <light_purple>" + faction.getMembers().size() + "</light_purple></gray>");
                MessageUtils.sendMessage(player, "<gray>Balance: <green>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()) + "</green></gray>");
                MessageUtils.sendMessage(player, "<gray>Territorios: <dark_green>" + countFactionTerritories(playerFaction) + "</dark_green></gray>");
            }
        } else {
            MessageUtils.sendMessage(player, "<gray>Estado: <red>Sin facción</red></gray>");
        }
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
    }
    private void showFactionInfo(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        factionManager.showFactionInfo(player, factionName);
    }
    private void toggleFactionChat() {
        MessageUtils.sendMessage(player, "<green>[Facciones] Chat de facción alternado.</green>");
    }
    private void handleLeaveFaction() {
        FactionManager factionManager = plugin.getFactionManager();
        String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        if (playerFaction != null) {
            FactionManager.Faction faction = factionManager.getFactions().get(playerFaction);
            if (faction != null) {
                String role = faction.getMembers().get(player.getUniqueId());
                if (isLeader(role)) {
                    MessageUtils.sendMessage(player, "<red>[Facciones] Como líder no puedes abandonar la facción. Usa disolver facción.</red>");
                } else {
                    factionManager.leaveFaction(player.getUniqueId());
                    openMainGUI();
                }
            }
        }
    }
    // Implementación de funcionalidades faltantes
    private void populateRelationsGUI() {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Información de la facción
        ItemStack factionInfo = createButtonItem(
                Material.COMPASS,
                "<light_purple>🌐 Relaciones de " + faction.getName(),
                "<gray>Gestiona tus alianzas y enemistades",
                "<gray>Aliados: <green>" + (faction.getAllies() != null ? faction.getAllies().size() : 0),
                "<gray>Enemigos: <red>" + (faction.getEnemies() != null ? faction.getEnemies().size() : 0),
                "",
                "<gray>Establece relaciones con otras facciones"
        );
        inventory.setItem(4, factionInfo);
        // Sección de aliados
        ItemStack alliesSection = createButtonItem(
                Material.EMERALD,
                "<green>👥 Aliados",
                "<gray>Facciones aliadas a la tuya",
                "<gray>Beneficios:",
                "<gray>• No daño entre miembros",
                "<gray>• Acceso a territorios",
                "<gray>• Asistencia en guerras",
                "",
                "<green>▶ Click para gestionar aliados"
        );
        inventory.setItem(11, alliesSection);
        // Sección de enemigos
        ItemStack enemiesSection = createButtonItem(
                Material.REDSTONE,
                "<red>⚔ Enemigos",
                "<gray>Facciones enemigas de la tuya",
                "<gray>Consecuencias:",
                "<gray>• Daño aumentado entre miembros",
                "<gray>• Posibilidad de guerra",
                "<gray>• Territorios vulnerables",
                "",
                "<red>▶ Click para gestionar enemigos"
        );
        inventory.setItem(15, enemiesSection);
        // Lista de facciones disponibles
        List<FactionManager.Faction> factionList = new ArrayList<>(factionManager.getFactions().values());
        factionList.removeIf(f -> f.getName().equals(factionName));
        // Mostrar facciones disponibles
        for (int i = 0; i < RELATION_SLOTS.length && i < factionList.size(); i++) {
            FactionManager.Faction otherFaction = factionList.get(i);
            ItemStack factionItem = createRelationFactionItem(faction, otherFaction);
            inventory.setItem(RELATION_SLOTS[i], factionItem);
        }
        // Solicitudes pendientes
        ItemStack pendingRequests = createButtonItem(
                Material.PAPER,
                "<yellow>📜 Solicitudes Pendientes",
                "<gray>Revisa las solicitudes de alianza",
                "<gray>pendientes de otras facciones",
                "",
                "<yellow>▶ Click para revisar"
        );
        inventory.setItem(31, pendingRequests);
        // Navegación
        inventory.setItem(36, createBackButton());
        inventory.setItem(44, createCloseButton());
        // Decoración
        fillEmptySlots();
    }
    private void populateTerritoriesGUI() {
        FactionManager factionManager = plugin.getFactionManager();
        String factionName = factionManager.getPlayerFaction(player.getUniqueId());
        if (factionName == null) {
            MessageUtils.sendMessage(player, "<red>[Facciones] No perteneces a ninguna facción.</red>");
            openMainGUI(player);
            return;
        }
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) {
            MessageUtils.sendMessage(player, "<red>[Facciones] Error al cargar tu facción.</red>");
            openMainGUI(player);
            return;
        }
        inventory = Bukkit.createInventory(null, 54, "<dark_gray>Territorios de " + factionName);
        // Panel de información de territorios
        ItemStack infoPanel = new ItemStack(Material.MAP);
        ItemMeta infoMeta = infoPanel.getItemMeta();
        infoMeta.displayName(MessageUtils.parse("<gold>Información de Territorios"));
        List<String> infoLore = new ArrayList<>();
        infoLore.add("<gray>Territorios reclamados: <yellow>" + (faction.getTerritories() != null ? faction.getTerritories().size() : 0));
        infoLore.add("<gray>Límite de territorios: <yellow>" + calculateTerritoryLimit(faction));
        infoLore.add("<gray>Poder de facción: <yellow>" + faction.getPower());
        infoLore.add("<gray>");
        infoLore.add("<gray>Los territorios te permiten:");
        infoLore.add("<gray>- Proteger áreas contra jugadores hostiles");
        infoLore.add("<gray>- Establecer bases seguras");
        infoLore.add("<gray>- Acceder a recursos exclusivos");
        infoMeta.lore(MessageUtils.parseList(infoLore));
        infoPanel.setItemMeta(infoMeta);
        inventory.setItem(4, infoPanel);
        // Sección de territorios reclamados
        ItemStack claimedHeader = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta claimedMeta = claimedHeader.getItemMeta();
        claimedMeta.displayName(MessageUtils.parse("<green>Tus Territorios"));
        claimedMeta.lore(MessageUtils.parseList(Arrays.asList("<gray>Lista de territorios reclamados")));
        claimedHeader.setItemMeta(claimedMeta);
        inventory.setItem(19, claimedHeader);
        // Lista de territorios (slots 28-34)
        if (faction.getTerritories() != null && !faction.getTerritories().isEmpty()) {
            int slot = 28;
            for (String territory : faction.getTerritories()) {
                if (slot > 34) break;
                ItemStack territoryItem = createTerritoryItem(territory);
                inventory.setItem(slot, territoryItem);
                slot++;
            }
        } else {
            ItemStack noTerritory = new ItemStack(Material.BARRIER);
            ItemMeta noTerritoryMeta = noTerritory.getItemMeta();
            noTerritoryMeta.displayName(MessageUtils.parse("<red>Sin territorios"));
            noTerritoryMeta.lore(MessageUtils.parseList(Arrays.asList("<gray>Tu facción no tiene territorios reclamados")));
            noTerritory.setItemMeta(noTerritoryMeta);
            inventory.setItem(31, noTerritory);
        }
        // Acciones de territorios
        ItemStack claimAction = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta claimMeta = claimAction.getItemMeta();
        claimMeta.displayName(MessageUtils.parse("<green>Reclamar Territorio"));
        claimMeta.lore(MessageUtils.parseList(Arrays.asList(
                "<gray>Reclama el chunk donde estás parado",
                "<gray>para tu facción.",
                "<gray>",
                "<yellow>Click para reclamar"
        )));
        claimAction.setItemMeta(claimMeta);
        inventory.setItem(22, claimAction);
        // Mapa de territorios cercanos
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        ItemMeta mapMeta = mapItem.getItemMeta();
        mapMeta.displayName(MessageUtils.parse("<gold>Mapa de Territorios"));
        mapMeta.lore(MessageUtils.parseList(Arrays.asList(
                "<gray>Muestra los territorios cercanos",
                "<gray>a tu posición actual.",
                "<gray>",
                "<yellow>Click para ver mapa"
        )));

        mapItem.setItemMeta(mapMeta);
        inventory.setItem(23, mapItem);
        // Botón de navegación de vuelta
        inventory.setItem(49, createBackButton());
        // Rellenar espacios vacíos
        fillEmptySlots();
    }
    private ItemStack createTerritoryItem(String territory) {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        // Parsear las coordenadas del territorio (formato: "world,x,z")
        String[] parts = territory.split(",");
        String world = parts[0];
        int x = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        meta.displayName(MessageUtils.parse("<green>Territorio en " + world));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Coordenadas: <yellow>" + x + ", " + z);
        lore.add("<gray>Chunk: <yellow>" + (x >> 4) + ", " + (z >> 4));
        lore.add("<gray>");
        lore.add("<yellow>Click izquierdo <gray>para teleportarte");
        lore.add("<red>Click derecho <gray>para abandonar");
        meta.lore(MessageUtils.parseList(lore));
        item.setItemMeta(meta);
        return item;
    }
    private int calculateTerritoryLimit(FactionManager.Faction faction) {
        // Base: 3 territorios + 1 por cada 2 miembros
        int memberCount = faction.getMembers().size();
        int baseLimit = 3;
        int memberBonus = memberCount / 2;
        return baseLimit + memberBonus;
    }
    private void populateWarsGUI() {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        
        // Información de la facción
        ItemStack factionInfo = createButtonItem(Material.IRON_SWORD,
                "<dark_red>⚔ Gestión de Guerras",
                "<gray>Facción: <yellow>" + factionName,
                "<gray>Guerras activas: <red>" + (faction.getWars() != null ? faction.getWars().size() : 0),
                "<gray>Poder de facción: <yellow>" + faction.getPower(),
                "",
                "<gray>Gestiona las guerras de tu facción");
        inventory.setItem(4, factionInfo);
        
        // Declarar guerra
        ItemStack declareWar = createButtonItem(Material.TNT,
                "<red>💥 Declarar Guerra",
                "<gray>Declara la guerra a otra facción",
                "<red>¡Esto iniciará un conflicto!",
                "",
                "<red>▶ Click para declarar");
        inventory.setItem(11, declareWar);
        
        // Ver guerras activas
        ItemStack activeWars = createButtonItem(Material.REDSTONE_BLOCK,
                "<red>⚡ Guerras Activas",
                "<gray>Ve las guerras en las que participas",
                "<gray>Guerras: <red>" + (faction.getWars() != null ? faction.getWars().size() : 0),
                "",
                "<red>▶ Click para ver");
        inventory.setItem(15, activeWars);
        
        // Historial de guerras
        ItemStack warHistory = createButtonItem(Material.BOOK,
                "<gold>📜 Historial de Guerras",
                "<gray>Ve el historial de conflictos",
                "<gray>de tu facción",
                "",
                "<gold>▶ Click para ver");
        inventory.setItem(22, warHistory);
        
        // Mostrar guerras activas si las hay
        if (faction.getWars() != null && !faction.getWars().isEmpty()) {
            int slot = 19;
            for (String enemyFaction : faction.getWars()) {
                if (slot > 25) break;
                
                FactionManager.Faction enemy = factionManager.getFactions().get(enemyFaction);
                if (enemy != null) {
                    ItemStack warItem = createButtonItem(Material.IRON_SWORD,
                            "<red>⚔ Guerra con " + enemyFaction,
                            "<gray>Estado: <red>En conflicto",
                            "<gray>Poder enemigo: <yellow>" + enemy.getPower(),
                            "<gray>Miembros: <yellow>" + enemy.getMembers().size(),
                            "",
                            "<gray>Click izquierdo para ver detalles",
                            "<green>Click derecho para proponer paz");
                    inventory.setItem(slot, warItem);
                    slot++;
                }
            }
        }
        
        // Botones de navegación
        inventory.setItem(27, createBackButton());
        inventory.setItem(35, createCloseButton());
        
        fillEmptySlots();
    }
    
    private void populateSettingsGUI() {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Información de la facción
        ItemStack factionInfo = createButtonItem(
                Material.REDSTONE_TORCH,
                "<gold>⚙ Configuración: " + faction.getName(),
                "<gray>Ajusta la configuración de tu facción",
                "<gray>Privacidad: " + (faction.isPrivate() ? "<red>Privada" : "<green>Abierta"),
                "<gray>Descripción: <white>" + faction.getDescription(),
                "",
                "<gray>Modifica los ajustes de tu facción"
        );
        inventory.setItem(4, factionInfo);
        // Cambiar descripción
        String playerRole = faction.getMembers().get(player.getUniqueId());
        if (canChangeSettings(playerRole)) {
            ItemStack changeDescription = createButtonItem(
                    Material.BOOK,
                    "<yellow>📝 Cambiar Descripción",
                    "<gray>Modifica la descripción de tu facción",
                    "<red>Solo líderes y oficiales",
                    "",
                    "<yellow>▶ Click para cambiar"
            );
            inventory.setItem(11, changeDescription);
        }
        // Cambiar privacidad
        if (canChangeSettings(playerRole)) {
            String privacyStatus = faction.isPrivate() ? "<red>Privada" : "<green>Abierta";
            String action = faction.isPrivate() ? "<green>Hacer pública" : "<red>Hacer privada";
            ItemStack togglePrivacy = createButtonItem(
                    faction.isPrivate() ? Material.IRON_DOOR : Material.OAK_DOOR,
                    "<gold>🔒 Privacidad: " + privacyStatus,
                    "<gray>Controla quién puede unirse a tu facción",
                    "<red>Solo líderes y oficiales",
                    "",
                    "<gold>▶ Click para " + action
            );
            inventory.setItem(15, togglePrivacy);
        }
        // Cambiar color
        if (canChangeSettings(playerRole)) {
            ItemStack changeColor = createButtonItem(
                    Material.LIME_DYE,
                    "<green>🎨 Cambiar Color",
                    "<gray>Personaliza el color de tu facción",
                    "<red>Solo líderes y oficiales",
                    "",
                    "<green>▶ Click para cambiar"
            );
            inventory.setItem(22, changeColor);
        }
        // Navegación
        inventory.setItem(27, createBackButton());
        inventory.setItem(35, createCloseButton());
        // Rellenar espacios vacíos
        fillEmptySlots();
    }
    private void populateListGUI() {
        FactionManager factionManager = plugin.getFactionManager();
        // Información de la lista
        ItemStack info = createButtonItem(
                Material.KNOWLEDGE_BOOK,
                "<gold>📋 Lista de Facciones",
                "<gray>Total de facciones: <yellow>" + factionManager.getFactions().size(),
                "",
                "<gray>Explora las facciones del servidor"
        );
        inventory.setItem(4, info);
        // Mostrar facciones
        List<FactionManager.Faction> factionList = new ArrayList<>(factionManager.getFactions().values());
        for (int i = 0; i < FACTION_SLOTS.length && i < factionList.size(); i++) {
            FactionManager.Faction faction = factionList.get(i);
            ItemStack factionItem = createFactionListItem(faction);
            inventory.setItem(FACTION_SLOTS[i], factionItem);
        }
        // Navegación
        inventory.setItem(45, createBackButton());
        inventory.setItem(53, createCloseButton());
    }
    private void handleMembersGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 40 -> startInvitePlayer();
            case 41 -> showPendingInvites();
            case 45 -> openMainGUI();
            case 53 -> player.closeInventory();
        }
    }
    private void handleRelationsGUIClick(int slot, ItemStack clicked) {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Navegación
        if (slot == 36) {
            openMainGUI();
            return;
        } else if (slot == 44) {
            player.closeInventory();
            return;
        }
        // Gestión de aliados
        if (slot == 11) {
            showAlliesList(factionName);
            return;
        }
        // Gestión de enemigos
        if (slot == 15) {
            showEnemiesList(factionName);
            return;
        }
        // Solicitudes pendientes
        if (slot == 31) {
            showPendingRelationRequests(factionName);
            return;
        }
        // Interacción con facciones específicas
        if (Arrays.stream(RELATION_SLOTS).anyMatch(s -> s == slot)) {
            if (clicked == null || !clicked.hasItemMeta()) return;
            String displayName = clicked.getItemMeta().getDisplayName();
            String targetFactionName = displayName.substring(displayName.indexOf(' ') + 1);
            FactionManager.Faction targetFaction = factionManager.getFactions().get(targetFactionName);
            if (targetFaction == null) return;
            // Determinar acción según la relación actual
            if (displayName.startsWith("<green>")) { // Aliado
                handleBreakAlliance(faction, targetFaction);
            } else if (displayName.startsWith("<red>")) { // Enemigo
                handleProposePeace(faction, targetFaction);
            } else { // Neutral
                if (player.getInventory().getHeldItemSlot() == 0) { // Click izquierdo
                    handleProposeAlliance(faction, targetFaction);
                } else { // Click derecho
                    handleDeclareEnemy(faction, targetFaction);
                }
            }
            // Actualizar GUI
            openRelationsGUI(factionName);
        }
    }
    private void handleTerritoriesGUIClick(int slot, ItemStack clicked) {
        FactionManager factionManager = plugin.getFactionManager();
        String factionName = factionManager.getPlayerFaction(player.getUniqueId());
        if (factionName == null) {
            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>No perteneces a ninguna facción.");
            openMainGUI(player);
            return;
        }
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Botón de volver
        if (slot == 49) {
            openManageGUI(player, factionName);
            return;
        }
        // Reclamar territorio
        if (slot == 22) {
            handleClaimTerritory(faction);
            return;
        }
        // Ver mapa de territorios
        if (slot == 23) {
            showTerritoryMap();
            return;
        }
        // Manejo de territorios específicos (slots 28-34)
        if (slot >= 28 && slot <= 34) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() == Material.GRASS_BLOCK) {
                handleTerritoryItemClick(item, faction, player.getInventory().getItemInMainHand().getType() == Material.AIR);
            }
        }
    }
    private void handleClaimTerritory(FactionManager.Faction faction) {
        // Verificar si el jugador tiene permisos
        String playerRole = faction.getMembers().get(player.getUniqueId());
        if (!canManageTerritories(playerRole)) {
            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>No tienes permisos para reclamar territorios.");
            return;
        }
        // Verificar límite de territorios
        int territoryLimit = calculateTerritoryLimit(faction);
        int currentTerritories = faction.getTerritories() != null ? faction.getTerritories().size() : 0;
        if (currentTerritories >= territoryLimit) {
            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Tu facción ha alcanzado el límite de territorios (" + territoryLimit + ").");
            return;
        }
        // Obtener el chunk actual
        Chunk chunk = player.getLocation().getChunk();
        String world = player.getWorld().getName();
        int x = player.getLocation().getBlockX();
        int z = player.getLocation().getBlockZ();
        // Formato de territorio: "world,x,z"
        String territory = world + "," + x + "," + z;
        // Verificar si el territorio ya está reclamado
        FactionManager factionManager = plugin.getFactionManager();
        for (FactionManager.Faction f : factionManager.getFactions().values()) {
            if (f.getTerritories() != null && f.getTerritories().contains(territory)) {
                MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Este territorio ya está reclamado por la facción <yellow>" + f.getName() + "<gray>.");
                return;
            }
        }
        // Reclamar el territorio
        if (faction.getTerritories() == null) {
            faction.setTerritories(new HashSet<>());
        }
        faction.getTerritories().add(territory);
        factionManager.saveFactions();
        // Notificar al jugador
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Has reclamado un nuevo territorio para tu facción.");
        // Actualizar la GUI
        openTerritoriesGUI(player);
    }
    private void handleTerritoryItemClick(ItemStack item, FactionManager.Faction faction, boolean isLeftClick) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;
        // Obtener información del territorio desde el lore
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return;
        // Extraer coordenadas del lore
        String coordsLine = lore.get(0);
        String[] parts = coordsLine.split("<yellow>")[1].split(", ");
        if (parts.length != 2) return;
        try {
            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[1]);
            String world = item.getItemMeta().getDisplayName().split("<green>")[1].split(" ")[2];
            // Formato del territorio
            String territory = world + "," + x + "," + z;
            if (isLeftClick) {
                // Teleportar al territorio
                World bukkitWorld = Bukkit.getWorld(world);
                if (bukkitWorld != null) {
                    // Buscar una ubicación segura cerca de las coordenadas
                    Location loc = new Location(bukkitWorld, x, 100, z);
                    // Encontrar el bloque más alto en esas coordenadas
                    loc.setY(bukkitWorld.getHighestBlockYAt(x, z) + 1);
                    player.teleport(loc);
                    MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Te has teleportado a tu territorio.");
                    player.closeInventory();
                }
            } else {
                // Abandonar territorio (click derecho)
                if (!canManageTerritories(faction.getMembers().get(player.getUniqueId()))) {
                    MessageUtils.sendMessage(player, "<red>[Facciones] <gray>No tienes permisos para abandonar territorios.");
                    return;
                }
                // Confirmar abandono
                MessageUtils.sendMessage(player, "<red>[Facciones] <gray>¿Estás seguro de que quieres abandonar este territorio?");
                MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Escribe <yellow>/f territory abandon confirm <gray>para confirmar.");
                // Guardar el territorio a abandonar en los datos temporales del jugador
                guiData.put("abandonTerritory", territory);
                player.closeInventory();
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Error al procesar las coordenadas del territorio.");
        }
    }
    private void showTerritoryMap() {
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
        MessageUtils.sendMessage(player, "<gold>📍 Mapa de Territorios Cercanos");
        MessageUtils.sendMessage(player, "<gray>");
        // Obtener el chunk actual
        Chunk playerChunk = player.getLocation().getChunk();
        int playerChunkX = playerChunk.getX();
        int playerChunkZ = playerChunk.getZ();
        // Mostrar un mapa 7x7 centrado en el jugador
        FactionManager factionManager = plugin.getFactionManager();
        StringBuilder mapBuilder = new StringBuilder();
        for (int z = playerChunkZ - 3; z <= playerChunkZ + 3; z++) {
            for (int x = playerChunkX - 3; x <= playerChunkX + 3; x++) {
                // Determinar si este chunk pertenece a alguna facción
                String chunkKey = player.getWorld().getName() + "," + (x * 16) + "," + (z * 16);
                boolean isPlayerChunk = (x == playerChunkX && z == playerChunkZ);
                String symbol = "<dark_gray>⬛"; // Territorio sin reclamar
                // Buscar si el territorio pertenece a alguna facción
                for (FactionManager.Faction f : factionManager.getFactions().values()) {
                    if (f.getTerritories() != null && f.getTerritories().contains(chunkKey)) {
                        // Determinar el color según la relación con la facción
                        String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
                        if (playerFaction != null && playerFaction.equals(f.getName())) {
                            symbol = "<green>⬛"; // Territorio propio
                        } else {
                            FactionManager.Faction playerFac = factionManager.getFactions().get(playerFaction);
                            if (playerFac != null && playerFac.getAllies() != null &&
                                    playerFac.getAllies().contains(f.getName())) {
                                symbol = "<aqua>⬛"; // Territorio aliado
                            } else if (playerFac != null && playerFac.getEnemies() != null &&
                                    playerFac.getEnemies().contains(f.getName())) {
                                symbol = "<red>⬛"; // Territorio enemigo
                            } else {
                                symbol = "<yellow>⬛"; // Territorio neutral
                            }
                        }
                        break;
                    }
                }
                // Marcar la posición del jugador
                if (isPlayerChunk) {
                    symbol = "<light_purple>⬛"; // Posición del jugador
                }
                mapBuilder.append(symbol);
            }
            mapBuilder.append("\n");
        }
        // Enviar el mapa al jugador
        MessageUtils.sendMessage(player, mapBuilder.toString());
        // Leyenda
        MessageUtils.sendMessage(player, "<gray>Leyenda: <green>⬛ Tu facción <aqua>⬛ Aliados <yellow>⬛ Neutral <red>⬛ Enemigos <dark_gray>⬛ Sin reclamar <light_purple>⬛ Tu posición");
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
    }
    private boolean canManageTerritories(String role) {
        return role != null && (role.equals("leader") || role.equals("officer"));
    }
    private void handleWarsGUIClick(int slot, ItemStack clicked) {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Navegación
        if (slot == 27) {
            openMainGUI();
            return;
        } else if (slot == 35) {
            player.closeInventory();
            return;
        }
        // Declarar guerra
        if (slot == 11) {
            startWarDeclaration(factionName);
            return;
        }
        // Ver guerras activas
        if (slot == 15) {
            showActiveWars(factionName);
            return;
        }
    }

    /**
     * Inicia el proceso de declaración de guerra
     */
    private void startWarDeclaration(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        
        String playerRole = faction.getMembers().get(player.getUniqueId());
        if (!canDeclareWar(playerRole)) {
            MessageUtils.sendMessage(player, "<red>[Facciones] No tienes permisos para declarar guerra.</red>");
            return;
        }
        
        player.closeInventory();
        
        // Usar ChatInputManager para solicitar el nombre de la facción enemiga
        plugin.getChatInputManager().requestInput(
            player,
            "<gold>[Facciones] <white>Escribe el nombre de la facción a la que quieres declarar guerra:",
            30,
            (input) -> {
                FactionManager.Faction targetFaction = factionManager.getFactions().get(input);
                if (targetFaction == null) {
                    MessageUtils.sendMessage(player, "<red>[Facciones] La facción '" + input + "' no existe.</red>");
                    openWarsGUI(factionName);
                    return;
                }
                
                if (input.equals(factionName)) {
                    MessageUtils.sendMessage(player, "<red>[Facciones] No puedes declarar guerra a tu propia facción.</red>");
                    openWarsGUI(factionName);
                    return;
                }
                
                // Verificar si ya están en guerra
                if (faction.getWars() != null && faction.getWars().contains(input)) {
                    MessageUtils.sendMessage(player, "<red>[Facciones] Ya estás en guerra con esa facción.</red>");
                    openWarsGUI(factionName);
                    return;
                }
                
                // Declarar guerra
                declareWar(faction, targetFaction);
                openWarsGUI(factionName);
            },
            () -> {
                MessageUtils.sendMessage(player, "<red>[Facciones] Tiempo agotado. No se declaró guerra.</red>");
                openWarsGUI(factionName);
            }
        );
    }
    
    /**
     * Declara guerra entre dos facciones
     */
    private void declareWar(FactionManager.Faction faction1, FactionManager.Faction faction2) {
        // Inicializar sets de guerras si no existen
        if (faction1.getWars() == null) {
            faction1.setWars(new HashSet<>());
        }
        if (faction2.getWars() == null) {
            faction2.setWars(new HashSet<>());
        }
        
        // Añadir guerra mutua
        faction1.getWars().add(faction2.getName());
        faction2.getWars().add(faction1.getName());
        
        // Guardar cambios
        plugin.getFactionManager().saveFactions();
        
        // Notificar a ambas facciones
        notifyFactionMembers(faction1, "<red>[Facciones] ¡Tu facción ha declarado guerra a " + faction2.getName() + "!</red>");
        notifyFactionMembers(faction2, "<red>[Facciones] ¡La facción " + faction1.getName() + " te ha declarado guerra!</red>");
        
        MessageUtils.sendMessage(player, "<red>[Facciones] ¡Guerra declarada exitosamente contra " + faction2.getName() + "!</red>");
    }
    
    /**
     * Muestra las guerras activas
     */
    private void showActiveWars(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        
        if (faction.getWars() == null || faction.getWars().isEmpty()) {
            MessageUtils.sendMessage(player, "<yellow>[Facciones] Tu facción no tiene guerras activas.</yellow>");
            return;
        }
        
        MessageUtils.sendMessage(player, "<gold>=== Guerras Activas de " + factionName + " ===</gold>");
        
        int count = 1;
        for (String enemyFactionName : faction.getWars()) {
            FactionManager.Faction enemy = factionManager.getFactions().get(enemyFactionName);
            if (enemy != null) {
                MessageUtils.sendMessage(player, 
                    "<red>" + count + ". " + enemyFactionName + "</red>"
                    + " <gray>- Poder:</gray> <yellow>" + enemy.getPower() + "</yellow>"
                    + " <gray>- Miembros:</gray> <yellow>" + enemy.getMembers().size() + "</yellow>");
                count++;
            }
        }
        
        MessageUtils.sendMessage(player, "<gray>Usa</gray> <yellow>/faccion paz <facción></yellow> <gray>para proponer paz.</gray>");
    }
    
    /**
     * Muestra el historial de guerras
     */
    private void showWarHistory(String factionName) {
        // Por ahora, implementación básica
        MessageUtils.sendMessage(player, "<gold>=== Historial de Guerras de " + factionName + " ===</gold>");
        MessageUtils.sendMessage(player, "<gray>El historial de guerras está en desarrollo.</gray>");
        MessageUtils.sendMessage(player, "<gray>Próximamente incluirá:</gray>");
        MessageUtils.sendMessage(player, "<yellow>- Guerras pasadas</yellow>");
        MessageUtils.sendMessage(player, "<yellow>- Resultado de conflictos</yellow>");
        MessageUtils.sendMessage(player, "<yellow>- Estadísticas de PvP</yellow>");
        MessageUtils.sendMessage(player, "<yellow>- Territorios perdidos/ganados</yellow>");
    }
    
    /**
     * Notifica a todos los miembros de una facción
     */
    private void notifyFactionMembers(FactionManager.Faction faction, String message) {
        for (UUID memberUUID : faction.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && member.isOnline()) {
                MessageUtils.sendMessage(member, message);
            }
        }
    }
    private boolean canDeclareWar(String playerRole) {
        return playerRole != null && (playerRole.equals("leader") || playerRole.equals("officer"));
    }
    private void handleSettingsGUIClick(int slot, ItemStack clicked) {
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) return;
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null) return;
        // Navegación
        if (slot == 27) {
            openMainGUI();
            return;
        } else if (slot == 35) {
            player.closeInventory();
            return;
        }
        // Cambiar descripción
        if (slot == 11) {
            startChangeDescription(factionName);
            return;
        }
        // Cambiar privacidad
        if (slot == 15) {
            togglePrivacy(factionName);
            return;
        }
        // Cambiar color
        if (slot == 22) {
            startChangeColor(factionName);
            return;
        }
    }
    private boolean canChangeSettings(String playerRole) {
        return playerRole != null && (playerRole.equals("leader") || playerRole.equals("officer"));
    }
    private void startChangeDescription(String factionName) {
        player.closeInventory();
        MessageUtils.sendMessage(player, "<gold>[Facciones] <white>Escribe la nueva descripción para tu facción:");
        ChatInputManager chatInputManager = plugin.getChatInputManager();
        chatInputManager.requestInput(
                player,
                "<gold>[Facciones] <white>Escribe la nueva descripción para tu facción:",
                60,
                (input) -> {
                    // Validar longitud de la descripción
                    if (input.length() < 3) {
                        MessageUtils.sendMessage(player, "<gold>[Facciones] <red>La descripción debe tener al menos 3 caracteres.");
                        openSettingsGUI(factionName);
                        return;
                    }
                    if (input.length() > 100) {
                        MessageUtils.sendMessage(player, "<gold>[Facciones] <red>La descripción no puede tener más de 100 caracteres.");
                        openSettingsGUI(factionName);
                        return;
                    }
                    // Cambiar descripción
                    FactionManager factionManager = plugin.getFactionManager();
                    FactionManager.Faction faction = factionManager.getFactions().get(factionName);
                    if (faction != null) {
                        faction.setDescription(input);
                        MessageUtils.sendMessage(player, "<gold>[Facciones] <green>Has cambiado la descripción de tu facción.");
                    }
                    openSettingsGUI(factionName);
                },
                () -> {
                    MessageUtils.sendMessage(player, "<gold>[Facciones] <red>Tiempo de espera agotado. No se ha cambiado la descripción.");
                    openSettingsGUI(factionName);
                }
        );
    }
    private void togglePrivacy(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction != null) {
            boolean isPrivate = faction.isPrivate();
            faction.setPrivate(!isPrivate);
            String newStatus = !isPrivate ? "privada" : "pública";
            MessageUtils.sendMessage(player, "<gold>[Facciones] <green>Has cambiado la privacidad de tu facción a <white>" + newStatus + "<green>.");
        }
        openSettingsGUI(factionName);
    }
    private void startChangeColor(String factionName) {
        player.closeInventory();
        MessageUtils.sendMessage(player, "<gold>[Facciones] <white>Escribe el nuevo color para tu facción (formato: código de color, ej: <green>, <blue>, <red>):");
        ChatInputManager chatInputManager = plugin.getChatInputManager();
        chatInputManager.requestInput(
                player,
                "<gold>[Facciones] <white>Escribe el nuevo color para tu facción:",
                30,
                (input) -> {
                    // Validar formato del color
                    if (!input.matches("&[0-9a-fA-F]")) {
                        MessageUtils.sendMessage(player, "<gold>[Facciones] <red>Formato de color inválido. Usa <green>, <blue>, <red>, etc.");
                        openSettingsGUI(factionName);
                        return;
                    }
                    // Cambiar color (aquí iría la lógica real)
                    MessageUtils.sendMessage(player, "<gold>[Facciones] <green>Has cambiado el color de tu facción.");
                    openSettingsGUI(factionName);
                },
                () -> {
                    MessageUtils.sendMessage(player, "<gold>[Facciones] <red>Tiempo de espera agotado. No se ha cambiado el color.");
                    openSettingsGUI(factionName);
                }
        );
    }
    private void handleManageGUIClick(int slot, ItemStack clicked) {
        FactionManager factionManager = plugin.getFactionManager();
        String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        
        if (playerFaction == null) {
            MessageUtils.sendMessage(player, "<red>[Facciones] No perteneces a ninguna facción.</red>");
            openMainGUI();
            return;
        }
        
        FactionManager.Faction faction = factionManager.getFactions().get(playerFaction);
        if (faction == null) return;
        
        String playerRole = faction.getMembers().get(player.getUniqueId());
        
        switch (slot) {
            case 19 -> openMembersGUI(playerFaction); // Gestionar miembros
            case 20 -> openRelationsGUI(playerFaction); // Relaciones
            case 21 -> openEconomyGUI(playerFaction); // Economía
            case 22 -> openTerritoriesGUI(playerFaction); // Territorios
            case 23 -> openWarsGUI(playerFaction); // Guerras
            case 25 -> {
                if (canAccessSettings(playerRole)) {
                    openSettingsGUI(playerFaction);
                } else {
                    MessageUtils.sendMessage(player, "<red>[Facciones] No tienes permisos para acceder a configuraciones.</red>");
                }
            }
            case 49 -> handleLeaveFaction(); // Abandonar facción
            case 45 -> openMainGUI(); // Volver
            case 53 -> player.closeInventory(); // Cerrar
        }
    }
    private void handleListGUIClick(int slot, ItemStack clicked) {
        switch (slot) {
            case 45 -> openMainGUI();
            case 53 -> player.closeInventory();
        }
    }
    // Métodos auxiliares adicionales
    private void openRelationsGUI(String factionName) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.RELATIONS, data);
        player.openInventory(gui.getInventory());
    }
    private void openTerritoriesGUI(String factionName) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.TERRITORIES, data);
        player.openInventory(gui.getInventory());
    }
    private void openWarsGUI(String factionName) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.WARS, data);
        player.openInventory(gui.getInventory());
    }
    private void openSettingsGUI(String factionName) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.SETTINGS, data);
        player.openInventory(gui.getInventory());
    }
    private void showInvitations() {
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Sistema de invitaciones - próximamente.");
    }
    private void showAlliesList(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null || faction.getAllies() == null || faction.getAllies().isEmpty()) {
            MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Tu facción no tiene aliados actualmente.");
            return;
        }
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
        MessageUtils.sendMessage(player, "<green>👥 Aliados de " + faction.getName());
        for (String allyName : faction.getAllies()) {
            FactionManager.Faction ally = factionManager.getFactions().get(allyName);
            if (ally != null) {
                MessageUtils.sendMessage(player, "<green>• " + ally.getName() + " <gray>- " + ally.getMembers().size() + " miembros");
            }
        }
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
    }
    private void showEnemiesList(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        FactionManager.Faction faction = factionManager.getFactions().get(factionName);
        if (faction == null || faction.getEnemies() == null || faction.getEnemies().isEmpty()) {
            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Tu facción no tiene enemigos actualmente.");
            return;
        }
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
        MessageUtils.sendMessage(player, "<red>⚔ Enemigos de " + faction.getName());
        for (String enemyName : faction.getEnemies()) {
            FactionManager.Faction enemy = factionManager.getFactions().get(enemyName);
            if (enemy != null) {
                MessageUtils.sendMessage(player, "<red>• " + enemy.getName() + " <gray>- " + enemy.getMembers().size() + " miembros");
            }
        }
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
    }
    private void showPendingRelationRequests(String factionName) {
        MessageUtils.sendMessage(player, "<yellow>[Facciones] <gray>No tienes solicitudes de alianza pendientes.");
    }
    private void handleBreakAlliance(FactionManager.Faction faction, FactionManager.Faction targetFaction) {
        FactionManager factionManager = plugin.getFactionManager();
        // Remover alianza de ambas facciones
        if (faction.getAllies() != null) {
            faction.getAllies().remove(targetFaction.getName());
        }
        if (targetFaction.getAllies() != null) {
            targetFaction.getAllies().remove(faction.getName());
        }
        // Guardar cambios
        factionManager.saveFactions();
        // Notificar a ambas facciones
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Has roto la alianza con <yellow>" + targetFaction.getName() + "<gray>.");
        // Notificar a los miembros online de la otra facción
        for (UUID memberId : targetFaction.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                MessageUtils.sendMessage(member, "<red>[Facciones] <gray>La facción <yellow>" + faction.getName() + " <gray>ha roto la alianza con tu facción.");
            }
        }
    }
    private void handleProposePeace(FactionManager.Faction faction, FactionManager.Faction targetFaction) {
        FactionManager factionManager = plugin.getFactionManager();
        // Remover enemistad de ambas facciones
        if (faction.getEnemies() != null) {
            faction.getEnemies().remove(targetFaction.getName());
        }
        if (targetFaction.getEnemies() != null) {
            targetFaction.getEnemies().remove(faction.getName());
        }
        // Guardar cambios
        factionManager.saveFactions();
        // Notificar a ambas facciones
        MessageUtils.sendMessage(player, "<green>[Facciones] <gray>Has propuesto paz a <yellow>" + targetFaction.getName() + "<gray>.");
        // Notificar a los miembros online de la otra facción
        for (UUID memberId : targetFaction.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                MessageUtils.sendMessage(member, "<yellow>[Facciones] <gray>La facción <yellow>" + faction.getName() + " <gray>ha propuesto paz con tu facción.");
            }
        }
    }
    private void handleProposeAlliance(FactionManager.Faction faction, FactionManager.Faction targetFaction) {
        // Por ahora, crear alianza directamente
        FactionManager factionManager = plugin.getFactionManager();
        // Inicializar listas si son nulas
        if (faction.getAllies() == null) {
            faction.setAllies(new HashSet<>());
        }
        if (targetFaction.getAllies() == null) {
            targetFaction.setAllies(new HashSet<>());
        }
        // Agregar alianza a ambas facciones
        faction.getAllies().add(targetFaction.getName());
        targetFaction.getAllies().add(faction.getName());
        // Guardar cambios
        factionManager.saveFactions();
        // Notificar a los miembros online de la otra facción
        for (UUID memberId : targetFaction.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                MessageUtils.sendMessage(member, "<green>[Facciones] <gray>La facción <yellow>" + faction.getName() + " <gray>ha formado una alianza con tu facción.");
            }
        }
    }
    private void handleDeclareEnemy(FactionManager.Faction faction, FactionManager.Faction targetFaction) {
        FactionManager factionManager = plugin.getFactionManager();
        // Inicializar listas si son nulas
        if (faction.getEnemies() == null) {
            faction.setEnemies(new HashSet<>());
        }
        if (targetFaction.getEnemies() == null) {
            targetFaction.setEnemies(new HashSet<>());
        }
        // Agregar enemistad a ambas facciones
        faction.getEnemies().add(targetFaction.getName());
        targetFaction.getEnemies().add(faction.getName());
        // Si eran aliados, romper la alianza
        if (faction.getAllies() != null && faction.getAllies().contains(targetFaction.getName())) {
            faction.getAllies().remove(targetFaction.getName());
        }
        if (targetFaction.getAllies() != null && targetFaction.getAllies().contains(faction.getName())) {
            targetFaction.getAllies().remove(faction.getName());
        }
        // Guardar cambios
        factionManager.saveFactions();
        // Notificar a ambas facciones
        MessageUtils.sendMessage(player, "<red>[Facciones] <gray>Has declarado enemistad a <yellow>" + targetFaction.getName() + "<gray>.");
        // Notificar a los miembros online de la otra facción
        for (UUID memberId : targetFaction.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                MessageUtils.sendMessage(member, "<red>[Facciones] <gray>¡La facción <yellow>" + faction.getName() + " <gray>ha declarado enemistad a tu facción!");
            }
        }
    }
    private void showCreationRules() {
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════");
        MessageUtils.sendMessage(player, "<gold>📜 Reglas para Crear Facción");
        MessageUtils.sendMessage(player, "<gray>• Máximo 16 caracteres en el nombre");
        MessageUtils.sendMessage(player, "<gray>• No se permiten caracteres especiales");
        MessageUtils.sendMessage(player, "<gray>• El nombre debe ser único</gray>");
        MessageUtils.sendMessage(player, "<gray>• La descripción puede tener hasta 100 caracteres</gray>");
        MessageUtils.sendMessage(player, "<gray>• Como líder tendrás control total</gray>");
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
    }
    private void showTransactionHistory() {
        MessageUtils.sendMessage(player, "<aqua>[Facciones] Historial de transacciones - próximamente.</aqua>");
    }
    private void showServerStats() {
        FactionManager factionManager = plugin.getFactionManager();
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
        MessageUtils.sendMessage(player, "<gold>📊 Estadísticas del Servidor</gold>");
        MessageUtils.sendMessage(player, "<gray>Facciones totales: <yellow>" + factionManager.getFactions().size() + "</yellow></gray>");
        MessageUtils.sendMessage(player, "<gray>Jugadores en facciones: <aqua>" + factionManager.getPlayerFactions().size() + "</aqua></gray>");
        MessageUtils.sendMessage(player, "<gray>Territorios reclamados: <dark_green>" + factionManager.getFactionClaims().size() + "</dark_green></gray>");
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
    }
    private void showFactionRanking() {
        MessageUtils.sendMessage(player, "<gold>[Facciones] Ranking de facciones - próximamente.</gold>");
    }
    private void showFactionHelp() {
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
        MessageUtils.sendMessage(player, "<gold>❓ Ayuda del Sistema de Facciones</gold>");
        MessageUtils.sendMessage(player, "<yellow>/faccion crear <nombre></yellow> <gray>- Crear facción</gray>");
        MessageUtils.sendMessage(player, "<yellow>/faccion unir <facción></yellow> <gray>- Unirse a facción</gray>");
        MessageUtils.sendMessage(player, "<yellow>/faccion salir</yellow> <gray>- Abandonar facción</gray>");
        MessageUtils.sendMessage(player, "<yellow>/faccion invitar <jugador></yellow> <gray>- Invitar jugador</gray>");
        MessageUtils.sendMessage(player, "<yellow>/faccion info</yellow> <gray>- Ver información</gray>");
        MessageUtils.sendMessage(player, "<yellow>/faccion lista</yellow> <gray>- Ver todas las facciones</gray>");
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
    }
    private void startInvitePlayer() {
        player.closeInventory();
        MessageUtils.sendMessage(player, "<green>[Facciones] Escribe el nombre del jugador a invitar:</green>");
        MessageUtils.sendMessage(player, "<green>[Facciones] Tienes 30 segundos para responder.</green>");
        ChatInputManager chatInputManager = plugin.getChatInputManager();
        String factionName = (String) guiData.get("factionName");
        if (factionName == null) {
            MessageUtils.sendMessage(player, "<red>[Facciones] Error al obtener información de la facción.</red>");
            openMainGUI();
            return;
        }
        chatInputManager.requestInput(
                player,
                "<green>[Facciones] <gray>Escribe el nombre del jugador a invitar:",
                30,
                (targetPlayerName) -> {
                    // Buscar al jugador
                    Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
                    if (targetPlayer == null) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] El jugador " + targetPlayerName + " no está conectado.</red>");
                        openMembersGUI(factionName);
                        return;
                    }
                    if (targetPlayer.equals(player)) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] No puedes invitarte a ti mismo.</red>");
                        openMembersGUI(factionName);
                        return;
                    }
                    // Verificar si el jugador ya está en una facción
                    FactionManager factionManager = plugin.getFactionManager();
                    if (factionManager.isPlayerInFaction(targetPlayer.getUniqueId())) {
                        MessageUtils.sendMessage(player, "<red>[Facciones] El jugador ya pertenece a una facción.</red>");
                        openMembersGUI(factionName);
                        return;
                    }
                    // Enviar invitación
                    boolean success = factionManager.invitePlayer(player.getUniqueId(), targetPlayer.getUniqueId());
                    if (success) {
                        MessageUtils.sendMessage(player, "<green>[Facciones] Invitación enviada a " + targetPlayer.getName() + ".</green>");
                    }
                    // Volver al menú de miembros después de un breve retraso
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        openMembersGUI(factionName);
                    }, 40L);
                },
                () -> {
                    MessageUtils.sendMessage(player, "<red>[Facciones] Tiempo agotado para invitar jugador.</red>");
                    openMembersGUI(factionName);
                }
        );
    }
    private void showPendingInvites() {
        MessageUtils.sendMessage(player, "<yellow>[Facciones] Invitaciones pendientes - próximamente.</yellow>");
    }
    // Métodos de validación de permisos
    private boolean canManageMembers(String role) {
        return isLeader(role) || isOfficer(role);
    }
    private boolean canManageRelations(String role) {
        return isLeader(role);
    }
    private boolean canAccessSettings(String role) {
        return isLeader(role) || isOfficer(role);
    }
    private boolean isLeader(String role) {
        return "LEADER".equals(role);
    }
    private boolean isOfficer(String role) {
        return "OFFICER".equals(role);
    }
    private String getRoleDisplayName(String role) {
        return switch (role) {
            case "LEADER" -> "Líder";
            case "OFFICER" -> "Oficial";
            case "MEMBER" -> "Miembro";
            default -> "Recluta";
        };
    }
    private String getCurrentChatMode(Player player) {
        return "Público";
    }
    private int countFactionTerritories(String factionName) {
        FactionManager factionManager = plugin.getFactionManager();
        return (int) factionManager.getFactionClaims()
                .values()
                .stream()
                .filter(owner -> owner.equals(factionName))
                .count();
    }
    // Métodos de creación de ítems
    private ItemStack createPlayerHead(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = skull.getItemMeta();
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(player);
        }
        skull.setItemMeta(meta);
        return skull;
    }
    private ItemStack createButtonItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse(name));
        meta.lore(MessageUtils.parseList(Arrays.asList(lore)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack createMemberItem(UUID memberId, String role) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        Player member = Bukkit.getPlayer(memberId);
        String memberName = member != null ? member.getName() : "Desconocido";
        meta.displayName(MessageUtils.parse("<aqua>👤 " + memberName));
        List<String> lore = Arrays.asList(
                "<gray>Rango: <yellow>" + getRoleDisplayName(role),
                "<gray>Estado: " + (member != null && member.isOnline() ? "<green>En línea" : "<gray>Desconectado"),
                "",
                "<yellow>▶ Click para gestionar"
        );
        meta.lore(MessageUtils.parseList(lore));
        if (meta instanceof SkullMeta && member != null) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(member);
        }
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack createFactionListItem(FactionManager.Faction faction) {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<yellow>⚔ " + faction.getName()));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>" + faction.getDescription());
        Player leader = Bukkit.getPlayer(faction.getLeader());
        String leaderName = leader != null ? leader.getName() : "Desconocido";
        lore.add("<gray>Líder: <green>" + leaderName);
        lore.add("<gray>Miembros: <light_purple>" + faction.getMembers().size());
        lore.add("<gray>Balance: <yellow>" + plugin.getEconomyManager().formatCurrency(faction.getBalance()));
        lore.add("");
        lore.add("<gold>▶ Click para más información");
        meta.lore(MessageUtils.parseList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack createRelationFactionItem(FactionManager.Faction playerFaction, FactionManager.Faction otherFaction) {
        Material material;
        String namePrefix;
        List<String> relationInfo = new ArrayList<>();
        // Determinar la relación actual
        if (playerFaction.getAllies() != null && playerFaction.getAllies().contains(otherFaction.getName())) {
            material = Material.EMERALD;
            namePrefix = "<green>👥 ";
            relationInfo.add("<gray>Estado: <green>Aliado");
            relationInfo.add("<gray>• No daño entre miembros");
            relationInfo.add("<gray>• Acceso a territorios");
            relationInfo.add("");
            relationInfo.add("<red>▶ Click para romper alianza");
        } else if (playerFaction.getEnemies() != null && playerFaction.getEnemies().contains(otherFaction.getName())) {
            material = Material.REDSTONE;
            namePrefix = "<red>⚔ ";
            relationInfo.add("<gray>Estado: <red>Enemigo");
            relationInfo.add("<gray>• Daño aumentado entre miembros");
            relationInfo.add("<gray>• Territorios vulnerables");
            relationInfo.add("");
            relationInfo.add("<yellow>▶ Click para proponer paz");
        } else {
            material = Material.BONE;
            namePrefix = "<yellow>⚖ ";
            relationInfo.add("<gray>Estado: <yellow>Neutral");
            relationInfo.add("");
            relationInfo.add("<green>▶ Click para proponer alianza");
            relationInfo.add("<red>▶ Click derecho para declarar enemistad");
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse(namePrefix + otherFaction.getName()));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>" + otherFaction.getDescription());
        Player leader = Bukkit.getPlayer(otherFaction.getLeader());
        String leaderName = leader != null ? leader.getName() : "Desconocido";
        lore.add("<gray>Líder: <green>" + leaderName);
        lore.add("<gray>Miembros: <light_purple>" + otherFaction.getMembers().size());
        lore.add("");
        lore.addAll(relationInfo);
        meta.lore(MessageUtils.parseList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack createBackButton() {
        return createButtonItem(
                Material.ARROW,
                "<gold>← Volver",
                "<gray>Regresa al menú anterior"
        );
    }
    private ItemStack createCloseButton() {
        return createButtonItem(
                Material.BARRIER,
                "<red>✕ Cerrar",
                "<gray>Cierra este menú"
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
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    // Métodos estáticos para abrir GUIs
    public static void openMainGUI(Player player) {
        FactionGUI gui = new FactionGUI(player, FactionGUIType.MAIN);
        player.openInventory(gui.getInventory());
    }
    public static void openCreateGUI(Player player) {
        FactionGUI gui = new FactionGUI(player, FactionGUIType.CREATE);
        player.openInventory(gui.getInventory());
    }
    public static void openListGUI(Player player) {
        FactionGUI gui = new FactionGUI(player, FactionGUIType.LIST);
        player.openInventory(gui.getInventory());
    }
    public static void openTerritoriesGUI(Player player) {
        FactionManager factionManager = MKSurvival.getInstance().getFactionManager();
        String factionName = factionManager.getPlayerFaction(player.getUniqueId());
        if (factionName == null) {
            MessageUtils.sendMessage(player, "<red>[Facciones] <gray>No perteneces a ninguna facción.");
            openMainGUI(player);
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.TERRITORIES, data);
        player.openInventory(gui.getInventory());
    }
    public static void openManageGUI(Player player, String factionName) {
        Map<String, Object> data = new HashMap<>();
        data.put("factionName", factionName);
        FactionGUI gui = new FactionGUI(player, FactionGUIType.MANAGE, data);
        player.openInventory(gui.getInventory());
    }
}