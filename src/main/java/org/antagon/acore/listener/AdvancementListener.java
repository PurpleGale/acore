package org.antagon.acore.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListener implements Listener {

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        String advancementKey = event.getAdvancement().getKey().toString();
        String playerName = event.getPlayer().getName();

        if ("acore:new_year/candy_eater".equals(advancementKey)) {
            String command = "lp user " + playerName + " permission set uniqueprefix.candyeater true";
            // Execute the command as console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else if ("acore:new_year/holiday_savior".equals(advancementKey)) {
            String command = "lp user " + playerName + " permission set uniqnameplates.krampus true";
            // Execute the command as console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
