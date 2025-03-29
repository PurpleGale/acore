package org.antagon.acore.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.utils.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final boolean betterRunEnabled;
    private final ConfigurationSection blockTypes;
    private final int checkFrequency;
    private final Set<Material> validBlocks;
    private final Map<UUID, Long> lastCheckTime = new HashMap<>();

    public PlayerMoveListener() {
        ConfigManager config = ConfigManager.getInstance();

        this.betterRunEnabled = config.getBoolean("betterRun.enabled");
        this.blockTypes = config.getSection("betterRun.block-types");
        this.checkFrequency = config.getInt("betterRun.tick-frequency");

        this.validBlocks = MaterialValidator.validateMaterials(blockTypes.getKeys(false));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!betterRunEnabled) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        long currentTime = System.currentTimeMillis();
        long lastCheck = lastCheckTime.getOrDefault(playerId, 0L);

        if (currentTime - lastCheck < checkFrequency) {
            return; // Too quick check
        }

        Block blockUnder = player.getLocation().getBlock().getRelative(0, -1, 0);
    }
}
