package com.mk.mksurvival.managers.utils;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {
    private final MKSurvival plugin;
    private final Map<UUID, ChatInputRequest> activeRequests;
    private final Map<UUID, BukkitTask> timeoutTasks;

    public ChatInputManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.activeRequests = new HashMap<>();
        this.timeoutTasks = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Registra una solicitud de entrada de chat para un jugador
     * @param player El jugador del que se espera la entrada
     * @param prompt El mensaje a mostrar al jugador
     * @param timeoutSeconds Tiempo en segundos antes de cancelar la solicitud
     * @param callback Función a llamar cuando se reciba la entrada
     * @param timeoutCallback Función a llamar si se agota el tiempo
     */
    public void requestInput(Player player, String prompt, int timeoutSeconds, 
                            Consumer<String> callback, Runnable timeoutCallback) {
        UUID playerUUID = player.getUniqueId();
        
        // Cancelar cualquier solicitud anterior
        cancelRequest(playerUUID);
        
        // Enviar el mensaje al jugador
        MessageUtils.sendMessage(player, prompt);
        
        // Crear la nueva solicitud
        ChatInputRequest request = new ChatInputRequest(callback);
        activeRequests.put(playerUUID, request);
        
        // Configurar el timeout
        BukkitTask timeoutTask = plugin.getServer().getScheduler().runTaskLater(
            plugin,
            () -> {
                if (activeRequests.remove(playerUUID) != null) {
                    timeoutTasks.remove(playerUUID);
                    if (timeoutCallback != null) {
                        timeoutCallback.run();
                    }
                }
            },
            timeoutSeconds * 20L // Convertir segundos a ticks
        );
        
        timeoutTasks.put(playerUUID, timeoutTask);
    }

    /**
     * Cancela una solicitud de entrada de chat activa
     * @param playerUUID UUID del jugador
     */
    public void cancelRequest(UUID playerUUID) {
        activeRequests.remove(playerUUID);
        
        BukkitTask task = timeoutTasks.remove(playerUUID);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Verifica si un jugador tiene una solicitud de entrada activa
     * @param playerUUID UUID del jugador
     * @return true si el jugador tiene una solicitud activa
     */
    public boolean hasActiveRequest(UUID playerUUID) {
        return activeRequests.containsKey(playerUUID);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        ChatInputRequest request = activeRequests.get(playerUUID);
        if (request != null) {
            event.setCancelled(true); // Cancelar el evento de chat para que no se muestre públicamente
            
            // Procesar la entrada en el hilo principal
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                String input = event.getMessage();
                activeRequests.remove(playerUUID);
                
                BukkitTask task = timeoutTasks.remove(playerUUID);
                if (task != null) {
                    task.cancel();
                }
                
                request.getCallback().accept(input);
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Limpiar solicitudes cuando un jugador se desconecta
        cancelRequest(event.getPlayer().getUniqueId());
    }

    /**
     * Clase interna para almacenar información sobre una solicitud de entrada de chat
     */
    private static class ChatInputRequest {
        private final Consumer<String> callback;

        public ChatInputRequest(Consumer<String> callback) {
            this.callback = callback;
        }

        public Consumer<String> getCallback() {
            return callback;
        }
    }
}