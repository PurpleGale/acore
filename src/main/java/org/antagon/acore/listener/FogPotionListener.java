package org.antagon.acore.listener;

import java.util.List;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.util.BlockInteractionTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listens for fog potion throws and displays last players who interacted with blocks in radius
 */
public class FogPotionListener implements Listener {

    private final ConfigManager configManager;
    private final BlockInteractionTracker tracker;
    private final JavaPlugin plugin;

    public FogPotionListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.tracker = BlockInteractionTracker.getInstance();
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion)) {
            return;
        }

        ThrownPotion thrownPotion = (ThrownPotion) event.getEntity();

        // Try to get the player who threw the potion
        if (!(thrownPotion.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player) thrownPotion.getShooter();

        // Check if this is our custom fog potion by Custom Model Data
        int customModelData = getCustomModelData(thrownPotion);
        int configuredCustomModelData = configManager.getInt("fogPotion.custom-model-data", 1001);

        if (customModelData != configuredCustomModelData) {
            return;
        }

        // Check if potion is enabled in config
        if (!configManager.getBoolean("fogPotion.enabled", true)) {
            return;
        }

        // Check player-specific cooldown (global cooldown per player)
        int cooldown = configManager.getInt("fogPotion.cooldown", 30);

        if (tracker.isPlayerOnCooldown(player, cooldown)) {
            int remainingCooldown = tracker.getPlayerRemainingCooldown(player, cooldown);
            player.sendMessage("§cВы недавно использовали зелье! Подождите еще §e" + remainingCooldown + " §cсекунд.");
            event.setCancelled(true); // Cancel the potion throw
            return;
        }

        // Set player cooldown immediately when potion is thrown
        tracker.setPlayerCooldown(player);

        final ThrownPotion finalThrownPotion = thrownPotion;

        // Schedule the effect after a short delay to let the potion land
        new BukkitRunnable() {
            @Override
            public void run() {
                handleFogPotionEffect(finalThrownPotion, player);
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }

    private void handleFogPotionEffect(ThrownPotion thrownPotion, Player player) {
        Location effectLocation = thrownPotion.getLocation();

        // Get configuration values
        int radius = configManager.getInt("fogPotion.radius", 10);
        int displayDuration = configManager.getInt("fogPotion.display-duration", 10);

        // Get last players in radius
        List<String> lastPlayers = tracker.getLastPlayersInRadius(effectLocation, radius);

        if (lastPlayers.isEmpty()) {
            player.sendMessage("§7В этом радиусе никто не взаимодействовал с блоками недавно.");
            return;
        }

        // Format message - all player names in white color
        String playerList = String.join("§f, §f", lastPlayers);
        String message = "§aПоследние взаимодействия в радиусе: §f" + playerList;

        // Show in action bar for specified duration
        showActionBarForDuration(player, message, displayDuration);
    }

    private int getCustomModelData(ThrownPotion potion) {
        // Get Custom Model Data from the potion item to identify it as our fog potion
        if (potion.getItem() != null && potion.getItem().hasItemMeta()) {
            var meta = potion.getItem().getItemMeta();
            if (meta != null && meta.hasCustomModelData()) {
                return meta.getCustomModelData();
            }
        }

        return -1; // Return -1 if no Custom Model Data is found
    }

    private void showActionBarForDuration(Player player, String message, int seconds) {
        // Show initial message
        player.sendActionBar(message);

        if (seconds > 0) {
            // Schedule repeated messages
            new BukkitRunnable() {
                private int remaining = seconds;

                @Override
                public void run() {
                    if (remaining <= 0 || !player.isOnline()) {
                        cancel();
                        return;
                    }

                    player.sendActionBar(message);
                    remaining--;
                }
            }.runTaskTimer(plugin, 20L, 20L); // Every second
        }
    }
}
