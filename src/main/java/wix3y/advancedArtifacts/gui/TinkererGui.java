package wix3y.advancedArtifacts.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import wix3y.advancedArtifacts.AdvancedArtifacts;

public class TinkererGui implements InventoryHolder {
    private final Inventory inventory;

    public TinkererGui(AdvancedArtifacts plugin, String guiName) {
        String menuName = "<white>七七七七七七七七ェ七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七" + guiName;
        this.inventory = plugin.getServer().createInventory(this, 36, MiniMessage.miniMessage().deserialize(menuName));
        initialize();
    }

    /**
     * Set filler items in slots in inventory
     *
     */
    private void initialize() {
        ItemStack empty = new ItemStack(Material.PAPER);
        ItemMeta meta = empty.getItemMeta();
        meta.setItemModel(new NamespacedKey("advancedartifacts", "empty"));
        meta.setHideTooltip(true);
        empty.setItemMeta(meta);

        for (int i=0; i<inventory.getSize(); i++) {
            if (!(i==19 || i==22 || i==25)) {
                inventory.setItem(i, empty);
            }
        }
    }

    /**
     * Get the inventory
     *
     * @return the inventory
     */
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}