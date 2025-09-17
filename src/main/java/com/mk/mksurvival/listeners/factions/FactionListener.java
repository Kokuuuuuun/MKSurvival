package com.mk.mksurvival.listeners.factions;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.factions.FactionGUI;
import com.mk.mksurvival.managers.factions.FactionManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class FactionListener implements Listener {
    private final MKSurvival plugin;
    private final FactionManager factionManager;

    public FactionListener(MKSurvival plugin) {
        this.plugin = plugin;
        this.factionManager = plugin.getFactionManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Cargar datos del jugador si es necesario
        // Esto ya se maneja en el manager
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Guardar datos del jugador si es necesario
        // Esto ya se maneja en el manager
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Menú de Facciones")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            switch (event.getCurrentItem().getType()) {
                case BEACON:
                    // Abrir GUI para crear facción
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "<yellow>Usa el comando: /faccion crear <nombre> <descripción>");
                    break;
                case BOOK:
                    // Abrir lista de facciones
                    FactionGUI.openListGUI(player);
                    break;
                case PAPER:
                    // Abrir información de la facción del jugador
                    if (factionManager.isPlayerInFaction(player.getUniqueId())) {
                        String factionName = factionManager.getPlayerFaction(player.getUniqueId());
                        FactionGUI.openManageGUI(player, factionName);
                    }
                    break;
            }
        }
        else if (event.getView().getTitle().equals("Lista de Facciones")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            if (Material.WHITE_BANNER == event.getCurrentItem().getType()) {
                String factionName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2); // Eliminar color
                FactionGUI.openManageGUI(player, factionName);
            }
        }
        else if (event.getView().getTitle().startsWith("Facción: ")) {
            event.setCancelled(true);
            // No se necesita acción adicional, solo mostrar información
        }
        else if (event.getView().getTitle().startsWith("Gestionar Facción: ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            String factionName = event.getView().getTitle().substring(17); // "Gestionar Facción: ".length()
            switch (event.getCurrentItem().getType()) {
                case WRITTEN_BOOK:
                    // Invitar jugador
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "<yellow>Usa el comando: /faccion invitar <jugador>");
                    break;
                case GRASS_BLOCK:
                    // Reclamar tierra
                    player.closeInventory();
                    factionManager.claimLand(player.getUniqueId());
                    break;
                case PLAYER_HEAD:
                    // Ver miembros
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "<yellow>Usa el comando: /faccion info " + factionName);
                    break;
                case MAP:
                    // Ver relaciones
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "<yellow>Usa el comando: /faccion info " + factionName);
                    break;
                case GOLD_INGOT:
                    // Banco de facción
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "<yellow>Usa el comando: /faccion depositar <cantidad> o /faccion retirar <cantidad>");
                    break;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        String owner = factionManager.getChunkOwner(chunk);
        if (owner != null) {
            String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
            if (playerFaction == null || !playerFaction.equals(owner)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, "<red>No puedes romper bloques en territorio de la facción <yellow>" + owner + "<red>.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        String owner = factionManager.getChunkOwner(chunk);
        if (owner != null) {
            String playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
            if (playerFaction == null || !playerFaction.equals(owner)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, "<red>No puedes colocar bloques en territorio de la facción <yellow>" + owner + "<red>.");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        UUID victimId = victim.getUniqueId();
        UUID attackerId = attacker.getUniqueId();

        String victimFaction = factionManager.getPlayerFaction(victimId);
        String attackerFaction = factionManager.getPlayerFaction(attackerId);

        if (victimFaction != null && attackerFaction != null) {
            if (victimFaction.equals(attackerFaction)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(attacker, "<red>No puedes dañar a miembros de tu facción.");
            } else if (factionManager.areAllies(victimFaction, attackerFaction)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(attacker, "<red>No puedes dañar a miembros de facciones aliadas.");
            } else if (factionManager.areEnemies(victimFaction, attackerFaction)) {
                // Daño adicional contra enemigos
                event.setDamage(event.getDamage() * 1.2);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            UUID killerId = killer.getUniqueId();
            UUID victimId = victim.getUniqueId();

            String killerFaction = factionManager.getPlayerFaction(killerId);
            String victimFaction = factionManager.getPlayerFaction(victimId);

            if (killerFaction != null && victimFaction != null && factionManager.areEnemies(killerFaction, victimFaction)) {
                // Recompensa por matar a un enemigo
                plugin.getEconomyManager().depositBalance(killer, 50.0);
                MessageUtils.sendMessage(killer, "<green>Has recibido <yellow>50.0 <green>por matar a un enemigo de facción.");

                // Notificar a la facción
                for (UUID memberUuid : factionManager.getFactions().get(killerFaction).getMembers().keySet()) {
                    Player member = plugin.getServer().getPlayer(memberUuid);
                    if (member != null) {
                        MessageUtils.sendMessage(member, "<yellow>" + killer.getName() + " <green>ha matado a <yellow>" + victim.getName() + " <red>(enemigo)<green>.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Notificar cuando un jugador entra en territorio de una facción
        if (event.getFrom().getChunk() != event.getTo().getChunk()) {
            Chunk fromChunk = event.getFrom().getChunk();
            Chunk toChunk = event.getTo().getChunk();
            String fromOwner = factionManager.getChunkOwner(fromChunk);
            String toOwner = factionManager.getChunkOwner(toChunk);

            if (fromOwner != toOwner) {
                Player player = event.getPlayer();
                UUID playerId = player.getUniqueId();
                String playerFaction = factionManager.getPlayerFaction(playerId);

                if (toOwner == null) {
                    MessageUtils.sendMessage(player, "<yellow>Has entrado en territorio salvaje.");
                } else if (playerFaction != null && playerFaction.equals(toOwner)) {
                    MessageUtils.sendMessage(player, "<green>Has entrado en territorio de tu facción.");
                } else if (playerFaction != null && factionManager.areAllies(playerFaction, toOwner)) {
                    MessageUtils.sendMessage(player, "<aqua>Has entrado en territorio aliado.");
                } else if (playerFaction != null && factionManager.areEnemies(playerFaction, toOwner)) {
                    MessageUtils.sendMessage(player, "<red>¡Cuidado! Has entrado en territorio enemigo.");
                } else {
                    MessageUtils.sendMessage(player, "<yellow>Has entrado en territorio de la facción <yellow>" + toOwner + "<yellow>.");
                }
            }
        }
    }
}