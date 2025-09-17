package com.mk.mksurvival.managers.skills;

import java.util.EnumMap;
import java.util.Map;

public class PlayerSkills {
    private final Map<SkillType, PlayerSkill> skills;

    public PlayerSkills() {
        this.skills = new EnumMap<>(SkillType.class);
        initializeSkills();
    }

    public Map<SkillType, PlayerSkill> getSkills() {
        return skills;
    }

    private void initializeSkills() {
        for (SkillType type : SkillType.values()) {
            skills.put(type, new PlayerSkill(type));
        }
    }

    public PlayerSkill getSkill(SkillType type) {
        return skills.get(type);
    }

    public int getLevel(SkillType type) {
        return skills.get(type).getLevel();
    }

    public double getExp(SkillType type) {
        return skills.get(type).getExp();
    }

    public void setLevel(SkillType type, int level) {
        skills.get(type).setLevel(level);
    }

    public void setExp(SkillType type, double exp) {
        skills.get(type).setExp(exp);
    }

    public void addExp(SkillType type, double amount) {
        skills.get(type).addExp(amount);
    }

    public boolean checkLevelUp(SkillType type) {
        return skills.get(type).checkLevelUp();
    }

    public int getTotalLevel() {
        int total = 0;
        for (PlayerSkill skill : skills.values()) {
            total += skill.getLevel();
        }
        return total;
    }

    public double getTotalExp() {
        double total = 0;
        for (PlayerSkill skill : skills.values()) {
            total += skill.getExp();
        }
        return total;
    }

    public int getTotalExperience() {
        return (int) getTotalExp();
    }

    public double getOverallProgress() {
        double totalProgress = 0;
        for (SkillType type : SkillType.values()) {
            PlayerSkill skill = skills.get(type);
            totalProgress += (skill.getExp() / skill.getExpNeeded());
        }
        return (totalProgress / SkillType.values().length) * 100;
    }

    public double getExpNeeded(int level) {
        return 100 * level; // Fórmula simple: 100 * nivel
    }
}