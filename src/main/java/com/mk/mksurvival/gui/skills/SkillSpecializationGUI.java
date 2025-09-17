package com.mk.mksurvival.gui.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SpecializationManager;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.managers.skills.PlayerSpecializations;
import com.mk.mksurvival.managers.skills.SkillSpecialization;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillSpecializationGUI {
    private final MKSurvival plugin;
    private final SpecializationManager specializationManager;

    public SkillSpecializationGUI(MKSurvival plugin) {
        this.plugin = plugin;
        this.specializationManager = plugin.getSpecializationManager();
    }

    public void openSpecializationMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Especializaciones de Habilidades");

        // Item para ver especializaciones de minería
        ItemStack miningItem = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta miningMeta = miningItem.getItemMeta();
        miningMeta.displayName(MessageUtils.parse("<yellow>Minería"));
        List<String> miningLore = new ArrayList<>();
        miningLore.add(MessageUtils.parse(("<gray>Ver especializaciones de minería.")));
        miningMeta.lore(MessageUtils.parseList(miningLore));
        miningItem.setItemMeta(miningMeta);
        inv.setItem(10, miningItem);

        // Item para ver especializaciones de combate
        ItemStack combatItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta combatMeta = combatItem.getItemMeta();
        combatMeta.displayName(MessageUtils.parse("<yellow>Combate"));
        List<String> combatLore = new ArrayList<>();
        combatLore.add(MessageUtils.parse(("<gray>Ver especializaciones de combate.")));
        combatMeta.lore(MessageUtils.parseList(combatLore));
        combatItem.setItemMeta(combatMeta);
        inv.setItem(12, combatItem);

        // Item para ver especializaciones de tala
        ItemStack woodcuttingItem = new ItemStack(Material.IRON_AXE);
        ItemMeta woodcuttingMeta = woodcuttingItem.getItemMeta();
        woodcuttingMeta.displayName(MessageUtils.parse("<yellow>Tala"));
        List<String> woodcuttingLore = new ArrayList<>();
        woodcuttingLore.add(MessageUtils.parse(("<gray>Ver especializaciones de tala.")));
        woodcuttingMeta.lore(MessageUtils.parseList(woodcuttingLore));
        woodcuttingItem.setItemMeta(woodcuttingMeta);
        inv.setItem(14, woodcuttingItem);

        // Item para ver especializaciones de pesca
        ItemStack fishingItem = new ItemStack(Material.FISHING_ROD);
        ItemMeta fishingMeta = fishingItem.getItemMeta();
        fishingMeta.displayName(MessageUtils.parse("<yellow>Pesca"));
        List<String> fishingLore = new ArrayList<>();
        fishingLore.add(MessageUtils.parse(("<gray>Ver especializaciones de pesca.")));
        fishingMeta.lore(MessageUtils.parseList(fishingLore));
        fishingItem.setItemMeta(fishingMeta);
        inv.setItem(16, fishingItem);

        player.openInventory(inv);
    }

    public void openSkillSpecializations(Player player, SkillType skillType) {
        PlayerSpecializations playerSpecs = specializationManager.getPlayerSpecializations(player);
        
        // Get specializations for this skill type
        List<SkillSpecialization> specs = new ArrayList<>();
        for (SkillSpecialization spec : specializationManager.getSpecializations().values()) {
            if (spec.getSkillType().equals(skillType)) {
                specs.add(spec);
            }
        }
        
        if (specs.isEmpty()) {
            MessageUtils.sendMessage(player, "<red>Esa habilidad no tiene especializaciones.");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, "Especializaciones: " + skillType.name());

        // Item de información
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.displayName(MessageUtils.parse("<yellow>Información"));
        List<String> infoLore = new ArrayList<>();
        infoLore.add(MessageUtils.parse(("<gray>Puntos disponibles: " + "0"))); // TODO: Implement points system
        infoLore.add(MessageUtils.parse(("<gray>Nivel de habilidad: " + plugin.getSkillManager().getPlayerSkills(player.getUniqueId()).getLevel(skillType))));
        infoMeta.lore(MessageUtils.parseList(infoLore));
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);

        // Items de especializaciones
        int slot = 10;
        for (SkillSpecialization spec : specs) {
            boolean unlocked = playerSpecs.hasSpecialization(spec.getId());
            int level = unlocked ? playerSpecs.getSpecializationLevel(spec.getId()) : 0;

            ItemStack specItem = new ItemStack(unlocked ? Material.ENCHANTED_BOOK : Material.BOOK);
            ItemMeta specMeta = specItem.getItemMeta();
            specMeta.displayName(MessageUtils.parse((unlocked ? "<green>" : "<gray>") + spec.getName() + " <gray>(Nivel " + level + "/" + spec.getMaxLevel() + ")"));

            List<String> specLore = new ArrayList<>();
            specLore.add(MessageUtils.parse(("<gray>" + spec.getDescription())));

            if (unlocked) {
                if (level < spec.getMaxLevel()) {
                    specLore.add(MessageUtils.parse(("<green>Click para mejorar.")));
                } else {
                    specLore.add(MessageUtils.parse(("<red>Esta especialización está al máximo nivel.")));
                }
            } else {
                specLore.add(MessageUtils.parse(("<green>Click para desbloquear.")));
            }

            specMeta.lore(MessageUtils.parseList(specLore));
            specItem.setItemMeta(specMeta);

            inv.setItem(slot, specItem);
            slot += 2;
        }

        player.openInventory(inv);
    }
}