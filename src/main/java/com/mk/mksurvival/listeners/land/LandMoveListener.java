package com.mk.mksurvival.listeners.land;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.land.LandManager;
import com.mk.mksurvival.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class LandMoveListener implements Listener {
    private final MKSurvival plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    public LandMoveListener(MKSurvival plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Solo verificar cuando el jugador cambia de bloque (para optimizar)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        LandManager landManager = plugin.getLandManager();
        
        // Verificar si el jugador está entrando a un terreno
        LandManager.LandClaim fromClaim = landManager.getLandAt(event.getFrom());
        LandManager.LandClaim toClaim = landManager.getLandAt(event.getTo());
        
        // Si no hay cambio de terreno, no hacer nada
        if (fromClaim == toClaim) {
            return;
        }
        
        // Si está entrando a un terreno
        if (toClaim != null && (fromClaim == null || !fromClaim.getId().equals(toClaim.getId()))) {
            // Obtener el nombre del dueño
            String ownerName = plugin.getServer().getOfflinePlayer(toClaim.getOwnerId()).getName();
            
            // Si el jugador no es el dueño, mostrar mensaje
            if (!toClaim.getOwnerId().equals(player.getUniqueId())) {
                String message = plugin.getConfigManager().getLandConfig().getString("messages.enter_claim", "<yellow>Has entrado a la tierra de {owner}</yellow>");
                message = message.replace("{owner}", ownerName);
                MessageUtils.sendMessage(player, message);
            } else {
                MessageUtils.sendMessage(player, "<green>Has entrado a tu tierra.</green>");
            }
        } 
        // Si está saliendo de un terreno
        else if (fromClaim != null && toClaim == null) {
            String ownerName = plugin.getServer().getOfflinePlayer(fromClaim.getOwnerId()).getName();
            
            if (!fromClaim.getOwnerId().equals(player.getUniqueId())) {
                String message = plugin.getConfigManager().getLandConfig().getString("messages.exit_claim", "<yellow>Has salido de la tierra de {owner}</yellow>");
                message = message.replace("{owner}", ownerName);
                MessageUtils.sendMessage(player, message);
            } else {
                MessageUtils.sendMessage(player, "<green>Has salido de tu tierra.</green>");
            }
        }
    }
}