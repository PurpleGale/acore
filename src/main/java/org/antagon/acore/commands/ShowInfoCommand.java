package org.antagon.acore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowInfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;

        String playerName = player.getName();

        boolean hasPermission = player.hasPermission("bossbar.show");

        if (hasPermission) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + playerName + " permission set bossbar.show false");
            player.sendMessage("§cBossbar отключен!");
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + playerName + " permission set bossbar.show true");
            player.sendMessage("§aBossbar включен!");
        }

        return true;
    }
}
