package com.mk.mksurvival.managers.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Gestiona todas las especializaciones de habilidades de un jugador.
 */
public class PlayerSpecializations {
    private final Map<String, PlayerSpecialization> specializations;
    
    /**
     * Constructor para crear un nuevo gestor de especializaciones de jugador.
     */
    public PlayerSpecializations() {
        this.specializations = new HashMap<>();
    }
    
    /**
     * Verifica si el jugador tiene desbloqueada una especialización específica.
     *
     * @param specializationId El ID de la especialización a verificar
     * @return true si la especialización está desbloqueada, false en caso contrario
     */
    public boolean hasSpecialization(String specializationId) {
        return specializations.containsKey(specializationId);
    }
    
    /**
     * Obtiene una especialización específica del jugador.
     *
     * @param specializationId El ID de la especialización a obtener
     * @return La especialización del jugador, o null si no está desbloqueada
     */
    public PlayerSpecialization getSpecialization(String specializationId) {
        return specializations.get(specializationId);
    }
    
    /**
     * Obtiene el nivel de una especialización específica.
     *
     * @param specializationId El ID de la especialización
     * @return El nivel de la especialización, o 0 si no está desbloqueada
     */
    public int getSpecializationLevel(String specializationId) {
        PlayerSpecialization spec = getSpecialization(specializationId);
        return spec != null ? spec.getLevel() : 0;
    }
    
    /**
     * Desbloquea una nueva especialización para el jugador.
     *
     * @param specializationId El ID de la especialización a desbloquear
     * @return true si se desbloqueó correctamente, false si ya estaba desbloqueada
     */
    public boolean unlockSpecialization(String specializationId) {
        if (hasSpecialization(specializationId)) {
            return false;
        }
        specializations.put(specializationId, new PlayerSpecialization(specializationId));
        return true;
    }
    
    /**
     * Mejora el nivel de una especialización específica.
     *
     * @param specializationId El ID de la especialización a mejorar
     * @param maxLevel El nivel máximo permitido para esta especialización
     * @return true si se mejoró correctamente, false si no está desbloqueada o ya está en el nivel máximo
     */
    public boolean upgradeSpecialization(String specializationId, int maxLevel) {
        PlayerSpecialization spec = getSpecialization(specializationId);
        if (spec == null || !spec.canUpgrade(maxLevel)) {
            return false;
        }
        spec.incrementLevel();
        return true;
    }
    
    /**
     * Establece el nivel de una especialización específica.
     *
     * @param specializationId El ID de la especialización
     * @param level El nuevo nivel
     * @return true si se estableció correctamente, false si no está desbloqueada
     */
    public boolean setSpecializationLevel(String specializationId, int level) {
        PlayerSpecialization spec = getSpecialization(specializationId);
        if (spec == null) {
            return false;
        }
        spec.setLevel(level);
        return true;
    }
    
    /**
     * Obtiene todos los IDs de las especializaciones desbloqueadas por el jugador.
     *
     * @return Un conjunto con los IDs de las especializaciones desbloqueadas
     */
    public Set<String> getUnlockedSpecializations() {
        return specializations.keySet();
    }
    
    /**
     * Obtiene todas las especializaciones desbloqueadas por el jugador.
     *
     * @return Un mapa con las especializaciones desbloqueadas
     */
    public Map<String, PlayerSpecialization> getAllSpecializations() {
        return new HashMap<>(specializations);
    }
    
    /**
     * Añade una especialización ya existente al jugador.
     * Útil para cargar datos guardados.
     *
     * @param specializationId El ID de la especialización
     * @param level El nivel de la especialización
     */
    public void addSpecialization(String specializationId, int level) {
        specializations.put(specializationId, new PlayerSpecialization(specializationId, level));
    }
}