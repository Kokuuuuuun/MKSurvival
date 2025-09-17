package com.mk.mksurvival.commands.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.skills.SkillsGUI;
import com.mk.mksurvival.gui.skills.SpecializationsGUI;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.skills.SpecializationManager;
import com.mk.mksurvival.managers.skills.SkillType;
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

public class SkillsCommand implements CommandExecutor, TabCompleter {
    private final SkillManager skillManager;
    private final SpecializationManager specializationManager;
    private final MKSurvival plugin;
    
    public SkillsCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.skillManager = plugin.getSkillManager();
        this.specializationManager = plugin.getSpecializationManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Abrir GUI de habilidades
            SkillsGUI.open(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
            case "lista":
                skillManager.showAllSkills(player);
                break;
                
            case "info":
            case "details":
            case "detalles":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso correcto: /skills info <tipo>");
                    return true;
                }
                
                try {
                    SkillType skillType = SkillType.valueOf(args[1].toUpperCase());
                    skillManager.showSkillInfo(player, skillType);
                } catch (IllegalArgumentException e) {
                    MessageUtils.sendMessage(player, "<red>Tipo de habilidad no válido. Usa /skills help para ver los tipos disponibles.");
                }
                break;
                
            case "especializacion":
            case "especialización":
            case "specialization":
            case "spec":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso correcto: /skills especialización <tipo>");
                    return true;
                }
                
                try {
                    SkillType skillType = SkillType.valueOf(args[1].toUpperCase());
                    new SpecializationsGUI(plugin, player, skillType).open();
                } catch (IllegalArgumentException e) {
                    MessageUtils.sendMessage(player, "<red>Tipo de habilidad no válido. Usa /skills help para ver los tipos disponibles.");
                }
                break;
                
            case "help":
            case "ayuda":
                showHelp(player);
                break;
                
            default:
                MessageUtils.sendMessage(player, "<red>Comando no reconocido. Usa /skills help para ver los comandos disponibles.");
                break;
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<gold>✦ Comandos de Habilidades ✦");
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<yellow>/skills</yellow> <gray>- Abre el menú de habilidades");
        MessageUtils.sendMessage(player, "<yellow>/skills list</yellow> <gray>- Muestra todas tus habilidades");
        MessageUtils.sendMessage(player, "<yellow>/skills info <tipo></yellow> <gray>- Muestra detalles de una habilidad");
        MessageUtils.sendMessage(player, "<yellow>/skills especialización <tipo></yellow> <gray>- Abre el menú de especializaciones para una habilidad");
        MessageUtils.sendMessage(player, "<yellow>/skills help</yellow> <gray>- Muestra esta ayuda");
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(player, "<gold>✦ Tipos de Habilidades ✦");
        
        StringBuilder skillTypes = new StringBuilder("<gray>");
        for (SkillType type : SkillType.values()) {
            skillTypes.append(type.name().toLowerCase()).append(", ");
        }
        String types = skillTypes.toString().substring(0, skillTypes.length() - 2);
        MessageUtils.sendMessage(player, types);
        
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("list", "info", "especialización", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("especialización") || args[0].equalsIgnoreCase("especializacion") || args[0].equalsIgnoreCase("specialization") || args[0].equalsIgnoreCase("spec"))) {
            return Arrays.stream(SkillType.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}