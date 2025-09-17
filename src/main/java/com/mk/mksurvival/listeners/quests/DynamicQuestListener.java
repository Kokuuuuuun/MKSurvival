package com.mk.mksurvival.listeners.quests;
import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.quests.DynamicQuestGUI;
import com.mk.mksurvival.managers.quests.DynamicQuestManager;
import org.bukkit.entity.Player;
import com.mk.mksurvival.utils.MessageUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import java.util.List;

public class DynamicQuestListener implements Listener {
    private final MKSurvival plugin;
    private final DynamicQuestManager questManager;
    private final DynamicQuestGUI questGUI;

    public DynamicQuestListener(MKSurvival plugin) {
        this.plugin = plugin;
        this.questManager = plugin.getDynamicQuestManager();
        this.questGUI = new DynamicQuestGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Misiones Dinámicas")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            switch (event.getCurrentItem().getType()) {
                case EMERALD:
                    // Generar nueva misión
                    DynamicQuestManager.DynamicQuest quest = questManager.generateRandomQuest(player);
                    if (quest != null) {
                        MessageUtils.sendMessage(player, "<green>Se ha generado una nueva misión:");
                        MessageUtils.sendMessage(player, "<yellow>" + quest.getName());
                        MessageUtils.sendMessage(player, "<gray>" + quest.getDescription());
                        MessageUtils.sendMessage(player, "<green>Usa <white>/mision aceptar " + quest.getId() + " <green>para aceptarla.");
                    }
                    break;
                case BOOK:
                    // Ver misiones activas
                    questGUI.openActiveQuests(player);
                    break;
                case PAPER:
                    // Ver plantillas de misiones
                    questGUI.openQuestTemplates(player);
                    break;
            }
        }
        else if (event.getView().getTitle().equals("Misiones Activas")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            // Abandonar misión
            if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasLore()) {
                List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                // Buscar el ID en el lore
                for (String line : lore) {
                    if (line.startsWith("<gray>ID: ")) {
                        String questId = line.substring(10);
                        questManager.abandonQuest(player, questId);
                        player.closeInventory();
                        break;
                    }
                }
            }
        }
        else if (event.getView().getTitle().equals("Plantillas de Misiones")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            // Generar misión basada en plantilla
            if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasLore()) {
                List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                // Buscar el ID en el lore
                for (String line : lore) {
                    if (line.startsWith("<gray>ID: ")) {
                        String templateId = line.substring(10);
                        // Encontrar la plantilla y generar una misión basada en ella
                        for (DynamicQuestManager.QuestTemplate template : questManager.getQuestTemplates()) {
                            if (template.getId().equals(templateId)) {
                                DynamicQuestManager.DynamicQuest quest = new DynamicQuestManager.DynamicQuest(
                                        java.util.UUID.randomUUID().toString(),
                                        template.getName(),
                                        template.getDescription(),
                                        template.getType(),
                                        template.getLevel(),
                                        new java.util.HashMap<>(template.getParameters()),
                                        template.getReward()
                                );
                                questManager.customizeQuestParameters(quest);
                                MessageUtils.sendMessage(player, "<green>Se ha generado una nueva misión:");
                                MessageUtils.sendMessage(player, "<yellow>" + quest.getName());
                                MessageUtils.sendMessage(player, "<gray>" + quest.getDescription());
                                MessageUtils.sendMessage(player, "<green>Usa <white>/mision aceptar " + quest.getId() + " <green>para aceptarla.");
                                player.closeInventory();
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Interacción con NPCs para obtener misiones
        if (event.getRightClicked() instanceof org.bukkit.entity.Villager) {
            DynamicQuestManager.DynamicQuest quest = questManager.generateRandomQuest(event.getPlayer());
            if (quest != null) {
                MessageUtils.sendMessage(event.getPlayer(), "<yellow>El aldeano te ofrece una misión:");
                MessageUtils.sendMessage(event.getPlayer(), "<yellow>" + quest.getName());
                MessageUtils.sendMessage(event.getPlayer(), "<gray>" + quest.getDescription());
                MessageUtils.sendMessage(event.getPlayer(), "<green>Usa <white>/mision aceptar " + quest.getId() + " <green>para aceptarla.");
            }
        }
    }
}