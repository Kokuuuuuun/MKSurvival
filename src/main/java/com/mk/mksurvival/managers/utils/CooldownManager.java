package com.mk.mksurvival.managers.utils;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final MKSurvival plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(MKSurvival plugin) {
        this.plugin = plugin;
    }

    public void setCooldown(Player player, String key, long seconds) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            cooldowns.put(uuid, new HashMap<>());
        }

        cooldowns.get(uuid).put(key, System.currentTimeMillis() + (seconds * 1000));

        // Notificar al jugador cuando termine el cooldown
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    MessageUtils.sendMessage(player, "<green>[Cooldown] Tu cooldown para " + key + " ha terminado.");
                }
            }
        }.runTaskLater(plugin, seconds * 20L);
    }

    public boolean hasCooldown(Player player, String key) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }

        Long endTime = cooldowns.get(uuid).get(key);
        if (endTime == null) {
            return false;
        }

        return System.currentTimeMillis() < endTime;
    }

    public long getRemainingCooldown(Player player, String key) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }

        Long endTime = cooldowns.get(uuid).get(key);
        if (endTime == null) {
            return 0;
        }

        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining / 1000); // Convertir a segundos
    }

    public void removeCooldown(Player player, String key) {
        UUID uuid = player.getUniqueId();
        if (cooldowns.containsKey(uuid)) {
            cooldowns.get(uuid).remove(key);
        }
    }

    public void clearAllCooldowns(Player player) {
        UUID uuid = player.getUniqueId();
        cooldowns.remove(uuid);
    }

    public static class f {
    }
}