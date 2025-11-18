package org.antagon.acore.listener;

import org.antagon.acore.util.BlockInteractionTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listens for player interactions with blocks to track them for fog potion feature
 */
public class BlockInteractionListener implements Listener {

    private final BlockInteractionTracker tracker;

    public BlockInteractionListener() {
        this.tracker = BlockInteractionTracker.getInstance();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        Location location = event.getBlock().getLocation();
        tracker.recordInteraction(player, location);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        Location location = event.getBlock().getLocation();
        tracker.recordInteraction(player, location);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only track interactions with blocks, not air
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        if (player == null) return;

        Location location = event.getClickedBlock().getLocation();
        tracker.recordInteraction(player, location);
    }
}
