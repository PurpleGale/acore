package org.antagon.acore.listeners;

import org.antagon.acore.core.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

public class LightningConversionController implements Listener {
    private final boolean lightningConversionEnabled;
    private final ConfigurationSection blockTypes;

    public LightningConversionController() {
        ConfigManager config = ConfigManager.getInstance();

        this.lightningConversionEnabled = config.getBoolean("lightningConversion.enabled", true);
        this.blockTypes = config.getSection("lightningConversion.block-types");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLightningStrike(LightningStrikeEvent event) {
        if (!lightningConversionEnabled) return;

        Location location = event.getLightning().getLocation();
        Block block = location.getBlock();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                Block surroundingBlock = event.getLightning().getWorld().getBlockAt(x + dx, y-1, z + dz);
                String surroundingBlockType = surroundingBlock.getType().toString();
                if (blockTypes.getKeys(false).contains(surroundingBlockType)) {

                    String newMaterial = blockTypes.getString(surroundingBlockType);
                    Material material = Material.matchMaterial(newMaterial)

                    if (material != null) surroundingBlock.setType(material);

                }

            }
        }
    }
}