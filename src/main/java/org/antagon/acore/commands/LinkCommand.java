package org.antagon.acore.commands;

import java.util.UUID;

import org.antagon.acore.util.ReferralManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LinkCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ReferralManager referralManager;

    public LinkCommand(JavaPlugin plugin, ReferralManager referralManager) {
        this.plugin = plugin;
        this.referralManager = referralManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }

        Player inviter = (Player) sender;

        // Check permissions
        if (!inviter.hasPermission("acore.link")) {
            inviter.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }

        // Check if feature is enabled
        if (!plugin.getConfig().getBoolean("referrals.enabled", true)) {
            inviter.sendMessage("§cЭта фича временно отключена!");
            return true;
        }

        if (args.length != 1) {
            inviter.sendMessage("§cИспользование: /link <ник_реферала>");
            return true;
        }

        String referralName = args[0];
        Player referral = Bukkit.getPlayer(referralName);

        if (referral == null) {
            inviter.sendMessage("§cИгрок " + referralName + " не онлайн!");
            return true;
        }

        if (referral.equals(inviter)) {
            inviter.sendMessage("§cВы не можете пригласить самого себя!");
            return true;
        }

        UUID referralId = referral.getUniqueId();

        if (referralManager.isReferral(referralId)) {
            inviter.sendMessage("§cЭтот игрок уже является рефералом!");
            return true;
        }

        // Send invitation message to referral
        sendInvitationMessage(inviter, referral);

        inviter.sendMessage("§aПриглашение отправлено игроку " + referralName);

        return true;
    }

    private void sendInvitationMessage(Player inviter, Player referral) {
        Component message = Component.text("Вы являетесь рефералом игрока ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(inviter.getName()).color(NamedTextColor.GREEN))
                .append(Component.text(". Принять приглашение?\n").color(NamedTextColor.YELLOW))
                .append(Component.text("[ДА]").color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/referral_accept " + inviter.getName() + " " + referral.getName())))
                .append(Component.text(" "))
                .append(Component.text("[НЕТ]").color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/referral_decline " + inviter.getName() + " " + referral.getName())));

        referral.sendMessage(message);
    }
}
