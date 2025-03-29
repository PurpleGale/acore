package org.antagon.acore.listeners;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.utils.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class MinecartSpeedListener implements Listener {

    private final boolean betterMinecartsEnabled;
    private final ConfigurationSection blockTypes;
    private Map<Material, Double> validBlocks = new HashMap<>();

    public MinecartSpeedListener() {
        ConfigManager config = ConfigManager.getInstance();

        this.betterMinecartsEnabled = config.getBoolean("betterMinecarts.enabled", true);
        this.blockTypes = config.getSection("betterMinecarts.block-types");

        for (String key : blockTypes.getKeys(false)) {
            try {
                Material blockType = MaterialValidator.validateMaterial(key);
                double blockSpeedMultiplier = blockTypes.getDouble(key);

                validBlocks.put(blockType, blockSpeedMultiplier);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        // this.blockSpeedMultipliers = new HashMap<>();
        // for (String key : config.getStringList("block-types")) {
        //     String[] parts = key.split(":");
        //     Material material = Material.valueOf(parts[0]);
        //     double multiplier = Double.parseDouble(parts[1]);
        //     this.blockSpeedMultipliers.put(material, multiplier);
        // }

    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!betterMinecartsEnabled || !(event.getVehicle() instanceof Minecart minecart)) return;

        //Entity passenger = minecart.getPassengers().getFirst();

        Material belowBlock = minecart.getLocation().add(0, -1, 0).getBlock().getType();
        double multiplier = validBlocks.getOrDefault(belowBlock, 1.0);

        Vector velocity = minecart.getVelocity();
        Vector newVelocity = velocity.multiply(multiplier);
        minecart.setVelocity(newVelocity);
    }
}
