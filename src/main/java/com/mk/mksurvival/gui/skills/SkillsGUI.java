package com.mk.mksurvival.gui.skills;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.skills.SkillManager;
import com.mk.mksurvival.managers.skills.PlayerSkill;
import com.mk.mksurvival.managers.skills.PlayerSkills;
import com.mk.mksurvival.managers.skills.SkillType;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SkillsGUI implements InventoryHolder {
    private final Player player;
    private final Inventory inventory;

    public SkillsGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.parse(("<gold>✦ Habilidades ✦")));
        initializeItems();
    }

    public SkillsGUI(MKSurvival plugin, Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, MessageUtils.parse(("<gold>✦ Habilidades ✦")));
        initializeItems();
    }

    private void initializeItems() {
        SkillManager skillManager = MKSurvival.getInstance().getSkillManager();
        PlayerSkills playerSkills = skillManager.getPlayerSkills(player);
        createPlayerInfoPanel(playerSkills);
        createSkillsPanel(playerSkills);
        createNavigationButtons();
    }

    private void createPlayerInfoPanel(PlayerSkills playerSkills) {
        int totalLevel = playerSkills.getTotalLevel();
        int totalExp = playerSkills.getTotalExperience();
        ItemStack playerInfo = createSkullItem(player);
        ItemMeta meta = playerInfo.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Nivel Total: <yellow>" + totalLevel)));
        lore.add(MessageUtils.parse(("<gray>Experiencia Total: <aqua>" + totalExp)));
        lore.add(MessageUtils.parse(("<gray>Progreso General: <green>" + getOverallProgress(playerSkills) + "%")));
        lore.add("");
        lore.add(MessageUtils.parse(("<gold>▶ Click para ver estadísticas detalladas")));
        meta.setLore(lore);
        playerInfo.setItemMeta(meta);
        inventory.setItem(4, playerInfo);
    }

    private void createSkillsPanel(PlayerSkills playerSkills) {
        int[] skillSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int skillIndex = 0;
        for (SkillType skill : SkillType.values()) {
            if (skillIndex >= skillSlots.length) break;
            ItemStack skillItem = createSkillItem(skill, playerSkills);
            inventory.setItem(skillSlots[skillIndex], skillItem);
            skillIndex++;
        }
    }

    private ItemStack createSkillItem(SkillType skill, PlayerSkills playerSkills) {
        Material material = getMaterialForSkill(skill);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        PlayerSkill playerSkill = playerSkills.getSkill(skill);
        int level = playerSkill.getLevel();
        double exp = playerSkill.getExp();
        double expNeeded = playerSkill.getExpNeeded();
        double progress = exp / expNeeded;
        meta.displayName(MessageUtils.parse("<aqua>⚡ " + formatSkillName(skill.name())));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Nivel: <yellow>" + level)));
        lore.add(MessageUtils.parse(("<gray>Experiencia: <green>" + (int)exp + " / " + (int)expNeeded)));
        lore.add("");
        lore.add(MessageUtils.parse(("<green>" + playerSkill.getProgressBar())));
        lore.add("");
        addSkillBonuses(lore, skill, level);
        lore.add("");
        lore.add(MessageUtils.parse("<gold>▶ Click para ver detalles"));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void addSkillBonuses(List<String> lore, SkillType skill, int level) {
        lore.add(MessageUtils.parse(("<yellow>✦ Bonificaciones:")));
        switch (skill) {
            case MINING:
                int doubleDropChance = (level / 10) * 10;
                lore.add(MessageUtils.parse(("  <gray>• Doble Drop: <green>" + doubleDropChance + "%")));
                if (level >= 20) lore.add(MessageUtils.parse(("  <gray>• Velocidad Minera: <green>+10%")));
                if (level >= 50) lore.add(MessageUtils.parse(("  <gray>• Chance de Diamante: <green>+5%")));
                break;
            case COMBAT:
                int damageBonus = (level / 5) * 5;
                lore.add(MessageUtils.parse(("  <gray>• Daño: <green>+" + damageBonus + "%")));
                if (level >= 15) lore.add(MessageUtils.parse(("  <gray>• Crítico: <green>+" + (level / 15) * 3 + "%")));
                if (level >= 30) lore.add(MessageUtils.parse(("  <gray>• Vampirismo: <green>+" + (level / 30) * 2 + "%")));
                break;
            case WOODCUTTING:
                int treeFellerChance = (level / 8) * 8;
                lore.add(MessageUtils.parse(("  <gray>• Tala Completa: <green>" + treeFellerChance + "%")));
                if (level >= 25) lore.add(MessageUtils.parse(("  <gray>• Drops Extra: <green>+15%")));
                break;
            case FISHING:
                int rareFishChance = (level / 5) * 5;
                lore.add(MessageUtils.parse(("  <gray>• Peces Raros: <green>+" + rareFishChance + "%")));
                if (level >= 20) lore.add(MessageUtils.parse(("  <gray>• Tesoros: <green>+" + (level / 20) * 3 + "%")));
                break;
            case FARMING:
                int cropYield = (level / 6) * 6;
                lore.add(MessageUtils.parse(("  <gray>• Rendimiento: <green>+" + cropYield + "%")));
                if (level >= 18) lore.add(MessageUtils.parse(("  <gray>• Crecimiento Rápido: <green>+10%")));
                break;
            case FORAGING:
                int rareFindChance = (level / 7) * 7;
                lore.add(MessageUtils.parse(("  <gray>• Hallazgos Raros: <green>+" + rareFindChance + "%")));
                break;
            case ALCHEMY:
                int potionEfficiency = (level / 10) * 10;
                lore.add(MessageUtils.parse(("  <gray>• Eficiencia: <green>+" + potionEfficiency + "%")));
                if (level >= 25) lore.add(MessageUtils.parse(("  <gray>• Duración: <green>+20%")));
                break;
            case ENCHANTING:
                int enchantBonus = (level / 8) * 8;
                lore.add(MessageUtils.parse(("  <gray>• Bonus Encantamientos: <green>+" + enchantBonus + "%")));
                if (level >= 22) lore.add(MessageUtils.parse(("  <gray>• Niveles Extra: <green>+1")));
                break;
        }
    }

    private void createNavigationButtons() {
        ItemStack statsButton = createButtonItem(Material.BOOK, MessageUtils.parse(("<gold>ⓘ Estadísticas")),
                MessageUtils.parse(("<gray>Ver estadísticas detalladas")));
        inventory.setItem(37, statsButton);
        ItemStack achievementsButton = createButtonItem(Material.GOLD_INGOT, MessageUtils.parse(("<gold>⭐ Logros")),
                MessageUtils.parse(("<gray>Ver logros desbloqueados")));
        inventory.setItem(39, achievementsButton);
        ItemStack helpButton = createButtonItem(Material.EMERALD, MessageUtils.parse(("<gold>❓ Ayuda")),
                MessageUtils.parse(("<gray>Ver información de habilidades")));
        inventory.setItem(41, helpButton);
        ItemStack closeButton = createButtonItem(Material.BARRIER, MessageUtils.parse(("<red>✕ Cerrar")),
                MessageUtils.parse(("<gray>Cerrar menú de habilidades")));
        inventory.setItem(49, closeButton);
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

    private String createProgressBar(double progress) {
        int bars = 20;
        int filledBars = (int) (progress * bars);
        StringBuilder bar = new StringBuilder();
        bar.append(MessageUtils.parse("<green>"));
        for (int i = 0; i < filledBars; i++) {
            bar.append("█");
        }
        bar.append(MessageUtils.parse("<gray>"));
        for (int i = filledBars; i < bars; i++) {
            bar.append("█");
        }
        return bar.toString() + " " + (int) (progress * 100) + "%";
    }

    private int getOverallProgress(PlayerSkills playerSkills) {
        return (int) (playerSkills.getOverallProgress() * 100);
    }

    private String formatSkillName(String skillName) {
        return skillName.charAt(0) + skillName.substring(1).toLowerCase().replace("_", " ");
    }

    private Material getMaterialForSkill(SkillType skill) {
        switch (skill) {
            case MINING:
                return Material.DIAMOND_PICKAXE;
            case WOODCUTTING:
                return Material.DIAMOND_AXE;
            case FISHING:
                return Material.FISHING_ROD;
            case COMBAT:
                return Material.DIAMOND_SWORD;
            case FARMING:
                return Material.DIAMOND_HOE;
            case FORAGING:
                return Material.OAK_LEAVES;
            case ALCHEMY:
                return Material.BREWING_STAND;
            case ENCHANTING:
                return Material.ENCHANTING_TABLE;
            default:
                return Material.BOOK;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
    
    private int getTotalAchievements() {
        // Implementar lógica para obtener el total de logros disponibles
        return 10; // Valor temporal
    }
    
    private int countUnlockedAchievements(PlayerSkills playerSkills) {
        // Implementar lógica para contar logros desbloqueados
        return 0; // Valor temporal
    }

    public static void open(Player player) {
        SkillsGUI gui = new SkillsGUI(player);
        player.openInventory(gui.getInventory());
        // Forzar actualización inmediata
        gui.updatePlayerExpBar(player);
    }

    // En el método handleClick de SkillsGUI.java

    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();

        // Botón de cerrar
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        // Botón de estadísticas
        if (slot == 37) {
            MessageUtils.sendMessage(player, "<gold>[Habilidades] === Estadísticas de Habilidades ===");
            PlayerSkills skills = MKSurvival.getInstance().getSkillManager().getPlayerSkills(player.getUniqueId());

            for (SkillType skill : SkillType.values()) {
                int level = skills.getLevel(skill);
                double exp = skills.getExp(skill);
                double expNeeded = skills.getExpNeeded(level);
                MessageUtils.sendMessage(player, "<gray>" + formatSkillName(skill.name()) + ": <yellow>Nivel " + level +
                        " (" + (int)exp + "/" + (int)expNeeded + " EXP)");
            }
            return;
        }

        // Botón de logros
        if (slot == 39) {
            showAchievements(player);
            return;
        }

        // Botón de ayuda
        if (slot == 41) {
            MessageUtils.sendMessage(player, "<gold>[Habilidades] === Sistema de Habilidades ===");
            MessageUtils.sendMessage(player, "<gray>Gana experiencia realizando actividades relacionadas:");
            MessageUtils.sendMessage(player, "<yellow>• Minería: Romper minerales con pico");
            MessageUtils.sendMessage(player, "<yellow>• Tala: Romper troncos con hacha");
            MessageUtils.sendMessage(player, "<yellow>• Pesca: Pescar con caña");
            MessageUtils.sendMessage(player, "<yellow>• Combate: Derrotar mobs");
            MessageUtils.sendMessage(player, "<yellow>• Agricultura: Cosechar cultivos con azada");
            MessageUtils.sendMessage(player, "<yellow>• Excavación: Cavar con pala");
            MessageUtils.sendMessage(player, "<yellow>• Alquimia: Beber pociones");
            MessageUtils.sendMessage(player, "<yellow>• Encantamiento: Encantar items");
            return;
        }

        // Slots de habilidades
        int[] skillSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int skillSlot : skillSlots) {
            if (slot == skillSlot) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    String skillName = clickedItem.getItemMeta().getDisplayName();
                    skillName = MessageUtils.stripColors(skillName).replace("⚡ ", "");

                    SkillType skillType = SkillType.valueOf(
                            skillName.toUpperCase().replace(" ", "_"));

                    PlayerSkills skills = MKSurvival.getInstance().getSkillManager().getPlayerSkills(player.getUniqueId());
                    int level = skills.getLevel(skillType);
                    double exp = skills.getExp(skillType);
                    double expNeeded = skills.getExpNeeded(level);

                    MessageUtils.sendMessage(player, "<green>[Habilidades] " + skillName);
                    MessageUtils.sendMessage(player, "<gray>Nivel: " + level);
                    MessageUtils.sendMessage(player, "<gray>Experiencia: " + (int)exp + "/" + (int)expNeeded);
                    MessageUtils.sendMessage(player, "<gray>Progreso: " + (int)((exp / expNeeded) * 100) + "%");

                    // Mostrar bonificaciones
                    showSkillBonuses(player, skillType, level);
                }
                return;
            }
        }
    }

    private void showSkillBonuses(Player player, SkillType skill, int level) {
        MessageUtils.sendMessage(player, "<yellow>Bonificaciones actuales:");

        switch (skill) {
            case MINING:
                if (level >= 10) {
                    int chance = (level / 10) * 10;
                    MessageUtils.sendMessage(player, "<gray>• " + chance + "% de chance de doble drop");
                }
                if (level >= 20) {
                    MessageUtils.sendMessage(player, "<gray>• +10% velocidad de minería");
                }
                if (level >= 50) {
                    MessageUtils.sendMessage(player, "<gray>• +5% chance de diamantes extra");
                }
                break;
            case COMBAT:
                if (level >= 5) {
                    int damage = (level / 5) * 5;
                    MessageUtils.sendMessage(player, "<gray>• +" + damage + "% de daño");
                }
                if (level >= 15) {
                    int crit = (level / 15) * 3;
                    MessageUtils.sendMessage(player, "<gray>• +" + crit + "% de chance de crítico");
                }
                if (level >= 30) {
                    int lifeSteal = (level / 30) * 2;
                    MessageUtils.sendMessage(player, "<gray>• +" + lifeSteal + "% de vampirismo");
                }
                break;
            case WOODCUTTING:
                if (level >= 8) {
                    int chance = (level / 8) * 8;
                    MessageUtils.sendMessage(player, "<gray>• " + chance + "% de chance de tala completa");
                }
                if (level >= 25) {
                    MessageUtils.sendMessage(player, "<gray>• +15% drops extra");
                }
                break;
            case FISHING:
                if (level >= 5) {
                    int chance = (level / 5) * 5;
                    MessageUtils.sendMessage(player, "<gray>• +" + chance + "% de peces raros");
                }
                if (level >= 20) {
                    int treasure = (level / 20) * 3;
                    MessageUtils.sendMessage(player, "<gray>• +" + treasure + "% de tesoros");
                }
                break;
            case FARMING:
                if (level >= 6) {
                    int yield = (level / 6) * 6;
                    MessageUtils.sendMessage(player, "<gray>• +" + yield + "% de rendimiento");
                }
                if (level >= 18) {
                    MessageUtils.sendMessage(player, "<gray>• +10% crecimiento rápido");
                }
                break;
            case FORAGING:
                if (level >= 7) {
                    int chance = (level / 7) * 7;
                    MessageUtils.sendMessage(player, "<gray>• +" + chance + "% de hallazgos raros");
                }
                break;
            case ALCHEMY:
                if (level >= 10) {
                    int efficiency = (level / 10) * 10;
                    MessageUtils.sendMessage(player, "<gray>• +" + efficiency + "% de eficiencia");
                }
                if (level >= 25) {
                    MessageUtils.sendMessage(player, "<gray>• +20% duración de pociones");
                }
                break;
            case ENCHANTING:
                if (level >= 8) {
                    int bonus = (level / 8) * 8;
                    MessageUtils.sendMessage(player, "<gray>• +" + bonus + "% bonus de encantamientos");
                }
                if (level >= 22) {
                    MessageUtils.sendMessage(player, "<gray>• +1 nivel extra de encantamiento");
                }
                break;
        }
    }
    public void updatePlayerExpBar(Player player) {
        SkillManager skillManager = MKSurvival.getInstance().getSkillManager();
        PlayerSkills playerSkills = skillManager.getPlayerSkills(player.getUniqueId());

        // Actualizar información del jugador
        int totalLevel = playerSkills.getTotalLevel();
        int totalExp = playerSkills.getTotalExperience();

        ItemStack playerInfo = createSkullItem(player);
        ItemMeta meta = playerInfo.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.parse(("<gray>Nivel Total: <yellow>" + totalLevel)));
        lore.add(MessageUtils.parse(("<gray>Experiencia Total: <aqua>" + totalExp)));
        lore.add(MessageUtils.parse(("<gray>Progreso General: <green>" + getOverallProgress(playerSkills) + "%")));
        lore.add("");
        lore.add(MessageUtils.parse(("<gold>▶ Click para ver estadísticas detalladas")));
        meta.setLore(lore);
        playerInfo.setItemMeta(meta);
        inventory.setItem(4, playerInfo);

        // Actualizar items de habilidades
        int[] skillSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int skillIndex = 0;
        for (SkillType skill : SkillType.values()) {
            if (skillIndex >= skillSlots.length) break;
            ItemStack skillItem = createSkillItem(skill, playerSkills);
            inventory.setItem(skillSlots[skillIndex], skillItem);
            skillIndex++;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    // ==================== MÉTODOS DE FUNCIONALIDAD ESPECÍFICA ====================

    private void showAchievements(Player player) {
        SkillManager skillManager = MKSurvival.getInstance().getSkillManager();
        PlayerSkills playerSkills = skillManager.getPlayerSkills(player.getUniqueId());
        
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════════");
        MessageUtils.sendMessage(player, "<gold>🏆 Logros de Habilidades");
        MessageUtils.sendMessage(player, "");
        
        // Logros generales
        int totalLevel = playerSkills.getTotalLevel();
        int totalExp = playerSkills.getTotalExperience();
        
        checkAndShowAchievement(player, "Novato", "Alcanza nivel 10 total", totalLevel >= 10);
        checkAndShowAchievement(player, "Experimentado", "Alcanza nivel 50 total", totalLevel >= 50);
        checkAndShowAchievement(player, "Veterano", "Alcanza nivel 100 total", totalLevel >= 100);
        checkAndShowAchievement(player, "Maestro", "Alcanza nivel 200 total", totalLevel >= 200);
        checkAndShowAchievement(player, "Leyenda", "Alcanza nivel 500 total", totalLevel >= 500);
        
        MessageUtils.sendMessage(player, "");
        
        // Logros por habilidad individual
        for (SkillType skill : SkillType.values()) {
            int level = playerSkills.getLevel(skill);
            String skillName = formatSkillName(skill.name());
            
            if (level >= 25) {
                checkAndShowAchievement(player, skillName + " Maestro", "Alcanza nivel 25 en " + skillName, true);
            }
            if (level >= 50) {
                checkAndShowAchievement(player, skillName + " Experto", "Alcanza nivel 50 en " + skillName, true);
            }
            if (level >= 100) {
                checkAndShowAchievement(player, skillName + " Leyenda", "Alcanza nivel 100 en " + skillName, true);
            }
        }
        
        MessageUtils.sendMessage(player, "");
        
        // Logros especiales
        checkAndShowAchievement(player, "Poliválente", "Alcanza nivel 20 en todas las habilidades", 
            isAllSkillsAtLevel(playerSkills, 20));
        checkAndShowAchievement(player, "Perfeccionista", "Alcanza nivel 50 en todas las habilidades", 
            isAllSkillsAtLevel(playerSkills, 50));
        checkAndShowAchievement(player, "Acumulador de Experiencia", "Gana 100,000 puntos de experiencia total", 
            totalExp >= 100000);
        checkAndShowAchievement(player, "Millón de Experiencia", "Gana 1,000,000 puntos de experiencia total", 
            totalExp >= 1000000);
        
        MessageUtils.sendMessage(player, "");
        
        // Estadísticas generales
        MessageUtils.sendMessage(player, "<yellow>Estadísticas Generales:");
        MessageUtils.sendMessage(player, "<gray>• Nivel Total: <yellow>" + totalLevel);
        MessageUtils.sendMessage(player, "<gray>• Experiencia Total: <aqua>" + totalExp);
        MessageUtils.sendMessage(player, "<gray>• Progreso General: <green>" + getOverallProgress(playerSkills) + "%");
        
        int achievementsUnlocked = countUnlockedAchievements(playerSkills);
        int totalAchievements = getTotalAchievements();
        MessageUtils.sendMessage(player, "<gray>• Logros Desbloqueados: <light_purple>" + achievementsUnlocked + "/" + totalAchievements);
        
        MessageUtils.sendMessage(player, "<gold>═══════════════════════════════════");
    }
    
    private void checkAndShowAchievement(Player player, String name, String description, boolean unlocked) {
        String status = unlocked ? "<green>✓" : "<red>✗";
        String color = unlocked ? "<green>" : "<gray>";
        MessageUtils.sendMessage(player, status + " " + color + name + ": <gray>" + description);
    }
    
    private boolean isAllSkillsAtLevel(PlayerSkills playerSkills, int minLevel) {
        for (SkillType skill : SkillType.values()) {
            if (playerSkills.getLevel(skill) < minLevel) {
                return false;
            }
        }
        return true;
    }
    
}