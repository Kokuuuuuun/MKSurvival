package com.mk.mksurvival.listeners.experience;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

/**
 * Listener para eventos relacionados con el sistema de experiencia
 */
public class ExperienceListener implements Listener {
    private final MKSurvival plugin;

    public ExperienceListener(MKSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Actualizar barra de experiencia al unirse
        plugin.getExpBarManager().updatePlayerExpBar(player);
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        
        // Actualizar barra cuando cambie la experiencia vanilla
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getExpBarManager().updatePlayerExpBar(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        
        // Actualizar barra cuando cambie el nivel vanilla
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getExpBarManager().updatePlayerExpBar(player);
        }, 1L);
    }
}