package com.mk.mksurvival.managers.quests;

import com.mk.mksurvival.MKSurvival;
import com.mk.mksurvival.managers.quests.PlayerQuest;
import com.mk.mksurvival.managers.quests.Quest;
import com.mk.mksurvival.managers.quests.QuestType;
import com.mk.mksurvival.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class QuestManager {
    private final MKSurvival plugin;
    private final HashMap<UUID, List<PlayerQuest>> playerQuests = new HashMap<>();
    private final HashMap<String, Quest> availableQuests = new HashMap<>();
    private FileConfiguration questsConfig;

    public QuestManager(MKSurvival plugin) {
        this.plugin = plugin;
        this.questsConfig = plugin.getConfigManager().getQuestsConfig();
        loadQuests();
        loadAllPlayerData();
    }

    private void loadQuests() {
        ConfigurationSection questsSection = questsConfig.getConfigurationSection("quests");
        if (questsSection == null) {
            plugin.getLogger().warning("No quests found in quests.yml");
            return;
        }

        for (String questId : questsSection.getKeys(false)) {
            String path = "quests." + questId + ".";
            String name = questsConfig.getString(path + "name", "Quest");
            String description = questsConfig.getString(path + "description", "No description");
            QuestType type = QuestType.fromString(questsConfig.getString(path + "type", "CUSTOM"));
            HashMap<String, Integer> requirements = new HashMap<>();
            ConfigurationSection reqSection = questsConfig.getConfigurationSection(path + "requirements");
            if (reqSection != null) {
                for (String req : reqSection.getKeys(false)) {
                    requirements.put(req, questsConfig.getInt(path + "requirements." + req));
                }
            }
            double moneyReward = questsConfig.getDouble(path + "rewards.money", 0.0);
            int expReward = questsConfig.getInt(path + "rewards.exp", 0);

            List<ItemStack> itemRewards = new ArrayList<>();
            // Cargar items como recompensas desde la configuración
            ConfigurationSection itemsSection = questsConfig.getConfigurationSection(path + "rewards.items");
            if (itemsSection != null) {
                for (String itemKey : itemsSection.getKeys(false)) {
                    String itemPath = path + "rewards.items." + itemKey + ".";
                    try {
                        String materialName = questsConfig.getString(itemPath + "material", "STONE");
                        int amount = questsConfig.getInt(itemPath + "amount", 1);
                        String displayName = questsConfig.getString(itemPath + "display_name");
                        List<String> lore = questsConfig.getStringList(itemPath + "lore");
                        
                        Material material = Material.getMaterial(materialName.toUpperCase());
                        if (material != null) {
                            ItemStack itemReward = new ItemStack(material, amount);
                            ItemMeta meta = itemReward.getItemMeta();
                            
                            if (meta != null) {
                                if (displayName != null && !displayName.isEmpty()) {
                                    meta.displayName(MessageUtils.parse(displayName));
                                }
                                if (!lore.isEmpty()) {
                                    List<String> coloredLore = new ArrayList<>();
                                    for (String loreLine : lore) {
                                        coloredLore.add(String.valueOf(MessageUtils.parse((loreLine))));
                                    }
                                    meta.lore(MessageUtils.parseList(coloredLore));
                                }
                                itemReward.setItemMeta(meta);
                            }
                            
                            itemRewards.add(itemReward);
                        } else {
                            plugin.getLogger().warning("Material inválido en misión " + questId + ": " + materialName);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error cargando item reward para misión " + questId + ": " + e.getMessage());
                    }
                }
            }

            List<String> commandRewards = questsConfig.getStringList(path + "rewards.commands");
            Quest quest = new Quest(questId, name, description, requirements, moneyReward, itemRewards, expReward, commandRewards);
            availableQuests.put(questId, quest);
        }
    }

    public List<PlayerQuest> getPlayerQuests(Player player) {
        return playerQuests.computeIfAbsent(player.getUniqueId(), uuid -> new ArrayList<>());
    }

    public void addQuest(Player player, String questId) {
        Quest quest = availableQuests.get(questId);
        if (quest != null) {
            List<PlayerQuest> quests = getPlayerQuests(player);
            // Check if player already has this quest
            for (PlayerQuest playerQuest : quests) {
                if (playerQuest.getQuest().getId().equals(questId)) {
                    MessageUtils.sendMessage(player, "<red>[Quests] Ya tienes esta misión activa.</red>");
                    return;
                }
            }
            if (quests.size() >= plugin.getConfig().getInt("quests.max_active_quests", 5)) {
                MessageUtils.sendMessage(player, "<red>[Quests] Has alcanzado el límite de misiones activas.</red>");
                return;
            }
            quests.add(new PlayerQuest(quest));

            String message = String.valueOf(MessageUtils.parse(
                    "<green>[Quests] Has aceptado la misión: </green>" + quest.getName()
            ));
            MessageUtils.sendMessage(player, message);

            // Mostrar detalles de la misión
            showQuestDetails(player, quest);
        } else {
            MessageUtils.sendMessage(player, "<red>[Quests] La misión no existe.</red>");
        }
    }

    public void checkQuestCompletion(Player player, String material, int amount) {
        List<PlayerQuest> quests = getPlayerQuests(player);
        for (PlayerQuest playerQuest : quests) {
            if (playerQuest.isCompleted()) continue;
            if (playerQuest.addProgress(material, amount)) {
                completeQuest(player, playerQuest);
            }
        }
    }

    private void completeQuest(Player player, PlayerQuest playerQuest) {
        Quest quest = playerQuest.getQuest();
        playerQuest.setCompleted(true);
        playerQuest.setCompletionTime(LocalDateTime.now());

        // Mensaje de misión completada
        Component message = MessageUtils.parse(
                "<green>[Quests] ¡Misión completada: </green>" + quest.getName() + "<green>!</green>"
        );
        MessageUtils.sendMessage(player, message);

        // Dar recompensas monetarias
        if (quest.getMoneyReward() > 0) {
            plugin.getEconomyManager().addBalance(player, quest.getMoneyReward());
            MessageUtils.sendMessage(player,
                    "<yellow>[Quests] Recibiste: " +
                            plugin.getEconomyManager().formatCurrency(quest.getMoneyReward()) +
                            "</yellow>"
            );
        }

        // Dar recompensas de experiencia
        if (quest.getExpReward() > 0) {
            player.giveExp(quest.getExpReward());
            MessageUtils.sendMessage(player,
                    "<green>[Quests] Recibiste: " + quest.getExpReward() + " puntos de experiencia</green>"
            );
        }

        // Dar recompensas de items
        for (ItemStack reward : quest.getItemRewards()) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(reward);
            if (!leftover.isEmpty()) {
                // Si el inventario está lleno, dejar caer los items en el suelo
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
                MessageUtils.sendMessage(player,
                        "<yellow>[Quests] Tu inventario está lleno. Algunos items han caído al suelo.</yellow>"
                );
            }
        }

        // Ejecutar comandos de recompensa
        for (String command : quest.getCommands()) {
            String processedCommand = command.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
        }
    }

    public void saveAllPlayerData() {
        for (UUID uuid : playerQuests.keySet()) {
            List<PlayerQuest> quests = playerQuests.get(uuid);
            String path = "players." + uuid.toString() + ".quests.";
            for (int i = 0; i < quests.size(); i++) {
                PlayerQuest playerQuest = quests.get(i);
                String questPath = path + i + ".";
                questsConfig.set(questPath + "id", playerQuest.getQuest().getId());
                questsConfig.set(questPath + "completed", playerQuest.isCompleted());
                // Save progress
                for (String material : playerQuest.getProgress().keySet()) {
                    questsConfig.set(questPath + "progress." + material, playerQuest.getProgress().get(material));
                }
            }
        }
        plugin.getConfigManager().saveQuestsConfig();
    }

    private void loadAllPlayerData() {
        // Load data for online players if server reloaded
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            List<PlayerQuest> quests = new ArrayList<>();
            String path = "players." + uuid.toString() + ".quests.";
            if (questsConfig.contains(path)) {
                for (String questIndex : questsConfig.getConfigurationSection(path).getKeys(false)) {
                    String questPath = path + questIndex + ".";
                    String questId = questsConfig.getString(questPath + "id");
                    boolean completed = questsConfig.getBoolean(questPath + "completed");
                    Quest quest = availableQuests.get(questId);
                    if (quest != null) {
                        PlayerQuest playerQuest = new PlayerQuest(quest);
                        playerQuest.setCompleted(completed);
                        // Load progress
                        if (questsConfig.contains(questPath + "progress")) {
                            for (String material : questsConfig.getConfigurationSection(questPath + "progress").getKeys(false)) {
                                int progress = questsConfig.getInt(questPath + "progress." + material);
                                playerQuest.getProgress().put(material, progress);
                            }
                        }
                        quests.add(playerQuest);
                    }
                }
            }
            playerQuests.put(uuid, quests);
        }
    }

    public HashMap<String, Quest> getAvailableQuests() {
        return availableQuests;
    }

    /**
     * Muestra los detalles de una misión a un jugador
     * @param player El jugador
     * @param quest La misión
     */
    public void showQuestDetails(Player player, Quest quest) {
        MessageUtils.sendMessage(player, "<gold>==== Detalles de la Misión ====</gold>");
        MessageUtils.sendMessage(player, quest.getFormattedName());
        MessageUtils.sendMessage(player, quest.getFormattedDescription());

        // Para mostrar el progreso, necesitamos obtener la PlayerQuest si existe
        PlayerQuest playerQuest = null;
        for (PlayerQuest pq : getPlayerQuests(player)) {
            if (pq.getQuest().getId().equals(quest.getId())) {
                playerQuest = pq;
                break;
            }
        }

        HashMap<String, Integer> progress = new HashMap<>();
        if (playerQuest != null) {
            progress = playerQuest.getProgress();
        }

        MessageUtils.sendMessage(player, quest.getFormattedRequirements(player, progress));
        MessageUtils.sendMessage(player, quest.getFormattedRewards());
        MessageUtils.sendMessage(player, "<gold>========================</gold>");
    }

    /**
     * Muestra todas las misiones activas de un jugador
     * @param player El jugador
     */
    public void showActiveQuests(Player player) {
        List<PlayerQuest> quests = getPlayerQuests(player);

        if (quests.isEmpty()) {
            MessageUtils.sendMessage(player, "<yellow>No tienes misiones activas.</yellow>");
            return;
        }

        MessageUtils.sendMessage(player, "<gold>==== Tus Misiones Activas ====</gold>");
        for (PlayerQuest quest : quests) {
            String status = quest.isCompleted() ? "<green>[Completada]</green> " : "<yellow>[" + quest.getProgressPercentage() + "%]</yellow> ";
            String message = String.valueOf(MessageUtils.parse(status + quest.getQuest().getName()));
            MessageUtils.sendMessage(player, message);
        }
        MessageUtils.sendMessage(player, "<gold>========================</gold>");
    }

    /**
     * Muestra todas las misiones disponibles a un jugador
     * @param player El jugador
     */
    public void showAvailableQuests(Player player) {
        if (availableQuests.isEmpty()) {
            MessageUtils.sendMessage(player, "<yellow>No hay misiones disponibles.</yellow>");
            return;
        }

        MessageUtils.sendMessage(player, "<gold>==== Misiones Disponibles ====</gold>");
        for (Quest quest : availableQuests.values()) {
            // Verificar si el jugador ya tiene esta misión
            boolean hasQuest = false;
            for (PlayerQuest playerQuest : getPlayerQuests(player)) {
                if (playerQuest.getQuest().getId().equals(quest.getId())) {
                    hasQuest = true;
                    break;
                }
            }

            String status = hasQuest ? "<gray>[Ya aceptada]</gray> " : "<green>[Disponible]</green> ";
            String message = String.valueOf(MessageUtils.parse(status + quest.getName()));
            MessageUtils.sendMessage(player, message);
        }
        MessageUtils.sendMessage(player, "<gold>========================</gold>");
    }
}