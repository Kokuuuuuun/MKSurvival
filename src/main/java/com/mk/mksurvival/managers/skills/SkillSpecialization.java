package com.mk.mksurvival.managers.skills;

import com.mk.mksurvival.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una especialización de habilidad que los jugadores pueden desbloquear
 * para obtener bonificaciones adicionales.
 */
public class SkillSpecialization {
    private final String id;
    private final String name;
    private final String description;
    private final SkillType skillType;
    private final int maxLevel;
    private final List<String> levelBonuses;
    private final int requiredSkillLevel;
    private final int unlockCost;
    private final int upgradeCostBase;
    
    /**
     * Constructor para crear una nueva especialización de habilidad.
     *
     * @param id El identificador único de la especialización
     * @param name El nombre de la especialización
     * @param description La descripción de la especialización
     * @param skillType El tipo de habilidad al que pertenece esta especialización
     * @param maxLevel El nivel máximo de esta especialización
     * @param levelBonuses Lista de descripciones de bonificaciones por nivel
     * @param requiredSkillLevel Nivel de habilidad requerido para desbloquear esta especialización
     * @param unlockCost Costo para desbloquear esta especialización
     * @param upgradeCostBase Costo base para mejorar esta especialización (se multiplica por el nivel)
     */
    public SkillSpecialization(String id, String name, String description, SkillType skillType, 
                              int maxLevel, List<String> levelBonuses, int requiredSkillLevel, 
                              int unlockCost, int upgradeCostBase) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.maxLevel = maxLevel;
        this.levelBonuses = levelBonuses;
        this.requiredSkillLevel = requiredSkillLevel;
        this.unlockCost = unlockCost;
        this.upgradeCostBase = upgradeCostBase;
    }
    
    /**
     * Obtiene el ID de la especialización.
     *
     * @return El ID de la especialización
     */
    public String getId() {
        return id;
    }
    
    /**
     * Obtiene el nombre de la especialización.
     *
     * @return El nombre de la especialización
     */
    public String getName() {
        return name;
    }
    
    /**
     * Obtiene el nombre formateado de la especialización con MiniMessage.
     *
     * @return El nombre formateado de la especialización
     */
    public String getFormattedName() {
        return MessageUtils.toLegacy(MessageUtils.parse("<" + skillType.getColor() + ">" + name));
    }
    
    /**
     * Obtiene la descripción de la especialización.
     *
     * @return La descripción de la especialización
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Obtiene la descripción formateada de la especialización con MiniMessage.
     *
     * @return La descripción formateada de la especialización
     */
    public String getFormattedDescription() {
        return MessageUtils.toLegacy(MessageUtils.parse("<gray>" + description));
    }
    
    /**
     * Obtiene el tipo de habilidad al que pertenece esta especialización.
     *
     * @return El tipo de habilidad
     */
    public SkillType getSkillType() {
        return skillType;
    }
    
    /**
     * Obtiene el nivel máximo de esta especialización.
     *
     * @return El nivel máximo
     */
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Obtiene la lista de bonificaciones por nivel.
     *
     * @return Lista de descripciones de bonificaciones
     */
    public List<String> getLevelBonuses() {
        return new ArrayList<>(levelBonuses);
    }
    
    /**
     * Obtiene la bonificación para un nivel específico.
     *
     * @param level El nivel para el que se quiere obtener la bonificación
     * @return La descripción de la bonificación para ese nivel, o null si no existe
     */
    public String getBonusForLevel(int level) {
        if (level <= 0 || level > levelBonuses.size()) {
            return null;
        }
        return levelBonuses.get(level - 1);
    }
    
    /**
     * Obtiene la bonificación formateada para un nivel específico con MiniMessage.
     *
     * @param level El nivel para el que se quiere obtener la bonificación
     * @return La descripción formateada de la bonificación para ese nivel, o null si no existe
     */
    public String getFormattedBonusForLevel(int level) {
        String bonus = getBonusForLevel(level);
        if (bonus == null) {
            return MessageUtils.toLegacy(MessageUtils.parse("<gray>Especialización no encontrada"));
        }
        return MessageUtils.toLegacy(MessageUtils.parse("<" + skillType.getColor() + ">" + bonus));
    }
    
    /**
     * Obtiene el nivel de habilidad requerido para desbloquear esta especialización.
     *
     * @return El nivel de habilidad requerido
     */
    public int getRequiredSkillLevel() {
        return requiredSkillLevel;
    }
    
    /**
     * Obtiene el costo para desbloquear esta especialización.
     *
     * @return El costo de desbloqueo
     */
    public int getUnlockCost() {
        return unlockCost;
    }
    
    /**
     * Calcula el costo para mejorar esta especialización a un nivel específico.
     *
     * @param currentLevel El nivel actual de la especialización
     * @return El costo para mejorar al siguiente nivel
     */
    public int getUpgradeCost(int currentLevel) {
        if (currentLevel >= maxLevel) {
            return 0; // Ya está en el nivel máximo
        }
        return upgradeCostBase * (currentLevel + 1);
    }
}