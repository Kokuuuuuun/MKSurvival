package com.mk.mksurvival.managers.particles;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleManager {
    private final MKSurvival plugin;
    private final Map<UUID, Long> lastParticleEffect = new HashMap<>();

    public ParticleManager(MKSurvival plugin) {
        this.plugin = plugin;
    }

    public void spawnLevelUpEffect(Player player) {
        Location location = player.getLocation().add(0, 1, 0);

        // Crear un círculo de partículas alrededor del jugador
        for (int i = 0; i < 20; i++) {
            double angle = 2 * Math.PI * i / 20;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;

            location.getWorld().spawnParticle(Particle.ITEM,
                    location.clone().add(x, 0, z), 1);
        }

        // Crear una columna de partículas hacia arriba
        for (int y = 0; y < 3; y++) {
            location.getWorld().spawnParticle(Particle.END_ROD,
                    location.clone().add(0, y * 0.5, 0), 1);
        }

        // Reproducir sonido
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    public void spawnQuestCompleteEffect(Player player) {
        Location location = player.getLocation().add(0, 1, 0);

        // Crear una espiral de partículas
        for (int i = 0; i < 30; i++) {
            double angle = 2 * Math.PI * i / 10;
            double y = i * 0.1;
            double radius = 1.5 - (y * 0.3);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            location.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                    location.clone().add(x, y, z), 1);
        }

        // Reproducir sonido
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public void spawnRewardEffect(Player player) {
        Location location = player.getLocation().add(0, 1, 0);

        // Crear una explosión de partículas
        for (int i = 0; i < 50; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.random() * Math.PI;
            double r = Math.random() * 2;

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.sin(phi) * Math.sin(theta);
            double z = r * Math.cos(phi);

            location.getWorld().spawnParticle(Particle.GUST,
                    location.clone().add(x, y, z), 1);
        }

        // Reproducir sonido
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }

    public void spawnDungeonEntranceEffect(Location location) {
        // Crear un efecto de portal en la entrada de la dungeon
        for (int i = 0; i < 50; i++) {
            double x = (Math.random() - 0.5) * 3;
            double y = Math.random() * 3;
            double z = (Math.random() - 0.5) * 3;

            location.getWorld().spawnParticle(Particle.PORTAL,
                    location.clone().add(x, y, z), 1);
        }
    }

    public void spawnMineEntranceEffect(Location location) {
        // Crear un efecto de mina en la entrada
        for (int i = 0; i < 30; i++) {
            double x = (Math.random() - 0.5) * 2;
            double y = Math.random() * 2;
            double z = (Math.random() - 0.5) * 2;

            location.getWorld().spawnParticle(Particle.CRIT,
                    location.clone().add(x, y, z), 1);
        }
    }

    public void spawnSkillProgressEffect(Player player, SkillType skillType) {
        UUID uuid = player.getUniqueId();

        // Evitar spam de efectos (mínimo 2 segundos entre efectos)
        if (lastParticleEffect.containsKey(uuid) &&
                System.currentTimeMillis() - lastParticleEffect.get(uuid) < 2000) {
            return;
        }

        lastParticleEffect.put(uuid, System.currentTimeMillis());

        Location location = player.getLocation().add(0, 1, 0);

        // Seleccionar partícula según el tipo de habilidad
        Particle particle = Particle.CRIT;
        switch (skillType) {
            case MINING:
                particle = Particle.CRIT;
                break;
            case COMBAT:
                particle = Particle.SWEEP_ATTACK;
                break;
            case FISHING:
                particle = Particle.SPLASH;
                break;
            case FARMING:
                particle = Particle.ITEM;
                break;
            case WOODCUTTING:
                particle = Particle.BLOCK;
                break;
            case FORAGING:
                particle = Particle.SNEEZE;
                break;
            case ALCHEMY:
                particle = Particle.WITCH;
                break;
            case ENCHANTING:
                particle = Particle.ENCHANT;
                break;
        }

        // Crear un pequeño efecto alrededor del jugador
        for (int i = 0; i < 10; i++) {
            double angle = 2 * Math.PI * i / 10;
            double x = Math.cos(angle) * 0.8;
            double z = Math.sin(angle) * 0.8;

            location.getWorld().spawnParticle(particle,
                    location.clone().add(x, 0, z), 1);
        }
    }

    public void spawnDeathEffect(Location location, int level) {
        // Crear efectos diferentes según el nivel del mob
        if (level >= 20) {
            // Efecto especial para mobs de alto nivel
            for (int i = 0; i < 30; i++) {
                double x = (Math.random() - 0.5) * 2;
                double y = Math.random() * 2;
                double z = (Math.random() - 0.5) * 2;

                location.getWorld().spawnParticle(Particle.DRAGON_BREATH,
                        location.clone().add(x, y, z), 1);
            }
        } else if (level >= 10) {
            // Efecto para mobs de nivel medio
            for (int i = 0; i < 20; i++) {
                double x = (Math.random() - 0.5) * 1.5;
                double y = Math.random() * 1.5;
                double z = (Math.random() - 0.5) * 1.5;

                location.getWorld().spawnParticle(Particle.SMOKE,
                        location.clone().add(x, y, z), 1);
            }
        } else {
            // Efecto básico para mobs de bajo nivel
            for (int i = 0; i < 10; i++) {
                double x = (Math.random() - 0.5) * 1;
                double y = Math.random() * 1;
                double z = (Math.random() - 0.5) * 1;

                location.getWorld().spawnParticle(Particle.CLOUD,
                        location.clone().add(x, y, z), 1);
            }
        }
    }

    public void spawnEliteEffect(Location location, int enemyLevel) {
        // Crear efecto especial para mobs elite
        for (int i = 0; i < 40; i++) {
            double angle = 2 * Math.PI * i / 20;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            double y = Math.sin(i * 0.5) * 0.5;

            location.getWorld().spawnParticle(Particle.FLAME,
                    location.clone().add(x, y + 1, z), 1);
        }
    }

    public void spawnBossEffect(Location location, int enemyLevel) {
        // Crear efecto especial para jefes
        for (int i = 0; i < 60; i++) {
            double angle = 2 * Math.PI * i / 30;
            double x = Math.cos(angle) * 3;
            double z = Math.sin(angle) * 3;
            double y = Math.sin(i * 0.3) * 1;

            location.getWorld().spawnParticle(Particle.DRAGON_BREATH,
                    location.clone().add(x, y + 2, z), 1);
        }
    }

    public void spawnEliteDeathEffect(Location location, int level) {
        // Efecto de muerte para mobs elite
        for (int i = 0; i < 50; i++) {
            double x = (Math.random() - 0.5) * 3;
            double y = Math.random() * 3;
            double z = (Math.random() - 0.5) * 3;

            location.getWorld().spawnParticle(Particle.LAVA,
                    location.clone().add(x, y, z), 1);
        }
    }

    public void spawnBossDeathEffect(Location location, int level) {
        // Efecto de muerte para jefes
        for (int i = 0; i < 100; i++) {
            double x = (Math.random() - 0.5) * 4;
            double y = Math.random() * 4;
            double z = (Math.random() - 0.5) * 4;

            location.getWorld().spawnParticle(Particle.EXPLOSION,
                    location.clone().add(x, y, z), 1);
        }
    }

    public void spawnCriticalEffect(Location location) {
        // Efecto para golpes críticos
        for (int i = 0; i < 20; i++) {
            double x = (Math.random() - 0.5) * 1.5;
            double y = Math.random() * 1.5;
            double z = (Math.random() - 0.5) * 1.5;

            location.getWorld().spawnParticle(Particle.CRIT,
                    location.clone().add(x, y, z), 1);
        }
    }
}