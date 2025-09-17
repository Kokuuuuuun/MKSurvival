package com.mk.mksurvival.managers.economy;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EconomyManager {
    private final MKSurvival plugin;
    private final HashMap<UUID, Double> balances = new HashMap<>();
    private FileConfiguration config;
    private String currencySymbol;
    private LoreEnchantmentManager loreEnchantmentManager;

    public EconomyManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig();
        this.currencySymbol = config.getString("economy.currency_symbol", "$");
        this.loreEnchantmentManager = new LoreEnchantmentManager(plugin);
        loadAllPlayerData();
    }

    // Métodos de economía
    public double getBalance(UUID player) {
        return balances.getOrDefault(player, config.getDouble("economy.starting_balance", 0.0));
    }

    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public void setBalance(UUID player, double amount) {
        balances.put(player, amount);
    }

    public void setBalance(Player player, double amount) {
        setBalance(player.getUniqueId(), amount);
    }

    public void addBalance(UUID player, double amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public void addBalance(Player player, double amount) {
        addBalance(player.getUniqueId(), amount);
    }

    public void removeBalance(UUID player, double amount) {
        double currentBalance = getBalance(player);
        double newBalance = Math.max(0, currentBalance - amount);
        setBalance(player, newBalance);
    }

    public void removeBalance(Player player, double amount) {
        removeBalance(player.getUniqueId(), amount);
    }

    public void withdrawBalance(Player player, double amount) {
        removeBalance(player, amount);
    }

    public boolean hasBalance(UUID player, double amount) {
        return getBalance(player) >= amount;
    }

    public boolean hasBalance(Player player, double amount) {
        return hasBalance(player.getUniqueId(), amount);
    }

    public String formatCurrency(double amount) {
        return currencySymbol + String.format("%.2f", amount);
    }

    public void depositBalance(Player player, double amount) {
        addBalance(player, amount);
    }

    public void depositOfflineBalance(UUID player, double amount) {
        addBalance(player, amount);
    }

    public void saveAllPlayerData() {
        for (UUID uuid : balances.keySet()) {
            config.set("players." + uuid.toString() + ".balance", balances.get(uuid));
        }
        plugin.getConfigManager().saveConfig();
    }

    private void loadAllPlayerData() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (config.contains("players." + uuid.toString() + ".balance")) {
                balances.put(uuid, config.getDouble("players." + uuid.toString() + ".balance"));
            } else {
                balances.put(uuid, config.getDouble("economy.starting_balance", 0.0));
            }
        }
    }

    // Getter para el gestor de encantamientos
    public LoreEnchantmentManager getLoreEnchantmentManager() {
        return loreEnchantmentManager;
    }

    // Clase interna para encantamientos personalizados
    public static class LoreEnchantmentManager implements Listener {
        private final MKSurvival plugin;
        private final Map<String, CustomEnchantment> enchantments = new HashMap<>();
        private final Map<UUID, Long> lastEnchantUse = new HashMap<>();

        public LoreEnchantmentManager(MKSurvival plugin) {
            this.plugin = plugin;
            registerCustomEnchantments();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        private void registerCustomEnchantments() {
            // Encantamiento de Vampirismo
            enchantments.put("vampirismo", new CustomEnchantment(
                    "Vampirismo",
                    "Drena vida de tus enemigos",
                    3,
                    EnchantmentTarget.WEAPON,
                    new ArrayList<>(Arrays.asList(Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.NETHERITE_SWORD))
            ));

            // Encantamiento de Explosión
            enchantments.put("explosion", new CustomEnchantment(
                    "Explosión",
                    "Crea explosiones al impactar",
                    5,
                    EnchantmentTarget.WEAPON,
                    new ArrayList<>(Arrays.asList(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.NETHERITE_PICKAXE))
            ));

            // Encantamiento de Teletransporte
            enchantments.put("teleport", new CustomEnchantment(
                    "Teletransporte",
                    "Teletransporta al enemigo al impactar",
                    2,
                    EnchantmentTarget.WEAPON,
                    new ArrayList<>(Arrays.asList(Material.BOW, Material.CROSSBOW))
            ));

            // Encantamiento de Vuelo
            enchantments.put("vuelo", new CustomEnchantment(
                    "Vuelo",
                    "Permite volar temporalmente",
                    1,
                    EnchantmentTarget.ARMOR,
                    new ArrayList<>(Arrays.asList(Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS))
            ));

            // Encantamiento de Fortuna
            enchantments.put("fortuna", new CustomEnchantment(
                    "Fortuna",
                    "Aumenta las probabilidades de obtener drops raros",
                    5,
                    EnchantmentTarget.TOOL,
                    new ArrayList<>(Arrays.asList(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.NETHERITE_PICKAXE,
                            Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.NETHERITE_SWORD,
                            Material.BOW, Material.CROSSBOW))
            ));

            // Encantamiento de Regeneración
            enchantments.put("regeneracion", new CustomEnchantment(
                    "Regeneración",
                    "Regenera salud lentamente",
                    3,
                    EnchantmentTarget.ARMOR,
                    new ArrayList<>(Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
                            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
                            Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS))
            ));

            // Encantamiento de Invisibilidad
            enchantments.put("invisibilidad", new CustomEnchantment(
                    "Invisibilidad",
                    "Te hace invisible al agacharte",
                    1,
                    EnchantmentTarget.ARMOR,
                    new ArrayList<>(Arrays.asList(Material.LEATHER_BOOTS))
            ));

            // Encantamiento de Velocidad de Minado
            enchantments.put("velocidad_minado", new CustomEnchantment(
                    "Velocidad de Minado",
                    "Aumenta la velocidad de minado",
                    5,
                    EnchantmentTarget.TOOL,
                    new ArrayList<>(Arrays.asList(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.NETHERITE_PICKAXE))
            ));

            // Encantamiento de Multishot
            enchantments.put("multishot", new CustomEnchantment(
                    "Multishot",
                    "Dispara múltiples flechas",
                    3,
                    EnchantmentTarget.WEAPON,
                    new ArrayList<>(Arrays.asList(Material.BOW, Material.CROSSBOW))
            ));

            // Encantamiento de Absorción
            enchantments.put("absorcion", new CustomEnchantment(
                    "Absorción",
                    "Absorbe parte del daño recibido",
                    3,
                    EnchantmentTarget.ARMOR,
                    new ArrayList<>(Arrays.asList(Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE))
            ));
        }

        public void applyCustomEnchantment(Player player, ItemStack item, String enchantmentId, int level) {
            // Implementación pendiente
        }

        private int getEnchantmentCost(String enchantmentId, int level) {
            CustomEnchantment enchantment = enchantments.get(enchantmentId);
            if (enchantment == null) return 0;
            return 1000 * level * enchantment.getRarity(); // Costo base * nivel * rareza
        }

        public void listCustomEnchantments(Player player) {
            MessageUtils.sendMessage(player, "<gold>=== Encantamientos Personalizados ===");
            for (CustomEnchantment enchantment : enchantments.values()) {
                MessageUtils.sendMessage(player, "<yellow>" + enchantment.getName() + " (Nivel " + enchantment.getMaxLevel() + ")");
                MessageUtils.sendMessage(player, "<gray>" + enchantment.getDescription());
                MessageUtils.sendMessage(player, "<gray>Aplicable a: " + formatMaterials(enchantment.getApplicableMaterials()));
                MessageUtils.sendMessage(player, "<gray>Rareza: " + enchantment.getRarity());
                MessageUtils.sendMessage(player, "");
            }
        }

        private String formatMaterials(List<Material> materials) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < materials.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(materials.get(i).name().replace("_", " ").toLowerCase());
            }
            return sb.toString();
        }

        @EventHandler
        public void onEntityDamage(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();
            Player attacker = null;

            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
                attacker = (Player) ((Arrow) event.getDamager()).getShooter();
            }

            if (attacker == null) return;

            // Verificar encantamientos del atacante
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasLore()) {
                List<String> lore = weapon.getItemMeta().getLore();
                for (String line : lore) {
                    if (line.startsWith("<gray>[Encantamiento: ")) {
                        String enchantmentName = line.split(": ")[1].split(" \\(")[0];
                        int level = Integer.parseInt(line.split("\\(")[1].split("\\)")[0]);
                        applyEnchantmentEffect(attacker, player, enchantmentName, level, event);
                    }
                }
            }

            // Verificar encantamientos del defensor
            for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
                if (armorPiece != null && armorPiece.hasItemMeta() && armorPiece.getItemMeta().hasLore()) {
                    List<String> lore = armorPiece.getItemMeta().getLore();
                    for (String line : lore) {
                        if (line.startsWith("<gray>[Encantamiento: ")) {
                            String enchantmentName = line.split(": ")[1].split(" \\(")[0];
                            int level = Integer.parseInt(line.split("\\(")[1].split("\\)")[0]);
                            applyDefenseEnchantmentEffect(player, attacker, enchantmentName, level, event);
                        }
                    }
                }
            }
        }

        private void applyEnchantmentEffect(Player attacker, Player victim, String enchantmentName, int level, EntityDamageEvent event) {
            switch (enchantmentName.toLowerCase()) {
                case "vampirismo":
                    // Drenar vida
                    double drainAmount = event.getFinalDamage() * (0.1 * level); // 10% por nivel
                    double newHealth = Math.min(attacker.getHealth() + drainAmount, attacker.getMaxHealth());
                    attacker.setHealth(newHealth);
                    MessageUtils.sendMessage(attacker, "<red>[Vampirismo] Has drenado " + String.format("%.1f", drainAmount) + " de vida");
                    // Efectos visuales
                    victim.getWorld().spawnParticle(Particle.DUST, victim.getLocation(), 10);
                    break;
                case "explosion":
                    // Crear explosión
                    if (ThreadLocalRandom.current().nextInt(100) < (20 * level)) { // 20% por nivel
                        victim.getWorld().createExplosion(victim.getLocation(), 2.0f * level, false, false);
                        MessageUtils.sendMessage(attacker, "<red>[Explosión] ¡Explosión activada!");
                    }
                    break;
                case "teleport":
                    // Teletransportar al enemigo
                    if (ThreadLocalRandom.current().nextInt(100) < (15 * level)) { // 15% por nivel
                        Location attackerLoc = attacker.getLocation();
                        Location victimLoc = victim.getLocation();
                        victim.teleport(attackerLoc);
                        attacker.teleport(victimLoc);
                        MessageUtils.sendMessage(attacker, "<red>[Teletransporte] ¡Has intercambiado posiciones!");
                        MessageUtils.sendMessage(victim, "<red>[Teletransporte] ¡Has sido teletransportado!");
                    }
                    break;
            }
        }

        private void applyDefenseEnchantmentEffect(Player defender, Player attacker, String enchantmentName, int level, EntityDamageEvent event) {
            switch (enchantmentName.toLowerCase()) {
                case "absorcion":
                    // Absorber daño
                    double absorption = event.getFinalDamage() * (0.15 * level); // 15% por nivel
                    event.setDamage(event.getFinalDamage() - absorption);
                    MessageUtils.sendMessage(defender, "<green>[Absorción] Has absorbido " + String.format("%.1f", absorption) + " de daño");
                    break;
                case "regeneracion":
                    // Regenerar salud después del daño
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (defender.isOnline() && defender.getHealth() < defender.getMaxHealth()) {
                                double newHealth = Math.min(defender.getHealth() + (1.0 * level), defender.getMaxHealth());
                                defender.setHealth(newHealth);
                                defender.getWorld().spawnParticle(Particle.HEART, defender.getLocation(), 5);
                            }
                        }
                    }.runTaskLater(plugin, 20); // 1 segundo después
                    break;
            }
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool != null && tool.hasItemMeta() && tool.getItemMeta().hasLore()) {
                List<String> lore = tool.getItemMeta().getLore();
                for (String line : lore) {
                    if (line.startsWith("<gray>[Encantamiento: ")) {
                        String enchantmentName = line.split(": ")[1].split(" \\(")[0];
                        int level = Integer.parseInt(line.split("\\(")[1].split("\\)")[0]);
                        applyToolEnchantmentEffect(player, event.getBlock(), enchantmentName, level, event);
                    }
                }
            }
        }

        private void applyToolEnchantmentEffect(Player player, Block block, String enchantmentName, int level, BlockBreakEvent event) {
            ItemStack tool = player.getInventory().getItemInMainHand();

            switch (enchantmentName.toLowerCase()) {
                case "explosion":
                    // Romper bloques en área
                    if (ThreadLocalRandom.current().nextInt(100) < (30 * level)) { // 30% por nivel
                        int radius = 1 + level;
                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    Block relativeBlock = block.getRelative(x, y, z);
                                    if (relativeBlock.getType() != Material.BEDROCK &&
                                            relativeBlock.getType().getHardness() <= block.getType().getHardness()) {
                                        relativeBlock.breakNaturally(tool);
                                    }
                                }
                            }
                        }
                        MessageUtils.sendMessage(player, "<red>[Explosión] ¡Rompeo en área!");
                    }
                    break;
                case "velocidad_minado":
                    // Aumentar velocidad de minado
                    if (ThreadLocalRandom.current().nextInt(100) < (50 * level)) { // 50% por nivel
                        // Dar experiencia extra
                        plugin.getSkillManager().addExp(player, SkillType.MINING, level * 2);
                        // Chance de drop extra
                        if (ThreadLocalRandom.current().nextInt(100) < (25 * level)) {
                            Collection<ItemStack> drops = block.getDrops(tool);
                            for (ItemStack drop : drops) {
                                block.getWorld().dropItemNaturally(block.getLocation(), drop);
                            }
                        }
                        MessageUtils.sendMessage(player, "<yellow>[Velocidad de Minado] ¡Minado rápido!");
                    }
                    break;
                case "fortuna":
                    // Aumentar drops raros
                    if (ThreadLocalRandom.current().nextInt(100) < (10 * level)) { // 10% por nivel
                        // Generar item raro
                        ItemStack rareDrop = getRareDrop(block.getType());
                        if (rareDrop != null) {
                            block.getWorld().dropItemNaturally(block.getLocation(), rareDrop);
                            MessageUtils.sendMessage(player, "<gold>[Fortuna] ¡Has encontrado un item raro!");
                        }
                    }
                    break;
            }
        }

        private ItemStack getRareDrop(Material blockType) {
            // Drops raros según el tipo de bloque
            if (blockType.name().contains("ORE")) {
                Material[] rareDrops = {
                        Material.DIAMOND, Material.EMERALD, Material.NETHERITE_SCRAP,
                        Material.ENDER_PEARL, Material.BLAZE_ROD, Material.END_CRYSTAL
                };
                return new ItemStack(rareDrops[ThreadLocalRandom.current().nextInt(rareDrops.length)]);
            } else if (blockType.name().contains("LOG")) {
                return new ItemStack(Material.GOLDEN_APPLE);
            } else {
                return null;
            }
        }

        @EventHandler
        public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
            Player player = event.getPlayer();
            if (!event.isSneaking()) return; // Solo cuando se agacha

            // Verificar botas con encantamiento de vuelo
            ItemStack boots = player.getInventory().getBoots();
            if (boots != null && boots.hasItemMeta() && boots.getItemMeta().hasLore()) {
                List<String> lore = boots.getItemMeta().getLore();
                for (String line : lore) {
                    if (line.startsWith("<gray>[Encantamiento: Vuelo ")) {
                        int level = Integer.parseInt(line.split("\\(")[1].split("\\)")[0]);

                        // Verificar cooldown
                        UUID uuid = player.getUniqueId();
                        if (lastEnchantUse.containsKey(uuid) &&
                                System.currentTimeMillis() - lastEnchantUse.get(uuid) < 30000) { // 30 segundos
                            MessageUtils.sendMessage(player, "<red>[Vuelo] Debes esperar antes de usar este encantamiento nuevamente.");
                            return;
                        }

                        lastEnchantUse.put(uuid, System.currentTimeMillis());

                        // Dar efecto de vuelo
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        MessageUtils.sendMessage(player, "<green>[Vuelo] ¡Estás volando!");

                        // Quitar vuelo después de 10 segundos
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    player.setFlying(false);
                                    player.setAllowFlight(false);
                                    MessageUtils.sendMessage(player, "<red>[Vuelo] Tu vuelo ha terminado.");
                                }
                            }
                        }.runTaskLater(plugin, 200); // 10 segundos
                        break;
                    }
                }
            }
        }

        @EventHandler
        public void onPlayerToggleSneakForInvisibility(PlayerToggleSneakEvent event) {
            Player player = event.getPlayer();
            if (!event.isSneaking()) return; // Solo cuando se agacha

            // Verificar botas de cuero con encantamiento de invisibilidad
            ItemStack boots = player.getInventory().getBoots();
            if (boots != null && boots.getType() == Material.LEATHER_BOOTS &&
                    boots.hasItemMeta() && boots.getItemMeta().hasLore()) {
                List<String> lore = boots.getItemMeta().getLore();
                for (String line : lore) {
                    if (line.startsWith("<gray>[Encantamiento: Invisibilidad ")) {
                        // Hacer invisible al jugador
                        for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                            if (onlinePlayer != player) {
                                onlinePlayer.hidePlayer(plugin, player);
                            }
                        }
                        MessageUtils.sendMessage(player, "<green>[Invisibilidad] Ahora eres invisible para otros jugadores.");

                        // Quitar invisibilidad cuando deje de agacharse
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline() && !player.isSneaking()) {
                                    for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                                        if (onlinePlayer != player) {
                                            onlinePlayer.showPlayer(plugin, player);
                                        }
                                    }
                                    MessageUtils.sendMessage(player, "<red>[Invisibilidad] Ya no eres invisible.");
                                }
                            }
                        }.runTaskLater(plugin, 5); // Verificar después de 0.25 segundos
                        break;
                    }
                }
            }
        }

        @EventHandler
        public void onEntityShootBow(EntityShootBowEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();
            ItemStack bow = event.getBow();

            if (bow != null && bow.hasItemMeta() && bow.getItemMeta().hasLore()) {
                List<String> lore = bow.getItemMeta().getLore();
                for (String line : lore) {
                    if (line.startsWith("<gray>[Encantamiento: Multishot ")) {
                        int level = Integer.parseInt(line.split("\\(")[1].split("\\)")[0]);

                        // Disparar flechas adicionales
                        int extraArrows = level;
                        for (int i = 0; i < extraArrows; i++) {
                            Arrow arrow = (Arrow) player.getWorld().spawnEntity(
                                    player.getEyeLocation(), EntityType.ARROW);
                            arrow.setShooter(player);
                            arrow.setVelocity(event.getProjectile().getVelocity());
                        }
                        MessageUtils.sendMessage(player, "<yellow>[Multishot] ¡Has disparado " + (extraArrows + 1) + " flechas!");
                        break;
                    }
                }
            }
        }

        public static class CustomEnchantment {
            private final String name;
            private final String description;
            private final int maxLevel;
            private final EnchantmentTarget target;
            private final List<Material> applicableMaterials;
            private final int rarity; // 1-5, donde 5 es el más raro

            public CustomEnchantment(String name, String description, int maxLevel,
                                     EnchantmentTarget target, List<Material> applicableMaterials) {
                this.name = name;
                this.description = description;
                this.maxLevel = maxLevel;
                this.target = target;
                this.applicableMaterials = applicableMaterials;
                this.rarity = calculateRarity(name);
            }

            private int calculateRarity(String name) {
                switch (name.toLowerCase()) {
                    case "vampirismo":
                    case "explosion":
                        return 3;
                    case "teleport":
                    case "vuelo":
                        return 4;
                    case "fortuna":
                    case "absorcion":
                        return 2;
                    case "regeneracion":
                    case "invisibilidad":
                    case "velocidad_minado":
                        return 1;
                    case "multishot":
                        return 3;
                    default:
                        return 1;
                }
            }

            // Getters
            public String getName() { return name; }
            public String getDescription() { return description; }
            public int getMaxLevel() { return maxLevel; }
            public EnchantmentTarget getTarget() { return target; }
            public List<Material> getApplicableMaterials() { return applicableMaterials; }
            public int getRarity() { return rarity; }
            public boolean canApplyTo(Material material) {
                return applicableMaterials.contains(material);
            }
        }
    }
}