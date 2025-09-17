package com.mk.mksurvival.managers.skills;

import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a skill in the game with its properties and bonuses.
 */
public class Skill {
    private final SkillType type;
    private final String name;
    private final String description;
    private final Material icon;
    private final List<String> bonuses;

    public Skill(SkillType type, String name, String description, Material icon) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.bonuses = new ArrayList<>();
    }

    public SkillType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public List<String> getBonuses() {
        return new ArrayList<>(bonuses);
    }

    public void addBonus(String bonus) {
        bonuses.add(bonus);
    }

    public void clearBonuses() {
        bonuses.clear();
    }

    public String getFormattedName() {
        return MessageUtils.toLegacy(MessageUtils.parse("<" + type.getColor() + ">" + name));
    }

    public String getFormattedDescription() {
        return MessageUtils.toLegacy(MessageUtils.parse("<gray>" + description));
    }

    public List<String> getFormattedBonuses() {
        List<String> formatted = new ArrayList<>();
        for (String bonus : bonuses) {
            formatted.add(MessageUtils.toLegacy(MessageUtils.parse("<white>• " + bonus)));
        }
        return formatted;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon=" + icon +
                ", bonuses=" + bonuses.size() +
                '}';
    }
}