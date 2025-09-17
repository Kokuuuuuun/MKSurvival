package com.mk.mksurvival.listeners.menu;

import com.mk.mksurvival.gui.classes.ClassesGUI;
import com.mk.mksurvival.gui.land.*;
import com.mk.mksurvival.gui.quests.QuestsGUI;
import com.mk.mksurvival.gui.skills.SkillsGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Si el inventario clickeado no tiene holder, salir
        if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() == null) {
            return;
        }

        InventoryHolder holder = event.getClickedInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        // Solo cancelar si es uno de nuestros menús
        if (holder instanceof ClassesGUI || holder instanceof LandGUI ||
                holder instanceof QuestsGUI || holder instanceof SkillsGUI) {
            event.setCancelled(true);

            // Manejar clic según el tipo de menú
            if (holder instanceof ClassesGUI) {
                ((ClassesGUI) holder).handleClick(event, player);
            } else if (holder instanceof LandGUI) {
                ((LandGUI) holder).handleClick(event, player);
            } else if (holder instanceof QuestsGUI) {
                ((QuestsGUI) holder).handleClick(event, player);
            } else if  (holder instanceof SkillsGUI) {
                ((SkillsGUI) holder).handleClick(event, player);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Si el inventario arrastrado no tiene holder, salir
        if (event.getInventory().getHolder() == null) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();

        // Solo cancelar si es uno de nuestros menús
        if (holder instanceof ClassesGUI || holder instanceof LandGUI ||
                holder instanceof QuestsGUI || holder instanceof SkillsGUI) {
            event.setCancelled(true);
        }
    }
}