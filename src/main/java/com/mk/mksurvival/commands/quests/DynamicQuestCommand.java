package com.mk.mksurvival.commands.quests;
import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.quests.DynamicQuestManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DynamicQuestCommand implements CommandExecutor {
    private final MKSurvival plugin;
    private final DynamicQuestManager questManager;

    public DynamicQuestCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.questManager = plugin.getDynamicQuestManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            questManager.showActiveQuests(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "generar":
                DynamicQuestManager.DynamicQuest quest = questManager.generateRandomQuest(player);
                if (quest != null) {
                    MessageUtils.sendMessage(player, "<green>Se ha generado una nueva misión:</green>");
                    MessageUtils.sendMessage(player, "<yellow>" + quest.getName() + "</yellow>");
                    MessageUtils.sendMessage(player, "<gray>" + quest.getDescription() + "</gray>");
                    MessageUtils.sendMessage(player, "<green>Usa <white>/mision aceptar " + quest.getId() + "</white> para aceptarla.</green>");
                }
                break;
            case "aceptar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /mision aceptar <id></red>");
                    return true;
                }
                questManager.acceptQuest(player, args[1]);
                break;
            case "abandonar":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /mision abandonar <id></red>");
                    return true;
                }
                questManager.abandonQuest(player, args[1]);
                break;
            case "completar":
                if (!player.hasPermission("mksurvival.admin")) {
                    MessageUtils.sendMessage(player, "<red>No tienes permiso para usar este comando.</red>");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /mision completar <id></red>");
                    return true;
                }
                questManager.completeQuest(player, args[1]);
                break;
            default:
                MessageUtils.sendMessage(player, "<red>Comando desconocido.</red>");
                break;
        }
        return true;
    }
}