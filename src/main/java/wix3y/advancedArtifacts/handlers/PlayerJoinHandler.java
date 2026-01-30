package wix3y.advancedArtifacts.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import wix3y.advancedArtifacts.AdvancedArtifacts;

public class PlayerJoinHandler implements Listener {
    private final AdvancedArtifacts plugin;
    private final InventoryHandler inventoryHandler;

    public PlayerJoinHandler(AdvancedArtifacts plugin, InventoryHandler inventoryHandler) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.inventoryHandler = inventoryHandler;
    }

    /**
     * Load player data into cache upon player join
     *
     * @param event the player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String uuid = event.getPlayer().getUniqueId().toString();
            inventoryHandler.addPlayerToCache(uuid);
        });
    }
}