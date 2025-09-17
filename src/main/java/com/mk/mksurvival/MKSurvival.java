package com.mk.mksurvival;

import com.mk.mksurvival.listeners.factions.FactionListener;
import com.mk.mksurvival.listeners.items.ItemQualityListener;
import com.mk.mksurvival.listeners.land.LandMoveListener;
import com.mk.mksurvival.managers.land.LandManager;
import com.mk.mksurvival.managers.land.LandSelectionManager;
import com.mk.mksurvival.managers.items.ItemQualityManager;
import com.mk.mksurvival.managers.enemies.EnemyLevelManager;
import com.mk.mksurvival.managers.experience.ExpBarManager;
import com.mk.mksurvival.commands.experience.ExpBarCommand;
import com.mk.mksurvival.listeners.enemies.EnemySystemListener;
import com.mk.mksurvival.listeners.experience.ExperienceListener;
import com.mk.mksurvival.commands.enemies.EnemyCommand;
// Organized managers by system
import com.mk.mksurvival.managers.economy.EconomyManager;
import com.mk.mksurvival.managers.rewards.RewardManager;
import com.mk.mksurvival.managers.rankings.RankingManager;
import com.mk.mksurvival.managers.particles.ParticleManager;
import com.mk.mksurvival.managers.config.ConfigManager;
import com.mk.mksurvival.managers.utils.CooldownManager;
import com.mk.mksurvival.managers.utils.ChatInputManager;
import com.mk.mksurvival.managers.bosses.BossManager;
import com.mk.mksurvival.managers.classes.ClassManager;
import com.mk.mksurvival.managers.dungeons.DungeonManager;
import com.mk.mksurvival.managers.pets.PetManager;
import com.mk.mksurvival.managers.quests.QuestManager;
import com.mk.mksurvival.managers.quests.DynamicQuestManager;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.skills.SpecializationManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.mk.mksurvival.managers.factions.FactionManager;
// Import specific listener classes from organized packages
import com.mk.mksurvival.listeners.classes.ClassListener;
import com.mk.mksurvival.listeners.quests.QuestListener;
import com.mk.mksurvival.listeners.quests.DynamicQuestListener;
import com.mk.mksurvival.listeners.skills.SkillListener;
import com.mk.mksurvival.listeners.skills.SkillSpecializationListener;
import com.mk.mksurvival.listeners.skills.SpecializationGUIListener;
import com.mk.mksurvival.listeners.pets.PetListener;
import com.mk.mksurvival.listeners.economy.EconomyListener;
import com.mk.mksurvival.listeners.menu.MenuListener;
// Import specific command classes from organized packages
import com.mk.mksurvival.commands.classes.ClassCommand;
import com.mk.mksurvival.commands.quests.QuestCommand;
import com.mk.mksurvival.commands.quests.DynamicQuestCommand;
import com.mk.mksurvival.commands.skills.SkillsCommand;
import com.mk.mksurvival.commands.skills.SkillSpecializationCommand;
import com.mk.mksurvival.commands.pets.PetCommand;
import com.mk.mksurvival.commands.economy.EconomyCommand;
import com.mk.mksurvival.commands.dungeons.DungeonCommand;
import com.mk.mksurvival.commands.MainCommand;
import com.mk.mksurvival.commands.LandCommand;
import com.mk.mksurvival.commands.FactionCommand;
import com.mk.mksurvival.commands.ItemQualityCommand;

public class MKSurvival extends JavaPlugin {
    private static MKSurvival instance;

    // Managers
    private LandManager landManager;
    private SkillManager skillManager;
    private QuestManager questManager;
    private ClassManager classManager;
    private BossManager bossManager;
    private EnemyLevelManager enemyLevelManager;
    private DungeonManager dungeonManager;
    private ConfigManager configManager;
    private EconomyManager economyManager;
    private CooldownManager cooldownManager;
    private ParticleManager particleManager;
    private LandSelectionManager landSelectionManager;
    private FactionManager factionManager;
    private PetManager petManager;
    private ItemQualityManager itemQualityManager;
    private SpecializationManager specializationManager;
    private DynamicQuestManager dynamicQuestManager;
    private ChatInputManager chatInputManager;
    private RewardManager rewardManager;
    private ExpBarManager expBarManager;

    // Comandos
    private QuestCommand questCommand;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Inicializar managers en orden correcto
            this.configManager = new ConfigManager(this);
            this.economyManager = new EconomyManager(this);
            this.landManager = new LandManager(this);
            this.skillManager = new SkillManager(this);

            // Inicializar FactionManager con manejo de errores
            try {
                this.factionManager = new FactionManager(this);
            } catch (Exception e) {
                getLogger().severe("Error al inicializar FactionManager: " + e.getMessage());
                e.printStackTrace();
                // Continuar sin FactionManager si hay errores
            }

            this.petManager = new PetManager(this);

            // Inicializar QuestManager con manejo de errores
            try {
                this.questManager = new QuestManager(this);
            } catch (Exception e) {
                getLogger().severe("Error al inicializar QuestManager: " + e.getMessage());
                e.printStackTrace();
                // Continuar sin QuestManager si hay errores
            }

            this.classManager = new ClassManager(this);
            this.bossManager = new BossManager(this);
            this.enemyLevelManager = new EnemyLevelManager(this);
            this.rewardManager = new RewardManager(this);
            this.dungeonManager = new DungeonManager(this);
            this.cooldownManager = new CooldownManager(this);
            this.itemQualityManager = new ItemQualityManager(this);
            this.specializationManager = new SpecializationManager(this);
            this.dynamicQuestManager = new DynamicQuestManager(this);
            this.particleManager = new ParticleManager(this);
            this.chatInputManager = new ChatInputManager(this);
            this.landSelectionManager = new LandSelectionManager(this);
            this.expBarManager = new ExpBarManager(this);

            // Inicializar comandos después de los managers
            if (this.questManager != null) {
                this.questCommand = new QuestCommand(this);
                this.questCommand.initialize();
            }

            // Registrar comandos
            getCommand("mksurvival").setExecutor(new MainCommand());
            getCommand("balance").setExecutor(new EconomyCommand());
            getCommand("land").setExecutor(new LandCommand(this));
            getCommand("class").setExecutor(new ClassCommand(this));

            // Registrar comando de quests con TabCompleter (solo si questManager está disponible)
            if (this.questManager != null) {
                getCommand("quests").setExecutor(this.questCommand);
                getCommand("quests").setTabCompleter(this.questCommand);
            } else {
                getLogger().warning("QuestCommand no registrado porque QuestManager no está disponible");
            }

            // Registrar comando de skills con TabCompleter
            SkillsCommand skillsCommand = new SkillsCommand(this);
            getCommand("skills").setExecutor(skillsCommand);
            getCommand("skills").setTabCompleter(skillsCommand);

            getCommand("dungeon").setExecutor(new DungeonCommand(this));

            // Registrar comando de facción (solo si factionManager está disponible)
            if (this.factionManager != null) {
                getCommand("faccion").setExecutor(new FactionCommand(this));
            } else {
                getLogger().warning("FactionCommand no registrado porque FactionManager no está disponible");
            }

            getCommand("mascota").setExecutor(new PetCommand(this));
            getCommand("calidad").setExecutor(new ItemQualityCommand(this));

            // Registrar comando de especialización con TabCompleter
            SkillSpecializationCommand specializationCommand = new SkillSpecializationCommand(this);
            getCommand("especializacion").setExecutor(specializationCommand);
            getCommand("especializacion").setTabCompleter(specializationCommand);

            getCommand("mision").setExecutor(new DynamicQuestCommand(this));

            // Registrar nuevos comandos mejorados
            ExpBarCommand expBarCommand = new ExpBarCommand(this);
            getCommand("expbar").setExecutor(expBarCommand);
            getCommand("expbar").setTabCompleter(expBarCommand);

            EnemyCommand enemyCommand = new EnemyCommand(this);
            getCommand("enemy").setExecutor(enemyCommand);
            getCommand("enemy").setTabCompleter(enemyCommand);

            // Registrar listeners
            getServer().getPluginManager().registerEvents(new MenuListener(), this);
            getServer().getPluginManager().registerEvents(new ClassListener(), this);
            getServer().getPluginManager().registerEvents(new EconomyListener(), this);

            // Registrar listeners de quests (solo si questManager está disponible)
            if (this.questManager != null) {
                getServer().getPluginManager().registerEvents(new QuestListener(), this);
            }

            getServer().getPluginManager().registerEvents(new SkillListener(), this);

            // Registrar listeners de facciones (solo si factionManager está disponible)
            if (this.factionManager != null) {
                getServer().getPluginManager().registerEvents(new FactionListener(this), this);
            }

            getServer().getPluginManager().registerEvents(new PetListener(this), this);
            getServer().getPluginManager().registerEvents(new ItemQualityListener(this), this);
            getServer().getPluginManager().registerEvents(new SpecializationGUIListener(this), this);
            getServer().getPluginManager().registerEvents(new DynamicQuestListener(this), this);
            getServer().getPluginManager().registerEvents(new LandMoveListener(this), this);
            getServer().getPluginManager().registerEvents(new com.mk.mksurvival.listeners.land.LandListener(), this);
            getServer().getPluginManager().registerEvents(new EnemySystemListener(this), this);
            getServer().getPluginManager().registerEvents(new ExperienceListener(this), this);

            // Cargar datos de jugadores
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                this.classManager.loadAllPlayerData();
            }

            getLogger().info("MKSurvival ha sido habilitado correctamente.");
        } catch (Exception e) {
            getLogger().severe("Error crítico al habilitar MKSurvival: " + e.getMessage());
            e.printStackTrace();

            // Deshabilitar el plugin si ocurre un error crítico
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Guardar datos de facciones (solo si factionManager está disponible)
            if (factionManager != null) {
                factionManager.saveFactions();
            } else {
                getLogger().warning("FactionManager no está disponible, no se pueden guardar los datos de facciones");
            }

            // Guardar datos de misiones con comprobación de nulidad
            if (questManager != null) {
                questManager.saveAllPlayerData();
            } else {
                getLogger().warning("QuestManager no está disponible, no se pueden guardar los datos de misiones");
            }

            // Guardar especializaciones (solo si specializationManager está disponible)
            if (specializationManager != null) {
                specializationManager.saveAllPlayerSpecializations();
            } else {
                getLogger().warning("SpecializationManager no está disponible, no se pueden guardar las especializaciones");
            }

            // Guardar datos de clases
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                if (classManager != null) {
                    this.classManager.saveAllPlayerData();
                }
            }

            getLogger().info("MKSurvival ha sido deshabilitado correctamente.");
        } catch (Exception e) {
            getLogger().severe("Error al deshabilitar MKSurvival: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters para los managers
    public static MKSurvival getInstance() {
        return instance;
    }

    public LandManager getLandManager() {
        return landManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public EnemyLevelManager getEnemyLevelManager() {
        return enemyLevelManager;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public ItemQualityManager getItemQualityManager() {
        return itemQualityManager;
    }

    public SpecializationManager getSpecializationManager() {
        return specializationManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public FactionManager getFactionManager() {
        return factionManager;
    }

    public ExpBarManager getExpBarManager() {
        return expBarManager;
    }

    public PetManager getPetManager() {
        return petManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public LandSelectionManager getLandSelectionManager() {
        return landSelectionManager;
    }

    public DynamicQuestManager getDynamicQuestManager() {
        return dynamicQuestManager;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }
}