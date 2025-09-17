package com.mk.mksurvival.managers.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Gestiona las especializaciones de habilidades disponibles y las especializaciones de los jugadores.
 */
public class SpecializationManager implements Listener {
    private final MKSurvival plugin;
    private final Map<String, SkillSpecialization> specializations;
    private final Map<UUID, PlayerSpecializations> playerSpecializations;
    private final File playerDataFolder;
    
    /**
     * Constructor para crear un nuevo gestor de especializaciones.
     *
     * @param plugin Instancia del plugin principal
     */
    public SpecializationManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.specializations = new HashMap<>();
        this.playerSpecializations = new HashMap<>();
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        
        loadSpecializations();
        
        // Register as event listener to handle specialization bonuses
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Carga todas las especializaciones desde la configuración.
     */
    private void loadSpecializations() {
        // Asegurarse de que el archivo de configuración existe
        File specializationsFile = new File(plugin.getDataFolder(), "specializations.yml");
        if (!specializationsFile.exists()) {
            plugin.saveResource("specializations.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(specializationsFile);
        ConfigurationSection specializationsSection = config.getConfigurationSection("specializations");
        
        if (specializationsSection == null) {
            plugin.getLogger().warning("No se encontraron especializaciones en la configuración.");
            return;
        }
        
        for (String specializationId : specializationsSection.getKeys(false)) {
            ConfigurationSection specSection = specializationsSection.getConfigurationSection(specializationId);
            if (specSection == null) continue;
            
            String name = specSection.getString("name", "Especialización desconocida");
            String description = specSection.getString("description", "Sin descripción");
            String skillTypeStr = specSection.getString("skill_type", "MINING");
            int maxLevel = specSection.getInt("max_level", 5);
            int requiredSkillLevel = specSection.getInt("required_skill_level", 10);
            int unlockCost = specSection.getInt("unlock_cost", 1000);
            int upgradeCostBase = specSection.getInt("upgrade_cost_base", 500);
            
            SkillType skillType;
            try {
                skillType = SkillType.valueOf(skillTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Tipo de habilidad inválido para la especialización " + specializationId + ": " + skillTypeStr);
                continue;
            }
            
            List<String> levelBonuses = specSection.getStringList("level_bonuses");
            if (levelBonuses.isEmpty()) {
                // Crear bonificaciones predeterminadas si no se especifican
                levelBonuses = new ArrayList<>();
                for (int i = 1; i <= maxLevel; i++) {
                    levelBonuses.add("Nivel " + i + ": Bonificación predeterminada");
                }
            }
            
            SkillSpecialization specialization = new SkillSpecialization(
                specializationId, name, description, skillType, maxLevel, 
                levelBonuses, requiredSkillLevel, unlockCost, upgradeCostBase
            );
            
            specializations.put(specializationId, specialization);
        }
        
        plugin.getLogger().info("Se cargaron " + specializations.size() + " especializaciones.");
    }
    
    /**
     * Obtiene una especialización por su ID.
     *
     * @param specializationId El ID de la especialización
     * @return La especialización, o null si no existe
     */
    public SkillSpecialization getSpecialization(String specializationId) {
        return specializations.get(specializationId);
    }
    
    /**
     * Obtiene todas las especializaciones disponibles.
     *
     * @return Un mapa con todas las especializaciones
     */
    public Map<String, SkillSpecialization> getAllSpecializations() {
        return new HashMap<>(specializations);
    }
    
    /**
     * Obtiene todas las especializaciones para un tipo de habilidad específico.
     *
     * @param skillType El tipo de habilidad
     * @return Una lista con las especializaciones para ese tipo de habilidad
     */
    public List<SkillSpecialization> getSpecializationsForSkillType(SkillType skillType) {
        List<SkillSpecialization> result = new ArrayList<>();
        for (SkillSpecialization spec : specializations.values()) {
            if (spec.getSkillType() == skillType) {
                result.add(spec);
            }
        }
        return result;
    }
    
    /**
     * Obtiene las especializaciones de un jugador.
     *
     * @param player El jugador
     * @return Las especializaciones del jugador
     */
    public PlayerSpecializations getPlayerSpecializations(Player player) {
        return getPlayerSpecializations(player.getUniqueId());
    }
    
    /**
     * Obtiene las especializaciones de un jugador por su UUID.
     *
     * @param playerId El UUID del jugador
     * @return Las especializaciones del jugador
     */
    public PlayerSpecializations getPlayerSpecializations(UUID playerId) {
        PlayerSpecializations playerSpecs = playerSpecializations.get(playerId);
        if (playerSpecs == null) {
            playerSpecs = new PlayerSpecializations();
            playerSpecializations.put(playerId, playerSpecs);
            loadPlayerSpecializations(playerId);
        }
        return playerSpecs;
    }
    
    /**
     * Desbloquea una especialización para un jugador.
     *
     * @param player El jugador
     * @param specializationId El ID de la especialización a desbloquear
     * @return true si se desbloqueó correctamente, false en caso contrario
     */
    public boolean unlockSpecialization(Player player, String specializationId) {
        SkillSpecialization specialization = getSpecialization(specializationId);
        if (specialization == null) {
            return false;
        }
        
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        if (playerSpecs.hasSpecialization(specializationId)) {
            return false; // Ya está desbloqueada
        }
        
        // Verificar si el jugador tiene el nivel de habilidad requerido
        SkillManager skillManager = plugin.getSkillManager();
        int skillLevel = skillManager.getSkillLevel(player, specialization.getSkillType());
        if (skillLevel < specialization.getRequiredSkillLevel()) {
            MessageUtils.sendMessage(player, "<red>Necesitas tener nivel " + 
                specialization.getRequiredSkillLevel() + " en " + 
                specialization.getSkillType().getFormattedName() + " <red>para desbloquear esta especialización.");
            return false;
        }
        
        // Verificar si el jugador tiene suficiente dinero
        int cost = specialization.getUnlockCost();
        if (plugin.getEconomyManager().getBalance(player) < cost) {
            MessageUtils.sendMessage(player, "<red>Necesitas $" + cost + " para desbloquear esta especialización.");
            return false;
        }
        
        boolean success = playerSpecs.unlockSpecialization(specializationId);
        if (success) {
            // Cobrar dinero
            plugin.getEconomyManager().withdrawBalance(player, cost);
            
            MessageUtils.sendMessage(player, "<green>¡Has desbloqueado la especialización " + 
                specialization.getFormattedName() + "<green>!");
            savePlayerSpecializations(player.getUniqueId());
        }
        
        return success;
    }
    
    /**
     * Mejora una especialización para un jugador.
     *
     * @param player El jugador
     * @param specializationId El ID de la especialización a mejorar
     * @return true si se mejoró correctamente, false en caso contrario
     */
    public boolean upgradeSpecialization(Player player, String specializationId) {
        SkillSpecialization specialization = getSpecialization(specializationId);
        if (specialization == null) {
            return false;
        }
        
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        if (!playerSpecs.hasSpecialization(specializationId)) {
            MessageUtils.sendMessage(player, "<red>No tienes desbloqueada esta especialización.</red>");
            return false;
        }
        
        int currentLevel = playerSpecs.getSpecializationLevel(specializationId);
        if (currentLevel >= specialization.getMaxLevel()) {
            MessageUtils.sendMessage(player, "<red>Ya has alcanzado el nivel máximo en esta especialización.</red>");
            return false;
        }
        
        // Verificar si el jugador tiene suficiente dinero
        int cost = specialization.getUpgradeCost(currentLevel);
        if (plugin.getEconomyManager().getBalance(player) < cost) {
            MessageUtils.sendMessage(player, "<red>Necesitas $" + cost + " para mejorar esta especialización.");
            return false;
        }
        
        boolean success = playerSpecs.upgradeSpecialization(specializationId, specialization.getMaxLevel());
        if (success) {
            // Cobrar dinero
            plugin.getEconomyManager().withdrawBalance(player, cost);
            
            int newLevel = playerSpecs.getSpecializationLevel(specializationId);
            MessageUtils.sendMessage(player, "<green>¡Has mejorado la especialización " + 
                specialization.getFormattedName() + " al nivel " + newLevel + "!</green>");
            
            // Mostrar la bonificación del nuevo nivel
            String bonus = specialization.getFormattedBonusForLevel(newLevel);
            if (bonus != null) {
                MessageUtils.sendMessage(player, "<green>Nueva bonificación: " + bonus + "</green>");
            }
            
            savePlayerSpecializations(player.getUniqueId());
        }
        
        return success;
    }
    
    /**
     * Carga las especializaciones de un jugador desde el archivo de datos.
     *
     * @param playerId El UUID del jugador
     */
    public void loadPlayerSpecializations(UUID playerId) {
        File playerFile = new File(playerDataFolder, playerId.toString() + ".yml");
        if (!playerFile.exists()) {
            return; // No hay datos guardados para este jugador
        }
        
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        ConfigurationSection specializationsSection = playerConfig.getConfigurationSection("specializations");
        if (specializationsSection == null) {
            return;
        }
        
        PlayerSpecializations playerSpecs = getPlayerSpecializations(playerId);
        
        for (String specializationId : specializationsSection.getKeys(false)) {
            int level = specializationsSection.getInt(specializationId, 1);
            playerSpecs.addSpecialization(specializationId, level);
        }
    }
    
    /**
     * Guarda las especializaciones de un jugador en el archivo de datos.
     *
     * @param playerId El UUID del jugador
     */
    public void savePlayerSpecializations(UUID playerId) {
        PlayerSpecializations playerSpecs = playerSpecializations.get(playerId);
        if (playerSpecs == null) {
            return; // No hay datos para guardar
        }
        
        File playerFile = new File(playerDataFolder, playerId.toString() + ".yml");
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        
        // Limpiar la sección de especializaciones existente
        playerConfig.set("specializations", null);
        
        // Guardar las especializaciones actuales
        Map<String, PlayerSpecialization> specs = playerSpecs.getAllSpecializations();
        for (Map.Entry<String, PlayerSpecialization> entry : specs.entrySet()) {
            String specializationId = entry.getKey();
            int level = entry.getValue().getLevel();
            playerConfig.set("specializations." + specializationId, level);
        }
        
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar las especializaciones del jugador " + playerId, e);
        }
    }
    
    /**
     * Guarda las especializaciones de todos los jugadores.
     */
    public void saveAllPlayerSpecializations() {
        for (UUID playerId : playerSpecializations.keySet()) {
            savePlayerSpecializations(playerId);
        }
    }
    
    /**
     * Limpia los datos de un jugador cuando se desconecta.
     *
     * @param playerId El UUID del jugador
     */
    public void clearPlayerData(UUID playerId) {
        savePlayerSpecializations(playerId);
        playerSpecializations.remove(playerId);
    }
    
    // ===== SPECIALIZATION BONUS SYSTEM =====
    
    /**
     * Aplica bonificaciones de minería cuando un jugador rompe un bloque.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        
        // Enhanced Mining specialization
        if (playerSpecs.hasSpecialization("enhanced_mining")) {
            int level = playerSpecs.getSpecializationLevel("enhanced_mining");
            
            // Faster mining speed (haste effect)
            if (ThreadLocalRandom.current().nextInt(100) < 25 + (level * 5)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, level - 1));
            }
        }
        
        // Fortune Mining specialization
        if (playerSpecs.hasSpecialization("fortune_mining")) {
            int level = playerSpecs.getSpecializationLevel("fortune_mining");
            
            // Extra drops chance
            if (ThreadLocalRandom.current().nextInt(100) < 10 + (level * 5)) {
                ItemStack bonus = new ItemStack(event.getBlock().getType());
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), bonus);
                MessageUtils.sendMessage(player, "<green>[Especialización] ¡Gota extra!");
            }
        }
        
        // Gem Finding specialization
        if (playerSpecs.hasSpecialization("gem_finding")) {
            int level = playerSpecs.getSpecializationLevel("gem_finding");
            
            // Rare gem drops
            if (ThreadLocalRandom.current().nextInt(1000) < level * 2) {
                ItemStack gem = plugin.getItemQualityManager().createRandomGem();
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), gem);
                MessageUtils.sendMessage(player, "<rainbow>[Especialización] ¡Has encontrado una gema rara!");
            }
        }
    }
    
    /**
     * Aplica bonificaciones de combate cuando un jugador ataca una entidad.
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        
        // Critical Strikes specialization
        if (playerSpecs.hasSpecialization("critical_strikes")) {
            int level = playerSpecs.getSpecializationLevel("critical_strikes");
            
            // Critical hit chance
            if (ThreadLocalRandom.current().nextInt(100) < 5 + (level * 3)) {
                double multiplier = 1.5 + (level * 0.2);
                event.setDamage(event.getDamage() * multiplier);
                MessageUtils.sendMessage(player, "<red>[Crítico] ¡Golpe crítico!");
                
                // Visual effect
                plugin.getParticleManager().spawnCriticalEffect(event.getEntity().getLocation());
            }
        }
        
        // Berserker specialization
        if (playerSpecs.hasSpecialization("berserker")) {
            int level = playerSpecs.getSpecializationLevel("berserker");
            
            // Damage increase when low health
            double healthPercent = player.getHealth() / player.getMaxHealth();
            if (healthPercent < 0.5) {
                double bonus = 1.0 + (level * 0.15) * (1.0 - healthPercent);
                event.setDamage(event.getDamage() * bonus);
            }
        }
        
        // Lifesteal specialization
        if (playerSpecs.hasSpecialization("lifesteal")) {
            int level = playerSpecs.getSpecializationLevel("lifesteal");
            
            // Heal on damage
            double healAmount = event.getDamage() * (level * 0.05);
            if (player.getHealth() + healAmount <= player.getMaxHealth()) {
                player.setHealth(player.getHealth() + healAmount);
            } else {
                player.setHealth(player.getMaxHealth());
            }
        }
    }
    
    /**
     * Aplica bonificaciones de pesca cuando un jugador pesca.
     */
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        
        Player player = event.getPlayer();
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        
        // Lucky Fishing specialization
        if (playerSpecs.hasSpecialization("lucky_fishing")) {
            int level = playerSpecs.getSpecializationLevel("lucky_fishing");
            
            // Rare catch chance
            if (ThreadLocalRandom.current().nextInt(100) < 5 + (level * 3)) {
                ItemStack rareItem = plugin.getItemQualityManager().createRandomRareItem();
                player.getWorld().dropItemNaturally(player.getLocation(), rareItem);
                MessageUtils.sendMessage(player, "<aqua>[Especialización] ¡Captura especial!");
            }
        }
        
        // Treasure Hunter specialization
        if (playerSpecs.hasSpecialization("treasure_hunter")) {
            int level = playerSpecs.getSpecializationLevel("treasure_hunter");
            
            // Treasure chance
            if (ThreadLocalRandom.current().nextInt(200) < level) {
                int reward = 100 + (level * 50);
                plugin.getEconomyManager().addBalance(player, reward);
                MessageUtils.sendMessage(player, "<gold>[Especialización] ¡Has encontrado un tesoro! +$" + reward);
            }
        }
        
        // Double Catch specialization
        if (playerSpecs.hasSpecialization("double_catch")) {
            int level = playerSpecs.getSpecializationLevel("double_catch");
            
            // Double catch chance
            if (ThreadLocalRandom.current().nextInt(100) < 10 + (level * 5)) {
                if (event.getCaught() instanceof org.bukkit.entity.Item) {
                    org.bukkit.entity.Item caughtItem = (org.bukkit.entity.Item) event.getCaught();
                    ItemStack duplicate = caughtItem.getItemStack().clone();
                    player.getWorld().dropItemNaturally(player.getLocation(), duplicate);
                    MessageUtils.sendMessage(player, "<blue>[Especialización] ¡Captura doble!");
                }
            }
        }
    }
    
    /**
     * Obtiene el multiplicador de experiencia para un jugador basado en sus especializaciones.
     */
    public double getExperienceMultiplier(Player player, SkillType skillType) {
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        double multiplier = 1.0;
        
        switch (skillType) {
            case MINING:
                if (playerSpecs.hasSpecialization("efficient_miner")) {
                    int level = playerSpecs.getSpecializationLevel("efficient_miner");
                    multiplier += level * 0.1; // 10% per level
                }
                break;
            case COMBAT:
                if (playerSpecs.hasSpecialization("combat_mastery")) {
                    int level = playerSpecs.getSpecializationLevel("combat_mastery");
                    multiplier += level * 0.08; // 8% per level
                }
                break;
            case WOODCUTTING:
                if (playerSpecs.hasSpecialization("forestry_expert")) {
                    int level = playerSpecs.getSpecializationLevel("forestry_expert");
                    multiplier += level * 0.12; // 12% per level
                }
                break;
            case FISHING:
                if (playerSpecs.hasSpecialization("fishing_mastery")) {
                    int level = playerSpecs.getSpecializationLevel("fishing_mastery");
                    multiplier += level * 0.15; // 15% per level
                }
                break;
        }
        
        return multiplier;
    }
    
    /**
     * Obtiene bonificaciones de daño para combate.
     */
    public double getDamageBonus(Player player) {
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        double bonus = 0.0;
        
        if (playerSpecs.hasSpecialization("weapon_mastery")) {
            int level = playerSpecs.getSpecializationLevel("weapon_mastery");
            bonus += level * 0.1; // 10% damage per level
        }
        
        if (playerSpecs.hasSpecialization("strength_training")) {
            int level = playerSpecs.getSpecializationLevel("strength_training");
            bonus += level * 0.05; // 5% damage per level
        }
        
        return bonus;
    }
    
    /**
     * Obtiene bonificaciones de velocidad de minado.
     */
    public double getMiningSpeedBonus(Player player) {
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        double bonus = 0.0;
        
        if (playerSpecs.hasSpecialization("speed_mining")) {
            int level = playerSpecs.getSpecializationLevel("speed_mining");
            bonus += level * 0.15; // 15% speed per level
        }
        
        return bonus;
    }
    
    /**
     * Verifica si un jugador tiene inmunidad a ciertos efectos.
     */
    public boolean hasImmunity(Player player, String immunityType) {
        PlayerSpecializations playerSpecs = getPlayerSpecializations(player);
        
        switch (immunityType.toLowerCase()) {
            case "fire":
                return playerSpecs.hasSpecialization("fire_resistance") &&
                       playerSpecs.getSpecializationLevel("fire_resistance") >= 3;
            case "poison":
                return playerSpecs.hasSpecialization("poison_immunity") &&
                       playerSpecs.getSpecializationLevel("poison_immunity") >= 4;
            case "fall":
                return playerSpecs.hasSpecialization("feather_fall") &&
                       playerSpecs.getSpecializationLevel("feather_fall") >= 5;
            default:
                return false;
        }
    }
    
    /**
     * Otorga un punto de especialización al jugador.
     *
     * @param player El jugador al que otorgar el punto
     */
    public void grantSpecializationPoint(Player player) {
        // Implementation for granting specialization points
        // This could involve updating a counter or currency system
        MessageUtils.sendMessage(player, "<green>[Especialización]</green> <yellow>¡Has ganado un punto de especialización!");
    }
    
    /**
     * Obtiene todas las especializaciones disponibles.
     *
     * @return Mapa con todas las especializaciones disponibles
     */
    public Map<String, SkillSpecialization> getSpecializations() {
        return new HashMap<>(specializations);
    }
}