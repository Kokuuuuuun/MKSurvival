package com.mk.mksurvival.commands.enemies;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.enemies.EnemyLevelManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Comando para gestionar enemigos y sus niveles
 */
public class EnemyCommand implements CommandExecutor, TabCompleter {
    private final MKSurvival plugin;

    public EnemyCommand(MKSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mksurvival.enemy.admin")) {
            MessageUtils.sendMessage(sender, "<red>No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn" -> handleSpawnCommand(sender, args);
            case "modify", "edit" -> handleModifyCommand(sender, args);
            case "clone", "clonar" -> handleCloneCommand(sender, args);
            case "setarea", "area" -> handleSetAreaCommand(sender, args);
            case "info" -> handleInfoCommand(sender, args);
            case "clear", "limpiar" -> handleClearCommand(sender, args);
            case "reload", "recargar" -> handleReloadCommand(sender);
            case "stats", "estadisticas" -> handleStatsCommand(sender);
            case "list", "listar" -> handleListCommand(sender, args);
            case "help", "ayuda" -> showHelp(sender);
            default -> {
                MessageUtils.sendMessage(sender, "<red>Subcomando no válido. Usa '/enemy help' para ver la ayuda.");
                return true;
            }
        }

        return true;
    }

    private void handleSpawnCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return;
        }

        if (args.length < 3) {
            MessageUtils.sendMessage(player, "<red>Uso: /enemy spawn <tipo> <nivel> [cantidad] [tipo_especial] [nombre_personalizado]");
            return;
        }

        try {
            EntityType entityType = EntityType.valueOf(args[1].toUpperCase());
            int level = Integer.parseInt(args[2]);
            int cantidad = args.length > 3 ? Integer.parseInt(args[3]) : 1;
            EnemyLevelManager.EnemyType specialType = args.length > 4 ? 
                EnemyLevelManager.EnemyType.valueOf(args[4].toUpperCase()) : 
                EnemyLevelManager.EnemyType.NORMAL;
            String customName = args.length > 5 ? String.join(" ", Arrays.copyOfRange(args, 5, args.length)) : null;

            if (level < 1 || level > 100) {
                MessageUtils.sendMessage(player, "<red>El nivel debe estar entre 1 y 100.");
                return;
            }

            if (cantidad < 1 || cantidad > 10) {
                MessageUtils.sendMessage(player, "<red>La cantidad debe estar entre 1 y 10.");
                return;
            }

            Location spawnLocation = player.getLocation();
            
            for (int i = 0; i < cantidad; i++) {
                Location loc = spawnLocation.clone().add(
                    (Math.random() - 0.5) * 4, 0, (Math.random() - 0.5) * 4
                );
                
                plugin.getEnemyLevelManager().spawnCustomEnemy(
                    loc, entityType, level, specialType, customName
                );
            }

            String typeName = switch (specialType) {
                case ELITE -> "<gold>[ÉLITE]</gold>";
                case MINI_BOSS -> "<red>[MINI-JEFE]</red>";
                default -> "";
            };

            String nameInfo = customName != null ? " con nombre '" + customName + "'" : "";
            MessageUtils.sendMessage(player, 
                "<green>[Enemy]</green> <yellow>Has generado " + cantidad + " " + 
                typeName + " <white>" + entityType.name() + "</white> <yellow>de nivel " + 
                level + nameInfo + ".</yellow>"
            );

        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "<red>Nivel y cantidad deben ser números válidos.");
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMessage(player, "<red>Tipo de entidad o tipo especial no válido.");
        }
    }

    private void handleModifyCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return;
        }

        if (args.length < 2) {
            MessageUtils.sendMessage(player, "<red>Uso: /enemy modify [nivel] [tipo] [nombre] [salud] [velocidad]");
            MessageUtils.sendMessage(player, "<yellow>Mira hacia un enemigo y usa los parámetros que quieras modificar.");
            return;
        }

        // Encontrar enemigo más cercano hacia donde mira el jugador
        LivingEntity target = getTargetedEntity(player, 10);
        
        if (target == null) {
            MessageUtils.sendMessage(player, "<red>No hay enemigos en tu línea de visión (10 bloques).");
            return;
        }

        if (!target.hasMetadata("enemy_level")) {
            MessageUtils.sendMessage(player, "<red>La entidad objetivo no es un enemigo del sistema.");
            return;
        }

        try {
            Integer newLevel = null;
            EnemyLevelManager.EnemyType newType = null;
            String newName = null;
            Double newHealth = null;
            Double newSpeed = null;

            // Parsear argumentos
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                
                if (arg.startsWith("nivel:") || arg.startsWith("level:")) {
                    newLevel = Integer.parseInt(arg.split(":")[1]);
                } else if (arg.startsWith("tipo:") || arg.startsWith("type:")) {
                    newType = EnemyLevelManager.EnemyType.valueOf(arg.split(":")[1].toUpperCase());
                } else if (arg.startsWith("nombre:") || arg.startsWith("name:")) {
                    newName = arg.split(":", 2)[1];
                } else if (arg.startsWith("salud:") || arg.startsWith("health:")) {
                    newHealth = Double.parseDouble(arg.split(":")[1]);
                } else if (arg.startsWith("velocidad:") || arg.startsWith("speed:")) {
                    newSpeed = Double.parseDouble(arg.split(":")[1]);
                }
            }

            boolean success = plugin.getEnemyLevelManager().modifyExistingEnemy(
                target, newLevel, newType, newName, newHealth, newSpeed
            );

            if (success) {
                MessageUtils.sendMessage(player, 
                    "<green>[Enemy]</green> <yellow>Enemigo modificado exitosamente!</yellow>"
                );
            } else {
                MessageUtils.sendMessage(player, 
                    "<red>[Enemy]</red> <yellow>No se pudo modificar el enemigo.</yellow>"
                );
            }

        } catch (Exception e) {
            MessageUtils.sendMessage(player, 
                "<red>Error en los parámetros. Formato: nivel:5 tipo:ELITE nombre:MiEnemigo salud:100 velocidad:0.3"
            );
        }
    }

    private void handleCloneCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return;
        }

        LivingEntity target = getTargetedEntity(player, 10);
        
        if (target == null || !target.hasMetadata("enemy_level")) {
            MessageUtils.sendMessage(player, "<red>No hay enemigos válidos en tu línea de visión.");
            return;
        }

        int cantidad = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        if (cantidad < 1 || cantidad > 5) {
            MessageUtils.sendMessage(player, "<red>La cantidad debe estar entre 1 y 5.");
            return;
        }

        Location spawnLocation = player.getLocation();
        
        for (int i = 0; i < cantidad; i++) {
            Location loc = spawnLocation.clone().add(
                (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6
            );
            
            plugin.getEnemyLevelManager().cloneEnemy(target, loc);
        }

        MessageUtils.sendMessage(player, 
            "<green>[Enemy]</green> <yellow>Has clonado " + cantidad + " enemigos!</yellow>"
        );
    }

    private void handleListCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return;
        }

        int radius = args.length > 1 ? Integer.parseInt(args[1]) : 50;
        
        List<LivingEntity> nearbyEnemies = new ArrayList<>();
        Location playerLoc = player.getLocation();

        for (LivingEntity entity : player.getWorld().getLivingEntities()) {
            if (entity == player) continue;
            
            if (entity.getLocation().distance(playerLoc) <= radius && 
                entity.hasMetadata("enemy_level")) {
                nearbyEnemies.add(entity);
            }
        }

        if (nearbyEnemies.isEmpty()) {
            MessageUtils.sendMessage(player, 
                "<yellow>No hay enemigos en un radio de " + radius + " bloques.</yellow>"
            );
            return;
        }

        MessageUtils.sendMessage(player, "<green>═══════════════════════════════════════</green>");
        MessageUtils.sendMessage(player, "<green>║</green>          <white>ENEMIGOS CERCANOS</white>          <green>║</green>");
        MessageUtils.sendMessage(player, "<green>═══════════════════════════════════════</green>");
        
        for (int i = 0; i < Math.min(10, nearbyEnemies.size()); i++) {
            LivingEntity enemy = nearbyEnemies.get(i);
            EnemyLevelManager.EnemyInfo info = plugin.getEnemyLevelManager().getEnemyInfo(enemy);
            
            if (info != null) {
                double distance = enemy.getLocation().distance(playerLoc);
                String manualTag = info.isManuallyCreated ? " <gold>[M]</gold>" : "";
                
                MessageUtils.sendMessage(player, 
                    "<gray>" + (i + 1) + ".</gray> <white>" + info.entityType.name() + "</white> " +
                    "<yellow>Lv." + info.level + "</yellow> " +
                    "<aqua>" + info.type.name() + "</aqua>" + manualTag + " " +
                    "<gray>(" + String.format("%.1f", distance) + "m)</gray>"
                );
            }
        }
        
        if (nearbyEnemies.size() > 10) {
            MessageUtils.sendMessage(player, 
                "<gray>... y " + (nearbyEnemies.size() - 10) + " más.</gray>"
            );
        }
        
        MessageUtils.sendMessage(player, "<green>═══════════════════════════════════════</green>");
    }

    private void handleSetAreaCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return;
        }

        if (args.length < 3) {
            MessageUtils.sendMessage(player, "<red>Uso: /enemy setarea <nivel> <radio>");
            return;
        }

        try {
            int level = Integer.parseInt(args[1]);
            int radius = Integer.parseInt(args[2]);

            if (level < 1 || level > 50) {
                MessageUtils.sendMessage(player, "<red>El nivel del área debe estar entre 1 y 50.");
                return;
            }

            if (radius < 10 || radius > 1000) {
                MessageUtils.sendMessage(player, "<red>El radio debe estar entre 10 y 1000 bloques.");
                return;
            }

            plugin.getEnemyLevelManager().setAreaLevel(player.getLocation(), level, radius);
            
            MessageUtils.sendMessage(player, 
                "<green>[Enemy]</green> <yellow>Área establecida con nivel " + level + 
                " en un radio de " + radius + " bloques desde tu posición.</yellow>"
            );

        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "<red>Nivel y radio deben ser números válidos.");
        }
    }

    private void handleInfoCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return;
        }

        // Buscar la entidad más cercana
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : player.getLocation().getWorld().getLivingEntities()) {
            if (entity == player) continue;
            
            double distance = entity.getLocation().distance(player.getLocation());
            if (distance < nearestDistance && distance <= 10) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        if (nearest == null) {
            MessageUtils.sendMessage(player, "<red>No hay enemigos cerca (radio de 10 bloques).");
            return;
        }

        if (!nearest.hasMetadata("enemy_level")) {
            MessageUtils.sendMessage(player, "<yellow>La entidad más cercana no tiene nivel asignado.");
            return;
        }

        EnemyLevelManager.EnemyInfo info = plugin.getEnemyLevelManager().getEnemyInfo(nearest);
        if (info == null) {
            MessageUtils.sendMessage(player, "<red>No se pudo obtener información del enemigo.");
            return;
        }

        double distance = nearest.getLocation().distance(player.getLocation());
        String manualTag = info.isManuallyCreated ? " <gold>[MANUAL]</gold>" : " <gray>[AUTO]</gray>";

        MessageUtils.sendMessage(player, "<green>╔═══════════════════════════════════════╗</green>");
        MessageUtils.sendMessage(player, "<green>║</green>              <white>INFO DEL ENEMIGO</white>              <green>║</green>");
        MessageUtils.sendMessage(player, "<green>╠═══════════════════════════════════════╣</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Tipo:</yellow> <white>" + info.entityType.name() + "</white>" + manualTag + " <green>║</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Nivel:</yellow> <gold>" + info.level + "</gold> <green>║</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Tipo Especial:</yellow> <aqua>" + info.type.name() + "</aqua> <green>║</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Tier:</yellow> <light_purple>" + info.tier.name() + "</light_purple> <green>║</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Salud:</yellow> <red>" + String.format("%.1f", info.health) + "/" + String.format("%.1f", info.maxHealth) + "</red> <green>║</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Daño:</yellow> <dark_red>" + String.format("%.1f", info.damage) + "</dark_red> <green>║</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Velocidad:</yellow> <blue>" + String.format("%.2f", info.speed) + "</blue> <green>║</green>");
        MessageUtils.sendMessage(player, "<green>║</green> <yellow>Distancia:</yellow> <gray>" + String.format("%.1f", distance) + " bloques</gray> <green>║</green>");
        if (info.customName != null) {
            MessageUtils.sendMessage(player, "<green>║</green> <yellow>Nombre:</yellow> <white>" + info.customName + "</white> <green>║</green>");
        }
        MessageUtils.sendMessage(player, "<green>╚═══════════════════════════════════════╝</green>");
    }

    private LivingEntity getTargetedEntity(Player player, double maxDistance) {
        return player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            maxDistance,
            entity -> entity instanceof LivingEntity && entity != player
        ).getHitEntity() instanceof LivingEntity target ? target : null;
    }

    private void handleClearCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return;
        }

        int radius = args.length > 1 ? Integer.parseInt(args[1]) : 50;
        
        if (radius < 1 || radius > 200) {
            MessageUtils.sendMessage(player, "<red>El radio debe estar entre 1 y 200 bloques.");
            return;
        }

        int cleared = 0;
        Location playerLoc = player.getLocation();

        for (LivingEntity entity : player.getWorld().getLivingEntities()) {
            if (entity == player) continue;
            
            if (entity.getLocation().distance(playerLoc) <= radius) {
                if (entity.hasMetadata("enemy_level")) {
                    entity.remove();
                    cleared++;
                }
            }
        }

        MessageUtils.sendMessage(player,
            "<green>[Enemy]</green> <yellow>Se eliminaron " + cleared + 
            " enemigos en un radio de " + radius + " bloques.</yellow>"
        );
    }

    private void handleReloadCommand(CommandSender sender) {
        // Aquí podrías recargar configuraciones del sistema de enemigos
        MessageUtils.sendMessage(sender,
            "<green>[Enemy]</green> <yellow>Sistema de enemigos recargado.</yellow>"
        );
    }

    private void handleStatsCommand(CommandSender sender) {
        int totalEnemies = 0;
        int eliteEnemies = 0;
        int bossEnemies = 0;
        int averageLevel = 0;
        int totalLevels = 0;

        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity.hasMetadata("enemy_level")) {
                    totalEnemies++;
                    int level = entity.getMetadata("enemy_level").get(0).asInt();
                    totalLevels += level;
                    
                    if (entity.hasMetadata("enemy_type")) {
                        String type = entity.getMetadata("enemy_type").get(0).asString();
                        if ("ELITE".equals(type)) {
                            eliteEnemies++;
                        } else if ("MINI_BOSS".equals(type)) {
                            bossEnemies++;
                        }
                    }
                }
            }
        }

        averageLevel = totalEnemies > 0 ? totalLevels / totalEnemies : 0;

        MessageUtils.sendMessage(sender, "<green>╔═══════════════════════════════════════╗</green>");
        MessageUtils.sendMessage(sender, "<green>║</green>           <white>ESTADÍSTICAS DE ENEMIGOS</white>           <green>║</green>");
        MessageUtils.sendMessage(sender, "<green>╠═══════════════════════════════════════╣</green>");
        MessageUtils.sendMessage(sender, "<green>║</green> <yellow>Total de enemigos:</yellow> <white>" + totalEnemies + "</white>");
        MessageUtils.sendMessage(sender, "<green>║</green> <yellow>Enemigos élite:</yellow> <gold>" + eliteEnemies + "</gold>");
        MessageUtils.sendMessage(sender, "<green>║</green> <yellow>Mini-jefes:</yellow> <red>" + bossEnemies + "</red>");
        MessageUtils.sendMessage(sender, "<green>║</green> <yellow>Nivel promedio:</yellow> <aqua>" + averageLevel + "</aqua>");
        MessageUtils.sendMessage(sender, "<green>╚═══════════════════════════════════════╝</green>");
    }

    private void showHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "<red>╔═══════════════════════════════════════╗</red>");
        MessageUtils.sendMessage(sender, "<red>║</red>              <white><bold>GESTIÓN DE ENEMIGOS</bold></white>               <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>╠═══════════════════════════════════════╣</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy spawn <tipo> <nivel> [cant] [especial] [nombre]</yellow> <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Genera enemigos personalizados completamente</gray>       <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy modify [nivel:X] [tipo:Y] [nombre:Z]</yellow>        <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Modifica enemigo hacia donde miras</gray>              <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy clone [cantidad]</yellow>                      <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Clona el enemigo hacia donde miras</gray>              <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy setarea <nivel> <radio></yellow>                <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Establece nivel de área para spawns</gray>             <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy info</yellow>                                  <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Información detallada del enemigo más cercano</gray>   <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy list [radio]</yellow>                          <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Lista enemigos cercanos</gray>                       <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy clear [radio]</yellow>                         <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Elimina enemigos en el área</gray>                   <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <yellow>/enemy stats</yellow>                                 <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <gray>  Estadísticas globales de enemigos</gray>              <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>╠═══════════════════════════════════════╣</red>");
        MessageUtils.sendMessage(sender, "<red>║</red>               <white>EJEMPLOS DE USO</white>                  <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <green>/enemy spawn ZOMBIE 25 3 ELITE Boss_Zombie</green>    <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <green>/enemy modify nivel:30 tipo:MINI_BOSS</green>          <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>║</red> <green>/enemy modify nombre:Rey_Esqueleto salud:500</green>   <red>║</red>");
        MessageUtils.sendMessage(sender, "<red>╚═══════════════════════════════════════╝</red>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("spawn", "modify", "clone", "setarea", "info", "clear", "reload", "stats", "list", "help")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            return Arrays.stream(EntityType.values())
                    .filter(EntityType::isAlive)
                    .map(EntityType::name)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 5 && args[0].equalsIgnoreCase("spawn")) {
            return Arrays.asList("NORMAL", "ELITE", "MINI_BOSS")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[4].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("modify")) {
            return Arrays.asList("nivel:", "tipo:", "nombre:", "salud:", "velocidad:")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return List.of();
    }
}