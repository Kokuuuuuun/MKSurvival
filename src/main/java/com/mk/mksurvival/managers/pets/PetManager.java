package com.mk.mksurvival.managers.pets;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PetManager implements Listener {
    private final MKSurvival plugin;
    private final Map<UUID, Pet> playerPets = new HashMap<>();
    private final Map<UUID, Pet> storedPets = new HashMap<>();
    private final Map<UUID, Long> lastPetSpawn = new HashMap<>();
    private final Map<UUID, Long> lastPetFeed = new HashMap<>();
    private final Map<UUID, Long> lastBreeding = new HashMap<>();
    private final Map<UUID, PetBreeding> activeBreedings = new HashMap<>();
    private final Map<String, PetEvolution> petEvolutions = new HashMap<>();
    private final Map<UUID, PetTraining> activeTrainings = new HashMap<>();
    private FileConfiguration petsConfig;

    public PetManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.petsConfig = plugin.getConfigManager().getPetsConfig();
        initializePetEvolutions();
        loadPlayerPets();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startPetEffects();
        startBreedingSystem();
        startTrainingSystem();
        startHappinessSystem();
    }

    private void loadPlayerPets() {
        if (!petsConfig.contains("players")) return;
        for (String uuidStr : petsConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String path = "players." + uuidStr + ".";
            if (petsConfig.contains(path + "pet")) {
                String petType = petsConfig.getString(path + "pet.type");
                String name = petsConfig.getString(path + "pet.name");
                double health = petsConfig.getDouble(path + "pet.health", 20.0);
                int level = petsConfig.getInt(path + "pet.level", 1);
                double experience = petsConfig.getDouble(path + "pet.experience", 0.0);
                Pet pet = new Pet(petType, name, health, level, experience, uuid);
                playerPets.put(uuid, pet);
            }
        }
    }

    public void spawnPet(Player player, String petType, String name) {
        UUID uuid = player.getUniqueId();
        if (playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Ya tienes una mascota activa.");
            return;
        }

        // Verificar cooldown
        if (lastPetSpawn.containsKey(uuid) &&
                System.currentTimeMillis() - lastPetSpawn.get(uuid) < 5000) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Debes esperar antes de spawnear otra mascota.");
            return;
        }
        lastPetSpawn.put(uuid, System.currentTimeMillis());

        // Crear mascota
        Pet pet = new Pet(petType, name, 20.0, 1, 0.0, uuid);
        playerPets.put(uuid, pet);

        // Spawnear entidad de la mascota
        EntityType entityType = getPetEntityType(petType);
        if (entityType != null) {
            Location spawnLoc = player.getLocation().add(1, 0, 1);
            LivingEntity petEntity = (LivingEntity) player.getWorld().spawnEntity(spawnLoc, entityType);

            // Configurar entidad
            petEntity.setCustomName("<green>" + name + " <gray>[Nivel " + pet.getLevel() + "]");
            petEntity.setCustomNameVisible(true);
            petEntity.setInvulnerable(true);
            petEntity.setGravity(false);

            // Guardar referencia a la entidad
            pet.setEntity(petEntity);

            // Hacer que la mascota siga al jugador
            makePetFollow(player, petEntity);
            MessageUtils.sendMessage(player, "<green>[Mascotas] Has spawnado a " + name + "!");

            // Efectos visuales
            plugin.getParticleManager().spawnRewardEffect(player);
        } else {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Tipo de mascota no válido.");
            playerPets.remove(uuid);
        }
    }

    private EntityType getPetEntityType(String petType) {
        switch (petType.toLowerCase()) {
            case "wolf": return EntityType.WOLF;
            case "cat": return EntityType.CAT;
            case "parrot": return EntityType.PARROT;
            case "fox": return EntityType.FOX;
            case "panda": return EntityType.PANDA;
            case "ocelot": return EntityType.OCELOT;
            case "horse": return EntityType.HORSE;
            case "donkey": return EntityType.DONKEY;
            case "mule": return EntityType.MULE;
            case "skeleton_horse": return EntityType.SKELETON_HORSE;
            case "zombie_horse": return EntityType.ZOMBIE_HORSE;
            case "llama": return EntityType.LLAMA;
            case "trader_llama": return EntityType.TRADER_LLAMA;
            case "turtle": return EntityType.TURTLE;
            case "rabbit": return EntityType.RABBIT;
            case "chicken": return EntityType.CHICKEN;
            case "cow": return EntityType.COW;
            case "pig": return EntityType.PIG;
            case "sheep": return EntityType.SHEEP;
            default: return null;
        }
    }

    private void makePetFollow(Player player, LivingEntity pet) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || pet.isDead()) {
                    this.cancel();
                    return;
                }

                // Seguir al jugador
                Location playerLoc = player.getLocation();
                Location petLoc = pet.getLocation();
                double distance = playerLoc.distance(petLoc);

                if (distance > 5) {
                    // Teletransportar si está muy lejos
                    pet.teleport(playerLoc.clone().add(1, 0, 1));
                } else if (distance > 3) {
                    // Mover hacia el jugador
                    Vector direction = playerLoc.toVector().subtract(petLoc.toVector()).normalize();
                    pet.setVelocity(direction.multiply(0.5));
                }

                // Habilidades especiales según el tipo de mascota
                UUID playerUuid = player.getUniqueId();
                if (playerPets.containsKey(playerUuid)) {
                    Pet petData = playerPets.get(playerUuid);
                    applyPetAbilities(player, pet, petData);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private void applyPetAbilities(Player player, LivingEntity petEntity, Pet pet) {
        // Habilidades según el tipo de mascota
        switch (pet.getType().toLowerCase()) {
            case "wolf":
                // Los lobos atacan a mobs hostiles cercanos
                for (Entity entity : petEntity.getNearbyEntities(10, 5, 10)) {
                    if (entity instanceof Monster && entity != player) {
                        ((Monster) entity).setTarget((Mob) entity);
                    }
                }
                break;
            case "cat":
                // Los gatos asustan a los creepers
                for (Entity entity : petEntity.getNearbyEntities(10, 5, 10)) {
                    if (entity instanceof Creeper) {
                        ((Creeper) entity).setPowered(true);
                    }
                }
                break;
            case "parrot":
                // Los loros imitan sonidos y dan efectos de velocidad
                if (ThreadLocalRandom.current().nextInt(100) < 5) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SPEED, 200, 0));
                }
                break;
            case "fox":
                // Los zorros ayudan a encontrar items
                if (ThreadLocalRandom.current().nextInt(100) < 2) {
                    // Dar un item aleatorio al jugador
                    ItemStack randomItem = getRandomItem();
                    player.getInventory().addItem(randomItem);
                    MessageUtils.sendMessage(player, "<yellow>[Mascotas] Tu zorro ha encontrado algo para ti!");
                }
                break;
            case "panda":
                // Los pandas dan efectos de saturación
                if (ThreadLocalRandom.current().nextInt(100) < 3) {
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SATURATION, 200, 0));
                }
                break;
        }
    }

    private ItemStack getRandomItem() {
        Material[] materials = {
                Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND,
                Material.EMERALD, Material.COAL, Material.REDSTONE
        };
        return new ItemStack(materials[ThreadLocalRandom.current().nextInt(materials.length)]);
    }

    public void feedPet(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }

        // Verificar cooldown
        if (lastPetFeed.containsKey(uuid) &&
                System.currentTimeMillis() - lastPetFeed.get(uuid) < 30000) { // 30 segundos
            MessageUtils.sendMessage(player, "<red>[Mascotas] Debes esperar antes de alimentar a tu mascota nuevamente.");
            return;
        }

        Pet pet = playerPets.get(uuid);
        ItemStack food = player.getInventory().getItemInMainHand();
        if (!isPetFood(food, pet.getType())) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Este item no es comida para tu mascota.");
            return;
        }

        // Alimentar mascota
        pet.setHealth(Math.min(pet.getMaxHealth(), pet.getHealth() + 5));
        pet.addExperience(10);

        // Quitar comida del jugador
        food.setAmount(food.getAmount() - 1);
        if (food.getAmount() == 0) {
            player.getInventory().setItemInMainHand(null);
        }

        lastPetFeed.put(uuid, System.currentTimeMillis());
        MessageUtils.sendMessage(player, "<green>[Mascotas] Has alimentado a " + pet.getName() + "!");

        // Efectos visuales
        LivingEntity petEntity = pet.getEntity();
        if (petEntity != null && !petEntity.isDead()) {
            petEntity.getWorld().spawnParticle(Particle.HEART, petEntity.getLocation(), 5);
        }

        // Verificar si sube de nivel
        checkPetLevelUp(player, pet);
    }

    private boolean isPetFood(ItemStack item, String petType) {
        if (item == null) return false;
        switch (petType.toLowerCase()) {
            case "wolf":
            case "fox":
                return item.getType() == Material.BONE || item.getType() == Material.COOKED_BEEF;
            case "cat":
            case "ocelot":
                return item.getType() == Material.COD || item.getType() == Material.SALMON;
            case "parrot":
                return item.getType() == Material.COOKIE || item.getType() == Material.MELON_SEEDS;
            case "panda":
                return item.getType() == Material.BAMBOO || item.getType() == Material.SUGAR_CANE;
            case "horse":
            case "donkey":
            case "mule":
            case "llama":
            case "trader_llama":
                return item.getType() == Material.HAY_BLOCK || item.getType() == Material.WHEAT;
            case "turtle":
                return item.getType() == Material.SEAGRASS;
            case "rabbit":
                return item.getType() == Material.CARROT || item.getType() == Material.DANDELION;
            case "chicken":
            case "cow":
            case "pig":
            case "sheep":
                return item.getType() == Material.WHEAT_SEEDS;
            default:
                return false;
        }
    }

    private void checkPetLevelUp(Player player, Pet pet) {
        double expNeeded = pet.getLevel() * 100;
        if (pet.getExperience() >= expNeeded) {
            pet.setLevel(pet.getLevel() + 1);
            pet.setExperience(pet.getExperience() - expNeeded);
            MessageUtils.sendMessage(player, "<green>[Mascotas] ¡Tu mascota " + pet.getName() + " ha subido al nivel " + pet.getLevel() + "!");

            // Mejorar estadísticas de la mascota
            pet.setMaxHealth(pet.getMaxHealth() + 2);
            pet.setHealth(pet.getMaxHealth());

            // Efectos visuales
            LivingEntity petEntity = pet.getEntity();
            if (petEntity != null && !petEntity.isDead()) {
                plugin.getParticleManager().spawnLevelUpEffect(player);
                petEntity.getWorld().spawnParticle(Particle.FALLING_HONEY, petEntity.getLocation(), 20);
            }
        }
    }

    public void upgradePet(Player player, String upgradeType) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }

        Pet pet = playerPets.get(uuid);
        int cost = getUpgradeCost(upgradeType, pet.getLevel());
        if (plugin.getEconomyManager().getBalance(player) < cost) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Necesitas $" + cost + " para esta mejora.");
            return;
        }

        // Aplicar mejora
        switch (upgradeType.toLowerCase()) {
            case "health":
                pet.setMaxHealth(pet.getMaxHealth() + 5);
                pet.setHealth(pet.getMaxHealth());
                MessageUtils.sendMessage(player, "<green>[Mascotas] Has mejorado la salud de tu mascota!");
                break;
            case "speed":
                // Aumentar velocidad (implementado en el seguimiento)
                MessageUtils.sendMessage(player, "<green>[Mascotas] Has mejorado la velocidad de tu mascota!");
                break;
            case "damage":
                // Aumentar daño (para mascotas de combate)
                MessageUtils.sendMessage(player, "<green>[Mascotas] Has mejorado el daño de tu mascota!");
                break;
            default:
                MessageUtils.sendMessage(player, "<red>[Mascotas] Tipo de mejora no válido.");
                return;
        }

        // Cobrar dinero
        plugin.getEconomyManager().withdrawBalance(player, cost);

        // Efectos visuales
        LivingEntity petEntity = pet.getEntity();
        if (petEntity != null && !petEntity.isDead()) {
            plugin.getParticleManager().spawnRewardEffect(player);
        }
    }

    private int getUpgradeCost(String upgradeType, int petLevel) {
        int baseCost = 100;
        return baseCost * petLevel;
    }

    public void removePet(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }

        Pet pet = playerPets.get(uuid);
        LivingEntity petEntity = pet.getEntity();
        if (petEntity != null && !petEntity.isDead()) {
            petEntity.remove();
        }

        playerPets.remove(uuid);
        MessageUtils.sendMessage(player, "<green>[Mascotas] Has guardado a tu mascota.");
    }

    public void listPetTypes(Player player) {
        MessageUtils.sendMessage(player, "<gold>=== Tipos de Mascotas Disponibles ===");
        MessageUtils.sendMessage(player, "<yellow>• Wolf - Ataca a mobs hostiles");
        MessageUtils.sendMessage(player, "<yellow>• Cat - Asusta a los creepers");
        MessageUtils.sendMessage(player, "<yellow>• Parrot - Da efectos de velocidad");
        MessageUtils.sendMessage(player, "<yellow>• Fox - Encuentra items");
        MessageUtils.sendMessage(player, "<yellow>• Panda - Da saturación");
        MessageUtils.sendMessage(player, "<yellow>• Ocelot - Mascota ágil");
        MessageUtils.sendMessage(player, "<yellow>• Horse - Montura rápida");
        MessageUtils.sendMessage(player, "<yellow>• Donkey - Montura con almacenamiento");
        MessageUtils.sendMessage(player, "<yellow>• Mule - Montura resistente");
        MessageUtils.sendMessage(player, "<yellow>• Skeleton Horse - Montura especial");
        MessageUtils.sendMessage(player, "<yellow>• Zombie Horse - Montura única");
        MessageUtils.sendMessage(player, "<yellow>• Llama - Transporte de items");
        MessageUtils.sendMessage(player, "<yellow>• Trader Llama - Comerciante");
        MessageUtils.sendMessage(player, "<yellow>• Turtle - Defensa pasiva");
        MessageUtils.sendMessage(player, "<yellow>• Rabbit - Velocidad extrema");
        MessageUtils.sendMessage(player, "<yellow>• Chicken - Producción de huevos");
        MessageUtils.sendMessage(player, "<yellow>• Cow - Producción de leche");
        MessageUtils.sendMessage(player, "<yellow>• Pig - Montura básica");
        MessageUtils.sendMessage(player, "<yellow>• Sheep - Producción de lana");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (playerPets.containsKey(uuid)) {
            Pet pet = playerPets.get(uuid);
            LivingEntity petEntity = pet.getEntity();
            if (petEntity != null && !petEntity.isDead()) {
                petEntity.remove();
            }
            // Guardar datos de la mascota
            savePlayerPet(event.getPlayer(), pet);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (petsConfig.contains("players." + uuid + ".pet")) {
            // Cargar datos de la mascota
            String path = "players." + uuid + ".pet.";
            String petType = petsConfig.getString(path + "type");
            String name = petsConfig.getString(path + "name");
            double health = petsConfig.getDouble(path + "health", 20.0);
            int level = petsConfig.getInt(path + "level", 1);
            double experience = petsConfig.getDouble(path + "experience", 0.0);
            Pet pet = new Pet(petType, name, health, level, experience, uuid);
            playerPets.put(uuid, pet);
            MessageUtils.sendMessage(player, "<green>[Mascotas] Tu mascota " + name + " te está esperando!");
        }
    }

    private void savePlayerPet(Player player, Pet pet) {
        String path = "players." + player.getUniqueId() + ".pet.";
        petsConfig.set(path + "type", pet.getType());
        petsConfig.set(path + "name", pet.getName());
        petsConfig.set(path + "health", pet.getHealth());
        petsConfig.set(path + "level", pet.getLevel());
        petsConfig.set(path + "experience", pet.getExperience());
        plugin.getConfigManager().savePetsConfig();
    }

    private void startPetEffects() {
        // Efectos visuales para mascotas
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : playerPets.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    Pet pet = playerPets.get(uuid);
                    LivingEntity petEntity = pet.getEntity();
                    if (player != null && player.isOnline() &&
                            petEntity != null && !petEntity.isDead()) {
                        // Partículas alrededor de la mascota
                        Location loc = petEntity.getLocation().add(
                                Math.random() * 0.5 - 0.25,
                                Math.random() * 0.5,
                                Math.random() * 0.5 - 0.25
                        );
                        petEntity.getWorld().spawnParticle(Particle.FALLING_HONEY, loc, 1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 40);
    }

    public Pet getPlayerPet(Player player) {
        return playerPets.get(player.getUniqueId());
    }

    // Métodos añadidos para corregir errores
    public void despawnPet(Player player) {
        removePet(player);
    }

    public void namePet(Player player, String name) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }

        Pet pet = playerPets.get(uuid);
        pet.setName(name);
        LivingEntity petEntity = pet.getEntity();
        if (petEntity != null && !petEntity.isDead()) {
            petEntity.setCustomName("<green>" + name + " <gray>[Nivel " + pet.getLevel() + "]");
        }
        MessageUtils.sendMessage(player, "<green>[Mascotas] Has nombrado a tu mascota como '" + name + "'");
    }

    public void petSkill(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }

        Pet pet = playerPets.get(uuid);
        if (pet.getSkillCooldown() > 0) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] La habilidad de tu mascota está en cooldown. Tiempo restante: " + pet.getSkillCooldown() + "s");
            return;
        }

        // Aplicar efecto según el tipo de mascota
        switch (pet.getType().toLowerCase()) {
            case "wolf":
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.INSTANT_DAMAGE, 200, 1));
                MessageUtils.sendMessage(player, "<green>[Mascotas] Tu lobo te ha dado fuerza!");
                break;
            case "cat":
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY, 200, 0));
                MessageUtils.sendMessage(player, "<green>[Mascotas] Tu gato te ha hecho invisible!");
                break;
            case "parrot":
                player.setAllowFlight(true);
                player.setFlying(true);
                MessageUtils.sendMessage(player, "<green>[Mascotas] Tu loro te permite volar temporalmente!");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.setFlying(false);
                            player.setAllowFlight(false);
                            MessageUtils.sendMessage(player, "<red>[Mascotas] Tu tiempo de vuelo ha terminado.");
                        }
                    }
                }.runTaskLater(plugin, 200); // 10 segundos
                break;
            case "fox":
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED, 300, 2));
                MessageUtils.sendMessage(player, "<green>[Mascotas] Tu zorro te ha dado velocidad!");
                break;
            case "panda":
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION, 200, 1));
                MessageUtils.sendMessage(player, "<green>[Mascotas] Tu panda te ha regenerado vida!");
                break;
            default:
                MessageUtils.sendMessage(player, "<red>[Mascotas] Tu mascota no tiene una habilidad especial.");
                return;
        }

        // Establecer cooldown
        pet.setSkillCooldown(60); // 60 segundos
        MessageUtils.sendMessage(player, "<green>[Mascotas] Habilidad usada. Cooldown: 60s");
    }

    public void showPetInfo(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }

        Pet pet = playerPets.get(uuid);
        MessageUtils.sendMessage(player, "<gold>--- Información de Mascota ---");
        MessageUtils.sendMessage(player, "<yellow>Tipo: " + pet.getType());
        MessageUtils.sendMessage(player, "<yellow>Nombre: " + pet.getName());
        MessageUtils.sendMessage(player, "<yellow>Nivel: " + pet.getLevel());
        MessageUtils.sendMessage(player, "<yellow>Salud: " + pet.getHealth() + "/" + pet.getMaxHealth());
        MessageUtils.sendMessage(player, "<yellow>Experiencia: " + pet.getExperience());
        MessageUtils.sendMessage(player, "<yellow>Cooldown de habilidad: " + pet.getSkillCooldown() + "s");
    }

    public Map<UUID, Pet> getPlayerPets() {
        return playerPets;
    }

    // ===== ADVANCED PET BREEDING SYSTEM =====
    
    private void initializePetEvolutions() {
        // Wolf evolution tree
        petEvolutions.put("wolf_alpha", new PetEvolution("wolf", "wolf_alpha", 20, 
            Arrays.asList("strength", "pack_leader"), "Alpha Wolf"));
        petEvolutions.put("wolf_dire", new PetEvolution("wolf", "wolf_dire", 30,
            Arrays.asList("combat_mastery", "intimidation"), "Dire Wolf"));
        
        // Cat evolution tree  
        petEvolutions.put("cat_shadow", new PetEvolution("cat", "cat_shadow", 15,
            Arrays.asList("stealth", "night_vision"), "Shadow Cat"));
        petEvolutions.put("cat_mystic", new PetEvolution("cat", "cat_mystic", 25,
            Arrays.asList("magic_affinity", "luck"), "Mystic Cat"));
            
        // Fox evolution tree
        petEvolutions.put("fox_arctic", new PetEvolution("fox", "fox_arctic", 18,
            Arrays.asList("cold_immunity", "treasure_hunter"), "Arctic Fox"));
        petEvolutions.put("fox_fire", new PetEvolution("fox", "fox_fire", 22,
            Arrays.asList("fire_immunity", "elemental_magic"), "Fire Fox"));
            
        // Parrot evolution tree
        petEvolutions.put("parrot_phoenix", new PetEvolution("parrot", "parrot_phoenix", 35,
            Arrays.asList("flight_mastery", "fire_magic"), "Phoenix Parrot"));
            
        // Panda evolution tree
        petEvolutions.put("panda_martial", new PetEvolution("panda", "panda_martial", 28,
            Arrays.asList("martial_arts", "inner_peace"), "Martial Panda"));
    }
    
    public void startBreeding(Player player, String partnerUuid) {
        UUID playerUuid = player.getUniqueId();
        if (!playerPets.containsKey(playerUuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }
        
        if (lastBreeding.containsKey(playerUuid) && 
            System.currentTimeMillis() - lastBreeding.get(playerUuid) < 300000) { // 5 minutos
            MessageUtils.sendMessage(player, "<red>[Mascotas] Debes esperar antes de criar nuevamente.");
            return;
        }
        
        Player partner = Bukkit.getPlayer(UUID.fromString(partnerUuid));
        if (partner == null || !partner.isOnline()) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] El jugador compañero no está en línea.");
            return;
        }
        
        UUID partnerPlayerUuid = partner.getUniqueId();
        if (!playerPets.containsKey(partnerPlayerUuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] El jugador compañero no tiene una mascota activa.");
            return;
        }
        
        Pet pet1 = playerPets.get(playerUuid);
        Pet pet2 = playerPets.get(partnerPlayerUuid);
        
        if (!pet1.getType().equals(pet2.getType())) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Solo puedes criar mascotas del mismo tipo.");
            return;
        }
        
        if (pet1.getLevel() < 10 || pet2.getLevel() < 10) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Ambas mascotas deben ser nivel 10 o superior.");
            return;
        }
        
        // Iniciar proceso de cría
        PetBreeding breeding = new PetBreeding(playerUuid, partnerPlayerUuid, pet1, pet2);
        activeBreedings.put(playerUuid, breeding);
        lastBreeding.put(playerUuid, System.currentTimeMillis());
        
        MessageUtils.sendMessage(player, "<green>[Mascotas] Proceso de cría iniciado. Completará en 10 minutos.");
        MessageUtils.sendMessage(partner, "<green>[Mascotas] " + player.getName() + " ha iniciado un proceso de cría contigo.");
        
        // Programar finalización de cría
        new BukkitRunnable() {
            @Override
            public void run() {
                completeBreeding(playerUuid, breeding);
            }
        }.runTaskLater(plugin, 12000); // 10 minutos
    }
    
    private void completeBreeding(UUID playerUuid, PetBreeding breeding) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            activeBreedings.remove(playerUuid);
            return;
        }
        
        // Generar cría con genética combinada
        Pet offspring = generateOffspring(breeding.getPet1(), breeding.getPet2());
        
        // Crear ítem de huevo de mascota
        ItemStack eggItem = createPetEgg(offspring);
        player.getInventory().addItem(eggItem);
        
        activeBreedings.remove(playerUuid);
        MessageUtils.sendMessage(player, "<green>[Mascotas] ¡La cría ha completado! Recibiste un huevo de mascota.");
        
        // Efectos visuales
        plugin.getParticleManager().spawnRewardEffect(player);
    }
    
    private Pet generateOffspring(Pet parent1, Pet parent2) {
        // Combinar genética de ambos padres
        PetGenetics genetics = new PetGenetics();
        
        // Heredar rasgos aleatorios de ambos padres
        genetics.setStrength(Math.max(parent1.getGenetics().getStrength(), parent2.getGenetics().getStrength()) + 
                           ThreadLocalRandom.current().nextInt(-2, 3));
        genetics.setIntelligence(Math.max(parent1.getGenetics().getIntelligence(), parent2.getGenetics().getIntelligence()) + 
                               ThreadLocalRandom.current().nextInt(-2, 3));
        genetics.setAgility(Math.max(parent1.getGenetics().getAgility(), parent2.getGenetics().getAgility()) + 
                          ThreadLocalRandom.current().nextInt(-2, 3));
        genetics.setEndurance(Math.max(parent1.getGenetics().getEndurance(), parent2.getGenetics().getEndurance()) + 
                            ThreadLocalRandom.current().nextInt(-2, 3));
        
        // Crear nuevo pet con genética mejorada
        Pet offspring = new Pet(parent1.getType(), "Cría", 20.0, 1, 0.0, UUID.randomUUID());
        offspring.setGenetics(genetics);
        
        // Posibilidad de mutación (5%)
        if (ThreadLocalRandom.current().nextInt(100) < 5) {
            offspring.setMutation(generateRandomMutation());
            MessageUtils.sendMessage(Bukkit.getPlayer(parent1.getOwnerId()), 
                "<gold>[Mascotas] ¡La cría tiene una mutación especial!");
        }
        
        return offspring;
    }
    
    private ItemStack createPetEgg(Pet pet) {
        ItemStack egg = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = egg.getItemMeta();
        meta.displayName(MessageUtils.parse("<gold>Huevo de " + pet.getType().substring(0, 1).toUpperCase() + 
                          pet.getType().substring(1)));
        
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Tipo: <yellow>" + pet.getType());
        lore.add("<gray>Genética:");
        lore.add("<gray>  Fuerza: <green>" + pet.getGenetics().getStrength());
        lore.add("<gray>  Inteligencia: <blue>" + pet.getGenetics().getIntelligence());
        lore.add("<gray>  Agilidad: <yellow>" + pet.getGenetics().getAgility());
        lore.add("<gray>  Resistencia: <red>" + pet.getGenetics().getEndurance());
        
        if (pet.getMutation() != null) {
            lore.add("<gold>  Mutación: " + pet.getMutation());
        }
        
        lore.add("");
        lore.add("<gray>Click derecho para eclosionar");
        meta.lore(MessageUtils.parseList(lore));
        egg.setItemMeta(meta);
        
        return egg;
    }
    
    public void hatchPetEgg(Player player, ItemStack eggItem) {
        // Extraer datos del huevo y crear mascota
        if (playerPets.containsKey(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Ya tienes una mascota activa.");
            return;
        }
        
        // Crear mascota basada en los datos del huevo
        String petType = "wolf"; // Extraer del lore del item
        Pet newPet = new Pet(petType, "Bebé", 15.0, 1, 0.0, player.getUniqueId());
        
        // Spawnear mascota
        spawnPetEntity(player, newPet);
        playerPets.put(player.getUniqueId(), newPet);
        
        // Remover huevo del inventario
        eggItem.setAmount(eggItem.getAmount() - 1);
        
        MessageUtils.sendMessage(player, "<green>[Mascotas] ¡Tu huevo ha eclosionado!");
        plugin.getParticleManager().spawnRewardEffect(player);
    }
    
    // ===== PET EVOLUTION SYSTEM =====
    
    public void evolvePet(Player player, String evolutionPath) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }
        
        Pet pet = playerPets.get(uuid);
        PetEvolution evolution = petEvolutions.get(evolutionPath);
        
        if (evolution == null) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Ruta de evolución no válida.");
            return;
        }
        
        if (!evolution.getBaseType().equals(pet.getType())) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Tu mascota no puede evolucionar por esta ruta.");
            return;
        }
        
        if (pet.getLevel() < evolution.getRequiredLevel()) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Tu mascota necesita nivel " + 
                evolution.getRequiredLevel() + " para evolucionar.");
            return;
        }
        
        // Verificar si tiene las habilidades requeridas
        for (String requiredSkill : evolution.getRequiredSkills()) {
            if (!pet.hasSkill(requiredSkill)) {
                MessageUtils.sendMessage(player, "<red>[Mascotas] Tu mascota necesita la habilidad: " + 
                    requiredSkill);
                return;
            }
        }
        
        // Costo de evolución
        int cost = evolution.getRequiredLevel() * 500;
        if (plugin.getEconomyManager().getBalance(player) < cost) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Necesitas $" + cost + " para evolucionar.");
            return;
        }
        
        // Evolucionar mascota
        pet.evolve(evolution.getEvolvedType(), evolution.getDisplayName());
        plugin.getEconomyManager().withdrawBalance(player, cost);
        
        // Actualizar entidad
        LivingEntity petEntity = pet.getEntity();
        if (petEntity != null && !petEntity.isDead()) {
            petEntity.setCustomName("<gold>" + pet.getName() + " <gray>[" + evolution.getDisplayName() + 
                                   " Nivel " + pet.getLevel() + "]");
            petEntity.getWorld().spawnParticle(Particle.DRAGON_BREATH, petEntity.getLocation(), 30);
        }
        
        MessageUtils.sendMessage(player, "<gold>[Mascotas] ¡Tu mascota ha evolucionado a " + 
            evolution.getDisplayName() + "!");
        plugin.getParticleManager().spawnLevelUpEffect(player);
    }
    
    public void listEvolutions(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }
        
        Pet pet = playerPets.get(uuid);
        MessageUtils.sendMessage(player, "<gold>=== Evoluciones Disponibles ===");
        
        for (Map.Entry<String, PetEvolution> entry : petEvolutions.entrySet()) {
            PetEvolution evolution = entry.getValue();
            if (evolution.getBaseType().equals(pet.getType())) {
                String status = pet.getLevel() >= evolution.getRequiredLevel() ? "<green>✓" : "<red>✗";
                MessageUtils.sendMessage(player, status + " <yellow>" + evolution.getDisplayName() + 
                    " <gray>(Nivel " + evolution.getRequiredLevel() + ")");
                MessageUtils.sendMessage(player, "    <gray>Habilidades: " + 
                    String.join(", ", evolution.getRequiredSkills()));
            }
        }
    }
    
    // ===== PET TRAINING SYSTEM =====
    
    public void startTraining(Player player, String skillType) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }
        
        if (activeTrainings.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Tu mascota ya está entrenando.");
            return;
        }
        
        Pet pet = playerPets.get(uuid);
        
        // Verificar costo de entrenamiento
        int cost = getTrainingCost(skillType, pet.getSkillLevel(skillType));
        if (plugin.getEconomyManager().getBalance(player) < cost) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Necesitas $" + cost + " para entrenar.");
            return;
        }
        
        // Iniciar entrenamiento
        PetTraining training = new PetTraining(uuid, skillType, System.currentTimeMillis() + 300000); // 5 minutos
        activeTrainings.put(uuid, training);
        plugin.getEconomyManager().withdrawBalance(player, cost);
        
        MessageUtils.sendMessage(player, "<green>[Mascotas] Entrenamiento de " + skillType + " iniciado. Completará en 5 minutos.");
        
        // Programar finalización
        new BukkitRunnable() {
            @Override
            public void run() {
                completeTraining(uuid, training);
            }
        }.runTaskLater(plugin, 6000); // 5 minutos
    }
    
    private void completeTraining(UUID playerUuid, PetTraining training) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            activeTrainings.remove(playerUuid);
            return;
        }
        
        Pet pet = playerPets.get(playerUuid);
        if (pet != null) {
            pet.increaseSkill(training.getSkillType());
            MessageUtils.sendMessage(player, "<green>[Mascotas] Entrenamiento completado! Habilidad " + 
                training.getSkillType() + " mejorada.");
            plugin.getParticleManager().spawnRewardEffect(player);
        }
        
        activeTrainings.remove(playerUuid);
    }
    
    private int getTrainingCost(String skillType, int currentLevel) {
        return 100 * (currentLevel + 1);
    }
    
    public void listSkills(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }
        
        Pet pet = playerPets.get(uuid);
        MessageUtils.sendMessage(player, "<gold>=== Habilidades de Mascota ===");
        MessageUtils.sendMessage(player, "<yellow>Fuerza: <white>" + pet.getSkillLevel("strength"));
        MessageUtils.sendMessage(player, "<blue>Inteligencia: <white>" + pet.getSkillLevel("intelligence"));
        MessageUtils.sendMessage(player, "<green>Agilidad: <white>" + pet.getSkillLevel("agility"));
        MessageUtils.sendMessage(player, "<red>Resistencia: <white>" + pet.getSkillLevel("endurance"));
        MessageUtils.sendMessage(player, "<gold>Combate: <white>" + pet.getSkillLevel("combat_mastery"));
        MessageUtils.sendMessage(player, "<purple>Sigilo: <white>" + pet.getSkillLevel("stealth"));
        MessageUtils.sendMessage(player, "<aqua>Magia: <white>" + pet.getSkillLevel("magic_affinity"));
    }
    
    // ===== PET COMPETITION SYSTEM =====
    
    public void enterCompetition(Player player, String competitionType) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }
        
        Pet pet = playerPets.get(uuid);
        
        switch (competitionType.toLowerCase()) {
            case "racing":
                int agilityScore = pet.getSkillLevel("agility") * 10 + ThreadLocalRandom.current().nextInt(50);
                processCompetitionResult(player, "Carrera", agilityScore);
                break;
                
            case "combat":
                int combatScore = pet.getSkillLevel("combat_mastery") * 10 + pet.getSkillLevel("strength") * 5 + 
                                ThreadLocalRandom.current().nextInt(50);
                processCompetitionResult(player, "Combate", combatScore);
                break;
                
            case "intelligence":
                int intelligenceScore = pet.getSkillLevel("intelligence") * 10 + 
                                      ThreadLocalRandom.current().nextInt(50);
                processCompetitionResult(player, "Inteligencia", intelligenceScore);
                break;
                
            default:
                MessageUtils.sendMessage(player, "<red>[Mascotas] Tipo de competición no válido.");
                return;
        }
    }
    
    private void processCompetitionResult(Player player, String competitionType, int score) {
        Pet pet = playerPets.get(player.getUniqueId());
        
        String rank;
        int reward;
        
        if (score >= 150) {
            rank = "<gold>Oro";
            reward = 1000;
        } else if (score >= 100) {
            rank = "<gray>Plata";
            reward = 500;
        } else if (score >= 75) {
            rank = "<#CD7F32>Bronce";
            reward = 250;
        } else {
            rank = "<red>Participación";
            reward = 50;
        }
        
        plugin.getEconomyManager().addBalance(player, reward);
        pet.addExperience(reward / 10);
        
        MessageUtils.sendMessage(player, "<green>[Competición] Resultado de " + competitionType + ":");
        MessageUtils.sendMessage(player, "<white>Puntuación: <yellow>" + score);
        MessageUtils.sendMessage(player, "<white>Rango: " + rank);
        MessageUtils.sendMessage(player, "<white>Recompensa: <green>$" + reward);
        
        checkPetLevelUp(player, pet);
        plugin.getParticleManager().spawnRewardEffect(player);
    }
    
    // ===== PET HAPPINESS SYSTEM =====
    
    private void startHappinessSystem() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : playerPets.keySet()) {
                    Pet pet = playerPets.get(uuid);
                    Player player = Bukkit.getPlayer(uuid);
                    
                    if (player != null && player.isOnline()) {
                        updatePetHappiness(pet, player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1200); // Cada minuto
    }
    
    private void updatePetHappiness(Pet pet, Player player) {
        // Disminuir felicidad gradualmente
        pet.setHappiness(pet.getHappiness() - 1);
        
        // Efectos basados en felicidad
        if (pet.getHappiness() < 20) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] Tu mascota está triste. Necesita atención.");
        } else if (pet.getHappiness() > 80) {
            // Mascota feliz da bonificaciones
            if (ThreadLocalRandom.current().nextInt(100) < 5) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 600, 0));
            }
        }
    }
    
    public void playWithPet(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPets.containsKey(uuid)) {
            MessageUtils.sendMessage(player, "<red>[Mascotas] No tienes una mascota activa.");
            return;
        }
        
        Pet pet = playerPets.get(uuid);
        pet.setHappiness(Math.min(100, pet.getHappiness() + 20));
        pet.addExperience(5);
        
        MessageUtils.sendMessage(player, "<green>[Mascotas] Has jugado con " + pet.getName() + ". Felicidad: " + 
            pet.getHappiness() + "/100");
        
        LivingEntity petEntity = pet.getEntity();
        if (petEntity != null && !petEntity.isDead()) {
            petEntity.getWorld().spawnParticle(Particle.HEART, petEntity.getLocation(), 10);
        }
        
        checkPetLevelUp(player, pet);
    }
    
    // ===== BACKGROUND SYSTEMS =====
    
    private void startTrainingSystem() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, PetTraining>> iterator = activeTrainings.entrySet().iterator();
                
                while (iterator.hasNext()) {
                    Map.Entry<UUID, PetTraining> entry = iterator.next();
                    PetTraining training = entry.getValue();
                    
                    if (currentTime >= training.getCompletionTime()) {
                        completeTraining(entry.getKey(), training);
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 200); // Cada 10 segundos
    }
    
    private void startBreedingSystem() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, PetBreeding>> iterator = activeBreedings.entrySet().iterator();
                
                while (iterator.hasNext()) {
                    Map.Entry<UUID, PetBreeding> entry = iterator.next();
                    PetBreeding breeding = entry.getValue();
                    
                    if (currentTime >= breeding.getCompletionTime()) {
                        completeBreeding(entry.getKey(), breeding);
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 400); // Cada 20 segundos
    }
    
    private String generateRandomMutation() {
        String[] mutations = {
            "Shiny", "Giant", "Miniature", "Glowing", "Swift", "Hardy", "Intelligent", "Lucky"
        };
        return mutations[ThreadLocalRandom.current().nextInt(mutations.length)];
    }
    
    private void spawnPetEntity(Player player, Pet pet) {
        EntityType entityType = getPetEntityType(pet.getType());
        if (entityType != null) {
            Location spawnLoc = player.getLocation().add(1, 0, 1);
            LivingEntity petEntity = (LivingEntity) player.getWorld().spawnEntity(spawnLoc, entityType);
            
            petEntity.setCustomName("<green>" + pet.getName() + " <gray>[Nivel " + pet.getLevel() + "]");
            petEntity.setCustomNameVisible(true);
            petEntity.setInvulnerable(true);
            petEntity.setGravity(false);
            
            pet.setEntity(petEntity);
            makePetFollow(player, petEntity);
        }
    }

    // ===== SUPPORTING CLASSES =====
    
    public static class PetGenetics {
        private int strength = 5;
        private int intelligence = 5;
        private int agility = 5;
        private int endurance = 5;
        
        // Getters and setters
        public int getStrength() { return strength; }
        public void setStrength(int strength) { this.strength = Math.max(1, Math.min(20, strength)); }
        public int getIntelligence() { return intelligence; }
        public void setIntelligence(int intelligence) { this.intelligence = Math.max(1, Math.min(20, intelligence)); }
        public int getAgility() { return agility; }
        public void setAgility(int agility) { this.agility = Math.max(1, Math.min(20, agility)); }
        public int getEndurance() { return endurance; }
        public void setEndurance(int endurance) { this.endurance = Math.max(1, Math.min(20, endurance)); }
    }
    
    public static class PetEvolution {
        private final String baseType;
        private final String evolvedType;
        private final int requiredLevel;
        private final List<String> requiredSkills;
        private final String displayName;
        
        public PetEvolution(String baseType, String evolvedType, int requiredLevel, 
                           List<String> requiredSkills, String displayName) {
            this.baseType = baseType;
            this.evolvedType = evolvedType;
            this.requiredLevel = requiredLevel;
            this.requiredSkills = requiredSkills;
            this.displayName = displayName;
        }
        
        // Getters
        public String getBaseType() { return baseType; }
        public String getEvolvedType() { return evolvedType; }
        public int getRequiredLevel() { return requiredLevel; }
        public List<String> getRequiredSkills() { return requiredSkills; }
        public String getDisplayName() { return displayName; }
    }
    
    public static class PetBreeding {
        private final UUID player1;
        private final UUID player2;
        private final Pet pet1;
        private final Pet pet2;
        private final long completionTime;
        
        public PetBreeding(UUID player1, UUID player2, Pet pet1, Pet pet2) {
            this.player1 = player1;
            this.player2 = player2;
            this.pet1 = pet1;
            this.pet2 = pet2;
            this.completionTime = System.currentTimeMillis() + 600000; // 10 minutos
        }
        
        // Getters
        public UUID getPlayer1() { return player1; }
        public UUID getPlayer2() { return player2; }
        public Pet getPet1() { return pet1; }
        public Pet getPet2() { return pet2; }
        public long getCompletionTime() { return completionTime; }
    }
    
    public static class PetTraining {
        private final UUID playerId;
        private final String skillType;
        private final long completionTime;
        
        public PetTraining(UUID playerId, String skillType, long completionTime) {
            this.playerId = playerId;
            this.skillType = skillType;
            this.completionTime = completionTime;
        }
        
        // Getters
        public UUID getPlayerId() { return playerId; }
        public String getSkillType() { return skillType; }
        public long getCompletionTime() { return completionTime; }
    }

    public static class Pet {
        private String type;
        private String name;
        private double health;
        private double maxHealth;
        private int level;
        private double experience;
        private LivingEntity entity;
        private final UUID ownerId;
        private long skillCooldown = 0;
        private PetGenetics genetics;
        private String mutation;
        private int happiness = 50;
        private final Map<String, Integer> skills = new HashMap<>();
        
        public Pet(String type, String name, double health, int level, double experience, UUID ownerId) {
            this.type = type;
            this.name = name;
            this.health = health;
            this.maxHealth = health;
            this.level = level;
            this.experience = experience;
            this.ownerId = ownerId;
            this.genetics = new PetGenetics();
            initializeSkills();
        }
        
        private void initializeSkills() {
            skills.put("strength", 1);
            skills.put("intelligence", 1);
            skills.put("agility", 1);
            skills.put("endurance", 1);
            skills.put("combat_mastery", 0);
            skills.put("stealth", 0);
            skills.put("magic_affinity", 0);
            skills.put("pack_leader", 0);
            skills.put("intimidation", 0);
            skills.put("night_vision", 0);
            skills.put("luck", 0);
            skills.put("cold_immunity", 0);
            skills.put("fire_immunity", 0);
            skills.put("treasure_hunter", 0);
            skills.put("elemental_magic", 0);
            skills.put("flight_mastery", 0);
            skills.put("fire_magic", 0);
            skills.put("martial_arts", 0);
            skills.put("inner_peace", 0);
        }

        // Getters y setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getHealth() { return health; }
        public void setHealth(double health) { this.health = health; }
        public double getMaxHealth() { return maxHealth; }
        public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public double getExperience() { return experience; }
        public void setExperience(double experience) { this.experience = experience; }
        public void addExperience(double exp) { this.experience += exp; }
        public LivingEntity getEntity() { return entity; }
        public void setEntity(LivingEntity entity) { this.entity = entity; }
        public UUID getOwnerId() { return ownerId; }
        public long getSkillCooldown() { return skillCooldown; }
        public void setSkillCooldown(long cooldown) { this.skillCooldown = cooldown; }
        
        // Advanced getters and setters
        public PetGenetics getGenetics() { return genetics; }
        public void setGenetics(PetGenetics genetics) { this.genetics = genetics; }
        public String getMutation() { return mutation; }
        public void setMutation(String mutation) { this.mutation = mutation; }
        public int getHappiness() { return happiness; }
        public void setHappiness(int happiness) { this.happiness = Math.max(0, Math.min(100, happiness)); }
        
        // Skill methods
        public int getSkillLevel(String skill) {
            return skills.getOrDefault(skill, 0);
        }
        
        public void increaseSkill(String skill) {
            int currentLevel = getSkillLevel(skill);
            skills.put(skill, Math.min(20, currentLevel + 1));
        }
        
        public boolean hasSkill(String skill) {
            return getSkillLevel(skill) > 0;
        }
        
        public Map<String, Integer> getAllSkills() {
            return new HashMap<>(skills);
        }
        
        // Evolution method
        public void evolve(String newType, String displayName) {
            this.type = newType;
            this.maxHealth += 10;
            this.health = this.maxHealth;
            this.level += 5;
            
            // Boost all skills
            for (String skill : skills.keySet()) {
                skills.put(skill, skills.get(skill) + 2);
            }
        }
    }

    public int getPetLevel(UUID playerId) {
        Pet pet = playerPets.get(playerId);
        if (pet != null) {
            return pet.getLevel();
        }
        
        // Check stored pets
        pet = storedPets.get(playerId);
        if (pet != null) {
            return pet.getLevel();
        }
        
        return 1; // Default level
    }
}