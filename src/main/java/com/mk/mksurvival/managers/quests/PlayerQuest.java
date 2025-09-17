package com.mk.mksurvival.managers.quests;

import java.time.LocalDateTime;
import java.util.HashMap;

public class PlayerQuest {
    private final Quest quest;
    private final HashMap<String, Integer> progress = new HashMap<>();
    private final LocalDateTime startTime;

    private boolean completed;
    private LocalDateTime completionTime;

    public PlayerQuest(Quest quest) {
        this.quest = quest;
        this.startTime = LocalDateTime.now();

        // Inicializar el progreso para cada requisito
        for (String material : quest.getRequirements().keySet()) {
            progress.put(material, 0);
        }
    }

    /**
     * Añade progreso a un requisito específico de la misión
     * @param material El material o tipo de requisito
     * @param amount La cantidad a añadir
     * @return true si la misión se ha completado con esta adición, false en caso contrario
     */
    public boolean addProgress(String material, int amount) {
        if (completed) return false;

        if (progress.containsKey(material)) {
            int current = progress.get(material);
            int required = quest.getRequirements().get(material);
            if (current < required) {
                int newAmount = Math.min(current + amount, required);
                progress.put(material, newAmount);
                // Verificar si todos los requisitos se han cumplido
                return checkCompletion();
            }
        }
        return false;
    }

    /**
     * Verifica si todos los requisitos de la misión se han cumplido
     * @return true si la misión está completa, false en caso contrario
     */
    public boolean checkCompletion() {
        if (completed) return true;

        for (String material : progress.keySet()) {
            if (progress.get(material) < quest.getRequirements().get(material)) {
                return false;
            }
        }

        // Si llegamos aquí, todos los requisitos están completos
        completed = true;
        completionTime = LocalDateTime.now();
        return true;
    }

    /**
     * Calcula el porcentaje de progreso total de la misión
     * @return Un valor entre 0 y 100 que representa el porcentaje de progreso
     */
    public int getProgressPercentage() {
        if (completed) return 100;

        int totalRequired = 0;
        int totalProgress = 0;

        for (String material : quest.getRequirements().keySet()) {
            int required = quest.getRequirements().get(material);
            int current = progress.getOrDefault(material, 0);

            totalRequired += required;
            totalProgress += Math.min(current, required);
        }

        if (totalRequired == 0) return 100; // Evitar división por cero
        return (int) ((totalProgress * 100.0) / totalRequired);
    }

    // Getter methods
    public Quest getQuest() { return quest; }
    public HashMap<String, Integer> getProgress() { return progress; }
    public LocalDateTime getStartTime() { return startTime; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCompletionTime() { return completionTime; }

    // Setter methods
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCompletionTime(LocalDateTime completionTime) { this.completionTime = completionTime; }
}