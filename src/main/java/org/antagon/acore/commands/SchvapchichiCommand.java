package org.antagon.acore.commands;

import java.util.UUID;

import org.antagon.acore.util.CurseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class SchvapchichiCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final CurseManager curseManager;

    public SchvapchichiCommand(JavaPlugin plugin, CurseManager curseManager) {
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
        if (!player.hasPermission("acore.schvapchichi")) {
            player.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }

        // Check if feature is enabled
        if (!plugin.getConfig().getBoolean("schvapchichi.enabled", true)) {
            player.sendMessage("§cЭта фича временно отключена!");
            return true;
        }

        // Apply curse to player
        applyCurse(player);

        return true;
    }

    private void applyCurse(Player player) {
        UUID playerId = player.getUniqueId();

        // Add player to permanent curse list
        curseManager.addCursedPlayer(playerId);

        // Mark player as cursed (we'll use metadata for this)
        player.setMetadata("schvapchichi_cursed", new FixedMetadataValue(plugin, true));

        // Grant the advancement directly using console command
        plugin.getServer().dispatchCommand(
            plugin.getServer().getConsoleSender(),
            "advancement grant " + player.getName() + " only acore:schvapchichi/root"
        );

        // Send curse message
        player.sendMessage("§7Швапчичи заметило вас...");
    }
}
