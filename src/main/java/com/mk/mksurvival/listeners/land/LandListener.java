package com.mk.mksurvival.listeners.land;
import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.land.LandManager;
import com.mk.mksurvival.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class LandListener implements Listener {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "<red>[Land] No puedes romper bloques en esta área.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "<red>[Land] No puedes colocar bloques en esta área.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && !canModify(event.getPlayer(), event.getClickedBlock())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "<red>[Land] No puedes interactuar con bloques en esta área.");
        }
    }

    private boolean canModify(Player player, Block block) {
        // Verificar si el jugador tiene permiso para construir en esta ubicación
        return MKSurvival.getInstance().getLandManager().canBuild(player, block.getLocation());
    }
}