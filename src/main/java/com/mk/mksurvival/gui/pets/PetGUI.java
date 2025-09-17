package com.mk.mksurvival.gui.pets;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.pets.PetManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PetGUI {
    private final MKSurvival plugin;
    private final PetManager petManager;

    public PetGUI(MKSurvival plugin) {
        this.plugin = plugin;
        this.petManager = plugin.getPetManager();
    }

    public void openPetMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Menú de Mascotas");

        // Item para spawnear mascota
        ItemStack spawnItem = new ItemStack(Material.EGG);
        ItemMeta spawnMeta = spawnItem.getItemMeta();
        spawnMeta.displayName(MessageUtils.parse("<green>Spawnear Mascota"));
        List<String> spawnLore = new ArrayList<>();
        spawnLore.add("<gray>Spawnea una nueva mascota.");
        spawnMeta.lore(MessageUtils.parseList(spawnLore));
        spawnItem.setItemMeta(spawnMeta);
        inv.setItem(11, spawnItem);

        // Item para ver tipos de mascotas
        ItemStack typesItem = new ItemStack(Material.BOOK);
        ItemMeta typesMeta = typesItem.getItemMeta();
        typesMeta.displayName(MessageUtils.parse("<yellow>Tipos de Mascotas"));
        List<String> typesLore = new ArrayList<>();
        typesLore.add("<gray>Ver todos los tipos de mascotas disponibles.");
        typesMeta.lore(MessageUtils.parseList(typesLore));
        typesItem.setItemMeta(typesMeta);
        inv.setItem(13, typesItem);

        // Item para gestionar mascota actual
        Optional<PetManager.Pet> petOptional = Optional.ofNullable(petManager.getPlayerPet(player));
        if (petOptional.isPresent()) {
            ItemStack manageItem = new ItemStack(Material.BONE);
            ItemMeta manageMeta = manageItem.getItemMeta();
            manageMeta.displayName(MessageUtils.parse("<aqua>Mi Mascota"));
            List<String> manageLore = new ArrayList<>();
            manageLore.add("<gray>Gestionar tu mascota actual.");
            manageMeta.lore(MessageUtils.parseList(manageLore));
            manageItem.setItemMeta(manageMeta);
            inv.setItem(15, manageItem);
        }

        player.openInventory(inv);
    }

    public void openPetTypes(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Tipos de Mascotas");

        // Lobo
        ItemStack wolfItem = new ItemStack(Material.BONE);
        ItemMeta wolfMeta = wolfItem.getItemMeta();
        wolfMeta.displayName(MessageUtils.parse("<yellow>Lobo"));
        List<String> wolfLore = new ArrayList<>();
        wolfLore.add("<gray>Habilidad: Da velocidad al dueño");
        wolfLore.add("<green>Click para spawnear.");
        wolfMeta.lore(MessageUtils.parseList(wolfLore));
        wolfItem.setItemMeta(wolfMeta);
        inv.setItem(10, wolfItem);

        // Gato
        ItemStack catItem = new ItemStack(Material.COD);
        ItemMeta catMeta = catItem.getItemMeta();
        catMeta.displayName(MessageUtils.parse("<yellow>Gato"));
        List<String> catLore = new ArrayList<>();
        catLore.add("<gray>Habilidad: Da invisibilidad al dueño");
        catLore.add("<green>Click para spawnear.");
        catMeta.lore(MessageUtils.parseList(catLore));
        catItem.setItemMeta(catMeta);
        inv.setItem(12, catItem);

        // Loro
        ItemStack parrotItem = new ItemStack(Material.FEATHER);
        ItemMeta parrotMeta = parrotItem.getItemMeta();
        parrotMeta.displayName(MessageUtils.parse("<yellow>Loro"));
        List<String> parrotLore = new ArrayList<>();
        parrotLore.add("<gray>Habilidad: Permite volar al dueño");
        parrotLore.add("<green>Click para spawnear.");
        parrotMeta.lore(MessageUtils.parseList(parrotLore));
        parrotItem.setItemMeta(parrotMeta);
        inv.setItem(14, parrotItem);

        // Caballo
        ItemStack horseItem = new ItemStack(Material.SADDLE);
        ItemMeta horseMeta = horseItem.getItemMeta();
        horseMeta.displayName(MessageUtils.parse("<yellow>Caballo"));
        List<String> horseLore = new ArrayList<>();
        horseLore.add("<gray>Habilidad: Montura rápida");
        horseLore.add("<green>Click para spawnear.");
        horseMeta.lore(MessageUtils.parseList(horseLore));
        horseItem.setItemMeta(horseMeta);
        inv.setItem(16, horseItem);

        // Caballo esqueleto
        ItemStack skeletonHorseItem = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta skeletonHorseMeta = skeletonHorseItem.getItemMeta();
        skeletonHorseMeta.displayName(MessageUtils.parse("<yellow>Caballo Esqueleto"));
        List<String> skeletonHorseLore = new ArrayList<>();
        skeletonHorseLore.add("<gray>Habilidad: Montura con salto mejorado");
        skeletonHorseLore.add("<green>Click para spawnear.");
        skeletonHorseMeta.lore(MessageUtils.parseList(skeletonHorseLore));
        skeletonHorseItem.setItemMeta(skeletonHorseMeta);
        inv.setItem(19, skeletonHorseItem);

        player.openInventory(inv);
    }

    public void openPetManage(Player player) {
        Optional<PetManager.Pet> petOptional = Optional.ofNullable(petManager.getPlayerPet(player));
        if (!petOptional.isPresent()) {
            MessageUtils.sendMessage(player, "<red>No tienes una mascota activa.");
            return;
        }

        PetManager.Pet pet = petOptional.get();
        Inventory inv = Bukkit.createInventory(null, 27, "Gestionar Mascota");

        // Item de información
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.displayName(MessageUtils.parse("<yellow>Información"));
        List<String> infoLore = new ArrayList<>();
        infoLore.add("<gray>Tipo: " + pet.getType());
        infoLore.add("<gray>Nombre: " + (pet.getName() != null ? pet.getName() : "Sin nombre"));
        infoLore.add("<gray>Habilidad: " + (pet.getSkillCooldown() > 0 ? "<red>En cooldown (" + pet.getSkillCooldown() + "s)" : "<green>Disponible"));
        infoMeta.lore(MessageUtils.parseList(infoLore));
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);

        // Item para nombrar
        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nameItem.getItemMeta();
        nameMeta.displayName(MessageUtils.parse("<aqua>Nombrar"));
        List<String> nameLore = new ArrayList<>();
        nameLore.add("<gray>Ponle un nombre a tu mascota.");
        nameMeta.lore(MessageUtils.parseList(nameLore));
        nameItem.setItemMeta(nameMeta);
        inv.setItem(11, nameItem);

        // Item para usar habilidad
        ItemStack skillItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta skillMeta = skillItem.getItemMeta();
        skillMeta.displayName(MessageUtils.parse("<green>Usar Habilidad"));
        List<String> skillLore = new ArrayList<>();
        skillLore.add("<gray>Usa la habilidad especial de tu mascota.");
        if (pet.getSkillCooldown() > 0) {
            skillLore.add("<red>Cooldown: " + pet.getSkillCooldown() + "s");
        }
        skillMeta.lore(MessageUtils.parseList(skillLore));
        skillItem.setItemMeta(skillMeta);
        inv.setItem(13, skillItem);

        // Item para despedir
        ItemStack dismissItem = new ItemStack(Material.BARRIER);
        ItemMeta dismissMeta = dismissItem.getItemMeta();
        dismissMeta.displayName(MessageUtils.parse("<red>Despedir"));
        List<String> dismissLore = new ArrayList<>();
        dismissLore.add("<gray>Despide a tu mascota.");
        dismissMeta.lore(MessageUtils.parseList(dismissLore));
        dismissItem.setItemMeta(dismissMeta);
        inv.setItem(15, dismissItem);

        player.openInventory(inv);
    }
}