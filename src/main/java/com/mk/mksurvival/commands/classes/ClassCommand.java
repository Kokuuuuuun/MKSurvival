package com.mk.mksurvival.commands.classes;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.classes.ClassManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassCommand implements CommandExecutor, TabCompleter {
    private final MKSurvival plugin;
    private final ClassManager classManager;
    
    public ClassCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.classManager = plugin.getClassManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            com.mk.mksurvival.gui.classes.ClassesGUI.open(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "choose":
            case "select":
            case "elegir":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /class choose <id_clase>");
                    return true;
                }
                String classId = args[1];
                classManager.setPlayerClass(player, classId);
                return true;
                
            case "info":
            case "details":
            case "detalles":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /class info <id_clase>");
                    return true;
                }
                String targetClassId = args[1];
                showClassInfo(player, targetClassId);
                return true;
                
            case "current":
            case "actual":
                showCurrentClass(player);
                return true;
                
            case "list":
            case "lista":
                showAvailableClasses(player);
                return true;
                
            case "help":
            case "ayuda":
                showHelp(player);
                return true;
                
            default:
                MessageUtils.sendMessage(player, "<red>Comando desconocido. Usa /class help para ver los comandos disponibles.");
                return true;
        }
    }
    
    private void showClassInfo(Player player, String classId) {
        if (!classManager.getAvailableClasses().containsKey(classId)) {
            MessageUtils.sendMessage(player, "<red>Clase no encontrada. Usa /class list para ver las clases disponibles.");
            return;
        }
        
        ClassManager.GameClass rpgClass = classManager.getAvailableClasses().get(classId);
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<gold>✦ Información de Clase ✦");
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<yellow>Nombre: <white>" + rpgClass.getName());
        MessageUtils.sendMessage(player, "<yellow>ID: <white>" + classId);
        MessageUtils.sendMessage(player, "<yellow>Descripción: <gray>" + rpgClass.getDescription());
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void showCurrentClass(Player player) {
        ClassManager.PlayerClass currentClass = classManager.getPlayerClass(player);
        if (currentClass == null || currentClass.getGameClass() == null) {
            MessageUtils.sendMessage(player, "<yellow>No tienes ninguna clase seleccionada. Usa /class choose <id> para elegir una.");
            return;
        }
        
        ClassManager.GameClass targetClass = classManager.getAvailableClasses().get(currentClass.getGameClass().getId());
        if (targetClass == null) {
            MessageUtils.sendMessage(player, "<red>Tu clase actual no es válida. Contacta a un administrador.");
            return;
        }
        
        MessageUtils.sendMessage(player, "<yellow>Tu clase actual es: <gold>" + targetClass.getName() + " <gray>(" + targetClass.getId() + ")");
        MessageUtils.sendMessage(player, "<gray>" + targetClass.getDescription());
        MessageUtils.sendMessage(player, "<gray>" + targetClass.getDescription());
    }
    
    private void showAvailableClasses(Player player) {
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<gold>✨ Clases Disponibles ✨");
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        for (String classId : classManager.getAvailableClasses().keySet()) {
            ClassManager.GameClass rpgClass = classManager.getAvailableClasses().get(classId);
            MessageUtils.sendMessage(player, "<yellow>" + rpgClass.getName() + " <gray>(" + classId + ")");
            MessageUtils.sendMessage(player, "  <gray>" + rpgClass.getDescription());
        }
        
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<yellow>Usa /class info <id> para más detalles sobre una clase.");
    }
    
    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<gold>✦ Comandos de Clases ✦");
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<yellow>/class</yellow> <gray>- Abre el menú de clases");
        MessageUtils.sendMessage(player, "<yellow>/class list</yellow> <gray>- Muestra todas las clases disponibles");
        MessageUtils.sendMessage(player, "<yellow>/class info <id></yellow> <gray>- Muestra información de una clase");
        MessageUtils.sendMessage(player, "<yellow>/class choose <id></yellow> <gray>- Selecciona una clase");
        MessageUtils.sendMessage(player, "<yellow>/class current</yellow> <gray>- Muestra tu clase actual");
        MessageUtils.sendMessage(player, "<yellow>/class help</yellow> <gray>- Muestra esta ayuda");
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("choose", "info", "current", "list", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("choose") || args[0].equalsIgnoreCase("info"))) {
            return classManager.getAvailableClasses().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}