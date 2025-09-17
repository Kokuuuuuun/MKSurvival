package com.mk.mksurvival.listeners.quests;

import com.mk.mksurvival.MKSurvival;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class QuestListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check quest progress
        // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
        MKSurvival.getInstance().getQuestManager().checkQuestCompletion(
                player,
                event.getBlock().getType().name(),
                1
        );
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();

            // Check quest progress for mob kills
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MKSurvival.getInstance().getQuestManager().checkQuestCompletion(
                    player,
                    "MOB_" + event.getEntityType().name(),
                    1
            );
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();

            // Check quest progress for fishing
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MKSurvival.getInstance().getQuestManager().checkQuestCompletion(
                    player,
                    "FISH",
                    1
            );
        }
    }
}