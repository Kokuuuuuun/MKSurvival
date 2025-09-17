package com.mk.mksurvival.listeners.enemies;

import com.mk.mksurvival.MKSurvival;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener para eventos relacionados con el sistema de enemigos
 */
public class EnemySystemListener implements Listener {
    private final MKSurvival plugin;

    public EnemySystemListener(MKSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Mostrar barra de experiencia por defecto
        plugin.getExpBarManager().updatePlayerExpBar(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Limpiar datos del jugador de la memoria
        plugin.getExpBarManager().removePlayerExpBar(player);
    }
}