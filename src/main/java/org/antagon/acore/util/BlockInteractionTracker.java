package org.antagon.acore.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Tracks player interactions with blocks in specific areas
 */
public class BlockInteractionTracker {

    private static BlockInteractionTracker instance;

    // Map of location -> list of player interactions (timestamp + player name)
    private final Map<Location, List<PlayerInteraction>> interactions = new ConcurrentHashMap<>();

    // Map of location -> last use timestamp for cooldowns
    private final Map<Location, Long> cooldowns = new ConcurrentHashMap<>();

    // Map of player UUID -> last use timestamp for player cooldowns
    private final Map<String, Long> playerCooldowns = new ConcurrentHashMap<>();

    private BlockInteractionTracker() {}

    public static BlockInteractionTracker getInstance() {
        if (instance == null) {
            instance = new BlockInteractionTracker();
        }
        return instance;
    }

    /**
     * Records a player interaction with a block
     */
    public void recordInteraction(Player player, Location location) {
        Location key = roundLocation(location);

        List<PlayerInteraction> locationInteractions = interactions.computeIfAbsent(key, k -> new ArrayList<>());

        // Add new interaction
        locationInteractions.add(new PlayerInteraction(player.getName(), System.currentTimeMillis()));

        // Keep only last 10 interactions per location to prevent memory leaks
        if (locationInteractions.size() > 10) {
            locationInteractions.remove(0);
        }
    }

    /**
     * Gets the last 3 players who interacted with blocks in the specified radius
     */
    public List<String> getLastPlayersInRadius(Location center, int radius) {
        Location centerKey = roundLocation(center);
        List<String> players = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        // Check all locations within radius
        for (Map.Entry<Location, List<PlayerInteraction>> entry : interactions.entrySet()) {
            Location interactionLocation = entry.getKey();

            if (isWithinRadius(centerKey, interactionLocation, radius)) {
                List<PlayerInteraction> locationInteractions = entry.getValue();

                for (PlayerInteraction interaction : locationInteractions) {
                    if (!players.contains(interaction.playerName)) {
                        players.add(interaction.playerName);
                        if (players.size() >= 3) {
                            return players;
                        }
                    }
                }
            }
        }

        return players;
    }

    /**
     * Checks if a location is on cooldown
     */
    public boolean isOnCooldown(Location location, int cooldownSeconds) {
        Location key = roundLocation(location);
        Long lastUse = cooldowns.get(key);

        if (lastUse == null) {
            return false;
        }

        long timeSinceLastUse = (System.currentTimeMillis() - lastUse) / 1000;
        return timeSinceLastUse < cooldownSeconds;
    }

    /**
     * Gets remaining cooldown time in seconds for a location
     */
    public int getRemainingCooldown(Location location, int cooldownSeconds) {
        Location key = roundLocation(location);
        Long lastUse = cooldowns.get(key);

        if (lastUse == null) {
            return 0;
        }

        long timeSinceLastUse = (System.currentTimeMillis() - lastUse) / 1000;
        long remaining = cooldownSeconds - timeSinceLastUse;

        return Math.max(0, (int) remaining);
    }

    /**
     * Sets cooldown for a location
     */
    public void setCooldown(Location location) {
        Location key = roundLocation(location);
        cooldowns.put(key, System.currentTimeMillis());
    }

    /**
     * Force removes cooldown for a location (for testing purposes)
     */
    public void removeCooldown(Location location) {
        Location key = roundLocation(location);
        cooldowns.remove(key);
    }

    /**
     * Gets all cooldown locations (for debugging)
     */
    public Map<Location, Long> getAllCooldowns() {
        return new ConcurrentHashMap<>(cooldowns);
    }

    /**
     * Checks if a player is on cooldown
     */
    public boolean isPlayerOnCooldown(Player player, int cooldownSeconds) {
        String playerUUID = player.getUniqueId().toString();
        Long lastUse = playerCooldowns.get(playerUUID);

        if (lastUse == null) {
            return false;
        }

        long timeSinceLastUse = (System.currentTimeMillis() - lastUse) / 1000;
        return timeSinceLastUse < cooldownSeconds;
    }

    /**
     * Gets remaining cooldown time in seconds for a player
     */
    public int getPlayerRemainingCooldown(Player player, int cooldownSeconds) {
        String playerUUID = player.getUniqueId().toString();
        Long lastUse = playerCooldowns.get(playerUUID);

        if (lastUse == null) {
            return 0;
        }

        long timeSinceLastUse = (System.currentTimeMillis() - lastUse) / 1000;
        long remaining = cooldownSeconds - timeSinceLastUse;

        return Math.max(0, (int) remaining);
    }

    /**
     * Sets cooldown for a player
     */
    public void setPlayerCooldown(Player player) {
        String playerUUID = player.getUniqueId().toString();
        playerCooldowns.put(playerUUID, System.currentTimeMillis());
    }

    /**
     * Gets all player cooldowns (for debugging)
     */
    public Map<String, Long> getAllPlayerCooldowns() {
        return new ConcurrentHashMap<>(playerCooldowns);
    }

    /**
     * Rounds location to block coordinates for consistent tracking
     */
    private Location roundLocation(Location location) {
        return new Location(location.getWorld(),
                          location.getBlockX(),
                          location.getBlockY(),
                          location.getBlockZ());
    }

    /**
     * Gets the exact location key for debugging purposes
     */
    public Location getLocationKey(Location location) {
        return roundLocation(location);
    }

    /**
     * Checks if two locations are within specified radius
     */
    private boolean isWithinRadius(Location center, Location location, int radius) {
        if (!center.getWorld().equals(location.getWorld())) {
            return false;
        }

        double distance = center.distance(location);
        return distance <= radius;
    }

    /**
     * Checks if two locations are the same (for exact location matching)
     */
    private boolean isSameLocation(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }

        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc2.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Represents a player interaction with a block
     */
    private static class PlayerInteraction {
        final String playerName;
        final long timestamp;

        PlayerInteraction(String playerName, long timestamp) {
            this.playerName = playerName;
            this.timestamp = timestamp;
        }
    }

    /**
     * Cleans up old interactions to prevent memory leaks
     */
    public void cleanupOldInteractions() {
        long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours ago

        interactions.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(interaction -> interaction.timestamp < cutoffTime);
            return entry.getValue().isEmpty();
        });

        // Clean up old cooldowns (older than 1 hour)
        long cooldownCutoff = System.currentTimeMillis() - (60 * 60 * 1000);
        cooldowns.entrySet().removeIf(entry -> entry.getValue() < cooldownCutoff);

        // Clean up old player cooldowns (older than 1 hour)
        playerCooldowns.entrySet().removeIf(entry -> entry.getValue() < cooldownCutoff);
    }
}
