package wix3y.advancedArtifacts.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import wix3y.advancedArtifacts.AdvancedArtifacts;

public class PlayerQuitHandler implements Listener {
    private final AdvancedArtifacts plugin;
    private final InventoryHandler inventoryHandler;

    public PlayerQuitHandler(AdvancedArtifacts plugin, InventoryHandler inventoryHandler) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.inventoryHandler = inventoryHandler;
    }

    /**
     * Unload player data from cache upon player quit and write data to database
     *
     * @param event the player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String uuid = event.getPlayer().getUniqueId().toString();
            inventoryHandler.removePlayerFromCache(uuid);
        });
    }
}