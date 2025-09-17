package com.mk.mksurvival.commands;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LandCommand implements CommandExecutor {

    public LandCommand(MKSurvival mkSurvival) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            com.mk.mksurvival.gui.land.LandGUI.openMainGUI(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim":
                MKSurvival.getInstance().getLandManager().claimSelection(player);
                break;
            case "unclaim":
                MKSurvival.getInstance().getLandManager().unclaimLand(player);
                break;
            case "info":
                MKSurvival.getInstance().getLandManager().showLandInfo(player);
                break;
            case "list":
                MKSurvival.getInstance().getLandManager().listLands(player);
                break;
            case "trust":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /land trust <jugador></red>");
                    return true;
                }
                MKSurvival.getInstance().getLandManager().trustPlayer(player, args[1]);
                break;
            case "untrust":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /land untrust <jugador></red>");
                    return true;
                }
                MKSurvival.getInstance().getLandManager().untrustPlayer(player, args[1]);
                break;
            case "selection":
                MKSurvival.getInstance().getLandSelectionManager().startSelection(player);
                break;
            case "setname":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /land setname <nombre></red>");
                    return true;
                }
                // Unir todos los argumentos restantes como el nombre
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i > 1) nameBuilder.append(" ");
                    nameBuilder.append(args[i]);
                }
                MKSurvival.getInstance().getLandManager().setLandName(player, nameBuilder.toString());
                break;
            case "removename":
                MKSurvival.getInstance().getLandManager().removeLandName(player);
                break;
            default:
                MessageUtils.sendMessage(player, "<red>Comando desconocido. Usa /land para ayuda.</red>");
        }
        return true;
    }
    
    private void sendHelpMessage(Player player) {
        MessageUtils.sendMessage(player, "<gold>=== Comandos de Tierras ===</gold>");
        MessageUtils.sendMessage(player, "<yellow>/land claim</yellow> <gray>- Reclama la selección actual</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land unclaim</yellow> <gray>- Elimina la reclamación donde estás</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land info</yellow> <gray>- Muestra información de la tierra</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land list</yellow> <gray>- Lista tus tierras reclamadas</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land trust <jugador></yellow> <gray>- Da acceso a un jugador</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land untrust <jugador></yellow> <gray>- Quita acceso a un jugador</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land selection</yellow> <gray>- Activa la herramienta de selección</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land setname <nombre></yellow> <gray>- Establece un nombre personalizado</gray>");
        MessageUtils.sendMessage(player, "<yellow>/land removename</yellow> <gray>- Elimina el nombre personalizado</gray>");
    }
}