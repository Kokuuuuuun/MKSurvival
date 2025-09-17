package com.mk.mksurvival.listeners.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.skills.SkillsGUI;
import com.mk.mksurvival.gui.skills.SpecializationsGUI;
import com.mk.mksurvival.managers.skills.SpecializationManager;
import com.mk.mksurvival.managers.skills.PlayerSpecializations;
import com.mk.mksurvival.managers.skills.SkillSpecialization;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Manejador de eventos para la GUI de especializaciones.
 */
public class SpecializationGUIListener implements Listener {
    private final MKSurvival plugin;
    
    /**
     * Constructor para crear un nuevo manejador de eventos.
     *
     * @param plugin Instancia del plugin principal
     */
    public SpecializationGUIListener(MKSurvival plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Maneja los clics en la GUI de especializaciones.
     *
     * @param event El evento de clic en inventario
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // Verificar si es la GUI de especializaciones
        if (!title.contains("Especializaciones de")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // Determinar el tipo de habilidad a partir del título
        SkillType skillType = null;
        for (SkillType type : SkillType.values()) {
            if (title.contains(type.getDisplayName())) {
                skillType = type;
                break;
            }
        }
        
        if (skillType == null) {
            return;
        }
        
        // Manejar botones de navegación
        if (event.getSlot() == 49 && clickedItem.getType() == Material.BARRIER) {
            // Cerrar inventario
            player.closeInventory();
            return;
        } else if (event.getSlot() == 45 && clickedItem.getType() == Material.ARROW) {
            // Volver a la lista de habilidades
            player.closeInventory();
            new SkillsGUI(plugin, player).open();
            return;
        }
        
        // Manejar clic en especialización
        if (clickedItem.getType() == Material.BOOK || clickedItem.getType() == Material.ENCHANTED_BOOK) {
            handleSpecializationClick(player, clickedItem, skillType);
        }
    }
    
    /**
     * Maneja el clic en un ítem de especialización.
     *
     * @param player El jugador
     * @param clickedItem El ítem en el que se hizo clic
     * @param skillType El tipo de habilidad
     */
    private void handleSpecializationClick(Player player, ItemStack clickedItem, SkillType skillType) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String skillName = skillType.getDisplayName();
        SpecializationManager specializationManager = plugin.getSpecializationManager();
        
        // Buscar la especialización por su nombre
        SkillSpecialization clickedSpec = null;
        for (SkillSpecialization spec : specializationManager.getSpecializationsForSkillType(skillType)) {
            if (meta.getDisplayName().contains(spec.getName())) {
                clickedSpec = spec;
                break;
            }
        }
        
        if (clickedSpec == null) {
            return;
        }
        
        PlayerSpecializations playerSpecs = specializationManager.getPlayerSpecializations(player);
        boolean unlocked = playerSpecs.hasSpecialization(clickedSpec.getId());
        
        if (unlocked) {
            // Intentar mejorar la especialización
            int currentLevel = playerSpecs.getSpecializationLevel(clickedSpec.getId());
            if (currentLevel < clickedSpec.getMaxLevel()) {
                specializationManager.upgradeSpecialization(player, clickedSpec.getId());
            } else {
                MessageUtils.sendMessage(player, "<red>Ya has alcanzado el nivel máximo en esta especialización.");
            }
        } else {
            // Intentar desbloquear la especialización
            specializationManager.unlockSpecialization(player, clickedSpec.getId());
        }
        
        // Actualizar la GUI
        player.closeInventory();
        new SpecializationsGUI(plugin, player, skillType).open();
    }
}