<<<<<<< HEAD:src/main/java/org/antagon/acore/listeners/MinecartSpeedListener.java
package org.antagon.acore.listeners;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.utils.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
=======
package org.antagon.acore.listener;

import org.antagon.acore.core.ConfigManager;
import org.bukkit.Material;
>>>>>>> 53ba489748348e49870d1547f8dc2dca6ab24c0b:src/main/java/org/antagon/acore/listener/MinecartSpeedListener.java
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
<<<<<<< HEAD:src/main/java/org/antagon/acore/listeners/MinecartSpeedListener.java
    private final ConfigurationSection blockTypes;
    private Map<Material, Double> validBlocks = new HashMap<>();
=======
    private final Map<Material, Double> blockSpeedMultipliers;
>>>>>>> 53ba489748348e49870d1547f8dc2dca6ab24c0b:src/main/java/org/antagon/acore/listener/MinecartSpeedListener.java

    public MinecartSpeedListener() {
        ConfigManager config = ConfigManager.getInstance();

        this.betterMinecartsEnabled = config.getBoolean("betterMinecarts.enabled", true);
<<<<<<< HEAD:src/main/java/org/antagon/acore/listeners/MinecartSpeedListener.java
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

=======

        this.blockSpeedMultipliers = new HashMap<>();
        for (String key : config.getStringList("block-types")) {
            String[] parts = key.split(":");
            Material material = Material.valueOf(parts[0]);
            double multiplier = Double.parseDouble(parts[1]);
            this.blockSpeedMultipliers.put(material, multiplier);
        }
>>>>>>> 53ba489748348e49870d1547f8dc2dca6ab24c0b:src/main/java/org/antagon/acore/listener/MinecartSpeedListener.java
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!betterMinecartsEnabled || !(event.getVehicle() instanceof Minecart minecart)) return;

        //Entity passenger = minecart.getPassengers().getFirst();

        Material belowBlock = minecart.getLocation().add(0, -1, 0).getBlock().getType();
<<<<<<< HEAD:src/main/java/org/antagon/acore/listeners/MinecartSpeedListener.java
        double multiplier = validBlocks.getOrDefault(belowBlock, 1.0);
=======
        double multiplier = blockSpeedMultipliers.getOrDefault(belowBlock, 1.0);
>>>>>>> 53ba489748348e49870d1547f8dc2dca6ab24c0b:src/main/java/org/antagon/acore/listener/MinecartSpeedListener.java

        Vector velocity = minecart.getVelocity();
        Vector newVelocity = velocity.multiply(multiplier);
        minecart.setVelocity(newVelocity);
    }
}
