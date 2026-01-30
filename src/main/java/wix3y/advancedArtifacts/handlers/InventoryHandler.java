package wix3y.advancedArtifacts.handlers;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import wix3y.advancedArtifacts.AdvancedArtifacts;
import wix3y.advancedArtifacts.gui.ArtifactInventory;
import wix3y.advancedArtifacts.util.ConfigUtil;
import wix3y.advancedArtifacts.util.DatabaseManager;
import wix3y.advancedArtifacts.util.DirtyString;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryHandler implements Listener {
    protected final AdvancedArtifacts plugin;
    protected final Map<String, Map<String, DirtyString>> playerDataCache = new ConcurrentHashMap<>();
    private final DatabaseManager databaseManager;
    private ConfigUtil configUtil;
    private final List<String> columns;
    private final String table;
    private final NamespacedKey uniqueID;
    private final int inventorySize;

    public InventoryHandler(AdvancedArtifacts plugin, DatabaseManager databaseManager, ConfigUtil configUtil, List<String> columns, String table, NamespacedKey uniqueID, int inventorySize) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.configUtil = configUtil;
        this.table = table;
        this.columns = columns;
        this.uniqueID = uniqueID;
        this.inventorySize = inventorySize;
    }

    /**
     * Reload the config util
     *
     * @param configUtil the new config util
     */
    public void reloadConfigUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    /**
     * Handle clicking in artifact inventory
     *
     * @param event inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder(false);

        if (!(holder instanceof ArtifactInventory artifactInventory)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory playerInventory = player.getInventory();

        if (slot < inventorySize) {
            // Click in artifact gui
            // move clicked item to player inventory
            int emptySlot = playerInventory.firstEmpty();
            if (emptySlot == -1) {
                // no empty slot exists
                return;
            }
            playerInventory.setItem(emptySlot, clickedItem);
            inventory.setItem(slot, null);

            Player targetPlayer = artifactInventory.getTarget();
            String uuid = targetPlayer.getUniqueId().toString();
            if (!playerDataCache.containsKey(uuid)) {
                plugin.getLogger().warning("Player " + targetPlayer.getName() + " does not exist in cache, adding player...");
                addPlayerToCache(uuid);
            }
            if (playerDataCache.get(uuid).containsKey("slot_" + slot)) {
                DirtyString str = playerDataCache.get(uuid).get("slot_" + slot);
                str.updateStr(null);
                str.setDirty(true);
            }
            else {
                plugin.getLogger().severe("Failed remove item " + clickedItem + " from artifact inventory for player " + targetPlayer.getName());
            }
        }

        else {
            // Click in player inventory
            if (!(clickedItem.hasItemMeta() && clickedItem.getItemMeta().getPersistentDataContainer().has(uniqueID))) {
                // clicked item is not an artifact
                return;
            }
            // move clicked item from player inventory to artifact inventory
            int emptySlot = inventory.firstEmpty();
            if (emptySlot == -1) {
                // no empty slot exists
                return;
            }
            inventory.setItem(emptySlot, clickedItem);
            playerInventory.setItem(event.getSlot(), null);

            Player targetPlayer = artifactInventory.getTarget();
            String uuid = targetPlayer.getUniqueId().toString();
            if (!playerDataCache.containsKey(uuid)) {
                plugin.getLogger().warning("Player " + targetPlayer.getName() + " does not exist in cache, adding player...");
                addPlayerToCache(uuid);
            }
            if (playerDataCache.get(uuid).containsKey("slot_" + emptySlot)) {
                DirtyString str = playerDataCache.get(uuid).get("slot_" + emptySlot);
                str.updateStr(configUtil.getArtifactID(clickedItem));
                str.setDirty(true);
            }
            else {
                plugin.getLogger().severe("Failed save item " + clickedItem + " to artifact inventory for player " + targetPlayer.getName());
            }
        }
    }

    /**
     * Add player data from MySQL to cache
     *
     * @param uuid uuid of the player
     */
    public void addPlayerToCache(String uuid) {
        Map<String, DirtyString> data = databaseManager.getPlayerData(uuid, columns, table);
        playerDataCache.put(uuid, data);
    }

    /**
     * Write player data from cache to MySQL for all players in cache
     *
     */
    public void writeToDatabase() {
        for (String uuid: playerDataCache.keySet()) {
            writeToDatabase(uuid);
        }
    }

    /**
     * Write player data from cache to MySQL
     *
     * @param uuid uuid of the player
     */
    public void writeToDatabase(String uuid) {
        Map<String, DirtyString> dataCache = playerDataCache.get(uuid);
        for (String col: dataCache.keySet()) {
            DirtyString str = dataCache.get(col);
            if (str.isDirty()) {
                str.setDirty(false);
                // Asynchronous execution means newer data may be written to the database possibly resulting
                // in the exact same data being written during the next execution of this method
                databaseManager.writeString(table, uuid, col, str.getStr());
            }
        }
    }

    /**
     * Write player data from cache to MySQL and remove player from cache
     *
     * @param uuid uuid of the player
     */
    public void removePlayerFromCache(String uuid) {
        writeToDatabase(uuid);
        playerDataCache.remove(uuid);
    }

    /**
     * Get artifact item identifier in specific slot in artifact inventory for player
     *
     * @param uuid uuid of the player
     * @param slot the inventory slot id
     * @return the artifact item identifier in the slot, or null if no item is in slot
     */
    public String getItem(String uuid, String slot) {
        try {
            return playerDataCache.get(uuid).get(slot).getStr();
        } catch (NullPointerException e) {
            plugin.getLogger().warning(table + " data for " + slot + " for player " + Bukkit.getPlayer(UUID.fromString(uuid)) + " does not exist in cache!");
            return null;
        }
    }

    /**
     * Get minimum percentage of any artifact of that type (for damage reduction, etc.) for player
     *
     * @param uuid the player's uuid
     * @param identifier the artifact type
     * @return the percentage for the artifact
     */
    public double getMinItemPercentage(String uuid, String identifier) {
        double minPercentage = 1;
        for (int i=0; i<inventorySize; i++) {
            String itemInSlot = getItem(uuid, "slot_" + i);
            if (itemInSlot != null) {

                int lastUnderscore = itemInSlot.lastIndexOf('_');
                if (lastUnderscore != -1) {
                    String itemInSlotID = itemInSlot.substring(0, lastUnderscore);

                    if (itemInSlotID.equals(identifier)) {
                        double itemPercentage = configUtil.getArtifactPercentage(itemInSlot);
                        if (itemPercentage < minPercentage) {
                            minPercentage = itemPercentage;
                        }
                    }
                }
            }
        }
        return minPercentage;
    }

    /**
     * Get maximum percentage of any artifact of that type (for damage increasing, etc.) for player
     *
     * @param uuid the player's uuid
     * @param identifier the artifact type
     * @return the percentage for the artifact
     */
    public double getMaxItemPercentage(String uuid, String identifier) {
        double maxPercentage = 1;
        for (int i=0; i<inventorySize; i++) {
            String itemInSlot = getItem(uuid, "slot_" + i);
            if (itemInSlot != null) {

                int lastUnderscore = itemInSlot.lastIndexOf('_');
                if (lastUnderscore != -1) {
                    String itemInSlotID = itemInSlot.substring(0, lastUnderscore);

                    if (itemInSlotID.equals(identifier)) {
                        double itemPercentage = configUtil.getArtifactPercentage(itemInSlot);
                        if (itemPercentage > maxPercentage) {
                            maxPercentage = itemPercentage;
                        }
                    }
                }
            }
        }
        return maxPercentage;
    }
}