package com.mk.mksurvival.commands;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            MessageUtils.sendMessage(player, "<green>=== MKSurvival ===</green>");
            MessageUtils.sendMessage(player, "<gray>/mksurvival reload - Recargar el plugin</gray>");
            MessageUtils.sendMessage(player, "<gray>/skills - Ver tus habilidades</gray>");
            MessageUtils.sendMessage(player, "<gray>/balance - Ver tu balance</gray>");
            MessageUtils.sendMessage(player, "<gray>/quests - Ver tus misiones</gray>");
            MessageUtils.sendMessage(player, "<gray>/class - Ver información de clase</gray>");
            MessageUtils.sendMessage(player, "<gray>/land - Gestionar tus tierras</gray>");
            MessageUtils.sendMessage(player, "<gray>/shop - Gestionar tiendas</gray>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("mksurvival.reload")) {
                MessageUtils.sendMessage(player, "<red>No tienes permiso para usar este comando.</red>");
                return true;
            }

            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MKSurvival.getInstance().getConfigManager().reloadConfigs();
            MessageUtils.sendMessage(player, "<green>Configuración de MKSurvival recargada.</green>");
            return true;
        }

        MessageUtils.sendMessage(player, "<red>Comando desconocido. Usa /mksurvival para ayuda.</red>");
        return true;
    }
}