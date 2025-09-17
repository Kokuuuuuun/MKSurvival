package com.mk.mksurvival.commands.skills;
import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.skills.SpecializationsGUI;
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

public class SkillSpecializationCommand implements CommandExecutor, TabCompleter {
    private final MKSurvival plugin;
    private final SpecializationManager specializationManager;

    public SkillSpecializationCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.specializationManager = plugin.getSpecializationManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            MessageUtils.sendMessage(sender, "<red>Uso: /specialization <habilidad>");
            return true;
        }
        try {
            SkillType skillType = SkillType.valueOf(args[0].toUpperCase());
            new SpecializationsGUI(plugin, player, skillType).open();
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMessage(sender, "<red>Habilidad no válida: " + args[0]);
        }
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.stream(SkillType.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}