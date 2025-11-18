package org.antagon.acore.commands;

import java.util.UUID;

import org.antagon.acore.util.CurseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiSchvapchichiCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final CurseManager curseManager;

    public AntiSchvapchichiCommand(JavaPlugin plugin, CurseManager curseManager) {
        this.plugin = plugin;
        this.curseManager = curseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;

        // Check permissions
        if (!player.hasPermission("acore.a_schvapchichi")) {
            player.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }

        // Check if feature is enabled
        if (!plugin.getConfig().getBoolean("schvapchichi.enabled", true)) {
            player.sendMessage("§cЭта фича временно отключена!");
            return true;
        }

        // Remove curse from player
        removeCurse(player);

        return true;
    }

    private void removeCurse(Player player) {
        UUID playerId = player.getUniqueId();

        // Remove player from permanent curse list
        curseManager.removeCursedPlayer(playerId);

        // Remove metadata curse
        if (player.hasMetadata("schvapchichi_cursed")) {
            player.removeMetadata("schvapchichi_cursed", plugin);
        }

        // Send success message
        player.sendMessage("§aВы избавились от проклятья Швапчичи!");
        player.sendMessage("§7Теперь ваши предметы в безопасности.");
    }
}
