package com.mk.mksurvival.managers.skills;

import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Material;

public enum SkillType {
    MINING("Minería", "aqua", Material.DIAMOND_PICKAXE, "Rompe bloques para ganar experiencia"),
    WOODCUTTING("Tala", "dark_green", Material.DIAMOND_AXE, "Tala árboles para ganar experiencia"),
    FISHING("Pesca", "blue", Material.FISHING_ROD, "Pesca para ganar experiencia"),
    COMBAT("Combate", "red", Material.DIAMOND_SWORD, "Derrota enemigos para ganar experiencia"),
    FARMING("Agricultura", "green", Material.DIAMOND_HOE, "Cultiva para ganar experiencia"),
    FORAGING("Recolección", "gold", Material.DIAMOND_SHOVEL, "Excava para ganar experiencia"),
    ALCHEMY("Alquimia", "light_purple", Material.BREWING_STAND, "Crea pociones para ganar experiencia"),
    ENCHANTING("Encantamiento", "dark_purple", Material.ENCHANTING_TABLE, "Encanta objetos para ganar experiencia");

    private final String displayName;
    private final String color;
    private final Material icon;
    private final String description;

    SkillType(String displayName, String color, Material icon, String description) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedName() {
        return MessageUtils.toLegacy(MessageUtils.parse("<" + color + ">" + displayName));
    }

    public String getFormattedDescription() {
        return MessageUtils.toLegacy(MessageUtils.parse("<gray>" + description));
    }

    @Override
    public String toString() {
        return displayName;
    }
}