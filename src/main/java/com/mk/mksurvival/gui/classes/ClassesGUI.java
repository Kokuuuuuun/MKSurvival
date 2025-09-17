package com.mk.mksurvival.gui.classes;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.classes.ClassManager;
import com.mk.mksurvival.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.List;

public class ClassesGUI implements InventoryHolder {
    private final Player player;
    private final Inventory inventory;

    public ClassesGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.parse(("<gold>Clases de Supervivencia")));
        initializeItems();
    }

    private void initializeItems() {
        ClassManager classManager = MKSurvival.getInstance().getClassManager();
        ClassManager.PlayerClass playerClass = classManager.getPlayerClass(player);
        createPlayerInfoPanel(playerClass);
        createAvailableClassesPanel(classManager, playerClass);
        createClassAbilitiesPanel(playerClass);
        createNavigationButtons();
    }

    private void createPlayerInfoPanel(ClassManager.PlayerClass playerClass) {
        ItemStack playerInfo = createSkullItem(player);
        ItemMeta meta = playerInfo.getItemMeta();
        ClassManager.GameClass gameClass = playerClass.getGameClass();
        meta.displayName(MessageUtils.parse("<green>" + gameClass.getName() + "</green>"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>" + playerClass.getGameClass().getDescription() + "</gray>")));
        lore.add("");
        lore.add(MessageUtils.parse(("<yellow>Nivel: " + playerClass.getLevel() + "</yellow>")));
        lore.add(MessageUtils.parse(("<green>Experiencia: " + playerClass.getExperience() + "/" + (playerClass.getLevel() * 1000) + "</green>")));
        lore.add("");
        lore.add(MessageUtils.parse("<gold>▶ Click para ver estadísticas</gold>"));
        meta.lore(lore);
        playerInfo.setItemMeta(meta);
        inventory.setItem(4, playerInfo);
    }

    private void createAvailableClassesPanel(ClassManager classManager, ClassManager.PlayerClass playerClass) {
        int[] classSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 24};
        int classIndex = 0;
        for (ClassManager.GameClass gameClass : classManager.getAvailableClasses().values()) {
            if (classIndex >= classSlots.length) break;
            if (gameClass.getId().equals(playerClass.getGameClass().getId())) continue;
            ItemStack classItem = createClassItem(gameClass);
            inventory.setItem(classSlots[classIndex], classItem);
            classIndex++;
        }
        while (classIndex < classSlots.length) {
            ItemStack emptySlot = createEmptyClassSlot();
            inventory.setItem(classSlots[classIndex], emptySlot);
            classIndex++;
        }
    }

    private ItemStack createClassItem(ClassManager.GameClass gameClass) {
        Material material = getClassMaterial(gameClass);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<green>" + gameClass.getName() + "</green>"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>" + gameClass.getDescription() + "</gray>")));
        lore.add("");
        lore.add(MessageUtils.parse(("<yellow>Nivel: 1</yellow>")));
        for (String ability : gameClass.getAbilities()) {
            lore.add(MessageUtils.parse(("  <gray>• " + ability + "</gray>")));
        }
        lore.add("");
        lore.add(MessageUtils.parse(("<yellow>✦ Efectos:</yellow>")));
        for (String effect : gameClass.getEffects()) {
            lore.add(MessageUtils.parse(("  <green>• " + formatEffect(effect) + "</green>")));
        }
        lore.add("");
        lore.add(MessageUtils.parse("<green>▶ Click para seleccionar</green>"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEmptyClassSlot() {
        ItemStack item = new ItemStack(Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse("<gray>Espacio de Clase Vacío</gray>"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Alcanza nivel requerido</gray>")));
        lore.add(MessageUtils.parse("<gray>para desbloquear más clases</gray>"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void createClassAbilitiesPanel(ClassManager.PlayerClass playerClass) {
        ClassManager.GameClass gameClass = playerClass.getGameClass();
        ItemStack abilitiesPanel = new ItemStack(Material.BOOK);
        ItemMeta meta = abilitiesPanel.getItemMeta();
        meta.displayName(MessageUtils.parse("<green>" + gameClass.getName() + "</green>"));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Habilidades Activas:</gray>")));
        for (String ability : gameClass.getAbilities()) {
            lore.add(MessageUtils.parse(("  <green>• " + ability + "</green>")));
        }
        lore.add("");
        lore.add(MessageUtils.parse(("<gray>Efectos Activos:</gray>")));
        for (String effect : gameClass.getEffects()) {
            lore.add(MessageUtils.parse(("  <green>• " + formatEffect(effect) + "</green>")));
        }
        lore.add("");
        lore.add("");
        lore.add(MessageUtils.parse(("<aqua>Experiencia: " + playerClass.getExperience() + "/" + (playerClass.getLevel() * 1000) + "</aqua>")));
        lore.add("");
        lore.add(MessageUtils.parse("<gold>▶ Click para ver detalles</gold>"));
        meta.lore(lore);
        abilitiesPanel.setItemMeta(meta);
        inventory.setItem(49, abilitiesPanel);
    }

    private void createNavigationButtons() {
        ItemStack upgradeButton = createButtonItem(Material.EXPERIENCE_BOTTLE, MessageUtils.parse(("<gold>⬆ Mejorar</gold>")),
                MessageUtils.parse(("<gray>Mejorar habilidades de clase</gray>")));
        inventory.setItem(1, upgradeButton);
        ItemStack resetButton = createButtonItem(Material.TNT, MessageUtils.parse(("<gold>💥 Resetear</gold>")),
                MessageUtils.parse(("<gray>Resetear puntos de habilidad</gray>")));
        inventory.setItem(7, resetButton);
        ItemStack infoButton = createButtonItem(Material.BOOK, MessageUtils.parse(("<gold>❓ Información</gold>")),
                MessageUtils.parse(("<gray>Ver información detallada</gray>")));
        inventory.setItem(46, infoButton);
        ItemStack closeButton = createButtonItem(Material.BARRIER, MessageUtils.parse(("<red>✕ Cerrar</red>")),
                MessageUtils.parse(("<gray>Cerrar menú de clases</gray>")));
        inventory.setItem(53, closeButton);
    }

    private ItemStack createButtonItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.parse(name));
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.lore(MessageUtils.parseList(loreList));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSkullItem(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        try {
            if (skull.getItemMeta() instanceof SkullMeta) {
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwningPlayer(player);
                skull.setItemMeta(meta);
            }
        } catch (NoSuchMethodError e) {
            ItemMeta meta = skull.getItemMeta();
            meta.displayName(MessageUtils.parse(player.getName()));
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private Material getClassMaterial(ClassManager.GameClass gameClass) {
        switch (gameClass.getId()) {
            case "warrior": return Material.DIAMOND_SWORD;
            case "archer": return Material.BOW;
            case "mage": return Material.STICK;
            case "adventurer": return Material.COMPASS;
            default: return Material.BOOK;
        }
    }

    private int calculateClassLevel(ClassManager.PlayerClass playerClass) {
        int totalLevel = 0;
        for (String ability : playerClass.getGameClass().getAbilities()) {
            totalLevel += 5;
        }
        for (String effect : playerClass.getGameClass().getEffects()) {
            totalLevel += 3;
        }
        return totalLevel;
    }

    private int calculateClassExp(ClassManager.PlayerClass playerClass) {
        int totalExp = 0;
        for (String ability : playerClass.getGameClass().getAbilities()) {
            totalExp += 100;
        }
        for (String effect : playerClass.getGameClass().getEffects()) {
            totalExp += 50;
        }
        return totalExp;
    }

    private String formatEffect(String effectStr) {
        String[] parts = effectStr.split(":");
        if (parts.length >= 2) {
            String effectName = parts[0];
            int amplifier = Integer.parseInt(parts[1]);
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type != null) {
                return effectName + " " + (amplifier + 1);
            }
        }
        return effectStr;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void open(Player player) {
        ClassesGUI gui = new ClassesGUI(player);
        player.openInventory(gui.getInventory());
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();

    // Botón de cerrar
    if (slot == 53) {
        player.closeInventory();
        return;
    }

    // Botón de mejorar
    if (slot == 1) {
        upgradeClass(player);
        return;
    }

    // Botón de resetear
    if (slot == 7) {
        resetClass(player);
        return;
    }

    // Botón de información
    if (slot == 46) {
        showClassInformation(player);
        return;
    }

    // Botón de habilidades
    if (slot == 49) {
        showClassAbilities(player);
        return;
    }

    // Slots de clases disponibles
    int[] classSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 24};
    for (int classSlot : classSlots) {
        if (slot == classSlot) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String className = clickedItem.getItemMeta().getDisplayName();
                // Extraer el nombre de la clase (quitando el formato MiniMessage)
                className = className.replaceAll("<[^>]*>", "").replace("⚡ ", "");
                // Seleccionar la clase
                MKSurvival.getInstance().getClassManager().setPlayerClass(player, className.toLowerCase().replace(" ", "_"));
                MessageUtils.sendMessage(player, "<green>[Clases] Has seleccionado la clase: " + className + "</green>");
                player.closeInventory();
            }
            return;
        }
    }
    }

    // ==================== MÉTODOS DE FUNCIONALIDAD ESPECÍFICA ====================

    private void upgradeClass(Player player) {
        ClassManager classManager = MKSurvival.getInstance().getClassManager();
        ClassManager.PlayerClass playerClass = classManager.getPlayerClass(player);
        
        if (playerClass == null || playerClass.getGameClass() == null) {
            MessageUtils.sendMessage(player, "<red>[Clases] No tienes una clase seleccionada.</red>");
            return;
        }
        
        // Verificar si el jugador tiene suficiente dinero para mejorar
        double upgradeCost = calculateUpgradeCost(playerClass.getGameClass().getId());
        
        if (!MKSurvival.getInstance().getEconomyManager().hasBalance(player, upgradeCost)) {
            MessageUtils.sendMessage(player, "<red>[Clases] No tienes suficiente dinero para mejorar tu clase.</red>");
            return;
        }
        
        // Aplicar mejora
        classManager.upgradeClass(player);
        MKSurvival.getInstance().getEconomyManager().withdrawBalance(player, upgradeCost);
        
        // Notificar al jugador
        MessageUtils.sendMessage(player, "<green>[Clases] ¡Has mejorado tu clase " + playerClass.getGameClass().getName() + "!</green>");
        MessageUtils.sendMessage(player, "<green>[Clases] Nuevas habilidades y efectos aplicados.</green>");
        player.closeInventory();
    }
    
    private void resetClass(Player player) {
        ClassManager classManager = MKSurvival.getInstance().getClassManager();
        
        // Confirmar con el jugador
        MessageUtils.sendMessage(player, "<yellow>[Clases] <bold>ATENCIÓN:</bold> <reset><yellow>Esto eliminará tu clase actual.</yellow>");
        MessageUtils.sendMessage(player, "<yellow>[Clases] Para confirmar, escribe: /class reset confirm</yellow>");
        player.closeInventory();
    }
    
    private void showClassInformation(Player player) {
        ClassManager classManager = MKSurvival.getInstance().getClassManager();
        ClassManager.PlayerClass playerClass = classManager.getPlayerClass(player);
        
        if (playerClass == null || playerClass.getGameClass() == null) {
            MessageUtils.sendMessage(player, "<red>[Clases] No tienes una clase seleccionada.</red>");
            return;
        }
        
        ClassManager.GameClass gameClass = playerClass.getGameClass();
        
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
        MessageUtils.sendMessage(player, "<gold>📖 Información de Clase</gold>");
        MessageUtils.sendMessage(player, "<gray>Clase: <yellow>" + gameClass.getName() + "</yellow></gray>");
        MessageUtils.sendMessage(player, "<gray>Descripción: <white>" + gameClass.getDescription() + "</white></gray>");
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "<gray>Habilidades:</gray>");
        for (String ability : gameClass.getAbilities()) {
            MessageUtils.sendMessage(player, "<yellow>• " + ability + "</yellow>");
        }
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "<gray>Efectos activos:</gray>");
        for (String effect : gameClass.getEffects()) {
            String[] parts = effect.split(":");
            if (parts.length >= 2) {
                String effectName = formatEffectName(parts[0]);
                String level = "Nivel " + (Integer.parseInt(parts[1]) + 1);
                MessageUtils.sendMessage(player, "<aqua>• " + effectName + " " + level + "</aqua>");
            }
        }
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
    }
    
    private void showClassAbilities(Player player) {
        ClassManager classManager = MKSurvival.getInstance().getClassManager();
        ClassManager.PlayerClass playerClass = classManager.getPlayerClass(player);
        
        if (playerClass == null || playerClass.getGameClass() == null) {
            MessageUtils.sendMessage(player, "<red>[Clases] No tienes una clase seleccionada.</red>");
            return;
        }
        
        ClassManager.GameClass gameClass = playerClass.getGameClass();
        
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
        MessageUtils.sendMessage(player, "<gold>⚡ Habilidades de " + gameClass.getName() + "</gold>");
        MessageUtils.sendMessage(player, "");
        
        List<String> abilities = gameClass.getAbilities();
        if (abilities.isEmpty()) {
            MessageUtils.sendMessage(player, "<gray>Esta clase no tiene habilidades especiales.</gray>");
        } else {
            for (int i = 0; i < abilities.size(); i++) {
                MessageUtils.sendMessage(player, "<yellow>" + (i + 1) + ". <white>" + abilities.get(i) + "</white></yellow>");
            }
        }
        
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "<gray>Comandos disponibles:</gray>");
        MessageUtils.sendMessage(player, "<yellow>/class upgrade <gray>- Mejorar clase</gray></yellow>");
        MessageUtils.sendMessage(player, "<yellow>/class reset <gray>- Resetear clase</gray></yellow>");
        MessageUtils.sendMessage(player, "<yellow>/class info <gray>- Ver información</gray></yellow>");
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════</gold>");
    }
    
    private double calculateUpgradeCost(String classId) {
        // Costo base basado en el tipo de clase
        switch (classId.toLowerCase()) {
            case "warrior":
            case "guerrero":
                return 5000.0;
            case "archer":
            case "arquero":
                return 4500.0;
            case "mage":
            case "mago":
                return 6000.0;
            case "rogue":
            case "ladron":
                return 4000.0;
            case "paladin":
                return 7000.0;
            case "necromancer":
            case "nigromante":
                return 8000.0;
            default:
                return 3000.0; // adventurer o clases básicas
        }
    }
    
    private void applyClassUpgrade(Player player, ClassManager.GameClass gameClass) {
        // Aplicar efectos mejorados según la clase
        for (String effectStr : gameClass.getEffects()) {
            String[] parts = effectStr.split(":");
            if (parts.length == 3) {
                org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(parts[0]);
                int amplifier = Integer.parseInt(parts[1]) + 1; // Incrementar el nivel
                int duration = Integer.parseInt(parts[2]);
                
                if (type != null) {
                    // Aplicar efecto permanente mejorado
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration > 0 ? duration * 20 : 999999, amplifier));
                }
            }
        }
    }
    
    private String formatEffectName(String effectName) {
        switch (effectName.toLowerCase()) {
            case "speed":
                return "Velocidad";
            case "strength":
                return "Fuerza";
            case "resistance":
                return "Resistencia";
            case "jump_boost":
                return "Salto Mejorado";
            case "night_vision":
                return "Visión Nocturna";
            case "water_breathing":
                return "Respiración Acuática";
            case "fire_resistance":
                return "Resistencia al Fuego";
            case "invisibility":
                return "Invisibilidad";
            case "regeneration":
                return "Regeneración";
            case "health_boost":
                return "Vida Extra";
            case "absorption":
                return "Absorción";
            case "saturation":
                return "Saturación";
            default:
                return effectName;
        }
    }
}