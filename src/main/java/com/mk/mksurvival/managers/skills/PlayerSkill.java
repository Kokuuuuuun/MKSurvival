package com.mk.mksurvival.managers.skills;

public class PlayerSkill {
    private final SkillType type;
    private int level;
    private double exp;
    private double expNeeded;

    public PlayerSkill(SkillType type) {
        this.type = type;
        this.level = 1;
        this.exp = 0;
        this.expNeeded = calculateExpNeeded(level);
    }

    public PlayerSkill(SkillType type, int level, double exp) {
        this.type = type;
        this.level = level;
        this.exp = exp;
        this.expNeeded = calculateExpNeeded(level);
    }

    public void addExp(double amount) {
        this.exp += amount;
    }

    public void addExperience(int amount) {
        addExp(amount);
    }

    public boolean checkLevelUp() {
        if (exp >= expNeeded) {
            level++;
            exp -= expNeeded;
            expNeeded = calculateExpNeeded(level);
            return true;
        }
        return false;
    }

    public double calculateExpNeeded(int level) {
        return 100 * level; // Fórmula simple: 100 * nivel
    }

    public double getProgressPercentage() {
        return (exp / expNeeded) * 100;
    }

    public String getProgressBar() {
        int bars = 20;
        int filledBars = (int) ((exp / expNeeded) * bars);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < filledBars; i++) {
            bar.append("█");
        }
        
        for (int i = filledBars; i < bars; i++) {
            bar.append("░");
        }
        
        return bar.toString();
    }

    // Métodos para obtener bonificaciones basadas en el nivel
    public int getDoubleDropChance() {
        if (type == SkillType.MINING) {
            return (level / 10) * 10;
        }
        return 0;
    }

    public int getDamageBonus() {
        if (type == SkillType.COMBAT) {
            return (level / 5) * 5;
        }
        return 0;
    }

    public int getTreeFellerChance() {
        if (type == SkillType.WOODCUTTING) {
            return (level / 8) * 8;
        }
        return 0;
    }

    public int getRareFishChance() {
        if (type == SkillType.FISHING) {
            return (level / 5) * 5;
        }
        return 0;
    }

    public int getCropYieldBonus() {
        if (type == SkillType.FARMING) {
            return (level / 6) * 6;
        }
        return 0;
    }

    public int getRareFindChance() {
        if (type == SkillType.FORAGING) {
            return (level / 7) * 7;
        }
        return 0;
    }

    public int getPotionEfficiencyBonus() {
        if (type == SkillType.ALCHEMY) {
            return (level / 10) * 10;
        }
        return 0;
    }

    public int getEnchantmentBonus() {
        if (type == SkillType.ENCHANTING) {
            return (level / 8) * 8;
        }
        return 0;
    }

    // Getters and setters
    public SkillType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.expNeeded = calculateExpNeeded(level);
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public double getExpNeeded() {
        return expNeeded;
    }
}