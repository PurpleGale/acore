package org.antagon.acore.listener;

import org.antagon.acore.core.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ItemFrameListener implements Listener {

    private final ConfigManager config;

    public ItemFrameListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!config.getBoolean("invisibleItemFrames.enabled", true)) {
            return;
        }

        if (event.getRightClicked().getType() != EntityType.ITEM_FRAME && 
            event.getRightClicked().getType() != EntityType.GLOW_ITEM_FRAME) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        if (itemInHand.getType() != Material.SHEARS) {
            return;
        }
        
        ItemFrame frame = (ItemFrame) event.getRightClicked();
        
        if (!frame.getItem().getType().isAir()) {
            if (frame.isVisible() == false && config.getBoolean("invisibleItemFrames.toggleable", true)) {
                frame.setVisible(true);
            } else {
                frame.setVisible(false);
            }
            
            event.setCancelled(true);
        }
    }
}
