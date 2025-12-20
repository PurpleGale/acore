package org.antagon.acore.listener;

import java.util.UUID;

import org.antagon.acore.util.ReferralManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ReferralListener implements Listener {

    private final JavaPlugin plugin;
    private final ReferralManager referralManager;

    public ReferralListener(JavaPlugin plugin, ReferralManager referralManager) {
        this.plugin = plugin;
        this.referralManager = referralManager;

        // Start scheduled task to check referral time every minute
        startReferralTimeChecker();
    }

    private void startReferralTimeChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkReferralTimes();
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Run every 60 seconds (1200 ticks)
    }

    private void checkReferralTimes() {
        plugin.getLogger().info("Checking referral times...");

        long currentTime = System.currentTimeMillis();
        long sevenHoursMs = 7 * 60 * 60 * 1000L;

        // Check all active referrals
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            plugin.getLogger().info("Checking player " + player.getName() + " (UUID: " + playerId + ")");

            if (referralManager.isReferral(playerId)) {
                plugin.getLogger().info("Player " + player.getName() + " is a referral");

                if (!referralManager.isReferralRewarded(playerId)) {
                    plugin.getLogger().info("Player " + player.getName() + " is not rewarded yet");

                    Long startTime = referralManager.getReferralStartTime(playerId);
                    if (startTime != null) {
                        long playedTime = currentTime - startTime;
                        long playedHours = playedTime / (60 * 60 * 1000L);

                        plugin.getLogger().info("Player " + player.getName() + " has played for " + playedHours + " hours");

                        if (playedTime >= sevenHoursMs) {
                            plugin.getLogger().info("Player " + player.getName() + " has completed 7 hours!");

                            // Give reward to inviter
                            UUID inviterId = referralManager.getInviter(playerId);
                            if (inviterId != null) {
                                Player inviter = Bukkit.getPlayer(inviterId);
                                if (inviter != null) {
                                    giveReward(inviter, 9);
                                    inviter.sendMessage("§aВаш реферал " + player.getName() + " отыграл 7 часов! Вы получили награду.");
                                }
                            }

                            // Mark as rewarded
                            referralManager.markReferralRewarded(playerId);
                            player.sendMessage("§aВы отыграли 7 часов как реферал! Ваш пригласивший получил награду.");
                            plugin.getLogger().info("Referral " + player.getName() + " completed 7 hours playtime");
                        }
                    } else {
                        plugin.getLogger().info("Player " + player.getName() + " has no start time");
                    }
                } else {
                    plugin.getLogger().info("Player " + player.getName() + " is already rewarded");
                }
            } else {
                plugin.getLogger().info("Player " + player.getName() + " is not a referral");
            }
        }

        plugin.getLogger().info("Referral time check completed");
    }

    private void handleAccept(Player referral, String inviterName, String referralName) {
        // Verify that the command is for this player
        if (!referral.getName().equals(referralName)) {
            return;
        }

        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter == null) {
            referral.sendMessage("§cПригласивший игрок не найден!");
            return;
        }

        // Check if already a referral
        if (referralManager.isReferral(referral.getUniqueId())) {
            referral.sendMessage("§cВы уже являетесь рефералом!");
            return;
        }

        // Add referral
        referralManager.addReferral(referral.getUniqueId(), inviter.getUniqueId());

        // Give initial reward to inviter
        giveReward(inviter, 1);

        // Start tracking time
        referralManager.startReferralTracking(referral.getUniqueId());

        referral.sendMessage("§aВы приняли приглашение от " + inviterName + "!");
        inviter.sendMessage("§aИгрок " + referralName + " принял ваше приглашение!");

        plugin.getLogger().info("Player " + referralName + " accepted referral from " + inviterName);
    }

    private void handleDecline(Player referral, String inviterName, String referralName) {
        // Verify that the command is for this player
        if (!referral.getName().equals(referralName)) {
            return;
        }

        referral.sendMessage("§cВы отклонили приглашение от " + inviterName);

        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter != null) {
            inviter.sendMessage("§cИгрок " + referralName + " отклонил ваше приглашение");
        }

        plugin.getLogger().info("Player " + referralName + " declined referral from " + inviterName);
    }

    private void giveReward(Player player, int amount) {
        String command = "antacoin give " + player.getName() + " " + amount;
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        // Check if this is a referral command
        if (message.startsWith("/referral_accept ")) {
            event.setCancelled(true);
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                handleAccept(player, parts[1], parts[2]);
            }
        } else if (message.startsWith("/referral_decline ")) {
            event.setCancelled(true);
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                handleDecline(player, parts[1], parts[2]);
            }
        }
    }
}
