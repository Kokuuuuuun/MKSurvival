package com.mk.mksurvival.managers.quests;

import lombok.Getter;

@Getter
public enum QuestType {
    MINING("Minería", "<dark_aqua>"),
    COMBAT("Combate", "<dark_red>"),
    FISHING("Pesca", "<blue>"),
    FARMING("Agricultura", "<green>"),
    EXPLORATION("Exploración", "<gold>"),
    CRAFTING("Fabricación", "<yellow>"),
    BUILDING("Construcción", "<dark_green>"),
    TRADING("Comercio", "<light_purple>"),
    CUSTOM("Personalizada", "<gray>");

    private final String displayName;
    private final String colorCode;

    QuestType(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getFormattedName() {
        return colorCode + displayName;
    }

    public static QuestType fromString(String type) {
        try {
            return QuestType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOM;
        }
    }
}