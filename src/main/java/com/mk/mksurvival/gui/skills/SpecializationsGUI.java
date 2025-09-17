package com.mk.mksurvival.gui.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SpecializationManager;
import com.mk.mksurvival.managers.skills.PlayerSpecialization;
import com.mk.mksurvival.managers.skills.PlayerSpecializations;
import com.mk.mksurvival.managers.skills.SkillSpecialization;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI para mostrar y gestionar las especializaciones de habilidades.
 */
public class SpecializationsGUI {
    private final MKSurvival plugin;
    private final Player player;
    private final SpecializationManager specializationManager;
    private final SkillType selectedSkillType;
    private Inventory inventory;
    
    /**
     * Constructor para crear una nueva GUI de especializaciones.
     *
     * @param plugin Instancia del plugin principal
     * @param player El jugador que verá la GUI
     * @param skillType El tipo de habilidad para mostrar sus especializaciones
     */
    public SpecializationsGUI(MKSurvival plugin, Player player, SkillType skillType) {
        this.plugin = plugin;
        this.player = player;
        this.specializationManager = plugin.getSpecializationManager();
        this.selectedSkillType = skillType;
        
        createInventory();
        fillInventory();
    }
    
    /**
     * Crea el inventario de la GUI.
     */
    private void createInventory() {
        String title = MessageUtils.parse("<" + selectedSkillType.getColor() + ">Especializaciones de " +
                selectedSkillType.getFormattedName());
        inventory = Bukkit.createInventory(null, 54, title);
    }
    
    /**
     * Llena el inventario con los ítems de especialización.
     */
    private void fillInventory() {
        // Obtener las especializaciones para el tipo de habilidad seleccionado
        List<SkillSpecialization> specializations = specializationManager.getSpecializationsForSkillType(selectedSkillType);
        PlayerSpecializations playerSpecs = specializationManager.getPlayerSpecializations(player);
        
        // Añadir las especializaciones al inventario
        int slot = 10;
        for (SkillSpecialization spec : specializations) {
            boolean unlocked = playerSpecs.hasSpecialization(spec.getId());
            int level = playerSpecs.getSpecializationLevel(spec.getId());
            
            ItemStack item = createSpecializationItem(spec, unlocked, level);
            inventory.setItem(slot, item);
            
            // Avanzar al siguiente slot, saltando los bordes
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
        }
        
        // Añadir botones de navegación
        inventory.setItem(49, createNavigationButton(Material.BARRIER, "<red>Cerrar", "Cierra esta ventana"));
        inventory.setItem(45, createNavigationButton(Material.ARROW, "<yellow>Volver", "Volver a la lista de habilidades"));
        
        // Añadir información del jugador
        inventory.setItem(4, createPlayerInfoItem());
    }
    
    /**
     * Crea un ítem para una especialización.
     *
     * @param spec La especialización
     * @param unlocked Si está desbloqueada
     * @param level El nivel actual
     * @return El ítem creado
     */
    private ItemStack createSpecializationItem(SkillSpecialization spec, boolean unlocked, int level) {
        Material material;
        if (unlocked) {
            material = Material.ENCHANTED_BOOK;
        } else {
            material = Material.BOOK;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Establecer nombre y lore
        meta.displayName(MessageUtils.parse(spec.getFormattedName()));
        List<String> lore = new ArrayList<>();
        
        // Descripción
        lore.add(spec.getFormattedDescription());
        lore.add("");
        
        if (unlocked) {
            // Información de nivel
            lore.add(MessageUtils.parse(("<green>Nivel: " + level + "/" + spec.getMaxLevel())));
            lore.add("");
            
            // Bonificación actual
            String currentBonus = spec.getFormattedBonusForLevel(level);
            lore.add(MessageUtils.parse(("<green>Bonificación actual:")));
            lore.add(currentBonus);
            lore.add("");
            
            // Siguiente bonificación si no está al máximo
            if (level < spec.getMaxLevel()) {
                String nextBonus = spec.getFormattedBonusForLevel(level + 1);
                int upgradeCost = spec.getUpgradeCost(level);
                
                lore.add(MessageUtils.parse(("<yellow>Siguiente bonificación:")));
                lore.add(nextBonus);
                lore.add("");
                lore.add(MessageUtils.parse(("<yellow>Costo de mejora: " + upgradeCost)));
                lore.add(MessageUtils.parse(("<gray>Clic para mejorar")));
            } else {
                lore.add(MessageUtils.parse(("<gold>¡Nivel máximo alcanzado!")));
            }
        } else {
            // Requisitos para desbloquear
            lore.add(MessageUtils.parse(("<red>Bloqueado")));
            lore.add("");
            lore.add(MessageUtils.parse(("<yellow>Requisitos para desbloquear:")));
            lore.add(MessageUtils.parse(("<gray>- Nivel " + spec.getRequiredSkillLevel() + " en " + 
                    spec.getSkillType().getFormattedName())));
            lore.add(MessageUtils.parse(("<gray>- Costo: " + spec.getUnlockCost())));
            lore.add("");
            lore.add(MessageUtils.parse(("<gray>Clic para desbloquear")));
        }
        
        meta.lore(MessageUtils.parseList(lore));
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Crea un botón de navegación.
     *
     * @param material El material del botón
     * @param name El nombre del botón
     * @param description La descripción del botón
     * @return El botón creado
     */
    private ItemStack createNavigationButton(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(MessageUtils.parse(name));
        
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>" + description)));
        meta.lore(MessageUtils.parseList(lore));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Crea un ítem con información del jugador.
     *
     * @return El ítem creado
     */
    private ItemStack createPlayerInfoItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(MessageUtils.parse("<gold>" + player.getName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<yellow>Habilidad: " + selectedSkillType.getFormattedName())));
        lore.add(MessageUtils.parse(("<yellow>Nivel: " + plugin.getSkillManager().getSkillLevel(player, selectedSkillType))));
        
        // Contar especializaciones desbloqueadas
        PlayerSpecializations playerSpecs = specializationManager.getPlayerSpecializations(player);
        int unlockedCount = 0;
        for (SkillSpecialization spec : specializationManager.getSpecializationsForSkillType(selectedSkillType)) {
            if (playerSpecs.hasSpecialization(spec.getId())) {
                unlockedCount++;
            }
        }
        
        lore.add(MessageUtils.parse(("<yellow>Especializaciones desbloqueadas: " + unlockedCount)));
        
        meta.lore(MessageUtils.parseList(lore));
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Abre la GUI para el jugador.
     */
    public void open() {
        player.openInventory(inventory);
    }
}