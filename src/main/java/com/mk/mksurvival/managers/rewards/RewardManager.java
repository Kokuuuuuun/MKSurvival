package com.mk.mksurvival.managers.rewards;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillType;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RewardManager {

    private final MKSurvival plugin;
    private final Map<String, Reward> rewards = new HashMap<>();

    public RewardManager(MKSurvival plugin) {
        this.plugin = plugin;
        setupDefaultRewards();
    }

    private void setupDefaultRewards() {
        // Recompensas comunes
        rewards.put("common_zombie", new Reward(
                "Zombie Común",
                0.7,
                new ArrayList<ItemStack>() {{
                    add(new ItemStack(org.bukkit.Material.ROTTEN_FLESH, 1));
                }},
                2.0,
                5
        ));

        rewards.put("rare_skeleton", new Reward(
                "Esqueleto Raro",
                0.3,
                new ArrayList<ItemStack>() {{
                    add(new ItemStack(org.bukkit.Material.BONE, 2));
                    add(new ItemStack(org.bukkit.Material.ARROW, 5));
                }},
                5.0,
                10
        ));

        rewards.put("epic_creeper", new Reward(
                "Creeper Épico",
                0.1,
                new ArrayList<ItemStack>() {{
                    add(new ItemStack(org.bukkit.Material.TNT, 1));
                    add(new ItemStack(org.bukkit.Material.GUNPOWDER, 3));
                }},
                10.0,
                25
        ));

        rewards.put("legendary_enderman", new Reward(
                "Enderman Legendario",
                0.05,
                new ArrayList<ItemStack>() {{
                    add(new ItemStack(org.bukkit.Material.ENDER_PEARL, 1));
                    add(new ItemStack(org.bukkit.Material.EXPERIENCE_BOTTLE, 3));
                }},
                20.0,
                50
        ));
    }

    public void giveReward(Player player, String rewardId) {
        Reward reward = rewards.get(rewardId);
        if (reward != null && Math.random() < reward.chance) {
            // Dar dinero
            plugin.getEconomyManager().addBalance(player, reward.money);

            // Dar experiencia
            plugin.getSkillManager().addExperience(player.getUniqueId(), SkillType.COMBAT, reward.exp);

            // Dar items
            for (ItemStack item : reward.items) {
                player.getInventory().addItem(item);
            }

            MessageUtils.sendMessage(player, "<green>[Recompensas] Has obtenido: " + reward.name + "</green>");
        }
    }

    public void giveEnemyReward(Player player, String entityType, int level, String enemyType) {
        // Calcular recompensas basadas en el nivel y tipo de enemigo
        double baseReward = level * 10.0;
        int expReward = level * 5;
        
        // Multiplicadores por tipo de enemigo
        double multiplier = switch (enemyType.toLowerCase()) {
            case "elite" -> 2.0;
            case "boss" -> 5.0;
            default -> 1.0;
        };
        
        double finalReward = baseReward * multiplier;
        int finalExp = (int) (expReward * multiplier);
        
        // Dar recompensas
        plugin.getEconomyManager().addBalance(player, finalReward);
        plugin.getSkillManager().addExperience(player.getUniqueId(), SkillType.COMBAT, finalExp);
        
        // Mensaje de recompensa
        MessageUtils.sendMessage(player, "<green>[Recompensas] +" + (int)finalReward + " monedas, +" + finalExp + " EXP de combate");
    }

    public void giveRandomReward(Player player, String entityType) {
        String rewardId = entityType.toLowerCase() + "_" + getRandomRarity();
        giveReward(player, rewardId);
    }

    private String getRandomRarity() {
        double random = Math.random();
        if (random < 0.6) return "common";
        if (random < 0.85) return "rare";
        if (random < 0.95) return "epic";
        return "legendary";
    }

    public static class Reward {
        public final String name;
        public final double chance;
        public final List<ItemStack> items;
        public final double money;
        public final int exp;
        public Object multiplier;

        public Reward(String name, double chance, List<ItemStack> items, double money, int exp) {
            this.name = name;
            this.chance = chance;
            this.items = items;
            this.money = money;
            this.exp = exp;
        }
    }

    public static class QuestReward {
        public <E> QuestReward(String string, double aDouble, ArrayList<E> es, double aDouble1, int anInt) {
        }
    }
}