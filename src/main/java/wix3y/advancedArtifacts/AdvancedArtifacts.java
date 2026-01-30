package wix3y.advancedArtifacts;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import wix3y.advancedArtifacts.commands.*;
import wix3y.advancedArtifacts.handlers.InventoryHandler;
import wix3y.advancedArtifacts.handlers.PlayerDamageHandler;
import wix3y.advancedArtifacts.handlers.PlayerJoinHandler;
import wix3y.advancedArtifacts.handlers.PlayerQuitHandler;
import wix3y.advancedArtifacts.util.ConfigUtil;
import wix3y.advancedArtifacts.util.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

public final class AdvancedArtifacts extends JavaPlugin {
    private DatabaseManager databaseManager;
    private ConfigUtil configUtil;
    private InventoryHandler inventoryHandler;
    private OpenInventory openInventoryCommand;
    private OpenInventoryOther openInventoryOtherCommand;
    private GiveArtifact giveArtifactCommand;
    private Tinker tinkerCommand;
    private final NamespacedKey uniqueID = new NamespacedKey(this, "artifact");

    @Override
    public void onEnable() {
        this.configUtil = new ConfigUtil(this, uniqueID);
        FileConfiguration config = this.getConfig();

        String table = "AA_ArtifactInventory";
        int inventorySize = 9 * Math.min(configUtil.getInventoryRows(), 6);
        List<String> columns = new ArrayList<>();
        for (int i=0; i<inventorySize; i++) {
            columns.add("slot_" + i);
        }

        databaseManager = new DatabaseManager(this);
        databaseManager.connect(config.getString("MySQL.ip"), config.getString("MySQL.port"), config.getString("MySQL.database"), config.getString("MySQL.username"), config.getString("MySQL.password"), config.getInt("MySQL.poolsize"));
        databaseManager.initialize(table, columns);

        inventoryHandler = new InventoryHandler(this, databaseManager, configUtil, columns, table, uniqueID, inventorySize);
        new PlayerJoinHandler(this, inventoryHandler);
        new PlayerQuitHandler(this, inventoryHandler);

        new PlayerDamageHandler(this, inventoryHandler);

        openInventoryCommand = new OpenInventory(this, inventoryHandler, configUtil, inventorySize);
        getCommand("aainventory").setExecutor(openInventoryCommand);

        openInventoryOtherCommand = new OpenInventoryOther(this, inventoryHandler, configUtil, inventorySize);
        getCommand("aainventoryother").setExecutor(openInventoryOtherCommand);

        giveArtifactCommand = new GiveArtifact(configUtil);
        getCommand("aagive").setExecutor(giveArtifactCommand);

        tinkerCommand = new Tinker(this, configUtil);
        getCommand("aatinker").setExecutor(tinkerCommand);

        getCommand("aareload").setExecutor(new Reload(this));

        int updateFreq = config.getInt("MySQL.updatefreq");
        if (updateFreq > 0) {
            run(updateFreq);
        }

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("     <gradient:#5544FF:#CCBBFF:#5544FF>Advanced Artifacts</gradient>"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("           <gray>v1.0.0"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("           <green>Enabled"));
        Bukkit.getConsoleSender().sendMessage("");
    }

    private void run(int updateFreq) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> {
                    inventoryHandler.writeToDatabase();
                }, updateFreq, updateFreq);
    }

    public void reload() {
        this.reloadConfig();
        this.configUtil = new ConfigUtil(this, uniqueID);
        inventoryHandler.reloadConfigUtil(configUtil);
        openInventoryCommand.reloadConfigUtil(configUtil);
        openInventoryOtherCommand.reloadConfigUtil(configUtil);
        giveArtifactCommand.reloadConfigUtil(configUtil);
        tinkerCommand.reloadConfigUtil(configUtil);
    }

    @Override
    public void onDisable() {
        inventoryHandler.writeToDatabase();
        databaseManager.disconnect();

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("     <gradient:#5544FF:#CCBBFF:#5544FF>Advanced Artifacts</gradient>"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("           <gray>v1.0.0"));
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("          <red>Disabled"));
        Bukkit.getConsoleSender().sendMessage("");
    }
}
