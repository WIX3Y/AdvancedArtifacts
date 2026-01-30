package wix3y.advancedArtifacts.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wix3y.advancedArtifacts.AdvancedArtifacts;
import wix3y.advancedArtifacts.gui.TinkererGui;
import wix3y.advancedArtifacts.util.ConfigUtil;

public class Tinker implements Listener, CommandExecutor {
    private final AdvancedArtifacts plugin;
    private ConfigUtil configUtil;
    private String guiName;

    public Tinker(AdvancedArtifacts plugin, ConfigUtil configUtil) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.configUtil = configUtil;
        this.guiName = configUtil.getTinkerMenuName();
    }

    /**
     * Reload the config util
     *
     * @param configUtil the new config util
     */
    public void reloadConfigUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
        this.guiName = configUtil.getTinkerMenuName();
    }

    /**
     * Open custom anvil gui
     *
     * @param sender  who executed the command
     * @param command the command
     * @param label   the command name
     * @param args    arguments for the command (player to display statistics for, or none)
     * @return whether command was successful
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = null;
        if (args.length > 0 && sender.hasPermission("advancedartifacts.tinker.others")) {
            player = Bukkit.getPlayer(args[0]);
        }
        else if (sender instanceof Player) {
            player = (Player) sender;
            if (!sender.hasPermission("advancedartifacts.tinker.self")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to use this command."));
                return true;
            }
        }

        if (player == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>/tinker <player>"));
            return true;
        }

        TinkererGui page = new TinkererGui(plugin, guiName);
        player.openInventory(page.getInventory());
        return true;
    }

    /**
     * Cancel inventory clicking in tinker gui
     *
     * @param event the inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder(false);

        if (holder instanceof TinkererGui tinkererGui) {
            event.setCancelled(true);
            onTinkerGuiClick(tinkererGui, event);
        }
    }

    /**
     * Return any remaining items in gui to player
     *
     * @param event the inventory click event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder(false);

        if (holder instanceof TinkererGui) {
            onTinkerGuiClose(event);
        }
    }

    /**
     * Return any items in slot 19 and 22 to player
     *
     * @param event inventory click event
     */
    private void onTinkerGuiClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        ItemStack input1 = event.getInventory().getItem(19);
        ItemStack input2 = event.getInventory().getItem(22);

        event.getInventory().setItem(19, null);
        event.getInventory().setItem(22, null);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (input1 != null && input1.getType() != Material.AIR) {
                player.give(input1);
            }

            if (input2 != null && input2.getType() != Material.AIR) {
                player.give(input2);
            }
        }, 1L);
    }

    /**
     * Allow moving items between tinker gui and inventory
     * Allow claiming result item
     *
     * @param tinkerGui the anvil gui
     * @param event inventory click event
     */
    private void onTinkerGuiClick(TinkererGui tinkerGui, InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int slot = event.getRawSlot();
        ItemStack input1 = event.getInventory().getItem(19);
        ItemStack input2 = event.getInventory().getItem(22);
        ItemStack result = event.getInventory().getItem(25);

        // Click in result slot
        if (slot == 25 && input1 != null && input2 != null && result != null && input1.getType() != Material.AIR && input2.getType() != Material.AIR && result.getType() != Material.AIR) {
            event.getInventory().setItem(19, null);
            event.getInventory().setItem(22, null);
            event.getInventory().setItem(25, null);
            player.give(result);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
        }
        // Click in input slot 1
        else if (slot == 19 && input1 != null && input1.getType() != Material.AIR) {
            event.getInventory().setItem(19, null);
            event.getInventory().setItem(25, null);
            player.give(input1);
        }
        // Click in input slot 2
        else if (slot == 22 && input2 != null && input2.getType() != Material.AIR) {
            event.getInventory().setItem(22, null);
            event.getInventory().setItem(25, null);
            player.give(input2);
        }
        // Click in player inventory
        else if (slot >= event.getInventory().getSize()) {
            int clickedSlot = event.getSlot();
            ItemStack clickedItem = player.getInventory().getItem(clickedSlot);

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                if (input1 == null || input1.getType() == Material.AIR) {
                    player.getInventory().setItem(clickedSlot, null);
                    event.getInventory().setItem(19, clickedItem);
                    if (input2 != null && input2.getType() != Material.AIR) {
                        checkSetResult(event, clickedItem, input2);
                    }
                } else if (input2 == null || input2.getType() == Material.AIR) {
                    player.getInventory().setItem(clickedSlot, null);
                    event.getInventory().setItem(22, clickedItem);
                    checkSetResult(event, input1, clickedItem);
                }
            }
        }
    }

    /**
     * Check if a result tinker item should be set in the gui and set it
     *
     * @param event the click event that triggered the check
     * @param input1 the item in the first input slot
     * @param input2 the item in the second input slot
     */
    private void checkSetResult(InventoryClickEvent event, ItemStack input1, ItemStack input2) {
        if (input1 == null || input2 == null || input1.getType() == Material.AIR || input2.getType() == Material.AIR) {
            return;
        }

        String artifact1 = configUtil.getArtifactID(input1);
        if (!configUtil.getArtifactID(input2).equals(artifact1)) {
            return;
        }

        int last_ = artifact1.lastIndexOf("_");
        int artifactLevel = Integer.parseInt(artifact1.substring(last_ + 1)) + 1;
        ItemStack result = configUtil.getArtifact(artifact1.substring(0, last_) + "_" + artifactLevel);

        event.getInventory().setItem(25, result);
    }
}
