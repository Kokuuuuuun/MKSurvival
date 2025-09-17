package com.mk.mksurvival.managers.config;

import com.mk.mksurvival.MKSurvival;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class ConfigManager {
    private final MKSurvival plugin;
    private FileConfiguration config;
    private FileConfiguration skillsConfig;
    private FileConfiguration questsConfig;
    private FileConfiguration classesConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration petsConfig;
    private FileConfiguration landConfig;
    private FileConfiguration playerConfig;
    private FileConfiguration dynamicQuestsConfig;
    private FileConfiguration factionsConfig;



    private FileConfiguration qualitiesConfig;

    private FileConfiguration specializationsConfig;
    private FileConfiguration economyConfig;
    private FileConfiguration rewardsConfig;
    private FileConfiguration bossesConfig;
    private FileConfiguration dungeonsConfig;


    private File configFile;
    private File skillsFile;
    private File questsFile;
    private File classesFile;

    private File messagesFile;
    private File petsFile;
    private File landFile;
    private File playerFile;
    private File dynamicQuestsFile;
    private File factionsFile;

    private File qualitiesFile;

    private File specializationsFile;
    private File economyFile;
    private File rewardsFile;
    private File bossesFile;
    private File dungeonsFile;

    public ConfigManager(MKSurvival plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        // Crear plugin folder si no existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        // Crear archivos de configuración
        configFile = new File(plugin.getDataFolder(), "config.yml");
        skillsFile = new File(plugin.getDataFolder(), "skills.yml");
        questsFile = new File(plugin.getDataFolder(), "quests.yml");
        classesFile = new File(plugin.getDataFolder(), "classes.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        petsFile = new File(plugin.getDataFolder(), "pets.yml");
        landFile = new File(plugin.getDataFolder(), "land.yml");
        playerFile = new File(plugin.getDataFolder(), "player.yml");
        dynamicQuestsFile = new File(plugin.getDataFolder(), "dynamic_quests.yml");
        factionsFile = new File(plugin.getDataFolder(), "factions.yml");
        qualitiesFile = new File(plugin.getDataFolder(), "qualities.yml");
        specializationsFile = new File(plugin.getDataFolder(), "specializations.yml");
        economyFile = new File(plugin.getDataFolder(), "economy.yml");
        rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        bossesFile = new File(plugin.getDataFolder(), "bosses.yml");
        dungeonsFile = new File(plugin.getDataFolder(), "dungeons.yml");

        // Crear archivos si no existen
        createFileIfNotExists(configFile, "config.yml");
        createFileIfNotExists(skillsFile, "skills.yml");
        createFileIfNotExists(questsFile, "quests.yml");
        createFileIfNotExists(classesFile, "classes.yml");
        createFileIfNotExists(messagesFile, "messages.yml");
        createFileIfNotExists(petsFile, "pets.yml");
        createFileIfNotExists(landFile, "land.yml");
        createFileIfNotExists(playerFile, "player.yml");
        createFileIfNotExists(dynamicQuestsFile, "dynamic_quests.yml");
        createFileIfNotExists(factionsFile, "factions.yml");
        createFileIfNotExists(qualitiesFile, "qualities.yml");
        createFileIfNotExists(specializationsFile, "specializations.yml");
        createFileIfNotExists(economyFile, "economy.yml");
        createFileIfNotExists(rewardsFile, "rewards.yml");
        createFileIfNotExists(bossesFile, "bosses.yml");
        createFileIfNotExists(dungeonsFile, "dungeons.yml");

        // Cargar configuraciones
        config = YamlConfiguration.loadConfiguration(configFile);
        skillsConfig = YamlConfiguration.loadConfiguration(skillsFile);
        questsConfig = YamlConfiguration.loadConfiguration(questsFile);
        classesConfig = YamlConfiguration.loadConfiguration(classesFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        petsConfig = YamlConfiguration.loadConfiguration(petsFile);
        landConfig = YamlConfiguration.loadConfiguration(landFile);
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        dynamicQuestsConfig = YamlConfiguration.loadConfiguration(dynamicQuestsFile);
        factionsConfig = YamlConfiguration.loadConfiguration(factionsFile);
        qualitiesConfig = YamlConfiguration.loadConfiguration(qualitiesFile);
        specializationsConfig = YamlConfiguration.loadConfiguration(specializationsFile);
        economyConfig = YamlConfiguration.loadConfiguration(economyFile);
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        bossesConfig = YamlConfiguration.loadConfiguration(bossesFile);
        dungeonsConfig = YamlConfiguration.loadConfiguration(dungeonsFile);
    }

    private void createFileIfNotExists(File file, String resourceName) {
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
        }
    }

    // Getters para todas las configuraciones
    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getSkillsConfig() {
        return skillsConfig;
    }

    public FileConfiguration getQuestsConfig() {
        return questsConfig;
    }

    public FileConfiguration getClassesConfig() {
        return classesConfig;
    }


    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getPetsConfig() {
        return petsConfig;
    }

    public FileConfiguration getLandConfig() {
        return landConfig;
    }

    public FileConfiguration getPlayerConfig() {
        return playerConfig;
    }

    public FileConfiguration getDynamicQuestsConfig() {
        return dynamicQuestsConfig;
    }


    public FileConfiguration getFactionsConfig() {
        return factionsConfig;
    }




    public FileConfiguration getQualitiesConfig() {
        return qualitiesConfig;
    }


    public FileConfiguration getSpecializationsConfig() {
        return specializationsConfig;
    }

    public FileConfiguration getEconomyConfig() {
        return economyConfig;
    }

    public FileConfiguration getRewardsConfig() {
        return rewardsConfig;
    }

    public FileConfiguration getBossesConfig() {
        return bossesConfig;
    }

    public FileConfiguration getDungeonsConfig() {
        return dungeonsConfig;
    }



    // Métodos para guardar configuraciones
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml");
            e.printStackTrace();
        }
    }

    public void saveSkillsConfig() {
        try {
            skillsConfig.save(skillsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save skills.yml");
            e.printStackTrace();
        }
    }

    public void saveQuestsConfig() {
        try {
            questsConfig.save(questsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save quests.yml");
            e.printStackTrace();
        }
    }

    public void saveClassesConfig() {
        try {
            classesConfig.save(classesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save classes.yml");
            e.printStackTrace();
        }
    }



    public void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages.yml");
            e.printStackTrace();
        }
    }

    public void savePetsConfig() {
        try {
            petsConfig.save(petsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save pets.yml");
            e.printStackTrace();
        }
    }

    public void saveLandConfig() {
        try {
            landConfig.save(landFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save land.yml");
            e.printStackTrace();
        }
    }

    public void savePlayerConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player.yml");
            e.printStackTrace();
        }
    }

    public void saveDynamicQuestsConfig() {
        try {
            dynamicQuestsConfig.save(dynamicQuestsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save dynamic_quests.yml");
            e.printStackTrace();
        }
    }



    public void saveFactionsConfig() {
        try {
            factionsConfig.save(factionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save factions.yml");
            e.printStackTrace();
        }
    }






    public void saveQualitiesConfig() {
        try {
            qualitiesConfig.save(qualitiesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save qualities.yml");
            e.printStackTrace();
        }
    }


    public void saveSpecializationsConfig() {
        try {
            specializationsConfig.save(specializationsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save specializations.yml");
            e.printStackTrace();
        }
    }

    public void saveEconomyConfig() {
        try {
            economyConfig.save(economyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save economy.yml");
            e.printStackTrace();
        }
    }

    public void saveRewardsConfig() {
        try {
            rewardsConfig.save(rewardsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rewards.yml");
            e.printStackTrace();
        }
    }

    public void saveBossesConfig() {
        try {
            bossesConfig.save(bossesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save bosses.yml");
            e.printStackTrace();
        }
    }

    public void saveDungeonsConfig() {
        try {
            dungeonsConfig.save(dungeonsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save dungeons.yml");
            e.printStackTrace();
        }
    }



    public void reloadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        skillsConfig = YamlConfiguration.loadConfiguration(skillsFile);
        questsConfig = YamlConfiguration.loadConfiguration(questsFile);
        classesConfig = YamlConfiguration.loadConfiguration(classesFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        petsConfig = YamlConfiguration.loadConfiguration(petsFile);
        landConfig = YamlConfiguration.loadConfiguration(landFile);
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        dynamicQuestsConfig = YamlConfiguration.loadConfiguration(dynamicQuestsFile);
        factionsConfig = YamlConfiguration.loadConfiguration(factionsFile);

        qualitiesConfig = YamlConfiguration.loadConfiguration(qualitiesFile);
        specializationsConfig = YamlConfiguration.loadConfiguration(specializationsFile);
        economyConfig = YamlConfiguration.loadConfiguration(economyFile);
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        bossesConfig = YamlConfiguration.loadConfiguration(bossesFile);
        dungeonsConfig = YamlConfiguration.loadConfiguration(dungeonsFile);
    }

    public void savePlayerSettings(UUID playerId, Map<String, Boolean> settings) {
    }

    public Map<String, Boolean> getPlayerSettings(UUID playerId) {
        return Map.of();
    }
}