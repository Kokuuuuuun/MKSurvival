package com.mk.mksurvival.managers.bosses;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.utils.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BossManager implements Listener {
    private final MKSurvival plugin;
    private final Map<String, Boss> bosses = new HashMap<>();
    private final Map<String, Location> bossSpawnPoints = new HashMap<>();
    private final Map<UUID, BossInstance> activeBosses = new HashMap<>();
    private final Map<UUID, BukkitTask> bossTasks = new HashMap<>();
    private final Map<String, Long> lastSpawnTime = new HashMap<>();
    private BukkitTask spawnScheduler;

    public BossManager(MKSurvival plugin) {
        this.plugin = plugin;
        setupBosses();
        setupSpawnPoints();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startBossSpawningSchedule();
    }

    private void setupBosses() {
        // Rey Zombi - Boss de nivel alto
        List<ItemStack> zombieKingDrops = new ArrayList<>();
        zombieKingDrops.add(createCustomItem(Material.NETHERITE_SWORD, "<dark_red>Espada del Rey Zombi", "<gray>Una espada maldita", "<gray>que drena la vida de los enemigos"));
        zombieKingDrops.add(createCustomItem(Material.EMERALD, "<green>Esmeralda Real", "<gray>Una gema valiosa del tesoro real"));
        zombieKingDrops.add(createCustomItem(Material.ZOMBIE_HEAD, "<dark_red>Cabeza del Rey Zombi", "<gray>Trofeo de un enemigo poderoso"));
        
        bosses.put("zombie_king", new Boss(
                "Rey Zombi",
                "zombie_king",
                EntityType.ZOMBIE,
                50,
                800.0, // health
                20.0,  // damage
                0.25,  // speed
                zombieKingDrops,
                1000.0, // money reward
                150,    // experience
                7200,   // respawn time (6 hours)
                Arrays.asList(
                    "<red><bold>REY ZOMBI",
                    "<dark_red>❤ Vida: <red>800",
                    "<gold>⚔ Nivel: <yellow>50"
                ),
                Arrays.asList(
                    BossAbility.LIFE_STEAL,
                    BossAbility.SUMMON_MINIONS,
                    BossAbility.POISON_AURA
                )
        ));

        // Señor Esqueleto - Boss de rango medio
        List<ItemStack> skeletonLordDrops = new ArrayList<>();
        skeletonLordDrops.add(createCustomItem(Material.BOW, "<aqua>Arco del Señor Esqueleto", "<gray>Un arco encantado", "<gray>con precisión mortal"));
        skeletonLordDrops.add(createCustomItem(Material.ARROW, "<white>Flechas Espectrales", "<gray>Flechas imbuidas con magia oscura"));
        skeletonLordDrops.add(createCustomItem(Material.BONE, "<white>Hueso Reforzado", "<gray>Material para crear objetos especiales"));
        
        bosses.put("skeleton_lord", new Boss(
                "Señor Esqueleto",
                "skeleton_lord",
                EntityType.SKELETON,
                35,
                400.0, // health
                15.0,  // damage
                0.3,   // speed
                skeletonLordDrops,
                500.0, // money reward
                100,   // experience
                5400,  // respawn time (4.5 hours)
                Arrays.asList(
                    "<yellow><bold>SEÑOR ESQUELETO",
                    "<dark_red>❤ Vida: <red>400",
                    "<gold>⚔ Nivel: <yellow>35"
                ),
                Arrays.asList(
                    BossAbility.MULTI_SHOT,
                    BossAbility.TELEPORT_STRIKE,
                    BossAbility.BONE_WALL
                )
        ));

        // Dragón de Fuego - Boss épico
        List<ItemStack> fireDragonDrops = new ArrayList<>();
        fireDragonDrops.add(createCustomItem(Material.ELYTRA, "<red>Alas de Dragón", "<gray>Alas que permiten volar", "<gray>como un verdadero dragón"));
        fireDragonDrops.add(createCustomItem(Material.DRAGON_HEAD, "<dark_red>Cabeza de Dragón", "<gray>Trofeo supremo de un dragón"));
        fireDragonDrops.add(createCustomItem(Material.FIRE_CHARGE, "<red>Corazón de Fuego", "<gray>El corazón ardiente del dragón"));
        
        bosses.put("fire_dragon", new Boss(
                "Dragón de Fuego",
                "fire_dragon",
                EntityType.ENDER_DRAGON,
                75,
                1500.0, // health
                35.0,   // damage
                0.4,    // speed
                fireDragonDrops,
                2500.0, // money reward
                300,    // experience
                14400,  // respawn time (12 hours)
                Arrays.asList(
                    "<dark_red><bold>DRAGÓN DE FUEGO",
                    "<dark_red>❤ Vida: <red>1500",
                    "<gold>⚔ Nivel: <yellow>75"
                ),
                Arrays.asList(
                    BossAbility.FIRE_BREATH,
                    BossAbility.METEOR_RAIN,
                    BossAbility.FLAME_BARRIER
                )
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

    private void setupSpawnPoints() {
        World world = plugin.getServer().getWorlds().get(0);
        
        // Configurar puntos de spawn para cada boss
        bossSpawnPoints.put("zombie_king", new Location(world, 100, 70, 100));
        bossSpawnPoints.put("skeleton_lord", new Location(world, -100, 70, -100));
        bossSpawnPoints.put("fire_dragon", new Location(world, 0, 100, 0));
        
        // TODO: Cargar ubicaciones desde configuración
        // loadSpawnPointsFromConfig();
    }

    private void startBossSpawningSchedule() {
        spawnScheduler = new BukkitRunnable() {
            @Override
            public void run() {
                trySpawnRandomBoss();
            }
        }.runTaskTimer(plugin, 6000L, 6000L); // Check every 5 minutes
    }

    private void trySpawnRandomBoss() {
        List<String> availableBosses = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        for (String bossId : bosses.keySet()) {
            Boss boss = bosses.get(bossId);
            long lastSpawn = lastSpawnTime.getOrDefault(bossId, 0L);
            
            // Check if enough time has passed for respawn
            if (currentTime - lastSpawn >= (boss.respawnTime * 1000L)) {
                // Check if boss is not already active
                boolean isActive = activeBosses.values().stream()
                    .anyMatch(instance -> instance.bossId.equals(bossId));
                    
                if (!isActive) {
                    availableBosses.add(bossId);
                }
            }
        }
        
        if (!availableBosses.isEmpty()) {
            String randomBoss = availableBosses.get(ThreadLocalRandom.current().nextInt(availableBosses.size()));
            spawnBoss(randomBoss);
        }
    }

    public boolean spawnBoss(String bossId) {
        Boss boss = bosses.get(bossId);
        Location spawnPoint = bossSpawnPoints.get(bossId);
        
        if (boss == null || spawnPoint == null) {
            return false;
        }
        
        // Check if boss is already active
        boolean isActive = activeBosses.values().stream()
            .anyMatch(instance -> instance.bossId.equals(bossId));
        if (isActive) {
            return false;
        }
        
        // Spawn the boss entity
        LivingEntity entity = (LivingEntity) spawnPoint.getWorld().spawnEntity(spawnPoint, boss.entityType);
        
        // Create boss instance
        BossInstance instance = new BossInstance(bossId, entity.getUniqueId(), spawnPoint);
        activeBosses.put(entity.getUniqueId(), instance);
        
        // Apply boss attributes and setup
        applyBossAttributes(entity, boss);
        setupBossName(entity, boss);
        
        // Start boss AI and abilities
        startBossAI(instance, boss);
        
        // Record spawn time
        lastSpawnTime.put(bossId, System.currentTimeMillis());
        
        // Announce spawn
        announceBossSpawn(boss, spawnPoint);
        
        return true;
    }

    private void applyBossAttributes(LivingEntity entity, Boss boss) {
        // Set health
        entity.setMaxHealth(boss.health);
        entity.setHealth(boss.health);

        // Apply damage attribute
        AttributeInstance damageAttribute = getAttributeSafely(entity, "GENERIC_ATTACK_DAMAGE", "ATTACK_DAMAGE");
        if (damageAttribute != null) {
            damageAttribute.setBaseValue(boss.damage);
        }

        // Apply speed attribute
        AttributeInstance speedAttribute = getAttributeSafely(entity, "GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED");
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(boss.speed);
        }

        // Apply knockback resistance
        AttributeInstance knockbackAttribute = getAttributeSafely(entity, "GENERIC_KNOCKBACK_RESISTANCE", "KNOCKBACK_RESISTANCE");
        if (knockbackAttribute != null) {
            knockbackAttribute.setBaseValue(0.8); // High knockback resistance for bosses
        }

        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        
        // Add boss effects
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false));
    }

    private void setupBossName(LivingEntity entity, Boss boss) {
        entity.setCustomName(String.join(" ", boss.displayNames));
        entity.setCustomNameVisible(true);
    }

    private void startBossAI(BossInstance instance, Boss boss) {
        BukkitTask aiTask = new BukkitRunnable() {
            private int abilityTimer = 0;
            
            @Override
            public void run() {
                LivingEntity entity = (LivingEntity) plugin.getServer().getEntity(instance.entityId);
                if (entity == null || entity.isDead()) {
                    this.cancel();
                    activeBosses.remove(instance.entityId);
                    return;
                }
                
                // Update boss health display
                updateHealthDisplay(entity, boss);
                
                // Execute abilities
                abilityTimer++;
                if (abilityTimer >= 100) { // Execute ability every 5 seconds
                    executeRandomAbility(entity, boss);
                    abilityTimer = 0;
                }
                
                // Check for nearby players for special behavior
                checkNearbyPlayers(entity, boss);
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
        
        bossTasks.put(instance.entityId, aiTask);
    }

    private void updateHealthDisplay(LivingEntity entity, Boss boss) {
        double healthPercent = (entity.getHealth() / entity.getMaxHealth()) * 100;
        String healthBar = createHealthBar(healthPercent);
        
        List<String> nameDisplay = new ArrayList<>(boss.displayNames);
        nameDisplay.add("<green>" + healthBar + "<gray> (" + String.format("%.0f", healthPercent) + "%)");
        
        entity.setCustomName(String.join(" ", nameDisplay));
        
        // Send action bar to nearby players
        for (Entity nearby : entity.getNearbyEntities(50, 50, 50)) {
            if (nearby instanceof Player player) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent( MessageUtils.parse(("<dark_red>" + boss.name + " <gray>- " + healthBar + " <gray>(" + String.format("%.0f", healthPercent) + "%)"))));
            }
        }
    }

    private String createHealthBar(double healthPercent) {
        int bars = 20;
        int filledBars = (int) ((healthPercent / 100.0) * bars);
        
        StringBuilder healthBar = new StringBuilder();
        for (int i = 0; i < bars; i++) {
            if (i < filledBars) {
                healthBar.append("<green>█");
            } else {
                healthBar.append("<gray>█");
            }
        }
        return healthBar.toString();
    }

    private void executeRandomAbility(LivingEntity entity, Boss boss) {
        if (boss.abilities.isEmpty()) return;
        
        BossAbility ability = boss.abilities.get(ThreadLocalRandom.current().nextInt(boss.abilities.size()));
        executeAbility(entity, boss, ability);
    }

    private void executeAbility(LivingEntity entity, Boss boss, BossAbility ability) {
        switch (ability) {
            case LIFE_STEAL -> {
                // Heal boss when dealing damage
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
                announceBossAbility(boss, "<red>" + boss.name + " se está regenerando!");
            }
            case SUMMON_MINIONS -> {
                // Spawn zombie minions around the boss
                for (int i = 0; i < 3; i++) {
                    Location spawnLoc = entity.getLocation().add(
                        ThreadLocalRandom.current().nextDouble(-5, 5),
                        0,
                        ThreadLocalRandom.current().nextDouble(-5, 5)
                    );
                    Zombie minion = (Zombie) entity.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                    minion.setCustomName(MessageUtils.parse(("<dark_gray>Esbirro de " + boss.name)));
                    minion.setCustomNameVisible(true);
                }
                announceBossAbility(boss, "<red>" + boss.name + " ha invocado esbirros!");
            }
            case POISON_AURA -> {
                // Apply poison to nearby players
                for (Entity nearby : entity.getNearbyEntities(10, 10, 10)) {
                    if (nearby instanceof Player player) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    }
                }
                announceBossAbility(boss, "<dark_green>" + boss.name + " emite un aura venenosa!");
            }
            case MULTI_SHOT -> {
                // Skeleton boss shoots multiple arrows
                if (entity instanceof Skeleton skeleton) {
                    for (int i = 0; i < 5; i++) {
                        skeleton.launchProjectile(org.bukkit.entity.Arrow.class);
                    }
                }
                announceBossAbility(boss, "<yellow>" + boss.name + " dispara múltiples flechas!");
            }
            case TELEPORT_STRIKE -> {
                // Teleport to a random nearby player and attack
                List<Player> nearbyPlayers = new ArrayList<>();
                for (Entity nearby : entity.getNearbyEntities(20, 20, 20)) {
                    if (nearby instanceof Player player) {
                        nearbyPlayers.add(player);
                    }
                }
                if (!nearbyPlayers.isEmpty()) {
                    Player target = nearbyPlayers.get(ThreadLocalRandom.current().nextInt(nearbyPlayers.size()));
                    entity.teleport(target.getLocation());
                    target.damage(boss.damage * 1.5, entity);
                }
                announceBossAbility(boss, "<light_purple>" + boss.name + " se teletransporta y ataca!");
            }
            case FIRE_BREATH -> {
                // Create fire in front of the dragon
                Location loc = entity.getLocation();
                for (int i = 1; i <= 10; i++) {
                    Location fireLoc = loc.clone().add(loc.getDirection().multiply(i));
                    fireLoc.getBlock().setType(Material.FIRE);
                }
                announceBossAbility(boss, "<dark_red>" + boss.name + " exhala fuego!");
            }
            // Add more abilities as needed
        }
    }

    private void checkNearbyPlayers(LivingEntity entity, Boss boss) {
        int nearbyPlayerCount = 0;
        for (Entity nearby : entity.getNearbyEntities(30, 30, 30)) {
            if (nearby instanceof Player) {
                nearbyPlayerCount++;
            }
        }
        
        // If no players nearby for 5 minutes, despawn boss
        if (nearbyPlayerCount == 0) {
            BossInstance instance = activeBosses.get(entity.getUniqueId());
            if (instance != null) {
                instance.noPlayerTime++;
                if (instance.noPlayerTime > 300) { // 5 minutes
                    despawnBoss(entity.getUniqueId());
                }
            }
        } else {
            BossInstance instance = activeBosses.get(entity.getUniqueId());
            if (instance != null) {
                instance.noPlayerTime = 0;
            }
        }
    }

    private void announceBossAbility(Boss boss, String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            MessageUtils.sendMessage(player, message);
        }
    }

    private void announceBossSpawn(Boss boss, Location location) {
        String message = "<red><bold>✨ BOSS APAREADO ✨\n" +
                "<yellow>" + boss.name + "<gray> (Nivel " + boss.level + ") ha aparecido!\n" +
                "<gray>Ubicación: <white>" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            MessageUtils.sendMessage(player, message);
            player.sendTitle(MessageUtils.parse(("<red><bold>BOSS APAREADO")), MessageUtils.parse(("<yellow>" + boss.name)), 10, 70, 20);
        }
    }

    // Método auxiliar para obtener atributos de forma segura según la versión
    private AttributeInstance getAttributeSafely(LivingEntity entity, String modernName, String legacyName) {
        try {
            // Primero intentar con el nombre moderno
            return entity.getAttribute(Attribute.valueOf(modernName));
        } catch (IllegalArgumentException e1) {
            try {
                // Si falla, intentar con el nombre legacy
                return entity.getAttribute(Attribute.valueOf(legacyName));
            } catch (IllegalArgumentException e2) {
                // Si ambos fallan, devolver null
                return null;
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        BossInstance instance = activeBosses.get(entity.getUniqueId());
        
        if (instance != null) {
            Boss boss = bosses.get(instance.bossId);
            if (boss != null) {
                handleBossDeath(entity, boss, event);
                cleanupBoss(entity.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity target) {
            BossInstance instance = activeBosses.get(target.getUniqueId());
            if (instance != null) {
                Boss boss = bosses.get(instance.bossId);
                if (boss != null && event.getDamager() instanceof Player player) {
                    // Track damage dealt by player
                    instance.addDamage(player.getUniqueId(), event.getFinalDamage());
                    
                    // Special boss damage effects
                    if (boss.abilities.contains(BossAbility.LIFE_STEAL)) {
                        // Boss heals when taking damage
                        double healAmount = event.getFinalDamage() * 0.1;
                        double newHealth = Math.min(target.getHealth() + healAmount, target.getMaxHealth());
                        target.setHealth(newHealth);
                    }
                }
            }
        }
    }

    private void handleBossDeath(LivingEntity entity, Boss boss, EntityDeathEvent event) {
        event.getDrops().clear(); // Clear default drops
        event.setDroppedExp(0);   // Clear default XP
        
        BossInstance instance = activeBosses.get(entity.getUniqueId());
        if (instance == null) return;
        
        // Get players who contributed to the kill
        List<Player> contributors = getContributors(instance);
        
        if (contributors.isEmpty()) return;
        
        // Announce boss death
        announceBossDeath(boss, contributors.get(0)); // Main killer
        
        // Distribute rewards
        distributeRewards(boss, contributors, entity.getLocation());
        
        // Record boss kill statistics
        recordBossKill(boss, contributors);
    }

    private List<Player> getContributors(BossInstance instance) {
        List<Player> contributors = new ArrayList<>();
        
        // Sort by damage dealt
        List<Map.Entry<UUID, Double>> sortedDamage = new ArrayList<>(instance.damageDealt.entrySet());
        sortedDamage.sort(Map.Entry.<UUID, Double>comparingByValue().reversed());
        
        for (Map.Entry<UUID, Double> entry : sortedDamage) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                contributors.add(player);
            }
        }
        
        return contributors;
    }

    private void distributeRewards(Boss boss, List<Player> contributors, Location deathLocation) {
        if (contributors.isEmpty()) return;
        
        Player mainKiller = contributors.get(0);
        
        // Drop items at death location
        for (ItemStack drop : boss.drops) {
            deathLocation.getWorld().dropItemNaturally(deathLocation, drop.clone());
        }
        
        // Distribute money and XP to all contributors
        double moneyPerPlayer = boss.moneyReward / contributors.size();
        int expPerPlayer = boss.experience / contributors.size();
        
        for (Player contributor : contributors) {
            // Give money
            plugin.getEconomyManager().depositBalance(contributor, moneyPerPlayer);
            
            // Give experience
            plugin.getSkillManager().addExperience(contributor.getUniqueId(), SkillType.COMBAT, expPerPlayer);
            
            // Send reward message
            MessageUtils.sendMessage(contributor, 
                "<green><bold>✨ RECOMPENSA DE BOSS ✨\n" +
                "<gray>Has recibido:\n" +
                "<gold><bold>+ " + plugin.getEconomyManager().formatCurrency(moneyPerPlayer) + "\n" +
                "<aqua><bold>+ " + expPerPlayer + " XP de Combate");
        }
        
        // Special reward for main killer
        MessageUtils.sendMessage(mainKiller, 
            "<yellow><bold>✨ ¡BOSS ELIMINADO! ✨\n" +
            "<gray>Has derrotado a <red>" + boss.name + "<gray>!\n" +
            "<gray>¡Felicitaciones por tu victoria!");
    }

    private void announceBossDeath(Boss boss, Player killer) {
        String message = "<red><bold>☠ BOSS DERROTADO ☠\n" +
                "<yellow>" + boss.name + "<gray> ha sido derrotado por <green>" + killer.getName() + "<gray>!";
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            MessageUtils.sendMessage(player, message);
            player.sendTitle(MessageUtils.parse(("<red><bold>BOSS DERROTADO")), MessageUtils.parse(("<gray>Por <green>" + killer.getName())), 10, 70, 20);
        }
    }

    private void recordBossKill(Boss boss, List<Player> contributors) {
        // TODO: Save boss kill statistics to database/config
        // For now, just log to console
        plugin.getLogger().info("Boss " + boss.name + " was killed by " + contributors.size() + " players");
    }

    private void cleanupBoss(UUID entityId) {
        activeBosses.remove(entityId);
        BukkitTask task = bossTasks.remove(entityId);
        if (task != null) {
            task.cancel();
        }
    }

    public void despawnBoss(UUID entityId) {
        LivingEntity entity = (LivingEntity) plugin.getServer().getEntity(entityId);
        if (entity != null) {
            entity.remove();
        }
        cleanupBoss(entityId);
    }

    public void despawnAllBosses() {
        for (UUID entityId : new ArrayList<>(activeBosses.keySet())) {
            despawnBoss(entityId);
        }
    }

    // Public methods for external access
    public Map<String, Boss> getBosses() {
        return new HashMap<>(bosses);
    }

    public Map<UUID, BossInstance> getActiveBosses() {
        return new HashMap<>(activeBosses);
    }

    public boolean isBossActive(String bossId) {
        return activeBosses.values().stream()
            .anyMatch(instance -> instance.bossId.equals(bossId));
    }

    public long getTimeUntilRespawn(String bossId) {
        Boss boss = bosses.get(bossId);
        if (boss == null) return -1;
        
        long lastSpawn = lastSpawnTime.getOrDefault(bossId, 0L);
        long currentTime = System.currentTimeMillis();
        long timeSinceSpawn = currentTime - lastSpawn;
        long respawnTime = boss.respawnTime * 1000L;
        
        return Math.max(0, respawnTime - timeSinceSpawn);
    }

    // Boss class definition
    public static class Boss {
        public final String name;
        public final String id;
        public final EntityType entityType;
        public final int level;
        public final double health;
        public final double damage;
        public final double speed;
        public final List<ItemStack> drops;
        public final double moneyReward;
        public final int experience;
        public final long respawnTime; // in seconds
        public final List<String> displayNames;
        public final List<BossAbility> abilities;

        public Boss(String name, String id, EntityType entityType, int level, double health, double damage, 
                   double speed, List<ItemStack> drops, double moneyReward, int experience, long respawnTime,
                   List<String> displayNames, List<BossAbility> abilities) {
            this.name = name;
            this.id = id;
            this.entityType = entityType;
            this.level = level;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.drops = new ArrayList<>(drops);
            this.moneyReward = moneyReward;
            this.experience = experience;
            this.respawnTime = respawnTime;
            this.displayNames = new ArrayList<>(displayNames);
            this.abilities = new ArrayList<>(abilities);
        }
    }

    // Boss instance class for tracking active bosses
    public static class BossInstance {
        public final String bossId;
        public final UUID entityId;
        public final Location spawnLocation;
        public final Map<UUID, Double> damageDealt = new HashMap<>();
        public final long spawnTime;
        public int noPlayerTime = 0;

        public BossInstance(String bossId, UUID entityId, Location spawnLocation) {
            this.bossId = bossId;
            this.entityId = entityId;
            this.spawnLocation = spawnLocation.clone();
            this.spawnTime = System.currentTimeMillis();
        }

        public void addDamage(UUID playerId, double damage) {
            damageDealt.merge(playerId, damage, Double::sum);
        }

        public double getTotalDamage() {
            return damageDealt.values().stream().mapToDouble(Double::doubleValue).sum();
        }
    }

    // Boss abilities enum
    public enum BossAbility {
        LIFE_STEAL,      // Heals when dealing/taking damage
        SUMMON_MINIONS,  // Spawns helper mobs
        POISON_AURA,     // Applies poison to nearby players
        MULTI_SHOT,      // Fires multiple projectiles
        TELEPORT_STRIKE, // Teleports to player and attacks
        BONE_WALL,       // Creates temporary barriers
        FIRE_BREATH,     // Creates fire blocks
        METEOR_RAIN,     // Drops fire charges from sky
        FLAME_BARRIER,   // Creates protective fire around boss
        LIGHTNING_BOLT,  // Strikes players with lightning
        FREEZE_PLAYERS,  // Applies slowness effect
        HEAL_BURST,      // Instant large heal
        DAMAGE_BOOST,    // Temporary damage increase
        SPEED_BOOST,     // Temporary speed increase
        INVISIBILITY     // Becomes temporarily invisible
    }
}