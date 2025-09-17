package com.mk.mksurvival.commands;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.items.ItemQualityManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemQualityCommand implements CommandExecutor {
    private final MKSurvival plugin;
    private final ItemQualityManager qualityManager;

    public ItemQualityCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.qualityManager = plugin.getItemQualityManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            MessageUtils.sendMessage(player, "<red>Debes tener un item en la mano.</red>");
            return true;
        }

        if (args.length == 0) {
            qualityManager.showQualityInfo(player, item);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "mejorar":
                qualityManager.upgradeItemQuality(player, item);
                break;

            case "quitar":
                qualityManager.removeQuality(item);
                MessageUtils.sendMessage(player, "<green>Has quitado la calidad del item.</green>");
                break;

            case "aplicar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /calidad aplicar <comun|raro|epico|legendario></red>");
                    return true;
                }

                try {
                    ItemQualityManager.ItemQuality quality = ItemQualityManager.ItemQuality.valueOf(args[1].toUpperCase());
                    qualityManager.applyQuality(item, quality);
                    MessageUtils.sendMessage(player, "<green>Has aplicado la calidad <yellow>" + quality.getDisplayName() + "</yellow> a tu item.</green>");
                } catch (IllegalArgumentException e) {
                    MessageUtils.sendMessage(player, "<red>Calidad inválida. Usa: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC, DIVINE o COSMIC.</red>");
                }
                break;

            default:
                MessageUtils.sendMessage(player, "<red>Comando desconocido.</red>");
                break;
        }

        return true;
    }
}