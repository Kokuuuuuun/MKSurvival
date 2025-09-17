package com.mk.mksurvival.managers.land;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LandSelectionManager implements Listener {
    private final MKSurvival plugin;
    private final Map<UUID, Location[]> selections = new HashMap<>();
    private final Map<UUID, Boolean> selecting = new HashMap<>();

    public LandSelectionManager(MKSurvival plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startSelection(Player player) {
        UUID uuid = player.getUniqueId();
        selecting.put(uuid, true);
        selections.remove(uuid);
        ItemStack selectionTool = new ItemStack(Material.WOODEN_AXE);
        org.bukkit.inventory.meta.ItemMeta meta = selectionTool.getItemMeta();
        meta.displayName(MessageUtils.parse("<gold>Herramienta de Selección de Terrenos"));
        selectionTool.setItemMeta(meta);
        player.getInventory().addItem(selectionTool);
        MessageUtils.sendMessage(player, "<green>[Land] Selecciona dos puntos para definir tu área. Click izquierdo para el primer punto, derecho para el segundo.</green>");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!selecting.getOrDefault(uuid, false)) return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK &&
                event.getItem() != null &&
                event.getItem().getType() == Material.WOODEN_AXE) {
            event.setCancelled(true);
            Location loc1 = event.getClickedBlock().getLocation();
            if (!selections.containsKey(uuid)) {
                selections.put(uuid, new Location[]{loc1, null});
                MessageUtils.sendMessage(player, "<green>[Land] Primer punto seleccionado. Ahora selecciona el segundo punto.</green>");
            } else {
                selections.get(uuid)[0] = loc1;
                MessageUtils.sendMessage(player, "<green>[Land] Primer punto actualizado.</green>");
            }
            showSelectionPreview(player);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getItem() != null &&
                event.getItem().getType() == Material.WOODEN_AXE) {
            event.setCancelled(true);
            Location loc2 = event.getClickedBlock().getLocation();
            if (selections.containsKey(uuid)) {
                selections.get(uuid)[1] = loc2;
                MessageUtils.sendMessage(player, "<green>[Land] Segundo punto seleccionado. Usa /land claim para reclamar esta área.</green>");
                showSelectionPreview(player);
            } else {
                MessageUtils.sendMessage(player, "<red>[Land] Primero selecciona el primer punto.</red>");
            }
        }
    }

    private void showSelectionPreview(Player player) {
        UUID uuid = player.getUniqueId();
        if (!selections.containsKey(uuid)) return;
        Location[] points = selections.get(uuid);
        if (points[0] == null) return;
        World world = player.getWorld();
        Location corner1 = points[0];
        Location corner2 = points[1] != null ? points[1] : corner1;
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || z == minZ || z == maxZ) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.GLOWSTONE);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                if (block.getType() == Material.GLOWSTONE) {
                                    block.setType(Material.AIR);
                                }
                            }, 100L);
                        }
                    }
                }
            }
        }
        MessageUtils.sendMessage(player, "<yellow>[Land] Vista previa del área: " +
                (maxX - minX + 1) + "x" + (maxZ - minZ + 1) + "x" + (maxY - minY + 1) + "</yellow>");
    }

    public Location[] getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void endSelection(Player player) {
        UUID uuid = player.getUniqueId();
        selecting.remove(uuid);
        selections.remove(uuid);
        
        // Buscar y eliminar la herramienta de selección del inventario
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.WOODEN_AXE && 
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() && 
                item.getItemMeta().getDisplayName().equals(MessageUtils.parse(("<gold>Herramienta de Selección de Terrenos")))) {
                player.getInventory().setItem(i, null);
                break;
            }
        }
        
        // Actualizar el inventario del jugador
        player.updateInventory();
    }
}