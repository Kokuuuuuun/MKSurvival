package com.mk.mksurvival.listeners.economy;

import com.mk.mksurvival.MKSurvival;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.mk.mksurvival.utils.MessageUtils;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Initialize player balance if not exists
        // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
        MKSurvival.getInstance().getEconomyManager().getBalance(player);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();

            // Give money for killing mobs
            double reward = getMobReward(event.getEntityType());
            if (reward > 0) {
                // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
                MKSurvival.getInstance().getEconomyManager().addBalance(player, reward);
                // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
                MessageUtils.sendMessage(player, "<yellow>[Economy] Has recibido " +
                        MKSurvival.getInstance().getEconomyManager().formatCurrency(reward) +
                        " por matar a " + event.getEntityType());
            }
        }
    }

    private double getMobReward(org.bukkit.entity.EntityType entityType) {
        switch (entityType) {
            case ZOMBIE: return 2.0;
            case SKELETON: return 2.0;
            case SPIDER: return 1.5;
            case CREEPER: return 3.0;
            case ENDERMAN: return 5.0;
            case WITCH: return 4.0;
            case CAVE_SPIDER: return 2.5;
            case SILVERFISH: return 1.0;
            default: return 0.0;
        }
    }
}