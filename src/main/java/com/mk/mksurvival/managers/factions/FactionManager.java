package com.mk.mksurvival.managers.factions;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import java.util.*;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import java.util.UUID;

public class FactionManager {
    private final MKSurvival plugin;
    private final Map<String, Faction> factions = new HashMap<>();
    private final Map<UUID, String> playerFactions = new HashMap<>();
    private final Map<String, Set<UUID>> factionInvites = new HashMap<>();
    private final Map<String, Set<String>> factionAllies = new HashMap<>();
    private final Map<String, Set<String>> factionEnemies = new HashMap<>();
    private final Map<Chunk, String> factionClaims = new HashMap<>();

    public FactionManager(MKSurvival plugin) {
        this.plugin = plugin;
        loadFactions();
    }

    private void loadFactions() {
        // Cargar facciones desde la configuración
        if (plugin.getConfigManager().getConfig().contains("factions")) {
            for (String factionName : plugin
                    .getConfigManager()
                    .getConfig()
                    .getConfigurationSection("factions")
                    .getKeys(false)) {
                String path = "factions." + factionName + ".";

                // Leer el UUID del líder con validación
                String leaderUuidStr = plugin
                        .getConfigManager()
                        .getConfig()
                        .getString(path + "leader");

                if (leaderUuidStr == null || leaderUuidStr.isEmpty()) {
                    plugin.getLogger().warning("Leader UUID is missing for faction: " + factionName);
                    continue; // Saltar esta facción y continuar con la siguiente
                }

                UUID leader;
                try {
                    leader = UUID.fromString(leaderUuidStr);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid leader UUID format for faction " + factionName + ": " + leaderUuidStr);
                    continue; // Saltar esta facción y continuar con la siguiente
                }

                String description = plugin
                        .getConfigManager()
                        .getConfig()
                        .getString(path + "description");
                double balance = plugin
                        .getConfigManager()
                        .getConfig()
                        .getDouble(path + "balance");
                Faction faction = new Faction(
                        factionName,
                        leader,
                        description,
                        balance
                );

                // Cargar miembros con validación de UUID
                if (plugin
                        .getConfigManager()
                        .getConfig()
                        .contains(path + "members")) {
                    for (String memberUuidStr : plugin
                            .getConfigManager()
                            .getConfig()
                            .getConfigurationSection(path + "members")
                            .getKeys(false)) {

                        if (memberUuidStr == null || memberUuidStr.isEmpty()) {
                            plugin.getLogger().warning("Member UUID is missing for faction: " + factionName);
                            continue;
                        }

                        UUID memberUuid;
                        try {
                            memberUuid = UUID.fromString(memberUuidStr);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid member UUID format for faction " + factionName + ": " + memberUuidStr);
                            continue;
                        }

                        String role = plugin
                                .getConfigManager()
                                .getConfig()
                                .getString(path + "members." + memberUuidStr);
                        faction.addMember(memberUuid, role);
                    }
                }

                // Cargar aliados
                if (plugin
                        .getConfigManager()
                        .getConfig()
                        .contains(path + "allies")) {
                    for (String ally : plugin
                            .getConfigManager()
                            .getConfig()
                            .getStringList(path + "allies")) {
                        factionAlly(factionName, ally, false);
                    }
                }

                // Cargar enemigos
                if (plugin
                        .getConfigManager()
                        .getConfig()
                        .contains(path + "enemies")) {
                    for (String enemy : plugin
                            .getConfigManager()
                            .getConfig()
                            .getStringList(path + "enemies")) {
                        factionEnemy(factionName, enemy, false);
                    }
                }

                // Cargar guerras
                if (plugin
                        .getConfigManager()
                        .getConfig()
                        .contains(path + "wars")) {
                    for (String war : plugin
                            .getConfigManager()
                            .getConfig()
                            .getStringList(path + "wars")) {
                        faction.addWar(war);
                    }
                }

                // Cargar configuraciones adicionales
                if (plugin
                        .getConfigManager()
                        .getConfig()
                        .contains(path + "isPrivate")) {
                    boolean isPrivate = plugin
                            .getConfigManager()
                            .getConfig()
                            .getBoolean(path + "isPrivate");
                    faction.setPrivate(isPrivate);
                }
                if (plugin
                        .getConfigManager()
                        .getConfig()
                        .contains(path + "power")) {
                    String power = plugin
                            .getConfigManager()
                            .getConfig()
                            .getString(path + "power");
                    faction.setPower(power);
                }

                factions.put(factionName, faction);
            }
        }

        // Cargar jugadores a facciones con validación de UUID
        if (plugin.getConfigManager().getConfig().contains("faction_players")) {
            for (String uuidStr : plugin
                    .getConfigManager()
                    .getConfig()
                    .getConfigurationSection("faction_players")
                    .getKeys(false)) {

                if (uuidStr == null || uuidStr.isEmpty()) {
                    plugin.getLogger().warning("Player UUID is missing in faction_players");
                    continue;
                }

                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid player UUID format in faction_players: " + uuidStr);
                    continue;
                }

                String factionName = plugin
                        .getConfigManager()
                        .getConfig()
                        .getString("faction_players." + uuidStr);
                playerFactions.put(uuid, factionName);
            }
        }
    }

    public void saveFactions() {
        // Guardar facciones en la configuración
        plugin.getConfigManager().getConfig().set("factions", null);
        plugin.getConfigManager().getConfig().set("faction_players", null);
        for (Faction faction : factions.values()) {
            String path = "factions." + faction.getName() + ".";
            plugin
                    .getConfigManager()
                    .getConfig()
                    .set(path + "leader", faction.getLeader().toString());
            plugin
                    .getConfigManager()
                    .getConfig()
                    .set(path + "description", faction.getDescription());
            plugin
                    .getConfigManager()
                    .getConfig()
                    .set(path + "balance", faction.getBalance());
            // Guardar miembros
            for (Map.Entry<UUID, String> member : faction
                    .getMembers()
                    .entrySet()) {
                plugin
                        .getConfigManager()
                        .getConfig()
                        .set(
                                path + "members." + member.getKey().toString(),
                                member.getValue()
                        );
            }
            // Guardar aliados
            if (factionAllies.containsKey(faction.getName())) {
                plugin
                        .getConfigManager()
                        .getConfig()
                        .set(
                                path + "allies",
                                new ArrayList<>(factionAllies.get(faction.getName()))
                        );
            }
            // Guardar enemigos
            if (factionEnemies.containsKey(faction.getName())) {
                plugin
                        .getConfigManager()
                        .getConfig()
                        .set(
                                path + "enemies",
                                new ArrayList<>(factionEnemies.get(faction.getName()))
                        );
            }
            // Guardar guerras
            if (faction.getWars() != null && !faction.getWars().isEmpty()) {
                plugin
                        .getConfigManager()
                        .getConfig()
                        .set(
                                path + "wars",
                                new ArrayList<>(faction.getWars())
                        );
            }
            // Guardar configuraciones adicionales
            plugin
                    .getConfigManager()
                    .getConfig()
                    .set(path + "isPrivate", faction.isPrivate());
            plugin
                    .getConfigManager()
                    .getConfig()
                    .set(path + "power", faction.getPower());
        }
        // Guardar jugadores a facciones
        for (Map.Entry<UUID, String> entry : playerFactions.entrySet()) {
            plugin
                    .getConfigManager()
                    .getConfig()
                    .set(
                            "faction_players." + entry.getKey().toString(),
                            entry.getValue()
                    );
        }
        plugin.getConfigManager().saveConfig();
    }

    public boolean createFaction(
            UUID player,
            String name,
            String description
    ) {
        if (factions.containsKey(name)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>Ya existe una facción con ese nombre.");
            return false;
        }
        if (playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>Ya eres miembro de una facción.");
            return false;
        }
        Faction faction = new Faction(
                name,
                player,
                description,
                0
        );
        factions.put(name, faction);
        playerFactions.put(player, name);
        Player p = plugin.getServer().getPlayer(player);
        if (p != null) MessageUtils.sendMessage(p, "<green>Has creado la facción <yellow>" + name + "<green>.");
        saveFactions();
        return true;
    }

    public boolean disbandFaction(UUID player) {
        if (!playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(player);
        Faction faction = factions.get(factionName);
        if (!faction.getLeader().equals(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>Solo el líder puede disolver la facción.");
            return false;
        }
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<red>La facción <yellow>" + factionName + " <red> ha sido disuelta."
                );
                playerFactions.remove(memberUuid);
            }
        }
        // Eliminar aliados y enemigos
        factionAllies.remove(factionName);
        factionEnemies.remove(factionName);
        // Eliminar reclamos de tierra
        factionClaims
                .entrySet()
                .removeIf(entry -> entry.getValue().equals(factionName));
        factions.remove(factionName);
        Player p = plugin.getServer().getPlayer(player);
        if (p != null) MessageUtils.sendMessage(p,
                "<green>Has disuelto la facción <yellow>" + factionName + "<green>."
        );
        saveFactions();
        return true;
    }

    public boolean invitePlayer(UUID leader, UUID invited) {
        if (!playerFactions.containsKey(leader)) {
            Player p = plugin.getServer().getPlayer(leader);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(leader);
        Faction faction = factions.get(factionName);
        if (
                !faction.getLeader().equals(leader) &&
                        !faction.getMembers().get(leader).equals("OFFICER")
        ) {
            Player p = plugin.getServer().getPlayer(leader);
            if (p != null) MessageUtils.sendMessage(p,
                    "<red>Solo el líder y oficiales pueden invitar jugadores."
            );
            return false;
        }
        if (playerFactions.containsKey(invited)) {
            Player p = plugin.getServer().getPlayer(leader);
            if (p != null) MessageUtils.sendMessage(p, "<red>Ese jugador ya es miembro de una facción.");
            return false;
        }
        if (!factionInvites.containsKey(factionName)) {
            factionInvites.put(factionName, new HashSet<>());
        }
        factionInvites.get(factionName).add(invited);
        Player leaderP = plugin.getServer().getPlayer(leader);
        Player invitedP = plugin.getServer().getPlayer(invited);
        if (leaderP != null) MessageUtils.sendMessage(leaderP,
                "<green>Has invitado a <yellow>" + invitedP.getName() + " <green>a la facción."
        );
        if (invitedP != null) MessageUtils.sendMessage(invitedP,
                "<green>Has sido invitado a unirte a la facción <yellow>" +
                        factionName +
                        "<green>. Usa <white>/faccion unir " +
                        factionName +
                        " <green>para aceptar."
        );
        return true;
    }

    public boolean joinFaction(UUID player, String factionName) {
        if (playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>Ya eres miembro de una facción.");
            return false;
        }
        if (!factions.containsKey(factionName)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>Esa facción no existe.");
            return false;
        }
        if (
                !factionInvites.containsKey(factionName) ||
                        !factionInvites.get(factionName).contains(player)
        ) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>No has sido invitado a esa facción.");
            return false;
        }
        Faction faction = factions.get(factionName);
        faction.addMember(player, "MEMBER");
        playerFactions.put(player, factionName);
        factionInvites.get(factionName).remove(player);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<green><yellow>" + plugin.getServer().getPlayer(player).getName() + " <green>se ha unido a la facción."
                );
            }
        }
        Player p = plugin.getServer().getPlayer(player);
        if (p != null) MessageUtils.sendMessage(p,
                "<green>Te has unido a la facción <yellow>" + factionName + "<green>."
        );
        saveFactions();
        return true;
    }

    public boolean leaveFaction(UUID player) {
        if (!playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(player);
        Faction faction = factions.get(factionName);
        if (faction.getLeader().equals(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p,
                    "<red>El líder no puede abandonar la facción. Debe disolverla o transferir el liderazgo."
            );
            return false;
        }
        faction.removeMember(player);
        playerFactions.remove(player);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<yellow>" + plugin.getServer().getPlayer(player).getName() + " <red>ha abandonado la facción."
                );
            }
        }
        Player p = plugin.getServer().getPlayer(player);
        if (p != null) MessageUtils.sendMessage(p,
                "<green>Has abandonado la facción <yellow>" + factionName + "<green>."
        );
        saveFactions();
        return true;
    }

    public boolean kickPlayer(UUID kicker, UUID kicked) {
        if (!playerFactions.containsKey(kicker)) {
            Player p = plugin.getServer().getPlayer(kicker);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(kicker);
        Faction faction = factions.get(factionName);
        if (
                !faction.getLeader().equals(kicker) &&
                        !faction.getMembers().get(kicker).equals("OFFICER")
        ) {
            Player p = plugin.getServer().getPlayer(kicker);
            if (p != null) MessageUtils.sendMessage(p,
                    "<red>Solo el líder y oficiales pueden expulsar jugadores."
            );
            return false;
        }
        if (
                !playerFactions.containsKey(kicked) ||
                        !playerFactions.get(kicked).equals(factionName)
        ) {
            Player p = plugin.getServer().getPlayer(kicker);
            if (p != null) MessageUtils.sendMessage(p, "<red>Ese jugador no es miembro de tu facción.");
            return false;
        }
        if (faction.getLeader().equals(kicked)) {
            Player p = plugin.getServer().getPlayer(kicker);
            if (p != null) MessageUtils.sendMessage(p, "<red>No puedes expulsar al líder.");
            return false;
        }
        faction.removeMember(kicked);
        playerFactions.remove(kicked);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<yellow>" +
                                plugin.getServer().getPlayer(kicked).getName() +
                                " <red>ha sido expulsado de la facción."
                );
            }
        }
        Player p = plugin.getServer().getPlayer(kicked);
        if (p != null) MessageUtils.sendMessage(p,
                "<red>Has sido expulsado de la facción <yellow>" + factionName + "<red>."
        );
        saveFactions();
        return true;
    }

    public boolean promotePlayer(
            UUID promoter,
            UUID promoted,
            String role
    ) {
        if (!playerFactions.containsKey(promoter)) {
            Player p = plugin.getServer().getPlayer(promoter);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(promoter);
        Faction faction = factions.get(factionName);
        if (!faction.getLeader().equals(promoter)) {
            Player p = plugin.getServer().getPlayer(promoter);
            if (p != null) MessageUtils.sendMessage(p,
                    "<red>Solo el líder puede promocionar jugadores."
            );
            return false;
        }
        if (
                !playerFactions.containsKey(promoted) ||
                        !playerFactions.get(promoted).equals(factionName)
        ) {
            Player p = plugin.getServer().getPlayer(promoter);
            if (p != null) MessageUtils.sendMessage(p, "<red>Ese jugador no es miembro de tu facción.");
            return false;
        }
        if (!role.equals("OFFICER") && !role.equals("MEMBER")) {
            Player p = plugin.getServer().getPlayer(promoter);
            if (p != null) MessageUtils.sendMessage(p, "<red>Rol inválido. Usa OFFICER o MEMBER.");
            return false;
        }
        faction.getMembers().put(promoted, role);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<yellow>" +
                                plugin.getServer().getPlayer(promoted).getName() +
                                " <green>ha sido promocionado a " +
                                role +
                                "."
                );
            }
        }
        Player p = plugin.getServer().getPlayer(promoted);
        if (p != null) MessageUtils.sendMessage(p,
                "<green>Has sido promocionado a " +
                        role +
                        " en la facción <yellow>" +
                        factionName +
                        "<green>."
        );
        saveFactions();
        return true;
    }

    public boolean factionAlly(String faction1, String faction2, boolean save) {
        if (
                !factions.containsKey(faction1) || !factions.containsKey(faction2)
        ) {
            return false;
        }
        if (faction1.equals(faction2)) {
            return false;
        }
        if (!factionAllies.containsKey(faction1)) {
            factionAllies.put(faction1, new HashSet<>());
        }
        if (!factionAllies.containsKey(faction2)) {
            factionAllies.put(faction2, new HashSet<>());
        }
        factionAllies.get(faction1).add(faction2);
        factionAllies.get(faction2).add(faction1);
        // Eliminar de enemigos si existía
        if (
                factionEnemies.containsKey(faction1) &&
                        factionEnemies.get(faction1).contains(faction2)
        ) {
            factionEnemies.get(faction1).remove(faction2);
        }
        if (
                factionEnemies.containsKey(faction2) &&
                        factionEnemies.get(faction2).contains(faction1)
        ) {
            factionEnemies.get(faction2).remove(faction1);
        }
        if (save) {
            saveFactions();
        }
        // Notificar a los miembros
        for (UUID memberUuid : factions.get(faction1).getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<green>Tu facción ahora es aliada de <yellow>" + faction2 + "<green>."
                );
            }
        }
        for (UUID memberUuid : factions.get(faction2).getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<green>Tu facción ahora es aliada de <yellow>" + faction1 + "<green>."
                );
            }
        }
        return true;
    }

    public boolean factionEnemy(
            String faction1,
            String faction2,
            boolean save
    ) {
        if (
                !factions.containsKey(faction1) || !factions.containsKey(faction2)
        ) {
            return false;
        }
        if (faction1.equals(faction2)) {
            return false;
        }
        if (!factionEnemies.containsKey(faction1)) {
            factionEnemies.put(faction1, new HashSet<>());
        }
        if (!factionEnemies.containsKey(faction2)) {
            factionEnemies.put(faction2, new HashSet<>());
        }
        factionEnemies.get(faction1).add(faction2);
        factionEnemies.get(faction2).add(faction1);
        // Eliminar de aliados si existía
        if (
                factionAllies.containsKey(faction1) &&
                        factionAllies.get(faction1).contains(faction2)
        ) {
            factionAllies.get(faction1).remove(faction2);
        }
        if (
                factionAllies.containsKey(faction2) &&
                        factionAllies.get(faction2).contains(faction1)
        ) {
            factionAllies.get(faction2).remove(faction1);
        }
        if (save) {
            saveFactions();
        }
        // Notificar a los miembros
        for (UUID memberUuid : factions.get(faction1).getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<red>Tu facción ahora es enemiga de <yellow>" + faction2 + "<red>."
                );
            }
        }
        for (UUID memberUuid : factions.get(faction2).getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<red>Tu facción ahora es enemiga de <yellow>" + faction1 + "<red>."
                );
            }
        }
        return true;
    }

    public boolean claimLand(UUID player) {
        if (!playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(player);
        Faction faction = factions.get(factionName);
        if (
                !faction.getLeader().equals(player) &&
                        !faction.getMembers().get(player).equals("OFFICER")
        ) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>Solo el líder y oficiales pueden reclamar tierras.");
            return false;
        }
        Player p = plugin.getServer().getPlayer(player);
        if (p == null) return false;
        Chunk chunk = p.getLocation().getChunk();
        if (factionClaims.containsKey(chunk)) {
            String owner = factionClaims.get(chunk);
            if (owner.equals(factionName)) {
                MessageUtils.sendMessage(p, "<red>Esta tierra ya pertenece a tu facción.");
            } else {
                MessageUtils.sendMessage(p,
                        "<red>Esta tierra pertenece a la facción <yellow>" + owner + "<red>."
                );
            }
            return false;
        }
        // Verificar límite de tierras
        long claimedChunks = factionClaims
                .values()
                .stream()
                .filter(f -> f.equals(factionName))
                .count();
        int maxClaims = faction.getMembers().size() * 5; // 5 chunks por miembro
        if (claimedChunks >= maxClaims) {
            MessageUtils.sendMessage(p,
                    "<red>Tu facción ha alcanzado el límite de tierras que puede reclamar."
            );
            return false;
        }
        // Verificar si hay tierras aliadas cerca
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                Chunk nearbyChunk = p
                        .getWorld()
                        .getChunkAt(chunk.getX() + x, chunk.getZ() + z);
                if (factionClaims.containsKey(nearbyChunk)) {
                    String nearbyFaction = factionClaims.get(nearbyChunk);
                    if (
                            factionEnemies.containsKey(factionName) &&
                                    factionEnemies.get(factionName).contains(nearbyFaction)
                    ) {
                        MessageUtils.sendMessage(p,
                                "<red>No puedes reclamar tierra cerca de territorios enemigos."
                        );
                        return false;
                    }
                }
            }
        }
        factionClaims.put(chunk, factionName);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<green>Se ha reclamado un nuevo territorio en <yellow>" +
                                chunk.getX() +
                                ", " +
                                chunk.getZ() +
                                "<green>."
                );
            }
        }
        MessageUtils.sendMessage(p, "<green>Has reclamado este territorio para tu facción.");
        saveFactions();
        return true;
    }

    public boolean unclaimLand(UUID player) {
        if (!playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(player);
        Faction faction = factions.get(factionName);
        if (
                !faction.getLeader().equals(player) &&
                        !faction.getMembers().get(player).equals("OFFICER")
        ) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>Solo el líder y oficiales pueden reclamar tierras.");
            return false;
        }
        Player p = plugin.getServer().getPlayer(player);
        if (p == null) return false;
        Chunk chunk = p.getLocation().getChunk();
        if (
                !factionClaims.containsKey(chunk) ||
                        !factionClaims.get(chunk).equals(factionName)
        ) {
            MessageUtils.sendMessage(p, "<red>Esta tierra no pertenece a tu facción.");
            return false;
        }
        factionClaims.remove(chunk);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<red>Se ha abandonado el territorio en <yellow>" +
                                chunk.getX() +
                                ", " +
                                chunk.getZ() +
                                "<red>."
                );
            }
        }
        MessageUtils.sendMessage(p, "<green>Has abandonado este territorio.");
        saveFactions();
        return true;
    }

    public boolean depositMoney(UUID player, double amount) {
        if (!playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        Player p = plugin.getServer().getPlayer(player);
        if (p == null) return false;
        if (plugin.getEconomyManager().getBalance(p) < amount) {
            MessageUtils.sendMessage(p, "<red>No tienes suficiente dinero.");
            return false;
        }
        String factionName = playerFactions.get(player);
        Faction faction = factions.get(factionName);
        plugin.getEconomyManager().withdrawBalance(p, amount);
        faction.deposit(amount);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<yellow>" +
                                p.getName() +
                                " <green>ha depositado <yellow>" +
                                plugin.getEconomyManager().formatCurrency(amount) +
                                " <green>en el banco de la facción."
                );
            }
        }
        MessageUtils.sendMessage(p,
                "<green>Has depositado <yellow>" +
                        plugin.getEconomyManager().formatCurrency(amount) +
                        " <green>en el banco de la facción."
        );
        saveFactions();
        return true;
    }

    public boolean withdrawMoney(UUID player, double amount) {
        if (!playerFactions.containsKey(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>No eres miembro de ninguna facción.");
            return false;
        }
        String factionName = playerFactions.get(player);
        Faction faction = factions.get(factionName);
        if (!faction.getLeader().equals(player)) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p,
                    "<red>Solo el líder puede retirar dinero del banco de la facción."
            );
            return false;
        }
        if (faction.getBalance() < amount) {
            Player p = plugin.getServer().getPlayer(player);
            if (p != null) MessageUtils.sendMessage(p, "<red>La facción no tiene suficiente dinero.");
            return false;
        }
        faction.withdraw(amount);
        Player p = plugin.getServer().getPlayer(player);
        if (p == null) return false;
        plugin.getEconomyManager().depositBalance(p, amount);
        // Notificar a todos los miembros
        for (UUID memberUuid : faction.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                MessageUtils.sendMessage(member,
                        "<yellow>" +
                                p.getName() +
                                " <red>ha retirado <yellow>" +
                                plugin.getEconomyManager().formatCurrency(amount) +
                                " <red>del banco de la facción."
                );
            }
        }
        MessageUtils.sendMessage(p,
                "<green>Has retirado <yellow>" +
                        plugin.getEconomyManager().formatCurrency(amount) +
                        " <green>del banco de la facción."
        );
        saveFactions();
        return true;
    }

    public void showFactionInfo(Player player, String factionName) {
        if (!factions.containsKey(factionName)) {
            MessageUtils.sendMessage(player, "<red>Esa facción no existe.");
            return;
        }
        Faction faction = factions.get(factionName);
        MessageUtils.sendMessage(player,
                "<gold>--- Información de la Facción " + factionName + " ---"
        );
        MessageUtils.sendMessage(player,
                "<gray>Líder: <yellow>" +
                        plugin
                                .getServer()
                                .getOfflinePlayer(faction.getLeader())
                                .getName()
        );
        MessageUtils.sendMessage(player, "<gray>Descripción: <white>" + faction.getDescription());
        MessageUtils.sendMessage(player, "<gray>Miembros: <yellow>" + faction.getMembers().size());
        MessageUtils.sendMessage(player,
                "<gray>Balance: <yellow>" +
                        plugin.getEconomyManager().formatCurrency(faction.getBalance())
        );
        MessageUtils.sendMessage(player,
                "<gray>Territorios: <yellow>" +
                        factionClaims
                                .values()
                                .stream()
                                .filter(f -> f.equals(factionName))
                                .count()
        );
        if (
                factionAllies.containsKey(factionName) &&
                        !factionAllies.get(factionName).isEmpty()
        ) {
            MessageUtils.sendMessage(player,
                    "<gray>Aliados: <green>" +
                            String.join(", ", factionAllies.get(factionName))
            );
        }
        if (
                factionEnemies.containsKey(factionName) &&
                        !factionEnemies.get(factionName).isEmpty()
        ) {
            MessageUtils.sendMessage(player,
                    "<gray>Enemigos: <red>" +
                            String.join(", ", factionEnemies.get(factionName))
            );
        }
        MessageUtils.sendMessage(player, "<gray>Miembros:");
        for (Map.Entry<UUID, String> member : faction.getMembers().entrySet()) {
            String roleName = member.getValue();
            String roleColor = roleName.equals("LEADER")
                    ? "<gold>"
                    : roleName.equals("OFFICER")
                    ? "<aqua>"
                    : "<gray>";
            MessageUtils.sendMessage(player,
                    "  " +
                            roleColor +
                            "- " +
                            plugin
                                    .getServer()
                                    .getOfflinePlayer(member.getKey())
                                    .getName() +
                            " <dark_gray>(" +
                            roleName +
                            ")"
            );
        }
    }

    public void showFactionList(Player player) {
        MessageUtils.sendMessage(player, "<gold>--- Lista de Facciones ---");
        for (Faction faction : factions.values()) {
            MessageUtils.sendMessage(player,
                    "<yellow>" +
                            faction.getName() +
                            " <gray>- " +
                            faction.getMembers().size() +
                            " miembros, " +
                            plugin
                                    .getEconomyManager()
                                    .formatCurrency(faction.getBalance())
            );
        }
    }

    // Getters
    public Map<String, Faction> getFactions() {
        return factions;
    }

    public Map<UUID, String> getPlayerFactions() {
        return playerFactions;
    }

    public Map<Chunk, String> getFactionClaims() {
        return factionClaims;
    }

    public Map<String, Set<String>> getFactionAllies() {
        return factionAllies;
    }

    public Map<String, Set<String>> getFactionEnemies() {
        return factionEnemies;
    }

    public boolean isPlayerInFaction(UUID player) {
        return playerFactions.containsKey(player);
    }

    public String getPlayerFaction(UUID player) {
        return playerFactions.get(player);
    }

    public boolean areAllies(String faction1, String faction2) {
        return (
                factionAllies.containsKey(faction1) &&
                        factionAllies.get(faction1).contains(faction2)
        );
    }

    public boolean areEnemies(String faction1, String faction2) {
        return (
                factionEnemies.containsKey(faction1) &&
                        factionEnemies.get(faction1).contains(faction2)
        );
    }

    public String getChunkOwner(Chunk chunk) {
        return factionClaims.get(chunk);
    }

    // Wars management methods
    public boolean declareWar(String faction1, String faction2) {
        if (!factions.containsKey(faction1) || !factions.containsKey(faction2)) {
            return false;
        }
        if (faction1.equals(faction2)) {
            return false;
        }

        Faction f1 = factions.get(faction1);
        Faction f2 = factions.get(faction2);

        // Add war relationship
        f1.addWar(faction2);
        f2.addWar(faction1);

        // Remove any existing ally relationship
        if (factionAllies.containsKey(faction1) && factionAllies.get(faction1).contains(faction2)) {
            factionAllies.get(faction1).remove(faction2);
        }
        if (factionAllies.containsKey(faction2) && factionAllies.get(faction2).contains(faction1)) {
            factionAllies.get(faction2).remove(faction1);
        }

        // Add to enemies if not already there
        factionEnemy(faction1, faction2, false);

        saveFactions();
        return true;
    }

    public boolean endWar(String faction1, String faction2) {
        if (!factions.containsKey(faction1) || !factions.containsKey(faction2)) {
            return false;
        }

        Faction f1 = factions.get(faction1);
        Faction f2 = factions.get(faction2);

        // Remove war relationship
        f1.removeWar(faction2);
        f2.removeWar(faction1);

        saveFactions();
        return true;
    }

    public Set<String> getFactionsAtWarWith(String factionName) {
        if (!factions.containsKey(factionName)) {
            return new HashSet<>();
        }
        Faction faction = factions.get(factionName);
        return faction.getWars() != null ? new HashSet<>(faction.getWars()) : new HashSet<>();
    }

    public Object getFactionPower(String factionName) {
        return null;
    }

    public static class Faction {
        private final String name;
        private final UUID leader;
        private String description;
        private double balance;
        private final Map<UUID, String> members = new HashMap<>();
        private Set<String> allies = new HashSet<>();
        private Set<String> enemies = new HashSet<>();
        private Set<String> territories = new HashSet<>();
        private boolean isPrivate = false;
        private String power = "100";

        public Faction(
                String name,
                UUID leader,
                String description,
                double balance
        ) {
            this.name = name;
            this.leader = leader;
            this.description = description;
            this.balance = balance;
            members.put(leader, "LEADER");
        }

        public void addMember(UUID uuid, String role) {
            members.put(uuid, role);
        }

        public void removeMember(UUID uuid) {
            members.remove(uuid);
        }

        public void deposit(double amount) {
            balance += amount;
        }

        public void withdraw(double amount) {
            balance -= amount;
        }

        // Getters
        public String getName() {
            return name;
        }

        public UUID getLeader() {
            return leader;
        }

        public String getDescription() {
            return description;
        }

        public double getBalance() {
            return balance;
        }

        public Map<UUID, String> getMembers() {
            return members;
        }

        public Collection<String> getAllies() {
            return allies;
        }

        public Set<String> getEnemies() {
            return enemies;
        }

        public Set<String> getTerritories() {
            return territories;
        }

        public boolean isPrivate() {
            return isPrivate;
        }

        public void setPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
        }

        public String getPower() {
            return power;
        }

        public void setPower(String power) {
            this.power = power;
        }

        // Wars management
        private Set<String> wars = new HashSet<>();

        public Set<String> getWars() {
            return wars;
        }

        public void setWars(Set<String> wars) {
            this.wars = wars;
        }

        public void addWar(String factionName) {
            if (wars == null) {
                wars = new HashSet<>();
            }
            wars.add(factionName);
        }

        public void removeWar(String factionName) {
            if (wars != null) {
                wars.remove(factionName);
            }
        }

        public boolean isAtWarWith(String factionName) {
            return wars != null && wars.contains(factionName);
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setEnemies(Set<String> enemies) {
            this.enemies = enemies;
        }

        public void setAllies(Set<String> allies) {
            this.allies = allies;
        }

        public void setTerritories(Set<String> territories) {
            this.territories = territories;
        }
    }
}