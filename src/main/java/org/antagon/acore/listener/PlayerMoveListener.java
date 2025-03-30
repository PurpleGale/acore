package org.antagon.acore.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.antagon.acore.core.ConfigManager;

import org.antagon.acore.util.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final Logger logger = Logger.getLogger(PlayerMoveListener.class.getName());
    private final boolean betterRunEnabled;
    private final ConfigurationSection blockTypes;
    private final int checkFrequency;
    private final double movementThreshold;
    private final Map<Material, Double> validBlocks = new HashMap<>();
    private final Map<UUID, Long> lastCheckTime = new HashMap<>();

    public PlayerMoveListener() {
        ConfigManager config = ConfigManager.getInstance();

        this.betterRunEnabled = config.getBoolean("betterRun.enabled");
        this.blockTypes = config.getSection("betterRun.block-types");
        this.checkFrequency = config.getInt("betterRun.tick-frequency", 20);
        this.movementThreshold = config.getDouble("betterRun.move-threshold", 0.01);

        // this.validBlocks = MaterialValidator.validateMaterials(blockTypes.getKeys(false));

        if (blockTypes == null) {
            logger.warning("Warning: configuration section ‘betterRun.block-types’ not found!");
            return;
        }

        for (String key : blockTypes.getKeys(false)) {
            try {
                Material blockType = MaterialValidator.validateMaterial(key);
                double speedMultiplier = blockTypes.getDouble(key);

                validBlocks.put(blockType, speedMultiplier);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!betterRunEnabled) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        long currentTime = System.currentTimeMillis();
        long lastCheck = lastCheckTime.getOrDefault(playerId, 0L);

        if (currentTime - lastCheck < checkFrequency) return;

        if (event.getFrom().distanceSquared(event.getTo()) < movementThreshold) return;

        Block blockUnder = player.getLocation().getBlock().getRelative(0, -1, 0);
        Material blockUnderType = blockUnder.getType();

        if (validBlocks.containsKey(blockUnderType)) {
            AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

            speedAttribute.setBaseValue(validBlocks.get(blockUnderType) + 0.2);
        }
    }
}