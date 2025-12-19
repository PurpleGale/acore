package org.antagon.acore.listener;

import java.util.UUID;

import org.antagon.acore.util.ReferralManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ReferralListener implements Listener {

    private final JavaPlugin plugin;
    private final ReferralManager referralManager;

    public ReferralListener(JavaPlugin plugin, ReferralManager referralManager) {
        this.plugin = plugin;
        this.referralManager = referralManager;
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if this is an active referral that needs time tracking
        if (referralManager.isReferral(playerId) && !referralManager.isReferralRewarded(playerId)) {
            Long startTime = referralManager.getReferralStartTime(playerId);
            if (startTime != null) {
                long currentTime = System.currentTimeMillis();
                long playedTime = currentTime - startTime;

                // 7 hours = 7 * 60 * 60 * 1000 = 25200000 milliseconds
                long sevenHoursMs = 7 * 60 * 60 * 1000L;

                if (playedTime >= sevenHoursMs) {
                    // Give reward to inviter
                    UUID inviterId = referralManager.getInviter(playerId);
                    if (inviterId != null) {
                        Player inviter = Bukkit.getPlayer(inviterId);
                        if (inviter != null) {
                            giveReward(inviter, 9);
                            inviter.sendMessage("§aВаш реферал " + player.getName() + " отыграл 7 часов! Вы получили награду.");
                        } else {
                            // Inviter offline, still give reward when they join
                            // This will be handled when inviter joins
                        }
                    }

                    // Mark as rewarded
                    referralManager.markReferralRewarded(playerId);

                    player.sendMessage("§aВы отыграли 7 часов как реферал! Ваш пригласивший получил награду.");

                    plugin.getLogger().info("Referral " + player.getName() + " completed 7 hours playtime");
                }
            }
        }
    }
}
