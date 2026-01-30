package wix3y.advancedArtifacts.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wix3y.advancedArtifacts.AdvancedArtifacts;
import wix3y.advancedArtifacts.handlers.InventoryHandler;
import wix3y.advancedArtifacts.util.ConfigUtil;

public class ArtifactInventory implements InventoryHolder {
    private final AdvancedArtifacts plugin;
    protected final Inventory inventory;
    private final Player player;

    public ArtifactInventory(AdvancedArtifacts plugin, Player player, InventoryHandler inventoryHandler, ConfigUtil configUtil, int inventorySize) {
        String menuName = configUtil.getMenuName();

        this.plugin = plugin;
        this.player = player;
        this.inventory = plugin.getServer().createInventory(this, inventorySize, MiniMessage.miniMessage().deserialize(menuName));

        initialize(configUtil, inventoryHandler, inventorySize);
    }

    /**
     * Set items in slots in inventory
     *
     * @param configUtil config util used to obtain information from config file
     * @param inventoryHandler containing player data information
     * @param inventorySize the size of the artifact inventory
     */
    private void initialize(ConfigUtil configUtil, InventoryHandler inventoryHandler, int inventorySize) {
        for (int slot=0; slot <inventorySize; slot++) {
             String artifactID = inventoryHandler.getItem(player.getUniqueId().toString(), "slot_" + slot);
             if (artifactID == null) {
                 continue;
             }
             ItemStack artifact = configUtil.getArtifact(artifactID);
             if (artifact == null) {
                 plugin.getLogger().severe("Failed to get artifact with ID " + artifactID + " for player " + player.getName());
                 continue;
             }
             inventory.setItem(slot, artifact);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Player getTarget() {
        return player;
    }
}