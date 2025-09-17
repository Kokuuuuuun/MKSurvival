package com.mk.mksurvival.managers.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class SkillManager implements Listener {
    private final MKSurvival plugin;
    private final Map<UUID, PlayerSkills> playerSkillsMap = new HashMap<>();
    private final FileConfiguration skillsConfig;
    private final Map<SkillType, Skill> skills = new EnumMap<>(SkillType.class);

    public SkillManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.skillsConfig = plugin.getConfigManager().getSkillsConfig();
        initializeSkills();
        loadAllPlayerData();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void initializeSkills() {
        // Inicializar las habilidades con sus bonificaciones
        for (SkillType type : SkillType.values()) {
            Skill skill = new Skill(type, type.getDisplayName(), type.getDescription(), type.getIcon());
            
            switch (type) {
                case MINING:
                    skill.addBonus("Doble Drop: +10% cada 10 niveles");
                    skill.addBonus("Velocidad Minera: +10% al nivel 20");
                    skill.addBonus("Chance de Diamante: +5% al nivel 50");
                    break;
                case COMBAT:
                    skill.addBonus("Daño: +5% cada 5 niveles");
                    skill.addBonus("Crítico: +3% cada 15 niveles");
                    skill.addBonus("Vampirismo: +2% cada 30 niveles");
                    break;
                case WOODCUTTING:
                    skill.addBonus("Tala Completa: +8% cada 8 niveles");
                    skill.addBonus("Drops Extra: +15% al nivel 25");
                    break;
                case FISHING:
                    skill.addBonus("Peces Raros: +5% cada 5 niveles");
                    skill.addBonus("Tesoros: +3% cada 20 niveles");
                    break;
                case FARMING:
                    skill.addBonus("Rendimiento: +6% cada 6 niveles");
                    skill.addBonus("Crecimiento Rápido: +10% al nivel 18");
                    break;
                case FORAGING:
                    skill.addBonus("Hallazgos Raros: +7% cada 7 niveles");
                    break;
                case ALCHEMY:
                    skill.addBonus("Eficiencia: +10% cada 10 niveles");
                    skill.addBonus("Duración: +20% al nivel 25");
                    break;
                case ENCHANTING:
                    skill.addBonus("Bonus Encantamientos: +8% cada 8 niveles");
                    skill.addBonus("Niveles Extra: +1 al nivel 22");
                    break;
            }
            
            skills.put(type, skill);
        }
    }

    public PlayerSkills getPlayerSkills(Player player) {
        return playerSkillsMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerSkills());
    }

    public PlayerSkills getPlayerSkills(UUID playerUUID) {
        return playerSkillsMap.computeIfAbsent(playerUUID, uuid -> new PlayerSkills());
    }

    public int getSkillLevel(Player player, SkillType skillType) {
        return getPlayerSkills(player).getLevel(skillType);
    }
    
    public Skill getSkill(SkillType type) {
        return skills.get(type);
    }
    
    public Map<SkillType, Skill> getAllSkills() {
        return skills;
    }

    public void addExp(Player player, SkillType skillType, double amount) {
        PlayerSkills playerSkills = getPlayerSkills(player);
        playerSkills.addExp(skillType, amount);
        
        // Mostrar mensaje de experiencia ganada
        MessageUtils.sendMessage(player, "<gray>[<gold>Skills<gray>] <white>+" + amount + " XP <gray>en " + skillType.getFormattedName());
        
        checkLevelUp(player, skillType);
        savePlayerData(player);
    }

    private void checkLevelUp(Player player, SkillType skillType) {
        PlayerSkills playerSkills = getPlayerSkills(player);
        PlayerSkill skill = playerSkills.getSkill(skillType);
        
        if (skill.checkLevelUp()) {
            // Mensaje de subida de nivel
            MessageUtils.sendMessage(player, "<gray>[<gold>Skills<gray>] <green>¡Has subido al nivel <yellow>" + skill.getLevel() + "</yellow> en " + skillType.getFormattedName() + "<green>!");
            
            // Aplicar recompensas de nivel
            applyLevelRewards(player, skillType, skill.getLevel());
            
            // Comprobar si sube más de un nivel
            checkLevelUp(player, skillType);
        }
    }

    private void applyLevelRewards(Player player, SkillType skillType, int level) {
        PlayerSkill skill = getPlayerSkills(player).getSkill(skillType);
        String message = null;
        
        switch (skillType) {
            case MINING:
                if (level % 10 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getDoubleDropChance() + "%</gold> de chance de doble drop en minería!</yellow>";
                }
                break;
            case COMBAT:
                if (level % 5 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getDamageBonus() + "%</gold> de bonus de daño!</yellow>";
                }
                break;
            case WOODCUTTING:
                if (level % 8 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getTreeFellerChance() + "%</gold> de chance de tala completa!</yellow>";
                }
                break;
            case FISHING:
                if (level % 5 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getRareFishChance() + "%</gold> de chance de peces raros!</yellow>";
                }
                break;
            case FARMING:
                if (level % 6 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getCropYieldBonus() + "%</gold> de bonus de rendimiento!</yellow>";
                }
                break;
            case FORAGING:
                if (level % 7 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getRareFindChance() + "%</gold> de chance de hallazgos raros!</yellow>";
                }
                break;
            case ALCHEMY:
                if (level % 10 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getPotionEfficiencyBonus() + "%</gold> de eficiencia en pociones!</yellow>";
                }
                break;
            case ENCHANTING:
                if (level % 8 == 0) {
                    message = "<yellow>[Skills] ¡Has desbloqueado un <gold>" + skill.getEnchantmentBonus() + "%</gold> de bonus en encantamientos!</yellow>";
                }
                break;
        }
        
        if (message != null) {
            MessageUtils.sendMessage(player, message);
        }
    }

    public double getExpNeeded(int level) {
        return 100 * level; // Fórmula simple: 100 * nivel
    }
    
    public void showSkillInfo(Player player, SkillType skillType) {
        PlayerSkills playerSkills = getPlayerSkills(player);
        PlayerSkill skill = playerSkills.getSkill(skillType);
        Skill skillInfo = skills.get(skillType);
        
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
        MessageUtils.sendMessage(player, "<gold>⚡ " + skillType.getFormattedName());
        MessageUtils.sendMessage(player, "<gray>" + skillType.getDescription());
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
        MessageUtils.sendMessage(player, "<white>Nivel: <yellow>" + skill.getLevel());
        MessageUtils.sendMessage(player, "<white>Experiencia: <green>" + (int)skill.getExp() + "<gray>/</gray><green>" + (int)skill.getExpNeeded());
        MessageUtils.sendMessage(player, "<white>Progreso: <aqua>" + String.format("%.1f", skill.getProgressPercentage()) + "%");
        
        // Barra de progreso
        String progressBar = "<green>" + skill.getProgressBar();
        MessageUtils.sendMessage(player, progressBar);
        
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
        MessageUtils.sendMessage(player, "<gold>✦ Bonificaciones:");
        
        for (String bonus : skillInfo.getFormattedBonuses()) {
            MessageUtils.sendMessage(player, bonus);
        }
        
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
    }
    
    public void showAllSkills(Player player) {
        PlayerSkills playerSkills = getPlayerSkills(player);
        
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
        MessageUtils.sendMessage(player, "<gold>✦ Tus Habilidades ✦");
        MessageUtils.sendMessage(player, "<gray>Nivel Total: <yellow>" + playerSkills.getTotalLevel());
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
        
        for (SkillType type : SkillType.values()) {
            PlayerSkill skill = playerSkills.getSkill(type);
            MessageUtils.sendMessage(player, type.getFormattedName() + ": <yellow>" + skill.getLevel() + " <gray>(" + String.format("%.1f", skill.getProgressPercentage()) + "%)");
        }
        
        MessageUtils.sendMessage(player, "<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>");
        MessageUtils.sendMessage(player, "<gray>Usa <yellow>/skills info <tipo></yellow> para más detalles");
    }

    public void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSkills playerSkills = getPlayerSkills(player);
        String path = "players." + uuid.toString() + ".";

        for (SkillType type : SkillType.values()) {
            PlayerSkill skill = playerSkills.getSkill(type);
            skillsConfig.set(path + type.name() + ".level", skill.getLevel());
            skillsConfig.set(path + type.name() + ".exp", skill.getExp());
        }

        plugin.getConfigManager().saveSkillsConfig();
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSkills playerSkills = getPlayerSkills(player);
        String path = "players." + uuid.toString() + ".";

        if (!skillsConfig.contains("players." + uuid.toString())) {
            return; // No hay datos guardados para este jugador
        }

        for (SkillType type : SkillType.values()) {
            int level = skillsConfig.getInt(path + type.name() + ".level", 1);
            double exp = skillsConfig.getDouble(path + type.name() + ".exp", 0);
            
            PlayerSkill skill = playerSkills.getSkill(type);
            skill.setLevel(level);
            skill.setExp(exp);
        }
    }

    public void loadAllPlayerData() {
        if (!skillsConfig.contains("players")) return;

        for (String uuidStr : skillsConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                loadPlayerData(player);
            }
        }
    }

    // Eventos para ganar experiencia
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Minería
        if (isPickaxe(tool) && isOre(block.getType())) {
            double exp = getMiningExp(block.getType());
            addExp(player, SkillType.MINING, exp);

            // Chance de doble drop
            PlayerSkills skills = getPlayerSkills(player);
            int level = skills.getLevel(SkillType.MINING);
            if (level >= 10 && Math.random() < (level / 10) * 0.1) {
                Collection<ItemStack> drops = block.getDrops(tool);
                for (ItemStack drop : drops) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }

        // Tala
        if (isAxe(tool) && isLog(block.getType())) {
            double exp = getWoodcuttingExp(block.getType());
            addExp(player, SkillType.WOODCUTTING, exp);

            // Chance de tala completa
            PlayerSkills skills = getPlayerSkills(player);
            int level = skills.getLevel(SkillType.WOODCUTTING);
            if (level >= 8 && Math.random() < (level / 8) * 0.1) {
                // Romper todos los logs conectados
                breakConnectedLogs(block);
            }
        }

        // Excavación
        if (isShovel(tool) && isExcavatable(block.getType())) {
            double exp = getForagingExp(block.getType());
            addExp(player, SkillType.FORAGING, exp);
        }

        // Agricultura
        if (isHoe(tool) && isCrop(block.getType())) {
            double exp = getFarmingExp(block.getType());
            addExp(player, SkillType.FARMING, exp);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            double exp = getCombatExp(event.getEntityType());
            addExp(killer, SkillType.COMBAT, exp);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            addExp(player, SkillType.FISHING, 1.0);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (isPotion(item)) {
            addExp(player, SkillType.ALCHEMY, 0.5);
        }
    }

    // Métodos auxiliares
    private boolean isPickaxe(ItemStack item) {
        return item != null && item.getType().name().contains("PICKAXE");
    }

    private boolean isAxe(ItemStack item) {
        return item != null && item.getType().name().contains("AXE");
    }

    private boolean isShovel(ItemStack item) {
        return item != null && item.getType().name().contains("SHOVEL");
    }

    private boolean isHoe(ItemStack item) {
        return item != null && item.getType().name().contains("HOE");
    }

    private boolean isOre(Material material) {
        return material == Material.COAL_ORE || material == Material.IRON_ORE ||
                material == Material.GOLD_ORE || material == Material.DIAMOND_ORE ||
                material == Material.EMERALD_ORE || material == Material.LAPIS_ORE ||
                material == Material.REDSTONE_ORE || material == Material.NETHER_QUARTZ_ORE;
    }

    private boolean isLog(Material material) {
        return material == Material.OAK_LOG || material == Material.BIRCH_LOG ||
                material == Material.SPRUCE_LOG || material == Material.JUNGLE_LOG ||
                material == Material.DARK_OAK_LOG || material == Material.ACACIA_LOG;
    }

    private boolean isExcavatable(Material material) {
        return material == Material.DIRT || material == Material.GRASS_BLOCK ||
                material == Material.SAND || material == Material.GRAVEL ||
                material == Material.CLAY;
    }

    private boolean isCrop(Material material) {
        return material == Material.WHEAT || material == Material.CARROTS ||
                material == Material.POTATOES || material == Material.BEETROOTS ||
                material == Material.NETHER_WART;
    }

    private boolean isPotion(ItemStack item) {
        return item != null && (item.getType() == Material.POTION ||
                item.getType() == Material.SPLASH_POTION ||
                item.getType() == Material.LINGERING_POTION);
    }

    private double getMiningExp(Material material) {
        switch (material) {
            case COAL_ORE: return 1.0;
            case IRON_ORE: return 2.0;
            case GOLD_ORE: return 3.0;
            case DIAMOND_ORE: return 5.0;
            case EMERALD_ORE: return 7.0;
            default: return 0.5;
        }
    }

    private double getWoodcuttingExp(Material material) {
        return 1.0; // Misma exp para todos los logs
    }

    private double getForagingExp(Material material) {
        switch (material) {
            case CLAY: return 2.0;
            case GRAVEL: return 1.0;
            default: return 0.5;
        }
    }

    private double getFarmingExp(Material material) {
        return 1.0; // Misma exp para todos los cultivos
    }

    private double getCombatExp(EntityType entityType) {
        switch (entityType) {
            case ZOMBIE: return 2.0;
            case SKELETON: return 2.0;
            case SPIDER: return 2.0;
            case CREEPER: return 3.0;
            case ENDERMAN: return 5.0;
            case WITCH: return 4.0;
            default: return 1.0;
        }
    }

    private void breakConnectedLogs(Block startBlock) {
        // Implementación simplificada de tala completa
        // En una implementación real, necesitarías un algoritmo más complejo
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = startBlock.getRelative(x, y, z);
                    if (isLog(block.getType())) {
                        block.breakNaturally();
                    }
                }
            }
        }
    }

    private String formatSkillName(String skillName) {
        return skillName.charAt(0) + skillName.substring(1).toLowerCase().replace("_", " ");
    }

    // Missing methods needed by other classes
    public void addExperience(UUID playerId, SkillType skillType, int amount) {

    }

    private void savePlayerData(UUID playerId) {
    }

    public SkillType getSkillFromName(String name) {
        try {
            return SkillType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (SkillType type : SkillType.values()) {
                if (type.getDisplayName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}