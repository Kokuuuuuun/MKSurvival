package com.mk.mksurvival.managers.skills;

/**
 * Representa una especialización de habilidad que un jugador ha desbloqueado.
 */
public class PlayerSpecialization {
    private final String specializationId;
    private int level;
    
    /**
     * Constructor para crear una nueva especialización de jugador.
     *
     * @param specializationId El ID de la especialización
     */
    public PlayerSpecialization(String specializationId) {
        this.specializationId = specializationId;
        this.level = 1; // Nivel inicial al desbloquear
    }
    
    /**
     * Constructor para crear una especialización de jugador con un nivel específico.
     *
     * @param specializationId El ID de la especialización
     * @param level El nivel de la especialización
     */
    public PlayerSpecialization(String specializationId, int level) {
        this.specializationId = specializationId;
        this.level = level;
    }
    
    /**
     * Obtiene el ID de la especialización.
     *
     * @return El ID de la especialización
     */
    public String getSpecializationId() {
        return specializationId;
    }
    
    /**
     * Obtiene el nivel actual de la especialización.
     *
     * @return El nivel actual
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Establece el nivel de la especialización.
     *
     * @param level El nuevo nivel
     */
    public void setLevel(int level) {
        this.level = level;
    }
    
    /**
     * Incrementa el nivel de la especialización en 1.
     *
     * @return El nuevo nivel después del incremento
     */
    public int incrementLevel() {
        return ++level;
    }
    
    /**
     * Verifica si esta especialización puede ser mejorada a un nivel superior.
     *
     * @param maxLevel El nivel máximo permitido para esta especialización
     * @return true si puede ser mejorada, false en caso contrario
     */
    public boolean canUpgrade(int maxLevel) {
        return level < maxLevel;
    }
}