package org.antagon.acore.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.util.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerMoveListener implements Listener {

    private final Logger logger = Logger.getLogger(PlayerMoveListener.class.getName());
    private final boolean betterRunEnabled;
    private final double smoothFactor;
    private final ConfigurationSection blockTypes;
    private final int checkFrequency;
    private final Map<Material, Double> validBlocks = new HashMap<>();
    private final Map<UUID, Long> lastCheckTime = new HashMap<>();
    private final Map<UUID, Long> lastBeehiveCheck = new HashMap<>();

    public PlayerMoveListener() {
        ConfigManager config = ConfigManager.getInstance();

        this.betterRunEnabled = config.getBoolean("betterRun.enabled", true);
        this.blockTypes = config.getSection("betterRun.block-types");
        this.smoothFactor = config.getDouble("betterRun.smooth-factor", 5.0);
        this.checkFrequency = config.getInt("betterRun.tick-frequency", 20);

        loadBlockTypes();
    }

    private void loadBlockTypes() {
        if (blockTypes == null) {
            logger.warning("Warning: configuration section ‘betterRun.block-types’ not found!");
            return;
        }
        for (String key : blockTypes.getKeys(false)) {
            try {
                Material blockType = MaterialValidator.validateMaterial(key);
                validBlocks.put(blockType, blockTypes.getDouble(key));
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material in configuration: " + key + ". " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!betterRunEnabled) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        long currentTime = System.currentTimeMillis();
        long lastCheck = lastCheckTime.getOrDefault(playerId, 0L);
        if (currentTime - lastCheck < checkFrequency) return;

        if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;

        lastCheckTime.put(playerId, currentTime);

        Block blockUnder = player.getLocation().subtract(0, 0.1, 0).getBlock();
        Material blockUnderType = blockUnder.getType();

        // Check for beehive achievement logic
        checkBeehiveAchievement(player, blockUnder);

        if (validBlocks.containsKey(blockUnderType)) {
            // Apply temporary speed effect
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0)); // 3 seconds, Speed I
            }
        } else {
            // Remove speed effect if player is not on valid block
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }
    }

    /**
     * Check if player is standing on a beehive and award achievement if conditions are met
     */
    private void checkBeehiveAchievement(Player player, Block blockUnder) {
        // Check if the block under the player is a beehive
        if (blockUnder.getType() != Material.BEEHIVE && blockUnder.getType() != Material.BEE_NEST) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastCheck = lastBeehiveCheck.getOrDefault(playerId, 0L);

        // Check cooldown (prevent spam)
        if (currentTime - lastCheck < 1000) { // 1 second cooldown
            return;
        }

        lastBeehiveCheck.put(playerId, currentTime);

        // Check if player has the root achievement
        if (playerHasAchievement(player, "acore:schvapchichi/root")) {
            // Award the swarmer achievement
            awardAchievement(player, "acore:schvapchichi/swarmer");
        } else {
            logger.info("Player " + player.getName() + " does not have root achievement");
        }
    }

    /**
     * Check if player has a specific achievement
     */
    private boolean playerHasAchievement(Player player, String advancementKey) {
        try {
            NamespacedKey key = NamespacedKey.fromString(advancementKey);
            if (key == null) {
                return false;
            }

            Advancement advancement = player.getServer().getAdvancement(key);
            if (advancement == null) {
                return false;
            }

            return player.getAdvancementProgress(advancement).isDone();
        } catch (Exception e) {
            logger.warning("Error checking achievement " + advancementKey + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Award achievement to player
     */
    private void awardAchievement(Player player, String advancementKey) {
        try {
            NamespacedKey key = NamespacedKey.fromString(advancementKey);
            if (key == null) {
                return;
            }

            Advancement advancement = player.getServer().getAdvancement(key);
            if (advancement == null) {
                return;
            }

            // Get available criteria for this advancement
            var criteria = advancement.getCriteria();
            if (criteria.isEmpty()) {
                logger.warning("No criteria found for advancement: " + advancementKey);
                return;
            }

            // Award the first available criteria
            String firstCriteria = criteria.iterator().next();
            var progress = player.getAdvancementProgress(advancement);

            if (!progress.getAwardedCriteria().contains(firstCriteria)) {
                progress.awardCriteria(firstCriteria);
            } else {
            }
        } catch (Exception e) {
            logger.warning("Error awarding achievement " + advancementKey + ": " + e.getMessage());
        }
    }
}
