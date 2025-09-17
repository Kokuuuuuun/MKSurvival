package com.mk.mksurvival.commands.experience;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.experience.ExpBarManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Comando para gestionar la barra de experiencia del jugador
 */
public class ExpBarCommand implements CommandExecutor, TabCompleter {
    private final MKSurvival plugin;

    public ExpBarCommand(MKSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "modo", "mode" -> handleModeCommand(player, args);
            case "toggle", "alternar" -> handleToggleCommand(player);
            case "animaciones", "animations" -> handleAnimationsCommand(player);
            case "actualizar", "update" -> handleUpdateCommand(player);
            case "ocultar", "hide" -> handleHideCommand(player);
            case "mostrar", "show" -> handleShowCommand(player);
            case "ayuda", "help" -> showHelp(player);
            default -> {
                MessageUtils.sendMessage(player, "<red>Subcomando no válido. Usa '/expbar help' para ver la ayuda.");
                return true;
            }
        }

        return true;
    }

    private void handleModeCommand(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendMessage(player, "<red>Uso: /expbar modo <modo>");
            MessageUtils.sendMessage(player, "<yellow>Modos disponibles: resumen, detallado, combate, progresion, compacto");
            return;
        }

        ExpBarManager.ExpBarMode mode = parseMode(args[1]);
        if (mode == null) {
            MessageUtils.sendMessage(player, "<red>Modo no válido. Modos disponibles:");
            MessageUtils.sendMessage(player, "<yellow>• resumen - Vista general de todas las habilidades");
            MessageUtils.sendMessage(player, "<yellow>• detallado - Vista detallada de todas las habilidades");
            MessageUtils.sendMessage(player, "<yellow>• combate - Enfoque en habilidades de combate");
            MessageUtils.sendMessage(player, "<yellow>• progresion - Muestra progreso hacia próximo nivel");
            MessageUtils.sendMessage(player, "<yellow>• compacto - Vista minimalista");
            return;
        }

        plugin.getExpBarManager().setPlayerMode(player, mode);
    }

    private void handleToggleCommand(Player player) {
        ExpBarManager.ExpBarMode currentMode = plugin.getExpBarManager().getPlayerMode(player);
        
        // Ciclar entre modos
        ExpBarManager.ExpBarMode nextMode = switch (currentMode) {
            case OVERVIEW -> ExpBarManager.ExpBarMode.DETAILED_SKILLS;
            case DETAILED_SKILLS -> ExpBarManager.ExpBarMode.COMBAT_FOCUS;
            case COMBAT_FOCUS -> ExpBarManager.ExpBarMode.PROGRESSION;
            case PROGRESSION -> ExpBarManager.ExpBarMode.COMPACT;
            case COMPACT -> ExpBarManager.ExpBarMode.OVERVIEW;
        };
        
        plugin.getExpBarManager().setPlayerMode(player, nextMode);
    }

    private void handleAnimationsCommand(Player player) {
        plugin.getExpBarManager().toggleAnimations(player);
    }

    private void handleUpdateCommand(Player player) {
        plugin.getExpBarManager().updatePlayerExpBar(player);
        MessageUtils.sendMessage(player, "<green>[ExpBar]</green> <yellow>Barra de experiencia actualizada.");
    }

    private void handleHideCommand(Player player) {
        plugin.getExpBarManager().removePlayerExpBar(player);
        MessageUtils.sendMessage(player, "<green>[ExpBar]</green> <yellow>Barra de experiencia ocultada.");
    }

    private void handleShowCommand(Player player) {
        plugin.getExpBarManager().updatePlayerExpBar(player);
        MessageUtils.sendMessage(player, "<green>[ExpBar]</green> <yellow>Barra de experiencia mostrada.");
    }

    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>╔═══════════════════════════════════════╗</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient>          <white><bold>BARRA DE EXPERIENCIA</bold></white>          <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>╠═══════════════════════════════════════╣</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <yellow>/expbar modo <modo></yellow> - Cambiar modo de vista <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <yellow>/expbar toggle</yellow> - Alternar entre modos      <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <yellow>/expbar animaciones</yellow> - Alternar animaciones <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <yellow>/expbar actualizar</yellow> - Actualizar barra      <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <yellow>/expbar ocultar</yellow> - Ocultar barra             <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <yellow>/expbar mostrar</yellow> - Mostrar barra             <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>╠═══════════════════════════════════════╣</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient>              <white>MODOS DISPONIBLES</white>               <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <green>resumen</green> - Vista general                    <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <blue>detallado</blue> - Todas las habilidades          <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <red>combate</red> - Enfoque en combate                <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <purple>progresion</purple> - Progreso hacia niveles       <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>║</gradient> <gray>compacto</gray> - Vista minimalista               <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(player, "<gradient:#FFD700:#FFA500>╚═══════════════════════════════════════╝</gradient>");
    }

    private ExpBarManager.ExpBarMode parseMode(String mode) {
        return switch (mode.toLowerCase()) {
            case "resumen", "overview", "general" -> ExpBarManager.ExpBarMode.OVERVIEW;
            case "detallado", "detailed", "skills" -> ExpBarManager.ExpBarMode.DETAILED_SKILLS;
            case "combate", "combat", "fight" -> ExpBarManager.ExpBarMode.COMBAT_FOCUS;
            case "progresion", "progression", "progress" -> ExpBarManager.ExpBarMode.PROGRESSION;
            case "compacto", "compact", "mini" -> ExpBarManager.ExpBarMode.COMPACT;
            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("modo", "toggle", "animaciones", "actualizar", "ocultar", "mostrar", "ayuda")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("modo")) {
            return Arrays.asList("resumen", "detallado", "combate", "progresion", "compacto")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return List.of();
    }
}