package com.mk.mksurvival.managers.enemies;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.rewards.RewardManager;
import com.mk.mksurvival.managers.particles.ParticleManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sistema mejorado de niveles de enemigos con características avanzadas
 */
public class EnemyLevelManager implements Listener {
    private final MKSurvival plugin;
    private final Map<EntityType, EnemyConfiguration> entityConfigs = new HashMap<>();
    private final Map<Location, Integer> areaLevels = new HashMap<>();
    private final Random random = new Random();
    
    // Configuraciones avanzadas
    private final double WORLD_LEVEL_SCALING = 0.1;
    private final int MAX_ENEMY_LEVEL = 100;
    private final double ELITE_CHANCE = 0.15; // 15% chance de elite
    private final double BOSS_CHANCE = 0.02; // 2% chance de mini-boss

    public EnemyLevelManager(MKSurvival plugin) {
        this.plugin = plugin;
        setupEntityConfigurations();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupEntityConfigurations() {
        // Configuraciones base mejoradas con diferentes rarities y scaling
        entityConfigs.put(EntityType.ZOMBIE, new EnemyConfiguration(1, 20, 1.2, EnemyTier.COMMON));
        entityConfigs.put(EntityType.SKELETON, new EnemyConfiguration(1, 25, 1.3, EnemyTier.COMMON));
        entityConfigs.put(EntityType.SPIDER, new EnemyConfiguration(1, 18, 1.1, EnemyTier.COMMON));
        entityConfigs.put(EntityType.CREEPER, new EnemyConfiguration(3, 30, 1.5, EnemyTier.UNCOMMON));
        entityConfigs.put(EntityType.ENDERMAN, new EnemyConfiguration(5, 40, 1.8, EnemyTier.RARE));
        entityConfigs.put(EntityType.WITCH, new EnemyConfiguration(3, 35, 1.6, EnemyTier.UNCOMMON));
        entityConfigs.put(EntityType.CAVE_SPIDER, new EnemyConfiguration(2, 20, 1.3, EnemyTier.COMMON));
        entityConfigs.put(EntityType.SILVERFISH, new EnemyConfiguration(1, 15, 1.0, EnemyTier.COMMON));
        entityConfigs.put(EntityType.BLAZE, new EnemyConfiguration(10, 50, 2.0, EnemyTier.RARE));
        entityConfigs.put(EntityType.WITHER_SKELETON, new EnemyConfiguration(15, 60, 2.5, EnemyTier.EPIC));
        entityConfigs.put(EntityType.ZOMBIFIED_PIGLIN, new EnemyConfiguration(8, 35, 1.4, EnemyTier.UNCOMMON));
        entityConfigs.put(EntityType.HUSK, new EnemyConfiguration(5, 25, 1.3, EnemyTier.COMMON));
        entityConfigs.put(EntityType.STRAY, new EnemyConfiguration(8, 30, 1.4, EnemyTier.UNCOMMON));
        entityConfigs.put(EntityType.DROWNED, new EnemyConfiguration(10, 35, 1.5, EnemyTier.UNCOMMON));
        entityConfigs.put(EntityType.PHANTOM, new EnemyConfiguration(15, 45, 1.7, EnemyTier.RARE));
        entityConfigs.put(EntityType.RAVAGER, new EnemyConfiguration(20, 70, 3.0, EnemyTier.EPIC));
        entityConfigs.put(EntityType.VEX, new EnemyConfiguration(12, 40, 1.6, EnemyTier.RARE));
        entityConfigs.put(EntityType.EVOKER, new EnemyConfiguration(18, 55, 2.2, EnemyTier.EPIC));
        entityConfigs.put(EntityType.VINDICATOR, new EnemyConfiguration(15, 45, 1.8, EnemyTier.RARE));
        entityConfigs.put(EntityType.PILLAGER, new EnemyConfiguration(10, 35, 1.5, EnemyTier.UNCOMMON));
        entityConfigs.put(EntityType.GUARDIAN, new EnemyConfiguration(12, 40, 1.7, EnemyTier.RARE));
        entityConfigs.put(EntityType.ELDER_GUARDIAN, new EnemyConfiguration(30, 80, 4.0, EnemyTier.LEGENDARY));
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity) || !entityConfigs.containsKey(event.getEntityType())) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getEntity();
        EnemyConfiguration config = entityConfigs.get(event.getEntityType());
        
        // Calcular nivel basado en configuración y localización
        int level = calculateEnemyLevel(entity.getLocation(), config);
        
        // Determinar tipo especial de enemigo
        EnemyType enemyType = determineEnemyType(config.tier);
        
        // Aplicar características del nivel y tipo
        applyEnemyEnhancements(entity, level, enemyType, config);
        
        // Establecer metadata
        entity.setMetadata("enemy_level", new FixedMetadataValue(plugin, level));
        entity.setMetadata("enemy_type", new FixedMetadataValue(plugin, enemyType.name()));
        entity.setMetadata("enemy_tier", new FixedMetadataValue(plugin, config.tier.name()));
    }

    private int calculateEnemyLevel(Location location, EnemyConfiguration config) {
        // Nivel base del área
        int areaLevel = getAreaLevel(location);
        
        // Distancia del spawn (más lejos = más difícil)
        double distanceFromSpawn = location.distance(location.getWorld().getSpawnLocation());
        int distanceBonus = (int) (distanceFromSpawn * WORLD_LEVEL_SCALING);
        
        // Profundidad (más profundo = más difícil)
        int depthBonus = location.getY() < 0 ? Math.abs(location.getBlockY()) / 10 : 0;
        
        // Dimensión bonus
        int dimensionBonus = getDimensionBonus(location.getWorld().getEnvironment());
        
        // Calcular nivel final
        int baseLevel = config.minLevel + ThreadLocalRandom.current().nextInt(config.maxLevel - config.minLevel + 1);
        int finalLevel = baseLevel + areaLevel + distanceBonus + depthBonus + dimensionBonus;
        
        return Math.min(MAX_ENEMY_LEVEL, Math.max(1, finalLevel));
    }

    private int getAreaLevel(Location location) {
        // Buscar nivel del área más cercana
        for (Map.Entry<Location, Integer> entry : areaLevels.entrySet()) {
            if (entry.getKey().getWorld() == location.getWorld() && 
                entry.getKey().distance(location) <= 500) { // Radio de 500 bloques
                return entry.getValue();
            }
        }
        return 0;
    }

    private int getDimensionBonus(org.bukkit.World.Environment environment) {
        return switch (environment) {
            case NETHER -> 10;
            case THE_END -> 20;
            default -> 0;
        };
    }

    private EnemyType determineEnemyType(EnemyTier tier) {
        double roll = random.nextDouble();
        
        // Probabilidades ajustadas por tier
        double eliteChance = ELITE_CHANCE * tier.eliteMultiplier;
        double bossChance = BOSS_CHANCE * tier.bossMultiplier;
        
        if (roll < bossChance) {
            return EnemyType.MINI_BOSS;
        } else if (roll < eliteChance) {
            return EnemyType.ELITE;
        } else {
            return EnemyType.NORMAL;
        }
    }

    private void applyEnemyEnhancements(LivingEntity entity, int level, EnemyType type, EnemyConfiguration config) {
        // Aplicar modificadores base del nivel
        applyLevelScaling(entity, level, config.scalingFactor);
        
        // Aplicar mejoras específicas del tipo
        switch (type) {
            case ELITE -> applyEliteEnhancements(entity, level);
            case MINI_BOSS -> applyMiniBossEnhancements(entity, level);
        }
        
        // Establecer nombre personalizado
        String displayName = formatEnemyName(entity.getType(), level, type);
        entity.setCustomName(displayName);
        entity.setCustomNameVisible(true);
        
        // Aplicar efectos visuales
        spawnVisualEffects(entity, type);
    }

    private void applyLevelScaling(LivingEntity entity, int level, double scalingFactor) {
        // Escalado mejorado de estadísticas
        double healthMultiplier = 1.0 + (level * 0.25 * scalingFactor);
        double damageMultiplier = 1.0 + (level * 0.15 * scalingFactor);
        double speedMultiplier = 1.0 + (level * 0.03 * scalingFactor);
        double armorMultiplier = Math.min(2.0, 1.0 + (level * 0.1 * scalingFactor));

        // Aplicar salud
        double newHealth = entity.getMaxHealth() * healthMultiplier;
        entity.setMaxHealth(Math.min(2048.0, newHealth)); // Límite de Minecraft
        entity.setHealth(entity.getMaxHealth());

        // Aplicar otros atributos
        setAttributeSafely(entity, Attribute.ATTACK_DAMAGE, 
                          getAttributeValue(entity, Attribute.ATTACK_DAMAGE, 1.0) * damageMultiplier);
        
        setAttributeSafely(entity, Attribute.MOVEMENT_SPEED,
                          getAttributeValue(entity, Attribute.MOVEMENT_SPEED, 0.23) * speedMultiplier);
        
        setAttributeSafely(entity, Attribute.ARMOR,
                          Math.min(20.0, level * 0.3 * armorMultiplier));
        
        if (level >= 20) {
            setAttributeSafely(entity, Attribute.ARMOR_TOUGHNESS,
                              Math.min(10.0, level * 0.15));
        }

        // Resistencias especiales para niveles altos
        if (level >= 30) {
            setAttributeSafely(entity, Attribute.KNOCKBACK_RESISTANCE, 0.5);
            entity.setRemoveWhenFarAway(false);
        }
    }

    private void applyEliteEnhancements(LivingEntity entity, int level) {
        // Bonuses adicionales para élites
        entity.setMaxHealth(entity.getMaxHealth() * 1.5);
        entity.setHealth(entity.getMaxHealth());
        
        // Efectos de poción permanentes
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false));
        
        // Resistencias elementales aleatorias
        List<PotionEffectType> resistances = Arrays.asList(
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.POISON, PotionEffectType.WITHER
        );
        
        PotionEffectType resistance = resistances.get(random.nextInt(resistances.size()));
        entity.addPotionEffect(new PotionEffect(resistance, Integer.MAX_VALUE, 0, false, false));
    }

    private void applyMiniBossEnhancements(LivingEntity entity, int level) {
        // Bonuses masivos para mini-bosses
        entity.setMaxHealth(entity.getMaxHealth() * 3.0);
        entity.setHealth(entity.getMaxHealth());
        
        // Múltiples efectos
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 0, false, false));
        
        // Resistencia total al knockback
        setAttributeSafely(entity, Attribute.KNOCKBACK_RESISTANCE, 1.0);
        entity.setRemoveWhenFarAway(false);
        
        // Regeneración lenta
        entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (!entity.hasMetadata("enemy_level")) {
            return;
        }
        
        int level = entity.getMetadata("enemy_level").get(0).asInt();
        EnemyType type = EnemyType.valueOf(entity.getMetadata("enemy_type").get(0).asString());
        EnemyTier tier = EnemyTier.valueOf(entity.getMetadata("enemy_tier").get(0).asString());
        
        Player killer = entity.getKiller();
        if (killer == null) return;
        
        // Calcular recompensas mejoradas
        EnemyRewards rewards = calculateRewards(level, type, tier);
        
        // Aplicar recompensas
        applyRewards(killer, rewards, entity, level, type);
        
        // Efectos especiales al morir
        spawnDeathEffects(entity.getLocation(), type, level);
        
        // Actualizar barra de experiencia
        plugin.getExpBarManager().updatePlayerExpBar(killer);
    }

    private EnemyRewards calculateRewards(int level, EnemyType type, EnemyTier tier) {
        double baseExp = level * 2.0;
        double baseMoney = level * 3.0;
        double baseSkillExp = level * 5.0;
        
        // Multiplicadores por tipo
        double typeMultiplier = switch (type) {
            case ELITE -> 2.5;
            case MINI_BOSS -> 5.0;
            default -> 1.0;
        };
        
        // Multiplicadores por tier
        double tierMultiplier = switch (tier) {
            case UNCOMMON -> 1.2;
            case RARE -> 1.5;
            case EPIC -> 2.0;
            case LEGENDARY -> 3.0;
            default -> 1.0;
        };
        
        double finalMultiplier = typeMultiplier * tierMultiplier;
        
        return new EnemyRewards(
            (int) (baseExp * finalMultiplier),
            baseMoney * finalMultiplier,
            baseSkillExp * finalMultiplier,
            calculateDropChance(level, type, tier)
        );
    }

    private double calculateDropChance(int level, EnemyType type, EnemyTier tier) {
        double baseChance = 0.1 + (level * 0.01); // 10% base + 1% por nivel
        
        double typeBonus = switch (type) {
            case ELITE -> 0.3;
            case MINI_BOSS -> 0.6;
            default -> 0.0;
        };
        
        double tierBonus = switch (tier) {
            case UNCOMMON -> 0.1;
            case RARE -> 0.2;
            case EPIC -> 0.4;
            case LEGENDARY -> 0.8;
            default -> 0.0;
        };
        
        return Math.min(0.95, baseChance + typeBonus + tierBonus);
    }

    private void applyRewards(Player player, EnemyRewards rewards, LivingEntity entity, int level, EnemyType type) {
        // Aplicar experiencia vanilla
        entity.getWorld().spawn(entity.getLocation(), ExperienceOrb.class)
               .setExperience(rewards.vanillaExp);
        
        // Aplicar experiencia de habilidades
        SkillManager skillManager = plugin.getSkillManager();
        skillManager.addExperience(player.getUniqueId(), SkillType.COMBAT, (int) rewards.skillExp);
        
        // Aplicar recompensas de dinero
        RewardManager rewardManager = plugin.getRewardManager();
        rewardManager.giveEnemyReward(player, entity.getType().name(), level, type.name());
    }

    public int getEnemyLevel(LivingEntity entity) {
        return entity.hasMetadata("enemy_level") ? 
            entity.getMetadata("enemy_level").get(0).asInt() : 1;
    }

    // Método para obtener el nivel base de un tipo de entidad
    public int getEntityLevel(EntityType entityType) {
        EnemyConfiguration config = entityConfigs.get(entityType);
        if (config != null) {
            return config.minLevel;
        }
        return 1; // Nivel por defecto
    }

    // Método para establecer el nivel de un área
    public void setAreaLevel(Location center, int level, int radius) {
        if (level < 1 || level > 50) {
            throw new IllegalArgumentException("Area level must be between 1 and 50");
        }
        
        areaLevels.put(center.clone(), level);
        
        // Opcional: aplicar el nivel inmediatamente a enemigos existentes en el área
        for (LivingEntity entity : center.getWorld().getLivingEntities()) {
            if (entity.getLocation().distance(center) <= radius && 
                entity.hasMetadata("enemy_level")) {
                
                EnemyConfiguration config = entityConfigs.getOrDefault(entity.getType(), 
                    new EnemyConfiguration(1, MAX_ENEMY_LEVEL, 1.0, EnemyTier.COMMON));
                
                // Recalcular nivel con el nuevo nivel de área
                int newLevel = Math.min(MAX_ENEMY_LEVEL, 
                    Math.max(config.minLevel, level + ThreadLocalRandom.current().nextInt(5)));
                
                entity.removeMetadata("enemy_level", plugin);
                entity.setMetadata("enemy_level", new FixedMetadataValue(plugin, newLevel));
                
                // Reapllicar scaling con el nuevo nivel
                applyLevelScaling(entity, newLevel, config.scalingFactor);
                
                // Actualizar nombre
                EnemyType type = entity.hasMetadata("enemy_type") ? 
                    EnemyType.valueOf(entity.getMetadata("enemy_type").get(0).asString()) : EnemyType.NORMAL;
                String displayName = formatEnemyName(entity.getType(), newLevel, type);
                entity.setCustomName(displayName);
            }
        }
    }

    // Clases internas para organización
    public static class EnemyConfiguration {
        public final int minLevel;
        public final int maxLevel;
        public final double scalingFactor;
        public final EnemyTier tier;

        public EnemyConfiguration(int minLevel, int maxLevel, double scalingFactor, EnemyTier tier) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.scalingFactor = scalingFactor;
            this.tier = tier;
        }
    }

    public enum EnemyType {
        NORMAL, ELITE, BOSS, MINI_BOSS
    }

    public enum EnemyTier {
        COMMON(1.0, 1.0),
        UNCOMMON(1.2, 1.5),
        RARE(1.5, 2.0),
        EPIC(2.0, 3.0),
        LEGENDARY(3.0, 5.0);

        public final double eliteMultiplier;
        public final double bossMultiplier;

        EnemyTier(double eliteMultiplier, double bossMultiplier) {
            this.eliteMultiplier = eliteMultiplier;
            this.bossMultiplier = bossMultiplier;
        }
    }

    public static class EnemyRewards {
        public final int vanillaExp;
        public final double money;
        public final double skillExp;
        public final double dropChance;

        public EnemyRewards(int vanillaExp, double money, double skillExp, double dropChance) {
            this.vanillaExp = vanillaExp;
            this.money = money;
            this.skillExp = skillExp;
            this.dropChance = dropChance;
        }
    }

    public static class EnemyInfo {
        public final EntityType entityType;
        public final int level;
        public final EnemyType type;
        public final EnemyTier tier;
        public final double health;
        public final double maxHealth;
        public final double damage;
        public final double speed;
        public final String customName;
        public final boolean isManuallyCreated;

        public EnemyInfo(EntityType entityType, int level, EnemyType type, EnemyTier tier,
                        double health, double maxHealth, double damage, double speed,
                        String customName, boolean isManuallyCreated) {
            this.entityType = entityType;
            this.level = level;
            this.type = type;
            this.tier = tier;
            this.health = health;
            this.maxHealth = maxHealth;
            this.damage = damage;
            this.speed = speed;
            this.customName = customName;
            this.isManuallyCreated = isManuallyCreated;
        }
    }

    private void setAttributeSafely(LivingEntity entity, Attribute attribute, double value) {
        try {
            AttributeInstance attributeInstance = entity.getAttribute(attribute);
            if (attributeInstance != null) {
                attributeInstance.setBaseValue(value);
            }
        } catch (Exception e) {
            // Ignore if attribute is not supported for this entity type
        }
    }

    private double getAttributeValue(LivingEntity entity, Attribute attribute, double defaultValue) {
        try {
            AttributeInstance attributeInstance = entity.getAttribute(attribute);
            return attributeInstance != null ? attributeInstance.getBaseValue() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void spawnDeathEffects(Location location, EnemyType type, int level) {
        ParticleManager particleManager = plugin.getParticleManager();
        switch (type) {
            case ELITE:
                particleManager.spawnEliteDeathEffect(location, level);
                break;
            case BOSS:
                particleManager.spawnBossDeathEffect(location, level);
                break;
            default:
                particleManager.spawnDeathEffect(location, level);
                break;
        }
    }

    private String formatEnemyName(EntityType entityType, int level, EnemyType type) {
        String baseName = entityType.name().toLowerCase().replace("_", " ");
        String prefix = switch (type) {
            case ELITE -> MessageUtils.parse(("<gold>[Elite] </gold>"));
            case BOSS -> MessageUtils.parse(("<dark_red>[Boss] </dark_red>"));
            default -> "";
        };
        return prefix + MessageUtils.parse(("<white>" + baseName + " <gray>(Nivel " + level + ")</gray></white>"));
    }

    public LivingEntity spawnCustomEnemy(Location location, EntityType entityType, int level, EnemyType type, String customName) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        
        // Set metadata
        entity.setMetadata("enemy_level", new FixedMetadataValue(plugin, level));
        entity.setMetadata("enemy_type", new FixedMetadataValue(plugin, type.name()));
        
        // Apply level scaling
        applyLevelScaling(entity, level, 1.0);
        if (type == EnemyType.BOSS) {
            applyBossEnhancements(entity, level);
        }
        
        if (customName != null) {
            entity.setCustomName(customName);
        } else {
            entity.setCustomName(formatEnemyName(entityType, level, type));
        }
        entity.setCustomNameVisible(true);
        
        return entity;
    }

    public boolean modifyExistingEnemy(LivingEntity entity, Integer level, EnemyType type, String customName, Double healthMultiplier, Double damageMultiplier) {
        if (level != null) {
            entity.setMetadata("enemy_level", new FixedMetadataValue(plugin, level));
            applyLevelScaling(entity, level, 1.0);
        }
        
        if (type != null) {
            entity.setMetadata("enemy_type", new FixedMetadataValue(plugin, type.name()));
            switch (type) {
                case ELITE:
                    applyEliteEnhancements(entity, getEnemyLevel(entity));
                    break;
                case BOSS:
                    applyBossEnhancements(entity, getEnemyLevel(entity));
                    break;
            }
        }
        
        if (customName != null) {
            entity.setCustomName(customName);
            entity.setCustomNameVisible(true);
        }
        
        if (healthMultiplier != null) {
            entity.setMaxHealth(entity.getMaxHealth() * healthMultiplier);
            entity.setHealth(entity.getMaxHealth());
        }
        
        if (damageMultiplier != null) {
            setAttributeSafely(entity, Attribute.ATTACK_DAMAGE, 
                getAttributeValue(entity, Attribute.ATTACK_DAMAGE, 1.0) * damageMultiplier);
        }
        return false;
    }

    public LivingEntity cloneEnemy(LivingEntity original, Location newLocation) {
        EntityType entityType = original.getType();
        int level = getEnemyLevel(original);
        EnemyType type = EnemyType.valueOf(original.getMetadata("enemy_type").get(0).asString());
        String customName = original.getCustomName();
        
        return spawnCustomEnemy(newLocation, entityType, level, type, customName);
    }

    public EnemyInfo getEnemyInfo(LivingEntity entity) {
        EntityType entityType = entity.getType();
        int level = getEnemyLevel(entity);
        EnemyType type = entity.hasMetadata("enemy_type") ? 
            EnemyType.valueOf(entity.getMetadata("enemy_type").get(0).asString()) : EnemyType.NORMAL;
        EnemyTier tier = EnemyTier.COMMON; // Default tier
        
        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();
        double damage = getAttributeValue(entity, Attribute.ATTACK_DAMAGE, 1.0);
        double speed = getAttributeValue(entity, Attribute.MOVEMENT_SPEED, 0.23);
        String customName = entity.getCustomName();
        boolean isManuallyCreated = entity.hasMetadata("manually_created");
        
        return new EnemyInfo(entityType, level, type, tier, health, maxHealth, damage, speed, customName, isManuallyCreated);
    }

    private void spawnVisualEffects(LivingEntity entity, EnemyType type) {
        ParticleManager particleManager = plugin.getParticleManager();
        switch (type) {
            case ELITE:
                particleManager.spawnEliteEffect(entity.getLocation(), getEnemyLevel(entity));
                break;
            case BOSS:
                particleManager.spawnBossEffect(entity.getLocation(), getEnemyLevel(entity));
                break;
            default:
                break;
        }
    }

    private void applyBossEnhancements(LivingEntity entity, int level) {
        // Apply boss-specific enhancements
        double healthMultiplier = 3.0 + (level * 0.5);
        double damageMultiplier = 2.0 + (level * 0.3);
        
        // Set enhanced health
        setAttributeSafely(entity, Attribute.MAX_HEALTH,
            getAttributeValue(entity, Attribute.MAX_HEALTH) * healthMultiplier);
        entity.setHealth(entity.getAttribute(Attribute.MAX_HEALTH).getValue());
        
        // Set enhanced damage
        setAttributeSafely(entity, Attribute.ATTACK_DAMAGE,
            getAttributeValue(entity, Attribute.ATTACK_DAMAGE) * damageMultiplier);
        
        // Add boss effects
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
    }

    private double getAttributeValue(LivingEntity entity, Attribute maxHealth) {
        return 0;
    }
}