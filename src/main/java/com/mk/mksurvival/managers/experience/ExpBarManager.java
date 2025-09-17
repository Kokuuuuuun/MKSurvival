package com.mk.mksurvival.managers.experience;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.skills.PlayerSkills;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.managers.skills.PlayerSkill;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema mejorado de barras de experiencia con múltiples vistas y personalización
 */
public class ExpBarManager {
    private final MKSurvival plugin;
    private final Map<UUID, PlayerExpBar> playerBars = new ConcurrentHashMap<>();
    private final Map<UUID, ExpBarMode> playerModes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastUpdate = new ConcurrentHashMap<>();
    
    // Configuraciones
    private final long UPDATE_INTERVAL = 50L; // 2.5 segundos
    private final int ANIMATION_FRAMES = 20;
    private boolean animationsEnabled = true;

    public ExpBarManager(MKSurvival plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (shouldUpdate(player)) {
                        updatePlayerExpBar(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, UPDATE_INTERVAL);
    }

    private boolean shouldUpdate(Player player) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastUpdateTime = lastUpdate.getOrDefault(uuid, 0L);
        
        return currentTime - lastUpdateTime >= (UPDATE_INTERVAL * 50); // Convertir ticks a ms
    }

    public void updatePlayerExpBar(Player player) {
        UUID uuid = player.getUniqueId();
        lastUpdate.put(uuid, System.currentTimeMillis());
        
        PlayerExpBar expBar = playerBars.computeIfAbsent(uuid, k -> new PlayerExpBar(player));
        ExpBarMode mode = playerModes.getOrDefault(uuid, ExpBarMode.OVERVIEW);
        
        expBar.update(mode);
    }

    public void setPlayerMode(Player player, ExpBarMode mode) {
        playerModes.put(player.getUniqueId(), mode);
        updatePlayerExpBar(player);
        
        String modeName = switch (mode) {
            case OVERVIEW -> "Resumen General";
            case DETAILED_SKILLS -> "Habilidades Detalladas";
            case COMBAT_FOCUS -> "Enfoque en Combate";
            case PROGRESSION -> "Progresión";
            case COMPACT -> "Vista Compacta";
        };
        
        MessageUtils.sendMessage(player, 
            "<green>[ExpBar]</green> <yellow>Modo cambiado a: <gold>" + modeName + "</gold></yellow>"
        );
    }

    public void toggleAnimations(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerExpBar expBar = playerBars.get(uuid);
        if (expBar != null) {
            expBar.animationsEnabled = !expBar.animationsEnabled;
            MessageUtils.sendMessage(player, 
                "<green>[ExpBar]</green> <yellow>Animaciones " + 
                (expBar.animationsEnabled ? "<green>activadas</green>" : "<red>desactivadas</red>") + 
                "</yellow>"
            );
        }
    }

    public void removePlayerExpBar(Player player) {
        UUID uuid = player.getUniqueId();
        playerBars.remove(uuid);
        playerModes.remove(uuid);
        lastUpdate.remove(uuid);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public ExpBarMode getPlayerMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), ExpBarMode.OVERVIEW);
    }

    // Clase interna para manejar la barra de cada jugador
    private class PlayerExpBar {
        private final Player player;
        private final Scoreboard scoreboard;
        private Objective objective;
        private boolean animationsEnabled = true;
        private int animationFrame = 0;

        public PlayerExpBar(Player player) {
            this.player = player;
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            createObjective();
            player.setScoreboard(scoreboard);
        }

        private void createObjective() {
            if (objective != null) {
                objective.unregister();
            }
            this.objective = scoreboard.registerNewObjective("expbar", "dummy", MessageUtils.parse(("<gradient:#FFD700:#FFA500>✦ MK SURVIVAL ✦</gradient>")));
            this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        public void update(ExpBarMode mode) {
            clearScoreboard();
            
            switch (mode) {
                case OVERVIEW -> updateOverviewMode();
                case DETAILED_SKILLS -> updateDetailedSkillsMode();
                case COMBAT_FOCUS -> updateCombatFocusMode();
                case PROGRESSION -> updateProgressionMode();
                case COMPACT -> updateCompactMode();
            }
            
            if (animationsEnabled) {
                animationFrame = (animationFrame + 1) % ANIMATION_FRAMES;
            }
        }

        private void clearScoreboard() {
            for (String entry : scoreboard.getEntries()) {
                scoreboard.resetScores(entry);
            }
        }

        private void updateOverviewMode() {
            PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
            int line = 15;
            
            // Header simple sin animación compleja
            setScore(MessageUtils.parse(("<dark_purple><bold>✦ PROGRESO ✦</bold></dark_purple>")), line--);
            setScore(MessageUtils.parse(("<dark_gray>━━━━━━━━━━━━━━━</dark_gray>")), line--);
            
            // Información del jugador
            int totalLevel = getTotalLevel(skills);
            int totalExp = (int) getTotalExp(skills);
            
            setScore(MessageUtils.parse(("<white>Jugador: <yellow>" + truncateText(player.getName(), 10) + "</yellow></white>")), line--);
            setScore(MessageUtils.parse(("<white>Nivel: <gold>" + totalLevel + "</gold></white>")), line--);
            setScore(MessageUtils.parse(("<white>EXP: <aqua>" + formatNumber(totalExp) + "</aqua></white>")), line--);
            setScore("", line--); // Línea vacía
            
            // Top 3 habilidades
            setScore(MessageUtils.parse(("<green>▶ Top Habilidades:</green>")), line--);
            
            List<Map.Entry<SkillType, Integer>> topSkills = getTopSkills(skills, 3);
            for (Map.Entry<SkillType, Integer> entry : topSkills) {
                String skillName = getSkillDisplayName(entry.getKey());
                String shortName = truncateText(skillName, 8);
                String progressBar = createProgressBar(
                    skills.getExp(entry.getKey()), 
                    skills.getExpNeeded(entry.getValue()), 
                    3
                );
                setScore(MessageUtils.parse(("<gray>• <white>" + shortName + " <yellow>" + entry.getValue() + "</yellow></white></gray>")), line--);
                setScore("§7" + progressBar, line--);
            }
            
            setScore("", line--);
            setScore(MessageUtils.parse(("<gray>/expbar para cambiar</gray>")), line--);
        }

        private void updateDetailedSkillsMode() {
            PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
            int line = 15;
            
            setScore(MessageUtils.parse(("<green><bold>✦ HABILIDADES ✦</bold></green>")), line--);
            setScore("§8━━━━━━━━━━━━━━━", line--);
            
            for (SkillType skill : SkillType.values()) {
                int level = skills.getLevel(skill);
                double exp = skills.getExp(skill);
                double needed = skills.getExpNeeded(level);
                
                String skillName = getSkillDisplayName(skill);
                String shortName = truncateText(skillName, 8);
                String progressBar = createProgressBar(exp, needed, 3);
                
                setScore("§f" + shortName + " §e" + level, line--);
                setScore("§7" + progressBar, line--);
            }
        }

        private void updateCombatFocusMode() {
            PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
            int line = 15;
            
            setScore(MessageUtils.parse(("<red><bold>⚔ COMBATE ⚔</bold></red>")), line--);
            setScore(MessageUtils.parse(("<dark_gray>━━━━━━━━━━━━━━━</dark_gray>")), line--);
            
            SkillType combatSkill = SkillType.COMBAT;
            int combatLevel = skills.getLevel(combatSkill);
            double combatExp = skills.getExp(combatSkill);
            double combatExpNeeded = skills.getExpNeeded(combatLevel);
            
            setScore(MessageUtils.parse(("<white>Nivel: <red>" + combatLevel + "</red></white>")), line--);
            setScore(MessageUtils.parse(("<white>EXP: <aqua>" + (int)combatExp + "<gray>/" + (int)combatExpNeeded + "</gray></aqua></white>")), line--);
            
            String progressBar = createProgressBar(combatExp, combatExpNeeded, 8);
            setScore(MessageUtils.parse(("<white>Progreso:</white>")), line--);
            setScore(MessageUtils.parse(("<gray>" + progressBar + "</gray>")), line--);
            setScore("", line--);
            
            // Estadísticas de combate
            setScore(MessageUtils.parse(("<yellow>▶ Estadísticas:</yellow>")), line--);
            
            int damageBonus = combatLevel * 2;
            int critChance = Math.min(25, combatLevel / 4);
            
            setScore(MessageUtils.parse(("<gray>• Daño: <red>+" + damageBonus + "%</red></gray>")), line--);
            setScore(MessageUtils.parse(("<gray>• Crítico: <yellow>" + critChance + "%</yellow></gray>")), line--);
            setScore(MessageUtils.parse(("<gray>• Siguiente: <green>" + (combatLevel + 1) + "</green></gray>")), line--);
        }

        private void updateProgressionMode() {
            PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
            int line = 15;
            
            setScore(MessageUtils.parse(("<green><bold>✦ HABILIDADES ✦</bold></green>")), line--);
            setScore(MessageUtils.parse(("<dark_gray>━━━━━━━━━━━━━━━</dark_gray>")), line--);
            
            // Habilidad más cercana a subir de nivel
            SkillType nextSkill = getClosestToLevelUp(skills);
            if (nextSkill != null) {
                int level = skills.getLevel(nextSkill);
                double exp = skills.getExp(nextSkill);
                double needed = skills.getExpNeeded(level);
                double progress = (exp / needed) * 100;
                
                setScore(MessageUtils.parse(("<green>▶ Próximo nivel:</green>")), line--);
                setScore(MessageUtils.parse(("<white>" + truncateText(getSkillDisplayName(nextSkill), 8) + " <yellow>" + level + "</yellow></white>")), line--);
                setScore(MessageUtils.parse(("<gray>(" + String.format("%.1f", progress) + "%)</gray>")), line--);
                
                String progressBar = createProgressBar(exp, needed, 8);
                setScore(MessageUtils.parse(("<gray>" + progressBar + "</gray>")), line--);
                
                setScore("", line--);
                setScore(MessageUtils.parse(("<gray>Restante: <white>" + formatNumber((int)(needed - exp)) + "</white></gray>")), line--);
            }
        }

        private void updateCompactMode() {
            PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player);
            int line = 10;
            
            setScore(MessageUtils.parse(("<gold><bold>✦ EXPERIENCIA ✦</bold></gold>")), line--);
            setScore(MessageUtils.parse(("<dark_gray>━━━━━━━━━━━━━━━</dark_gray>")), line--);
            
            // Solo mostrar las 2 habilidades principales
            List<Map.Entry<SkillType, Integer>> topSkills = getTopSkills(skills, 2);
            for (Map.Entry<SkillType, Integer> entry : topSkills) {
                SkillType mainSkill = entry.getKey();
                int level = entry.getValue();
                
                String progressBar = createProgressBar(
                    skills.getExp(mainSkill), 
                    skills.getExpNeeded(level), 
                    3
                );
                
                String skillName = getSkillDisplayName(mainSkill);
                String shortName = skillName.length() > 3 ? skillName.substring(0, 3) : skillName;
                setScore("§f" + shortName + " §e" + level, line--);
                setScore("§7" + progressBar, line--);
            }
        }

        private void setScore(String text, int score) {
            // Limitar la longitud del texto para evitar problemas de visualización
            String cleanText = truncateText(text, 32);
            objective.getScore(cleanText).setScore(score);
        }
        
        private String truncateText(String text, int maxLength) {
            if (text == null) return "";
            if (text.length() <= maxLength) return text;
            return text.substring(0, maxLength - 3) + "...";
        }

        private String getAnimatedHeader() {
            // Header simple sin gradientes complejos
            if (!animationsEnabled) {
                return "§6§l✦ EXPERIENCIA ✦";
            }
            
            String[] colors = {"<gold>", "<yellow>", "<red>", "<dark_red>"};
            int colorIndex = animationFrame % colors.length;
            return MessageUtils.parse((colors[colorIndex] + "<bold>✦ EXPERIENCIA ✦</bold>" + colors[colorIndex].replace("<", "</")));
        }

        // Métodos auxiliares
        private int getTotalLevel(PlayerSkills skills) {
            int total = 0;
            for (SkillType skill : SkillType.values()) {
                total += skills.getLevel(skill);
            }
            return total;
        }

        private double getTotalExp(PlayerSkills skills) {
            double total = 0;
            for (SkillType skill : SkillType.values()) {
                total += skills.getExp(skill);
            }
            return total;
        }

        private List<Map.Entry<SkillType, Integer>> getTopSkills(PlayerSkills skills, int count) {
            Map<SkillType, Integer> skillLevels = new HashMap<>();
            for (SkillType skill : SkillType.values()) {
                skillLevels.put(skill, skills.getLevel(skill));
            }
            
            return skillLevels.entrySet().stream()
                    .sorted(Map.Entry.<SkillType, Integer>comparingByValue().reversed())
                    .limit(count)
                    .toList();
        }

        private SkillType getHighestSkill(PlayerSkills skills) {
            SkillType highest = SkillType.COMBAT;
            int highestLevel = 0;
            
            for (SkillType skill : SkillType.values()) {
                int level = skills.getLevel(skill);
                if (level > highestLevel) {
                    highestLevel = level;
                    highest = skill;
                }
            }
            
            return highest;
        }

        private SkillType getClosestToLevelUp(PlayerSkills skills) {
            SkillType closest = null;
            double smallestRemaining = Double.MAX_VALUE;
            
            for (SkillType skill : SkillType.values()) {
                int level = skills.getLevel(skill);
                double exp = skills.getExp(skill);
                double expNeeded = skills.getExpNeeded(level);
                double remaining = expNeeded - exp;
                
                if (remaining > 0 && remaining < smallestRemaining) {
                    smallestRemaining = remaining;
                    closest = skill;
                }
            }
            
            return closest;
        }

        private String createProgressBar(double current, double total, int length) {
            double progress = Math.min(1.0, current / total);
            int filled = (int) (progress * length);
            
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < filled; i++) {
                bar.append("█");
            }
            for (int i = filled; i < length; i++) {
                bar.append("▒");
            }
            
            // Colorear la barra según el progreso - usar colores simples
            String color = progress >= 0.8 ? "<green>" : progress >= 0.5 ? "<yellow>" : "<red>";
            return MessageUtils.parse((color + bar.toString() + "<gray>"));
        }

        private String getSkillDisplayName(SkillType skill) {
            return switch (skill) {
                case COMBAT -> "Combate";
                case MINING -> "Minería";
                case ALCHEMY -> "Alquimia";
                case FORAGING -> "Recolección";
                default -> skill.name();
            };
        }

        private String formatNumber(int number) {
            if (number >= 1000000) {
                return String.format("%.1fM", number / 1000000.0);
            } else if (number >= 1000) {
                return String.format("%.1fK", number / 1000.0);
            } else {
                return String.valueOf(number);
            }
        }
    }

    // Enum para los diferentes modos de visualización
    public enum ExpBarMode {
        OVERVIEW,
        DETAILED_SKILLS,
        COMBAT_FOCUS,
        PROGRESSION,
        COMPACT
    }
}