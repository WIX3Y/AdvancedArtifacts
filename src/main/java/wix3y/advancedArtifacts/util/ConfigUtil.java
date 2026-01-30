package wix3y.advancedArtifacts.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import wix3y.advancedArtifacts.AdvancedArtifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigUtil {
    private final NamespacedKey uniqueID;
    private final String menuName;
    private final String tinkerName;
    private final int inventoryRows;
    private final Map<String, ItemStack> artifacts = new ConcurrentHashMap<>();
    private final Map<String, Double> multipliers = new ConcurrentHashMap<>();

    public ConfigUtil(AdvancedArtifacts plugin, NamespacedKey uniqueID) {
        this.uniqueID = uniqueID;
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        menuName = config.contains("MenuName") ? config.getString("MenuName") : "";
        tinkerName = config.contains("TinkerMenuName") ? config.getString("TinkerMenuName") : "";

        inventoryRows = config.contains("inventoryRows") ? config.getInt("inventoryRows") : 1;

        // Fetch artifacts
        List<String> artifactSection = new ArrayList<>(config.getConfigurationSection("Artifacts").getKeys(false));
        for (String artifact: artifactSection) {
            List<String> artifactLevels = new ArrayList<>(config.getConfigurationSection("Artifacts." + artifact + ".Levels").getKeys(false));

            String basePath = "Artifacts." + artifact;

            Material material;
            String materialStr = config.contains(basePath + ".Item") ? config.getString(basePath + ".Item") : null;
            try {
                material = Material.valueOf(materialStr);
            } catch (Exception e) {
                material = Material.PAPER;
            }

            NamespacedKey itemModel = null;
            String itemModelStr = config.contains(basePath + ".ItemModel") ? config.getString(basePath + ".ItemModel") : "none";
            if (itemModelStr != null && !itemModelStr.equals("none")) {
                itemModel= new NamespacedKey(plugin, itemModelStr);
            }

            String name = config.contains(basePath + ".Name") ? config.getString(basePath + ".Name") : "";
            List<String> lore = config.contains(basePath + ".Lore") ? config.getStringList(basePath + ".Lore") : new ArrayList<>();

            for (String level: artifactLevels) {
                String levelBasePath = basePath + ".Levels." + level;

                String loreLevel = config.contains(levelBasePath + ".level") ? config.getString(levelBasePath + ".level") : "";
                String loreDesc = config.contains(levelBasePath + ".description") ? config.getString(levelBasePath + ".description") : "";

                ItemStack item = createArtifact(artifact + "_" + level, material, itemModel, name, lore, loreLevel, loreDesc);
                artifacts.put(artifact + "_" + level, item);

                Double multiplier = config.contains(levelBasePath + ".multiplier") ? config.getDouble(levelBasePath + ".multiplier") : 1;
                multipliers.put(artifact + "_" + level, multiplier);
            }
        }
    }

    /**
     * Create an artifact from config specifications
     *
     * @param identifier the artifact's identifier
     * @param material the artifact's base material
     * @param itemModel the artifact's item model
     * @param name the display name
     * @param lores the lore
     * @param loreLevel a placeholder in the lore
     * @param loreDesc a placeholder in the lore
     * @return the artifact's ID
     */
    private ItemStack createArtifact(String identifier, Material material, NamespacedKey itemModel, String name, List<String> lores, String loreLevel, String loreDesc) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(uniqueID, PersistentDataType.STRING, identifier);

        if (itemModel != null) {
            meta.setItemModel(itemModel);
        }

        meta.setMaxStackSize(1);
        meta.displayName(MiniMessage.miniMessage().deserialize(name));
        List<Component> loreComponent = new ArrayList<>();
        for (String lore: lores) {
            lore = lore.replace("{level}", loreLevel)
                    .replace("{desc}", loreDesc);
            loreComponent.add(MiniMessage.miniMessage().deserialize(lore));
        }
        meta.lore(loreComponent);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Get artifact ID from artifact item
     *
     * @param item the artifact item
     * @return the artifact's ID
     */
    public String getArtifactID(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(uniqueID)) {
                return meta.getPersistentDataContainer().get(uniqueID, PersistentDataType.STRING);
            }
        }
        return null;
    }

    /**
     * Get artifact from ID
     *
     * @param artifactID the ID of the artifact
     * @return the artifact item
     */
    public ItemStack getArtifact(String artifactID) {
        return artifacts.get(artifactID);
    }

    /**
     * Get number of rows in the artifact inventory according to config
     *
     * @return the gui name
     */
    public int getInventoryRows() {
        return inventoryRows;
    }

    /**
     * Get name of artifact gui according to config
     *
     * @return the gui name
     */
    public String getMenuName() {
        return menuName;
    }

    /**
     * Get name of tinker gui according to config
     *
     * @return the gui name
     */
    public String getTinkerMenuName() {
        return tinkerName;
    }

    /**
     * Get artifact multiplier from ID
     *
     * @param artifactID uuid of the player
     * @return the artifact's multiplier
     */
    public double getArtifactPercentage(String artifactID) {
        return multipliers.get(artifactID);
    }
}