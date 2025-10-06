package org.antagon.acore.listener;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.util.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class MinecartSpeedListener implements Listener {

    private static final double VANILLA_MAX_SPEED = 0.4;
    private final Logger logger = Logger.getLogger(MinecartSpeedListener.class.getName());
    private final boolean minecartSpeedEnabled;
    private final double smoothFactor;
    private final List<String> minecartTypes;
    private final ConfigurationSection blockTypes;
    private final ConfigurationSection railTypes;
    private final Map<Material, Double> validBlocks = new HashMap<>();
    private final Map<Material, Double> validRails = new HashMap<>();
    private final EnumSet<EntityType> validMinecarts;
    private final EnumSet<Material> railTypesSet = EnumSet.of(
        Material.RAIL, Material.POWERED_RAIL,
        Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL
    );

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

        // Check if minecart is empty or passenger is not a player
        if (minecart.isEmpty() || !(minecart.getPassengers().get(0) instanceof Player)) return;

        Block railBlock = event.getVehicle().getLocation().getBlock();
        if (!railTypesSet.contains(railBlock.getType())) return;

        Block blockBelow = railBlock.getRelative(0, -1, 0);
        double blockMultiplier = validBlocks.getOrDefault(blockBelow.getType(), 1.0);
        double railMultiplier = validRails.getOrDefault(railBlock.getType(), 1.0);
        double totalMultiplier = blockMultiplier * railMultiplier;

        if (Math.abs(totalMultiplier - 1.0) < 0.001) return;

        // Use maxSpeed instead of velocity manipulation for proper minecart speed control
        double newMaxSpeed = VANILLA_MAX_SPEED * totalMultiplier;
        minecart.setMaxSpeed(newMaxSpeed);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) return;
        if (!(event.getExited() instanceof Player)) return;

        if (minecart.getMaxSpeed() > VANILLA_MAX_SPEED) {
            minecart.setMaxSpeed(VANILLA_MAX_SPEED);
        }
    }
}
