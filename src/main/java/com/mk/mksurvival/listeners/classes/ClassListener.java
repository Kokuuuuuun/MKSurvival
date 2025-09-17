package com.mk.mksurvival.listeners.classes;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.classes.ClassManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ClassListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Initialize player class if not exists
        // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
        MKSurvival.getInstance().getClassManager().getPlayerClass(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            ClassManager.PlayerClass playerClass = MKSurvival.getInstance().getClassManager().getPlayerClass(player);

            // Apply class-specific damage reduction
            if (playerClass.getGameClass().getId().equals("warrior")) {
                double reduction = 0.1 + (playerClass.getGameClass().getEffects().size() * 0.02);
                event.setDamage(event.getDamage() * (1 - reduction));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            ClassManager.PlayerClass playerClass = MKSurvival.getInstance().getClassManager().getPlayerClass(player);

            // Apply class-specific damage bonus
            if (playerClass.getGameClass().getId().equals("warrior")) {
                double bonus = 0.1 + (playerClass.getGameClass().getEffects().size() * 0.02);
                event.setDamage(event.getDamage() * (1 + bonus));
            }

            if (playerClass.getGameClass().getId().equals("archer")) {
                if (player.getInventory().getItemInMainHand().getType().name().contains("BOW")) {
                    double bonus = 0.2 + (playerClass.getGameClass().getEffects().size() * 0.03);
                    event.setDamage(event.getDamage() * (1 + bonus));
                }
            }
        }
    }
}