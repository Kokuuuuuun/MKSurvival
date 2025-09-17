package com.mk.mksurvival.commands;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.factions.FactionManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class FactionCommand implements CommandExecutor {
    private final MKSurvival plugin;
    private final FactionManager factionManager;

    public FactionCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.factionManager = plugin.getFactionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length == 0) {
            MessageUtils.sendMessage(player, "<red>Uso: /faccion <crear|disolver|unir|salir|invitar|expulsar|promover|aliar|enemistar|reclamar|abandonar|depositar|retirar|info|lista></red>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "crear":
                if (args.length < 3) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion crear <nombre> <descripción></red>");
                    return true;
                }
                String name = args[1];
                String description = String.join(" ", args).substring(name.length() + 7);
                factionManager.createFaction(playerId, name, description);
                break;

            case "disolver":
                factionManager.disbandFaction(playerId);
                break;

            case "unir":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion unir <nombre></red>");
                    return true;
                }
                factionManager.joinFaction(playerId, args[1]);
                break;

            case "salir":
                factionManager.leaveFaction(playerId);
                break;

            case "invitar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion invitar <jugador></red>");
                    return true;
                }
                Player invited = plugin.getServer().getPlayer(args[1]);
                if (invited == null) {
                    MessageUtils.sendMessage(player, "<red>Jugador no encontrado.</red>");
                    return true;
                }
                factionManager.invitePlayer(playerId, invited.getUniqueId());
                break;

            case "expulsar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion expulsar <jugador></red>");
                    return true;
                }
                Player kicked = plugin.getServer().getPlayer(args[1]);
                if (kicked == null) {
                    MessageUtils.sendMessage(player, "<red>Jugador no encontrado.</red>");
                    return true;
                }
                factionManager.kickPlayer(playerId, kicked.getUniqueId());
                break;

            case "promover":
                if (args.length < 3) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion promover <jugador> <rol></red>");
                    return true;
                }
                Player promoted = plugin.getServer().getPlayer(args[1]);
                if (promoted == null) {
                    MessageUtils.sendMessage(player, "<red>Jugador no encontrado.</red>");
                    return true;
                }
                factionManager.promotePlayer(playerId, promoted.getUniqueId(), args[2].toUpperCase());
                break;

            case "aliar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion aliar <facción></red>");
                    return true;
                }
                factionManager.factionAlly(factionManager.getPlayerFaction(playerId), args[1], true);
                break;

            case "enemistar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion enemistar <facción></red>");
                    return true;
                }
                factionManager.factionEnemy(factionManager.getPlayerFaction(playerId), args[1], true);
                break;

            case "reclamar":
                factionManager.claimLand(playerId);
                break;

            case "abandonar":
                factionManager.unclaimLand(playerId);
                break;

            case "depositar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion depositar <cantidad></red>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    factionManager.depositMoney(playerId, amount);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, "<red>Cantidad inválida.</red>");
                }
                break;

            case "retirar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /faccion retirar <cantidad></red>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    factionManager.withdrawMoney(playerId, amount);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, "<red>Cantidad inválida.</red>");
                }
                break;

            case "info":
                if (args.length < 2) {
                    if (factionManager.isPlayerInFaction(playerId)) {
                        factionManager.showFactionInfo(player, factionManager.getPlayerFaction(playerId));
                    } else {
                        MessageUtils.sendMessage(player, "<red>No eres miembro de ninguna facción. Usa /faccion info <nombre> para ver información de una facción.</red>");
                    }
                } else {
                    factionManager.showFactionInfo(player, args[1]);
                }
                break;

            case "lista":
                factionManager.showFactionList(player);
                break;

            default:
                MessageUtils.sendMessage(player, "<red>Comando desconocido.</red>");
                break;
        }
        return true;
    }
}