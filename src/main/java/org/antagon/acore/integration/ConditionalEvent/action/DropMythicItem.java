package org.antagon.acore.integration.ConditionalEvent.action;

import ce.ajneb97.api.ConditionalEventsAction;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DropMythicItem extends ConditionalEventsAction {

    public DropMythicItem() {
        super("drop_mythic_item");
    }

    @Override
    public void execute(Player player, String actionLine) {
        // Format: drop_mythic_item:<item_id>;<amount>;<world>;<x>;<y>;<z>
        String[] args = actionLine.split(";");

        String itemID = args[0];
        int amount = Integer.parseInt(args[1]);
        Location location = new Location(
                Bukkit.getWorld(args[2]),
                Double.parseDouble(args[3]),
                Double.parseDouble(args[4]),
                Double.parseDouble(args[5])
        );

        MythicBukkit.inst().getItemManager().getItem(itemID).ifPresent(mythicItem -> {
            ItemStack itemStack = (ItemStack) mythicItem.generateItemStack(amount);
            location.getWorld().dropItem(location, itemStack);
        });
    }
}
