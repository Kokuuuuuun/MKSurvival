package com.mk.mksurvival.commands.pets;
import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.pets.PetManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PetCommand implements CommandExecutor {
    private final MKSurvival plugin;
    private final PetManager petManager;

    public PetCommand(MKSurvival plugin) {
        this.plugin = plugin;
        this.petManager = plugin.getPetManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser usado por jugadores.</red>");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            MessageUtils.sendMessage(player, "<red>Uso: /mascota <spawn|despedir|nombre|habilidad|info|tipos></red>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "spawn":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /mascota spawn <tipo></red>");
                    return true;
                }
                try {
                    EntityType type = EntityType.valueOf(args[1].toUpperCase());
                    petManager.spawnPet(player, type.name(), "Mascota");
                } catch (IllegalArgumentException e) {
                    MessageUtils.sendMessage(player, "<red>Tipo de mascota inválido. Usa /mascota tipos para ver los disponibles.</red>");
                }
                break;
            case "despedir":
                petManager.despawnPet(player);
                break;
            case "nombre":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, "<red>Uso: /mascota nombre <nombre></red>");
                    return true;
                }
                String name = String.join(" ", args).substring(7);
                petManager.namePet(player, name);
                break;
            case "habilidad":
                petManager.petSkill(player);
                break;
            case "info":
                petManager.showPetInfo(player);
                break;
            case "tipos":
                petManager.listPetTypes(player);
                break;
            default:
                MessageUtils.sendMessage(player, "<red>Comando desconocido.</red>");
                break;
        }
        return true;
    }
}