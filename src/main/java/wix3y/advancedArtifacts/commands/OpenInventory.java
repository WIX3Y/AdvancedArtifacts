package wix3y.advancedArtifacts.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wix3y.advancedArtifacts.AdvancedArtifacts;
import wix3y.advancedArtifacts.gui.ArtifactInventory;
import wix3y.advancedArtifacts.handlers.InventoryHandler;
import wix3y.advancedArtifacts.util.ConfigUtil;

public class OpenInventory implements CommandExecutor {
    private final AdvancedArtifacts plugin;
    private final InventoryHandler inventoryHandler;
    private ConfigUtil configUtil;
    private final int inventorySize;

    public OpenInventory(AdvancedArtifacts plugin, InventoryHandler inventoryHandler, ConfigUtil configUtil, int inventorySize) {
        this.plugin = plugin;
        this.inventoryHandler = inventoryHandler;
        this.configUtil = configUtil;
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player targetPlayer = null;
        if (args.length > 0 && sender.hasPermission("advancedartifacts.inventory.others")) {
            targetPlayer = (Player) Bukkit.getOfflinePlayer(args[0]);
        }

        if (targetPlayer == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Invalid player.");
                return true;
            }
            targetPlayer = (Player) sender;
        }
        
        ArtifactInventory artifactInventory = new ArtifactInventory(plugin, targetPlayer, inventoryHandler, configUtil, inventorySize);
        targetPlayer.openInventory(artifactInventory.getInventory());
        return true;
    }
}