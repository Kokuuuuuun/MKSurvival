package com.mk.mksurvival.managers.land;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.*;

public class LandManager {
    private final MKSurvival plugin;
    private final Map<String, LandClaim> landClaims = new HashMap<>();
    private final Map<UUID, Integer> playerClaimBlocks = new HashMap<>();
    private final RegionContainer regionContainer;

    public LandManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        loadAllLandClaims();
        loadAllPlayerBlocks();
    }

    public void claimSelection(Player player) {
        Location[] selection = plugin.getLandSelectionManager().getSelection(player);
        if (selection == null || selection[0] == null || selection[1] == null) {
            MessageUtils.sendMessage(player, "<red>[Land] No tienes una selección completa. Usa /land selection para empezar.</red>");
            return;
        }

        int blocks = calculateBlocks(selection[0], selection[1]);
        int availableBlocks = getPlayerClaimBlocks(player);
        int maxClaims = plugin.getConfig().getInt("land.max_claims_per_player", 3);
        int playerClaims = (int) landClaims.values().stream()
                .filter(claim -> claim.getOwnerId().equals(player.getUniqueId()))
                .count();

        if (playerClaims >= maxClaims) {
            MessageUtils.sendMessage(player, "<red>[Land] Has alcanzado el límite de " + maxClaims + " tierras.</red>");
            return;
        }

        if (blocks > availableBlocks) {
            MessageUtils.sendMessage(player, "<red>[Land] No tienes suficientes bloques de reclamo. Necesitas " + blocks + " pero solo tienes " + availableBlocks + "</red>");
            return;
        }

        if (isOverlapping(selection[0], selection[1])) {
            MessageUtils.sendMessage(player, "<red>[Land] La selección se superpone con otra tierra.</red>");
            return;
        }

        String claimId = "land_" + player.getUniqueId() + "_" + System.currentTimeMillis();
        LandClaim claim = new LandClaim(claimId, player.getUniqueId(), selection[0], selection[1]);

        // Crear región de WorldGuard
        if (!createWorldGuardRegion(claimId, selection[0], selection[1], player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] No se pudo crear la región de WorldGuard.</red>");
            return;
        }

        landClaims.put(claimId, claim);
        setPlayerClaimBlocks(player, availableBlocks - blocks);
        
        // Desactivar la selección y eliminar la herramienta
        plugin.getLandSelectionManager().endSelection(player);
        
        MessageUtils.sendMessage(player, "<green>[Land] Has reclamado " + blocks + " bloques de tierra.</green>");
        MessageUtils.sendMessage(player, "<green>[Land] Usa /land trust <jugador> para dar acceso a otros jugadores.</green>");
        
        // Abrir el menú de administración de terrenos después de reclamar
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            com.mk.mksurvival.gui.land.LandGUI.openMainGUI(player);
        }, 20L); // Esperar 1 segundo antes de abrir el menú
    }

    private boolean createWorldGuardRegion(String regionId, Location loc1, Location loc2, UUID owner) {
        World world = loc1.getWorld();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            return false;
        }

        // Usar altura mínima y máxima del mundo para protección completa vertical
        BlockVector3 min = BlockVector3.at(
                Math.min(loc1.getX(), loc2.getX()),
                world.getMinHeight(), // Altura mínima del mundo
                Math.min(loc1.getZ(), loc2.getZ())
        );
        BlockVector3 max = BlockVector3.at(
                Math.max(loc1.getX(), loc2.getX()),
                world.getMaxHeight(), // Altura máxima del mundo
                Math.max(loc1.getZ(), loc2.getZ())
        );

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);

        // Establecer dueño
        region.getOwners().addPlayer(owner);

        // Establecer flags de protección (ALLOW para owners/members, DENY para otros)
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);
        region.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
        region.setFlag(Flags.ITEM_FRAME_ROTATE, StateFlag.State.ALLOW);
        region.setFlag(Flags.RIDE, StateFlag.State.ALLOW);
        region.setFlag(Flags.POTION_SPLASH, StateFlag.State.DENY);

        // ENTITY_DAMAGE fue eliminado en versiones recientes de WorldGuard
        // Si necesitas esta funcionalidad, usa Flags.ENTITY_DAMAGE_BY_BLOCK o Flags.ENTITY_DAMAGE_BY_ENTITY

        try {
            regionManager.addRegion(region);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void unclaimLand(Player player) {
        LandClaim claim = getLandAt(player.getLocation());
        if (claim == null) {
            MessageUtils.sendMessage(player, "<red>[Land] No estás en una tierra reclamada.</red>");
            return;
        }

        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] No eres el dueño de esta tierra.</red>");
            return;
        }

        int blocks = calculateBlocks(claim.getCorner1(), claim.getCorner2());
        int availableBlocks = getPlayerClaimBlocks(player);
        setPlayerClaimBlocks(player, availableBlocks + blocks);

        // Eliminar la región de WorldGuard
        removeWorldGuardRegion(claim.getId(), claim.getCorner1().getWorld());

        landClaims.remove(claim.getId());
        MessageUtils.sendMessage(player, "<green>[Land] Has abandonado esta tierra.</green>");
    }

    private void removeWorldGuardRegion(String regionId, World world) {
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager != null) {
            regionManager.removeRegion(regionId);
        }
    }

    public void trustPlayer(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            MessageUtils.sendMessage(player, "<red>[Land] Jugador no encontrado o no está en línea.</red>");
            return;
        }

        LandClaim claim = getLandAt(player.getLocation());
        if (claim == null) {
            MessageUtils.sendMessage(player, "<red>[Land] No estás en una tierra reclamada.</red>");
            return;
        }

        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] No eres el dueño de esta tierra.</red>");
            return;
        }

        if (claim.getTrustedPlayers().contains(target.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] " + targetName + " ya tiene acceso a esta tierra.</red>");
            return;
        }

        claim.addTrustedPlayer(target.getUniqueId());
        // Agregar como miembro en la región de WorldGuard
        addMemberToWorldGuardRegion(claim.getId(), claim.getCorner1().getWorld(), target.getUniqueId());

        MessageUtils.sendMessage(player, "<green>[Land] Has dado acceso a " + targetName + " en tu tierra.</green>");
        MessageUtils.sendMessage(target, "<green>[Land] " + player.getName() + " te ha dado acceso a su tierra.</green>");
    }

    private void addMemberToWorldGuardRegion(String regionId, World world, UUID memberId) {
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager != null) {
            ProtectedRegion region = regionManager.getRegion(regionId);
            if (region != null) {
                region.getMembers().addPlayer(memberId);
            }
        }
    }

    public void untrustPlayer(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            MessageUtils.sendMessage(player, "<red>[Land] Jugador no encontrado o no está en línea.</red>");
            return;
        }

        LandClaim claim = getLandAt(player.getLocation());
        if (claim == null) {
            MessageUtils.sendMessage(player, "<red>[Land] No estás en una tierra reclamada.</red>");
            return;
        }

        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] No eres el dueño de esta tierra.</red>");
            return;
        }

        if (!claim.getTrustedPlayers().contains(target.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] " + targetName + " no tiene acceso a esta tierra.</red>");
            return;
        }

        claim.removeTrustedPlayer(target.getUniqueId());
        // Remover miembro de la región de WorldGuard
        removeMemberFromWorldGuardRegion(claim.getId(), claim.getCorner1().getWorld(), target.getUniqueId());

        MessageUtils.sendMessage(player, "<green>[Land] Has quitado el acceso a " + targetName + " en tu tierra.</green>");
        MessageUtils.sendMessage(target, "<red>[Land] " + player.getName() + " te ha quitado el acceso a su tierra.</red>");
    }
    
    /**
     * Establece un nombre personalizado para el terreno en el que se encuentra el jugador
     * @param player El jugador que quiere establecer el nombre
     * @param name El nombre personalizado para el terreno
     */
    public void setLandName(Player player, String name) {
        LandClaim claim = getLandAt(player.getLocation());
        if (claim == null) {
            MessageUtils.sendMessage(player, "<red>[Land] No estás en una tierra reclamada.</red>");
            return;
        }

        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] No eres el dueño de esta tierra.</red>");
            return;
        }
        
        if (claim.setCustomName(name)) {
            saveLandClaim(claim);
            MessageUtils.sendMessage(player, "<green>[Land] Has establecido el nombre de tu terreno a: <yellow>" + name + "</yellow></green>");
        } else {
            MessageUtils.sendMessage(player, "<red>[Land] El nombre no es válido. Debe tener entre 3 y 32 caracteres y solo puede contener letras, números, espacios y algunos símbolos.</red>");
        }
    }
    
    /**
     * Elimina el nombre personalizado del terreno en el que se encuentra el jugador
     * @param player El jugador que quiere eliminar el nombre
     */
    public void removeLandName(Player player) {
        LandClaim claim = getLandAt(player.getLocation());
        if (claim == null) {
            MessageUtils.sendMessage(player, "<red>[Land] No estás en una tierra reclamada.</red>");
            return;
        }

        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "<red>[Land] No eres el dueño de esta tierra.</red>");
            return;
        }
        
        if (claim.getCustomName() == null) {
            MessageUtils.sendMessage(player, "<red>[Land] Este terreno no tiene un nombre personalizado.</red>");
            return;
        }
        
        claim.setCustomName(null);
        saveLandClaim(claim);
        MessageUtils.sendMessage(player, "<green>[Land] Has eliminado el nombre personalizado de tu terreno.</green>");
    }

    private void removeMemberFromWorldGuardRegion(String regionId, World world, UUID memberId) {
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager != null) {
            ProtectedRegion region = regionManager.getRegion(regionId);
            if (region != null) {
                region.getMembers().removePlayer(memberId);
            }
        }
    }

    // Métodos faltantes que se usan en LandCommand
    public void showLandInfo(Player player) {
        LandClaim claim = getLandAt(player.getLocation());
        if (claim == null) {
            MessageUtils.sendMessage(player, "<red>[Land] No estás en una tierra reclamada.</red>");
            return;
        }

        String ownerName = plugin.getServer().getOfflinePlayer(claim.getOwnerId()).getName();
        int blocks = calculateBlocks(claim.getCorner1(), claim.getCorner2());

        MessageUtils.sendMessage(player, "<gold>=== Información de la Tierra ===</gold>");
        MessageUtils.sendMessage(player, "<gray>Nombre: <yellow>" + claim.getDisplayName() + "</yellow></gray>");
        MessageUtils.sendMessage(player, "<gray>Dueño: <yellow>" + ownerName + "</yellow></gray>");
        MessageUtils.sendMessage(player, "<gray>Tamaño: <yellow>" + blocks + " bloques</yellow></gray>");
        MessageUtils.sendMessage(player, "<gray>Esquina 1: <yellow>" + formatLocation(claim.getCorner1()) + "</yellow></gray>");
        MessageUtils.sendMessage(player, "<gray>Esquina 2: <yellow>" + formatLocation(claim.getCorner2()) + "</yellow></gray>");
        MessageUtils.sendMessage(player, "<gray>Jugadores confiados: <yellow>" + claim.getTrustedPlayers().size() + "</yellow></gray>");
    }

    public void listLands(Player player) {
        List<LandClaim> playerClaims = new ArrayList<>();
        for (LandClaim claim : landClaims.values()) {
            if (claim.getOwnerId().equals(player.getUniqueId())) {
                playerClaims.add(claim);
            }
        }

        if (playerClaims.isEmpty()) {
            MessageUtils.sendMessage(player, "<red>[Land] No tienes tierras reclamadas.</red>");
            return;
        }

        MessageUtils.sendMessage(player, "<gold>=== Tus Tierras ===</gold>");
        for (LandClaim claim : playerClaims) {
            int blocks = calculateBlocks(claim.getCorner1(), claim.getCorner2());
            MessageUtils.sendMessage(player, "<gray>- " + claim.getDisplayName() + " (" + blocks + " bloques)</gray>");
        }
    }

    // Métodos auxiliares
    public int calculateBlocks(Location loc1, Location loc2) {
        int x1 = loc1.getBlockX();
        int z1 = loc1.getBlockZ();
        int x2 = loc2.getBlockX();
        int z2 = loc2.getBlockZ();
        return Math.abs(x2 - x1) * Math.abs(z2 - z1);
    }

    private boolean isOverlapping(Location loc1, Location loc2) {
        for (LandClaim claim : landClaims.values()) {
            if (claim.isOverlapping(loc1, loc2)) {
                return true;
            }
        }
        return false;
    }

    public LandClaim getLandAt(Location location) {
        for (LandClaim claim : landClaims.values()) {
            if (claim.contains(location)) {
                return claim;
            }
        }
        return null;
    }

    private String formatLocation(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    public int getPlayerClaimBlocks(Player player) {
        return playerClaimBlocks.getOrDefault(player.getUniqueId(),
                plugin.getConfig().getInt("land.starting_blocks", 100));
    }

    public void setPlayerClaimBlocks(Player player, int amount) {
        playerClaimBlocks.put(player.getUniqueId(), amount);
        savePlayerBlocks(player);
    }

    private void savePlayerBlocks(Player player) {
        plugin.getConfigManager().getLandConfig().set("players." + player.getUniqueId() + ".blocks", getPlayerClaimBlocks(player));
        plugin.getConfigManager().saveLandConfig();
    }

    private void saveLandClaim(LandClaim claim) {
        String path = "claims." + claim.getId() + ".";
        plugin.getConfigManager().getLandConfig().set(path + "owner", claim.getOwnerId().toString());
        plugin.getConfigManager().getLandConfig().set(path + "corner1", serializeLocation(claim.getCorner1()));
        plugin.getConfigManager().getLandConfig().set(path + "corner2", serializeLocation(claim.getCorner2()));
        plugin.getConfigManager().getLandConfig().set(path + "trusted", claim.getTrustedPlayers().stream()
                .map(UUID::toString).toArray(String[]::new));
        
        // Guardar el nombre personalizado si existe
        if (claim.getCustomName() != null) {
            plugin.getConfigManager().getLandConfig().set(path + "custom_name", claim.getCustomName());
        } else {
            plugin.getConfigManager().getLandConfig().set(path + "custom_name", null);
        }
        
        plugin.getConfigManager().saveLandConfig();
    }

    public boolean removeLandClaim(String claimId) {
        // Eliminar la región de WorldGuard si existe
        LandClaim claim = landClaims.get(claimId);
        if (claim != null) {
            removeWorldGuardRegion(claimId, claim.getCorner1().getWorld());
            landClaims.remove(claimId);
            plugin.getConfigManager().getLandConfig().set("claims." + claimId, null);
            plugin.getConfigManager().saveLandConfig();
            return true;
        }
        return false;
    }

    private void loadAllLandClaims() {
        if (!plugin.getConfigManager().getLandConfig().contains("claims")) return;
        for (String claimId : plugin.getConfigManager().getLandConfig().getConfigurationSection("claims").getKeys(false)) {
            String path = "claims." + claimId + ".";
            UUID owner = UUID.fromString(plugin.getConfigManager().getLandConfig().getString(path + "owner"));
            Location corner1 = deserializeLocation(plugin.getConfigManager().getLandConfig().getString(path + "corner1"));
            Location corner2 = deserializeLocation(plugin.getConfigManager().getLandConfig().getString(path + "corner2"));
            
            // Cargar el nombre personalizado si existe
            String customName = plugin.getConfigManager().getLandConfig().getString(path + "custom_name", null);
            
            // Crear el claim con o sin nombre personalizado
            LandClaim claim;
            if (customName != null) {
                claim = new LandClaim(claimId, owner, corner1, corner2, customName);
            } else {
                claim = new LandClaim(claimId, owner, corner1, corner2);
            }
            
            // Cargar jugadores confiados
            if (plugin.getConfigManager().getLandConfig().contains(path + "trusted")) {
                for (String trustedUuid : plugin.getConfigManager().getLandConfig().getStringList(path + "trusted")) {
                    claim.addTrustedPlayer(UUID.fromString(trustedUuid));
                }
            }
            landClaims.put(claimId, claim);
        }
    }

    private void loadAllPlayerBlocks() {
        if (!plugin.getConfigManager().getLandConfig().contains("players")) return;
        for (String uuidStr : plugin.getConfigManager().getLandConfig().getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            int blocks = plugin.getConfigManager().getLandConfig().getInt("players." + uuidStr + ".blocks");
            playerClaimBlocks.put(uuid, blocks);
        }
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private Location deserializeLocation(String str) {
        String[] parts = str.split(",");
        World world = plugin.getServer().getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Location(world, x, y, z);
    }

    public Map<String, LandClaim> getLandClaims() {
        return landClaims;
    }

    public boolean canBuild(Player player, Location location) {
        LandClaim claim = getLandAt(location);
        if (claim == null) {
            return true; // Si no está en un claim, se permite construir
        }
        
        // Verificar si el jugador es el dueño o un jugador confiado
        boolean isOwner = claim.getOwnerId().equals(player.getUniqueId());
        boolean isTrusted = claim.getTrustedPlayers().contains(player.getUniqueId());
        
        // Permitir construir si es dueño, jugador confiado o tiene permisos de administrador
        return isOwner || isTrusted || player.hasPermission("mksurvival.land.admin");
    }

    public int calculateBlocks(LandClaim value) {
        return 0;
    }

    public static class LandClaim {
        private final String id;
        private final UUID ownerId;
        private final Location corner1;
        private final Location corner2;
        private final Set<UUID> trustedPlayers = new HashSet<>();
        private String customName; // Nombre personalizado del terreno

        public LandClaim(String id, UUID ownerId, Location corner1, Location corner2) {
            this.id = id;
            this.ownerId = ownerId;
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.customName = null; // Por defecto no tiene nombre personalizado
        }
        
        public LandClaim(String id, UUID ownerId, Location corner1, Location corner2, String customName) {
            this.id = id;
            this.ownerId = ownerId;
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.customName = customName;
        }

        public boolean contains(Location location) {
            return location.getX() >= Math.min(corner1.getX(), corner2.getX()) &&
                    location.getX() <= Math.max(corner1.getX(), corner2.getX()) &&
                    location.getY() >= Math.min(corner1.getY(), corner2.getY()) &&
                    location.getY() <= Math.max(corner1.getY(), corner2.getY()) &&
                    location.getZ() >= Math.min(corner1.getZ(), corner2.getZ()) &&
                    location.getZ() <= Math.max(corner1.getZ(), corner2.getZ());
        }

        public boolean isOverlapping(Location loc1, Location loc2) {
            return !(loc2.getX() < corner1.getX() || loc1.getX() > corner2.getX() ||
                    loc2.getZ() < corner1.getZ() || loc1.getZ() > corner2.getZ());
        }

        public void addTrustedPlayer(UUID uuid) {
            trustedPlayers.add(uuid);
        }

        public void removeTrustedPlayer(UUID uuid) {
            trustedPlayers.remove(uuid);
        }

        // Getters
        public String getId() { return id; }
        public UUID getOwnerId() { return ownerId; }
        public Location getCorner1() { return corner1; }
        public Location getCorner2() { return corner2; }
        public Set<UUID> getTrustedPlayers() { return trustedPlayers; }
        
        // Getter y setter para el nombre personalizado
        public String getCustomName() { return customName; }
        
        /**
         * Establece un nombre personalizado para el terreno
         * @param customName El nombre personalizado a establecer
         * @return true si el nombre es válido y se estableció correctamente, false en caso contrario
         */
        public boolean setCustomName(String customName) {
            // Validación del nombre personalizado
            if (customName == null) {
                this.customName = null;
                return true;
            }
            
            // Verificar longitud mínima y máxima
            if (customName.length() < 3 || customName.length() > 32) {
                return false;
            }
            
            // Verificar caracteres permitidos (letras, números, espacios y algunos símbolos)
            if (!customName.matches("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s\\-_]+$")) {
                return false;
            }
            
            this.customName = customName;
            return true;
        }
        
        /**
         * Obtiene el nombre para mostrar del terreno
         * @return El nombre personalizado si existe, o el ID formateado si no
         */
        public String getDisplayName() {
            return customName != null ? customName : "Terreno #" + id.split("_")[1];
        }
    }
}