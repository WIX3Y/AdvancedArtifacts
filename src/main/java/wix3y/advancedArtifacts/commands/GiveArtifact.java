package wix3y.advancedArtifacts.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wix3y.advancedArtifacts.util.ConfigUtil;

public class GiveArtifact implements CommandExecutor {
    private ConfigUtil configUtil;

    public GiveArtifact(ConfigUtil configUtil) {
        this.configUtil = configUtil;
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
        if (args.length < 3) {
            sender.sendMessage("/aagive <player> <artifact> <level>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage("Invalid player " + args[0]);
            return true;
        }

        ItemStack item = configUtil.getArtifact(args[1] + "_" + args[2]);
        if (item == null) {
            sender.sendMessage("Invalid artifact or level " + args[1] + "_" + args[2]);
            return true;
        }

        targetPlayer.give(item);
        return true;
    }
}