package com.mk.mksurvival.commands.quests;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.quests.QuestsGUI;
import com.mk.mksurvival.managers.quests.QuestManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class QuestCommand implements CommandExecutor, TabCompleter {
    private final MKSurvival plugin;
    private QuestManager questManager;

    public QuestCommand(MKSurvival plugin) {
        this.plugin = plugin;
        // No inicializamos questManager aquí porque podría no estar disponible aún
    }

    // Método para inicializar el questManager después de que el plugin esté completamente cargado
    public void initialize() {
        this.questManager = plugin.getQuestManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }

        Player player = (Player) sender;

        // Asegurarse de que questManager esté inicializado
        if (questManager == null) {
            initialize();
        }

        if (args.length == 0) {
            // Abrir GUI de misiones si está disponible, si no mostrar lista de misiones activas
            try {
                QuestsGUI.open(player);
            } catch (Exception e) {
                questManager.showActiveQuests(player);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                questManager.showAvailableQuests(player);
                break;
            case "active":
                questManager.showActiveQuests(player);
                break;
            case "accept":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /quest accept <id></red>");
                    return true;
                }
                questManager.addQuest(player, args[1]);
                break;
            case "details":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /quest details <id></red>");
                    return true;
                }
                if (questManager.getAvailableQuests().containsKey(args[1])) {
                    questManager.showQuestDetails(player, questManager.getAvailableQuests().get(args[1]));
                } else {
                    MessageUtils.sendMessage(player, "<red>La misión no existe.</red>");
                }
                break;
            case "abandon":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /quest abandon <id> [confirm]</red>");
                    return true;
                }
                if (args.length >= 3 && args[2].equalsIgnoreCase("confirm")) {
                    confirmAbandonQuest(player, args[1]);
                } else {
                    abandonQuest(player, args[1]);
                }
                break;
            case "help":
                showHelp(player);
                break;
            default:
                MessageUtils.sendMessage(player, "<red>Comando desconocido. Usa /quest help para ver los comandos disponibles.</red>");
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "<gold>==== Comandos de Misiones ====</gold>");
        MessageUtils.sendMessage(player, "<yellow>/quest</yellow> - Abre el menú de misiones");
        MessageUtils.sendMessage(player, "<yellow>/quest list</yellow> - Muestra todas las misiones disponibles");
        MessageUtils.sendMessage(player, "<yellow>/quest active</yellow> - Muestra tus misiones activas");
        MessageUtils.sendMessage(player, "<yellow>/quest accept <id></yellow> - Acepta una misión");
        MessageUtils.sendMessage(player, "<yellow>/quest details <id></yellow> - Muestra los detalles de una misión");
        MessageUtils.sendMessage(player, "<yellow>/quest abandon <id></yellow> - Abandona una misión (próximamente)");
        MessageUtils.sendMessage(player, "<yellow>/quest help</yellow> - Muestra esta ayuda");
        MessageUtils.sendMessage(player, "<gold>========================</gold>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Asegurarse de que questManager esté inicializado
        if (questManager == null) {
            initialize();
        }

        if (args.length == 1) {
            String[] subcommands = {"list", "active", "accept", "details", "abandon", "help"};
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("details")) {
                for (String questId : questManager.getAvailableQuests().keySet()) {
                    if (questId.startsWith(args[1].toLowerCase())) {
                        completions.add(questId);
                    }
                }
            }
        }

        return completions;
    }

    private void abandonQuest(Player player, String questId) {
        List<com.mk.mksurvival.managers.quests.PlayerQuest> playerQuests = questManager.getPlayerQuests(player);
        
        // Buscar la misión activa
        com.mk.mksurvival.managers.quests.PlayerQuest questToAbandon = null;
        for (com.mk.mksurvival.managers.quests.PlayerQuest playerQuest : playerQuests) {
            if (playerQuest.getQuest().getId().equalsIgnoreCase(questId) && !playerQuest.isCompleted()) {
                questToAbandon = playerQuest;
                break;
            }
        }
        
        if (questToAbandon == null) {
            MessageUtils.sendMessage(player, "<red>[Misiones] No tienes esa misión activa o ya está completada.</red>");
            return;
        }
        
        // Confirmar abandono
        String questName = questToAbandon.getQuest().getName();
        MessageUtils.sendMessage(player, "<yellow>[Misiones] ¿Estás seguro de que quieres abandonar la misión '" + questName + "'?</yellow>");
        MessageUtils.sendMessage(player, "<red>Advertencia: Perderás todo el progreso de esta misión.</red>");
        MessageUtils.sendMessage(player, "<yellow>Para confirmar, escribe: /quest abandon " + questId + " confirm</yellow>");
    }
    
    private void confirmAbandonQuest(Player player, String questId) {
        List<com.mk.mksurvival.managers.quests.PlayerQuest> playerQuests = questManager.getPlayerQuests(player);
        
        // Buscar y remover la misión
        com.mk.mksurvival.managers.quests.PlayerQuest questToRemove = null;
        for (com.mk.mksurvival.managers.quests.PlayerQuest playerQuest : playerQuests) {
            if (playerQuest.getQuest().getId().equalsIgnoreCase(questId) && !playerQuest.isCompleted()) {
                questToRemove = playerQuest;
                break;
            }
        }
        
        if (questToRemove == null) {
            MessageUtils.sendMessage(player, "<red>[Misiones] No tienes esa misión activa o ya está completada.</red>");
            return;
        }
        
        // Remover la misión
        playerQuests.remove(questToRemove);
        String questName = questToRemove.getQuest().getName();
        
        MessageUtils.sendMessage(player, "<yellow>[Misiones] Has abandonado la misión '" + questName + "'.</yellow>");
        MessageUtils.sendMessage(player, "<gray>Todo el progreso de esta misión se ha perdido.</gray>");
        
        // Guardar cambios
        questManager.saveAllPlayerData();
    }
}