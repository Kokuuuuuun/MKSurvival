package com.mk.mksurvival.commands.dungeons;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.dungeons.DungeonManager;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class DungeonCommand implements CommandExecutor {
    private final MKSurvival plugin;

    public DungeonCommand(MKSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showDungeonList(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list" -> showDungeonList(player);
            case "join" -> {
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /dungeon join <dungeon_id></red>");
                    return true;
                }
                joinDungeon(player, args[1]);
            }
            case "leave" -> leaveDungeon(player);
            case "info" -> {
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /dungeon info <dungeon_id></red>");
                    return true;
                }
                showDungeonInfo(player, args[1]);
            }
            case "status" -> showPlayerStatus(player);
            default -> {
                MessageUtils.sendMessage(player, "<red>Subcomando no válido. Usa /dungeon para ver los comandos disponibles.</red>");
            }
        }
        
        return true;
    }

    private void showDungeonList(Player player) {
        Map<String, DungeonManager.DungeonTemplate> templates = plugin.getDungeonManager().getDungeonTemplates();
        
        MessageUtils.sendMessage(player, 
            "<gold><bold>=== MAZMORRAS DISPONIBLES ===</bold></gold>\n" +
            "<gray>Comandos:</gray>\n" +
            "<yellow>/dungeon join <id></yellow> <gray>- Unirse a una mazmorra</gray>\n" +
            "<yellow>/dungeon info <id></yellow> <gray>- Ver información detallada</gray>\n" +
            "<yellow>/dungeon leave</yellow> <gray>- Abandonar mazmorra actual</gray>\n" +
            "<yellow>/dungeon status</yellow> <gray>- Ver tu estado actual</gray>\n");
        
        if (templates.isEmpty()) {
            MessageUtils.sendMessage(player, "<red>No hay mazmorras disponibles actualmente.</red>");
            return;
        }
        
        for (DungeonManager.DungeonTemplate template : templates.values()) {
            String difficultyColor = getDifficultyColor(template.difficulty);
            MessageUtils.sendMessage(player, 
                "<yellow>• </yellow><white><bold>" + template.name + "</bold></white> <gray>(</gray><yellow>" + template.id + "</yellow><gray>)</gray>\n" +
                "  <gray>Nivel:</gray> <yellow>" + template.minLevel + "-" + template.maxLevel + "</yellow> " +
                "<gray>Dificultad:</gray> " + difficultyColor + template.difficulty.name() + "</color> " +
                "<gray>Jugadores:</gray> <yellow>" + template.maxPlayers + "</yellow>");
        }
    }

    private void joinDungeon(Player player, String dungeonId) {
        boolean success = plugin.getDungeonManager().joinDungeon(player, dungeonId);
        if (!success) {
            MessageUtils.sendMessage(player, "<red>No se pudo unir a la mazmorra. Verifica los requisitos.</red>");
        }
    }

    private void leaveDungeon(Player player) {
        plugin.getDungeonManager().leaveDungeon(player);
    }

    private void showDungeonInfo(Player player, String dungeonId) {
        Map<String, DungeonManager.DungeonTemplate> templates = plugin.getDungeonManager().getDungeonTemplates();
        DungeonManager.DungeonTemplate template = templates.get(dungeonId);
        
        if (template == null) {
            MessageUtils.sendMessage(player, "<red>Mazmorra no encontrada. Usa /dungeon list para ver las disponibles.</red>");
            return;
        }
        
        // Check player level
        com.mk.mksurvival.managers.skills.PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
        int combatLevel = skills.getLevel(com.mk.mksurvival.managers.skills.SkillType.COMBAT);
        
        String levelStatus;
        if (combatLevel < template.minLevel) {
            levelStatus = "<red>✗ Nivel muy bajo (necesitas " + template.minLevel + ", tienes " + combatLevel + ")</red>";
        } else if (combatLevel > template.maxLevel) {
            levelStatus = "<red>✗ Nivel muy alto (máximo " + template.maxLevel + ", tienes " + combatLevel + ")</red>";
        } else {
            levelStatus = "<green>✓ Nivel adecuado (" + combatLevel + ")</green>";
        }
        
        String difficultyColor = getDifficultyColor(template.difficulty);
        
        MessageUtils.sendMessage(player, 
            "<gold><bold>=== " + template.name.toUpperCase() + " ===</bold></gold>\n" +
            "<gray>ID:</gray> <yellow>" + template.id + "</yellow>\n" +
            "<gray>Descripción:</gray> <white>" + template.description + "</white>\n" +
            "<gray>Nivel requerido:</gray> <yellow>" + template.minLevel + "-" + template.maxLevel + "</yellow>\n" +
            "<gray>Dificultad:</gray> " + difficultyColor + template.difficulty.name() + "</color>\n" +
            "<gray>Tiempo límite:</gray> <yellow>" + (template.timeLimit / 60) + " minutos</yellow>\n" +
            "<gray>Máx jugadores:</gray> <yellow>" + template.maxPlayers + "</yellow>\n" +
            "<gray>Oleadas:</gray> <yellow>" + template.waves.size() + "</yellow>\n" +
            "<gray>Recompensa dinero:</gray> <yellow>" + plugin.getEconomyManager().formatCurrency(template.moneyReward) + "</yellow>\n" +
            "<gray>Experiencia:</gray> <yellow>" + template.experienceReward + " XP</yellow>\n\n" +
            levelStatus + "\n\n" +
            "<gray>Usa</gray> <yellow>/dungeon join " + template.id + "</yellow> <gray>para unirte</gray>");
    }

    private void showPlayerStatus(Player player) {
        DungeonManager.DungeonInstance instance = plugin.getDungeonManager().getPlayerInstance(player.getUniqueId());
        
        if (instance == null) {
            MessageUtils.sendMessage(player, "<gray>No estás en ninguna mazmorra actualmente.</gray>");
            return;
        }
        
        long elapsed = System.currentTimeMillis() - instance.startTime;
        long remaining = (instance.template.timeLimit * 1000L) - elapsed;
        
        MessageUtils.sendMessage(player, 
            "<gold><bold>=== ESTADO DE MAZMORRA ===</bold></gold>\n" +
            "<gray>Mazmorra:</gray> <yellow>" + instance.template.name + "</yellow>\n" +
            "<gray>Oleada actual:</gray> <yellow>" + instance.currentWave + "/" + instance.template.waves.size() + "</yellow>\n" +
            "<gray>Jugadores:</gray> <yellow>" + instance.players.size() + "/" + instance.template.maxPlayers + "</yellow>\n" +
            "<gray>Tiempo restante:</gray> <yellow>" + (remaining / 1000 / 60) + ":" + String.format("%02d", (remaining / 1000) % 60) + "</yellow>\n" +
            "<gray>Enemigos activos:</gray> <yellow>" + instance.activeMobs.size() + "</yellow>");
    }

    private String getDifficultyColor(DungeonManager.DungeonDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "<green>";
            case MEDIUM -> "<yellow>";
            case HARD -> "<red>";
            case NIGHTMARE -> "<dark_purple>";
        };
    }
}