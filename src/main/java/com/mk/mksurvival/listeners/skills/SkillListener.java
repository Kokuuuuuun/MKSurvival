package com.mk.mksurvival.listeners.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.managers.skills.PlayerSkills;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class SkillListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();

        // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
        SkillManager skillManager = MKSurvival.getInstance().getSkillManager();

        // Mining skills
        if (isOre(material) || isStone(material)) {
            skillManager.addExp(player, SkillType.MINING, getMiningExp(material));

            // Double drop chance
            PlayerSkills skills = skillManager.getPlayerSkills(player);
            int level = skills.getLevel(SkillType.MINING);
            if (level >= 10 && Math.random() < (level / 10) * 0.1) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), event.getBlock().getDrops().iterator().next());
                MessageUtils.sendMessage(player, "<yellow>[Skills] ¡Doble drop!");
            }
        }

        // Woodcutting skills
        if (isLog(material)) {
            skillManager.addExp(player, SkillType.WOODCUTTING, getWoodcuttingExp(material));
        }

        // Farming skills
        if (isCrop(material)) {
            skillManager.addExp(player, SkillType.FARMING, getFarmingExp(material));
        }

        // Foraging skills
        if (isForagable(material)) {
            skillManager.addExp(player, SkillType.FORAGING, getForagingExp(material));
        }

        // Quest progress
        // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
        MKSurvival.getInstance().getQuestManager().checkQuestCompletion(player, material.name(), 1);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();

                

            // Combat skills
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            SkillManager skillManager = MKSurvival.getInstance().getSkillManager();
            skillManager.addExp(player, SkillType.COMBAT, getCombatExp(event.getEntityType()));

            // Increase drops based on combat level
            PlayerSkills skills = skillManager.getPlayerSkills(player);
            int level = skills.getLevel(SkillType.COMBAT);
            if (level >= 5 && Math.random() < (level / 5) * 0.05) {
                if (event.getEntity().getEquipment().getItemInMainHandDropChance() > 0) {
                    event.getDrops().add(event.getEntity().getEquipment().getItemInMainHand());
                } else {
                    event.getDrops().add(event.getEntity().getEquipment().getItemInOffHand());
                }
                MessageUtils.sendMessage(player, "<yellow>[Skills] ¡Drop extra!");
            }
            // Añadir esto en onEntityDeath después de dar experiencia de combate
// Dar recompensas basadas en el nivel del enemigo
            MKSurvival plugin = null;
            int enemyLevel = plugin.getEnemyLevelManager().getEntityLevel(event.getEntityType());
            plugin.getRewardManager().giveRandomReward(player, event.getEntityType().name());

// Actualizar la barra de experiencia
            plugin.getExpBarManager().updatePlayerExpBar(player);

        }

    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();

            // Fishing skills
            // Corregido: usar MKSurvival.getInstance() en lugar de MKSurvival
            SkillManager skillManager = MKSurvival.getInstance().getSkillManager();
            skillManager.addExp(player, SkillType.FISHING, 5.0);

            // Rare fish chance
            PlayerSkills skills = skillManager.getPlayerSkills(player);
            int level = skills.getLevel(SkillType.FISHING);
            if (level >= 5 && Math.random() < (level / 5) * 0.05) {
                MessageUtils.sendMessage(player, "<yellow>[Skills] ¡Has pescado un pez raro!");
            }
        }
    }

    // Helper methods
    private boolean isOre(Material material) {
        return material == Material.COAL_ORE || material == Material.IRON_ORE ||
                material == Material.GOLD_ORE || material == Material.DIAMOND_ORE ||
                material == Material.EMERALD_ORE || material == Material.LAPIS_ORE ||
                material == Material.REDSTONE_ORE || material == Material.NETHER_QUARTZ_ORE;
    }

    private boolean isStone(Material material) {
        return material == Material.STONE || material == Material.COBBLESTONE ||
                material == Material.ANDESITE || material == Material.DIORITE ||
                material == Material.GRANITE || material == Material.TUFF;
    }

    private boolean isLog(Material material) {
        return material == Material.OAK_LOG || material == Material.BIRCH_LOG ||
                material == Material.SPRUCE_LOG || material == Material.JUNGLE_LOG ||
                material == Material.ACACIA_LOG || material == Material.DARK_OAK_LOG ||
                material == Material.MANGROVE_LOG || material == Material.CHERRY_LOG;
    }

    private boolean isCrop(Material material) {
        return material == Material.WHEAT || material == Material.CARROTS ||
                material == Material.POTATOES || material == Material.BEETROOTS ||
                material == Material.NETHER_WART || material == Material.COCOA;
    }

    private boolean isForagable(Material material) {
        return material == Material.SHORT_GRASS || material == Material.FERN ||
                material == Material.SEAGRASS || material == Material.VINE ||
                material == Material.DEAD_BUSH || material == Material.SUGAR_CANE;
    }

    private double getMiningExp(Material material) {
        if (isOre(material)) {
            if (material == Material.COAL_ORE) return 5.0;
            if (material == Material.IRON_ORE) return 7.0;
            if (material == Material.GOLD_ORE) return 10.0;
            if (material == Material.DIAMOND_ORE) return 15.0;
            if (material == Material.EMERALD_ORE) return 20.0;
            return 7.0;
        }
        if (isStone(material)) return 3.0;
        return 1.0;
    }

    private double getWoodcuttingExp(Material material) {
        return 7.0;
    }

    private double getFarmingExp(Material material) {
        return 5.0;
    }

    private double getForagingExp(Material material) {
        return 2.0;
    }

    private double getCombatExp(EntityType entityType) {
        switch (entityType) {
            case ZOMBIE: return 7.0;
            case SKELETON: return 7.0;
            case SPIDER: return 6.0;
            case CREEPER: return 10.0;
            case ENDERMAN: return 15.0;
            case WITCH: return 12.0;
            case CAVE_SPIDER: return 8.0;
            case SILVERFISH: return 5.0;
            default: return 5.0;
        }
    }
}