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

public class OpenInventoryOther implements CommandExecutor {
    private final AdvancedArtifacts plugin;
    private final InventoryHandler inventoryHandler;
    private ConfigUtil configUtil;
    private final int inventorySize;

    public OpenInventoryOther(AdvancedArtifacts plugin, InventoryHandler inventoryHandler, ConfigUtil configUtil, int inventorySize) {
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("/aainventoryother <player>");
            return true;

        }
        Player targetPlayer = (Player) Bukkit.getOfflinePlayer(args[0]);

        ArtifactInventory artifactInventory = new ArtifactInventory(plugin, targetPlayer, inventoryHandler, configUtil, inventorySize);
        player.openInventory(artifactInventory.getInventory());
        return true;
    }
}