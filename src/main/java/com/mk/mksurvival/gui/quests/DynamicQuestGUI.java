package com.mk.mksurvival.gui.quests;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.quests.DynamicQuestManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DynamicQuestGUI {
    private final MKSurvival plugin;
    private final DynamicQuestManager questManager;

    public DynamicQuestGUI(MKSurvival plugin) {
        this.plugin = plugin;
        this.questManager = plugin.getDynamicQuestManager();
    }

    public void openQuestMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Misiones Dinámicas");

        // Item para generar nueva misión
        ItemStack generateItem = new ItemStack(Material.EMERALD);
        ItemMeta generateMeta = generateItem.getItemMeta();
        generateMeta.displayName(MessageUtils.parse("<green>Generar Nueva Misión"));
        List<String> generateLore = new ArrayList<>();
        generateLore.add("<gray>Genera una misión aleatoria.");
        generateMeta.lore(MessageUtils.parseList(generateLore));
        generateItem.setItemMeta(generateMeta);
        inv.setItem(11, generateItem);

        // Item para ver misiones activas
        ItemStack activeItem = new ItemStack(Material.BOOK);
        ItemMeta activeMeta = activeItem.getItemMeta();
        activeMeta.displayName(MessageUtils.parse("<yellow>Misiones Activas"));
        List<String> activeLore = new ArrayList<>();
        activeLore.add("<gray>Ver tus misiones activas.");
        activeMeta.lore(MessageUtils.parseList(activeLore));
        activeItem.setItemMeta(activeMeta);
        inv.setItem(13, activeItem);

        // Item para ver plantillas de misiones
        ItemStack templatesItem = new ItemStack(Material.PAPER);
        ItemMeta templatesMeta = templatesItem.getItemMeta();
        templatesMeta.displayName(MessageUtils.parse("<aqua>Plantillas de Misiones"));
        List<String> templatesLore = new ArrayList<>();
        templatesLore.add("<gray>Ver todas las plantillas de misiones.");
        templatesMeta.lore(MessageUtils.parseList(templatesLore));
        templatesItem.setItemMeta(templatesMeta);
        inv.setItem(15, templatesItem);

        player.openInventory(inv);
    }

    public void openActiveQuests(Player player) {
        if (!questManager.getPlayerQuests().containsKey(player.getUniqueId()) ||
                questManager.getPlayerQuests().get(player.getUniqueId()).isEmpty()) {
            MessageUtils.sendMessage(player, "<red>No tienes misiones activas.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "Misiones Activas");

        int slot = 0;
        for (DynamicQuestManager.DynamicQuest quest : questManager.getPlayerQuests().get(player.getUniqueId())) {
            if (slot >= 54) break;

            ItemStack questItem = new ItemStack(Material.BOOK);
            ItemMeta questMeta = questItem.getItemMeta();
            questMeta.displayName(MessageUtils.parse("<yellow>" + quest.getName()));

            List<String> questLore = new ArrayList<>();
            questLore.add("<gray>" + quest.getDescription());
            questLore.add("<gray>Progreso: " + questManager.getQuestProgress(player, quest));
            questLore.add("<gray>ID: " + quest.getId());
            questLore.add("");
            questLore.add("<red>Click para abandonar.");

            questMeta.lore(MessageUtils.parseList(questLore));
            questItem.setItemMeta(questMeta);

            inv.setItem(slot, questItem);
            slot++;
        }

        player.openInventory(inv);
    }

    public void openQuestTemplates(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Plantillas de Misiones");

        int slot = 0;
        for (DynamicQuestManager.QuestTemplate template : questManager.getQuestTemplates()) {
            if (slot >= 54) break;

            ItemStack templateItem = new ItemStack(Material.PAPER);
            ItemMeta templateMeta = templateItem.getItemMeta();
            templateMeta.displayName(MessageUtils.parse("<yellow>" + template.getName()));

            List<String> templateLore = new ArrayList<>();
            templateLore.add("<gray>" + template.getDescription());
            templateLore.add("<gray>Tipo: " + template.getType().name());
            templateLore.add("<gray>Nivel: " + template.getLevel());
            templateLore.add("<gray>ID: " + template.getId());
            templateLore.add("");
            templateLore.add("<green>Click para generar una misión basada en esta plantilla.");

            templateMeta.lore(MessageUtils.parseList(templateLore));
            templateItem.setItemMeta(templateMeta);

            inv.setItem(slot, templateItem);
            slot++;
        }

        player.openInventory(inv);
    }
}