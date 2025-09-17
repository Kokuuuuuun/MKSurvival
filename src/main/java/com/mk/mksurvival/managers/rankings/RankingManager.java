package com.mk.mksurvival.managers.rankings;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RankingManager implements Listener {
    private final MKSurvival plugin;
    private final Map<String, Ranking> rankings = new HashMap<>();
    private final Map<String, Ranking> seasonalRankings = new HashMap<>();
    private final Map<UUID, Map<String, Double>> playerStats = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Double>> seasonalStats = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerJoinTimes = new HashMap<>();
    private final Map<String, Competition> activeCompetitions = new HashMap<>();
    private final Map<UUID, Set<String>> playerAchievements = new HashMap<>();
    private final Map<String, Long> lastRankingUpdate = new HashMap<>();
    private FileConfiguration rankingConfig;
    private FileConfiguration seasonalConfig;
    private File rankingFile;
    private File seasonalFile;
    private LocalDateTime currentSeasonStart;
    private int currentSeason;
    private final long RANKING_UPDATE_INTERVAL = 60000; // 1 minute

    public RankingManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.currentSeasonStart = LocalDateTime.now();
        this.currentSeason = 1;
        setupConfig();
        setupRankings();
        setupSeasonalRankings();
        loadRankings();
        loadSeasonalData();
        startCompetitionScheduler();
        startRankingUpdateScheduler();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupConfig() {
        rankingFile = new File(plugin.getDataFolder(), "rankings.yml");
        if (!rankingFile.exists()) {
            plugin.saveResource("rankings.yml", false);
        }
        rankingConfig = YamlConfiguration.loadConfiguration(rankingFile);

        seasonalFile = new File(plugin.getDataFolder(), "seasonal_rankings.yml");
        if (!seasonalFile.exists()) {
            try {
                seasonalFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create seasonal_rankings.yml");
            }
        }
        seasonalConfig = YamlConfiguration.loadConfiguration(seasonalFile);
    }

    private void setupRankings() {
        rankings.put("dinero", new Ranking(
                "dinero", "Ranking de Dinero", "Jugadores con más dinero", "<gold>",
                (player) -> plugin.getEconomyManager().getBalance(player)
        ));

        rankings.put("habilidades", new Ranking(
                "habilidades", "Ranking de Habilidades", "Jugadores con mayor nivel total de habilidades", "<aqua>",
                (player) -> {
                    double totalLevel = 0;
                    for (SkillType skillType : SkillType.values()) {
                        totalLevel += plugin.getSkillManager().getPlayerSkills(player.getUniqueId()).getLevel(skillType);
                    }
                    return totalLevel;
                }
        ));

        rankings.put("misiones", new Ranking(
                "misiones", "Ranking de Misiones", "Jugadores con más misiones completadas", "<green>",
                (player) -> playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("misiones_completadas", 0.0)
        ));

        rankings.put("mobs", new Ranking(
                "mobs", "Ranking de Mobs", "Jugadores con más mobs asesinados", "<red>",
                (player) -> playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("mobs_asesinados", 0.0)
        ));

        rankings.put("tiempo", new Ranking(
                "tiempo", "Ranking de Tiempo", "Jugadores con más tiempo jugado", "<light_purple>",
                (player) -> playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("tiempo_jugado", 0.0)
        ));

        // New comprehensive rankings
        rankings.put("dungeons", new Ranking(
                "dungeons", "Ranking de Mazmorras", "Jugadores con más mazmorras completadas", "<dark_purple>",
                (player) -> playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("dungeons_completadas", 0.0)
        ));

        rankings.put("bosses", new Ranking(
                "bosses", "Ranking de Jefes", "Jugadores con más jefes derrotados", "<dark_red>",
                (player) -> playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("bosses_derrotados", 0.0)
        ));

        rankings.put("faction_power", new Ranking(
                "faction_power", "Ranking de Poder de Facción", "Facciones con más poder", "<yellow>",
                (player) -> {
                    String factionName = plugin.getFactionManager().getPlayerFaction(player.getUniqueId());
                    if (factionName != null) {
                        // Usar el método getFactionPower del FactionManager en lugar de getpower()
                        return (double) plugin.getFactionManager().getFactionPower(factionName);
                    }
                    return 0.0;
                }
        ));

        rankings.put("pet_competitions", new Ranking(
                "pet_competitions", "Ranking de Competencias de Mascotas", "Jugadores con más competencias ganadas", "<pink>",
                (player) -> playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("pet_competitions_ganadas", 0.0)
        ));

        rankings.put("pvp_kills", new Ranking(
                "pvp_kills", "Ranking PvP", "Jugadores con más asesinatos PvP", "<dark_aqua>",
                (player) -> playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("pvp_kills", 0.0)
        ));

        rankings.put("achievements", new Ranking(
                "achievements", "Ranking de Logros", "Jugadores con más logros desbloqueados", "<gold>",
                (player) -> (double) playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>()).size()
        ));
    }

    private void setupSeasonalRankings() {
        seasonalRankings.put("seasonal_kills", new Ranking(
                "seasonal_kills", "Ranking de Temporada - Asesinatos", "Asesinatos en la temporada actual", "<gold>",
                (player) -> seasonalStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("kills", 0.0)
        ));

        seasonalRankings.put("seasonal_wealth", new Ranking(
                "seasonal_wealth", "Ranking de Temporada - Riqueza Ganada", "Dinero ganado en la temporada", "<yellow>",
                (player) -> seasonalStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("wealth_gained", 0.0)
        ));

        seasonalRankings.put("seasonal_quests", new Ranking(
                "seasonal_quests", "Ranking de Temporada - Misiones", "Misiones completadas en la temporada", "<green>",
                (player) -> seasonalStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                        .getOrDefault("quests_completed", 0.0)
        ));
    }

    private void startCompetitionScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndStartCompetitions();
                updateActiveCompetitions();
            }
        }.runTaskTimer(plugin, 20L, 1200L); // Check every minute
    }

    private void startRankingUpdateScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateRankingCache();
                checkSeasonEnd();
            }
        }.runTaskTimer(plugin, 20L, 1200L); // Update every minute
    }

    private void checkAndStartCompetitions() {
        Random random = new Random();

        // Weekly competitions
        if (random.nextInt(100) < 5) { // 5% chance every minute
            startCompetition("weekly_kills", "Competencia Semanal de Asesinatos",
                    "mobs_asesinados", 7 * 24 * 60, createCompetitionRewards("weekly"));
        }

        // Daily mini competitions  
        if (random.nextInt(100) < 10) { // 10% chance every minute
            String[] dailyTypes = {"dungeon_speedrun", "boss_hunting", "resource_gathering"};
            String type = dailyTypes[random.nextInt(dailyTypes.length)];
            startCompetition("daily_" + type, "Competencia Diaria: " + type,
                    type, 24 * 60, createCompetitionRewards("daily"));
        }
    }

    private void startCompetition(String id, String name, String statType, int durationMinutes, List<CompetitionReward> rewards) {
        if (activeCompetitions.containsKey(id)) return;

        Competition competition = new Competition(id, name, statType,
                System.currentTimeMillis() + (durationMinutes * 60000L), rewards);
        activeCompetitions.put(id, competition);

        // Announce competition
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            MessageUtils.sendMessage(player, "<gold>¡Nueva competencia iniciada!</gold>");
            MessageUtils.sendMessage(player, "<yellow>" + name + "</yellow>");
            MessageUtils.sendMessage(player, "<green>Duración: " + durationMinutes + " minutos</green>");
            MessageUtils.sendMessage(player, "<aqua>¡Compite por increíbles recompensas!</aqua>");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private List<CompetitionReward> createCompetitionRewards(String tier) {
        List<CompetitionReward> rewards = new ArrayList<>();

        switch (tier) {
            case "weekly":
                rewards.add(new CompetitionReward(1, 5000.0, new ItemStack(Material.DIAMOND, 10), "Campeón Semanal"));
                rewards.add(new CompetitionReward(2, 3000.0, new ItemStack(Material.GOLD_INGOT, 15), "Subcampeón"));
                rewards.add(new CompetitionReward(3, 1500.0, new ItemStack(Material.IRON_INGOT, 20), "Tercer Lugar"));
                break;
            case "daily":
                rewards.add(new CompetitionReward(1, 1000.0, new ItemStack(Material.EMERALD, 5), "Ganador Diario"));
                rewards.add(new CompetitionReward(2, 500.0, new ItemStack(Material.DIAMOND, 2), "Segundo Lugar"));
                break;
        }

        return rewards;
    }

    private void updateActiveCompetitions() {
        Iterator<Map.Entry<String, Competition>> iterator = activeCompetitions.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Competition> entry = iterator.next();
            Competition competition = entry.getValue();

            if (System.currentTimeMillis() >= competition.getEndTime()) {
                finishCompetition(competition);
                iterator.remove();
            }
        }
    }

    private void finishCompetition(Competition competition) {
        List<RankingEntry> winners = getRankingForCompetition(competition.getStatType(), 10);

        // Announce winners
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            MessageUtils.sendMessage(player, "<gold>¡Competencia finalizada: " + competition.getName() + "!</gold>");

            if (!winners.isEmpty()) {
                MessageUtils.sendMessage(player, "<yellow>Ganadores:</yellow>");
                for (int i = 0; i < Math.min(3, winners.size()); i++) {
                    RankingEntry winner = winners.get(i);
                    String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : "🥉";
                    MessageUtils.sendMessage(player, medal + " <white>" + winner.getPlayerName() + "</white> - " +
                            formatValue(winner.getValue(), competition.getStatType()));
                }
            }
        }

        // Give rewards
        giveCompetitionRewards(competition, winners);
    }

    private void giveCompetitionRewards(Competition competition, List<RankingEntry> winners) {
        for (CompetitionReward reward : competition.getRewards()) {
            if (reward.getPosition() <= winners.size()) {
                RankingEntry winner = winners.get(reward.getPosition() - 1);
                Player player = plugin.getServer().getPlayer(winner.getPlayerId());

                if (player != null && player.isOnline()) {
                    // Give money
                    if (reward.getMoney() > 0) {
                        plugin.getEconomyManager().addBalance(player, reward.getMoney());
                    }

                    // Give item
                    if (reward.getItem() != null) {
                        player.getInventory().addItem(reward.getItem());
                    }

                    // Give achievement
                    if (reward.getAchievement() != null) {
                        unlockAchievement(player, reward.getAchievement());
                    }

                    MessageUtils.sendMessage(player, "<gold>¡Felicidades! Has ganado la competencia " + competition.getName() + "!</gold>");
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
            }
        }
    }

    private void loadSeasonalData() {
        // Load seasonal stats
        if (seasonalConfig.contains("current_season")) {
            currentSeason = seasonalConfig.getInt("current_season");
        }

        if (seasonalConfig.contains("season_start")) {
            String startTime = seasonalConfig.getString("season_start");
            try {
                currentSeasonStart = LocalDateTime.parse(startTime);
            } catch (Exception e) {
                currentSeasonStart = LocalDateTime.now();
            }
        }

        // Load seasonal player stats
        if (seasonalConfig.contains("seasonal_stats")) {
            for (String uuidStr : seasonalConfig.getConfigurationSection("seasonal_stats").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Double> stats = new HashMap<>();

                if (seasonalConfig.contains("seasonal_stats." + uuidStr)) {
                    for (String stat : seasonalConfig.getConfigurationSection("seasonal_stats." + uuidStr).getKeys(false)) {
                        stats.put(stat, seasonalConfig.getDouble("seasonal_stats." + uuidStr + "." + stat));
                    }
                }

                seasonalStats.put(uuid, stats);
            }
        }

        // Load achievements
        if (rankingConfig.contains("achievements")) {
            for (String uuidStr : rankingConfig.getConfigurationSection("achievements").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Set<String> achievements = new HashSet<>();

                List<String> achievementList = rankingConfig.getStringList("achievements." + uuidStr);
                achievements.addAll(achievementList);

                playerAchievements.put(uuid, achievements);
            }
        }
    }

    private void loadRankings() {
        if (rankingConfig.contains("player_stats")) {
            for (String uuidStr : rankingConfig.getConfigurationSection("player_stats").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Double> stats = new HashMap<>();

                if (rankingConfig.contains("player_stats." + uuidStr)) {
                    for (String stat : rankingConfig.getConfigurationSection("player_stats." + uuidStr).getKeys(false)) {
                        stats.put(stat, rankingConfig.getDouble("player_stats." + uuidStr + "." + stat));
                    }
                }

                playerStats.put(uuid, stats);
            }
        }
    }

    public void saveRankings() {
        rankingConfig.set("player_stats", null);

        for (Map.Entry<UUID, Map<String, Double>> entry : playerStats.entrySet()) {
            String uuidStr = entry.getKey().toString();

            for (Map.Entry<String, Double> stat : entry.getValue().entrySet()) {
                rankingConfig.set("player_stats." + uuidStr + "." + stat.getKey(), stat.getValue());
            }
        }

        // Save achievements
        rankingConfig.set("achievements", null);
        for (Map.Entry<UUID, Set<String>> entry : playerAchievements.entrySet()) {
            String uuidStr = entry.getKey().toString();
            List<String> achievementList = new ArrayList<>(entry.getValue());
            rankingConfig.set("achievements." + uuidStr, achievementList);
        }

        // Save seasonal data
        seasonalConfig.set("current_season", currentSeason);
        seasonalConfig.set("season_start", currentSeasonStart.toString());

        seasonalConfig.set("seasonal_stats", null);
        for (Map.Entry<UUID, Map<String, Double>> entry : seasonalStats.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Double> stat : entry.getValue().entrySet()) {
                seasonalConfig.set("seasonal_stats." + uuidStr + "." + stat.getKey(), stat.getValue());
            }
        }

        try {
            rankingConfig.save(rankingFile);
            seasonalConfig.save(seasonalFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rankings.yml or seasonal_rankings.yml");
            e.printStackTrace();
        }
    }

    public void updatePlayerStat(Player player, String statName, double value) {
        if (!playerStats.containsKey(player.getUniqueId())) {
            playerStats.put(player.getUniqueId(), new HashMap<>());
        }

        playerStats.get(player.getUniqueId()).put(statName, value);

        // Also update seasonal stats
        updateSeasonalStat(player, statName, value);

        // Check for achievements
        checkStatAchievements(player, statName, value);

        saveRankings();
    }

    public void updateSeasonalStat(Player player, String statName, double value) {
        if (!seasonalStats.containsKey(player.getUniqueId())) {
            seasonalStats.put(player.getUniqueId(), new HashMap<>());
        }

        // For seasonal stats, we track differences (gains)
        String seasonalStatName = getSeasonalStatName(statName);
        if (seasonalStatName != null) {
            double currentValue = seasonalStats.get(player.getUniqueId()).getOrDefault(seasonalStatName, 0.0);
            seasonalStats.get(player.getUniqueId()).put(seasonalStatName, currentValue + value);
        }
    }

    private String getSeasonalStatName(String statName) {
        switch (statName) {
            case "mobs_asesinados": return "kills";
            case "misiones_completadas": return "quests_completed";
            case "dinero_ganado": return "wealth_gained";
            default: return null;
        }
    }

    private void checkStatAchievements(Player player, String statName, double value) {
        switch (statName) {
            case "mobs_asesinados":
                if (value >= 1000) unlockAchievement(player, "mob_slayer_1000");
                if (value >= 5000) unlockAchievement(player, "mob_slayer_5000");
                if (value >= 10000) unlockAchievement(player, "mob_slayer_master");
                break;
            case "misiones_completadas":
                if (value >= 50) unlockAchievement(player, "quest_master_50");
                if (value >= 100) unlockAchievement(player, "quest_master_100");
                if (value >= 500) unlockAchievement(player, "quest_legend");
                break;
            case "dungeons_completadas":
                if (value >= 10) unlockAchievement(player, "dungeon_explorer");
                if (value >= 50) unlockAchievement(player, "dungeon_master");
                break;
            case "bosses_derrotados":
                if (value >= 5) unlockAchievement(player, "boss_hunter");
                if (value >= 25) unlockAchievement(player, "boss_slayer");
                break;
        }
    }

    public void incrementPlayerStat(Player player, String statName, double increment) {
        double currentValue = playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                .getOrDefault(statName, 0.0);
        updatePlayerStat(player, statName, currentValue + increment);
    }

    public List<RankingEntry> getRanking(String rankingId, int limit) {
        Ranking ranking = rankings.get(rankingId);
        if (ranking == null) return new ArrayList<>();

        List<RankingEntry> entries = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            double value = ranking.getValueFunction().apply(player);
            entries.add(new RankingEntry(player.getUniqueId(), player.getName(), value));
        }

        entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        if (limit > 0 && entries.size() > limit) {
            entries = entries.subList(0, limit);
        }

        return entries;
    }

    public List<RankingEntry> getSeasonalRanking(String rankingId, int limit) {
        Ranking ranking = seasonalRankings.get(rankingId);
        if (ranking == null) return new ArrayList<>();

        List<RankingEntry> entries = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            double value = ranking.getValueFunction().apply(player);
            entries.add(new RankingEntry(player.getUniqueId(), player.getName(), value));
        }

        entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        if (limit > 0 && entries.size() > limit) {
            entries = entries.subList(0, limit);
        }

        return entries;
    }

    private List<RankingEntry> getRankingForCompetition(String statType, int limit) {
        List<RankingEntry> entries = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            double value = playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                    .getOrDefault(statType, 0.0);
            entries.add(new RankingEntry(player.getUniqueId(), player.getName(), value));
        }

        entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        if (limit > 0 && entries.size() > limit) {
            entries = entries.subList(0, limit);
        }

        return entries;
    }

    public void showRanking(Player player, String rankingId, int page) {
        Ranking ranking = rankings.get(rankingId);
        if (ranking == null) {
            MessageUtils.sendMessage(player, "<red>Ese ranking no existe.</red>");
            return;
        }

        int entriesPerPage = 10;
        List<RankingEntry> entries = getRanking(rankingId, 100);
        int startIndex = (page - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, entries.size());

        if (startIndex >= entries.size()) {
            MessageUtils.sendMessage(player, "<red>Esa página no existe.</red>");
            return;
        }

        MessageUtils.sendMessage(player, "<gold>--- " + ranking.getColor() + ranking.getName() + " <gold>(Página " + page + ") ---</gold>");
        MessageUtils.sendMessage(player, "<gray>" + ranking.getDescription() + "</gray>");
        MessageUtils.sendMessage(player, "");

        for (int i = startIndex; i < endIndex; i++) {
            RankingEntry entry = entries.get(i);
            String positionColor = i < 3 ? "<gold>" : "<gray>";
            MessageUtils.sendMessage(player, positionColor + (i + 1) + ". <yellow>" + entry.getPlayerName() + "</yellow> <gray>- " +
                    formatValue(entry.getValue(), rankingId) + "</gray>");
        }

        int totalPages = (int) Math.ceil((double) entries.size() / entriesPerPage);
        MessageUtils.sendMessage(player, "<gray>Página " + page + " de " + totalPages + "</gray>");
        MessageUtils.sendMessage(player, "<green>Usa <white>/ranking " + rankingId + " <página></white> para ver otras páginas.</green>");
    }

    public String formatValue(double value, String rankingId) {
        switch (rankingId) {
            case "dinero":
                return plugin.getEconomyManager().formatCurrency(value);
            case "habilidades":
                return String.format("%.1f", value) + " niveles";
            case "misiones":
                return String.format("%.0f", value) + " misiones";
            case "mobs":
                return String.format("%.0f", value) + " mobs";
            case "tiempo":
                return formatTime(value);
            default:
                return String.format("%.2f", value);
        }
    }

    private String formatTime(double seconds) {
        long hours = (int) (seconds / 3600);
        long minutes = (int) ((seconds % 3600) / 60);
        long secs = (int) (seconds % 60);

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public void showRankingList(Player player) {
        MessageUtils.sendMessage(player, "<gold>--- Rankings Disponibles ---</gold>");
        MessageUtils.sendMessage(player, "<gray>Rankings Permanentes:</gray>");

        for (Ranking ranking : rankings.values()) {
            MessageUtils.sendMessage(player, ranking.getColor() + ranking.getName() + " <gray>- " + ranking.getDescription() + "</gray>");
            MessageUtils.sendMessage(player, "<green>Usa <white>/ranking " + ranking.getId() + "</white> para ver el ranking.</green>");
            MessageUtils.sendMessage(player, "");
        }

        MessageUtils.sendMessage(player, "<aqua>Rankings de Temporada (Temporada " + currentSeason + "):</aqua>");
        for (Ranking ranking : seasonalRankings.values()) {
            MessageUtils.sendMessage(player, ranking.getColor() + ranking.getName() + " <gray>- " + ranking.getDescription() + "</gray>");
            MessageUtils.sendMessage(player, "<green>Usa <white>/ranking " + ranking.getId() + "</white> para ver el ranking.</green>");
            MessageUtils.sendMessage(player, "");
        }

        if (!activeCompetitions.isEmpty()) {
            MessageUtils.sendMessage(player, "<yellow>Competencias Activas:</yellow>");
            for (Competition competition : activeCompetitions.values()) {
                long remainingTime = (competition.getEndTime() - System.currentTimeMillis()) / 60000;
                MessageUtils.sendMessage(player, "<red>" + competition.getName() + "</red> <gray>(" + remainingTime + " minutos restantes)</gray>");
            }
        }
    }

    private void updateRankingCache() {
        for (String rankingId : rankings.keySet()) {
            lastRankingUpdate.put(rankingId, System.currentTimeMillis());
        }
    }

    private void checkSeasonEnd() {
        long daysSinceStart = ChronoUnit.DAYS.between(currentSeasonStart, LocalDateTime.now());

        if (daysSinceStart >= 30) { // Season lasts 30 days
            endSeason();
        }
    }

    private void endSeason() {
        // Announce season end
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            MessageUtils.sendMessage(player, "<gold>¡La temporada " + currentSeason + " ha terminado!</gold>");
            MessageUtils.sendMessage(player, "<yellow>¡Comenzando nueva temporada!</yellow>");
        }

        // Save seasonal rankings
        saveSeasonalResults();

        // Reset seasonal stats
        seasonalStats.clear();
        currentSeason++;
        currentSeasonStart = LocalDateTime.now();

        // Give seasonal rewards
        giveSeasonalRewards();
    }

    private void saveSeasonalResults() {
        String seasonKey = "season_" + currentSeason;

        for (Map.Entry<String, Ranking> entry : seasonalRankings.entrySet()) {
            List<RankingEntry> topPlayers = getSeasonalRanking(entry.getKey(), 10);

            for (int i = 0; i < topPlayers.size(); i++) {
                RankingEntry player = topPlayers.get(i);
                seasonalConfig.set(seasonKey + "." + entry.getKey() + "." + (i + 1) + ".name", player.getPlayerName());
                seasonalConfig.set(seasonKey + "." + entry.getKey() + "." + (i + 1) + ".value", player.getValue());
            }
        }

        try {
            seasonalConfig.save(seasonalFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save seasonal rankings");
        }
    }

    private void giveSeasonalRewards() {
        for (String rankingId : seasonalRankings.keySet()) {
            List<RankingEntry> topPlayers = getSeasonalRanking(rankingId, 3);

            for (int i = 0; i < topPlayers.size(); i++) {
                RankingEntry entry = topPlayers.get(i);
                Player player = plugin.getServer().getPlayer(entry.getPlayerId());

                if (player != null && player.isOnline()) {
                    double reward = (3 - i) * 2000.0; // 6000, 4000, 2000
                    plugin.getEconomyManager().addBalance(player, reward);

                    String achievement = "seasonal_top_" + (i + 1) + "_" + rankingId;
                    unlockAchievement(player, achievement);

                    MessageUtils.sendMessage(player, "<gold>¡Recompensa de temporada recibida!</gold>");
                    MessageUtils.sendMessage(player, "<yellow>+" + reward + " monedas por quedar #" + (i + 1) + " en " + rankingId + "</yellow>");
                }
            }
        }
    }

    public void unlockAchievement(Player player, String achievement) {
        if (!playerAchievements.containsKey(player.getUniqueId())) {
            playerAchievements.put(player.getUniqueId(), new HashSet<>());
        }

        Set<String> achievements = playerAchievements.get(player.getUniqueId());
        if (!achievements.contains(achievement)) {
            achievements.add(achievement);

            MessageUtils.sendMessage(player, "<gold>¡Logro desbloqueado!</gold>");
            MessageUtils.sendMessage(player, "<yellow>" + achievement + "</yellow>");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            // Give achievement bonus
            plugin.getEconomyManager().addBalance(player, 100.0);
        }
    }

    public void showPlayerRankings(Player player) {
        MessageUtils.sendMessage(player, "<gold>--- Tus Rankings ---</gold>");

        for (Ranking ranking : rankings.values()) {
            List<RankingEntry> entries = getRanking(ranking.getId(), 100);

            int position = -1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getPlayerId().equals(player.getUniqueId())) {
                    position = i + 1;
                    break;
                }
            }

            if (position != -1) {
                MessageUtils.sendMessage(player, ranking.getColor() + ranking.getName() + " <gray>- Posición: <yellow>" + position + "</yellow></gray>");
            } else {
                MessageUtils.sendMessage(player, ranking.getColor() + ranking.getName() + " <gray>- No estás en el ranking</gray>");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerJoinTimes.put(player.getUniqueId(), System.currentTimeMillis());

        if (!playerStats.containsKey(player.getUniqueId())) {
            playerStats.put(player.getUniqueId(), new HashMap<>());
        }

        if (!seasonalStats.containsKey(player.getUniqueId())) {
            seasonalStats.put(player.getUniqueId(), new HashMap<>());
        }

        if (!playerAchievements.containsKey(player.getUniqueId())) {
            playerAchievements.put(player.getUniqueId(), new HashSet<>());
        }

        // Welcome message with current season info
        new BukkitRunnable() {
            @Override
            public void run() {
                MessageUtils.sendMessage(player, "<gold>Bienvenido a la Temporada " + currentSeason + "!</gold>");
                long daysRemaining = 30 - ChronoUnit.DAYS.between(currentSeasonStart, LocalDateTime.now());
                MessageUtils.sendMessage(player, "<aqua>Quedan " + daysRemaining + " días en la temporada actual.</aqua>");

                if (!activeCompetitions.isEmpty()) {
                    MessageUtils.sendMessage(player, "<yellow>Hay " + activeCompetitions.size() + " competencias activas. ¡Usa /competitions para verlas!</yellow>");
                }
            }
        }.runTaskLater(plugin, 60L); // Delay 3 seconds
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerJoinTimes.containsKey(player.getUniqueId())) {
            long joinTime = playerJoinTimes.get(player.getUniqueId());
            long playTime = System.currentTimeMillis() - joinTime;

            double currentPlayTime = playerStats.getOrDefault(player.getUniqueId(), new HashMap<>())
                    .getOrDefault("tiempo_jugado", 0.0);

            updatePlayerStat(player, "tiempo_jugado", currentPlayTime + (playTime / 1000.0));

            playerJoinTimes.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player killer = event.getEntity().getKiller();
        incrementPlayerStat(killer, "mobs_asesinados", 1.0);

        // Special handling for boss kills
        if (event.getEntity().getCustomName() != null &&
                event.getEntity().getCustomName().contains("Boss")) {
            incrementPlayerStat(killer, "bosses_derrotados", 1.0);

            // Announce boss kill
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                MessageUtils.sendMessage(player, "<gold>" + killer.getName() + " ha derrotado a un jefe!</gold>");
            }
        }

        // PvP kills
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            incrementPlayerStat(killer, "pvp_kills", 1.0);

            MessageUtils.sendMessage(killer, "<red>Has derrotado a " + victim.getName() + " en combate PvP!</red>");
            MessageUtils.sendMessage(victim, "<gray>Has sido derrotado por " + killer.getName() + ".</gray>");
        }
    }

    public Map<String, Ranking> getRankings() {
        return rankings;
    }

    public Map<UUID, Map<String, Double>> getPlayerStats() {
        return playerStats;
    }

    // Enhanced public methods for system integration
    public Map<String, Ranking> getSeasonalRankings() {
        return seasonalRankings;
    }

    public Map<UUID, Map<String, Double>> getSeasonalStats() {
        return seasonalStats;
    }

    public Map<String, Competition> getActiveCompetitions() {
        return activeCompetitions;
    }

    public Map<UUID, Set<String>> getPlayerAchievements() {
        return playerAchievements;
    }

    public int getCurrentSeason() {
        return currentSeason;
    }

    public LocalDateTime getCurrentSeasonStart() {
        return currentSeasonStart;
    }

    // Methods for other systems to call
    public void onQuestCompleted(Player player) {
        incrementPlayerStat(player, "misiones_completadas", 1.0);
    }

    public void onDungeonCompleted(Player player) {
        incrementPlayerStat(player, "dungeons_completadas", 1.0);
    }

    public void onBossDefeated(Player player) {
        incrementPlayerStat(player, "bosses_derrotados", 1.0);
    }

    public void onPetCompetitionWon(Player player) {
        incrementPlayerStat(player, "pet_competitions_ganadas", 1.0);
    }

    public void onMoneyGained(Player player, double amount) {
        updateSeasonalStat(player, "dinero_ganado", amount);
    }

    // Force update ranking for immediate display
    public void forceUpdateRanking(String rankingId) {
        lastRankingUpdate.put(rankingId, System.currentTimeMillis());
    }

    // Check if player has achievement
    public boolean hasAchievement(Player player, String achievement) {
        return playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(achievement);
    }

    // Get player's rank in specific ranking
    public int getPlayerRank(Player player, String rankingId) {
        List<RankingEntry> entries = getRanking(rankingId, 1000);

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getPlayerId().equals(player.getUniqueId())) {
                return i + 1;
            }
        }

        return -1; // Not ranked
    }

    public static class Ranking {
        private final String id;
        private final String name;
        private final String description;
        private final String color;
        private final java.util.function.Function<Player, Double> valueFunction;

        public Ranking(String id, String name, String description, String color,
                       java.util.function.Function<Player, Double> valueFunction) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.color = color;
            this.valueFunction = valueFunction;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getColor() { return color; }
        public java.util.function.Function<Player, Double> getValueFunction() { return valueFunction; }
    }

    public static class RankingEntry {
        private final UUID playerId;
        private final String playerName;
        private final double value;

        public RankingEntry(UUID playerId, String playerName, double value) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.value = value;
        }

        public UUID getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public double getValue() { return value; }
    }

    public static class Competition {
        private final String id;
        private final String name;
        private final String statType;
        private final long endTime;
        private final List<CompetitionReward> rewards;

        public Competition(String id, String name, String statType, long endTime, List<CompetitionReward> rewards) {
            this.id = id;
            this.name = name;
            this.statType = statType;
            this.endTime = endTime;
            this.rewards = rewards;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getStatType() { return statType; }
        public long getEndTime() { return endTime; }
        public List<CompetitionReward> getRewards() { return rewards; }
    }

    public static class CompetitionReward {
        private final int position;
        private final double money;
        private final ItemStack item;
        private final String achievement;

        public CompetitionReward(int position, double money, ItemStack item, String achievement) {
            this.position = position;
            this.money = money;
            this.item = item;
            this.achievement = achievement;
        }

        public int getPosition() { return position; }
        public double getMoney() { return money; }
        public ItemStack getItem() { return item; }
        public String getAchievement() { return achievement; }
    }
}