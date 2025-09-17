package com.mk.mksurvival.managers.quests;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.managers.rewards.RewardManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DynamicQuestManager implements Listener {
    private final MKSurvival plugin;
    private final Map<UUID, List<DynamicQuest>> playerQuests = new HashMap<>();
    private final Map<UUID, Long> questCooldowns = new HashMap<>();
    private final Map<UUID, Set<String>> playerBiomes = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> questProgress = new HashMap<>();
    private final Map<UUID, Long> lastQuestGeneration = new HashMap<>();
    private final Random random = new Random();
    private final List<QuestTemplate> questTemplates = new ArrayList<>();
    private final Map<String, QuestChain> questChains = new HashMap<>();
    private FileConfiguration questConfig;
    private File questFile;

    public DynamicQuestManager(MKSurvival plugin) {
        this.plugin = plugin;
        setupConfig();
        setupQuestTemplates();
        setupQuestChains();
        loadQuests();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startQuestRefreshSystem();
        startDailyQuestSystem();
    }

    private void setupQuestChains() {
        // Initialize quest chains - placeholder implementation
    }

    private void startQuestRefreshSystem() {
        // Start quest refresh system - placeholder implementation
    }

    private void startDailyQuestSystem() {
        // Start daily quest system - placeholder implementation
    }

    private void setupConfig() {
        questFile = new File(plugin.getDataFolder(), "dynamic_quests.yml");
        if (!questFile.exists()) {
            plugin.saveResource("dynamic_quests.yml", false);
        }
        questConfig = YamlConfiguration.loadConfiguration(questFile);
    }

    private void setupQuestTemplates() {
        // ===== HUNTING QUESTS =====
        questTemplates.add(new QuestTemplate(
                "hunt_basic", "Cazador Novato", "Elimina {amount} {mob_type}",
                QuestType.HUNT, 1, 3,
                new HashMap<String, Object>() {{
                    put("mob_type", EntityType.ZOMBIE);
                    put("amount", 10);
                }},
                createDynamicReward("hunt_basic", 1)
        ));
        
        questTemplates.add(new QuestTemplate(
                "hunt_elite", "Cazador Elite", "Elimina {amount} {mob_type} durante la noche",
                QuestType.HUNT_ELITE, 15, 10,
                new HashMap<String, Object>() {{
                    put("mob_type", EntityType.SKELETON);
                    put("amount", 25);
                    put("time_requirement", "night");
                }},
                createDynamicReward("hunt_elite", 15)
        ));
        
        questTemplates.add(new QuestTemplate(
                "hunt_boss", "Asesino de Jefes", "Derrota {amount} jefes",
                QuestType.HUNT_BOSS, 30, 15,
                new HashMap<String, Object>() {{
                    put("amount", 3);
                    put("boss_type", "any");
                }},
                createDynamicReward("hunt_boss", 30)
        ));
        
        // ===== COLLECTION QUESTS =====
        questTemplates.add(new QuestTemplate(
                "collect_basic", "Recolector", "Recolecta {amount} {block_type}",
                QuestType.COLLECT, 1, 2,
                new HashMap<String, Object>() {{
                    put("block_type", Material.IRON_ORE);
                    put("amount", 15);
                }},
                createDynamicReward("collect_basic", 1)
        ));
        
        questTemplates.add(new QuestTemplate(
                "collect_rare", "Buscador de Tesoros", "Recolecta {amount} {block_type} raros",
                QuestType.COLLECT_RARE, 20, 8,
                new HashMap<String, Object>() {{
                    put("block_type", Material.DIAMOND_ORE);
                    put("amount", 5);
                    put("rarity", "rare");
                }},
                createDynamicReward("collect_rare", 20)
        ));
        
        questTemplates.add(new QuestTemplate(
                "collect_massive", "Excavador Masivo", "Recolecta {amount} bloques de cualquier mineral",
                QuestType.COLLECT_MASSIVE, 10, 6,
                new HashMap<String, Object>() {{
                    put("amount", 100);
                    put("block_types", Arrays.asList(Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE));
                }},
                createDynamicReward("collect_massive", 10)
        ));
        
        // ===== DELIVERY QUESTS =====
        questTemplates.add(new QuestTemplate(
                "deliver_basic", "Mensajero", "Entrega {amount} {item_type} al NPC",
                QuestType.DELIVER, 1, 4,
                new HashMap<String, Object>() {{
                    put("item_type", Material.WHEAT);
                    put("amount", 20);
                    put("npc_location", "village");
                }},
                createDynamicReward("deliver_basic", 1)
        ));
        
        questTemplates.add(new QuestTemplate(
                "deliver_distance", "Comerciante de Larga Distancia", "Entrega {amount} {item_type} a un NPC lejano",
                QuestType.DELIVER_DISTANCE, 12, 8,
                new HashMap<String, Object>() {{
                    put("item_type", Material.EMERALD);
                    put("amount", 5);
                    put("min_distance", 1000);
                }},
                createDynamicReward("deliver_distance", 12)
        ));
        
        // ===== EXPLORATION QUESTS =====
        questTemplates.add(new QuestTemplate(
                "explore_biomes", "Explorador de Biomas", "Visita {amount} biomas diferentes",
                QuestType.EXPLORE_BIOMES, 5, 12,
                new HashMap<String, Object>() {{
                    put("amount", 4);
                    put("biome_types", Arrays.asList("forest", "desert", "plains", "mountains"));
                }},
                createDynamicReward("explore_biomes", 5)
        ));
        
        questTemplates.add(new QuestTemplate(
                "explore_structures", "Descubridor de Estructuras", "Encuentra {amount} estructuras",
                QuestType.EXPLORE_STRUCTURES, 18, 15,
                new HashMap<String, Object>() {{
                    put("amount", 3);
                    put("structure_types", Arrays.asList("village", "dungeon", "stronghold"));
                }},
                createDynamicReward("explore_structures", 18)
        ));
        
        // ===== SKILL-BASED QUESTS =====
        questTemplates.add(new QuestTemplate(
                "skill_fishing", "Maestro Pescador", "Pesca {amount} peces",
                QuestType.SKILL_FISHING, 3, 5,
                new HashMap<String, Object>() {{
                    put("amount", 15);
                    put("fish_type", "any");
                }},
                createDynamicReward("skill_fishing", 3)
        ));
        
        questTemplates.add(new QuestTemplate(
                "skill_farming", "Agricultor Experimentado", "Cosecha {amount} cultivos",
                QuestType.SKILL_FARMING, 6, 4,
                new HashMap<String, Object>() {{
                    put("amount", 50);
                    put("crop_types", Arrays.asList(Material.WHEAT, Material.CARROT, Material.POTATO));
                }},
                createDynamicReward("skill_farming", 6)
        ));
        
        questTemplates.add(new QuestTemplate(
                "skill_building", "Maestro Constructor", "Construye una estructura de {amount} bloques",
                QuestType.SKILL_BUILDING, 8, 10,
                new HashMap<String, Object>() {{
                    put("amount", 200);
                    put("structure_type", "house");
                }},
                createDynamicReward("skill_building", 8)
        ));
        
        // ===== FACTION QUESTS =====
        questTemplates.add(new QuestTemplate(
                "faction_war", "Guerrero de Facción", "Participa en {amount} guerras de facción",
                QuestType.FACTION_WAR, 25, 20,
                new HashMap<String, Object>() {{
                    put("amount", 2);
                    put("role", "participant");
                }},
                createDynamicReward("faction_war", 25)
        ));
        
        questTemplates.add(new QuestTemplate(
                "faction_territory", "Conquistador", "Conquista {amount} territorios",
                QuestType.FACTION_TERRITORY, 35, 25,
                new HashMap<String, Object>() {{
                    put("amount", 3);
                    put("territory_type", "enemy");
                }},
                createDynamicReward("faction_territory", 35)
        ));
        
        // ===== DAILY/WEEKLY SPECIAL QUESTS =====
        questTemplates.add(new QuestTemplate(
                "daily_challenge", "Desafío Diario", "Completa {amount} misiones menores hoy",
                QuestType.DAILY_CHALLENGE, 10, 1,
                new HashMap<String, Object>() {{
                    put("amount", 3);
                    put("quest_types", Arrays.asList("hunt", "collect", "deliver"));
                }},
                createDynamicReward("daily_challenge", 10)
        ));
        
        questTemplates.add(new QuestTemplate(
                "weekly_epic", "Desafío Épico Semanal", "Logra {amount} objetivos épicos esta semana",
                QuestType.WEEKLY_EPIC, 50, 7,
                new HashMap<String, Object>() {{
                    put("amount", 5);
                    put("objectives", Arrays.asList("kill_boss", "explore_biome", "craft_legendary"));
                }},
                createDynamicReward("weekly_epic", 50)
        ));
    }
    
    private RewardManager.Reward createDynamicReward(String questType, int level) {
        double baseMoneyReward = 50.0 * level;
        int baseExpReward = 25 * level;
        double multiplier = 1.0 + (level * 0.1);
        
        List<ItemStack> items = new ArrayList<>();
        
        switch (questType) {
            case "hunt_basic":
            case "hunt_elite":
                items.add(new ItemStack(Material.IRON_SWORD, 1));
                items.add(new ItemStack(Material.COOKED_BEEF, 5 + level));
                break;
            case "hunt_boss":
                items.add(new ItemStack(Material.DIAMOND_SWORD, 1));
                items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
                baseMoneyReward *= 3;
                baseExpReward *= 4;
                break;
            case "collect_basic":
            case "collect_massive":
                items.add(new ItemStack(Material.IRON_PICKAXE, 1));
                items.add(new ItemStack(Material.TORCH, 10 + level));
                break;
            case "collect_rare":
                items.add(new ItemStack(Material.DIAMOND_PICKAXE, 1));
                items.add(new ItemStack(Material.EXPERIENCE_BOTTLE, level));
                baseMoneyReward *= 2;
                break;
            case "deliver_basic":
            case "deliver_distance":
                items.add(new ItemStack(Material.EMERALD, level));
                items.add(new ItemStack(Material.BREAD, 10));
                break;
            case "explore_biomes":
            case "explore_structures":
                items.add(new ItemStack(Material.MAP, 1));
                items.add(new ItemStack(Material.COMPASS, 1));
                items.add(new ItemStack(Material.ENDER_PEARL, level / 5 + 1));
                break;
            case "skill_fishing":
                items.add(new ItemStack(Material.FISHING_ROD, 1));
                items.add(new ItemStack(Material.COOKED_COD, level));
                break;
            case "skill_farming":
                items.add(new ItemStack(Material.DIAMOND_HOE, 1));
                items.add(new ItemStack(Material.BONE_MEAL, level * 2));
                break;
            case "skill_building":
                items.add(new ItemStack(Material.STONE_BRICKS, level * 10));
                items.add(new ItemStack(Material.GLASS, level * 5));
                break;
            case "faction_war":
            case "faction_territory":
                items.add(new ItemStack(Material.NETHERITE_SWORD, 1));
                items.add(new ItemStack(Material.GOLDEN_APPLE, level));
                baseMoneyReward *= 2.5;
                baseExpReward *= 3;
                break;
            case "daily_challenge":
                items.add(new ItemStack(Material.EXPERIENCE_BOTTLE, level));
                items.add(new ItemStack(Material.EMERALD, level));
                break;
            case "weekly_epic":
                items.add(new ItemStack(Material.NETHERITE_INGOT, 1));
                items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
                items.add(new ItemStack(Material.EXPERIENCE_BOTTLE, level * 2));
                baseMoneyReward *= 5;
                baseExpReward *= 10;
                break;
            default:
                items.add(new ItemStack(Material.IRON_INGOT, level));
        }
        
        return new RewardManager.Reward(
                "Recompensa: " + questType.replace("_", " "),
                multiplier,
                items,
                baseMoneyReward,
                baseExpReward
        );
    }

    private void loadQuests() {
        if (questConfig.contains("player_quests")) {
            for (String uuidStr : questConfig.getConfigurationSection("player_quests").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<DynamicQuest> quests = new ArrayList<>();
                if (questConfig.contains("player_quests." + uuidStr + ".quests")) {
                    for (String questId : questConfig.getConfigurationSection("player_quests." + uuidStr + ".quests").getKeys(false)) {
                        String path = "player_quests." + uuidStr + ".quests." + questId + ".";
                        String name = questConfig.getString(path + "name");
                        String description = questConfig.getString(path + "description");
                        QuestType type = QuestType.valueOf(questConfig.getString(path + "type"));
                        int level = questConfig.getInt(path + "level");
                        Map<String, Object> parameters = new HashMap<>();
                        if (questConfig.contains(path + "parameters")) {
                            for (String param : questConfig.getConfigurationSection(path + "parameters").getKeys(false)) {
                                parameters.put(param, questConfig.get(path + "parameters." + param));
                            }
                        }
                        RewardManager.QuestReward reward = new RewardManager.QuestReward(
                                questConfig.getString(path + "reward.name"),
                                questConfig.getDouble(path + "reward.multiplier"),
                                new ArrayList<>(),
                                questConfig.getDouble(path + "reward.money"),
                                questConfig.getInt(path + "reward.exp")
                        );
                        DynamicQuest quest = new DynamicQuest(questId, name, description, type, level, parameters,reward);
                        quests.add(quest);
                    }
                }
                playerQuests.put(uuid, quests);
            }
        }
    }

    public void saveQuests() {
        questConfig.set("player_quests", null);
        for (Map.Entry<UUID, List<DynamicQuest>> entry : playerQuests.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (DynamicQuest quest : entry.getValue()) {
                String path = "player_quests." + uuidStr + ".quests." + quest.getId() + ".";
                questConfig.set(path + "name", quest.getName());
                questConfig.set(path + "description", quest.getDescription());
                questConfig.set(path + "type", quest.getType().name());
                questConfig.set(path + "level", quest.getLevel());
                for (Map.Entry<String, Object> param : quest.getParameters().entrySet()) {
                    questConfig.set(path + "parameters." + param.getKey(), param.getValue());
                }
                questConfig.set(path + "reward.name", quest.getReward().name);
                questConfig.set(path + "reward.multiplier", quest.getReward().multiplier);
                questConfig.set(path + "reward.money", quest.getReward().money);
                questConfig.set(path + "reward.exp", quest.getReward().exp);
            }
        }
        try {
            questConfig.save(questFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save dynamic_quests.yml");
            e.printStackTrace();
        }
    }

    public DynamicQuest generateRandomQuest(Player player) {
        return generateSmartQuest(player);
    }
    
    public DynamicQuest generateSmartQuest(Player player) {
        if (questCooldowns.containsKey(player.getUniqueId())) {
            long cooldownTime = questCooldowns.get(player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            long timeLeft = (cooldownTime - currentTime) / 1000;
            if (timeLeft > 0) {
                MessageUtils.sendMessage(player, "<red>Debes esperar " + timeLeft + " segundos antes de generar otra misión.");
                return null;
            }
        }
        
        // Smart quest selection based on player profile
        List<QuestTemplate> suitableQuests = getSuitableQuests(player);
        if (suitableQuests.isEmpty()) {
            suitableQuests = questTemplates; // Fallback to all quests
        }
        
        QuestTemplate template = suitableQuests.get(random.nextInt(suitableQuests.size()));
        DynamicQuest quest = createQuestFromTemplate(template);
        
        customizeQuestParameters(quest);
        questCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + getQuestCooldown(player));
        return quest;
    }
    
    private List<QuestTemplate> getSuitableQuests(Player player) {
        List<QuestTemplate> suitable = new ArrayList<>();
        int playerLevel = plugin.getSkillManager().getPlayerSkills(player.getUniqueId()).getTotalLevel();
        
        for (QuestTemplate template : questTemplates) {
            // Check level requirements
            if (template.getRequiredLevel() <= playerLevel) {
                // Check if player hasn't done this type recently
                if (!hasRecentQuestOfType(player, template.getType())) {
                    // Check special requirements
                    if (meetsSpecialRequirements(player, template)) {
                        suitable.add(template);
                    }
                }
            }
        }
        
        return suitable;
    }
    
    private boolean hasRecentQuestOfType(Player player, QuestType type) {
        if (!playerQuests.containsKey(player.getUniqueId())) return false;
        
        return playerQuests.get(player.getUniqueId()).stream()
                .anyMatch(quest -> quest.getType() == type);
    }
    
    private boolean meetsSpecialRequirements(Player player, QuestTemplate template) {
        switch (template.getType()) {
            case FACTION_WAR:
            case FACTION_TERRITORY:
                // Check if player is in a faction
                return plugin.getFactionManager().getPlayerFaction(player.getUniqueId()) != null;
            case HUNT_BOSS:
                // Check if player has sufficient combat level
                return plugin.getSkillManager().getPlayerSkills(player.getUniqueId()).getLevel(SkillType.COMBAT) >= 20;
            case COLLECT_RARE:
                // Check mining level
                return plugin.getSkillManager().getPlayerSkills(player.getUniqueId()).getLevel(SkillType.MINING) >= 15;
            default:
                return true;
        }
    }
    
    private long getQuestCooldown(Player player) {
        int playerLevel = plugin.getSkillManager().getPlayerSkills(player.getUniqueId()).getTotalLevel();
        long baseCooldown = 300000; // 5 minutes
        
        // Reduce cooldown for higher level players
        long reduction = (playerLevel / 10) * 30000; // 30 seconds per 10 levels
        return Math.max(60000, baseCooldown - reduction); // Minimum 1 minute
    }
    
    private DynamicQuest createQuestFromTemplate(QuestTemplate template) {
        return new DynamicQuest(
                UUID.randomUUID().toString(),
                template.getName(),
                template.getDescription(),
                template.getType(),
                template.getRequiredLevel(),
                new HashMap<>(template.getParameters()),
                template.getReward()
        );
    }

    public void customizeQuestParameters(DynamicQuest quest) {
        switch (quest.getType()) {
            case HUNT:
                EntityType[] mobs = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER};
                quest.getParameters().put("mob_type", mobs[random.nextInt(mobs.length)]);
                quest.getParameters().put("amount", 5 + random.nextInt(15));
                break;
            case COLLECT:
                Material[] blocks = {Material.IRON_ORE, Material.COAL_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE};
                quest.getParameters().put("block_type", blocks[random.nextInt(blocks.length)]);
                quest.getParameters().put("amount", 5 + random.nextInt(20));
                break;
            case DELIVER:
                Material[] items = {Material.WHEAT, Material.CARROT, Material.POTATO, Material.BEETROOT};
                quest.getParameters().put("item_type", items[random.nextInt(items.length)]);
                quest.getParameters().put("amount", 10 + random.nextInt(30));
                break;
            case EXPLORE:
                quest.getParameters().put("amount", 2 + random.nextInt(4));
                break;
        }
        quest.setDescription(generateQuestDescription(quest));
    }

    private String generateQuestDescription(DynamicQuest quest) {
        String description = quest.getDescription();
        for (Map.Entry<String, Object> entry : quest.getParameters().entrySet()) {
            description = description.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return description;
    }

    public boolean acceptQuest(Player player, String questId) {
        if (!playerQuests.containsKey(player.getUniqueId())) {
            playerQuests.put(player.getUniqueId(), new ArrayList<>());
        }
        if (playerQuests.get(player.getUniqueId()).size() >= 3) {
            MessageUtils.sendMessage(player, "<red>Ya tienes el máximo de misiones activas (3).");
            return false;
        }
        for (DynamicQuest activeQuest : playerQuests.get(player.getUniqueId())) {
            if (activeQuest.getId().equals(questId)) {
                MessageUtils.sendMessage(player, "<red>Ya tienes esta misión activa.");
                return false;
            }
        }

        // Buscar la misión en las plantillas disponibles
        DynamicQuest questToAdd = null;
        for (QuestTemplate template : questTemplates) {
            DynamicQuest quest = new DynamicQuest(
                    UUID.randomUUID().toString(),
                    template.getName(),
                    template.getDescription(),
                    template.getType(),
                    template.getLevel(),
                    new HashMap<>(template.getParameters()),
                    template.getReward()
            );
            customizeQuestParameters(quest);
            if (quest.getId().equals(questId)) {
                questToAdd = quest;
                break;
            }
        }

        if (questToAdd == null) {
            MessageUtils.sendMessage(player, "<red>No se encontró la misión especificada.");
            return false;
        }

        playerQuests.get(player.getUniqueId()).add(questToAdd);
        MessageUtils.sendMessage(player, "<green>Has aceptado la misión: <yellow>" + questToAdd.getName());
        MessageUtils.sendMessage(player, "<gray>" + questToAdd.getDescription());
        saveQuests();
        return true;
    }

    public boolean abandonQuest(Player player, String questId) {
        if (!playerQuests.containsKey(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>No tienes misiones activas.");
            return false;
        }

        DynamicQuest questToRemove = null;
        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            if (quest.getId().equals(questId)) {
                questToRemove = quest;
                break;
            }
        }

        if (questToRemove == null) {
            MessageUtils.sendMessage(player, "<red>No tienes esa misión activa.");
            return false;
        }

        playerQuests.get(player.getUniqueId()).remove(questToRemove);
        MessageUtils.sendMessage(player, "<green>Has abandonado la misión: <yellow>" + questToRemove.getName());
        saveQuests();
        return true;
    }

    public void completeQuest(Player player, String questId) {
        if (!playerQuests.containsKey(player.getUniqueId())) {
            return;
        }

        DynamicQuest questToComplete = null;
        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            if (quest.getId().equals(questId)) {
                questToComplete = quest;
                break;
            }
        }

        if (questToComplete == null) {
            return;
        }

        plugin.getRewardManager().giveReward(player, questToComplete.getReward().name);
        MessageUtils.sendMessage(player, "<green>Has completado la misión: <yellow>" + questToComplete.getName());
        MessageUtils.sendMessage(player, "<gray>Has recibido una recompensa.");
        playerQuests.get(player.getUniqueId()).remove(questToComplete);
        saveQuests();
    }

    public void showActiveQuests(Player player) {
        if (!playerQuests.containsKey(player.getUniqueId()) || playerQuests.get(player.getUniqueId()).isEmpty()) {
            MessageUtils.sendMessage(player, "<red>No tienes misiones activas.");
            return;
        }

        MessageUtils.sendMessage(player, "<gold>--- Misiones Activas ---");
        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<yellow>" + quest.getName());
            MessageUtils.sendMessage(player, "<gray>" + quest.getDescription());
            MessageUtils.sendMessage(player, "<gray>Progreso: " + getQuestProgress(player, quest));
            MessageUtils.sendMessage(player, "<green>Usa <white>/mision abandonar " + quest.getId() + " <green>para abandonar.");
            MessageUtils.sendMessage(player, "");
        }
    }

    public String getQuestProgress(Player player, DynamicQuest quest) {
        String progressKey = player.getUniqueId().toString() + "_" + quest.getId();
        if (!questConfig.contains("progress." + progressKey)) {
            questConfig.set("progress." + progressKey, 0);
            try {
                questConfig.save(questFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "0/" + quest.getParameters().get("amount");
        }
        int current = questConfig.getInt("progress." + progressKey);
        int required = (int) quest.getParameters().get("amount");
        return current + "/" + required;
    }

    public void updateQuestProgress(Player player, DynamicQuest quest, int amount) {
        String progressKey = player.getUniqueId().toString() + "_" + quest.getId();
        int current = questConfig.getInt("progress." + progressKey, 0);
        int required = (int) quest.getParameters().get("amount");
        if (current < required) {
            current = Math.min(current + amount, required);
            questConfig.set("progress." + progressKey, current);
            try {
                questConfig.save(questFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (current >= required) {
                completeQuest(player, quest.getId());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;
        Player player = event.getEntity().getKiller();
        if (!playerQuests.containsKey(player.getUniqueId())) return;

        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            if (quest.getType() == QuestType.HUNT &&
                    event.getEntityType() == quest.getParameters().get("mob_type")) {
                updateQuestProgress(player, quest, 1);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = event.getPlayer();
        if (!playerQuests.containsKey(player.getUniqueId())) return;

        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            if (quest.getType() == QuestType.COLLECT &&
                    event.getBlock().getType() == quest.getParameters().get("block_type")) {
                updateQuestProgress(player, quest, 1);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = event.getPlayer();
        if (!playerQuests.containsKey(player.getUniqueId())) return;

        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            if (quest.getType() == QuestType.FISHING) {
                updateQuestProgress(player, quest, 1);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = event.getPlayer();
        if (!playerQuests.containsKey(player.getUniqueId())) return;

        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            if (quest.getType() == QuestType.DELIVER &&
                    event.getItem().getType() == quest.getParameters().get("item_type")) {
                updateQuestProgress(player, quest, 1);
            }
        }
    }

    public Map<UUID, List<DynamicQuest>> getPlayerQuests() {
        return playerQuests;
    }

    public List<QuestTemplate> getQuestTemplates() {
        return questTemplates;
    }

    public enum QuestType {
        HUNT, HUNT_ELITE, HUNT_BOSS, 
        COLLECT, COLLECT_RARE, COLLECT_MASSIVE,
        DELIVER, DELIVER_DISTANCE,
        EXPLORE_BIOMES, EXPLORE_STRUCTURES,
        SKILL_FISHING, SKILL_FARMING, SKILL_BUILDING,
        FACTION_WAR, FACTION_TERRITORY,
        DAILY_CHALLENGE, WEEKLY_EPIC,
        // Legacy types for compatibility
        EXPLORE, FISHING
    }

    public static class QuestChain {
        private final String id;
        private final String name;
        private final List<String> questIds = new ArrayList<>();
        private final Map<UUID, Integer> playerProgress = new HashMap<>();
        
        public QuestChain(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public void addQuest(String questId) {
            questIds.add(questId);
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public List<String> getQuestIds() { return questIds; }
        public Map<UUID, Integer> getPlayerProgress() { return playerProgress; }
        
        public int getPlayerProgress(UUID playerId) {
            return playerProgress.getOrDefault(playerId, 0);
        }
        
        public void advancePlayer(UUID playerId) {
            int current = getPlayerProgress(playerId);
            if (current < questIds.size()) {
                playerProgress.put(playerId, current + 1);
            }
        }
        
        public boolean isComplete(UUID playerId) {
            return getPlayerProgress(playerId) >= questIds.size();
        }
        
        public String getCurrentQuestId(UUID playerId) {
            int progress = getPlayerProgress(playerId);
            if (progress < questIds.size()) {
                return questIds.get(progress);
            }
            return null;
        }
    }
    
    public static class QuestTemplate {
        private final String id;
        private final String name;
        private final String description;
        private final QuestType type;
        private final int requiredLevel;
        private final int duration; // hours
        private final Map<String, Object> parameters;
        private final RewardManager.Reward reward;

        public QuestTemplate(String id, String name, String description, QuestType type, int requiredLevel, int duration,
                             Map<String, Object> parameters, RewardManager.Reward reward) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.requiredLevel = requiredLevel;
            this.duration = duration;
            this.parameters = parameters;
            this.reward = reward;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public QuestType getType() { return type; }
        public int getRequiredLevel() { return requiredLevel; }
        public int getLevel() { return requiredLevel; } // Alias for compatibility
        public int getDuration() { return duration; }
        public Map<String, Object> getParameters() { return parameters; }
        public RewardManager.Reward getReward() { return reward; }
    }

    public static class DynamicQuest {
        private String id = "";
        private String name;
        private String description;
        private QuestType type = null;
        private int level = 0;
        private Map<String, Object> parameters;
        private RewardManager.Reward reward = null;

        public DynamicQuest(String id, String name, String description, QuestType type, int level,
                            Map<String, Object> parameters, RewardManager.Reward reward) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.level = level;
            this.parameters = parameters;
            this.reward = reward;
        }

        public DynamicQuest(String questId, String name, String description, QuestType type, int level, Map<String, Object> parameters, RewardManager.QuestReward reward, String id, QuestType type1, int level1, Map<String, Object> parameters1, RewardManager.Reward reward1) {
            this.id = id;
            this.type = type1;
            this.level = level1;
            this.parameters = parameters1;
            this.reward = reward1;
        }

        public DynamicQuest(String string, String name, String description, QuestType type, int requiredLevel, HashMap<String, Object> parameters, RewardManager.Reward reward, String id, QuestType type1, int level) {

            this.id = id;
            this.type = type1;
            this.level = level;
        }

        public DynamicQuest(String questId, String name, String description, QuestType type, int level, Map<String, Object> parameters, RewardManager.QuestReward reward) {

        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public QuestType getType() { return type; }
        public int getLevel() { return level; }
        public Map<String, Object> getParameters() { return parameters; }
        public RewardManager.Reward getReward() { return reward; }
        
        // Additional quest status fields
        private long startTime = System.currentTimeMillis();
        private long expirationTime = startTime + (24 * 60 * 60 * 1000); // 24 hours default
        private boolean isExpired = false;
        
        public long getStartTime() { return startTime; }
        public long getExpirationTime() { return expirationTime; }
        public void setExpirationTime(long expirationTime) { this.expirationTime = expirationTime; }
        public boolean isExpired() { return System.currentTimeMillis() > expirationTime || isExpired; }
        public void setExpired(boolean expired) { this.isExpired = expired; }
    }
    
    // ===== MISSING HELPER METHODS =====
    
    public boolean isQuestComplete(Player player, DynamicQuest quest) {
        String progressKey = player.getUniqueId().toString() + "_" + quest.getId();
        int current = questConfig.getInt("progress." + progressKey, 0);
        int required = (int) quest.getParameters().getOrDefault("amount", 1);
        return current >= required;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!playerQuests.containsKey(player.getUniqueId())) return;
        
        // Track biome exploration
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        playerBiomes.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(currentBiome.name());
        
        // Update exploration quests
        for (DynamicQuest quest : playerQuests.get(player.getUniqueId())) {
            if (quest.getType() == QuestType.EXPLORE_BIOMES) {
                int biomesVisited = playerBiomes.get(player.getUniqueId()).size();
                int required = (int) quest.getParameters().get("amount");
                
                String progressKey = player.getUniqueId().toString() + "_" + quest.getId();
                questConfig.set("progress." + progressKey, Math.min(biomesVisited, required));
                
                if (biomesVisited >= required) {
                    completeQuest(player, quest.getId());
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        // Generate new quests when player levels up
        Player player = event.getPlayer();
        if (event.getNewLevel() > event.getOldLevel() && event.getNewLevel() % 5 == 0) {
            generateRandomQuest(player);
        }
    }
    
    // Enhanced quest progress tracking
    public void setQuestProgress(Player player, DynamicQuest quest, int progress) {
        String progressKey = player.getUniqueId().toString() + "_" + quest.getId();
        questConfig.set("progress." + progressKey, progress);
        saveQuests();
        
        int required = (int) quest.getParameters().getOrDefault("amount", 1);
        if (progress >= required) {
            completeQuest(player, quest.getId());
        } else {
            // Send progress update
            MessageUtils.sendMessage(player, "<green>[Progreso] " + quest.getName() + ": " + progress + "/" + required);
        }
    }
}