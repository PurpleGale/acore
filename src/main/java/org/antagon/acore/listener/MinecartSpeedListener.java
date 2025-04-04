package org.antagon.acore.listener;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.util.MaterialValidator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MinecartSpeedListener implements Listener {

    private final Logger logger = Logger.getLogger(MinecartSpeedListener.class.getName());
    private final boolean minecartSpeedEnabled;
    private final double smoothFactor;
    private final List<String> minecartTypes;
    private final ConfigurationSection blockTypes;
    private final ConfigurationSection railTypes;
    private final Map<Material, Double> validBlocks = new HashMap<>();
    private final Map<Material, Double> validRails = new HashMap<>();
    private final EnumSet<EntityType> validMinecarts;

    public MinecartSpeedListener() {
        ConfigManager config = ConfigManager.getInstance();

        this.minecartSpeedEnabled = config.getBoolean("minecartSpeed.enabled", true);
        this.blockTypes = config.getSection("minecartSpeed.block-types");
        this.railTypes = config.getSection("minecartSpeed.rail-types");
        this.smoothFactor = config.getDouble("minecartSpeed.smooth-factor", 5.0);
        this.minecartTypes = config.getStringList("minecartSpeed.minecart-types");

        loadBlockTypes();
        loadRailTypes();
        this.validMinecarts = loadMinecartTypes();
    }

    private void loadBlockTypes() {
        if (blockTypes == null) {
            logger.warning("Warning: configuration section 'minecartSpeed.block-types' not found!");
            return;
        }
        for (String key : blockTypes.getKeys(false)) {
            try {
                Material blockType = MaterialValidator.validateMaterial(key);
                validBlocks.put(blockType, blockTypes.getDouble(key));
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material in block-types: " + key + ". " + e.getMessage());
            }
        }
    }

    private void loadRailTypes() {
        if (railTypes == null) {
            logger.warning("Warning: configuration section 'minecartSpeed.rail-types' not found!");
            return;
        }
        for (String key : railTypes.getKeys(false)) {
            try {
                Material railType = MaterialValidator.validateMaterial(key);
                validRails.put(railType, railTypes.getDouble(key));
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material in rail-types: " + key + ". " + e.getMessage());
            }
        }
    }

    private EnumSet<EntityType> loadMinecartTypes() {
        EnumSet<EntityType> set = EnumSet.noneOf(EntityType.class);
        for (String name : minecartTypes) {
            try {
                set.add(EntityType.valueOf(name));
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid entity type in minecart-types: " + name + ". " + e.getMessage());
            }
        }
        if (set.isEmpty()) {
            logger.warning("Warning: configuration list 'minecartSpeed.minecart-types' is empty or not found!");
        }
        return set;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!minecartSpeedEnabled || !(event.getVehicle() instanceof Minecart minecart)) return;
        if (!validMinecarts.contains(minecart.getType())) return;
        if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;

        Location loc = minecart.getLocation();
        Block rail = loc.getBlock();
        Block belowBlock = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());

        double blockMultiplier = validBlocks.getOrDefault(belowBlock.getType(), 1.0);
        double railMultiplier = validRails.getOrDefault(rail.getType(), 1.0);
        double totalMultiplier = blockMultiplier * railMultiplier;

        if (Math.abs(totalMultiplier - 1.0) < 0.001) return;

        Vector velocity = minecart.getVelocity();
        Vector targetVelocity = velocity.multiply(totalMultiplier);

        if (velocity.distanceSquared(targetVelocity) < 0.000001) return;

        // Линейная интерполяция между текущей и целевой скоростью
        // newVelocity = velocity + (targetVelocity - velocity) * interpolationFactor
        double interpolationFactor = 1.0 - smoothFactor / 100.0;
        Vector newVelocity = velocity.clone()
                            .multiply(1 - interpolationFactor)
                            .add(targetVelocity.clone().multiply(interpolationFactor));
        
        minecart.setVelocity(newVelocity);
    }
}
