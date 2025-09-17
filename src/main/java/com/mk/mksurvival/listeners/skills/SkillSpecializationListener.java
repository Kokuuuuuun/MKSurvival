package com.mk.mksurvival.listeners.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SpecializationManager;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.gui.skills.SkillSpecializationGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class SkillSpecializationListener implements Listener {
    private final MKSurvival plugin;
    private final SpecializationManager specializationManager;
    private final SkillSpecializationGUI specializationGUI;

    public SkillSpecializationListener(MKSurvival plugin) {
        this.plugin = plugin;
        this.specializationManager = plugin.getSpecializationManager();
        this.specializationGUI = new SkillSpecializationGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals("Especializaciones de Habilidades")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            SkillType skillType = null;
            switch (event.getCurrentItem().getType()) {
                case IRON_PICKAXE:
                    skillType = SkillType.MINING;
                    break;
                case IRON_SWORD:
                    skillType = SkillType.COMBAT;
                    break;
                case IRON_AXE:
                    skillType = SkillType.WOODCUTTING;
                    break;
                case FISHING_ROD:
                    skillType = SkillType.FISHING;
                    break;
            }

            if (skillType != null) {
                specializationGUI.openSkillSpecializations(player, skillType);
            }
        }

        else if (event.getView().getTitle().startsWith("Especializaciones: ")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            // Obtener el tipo de habilidad del título
            String skillTypeName = event.getView().getTitle().substring(16); // "Especializaciones: ".length()
            SkillType skillType = SkillType.valueOf(skillTypeName);

            // Obtener el ID de la especialización del nombre del item
            String specName = event.getCurrentItem().getItemMeta().getDisplayName();
            String specId = specName.substring(specName.lastIndexOf(" ") + 1);

            if (event.getCurrentItem().getType() == Material.BOOK) {
                // Desbloquear especialización
                specializationManager.unlockSpecialization(player, specId);
                player.closeInventory();
                specializationGUI.openSkillSpecializations(player, skillType);
            } else if (event.getCurrentItem().getType() == Material.ENCHANTED_BOOK) {
                // Mejorar especialización
                specializationManager.upgradeSpecialization(player, specId);
                player.closeInventory();
                specializationGUI.openSkillSpecializations(player, skillType);
            }
        }
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        // Otorgar puntos de especialización cada 5 niveles
        if (event.getNewLevel() > event.getOldLevel() && event.getNewLevel() % 5 == 0) {
            specializationManager.grantSpecializationPoint(event.getPlayer());
        }
    }
}