package com.mk.mksurvival.managers.classes;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ClassManager {

    private final MKSurvival plugin;
    private final HashMap<UUID, PlayerClass> playerClasses = new HashMap<>();
    private final HashMap<String, GameClass> availableClasses = new HashMap<>();
    private FileConfiguration classesConfig;

    public ClassManager(MKSurvival plugin) {
        this.plugin = plugin;
        // Corregido: usar getConfigManager() correctamente
        this.classesConfig = plugin.getConfigManager().getClassesConfig();
        loadClasses();
        loadAllPlayerData();
    }

    private void loadClasses() {
        // Load classes from config
        for (String classId : classesConfig.getConfigurationSection("classes").getKeys(false)) {
            String path = "classes." + classId + ".";

            String name = classesConfig.getString(path + "name");
            String description = classesConfig.getString(path + "description");
            List<String> abilities = classesConfig.getStringList(path + "abilities");
            List<String> effects = classesConfig.getStringList(path + "effects");

            GameClass gameClass = new GameClass(classId, name, description, abilities, effects);
            availableClasses.put(classId, gameClass);
        }
    }

    public PlayerClass getPlayerClass(Player player) {
        return playerClasses.computeIfAbsent(player.getUniqueId(), uuid -> {
            String defaultClassId = plugin.getConfig().getString("classes.default_class", "adventurer");
            return new PlayerClass(availableClasses.get(defaultClassId));
        });
    }

    public void setPlayerClass(Player player, String classId) {
        GameClass gameClass = availableClasses.get(classId);
        if (gameClass != null) {
            PlayerClass playerClass = new PlayerClass(gameClass);
            playerClasses.put(player.getUniqueId(), playerClass);

            // Apply class effects
            applyClassEffects(player, gameClass);

            MessageUtils.sendMessage(player, "<green>[Classes] ¡Ahora eres un " + gameClass.getName() + "!");
        } else {
            MessageUtils.sendMessage(player, "<red>[Classes] La clase no existe.");
        }
    }

    private void applyClassEffects(Player player, GameClass gameClass) {
        // Remove all effects first
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Apply class effects
        for (String effectStr : gameClass.getEffects()) {
            String[] parts = effectStr.split(":");
            if (parts.length == 3) {
                PotionEffectType type = PotionEffectType.getByName(parts[0]);
                int amplifier = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]);

                if (type != null) {
                    // Apply permanent effect (duration 999999)
                    player.addPotionEffect(new PotionEffect(type, duration > 0 ? duration * 20 : 999999, amplifier));
                }
            }
        }
    }

    public void saveAllPlayerData() {
        for (UUID uuid : playerClasses.keySet()) {
            PlayerClass playerClass = playerClasses.get(uuid);
            classesConfig.set("players." + uuid.toString() + ".class", playerClass.getGameClass().getId());
        }

        // Corregido: usar getConfigManager() correctamente
        plugin.getConfigManager().saveClassesConfig();
    }

    public void loadAllPlayerData() {
        // Load data for online players if server reloaded
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (classesConfig.contains("players." + uuid.toString() + ".class")) {
                String classId = classesConfig.getString("players." + uuid.toString() + ".class");
                GameClass gameClass = availableClasses.get(classId);

                if (gameClass != null) {
                    PlayerClass playerClass = new PlayerClass(gameClass);
                    playerClasses.put(uuid, playerClass);

                    // Apply class effects
                    applyClassEffects(player, gameClass);
                }
            }
        }
    }

    public HashMap<String, GameClass> getAvailableClasses() {
        return availableClasses;
    }

    public void upgradeClass(Player player) {
        PlayerClass playerClass = getPlayerClass(player);
        // Simple upgrade logic - could be expanded
        playerClass.addExperience(100);
    }

    public static class GameClass {
        private final String id;
        private final String name;
        private final String description;
        private final List<String> abilities;
        private final List<String> effects;

        public GameClass(String id, String name, String description, List<String> abilities, List<String> effects) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.abilities = abilities;
            this.effects = effects;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<String> getAbilities() { return abilities; }
        public List<String> getEffects() { return effects; }
    }

    public static class PlayerClass {
        private final GameClass gameClass;
        private int level = 1;
        private int experience = 0;

        public PlayerClass(GameClass gameClass) {
            this.gameClass = gameClass;
        }

        public GameClass getGameClass() {
            return gameClass;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getExperience() {
            return experience;
        }

        public void setExperience(int experience) {
            this.experience = experience;
        }

        public void addExperience(int exp) {
            this.experience += exp;
            // Simple leveling logic
            int requiredExp = level * 1000;
            while (experience >= requiredExp) {
                experience -= requiredExp;
                level++;
                requiredExp = level * 1000;
            }
        }
    }
}