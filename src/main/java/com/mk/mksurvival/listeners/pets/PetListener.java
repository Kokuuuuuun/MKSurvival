package com.mk.mksurvival.listeners.pets;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.gui.pets.PetGUI;
import com.mk.mksurvival.managers.pets.PetManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PetListener implements Listener {
    private final MKSurvival plugin;
    private final PetManager petManager;
    private final PetGUI petGUI;

    public PetListener(MKSurvival plugin) {
        this.plugin = plugin;
        this.petManager = plugin.getPetManager();
        this.petGUI = new PetGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals("Menú de Mascotas")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            switch (event.getCurrentItem().getType()) {
                case EGG:
                    // Abrir GUI de tipos de mascotas
                    petGUI.openPetTypes(player);
                    break;
                case BOOK:
                    // Abrir GUI de tipos de mascotas
                    petGUI.openPetTypes(player);
                    break;
                case BONE:
                    // Abrir GUI de gestión de mascota
                    petGUI.openPetManage(player);
                    break;
            }
        }
        else if (event.getView().getTitle().equals("Tipos de Mascotas")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            EntityType type = null;
            switch (event.getCurrentItem().getType()) {
                case BONE:
                    type = EntityType.WOLF;
                    break;
                case COD:
                    type = EntityType.CAT;
                    break;
                case FEATHER:
                    type = EntityType.PARROT;
                    break;
                case SADDLE:
                    type = EntityType.HORSE;
                    break;
                case SKELETON_SKULL:
                    type = EntityType.SKELETON_HORSE;
                    break;
            }

            if (type != null) {
                player.closeInventory();
                petManager.spawnPet(player, type.name(), "Mascota");
            }
        }
        else if (event.getView().getTitle().equals("Gestionar Mascota")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            switch (event.getCurrentItem().getType()) {
                case NAME_TAG:
                    // Nombrar mascota
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "<yellow>Usa el comando: /mascota nombre <nombre>");
                    break;
                case NETHER_STAR:
                    // Usar habilidad
                    player.closeInventory();
                    petManager.petSkill(player);
                    break;
                case BARRIER:
                    // Despedir mascota
                    player.closeInventory();
                    petManager.despawnPet(player);
                    break;
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Evitar que las mascotas reciban daño de sus dueños
        if (event.getEntity() instanceof Player) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        PetManager.Pet pet = petManager.getPlayerPet(damager);
        if (pet != null && pet.getEntity() != null && pet.getEntity().equals(event.getEntity())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(damager, "<red>No puedes dañar a tu propia mascota.");
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Notificar al dueño si su mascota muere
        Map<UUID, PetManager.Pet> pets = petManager.getPlayerPets();
        for (PetManager.Pet pet : pets.values()) {
            if (pet.getEntity() != null && pet.getEntity().equals(event.getEntity())) {
                Player owner = plugin.getServer().getPlayer(pet.getOwnerId());
                if (owner != null) {
                    MessageUtils.sendMessage(owner, "<red>Tu mascota ha muerto.");
                    petManager.getPlayerPets().remove(owner.getUniqueId());
                }
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Montar en mascotas que son caballos
        if (!(event.getRightClicked() instanceof Player)) {
            Player player = event.getPlayer();
            PetManager.Pet pet = petManager.getPlayerPet(player);
            if (pet != null && pet.getEntity() != null && pet.getEntity().equals(event.getRightClicked())) {
                if (pet.getType().equals(EntityType.HORSE.name()) || pet.getType().equals(EntityType.SKELETON_HORSE.name())) {
                    if (pet.getEntity() instanceof org.bukkit.entity.AbstractHorse) {
                        org.bukkit.entity.AbstractHorse horse = (org.bukkit.entity.AbstractHorse) pet.getEntity();
                        horse.setOwner(player);
                        horse.setTamed(true);
                        horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(Material.SADDLE));
                        MessageUtils.sendMessage(player, "<green>Has montado a tu caballo.");
                    }
                }
            }
        }
    }
}