package com.mk.mksurvival.gui.quests;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.quests.QuestManager;
import com.mk.mksurvival.managers.quests.PlayerQuest;
import com.mk.mksurvival.managers.quests.Quest;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.ArrayList;
import java.util.List;

public class QuestsGUI implements InventoryHolder {
    private final Player player;
    private final Inventory inventory;

    public QuestsGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.parse(("<gold>✦ Misiones ✦")));
        initializeItems();
    }

    private void initializeItems() {
        QuestManager questManager = MKSurvival.getInstance().getQuestManager();
        createPlayerInfoPanel(questManager);
        createActiveQuestsPanel(questManager);
        createAvailableQuestsPanel(questManager);
        createRewardsPanel();
        createNavigationButtons();
    }

    private void createPlayerInfoPanel(QuestManager questManager) {
        List<PlayerQuest> playerQuests = questManager.getPlayerQuests(player);
        int totalQuests = playerQuests.size();
        int completedQuests = (int) playerQuests.stream().filter(PlayerQuest::isCompleted).count();

        ItemStack playerInfo = createSkullItem(player);
        ItemMeta meta = playerInfo.getItemMeta();
        meta.displayName(MessageUtils.parse("<gold>⚔ Misiones del Jugador"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Activas: <yellow>" + (totalQuests - completedQuests) + "/" + totalQuests)));
        lore.add(MessageUtils.parse(("<gray>Completadas: <green>" + completedQuests)));
        lore.add(MessageUtils.parse(("<gray>Progreso: <aqua>" + getCompletionPercentage(playerQuests) + "%")));
        lore.add("");
        lore.add(MessageUtils.parse("<gold>▶ Click para ver logros"));
        meta.lore(lore);
        playerInfo.setItemMeta(meta);
        inventory.setItem(4, playerInfo);
    }

    private void createActiveQuestsPanel(QuestManager questManager) {
        List<PlayerQuest> playerQuests = questManager.getPlayerQuests(player);
        int[] questSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 24};
        int questIndex = 0;

        for (PlayerQuest playerQuest : playerQuests) {
            if (playerQuest.isCompleted()) continue;
            if (questIndex >= questSlots.length) break;

            ItemStack questItem = createActiveQuestItem(playerQuest);
            inventory.setItem(questSlots[questIndex], questItem);
            questIndex++;
        }

        while (questIndex < questSlots.length) {
            ItemStack emptySlot = createEmptyQuestSlot();
            inventory.setItem(questSlots[questIndex], emptySlot);
            questIndex++;
        }
    }

    private ItemStack createActiveQuestItem(PlayerQuest playerQuest) {
        Quest quest = playerQuest.getQuest();
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<green>⚡ " + quest.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>" + quest.getDescription())));
        lore.add("");
        lore.add(MessageUtils.parse(("<yellow>✦ Progreso:")));

        for (String requirement : quest.getRequirements().keySet()) {
            int required = quest.getRequirements().get(requirement);
            int progress = playerQuest.getProgress().getOrDefault(requirement, 0);
            String progressText;

            if (progress >= required) {
                progressText = MessageUtils.parse(("  <gray>• " + formatRequirementName(requirement) + ": <green>✓ " + progress + "/" + required));
            } else {
                progressText = MessageUtils.parse(("  <gray>• " + formatRequirementName(requirement) + ": <red>✗ " + progress + "/" + required));
            }
            lore.add(progressText);
        }

        lore.add("");
        lore.add(MessageUtils.parse(("<gray>Recompensa: <yellow>" + MKSurvival.getInstance().getEconomyManager().formatCurrency(quest.getMoneyReward()))));
        lore.add("");
        lore.add(MessageUtils.parse("<gold>▶ Click para ver detalles"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void createAvailableQuestsPanel(QuestManager questManager) {
        List<PlayerQuest> playerQuests = questManager.getPlayerQuests(player);
        int[] questSlots = {28, 29, 30, 31, 32, 33, 34};
        int questIndex = 0;

        for (Quest quest : questManager.getAvailableQuests().values()) {
            boolean hasQuest = playerQuests.stream()
                    .anyMatch(pq -> pq.getQuest().getId().equals(quest.getId()));

            if (hasQuest) continue;
            if (questIndex >= questSlots.length) break;

            ItemStack questItem = createAvailableQuestItem(quest);
            inventory.setItem(questSlots[questIndex], questItem);
            questIndex++;
        }

        while (questIndex < questSlots.length) {
            ItemStack emptySlot = createEmptyQuestSlot();
            inventory.setItem(questSlots[questIndex], emptySlot);
            questIndex++;
        }
    }

    private ItemStack createAvailableQuestItem(Quest quest) {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<aqua>⚡ " + quest.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>" + quest.getDescription())));
        lore.add("");
        lore.add(MessageUtils.parse(("<yellow>✦ Requisitos:")));

        for (String requirement : quest.getRequirements().keySet()) {
            int required = quest.getRequirements().get(requirement);
            lore.add(MessageUtils.parse(("  <gray>• " + formatRequirementName(requirement) + ": <red>" + required)));
        }

        lore.add("");
        lore.add(MessageUtils.parse(("<gray>Recompensa: <yellow>" + MKSurvival.getInstance().getEconomyManager().formatCurrency(quest.getMoneyReward()))));
        lore.add("");
        lore.add(MessageUtils.parse("<green>▶ Click para aceptar"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEmptyQuestSlot() {
        ItemStack item = new ItemStack(Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<gray>Espacio de Misión Vacío"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Completa misiones activas")));
        lore.add(MessageUtils.parse("<gray>para desbloquear más"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void createRewardsPanel() {
        ItemStack rewardsPanel = new ItemStack(Material.CHEST);
        ItemMeta meta = rewardsPanel.getItemMeta();
        meta.displayName(MessageUtils.parse("<gold>⭐ Recompensas Recientes"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Últimas 5 recompensas obtenidas:")));
        lore.add(MessageUtils.parse(("  <green>• +$50 por minar carbón")));
        lore.add(MessageUtils.parse(("  <green>• +$75 por matar zombi")));
        lore.add(MessageUtils.parse(("  <green>• +$100 por misión completada")));
        lore.add(MessageUtils.parse(("  <green>• Espada de hierro +5")));
        lore.add(MessageUtils.parse(("  <green>• Poción de velocidad")));
        lore.add("");
        lore.add(MessageUtils.parse("<gold>▶ Click para ver historial"));
        meta.lore(lore);
        rewardsPanel.setItemMeta(meta);
        inventory.setItem(49, rewardsPanel);
    }

    private void createNavigationButtons() {
        ItemStack filterButton = createButtonItem(Material.HOPPER, "<gold>⚙ Filtrar",
                "<gray>Filtrar misiones por tipo");
        inventory.setItem(1, filterButton);

        ItemStack sortButton = createButtonItem(Material.COMPARATOR, "<gold>🔄 Ordenar",
                "<gray>Ordenar misiones por dificultad");
        inventory.setItem(7, sortButton);

        ItemStack searchButton = createButtonItem(Material.COMPASS, "<gold>🔍 Buscar",
                "<gray>Buscar misiones específicas");
        inventory.setItem(46, searchButton);

        ItemStack closeButton = createButtonItem(Material.BARRIER, "<red>✕ Cerrar",
                "<gray>Cerrar menú de misiones");
        inventory.setItem(53, closeButton);
    }

    private ItemStack createButtonItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse(name));
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(MessageUtils.parse(line));
        }
        meta.lore(loreList);
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

    private int getCompletionPercentage(List<PlayerQuest> playerQuests) {
        if (playerQuests.isEmpty()) return 0;

        int totalRequirements = 0;
        int completedRequirements = 0;

        for (PlayerQuest playerQuest : playerQuests) {
            for (String requirement : playerQuest.getQuest().getRequirements().keySet()) {
                totalRequirements++;
                int required = playerQuest.getQuest().getRequirements().get(requirement);
                int progress = playerQuest.getProgress().getOrDefault(requirement, 0);

                if (progress >= required) {
                    completedRequirements++;
                }
            }
        }

        return totalRequirements > 0 ? (completedRequirements * 100) / totalRequirements : 0;
    }

    private String formatRequirementName(String requirement) {
        if (requirement.startsWith("MOB_")) {
            return "Matar " + requirement.substring(4).replace("_", " ");
        } else if (requirement.equals("FISH")) {
            return "Pescar";
        } else {
            return requirement.charAt(0) + requirement.substring(1).toLowerCase().replace("_", " ");
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void open(Player player) {
        QuestsGUI gui = new QuestsGUI(player);
        player.openInventory(gui.getInventory());
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();

        // Botón de cerrar
        if (slot == 53) {
            player.closeInventory();
            return;
        }

        // Botón de filtrar
        if (slot == 1) {
            filterQuests(player);
            return;
        }

        // Botón de ordenar
        if (slot == 7) {
            sortQuests(player);
            return;
        }

        // Botón de buscar
        if (slot == 46) {
            searchQuests(player);
            return;
        }

        // Panel de recompensas
        if (slot == 49) {
            showRewardsPanel(player);
            return;
        }

        // Slots de misiones disponibles
        int[] availableQuestSlots = {28, 29, 30, 31, 32, 33, 34};
        for (int questSlot : availableQuestSlots) {
            if (slot == questSlot) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    String questName = clickedItem.getItemMeta().getDisplayName();
                    // Extraer el ID de la misión (quitando el formato)
                    String questId = MessageUtils.stripColors(questName).replace("⚡ ", "").toLowerCase().replace(" ", "_");
                    // Aceptar la misión
                    MKSurvival.getInstance().getQuestManager().addQuest(player, questId);
                    MessageUtils.sendMessage(player, "<green>[Misiones] Has aceptado la misión: " + questName + "</green>");
                    // Actualizar el inventario
                    initializeItems();
                }
                return;
            }
        }

        // Slots de misiones activas
        int[] activeQuestSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 24};
        for (int questSlot : activeQuestSlots) {
            if (slot == questSlot) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    String questName = clickedItem.getItemMeta().getDisplayName();
                    MessageUtils.sendMessage(player, "<green>[Misiones] Has seleccionado la misión activa: " + questName + "</green>");
                }
                return;
            }
        }
    }

    // ==================== MÉTODOS DE FUNCIONALIDAD ESPECÍFICA ====================

    private void filterQuests(Player player) {
        MessageUtils.sendMessage(player, "<yellow>══════════════════════════════</yellow>");
        MessageUtils.sendMessage(player, "<yellow>🔍 Filtros de Misiones</yellow>");
        MessageUtils.sendMessage(player, "<gray>Escoge un filtro para aplicar:</gray>");
        MessageUtils.sendMessage(player, "<green>• /quest filter all</green> - Todas las misiones");
        MessageUtils.sendMessage(player, "<blue>• /quest filter available</blue> - Solo disponibles");
        MessageUtils.sendMessage(player, "<yellow>• /quest filter active</yellow> - Solo activas");
        MessageUtils.sendMessage(player, "<purple>• /quest filter completed</purple> - Solo completadas");
        MessageUtils.sendMessage(player, "<red>• /quest filter type <tipo></red> - Por tipo de misión");
        MessageUtils.sendMessage(player, "<gray>Tipos disponibles: kill, collect, craft, explore</gray>");
        MessageUtils.sendMessage(player, "<yellow>══════════════════════════════</yellow>");
        player.closeInventory();
    }
    
    private void sortQuests(Player player) {
        MessageUtils.sendMessage(player, "<yellow>══════════════════════════════</yellow>");
        MessageUtils.sendMessage(player, "<yellow>🔄 Ordenar Misiones</yellow>");
        MessageUtils.sendMessage(player, "<gray>Elige cómo ordenar las misiones:</gray>");
        MessageUtils.sendMessage(player, "<green>• /quest sort name</green> - Por nombre (A-Z)");
        MessageUtils.sendMessage(player, "<blue>• /quest sort difficulty</blue> - Por dificultad");
        MessageUtils.sendMessage(player, "<yellow>• /quest sort reward</yellow> - Por recompensa");
        MessageUtils.sendMessage(player, "<purple>• /quest sort progress</purple> - Por progreso");
        MessageUtils.sendMessage(player, "<red>• /quest sort time</red> - Por tiempo restante");
        MessageUtils.sendMessage(player, "<gray>Usa 'desc' al final para orden descendente</gray>");
        MessageUtils.sendMessage(player, "<gray>Ejemplo: /quest sort reward desc</gray>");
        MessageUtils.sendMessage(player, "<yellow>══════════════════════════════</yellow>");
        player.closeInventory();
    }
    
    private void searchQuests(Player player) {
        MessageUtils.sendMessage(player, "<yellow>══════════════════════════════</yellow>");
        MessageUtils.sendMessage(player, "<yellow>🔍 Buscar Misiones</yellow>");
        MessageUtils.sendMessage(player, "<gray>Busca misiones por palabra clave:</gray>");
        MessageUtils.sendMessage(player, "<green>• /quest search <palabra></green> - Buscar en nombres");
        MessageUtils.sendMessage(player, "<blue>• /quest search desc <palabra></blue> - Buscar en descripciones");
        MessageUtils.sendMessage(player, "<yellow>• /quest search reward <cantidad></yellow> - Por recompensa mínima");
        MessageUtils.sendMessage(player, "<purple>• /quest search npc <nombre></purple> - Por NPC");
        MessageUtils.sendMessage(player, "<red>• /quest search location <lugar></red> - Por ubicación");
        MessageUtils.sendMessage(player, "<gray>Ejemplos:</gray>");
        MessageUtils.sendMessage(player, "<gray>  /quest search dragon</gray>");
        MessageUtils.sendMessage(player, "<gray>  /quest search reward 1000</gray>");
        MessageUtils.sendMessage(player, "<yellow>══════════════════════════════</yellow>");
        player.closeInventory();
    }
    
    private void showRewardsPanel(Player player) {
        QuestManager questManager = MKSurvival.getInstance().getQuestManager();
        List<com.mk.mksurvival.managers.quests.PlayerQuest> activeQuests = questManager.getPlayerQuests(player);
        
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
        MessageUtils.sendMessage(player, "<gold>🎁 Panel de Recompensas</gold>");
        MessageUtils.sendMessage(player, "<gray>Recompensas de tus misiones activas:</gray>");
        MessageUtils.sendMessage(player, "");
        
        if (activeQuests.isEmpty()) {
            MessageUtils.sendMessage(player, "<red>• No tienes misiones activas</red>");
        } else {
            double totalMoney = 0;
            int totalExp = 0;
            int completedQuests = 0;
            
            for (com.mk.mksurvival.managers.quests.PlayerQuest playerQuest : activeQuests) {
                com.mk.mksurvival.managers.quests.Quest quest = playerQuest.getQuest();
                String status = playerQuest.isCompleted() ? "<green>✓ Completada</green>" : "<yellow>⏳ En progreso</yellow>";
                
                MessageUtils.sendMessage(player, "<yellow>• " + quest.getName() + "</yellow> " + status);
                MessageUtils.sendMessage(player, "  <gray>Dinero:</gray> <green>" + 
                    MKSurvival.getInstance().getEconomyManager().formatCurrency(quest.getMoneyReward()) + "</green>");
                MessageUtils.sendMessage(player, "  <gray>Experiencia:</gray> <aqua>" + quest.getExpReward() + " XP</aqua>");
                
                if (!quest.getItemRewards().isEmpty()) {
                    MessageUtils.sendMessage(player, "  <gray>Ítems:</gray> <purple>" + quest.getItemRewards().size() + " item(s)</purple>");
                }
                
                if (playerQuest.isCompleted()) {
                    totalMoney += quest.getMoneyReward();
                    totalExp += quest.getExpReward();
                    completedQuests++;
                }
                
                MessageUtils.sendMessage(player, "");
            }
            
            MessageUtils.sendMessage(player, "<gold>Resumen de recompensas completadas:</gold>");
            MessageUtils.sendMessage(player, "<green>• Misiones completadas: " + completedQuests + "/" + activeQuests.size() + "</green>");
            MessageUtils.sendMessage(player, "<green>• Dinero ganado: " + 
                MKSurvival.getInstance().getEconomyManager().formatCurrency(totalMoney) + "</green>");
            MessageUtils.sendMessage(player, "<green>• Experiencia ganada: " + totalExp + " XP</green>");
        }
        
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "<gray>Usa </gray><yellow>/quest complete</yellow><gray> para reclamar recompensas</gray>");
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
        player.closeInventory();
    }
}