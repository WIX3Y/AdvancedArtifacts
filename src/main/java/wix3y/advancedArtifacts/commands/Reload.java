package wix3y.advancedArtifacts.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wix3y.advancedArtifacts.AdvancedArtifacts;

public class Reload implements CommandExecutor {
    private final AdvancedArtifacts plugin;

    public Reload(AdvancedArtifacts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.reload();
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_gray>[<gradient:#5544FF:#CCBBFF:#5544FF>Advanced Artifacts</gradient>]</dark_gray> <gray>>> <green>Config reloaded!"));
        return true;
    }
}