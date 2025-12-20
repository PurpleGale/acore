package org.antagon.acore.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ReferralManager {

    private final JavaPlugin plugin;
    private final File referralFile;
    private final Map<UUID, UUID> referrals; // реферал -> пригласивший
    private final Map<UUID, List<UUID>> inviterReferrals; // пригласивший -> список рефералов
    private final Map<UUID, Long> referralStartTime; // реферал -> время начала трекинга (в миллисекундах)
    private final Map<UUID, Boolean> referralRewarded; // реферал -> получил ли награду за 7 часов
    private YamlConfiguration referralConfig;

    public ReferralManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.referralFile = new File(plugin.getDataFolder(), "referrals.yml");
        this.referrals = new HashMap<>();
        this.inviterReferrals = new HashMap<>();
        this.referralStartTime = new HashMap<>();
        this.referralRewarded = new HashMap<>();
        loadReferrals();
    }

    /**
     * Load referrals from file
     */
    private void loadReferrals() {
        if (!referralFile.exists()) {
            try {
                referralFile.createNewFile();
                referralConfig = YamlConfiguration.loadConfiguration(referralFile);
                referralConfig.set("referrals", new HashMap<String, String>());
                referralConfig.set("inviter-referrals", new HashMap<String, List<String>>());
                referralConfig.set("referral-start-times", new HashMap<String, Long>());
                referralConfig.set("referral-rewarded", new HashMap<String, Boolean>());
                referralConfig.save(referralFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create referrals file: " + e.getMessage());
                return;
            }
        }

        referralConfig = YamlConfiguration.loadConfiguration(referralFile);

        // Load referrals map
        Map<String, Object> referralsMap = referralConfig.getConfigurationSection("referrals").getValues(false);
        for (Map.Entry<String, Object> entry : referralsMap.entrySet()) {
            try {
                UUID referralId = UUID.fromString(entry.getKey());
                UUID inviterId = UUID.fromString((String) entry.getValue());
                referrals.put(referralId, inviterId);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in referrals file: " + entry.getKey() + " -> " + entry.getValue());
            }
        }

        // Load inviter referrals map
        Map<String, Object> inviterMap = referralConfig.getConfigurationSection("inviter-referrals").getValues(false);
        for (Map.Entry<String, Object> entry : inviterMap.entrySet()) {
            try {
                UUID inviterId = UUID.fromString(entry.getKey());
                List<String> referralIds = referralConfig.getStringList("inviter-referrals." + entry.getKey());
                List<UUID> referralUUIDs = new ArrayList<>();
                for (String id : referralIds) {
                    referralUUIDs.add(UUID.fromString(id));
                }
                inviterReferrals.put(inviterId, referralUUIDs);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in inviter referrals file: " + entry.getKey());
            }
        }

        // Load start times
        Map<String, Object> startTimesMap = referralConfig.getConfigurationSection("referral-start-times").getValues(false);
        for (Map.Entry<String, Object> entry : startTimesMap.entrySet()) {
            try {
                UUID referralId = UUID.fromString(entry.getKey());
                long startTime = (Long) entry.getValue();
                referralStartTime.put(referralId, startTime);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in referral start times file: " + entry.getKey());
            }
        }

        // Load rewarded status
        Map<String, Object> rewardedMap = referralConfig.getConfigurationSection("referral-rewarded").getValues(false);
        for (Map.Entry<String, Object> entry : rewardedMap.entrySet()) {
            try {
                UUID referralId = UUID.fromString(entry.getKey());
                boolean rewarded = (Boolean) entry.getValue();
                referralRewarded.put(referralId, rewarded);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in referral rewarded file: " + entry.getKey());
            }
        }

        plugin.getLogger().info("Loaded " + referrals.size() + " referrals");
    }

    /**
     * Save referrals to file
     */
    private void saveReferrals() {
        // Save referrals map
        Map<String, String> referralsMap = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : referrals.entrySet()) {
            referralsMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        referralConfig.set("referrals", referralsMap);

        // Save inviter referrals map
        Map<String, List<String>> inviterMap = new HashMap<>();
        for (Map.Entry<UUID, List<UUID>> entry : inviterReferrals.entrySet()) {
            List<String> referralIds = new ArrayList<>();
            for (UUID id : entry.getValue()) {
                referralIds.add(id.toString());
            }
            inviterMap.put(entry.getKey().toString(), referralIds);
        }
        referralConfig.set("inviter-referrals", inviterMap);

        // Save start times
        Map<String, Long> startTimesMap = new HashMap<>();
        for (Map.Entry<UUID, Long> entry : referralStartTime.entrySet()) {
            startTimesMap.put(entry.getKey().toString(), entry.getValue());
        }
        referralConfig.set("referral-start-times", startTimesMap);

        // Save rewarded status
        Map<String, Boolean> rewardedMap = new HashMap<>();
        for (Map.Entry<UUID, Boolean> entry : referralRewarded.entrySet()) {
            rewardedMap.put(entry.getKey().toString(), entry.getValue());
        }
        referralConfig.set("referral-rewarded", rewardedMap);

        try {
            referralConfig.save(referralFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save referrals file: " + e.getMessage());
        }
    }

    /**
     * Add referral
     */
    public void addReferral(UUID referralId, UUID inviterId) {
        referrals.put(referralId, inviterId);

        // Add to inviter's list
        inviterReferrals.computeIfAbsent(inviterId, k -> new ArrayList<>()).add(referralId);

        saveReferrals();
    }

    /**
     * Remove referral
     */
    public void removeReferral(UUID referralId) {
        UUID inviterId = referrals.remove(referralId);
        if (inviterId != null) {
            List<UUID> inviterList = inviterReferrals.get(inviterId);
            if (inviterList != null) {
                inviterList.remove(referralId);
                if (inviterList.isEmpty()) {
                    inviterReferrals.remove(inviterId);
                }
            }
        }

        referralStartTime.remove(referralId);
        referralRewarded.remove(referralId);

        saveReferrals();
    }

    /**
     * Check if player is a referral
     */
    public boolean isReferral(UUID playerId) {
        return referrals.containsKey(playerId);
    }

    /**
     * Get inviter of a referral
     */
    public UUID getInviter(UUID referralId) {
        return referrals.get(referralId);
    }

    /**
     * Get referrals of an inviter
     */
    public List<UUID> getReferrals(UUID inviterId) {
        return inviterReferrals.getOrDefault(inviterId, new ArrayList<>());
    }

    /**
     * Start tracking time for referral
     */
    public void startReferralTracking(UUID referralId) {
        referralStartTime.put(referralId, System.currentTimeMillis());
        referralRewarded.put(referralId, false);
        saveReferrals();
    }

    /**
     * Get start time for referral
     */
    public Long getReferralStartTime(UUID referralId) {
        return referralStartTime.get(referralId);
    }

    /**
     * Check if referral has been rewarded for 7 hours
     */
    public boolean isReferralRewarded(UUID referralId) {
        return referralRewarded.getOrDefault(referralId, false);
    }

    /**
     * Mark referral as rewarded
     */
    public void markReferralRewarded(UUID referralId) {
        referralRewarded.put(referralId, true);
        saveReferrals();
    }

    /**
     * Get all active referrals (not rewarded yet)
     */
    public Map<UUID, Long> getActiveReferrals() {
        Map<UUID, Long> active = new HashMap<>();
        for (Map.Entry<UUID, Long> entry : referralStartTime.entrySet()) {
            if (!referralRewarded.getOrDefault(entry.getKey(), false)) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return active;
    }


}
