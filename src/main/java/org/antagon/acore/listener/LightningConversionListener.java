package org.antagon.acore.listener;

import java.util.HashMap;
import java.util.Map;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.utils.MaterialValidator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

public class LightningConversionListener implements Listener {
    private final boolean lightningConversionEnabled;
    private final ConfigurationSection blockTypes;
    private Map<Material, Material> validBlocks = new HashMap<>();

    public LightningConversionListener() {
        ConfigManager config = ConfigManager.getInstance();

        this.lightningConversionEnabled = config.getBoolean("lightningConversion.enabled", true);
        this.blockTypes = config.getSection("lightningConversion.block-types");

        for (String key : blockTypes.getKeys(false)) {
            try {
                Material fromBlock = MaterialValidator.validateMaterial(key);
                Material toBlock = MaterialValidator.validateMaterial(blockTypes.getString(key));

                validBlocks.put(fromBlock, toBlock);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
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
                Material surroundingBlockMaterial = surroundingBlock.getType();
                if (validBlocks.get(surroundingBlockMaterial) != null) {
                    
                    surroundingBlock.setType(validBlocks.get(surroundingBlockMaterial));

                }

            }
        }
    }
}