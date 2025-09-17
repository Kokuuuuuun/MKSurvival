package com.mk.mksurvival.managers.dungeons;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.managers.skills.PlayerSkills;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DungeonManager implements Listener {
    private final MKSurvival plugin;
    private final Map<String, DungeonTemplate> dungeonTemplates = new HashMap<>();
    private final Map<UUID, DungeonInstance> activeDungeons = new HashMap<>();
    private final Map<UUID, DungeonInstance> playerInstances = new HashMap<>();
    private final Map<UUID, BukkitTask> instanceTasks = new HashMap<>();
    private final Map<String, Location> dungeonEntrances = new HashMap<>();

    public DungeonManager(MKSurvival plugin) {
        this.plugin = plugin;
        setupDungeonTemplates();
        setupEntrances();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startInstanceCleaner();
    }

    private void setupDungeonTemplates() {
        // Zombie Crypt - Beginner dungeon
        List<DungeonWave> zombieWaves = Arrays.asList(
                new DungeonWave(1, Arrays.asList(
                        new DungeonMob(EntityType.ZOMBIE, 3, 40.0, 8.0, "<dark_green>Zombi Pútrido"),
                        new DungeonMob(EntityType.ZOMBIE, 1, 60.0, 12.0, "<dark_red>Zombi Guerrero")
                )),
                new DungeonWave(2, Arrays.asList(
                        new DungeonMob(EntityType.ZOMBIE, 4, 45.0, 10.0, "<dark_green>Zombi Hambriento"),
                        new DungeonMob(EntityType.ZOMBIE_VILLAGER, 2, 50.0, 8.0, "<gold>Aldeano Infectado")
                )),
                new DungeonWave(3, Arrays.asList(
                        new DungeonMob(EntityType.ZOMBIE, 1, 120.0, 20.0, "<dark_red><bold>REY ZOMBI DE LA CRIPTA")
                ))
        );

        List<ItemStack> zombieRewards = Arrays.asList(
                createCustomItem(Material.IRON_SWORD, "<green>Espada Bendita", "<gray>Daño extra contra no-muertos"),
                createCustomItem(Material.GOLDEN_APPLE, "<gold>Manzana Dorada", "<gray>Restaura salud completamente"),
                createCustomItem(Material.EMERALD, "<green>Esmeralda de la Cripta", "<gray>Moneda valiosa")
        );

        dungeonTemplates.put("zombie_crypt", new DungeonTemplate(
                "zombie_crypt",
                "Cripta de Zombis",
                "Una antigua cripta infestada de no-muertos",
                10, // min level
                25, // max level
                DungeonDifficulty.EASY,
                1800, // 30 minutes
                5, // max players
                zombieWaves,
                zombieRewards,
                1000.0, // money reward
                200 // experience reward
        ));

        // Skeleton Tower - Intermediate dungeon
        List<DungeonWave> skeletonWaves = Arrays.asList(
                new DungeonWave(1, Arrays.asList(
                        new DungeonMob(EntityType.SKELETON, 3, 60.0, 12.0, "<gray>Arquero Esquelético"),
                        new DungeonMob(EntityType.STRAY, 2, 50.0, 10.0, "<aqua> Arquero Helado")
                )),
                new DungeonWave(2, Arrays.asList(
                        new DungeonMob(EntityType.SKELETON, 2, 80.0, 15.0, "<gray>Guardia Esquelético"),
                        new DungeonMob(EntityType.WITHER_SKELETON, 1, 100.0, 20.0, "<dark_gray>Esqueleto Marchito"),
                        new DungeonMob(EntityType.SKELETON, 3, 65.0, 12.0, "<gray>Francotirador")
                )),
                new DungeonWave(3, Arrays.asList(
                        new DungeonMob(EntityType.WITHER_SKELETON, 1, 200.0, 35.0, "<dark_gray><bold>SEÑOR DE LA TORRE")
                ))
        );

        List<ItemStack> skeletonRewards = Arrays.asList(
                createCustomItem(Material.BOW, "<aqua>Arco de Precisión", "<gray>Nunca falla su objetivo"),
                createCustomItem(Material.ARROW, "<white>Flechas Espectrales", "<gray>Flechas mágicas", "<gray>Cantidad: 32"),
                createCustomItem(Material.BONE, "<white>Hueso Encantado", "<gray>Material mágico especial")
        );

        dungeonTemplates.put("skeleton_tower", new DungeonTemplate(
                "skeleton_tower",
                "Torre de Esqueletos",
                "Una torre antigua habitada por arqueros espectrales",
                20, // min level
                40, // max level
                DungeonDifficulty.MEDIUM,
                2400, // 40 minutes
                4, // max players
                skeletonWaves,
                skeletonRewards,
                2000.0, // money reward
                400 // experience reward
        ));

        // Dragon's Lair - Hard dungeon
        List<DungeonWave> dragonWaves = Arrays.asList(
                new DungeonWave(1, Arrays.asList(
                        new DungeonMob(EntityType.BLAZE, 4, 80.0, 18.0, "<gold>Guardián de Fuego"),
                        new DungeonMob(EntityType.MAGMA_CUBE, 3, 60.0, 12.0, "<dark_red>Cubo de Magma")
                )),
                new DungeonWave(2, Arrays.asList(
                        new DungeonMob(EntityType.WITHER_SKELETON, 3, 120.0, 25.0, "<dark_gray>Guardián Marchito"),
                        new DungeonMob(EntityType.BLAZE, 2, 100.0, 22.0, "<gold>Señor del Fuego")
                )),
                new DungeonWave(3, Arrays.asList(
                        new DungeonMob(EntityType.ENDER_DRAGON, 1, 500.0, 50.0, "<dark_purple><bold>DRAGÓN ANCIANO")
                ))
        );

        List<ItemStack> dragonRewards = Arrays.asList(
                createCustomItem(Material.NETHERITE_SWORD, "<dark_purple>Colmillo de Dragón", "<gray>Espada legendaria", "<gray>del dragón anciano"),
                createCustomItem(Material.ELYTRA, "<dark_purple>Alas de Dragón", "<gray>Permite volar como un dragón"),
                createCustomItem(Material.DRAGON_HEAD, "<dark_purple>Cabeza de Dragón", "<gray>Trofeo legendario")
        );

        dungeonTemplates.put("dragon_lair", new DungeonTemplate(
                "dragon_lair",
                "Guarida del Dragón",
                "La guarida del dragón más antiguo y poderoso",
                40, // min level
                100, // max level
                DungeonDifficulty.HARD,
                3600, // 60 minutes
                3, // max players
                dragonWaves,
                dragonRewards,
                5000.0, // money reward
                1000 // experience reward
        ));
    }

    private ItemStack createCustomItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.parse(name));
            if (lore.length > 0) {
                meta.lore(MessageUtils.parseList(Arrays.asList(lore)));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setupEntrances() {
        World world = plugin.getServer().getWorlds().get(0);

        // Setup entrance locations for each dungeon
        dungeonEntrances.put("zombie_crypt", new Location(world, 500, 70, 500));
        dungeonEntrances.put("skeleton_tower", new Location(world, -500, 70, 500));
        dungeonEntrances.put("dragon_lair", new Location(world, 0, 70, -500));

        // TODO: Load from configuration or create entrance portals
        // createEntrancePortals();
    }

    private void startInstanceCleaner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredInstances();
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Every minute
    }

    private void cleanupExpiredInstances() {
        long currentTime = System.currentTimeMillis();
        List<UUID> expiredInstances = new ArrayList<>();

        for (Map.Entry<UUID, DungeonInstance> entry : activeDungeons.entrySet()) {
            DungeonInstance instance = entry.getValue();
            if (currentTime - instance.startTime > (instance.template.timeLimit * 1000L)) {
                expiredInstances.add(entry.getKey());
            }
        }

        for (UUID instanceId : expiredInstances) {
            DungeonInstance instance = activeDungeons.get(instanceId);
            if (instance != null) {
                endDungeon(instance, false, "¡Tiempo agotado!");
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.NETHER_PORTAL) {
            // Check if player clicked on a dungeon entrance
            Location clickedLoc = event.getClickedBlock().getLocation();

            for (Map.Entry<String, Location> entry : dungeonEntrances.entrySet()) {
                if (isNearLocation(clickedLoc, entry.getValue(), 5)) {
                    event.setCancelled(true);
                    openDungeonMenu(player, entry.getKey());
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Check if this mob belongs to a dungeon instance
        for (DungeonInstance instance : activeDungeons.values()) {
            if (instance.activeMobs.contains(entity.getUniqueId())) {
                instance.activeMobs.remove(entity.getUniqueId());
                instance.mobsKilled++;

                // Clear default drops
                event.getDrops().clear();
                event.setDroppedExp(0);

                // Check if wave is complete
                checkWaveCompletion(instance);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Remove player from any active dungeon
        DungeonInstance instance = playerInstances.get(playerId);
        if (instance != null) {
            removeDungeonPlayer(instance, player);
        }
    }

    private boolean isNearLocation(Location loc1, Location loc2, double radius) {
        return loc1.getWorld().equals(loc2.getWorld()) && loc1.distance(loc2) <= radius;
    }

    private void openDungeonMenu(Player player, String dungeonId) {
        DungeonTemplate template = dungeonTemplates.get(dungeonId);
        if (template == null) {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] Mazmorra no encontrada.");
            return;
        }

        // Check level requirement
        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        int combatLevel = skills.getLevel(SkillType.COMBAT);

        if (combatLevel < template.minLevel) {
            MessageUtils.sendMessage(player,
                    "<red>[Mazmorra] Necesitas nivel " + template.minLevel + " de combate para entrar.\n" +
                            "<gray>Tu nivel actual: " + combatLevel);
            return;
        }

        if (combatLevel > template.maxLevel) {
            MessageUtils.sendMessage(player,
                    "<red>[Mazmorra] Has superado el nivel máximo (" + template.maxLevel + ") para esta mazmorra.\n" +
                            "<gray>Tu nivel actual: " + combatLevel);
            return;
        }

        // Check if player is already in a dungeon
        if (playerInstances.containsKey(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] Ya estás en una mazmorra.");
            return;
        }

        // Show dungeon info and options
        MessageUtils.sendMessage(player,
                "<gold><bold>=== " + template.name + " ===\n" +
                        "<gray>" + template.description + "\n" +
                        "<gray>Dificultad: " + getDifficultyColor(template.difficulty) + template.difficulty.name() + "\n" +
                        "<gray>Nivel: " + template.minLevel + "-" + template.maxLevel + "\n" +
                        "<gray>Tiempo límite: " + (template.timeLimit / 60) + " minutos\n" +
                        "<gray>Máx jugadores: " + template.maxPlayers + "\n" +
                        "<gray>Oleadas: " + template.waves.size() + "\n" +
                        "<green>\nEscribe '/dungeon join " + dungeonId + "' para entrar");
    }

    private String getDifficultyColor(DungeonDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "<green>";
            case MEDIUM -> "<yellow>";
            case HARD -> "<red>";
            case NIGHTMARE -> "<dark_purple>";
        };
    }

    public boolean joinDungeon(Player player, String dungeonId) {
        DungeonTemplate template = dungeonTemplates.get(dungeonId);
        if (template == null) {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] Mazmorra no encontrada.");
            return false;
        }

        // Verify requirements again
        PlayerSkills skills = plugin.getSkillManager().getPlayerSkills(player.getUniqueId());
        int combatLevel = skills.getLevel(SkillType.COMBAT);

        if (combatLevel < template.minLevel || combatLevel > template.maxLevel) {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] No cumples los requisitos de nivel.");
            return false;
        }

        if (playerInstances.containsKey(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] Ya estás en una mazmorra.");
            return false;
        }

        // Create new instance or join existing
        DungeonInstance instance = findOrCreateInstance(template);
        if (instance == null) {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] No se pudo crear la instancia.");
            return false;
        }

        if (instance.players.size() >= template.maxPlayers) {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] La instancia está llena.");
            return false;
        }

        // Add player to instance
        addPlayerToInstance(instance, player);
        return true;
    }

    private DungeonInstance findOrCreateInstance(DungeonTemplate template) {
        // Look for existing instance with space
        for (DungeonInstance instance : activeDungeons.values()) {
            if (instance.template.id.equals(template.id) &&
                    instance.players.size() < template.maxPlayers &&
                    instance.currentWave == 0) { // Only join before dungeon starts
                return instance;
            }
        }

        // Create new instance
        return createNewInstance(template);
    }

    private DungeonInstance createNewInstance(DungeonTemplate template) {
        UUID instanceId = UUID.randomUUID();
        Location instanceLocation = generateInstanceLocation();

        DungeonInstance instance = new DungeonInstance(
                instanceId, template, instanceLocation
        );

        activeDungeons.put(instanceId, instance);
        return instance;
    }

    private Location generateInstanceLocation() {
        World world = plugin.getServer().getWorlds().get(0);
        // Generate random location for instance (could be in separate world)
        int x = ThreadLocalRandom.current().nextInt(-10000, 10000);
        int z = ThreadLocalRandom.current().nextInt(-10000, 10000);
        return new Location(world, x, 100, z);
    }

    private void addPlayerToInstance(DungeonInstance instance, Player player) {
        instance.players.add(player.getUniqueId());
        playerInstances.put(player.getUniqueId(), instance);

        // Teleport player to instance
        player.teleport(instance.spawnLocation);

        // Send welcome message
        MessageUtils.sendMessage(player,
                "<gold><bold>=== MAZMORRA INICIADA ===\n" +
                        "<yellow>" + instance.template.name + "\n" +
                        "<gray>Jugadores: " + instance.players.size() + "/" + instance.template.maxPlayers + "\n" +
                        "<gray>Tiempo límite: " + (instance.template.timeLimit / 60) + " minutos\n" +
                        "<green>¡Prepárate para la batalla!");

        // Give starting effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false)); // 10 seconds protection

        // If this is the first player, start countdown
        if (instance.players.size() == 1) {
            startDungeonCountdown(instance);
        }

        // Notify other players
        notifyInstancePlayers(instance,
                "<green>" + player.getName() + " se ha unido a la mazmorra!",
                player.getUniqueId());
    }

    private void removeDungeonPlayer(DungeonInstance instance, Player player) {
        instance.players.remove(player.getUniqueId());
        playerInstances.remove(player.getUniqueId());

        // Teleport player back to spawn
        Location spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        player.teleport(spawn);

        // Notify remaining players
        notifyInstancePlayers(instance,
                "<red>" + player.getName() + " ha abandonado la mazmorra!", null);

        // If no players left, end dungeon
        if (instance.players.isEmpty()) {
            endDungeon(instance, false, "Todos los jugadores han abandonado la mazmorra.");
        }
    }

    private void startDungeonCountdown(DungeonInstance instance) {
        BukkitTask countdownTask = new BukkitRunnable() {
            int countdown = 30; // 30 seconds

            @Override
            public void run() {
                if (instance.players.isEmpty()) {
                    this.cancel();
                    return;
                }

                if (countdown <= 0) {
                    this.cancel();
                    startFirstWave(instance);
                    return;
                }

                if (countdown <= 10 || countdown % 10 == 0) {
                    notifyInstancePlayers(instance,
                            "<yellow><bold>La mazmorra comenzará en " + countdown + " segundos!", null);
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        instanceTasks.put(instance.instanceId, countdownTask);
    }

    private void startFirstWave(DungeonInstance instance) {
        instance.currentWave = 1;
        instance.waveStartTime = System.currentTimeMillis();

        notifyInstancePlayers(instance,
                "<red><bold>¡OLEADA " + instance.currentWave + " INICIADA!\n" +
                        "<gray>Prepárate para luchar!", null);

        spawnWaveMobs(instance);
        startInstanceTimer(instance);
    }

    private void spawnWaveMobs(DungeonInstance instance) {
        if (instance.currentWave > instance.template.waves.size()) {
            // Dungeon completed!
            endDungeon(instance, true, "¡Mazmorra completada!");
            return;
        }

        DungeonWave wave = instance.template.waves.get(instance.currentWave - 1);
        Location spawnLoc = instance.spawnLocation.clone().add(0, 1, 0);

        for (DungeonMob mobTemplate : wave.mobs) {
            for (int i = 0; i < mobTemplate.count; i++) {
                // Spawn mob with random offset
                Location mobSpawn = spawnLoc.clone().add(
                        ThreadLocalRandom.current().nextDouble(-5, 5),
                        0,
                        ThreadLocalRandom.current().nextDouble(-5, 5)
                );

                LivingEntity mob = (LivingEntity) mobSpawn.getWorld().spawnEntity(mobSpawn, mobTemplate.type);

                // Add mob to active mobs list
                instance.activeMobs.add(mob.getUniqueId());
                instance.totalMobsInWave++;
            }
        }
    }

    private void checkWaveCompletion(DungeonInstance instance) {
        if (instance.activeMobs.isEmpty()) {
            // Wave completed!
            completeWave(instance);
        } else {
            // Update progress
            int remaining = instance.activeMobs.size();
            int killed = instance.totalMobsInWave - remaining;

            notifyInstancePlayers(instance,
                    "<gray>Progreso: " + killed + "/" + instance.totalMobsInWave + " enemigos derrotados", null);
        }
    }

    private void completeWave(DungeonInstance instance) {
        long waveTime = System.currentTimeMillis() - instance.waveStartTime;

        notifyInstancePlayers(instance,
                "<green><bold>¡OLEADA " + instance.currentWave + " COMPLETADA!\n" +
                        "<gray>Tiempo: " + (waveTime / 1000) + " segundos", null);

        // Give players rewards and rest time
        giveWaveRewards(instance);

        if (instance.currentWave >= instance.template.waves.size()) {
            // Dungeon completed!
            new BukkitRunnable() {
                @Override
                public void run() {
                    endDungeon(instance, true, "¡Mazmorra completada con éxito!");
                }
            }.runTaskLater(plugin, 100L); // 5 seconds delay
        } else {
            // Start next wave after delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    startNextWave(instance);
                }
            }.runTaskLater(plugin, 200L); // 10 seconds delay
        }
    }

    private void startNextWave(DungeonInstance instance) {
        instance.currentWave++;
        instance.totalMobsInWave = 0;
        instance.waveStartTime = System.currentTimeMillis();

        notifyInstancePlayers(instance,
                "<yellow><bold>¡OLEADA " + instance.currentWave + " INICIADA!\n" +
                        "<gray>¡Los enemigos se vuelven más fuertes!", null);

        spawnWaveMobs(instance);
    }

    private void giveWaveRewards(DungeonInstance instance) {
        for (UUID playerId : instance.players) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Calculate wave rewards
                int waveXP = instance.template.experienceReward / 5; // 1/5 of total per wave
                int waveMoney = (int) (instance.template.moneyReward / 5);

                // Give rewards
                plugin.getEconomyManager().addBalance(player.getUniqueId(), waveMoney * 0.8);
                plugin.getSkillManager().addExperience(player.getUniqueId(), SkillType.COMBAT, waveXP);

                // Heal player
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);

                MessageUtils.sendMessage(player,
                        "<green><bold>+ " + waveXP + " XP\n" +
                                "<yellow><bold>+ " + plugin.getEconomyManager().formatCurrency(waveMoney));
            }
        }
    }

    private void startInstanceTimer(DungeonInstance instance) {
        BukkitTask timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - instance.startTime;
                long remaining = (instance.template.timeLimit * 1000L) - elapsed;

                if (remaining <= 0) {
                    this.cancel();
                    endDungeon(instance, false, "¡Tiempo agotado!");
                    return;
                }

                // Show time warnings
                if (remaining <= 300000 && remaining > 295000) { // 5 minutes left
                    notifyInstancePlayers(instance, "<red><bold>¡Quedan 5 minutos!", null);
                } else if (remaining <= 60000 && remaining > 55000) { // 1 minute left
                    notifyInstancePlayers(instance, "<red><bold>¡Queda 1 minuto!", null);
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Every 5 seconds

        instanceTasks.put(instance.instanceId, timerTask);
    }

    private void endDungeon(DungeonInstance instance, boolean success, String reason) {
        // Cancel any running tasks
        BukkitTask task = instanceTasks.remove(instance.instanceId);
        if (task != null) {
            task.cancel();
        }

        // Clean up mobs
        for (UUID mobId : instance.activeMobs) {
            LivingEntity mob = (LivingEntity) plugin.getServer().getEntity(mobId);
            if (mob != null) {
                mob.remove();
            }
        }

        // Handle players
        List<Player> players = new ArrayList<>();
        for (UUID playerId : instance.players) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                players.add(player);
                playerInstances.remove(playerId);

                // Teleport back to spawn
                Location spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
                player.teleport(spawn);
            }
        }

        // Give final rewards if successful
        if (success && !players.isEmpty()) {
            giveFinalRewards(instance, players);
        }

        // Announce result
        String message = success ?
                "<green><bold>✓ " + reason + " ✓" :
                "<red><bold>✗ " + reason + " ✗";

        for (Player player : players) {
            MessageUtils.sendMessage(player, message);
            player.sendTitle(
                    success ? "<green><bold>MIZMORR COMPLETADA" : "<red><bold>MIZMORR FALLIDA",
                    reason,
                    10, 60, 20
            );
        }

        // Remove instance
        activeDungeons.remove(instance.instanceId);
    }

    private void giveFinalRewards(DungeonInstance instance, List<Player> players) {
        for (Player player : players) {
            // Give main rewards
            plugin.getEconomyManager().depositBalance(player, instance.template.moneyReward);
            plugin.getSkillManager().addExperience(player.getUniqueId(), SkillType.COMBAT, instance.template.experienceReward);

            // Give random item from loot table
            if (!instance.template.rewards.isEmpty()) {
                ItemStack reward = instance.template.rewards.get(
                        ThreadLocalRandom.current().nextInt(instance.template.rewards.size())
                ).clone();
                player.getInventory().addItem(reward);
            }

            MessageUtils.sendMessage(player,
                    "<green><bold>✨ RECOMPENSAS FINALES ✨\n" +
                            "<yellow>+ " + plugin.getEconomyManager().formatCurrency(instance.template.moneyReward) + "\n" +
                            "<aqua>+ " + instance.template.experienceReward + " XP de Combate\n" +
                            "<gray>+ Íem especial obtenido");
        }
    }

    private void notifyInstancePlayers(DungeonInstance instance, String message, UUID excludePlayer) {
        for (UUID playerId : instance.players) {
            if (excludePlayer != null && playerId.equals(excludePlayer)) {
                continue;
            }

            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                MessageUtils.sendMessage(player, message);
            }
        }
    }

    // Public methods for external access
    public Map<String, DungeonTemplate> getDungeonTemplates() {
        return new HashMap<>(dungeonTemplates);
    }

    public Map<UUID, DungeonInstance> getActiveDungeons() {
        return new HashMap<>(activeDungeons);
    }

    public DungeonInstance getPlayerInstance(UUID playerId) {
        return playerInstances.get(playerId);
    }

    public boolean isPlayerInDungeon(UUID playerId) {
        return playerInstances.containsKey(playerId);
    }

    public void leaveDungeon(Player player) {
        DungeonInstance instance = playerInstances.get(player.getUniqueId());
        if (instance != null) {
            removeDungeonPlayer(instance, player);
            MessageUtils.sendMessage(player, "<green>[Mazmorra] Has abandonado la mazmorra.");
        } else {
            MessageUtils.sendMessage(player, "<red>[Mazmorra] No estás en ninguna mazmorra.");
        }
    }

    // Dungeon system classes
    public static class DungeonTemplate {
        public final String id;
        public final String name;
        public final String description;
        public final int minLevel;
        public final int maxLevel;
        public final DungeonDifficulty difficulty;
        public final int timeLimit; // in seconds
        public final int maxPlayers;
        public final List<DungeonWave> waves;
        public final List<ItemStack> rewards;
        public final double moneyReward;
        public final int experienceReward;

        public DungeonTemplate(String id, String name, String description, int minLevel, int maxLevel,
                               DungeonDifficulty difficulty, int timeLimit, int maxPlayers,
                               List<DungeonWave> waves, List<ItemStack> rewards,
                               double moneyReward, int experienceReward) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.difficulty = difficulty;
            this.timeLimit = timeLimit;
            this.maxPlayers = maxPlayers;
            this.waves = new ArrayList<>(waves);
            this.rewards = new ArrayList<>(rewards);
            this.moneyReward = moneyReward;
            this.experienceReward = experienceReward;
        }
    }

    public static class DungeonInstance {
        public final UUID instanceId;
        public final DungeonTemplate template;
        public final Location spawnLocation;
        public final Set<UUID> players = new HashSet<>();
        public final Set<UUID> activeMobs = new HashSet<>();
        public final long startTime;
        public int currentWave = 0;
        public long waveStartTime;
        public int mobsKilled = 0;
        public int totalMobsInWave = 0;

        public DungeonInstance(UUID instanceId, DungeonTemplate template, Location spawnLocation) {
            this.instanceId = instanceId;
            this.template = template;
            this.spawnLocation = spawnLocation.clone();
            this.startTime = System.currentTimeMillis();
        }
    }

    public static class DungeonWave {
        public final int waveNumber;
        public final List<DungeonMob> mobs;

        public DungeonWave(int waveNumber, List<DungeonMob> mobs) {
            this.waveNumber = waveNumber;
            this.mobs = new ArrayList<>(mobs);
        }
    }

    public static class DungeonMob {
        public final EntityType type;
        public final int count;
        public final double health;
        public final double damage;
        public final String name;

        public DungeonMob(EntityType type, int count, double health, double damage, String name) {
            this.type = type;
            this.count = count;
            this.health = health;
            this.damage = damage;
            this.name = name;
        }
    }

    public enum DungeonDifficulty {
        EASY,
        MEDIUM,
        HARD,
        NIGHTMARE
    }
}