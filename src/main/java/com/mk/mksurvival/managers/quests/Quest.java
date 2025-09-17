package com.mk.mksurvival.managers.quests;

import com.mk.mksurvival.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.List;

public class Quest {
    private final String id;
    private final String name;
    private final String description;
    private final HashMap<String, Integer> requirements;
    private final double moneyReward;
    private final List<ItemStack> itemRewards;
    private final int expReward;
    private final List<String> commands;

    public Quest(String id, String name, String description, HashMap<String, Integer> requirements,
                 double moneyReward, List<ItemStack> itemRewards, int expReward, List<String> commands) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requirements = requirements;
        this.moneyReward = moneyReward;
        this.itemRewards = itemRewards;
        this.expReward = expReward;
        this.commands = commands;
    }

    public Component getFormattedName() {
        return MessageUtils.parse("<gold>" + name);
    }

    public Component getFormattedDescription() {
        return MessageUtils.parse("<gray>" + description);
    }

    public Component getFormattedRequirements(Player player, HashMap<String, Integer> progress) {
        StringBuilder builder = new StringBuilder("<gray>Requisitos:</gray>\n");

        for (String material : requirements.keySet()) {
            int required = requirements.get(material);
            int current = progress.getOrDefault(material, 0);
            String color = current >= required ? "<green>" : "<yellow>";

            String materialName = formatMaterialName(material);
            builder.append(color).append("- ").append(materialName).append(": ")
                    .append(current).append("/").append(required).append("</gray>\n");
        }

        return MessageUtils.parse(builder.toString(), player);
    }

    private String formatMaterialName(String material) {
        if (material.startsWith("MOB_")) {
            return material.substring(4).toLowerCase().replace("_", " ");
        }
        return material.toLowerCase().replace("_", " ");
    }

    public Component getFormattedRewards() {
        StringBuilder builder = new StringBuilder("<gray>Recompensas:</gray>\n");

        if (moneyReward > 0) {
            builder.append("<yellow>- ").append(moneyReward).append(" monedas</yellow>\n");
        }

        if (expReward > 0) {
            builder.append("<green>- ").append(expReward).append(" puntos de experiencia</green>\n");
        }

        if (!itemRewards.isEmpty()) {
            builder.append("<aqua>- ").append(itemRewards.size()).append(" items</aqua>\n");
        }

        return MessageUtils.parse(builder.toString());
    }

    // Getter methods
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public HashMap<String, Integer> getRequirements() { return requirements; }
    public double getMoneyReward() { return moneyReward; }
    public List<ItemStack> getItemRewards() { return itemRewards; }
    public int getExpReward() { return expReward; }
    public List<String> getCommands() { return commands; }
}