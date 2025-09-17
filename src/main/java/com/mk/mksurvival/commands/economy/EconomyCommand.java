package com.mk.mksurvival.commands.economy;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconomyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            double balance = MKSurvival.getInstance().getEconomyManager().getBalance(player);
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MessageUtils.sendMessage(player, "<green>[Balance] Tu balance actual es: " +
                    MKSurvival.getInstance().getEconomyManager().formatCurrency(balance));
            return true;
        }

        if (args[0].equalsIgnoreCase("pay")) {
            if (args.length < 3) {
                MessageUtils.sendMessage(player, "<red>Uso: /balance pay <jugador> <cantidad></red>");
                return true;
            }

            Player target = player.getServer().getPlayer(args[1]);
            if (target == null) {
                MessageUtils.sendMessage(player, "<red>Jugador no encontrado.</red>");
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage(player, "<red>Cantidad inválida.</red>");
                return true;
            }

            if (amount <= 0) {
                MessageUtils.sendMessage(player, "<red>La cantidad debe ser mayor que 0.</red>");
                return true;
            }

            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            if (!MKSurvival.getInstance().getEconomyManager().hasBalance(player, amount)) {
                MessageUtils.sendMessage(player, "<red>No tienes suficiente dinero.</red>");
                return true;
            }

            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MKSurvival.getInstance().getEconomyManager().removeBalance(player, amount);
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MKSurvival.getInstance().getEconomyManager().addBalance(target, amount);

            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MessageUtils.sendMessage(player, "<green>Has enviado " + MKSurvival.getInstance().getEconomyManager().formatCurrency(amount) +
                    " a " + target.getName() + "</green>");
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            MessageUtils.sendMessage(target, "<green>Has recibido " + MKSurvival.getInstance().getEconomyManager().formatCurrency(amount) +
                    " de " + player.getName() + "</green>");

            return true;
        }

        MessageUtils.sendMessage(player, "<red>Comando desconocido. Usa /balance para ver tu balance.</red>");
        return true;
    }
}