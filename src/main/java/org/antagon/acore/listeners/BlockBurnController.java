package org.antagon.acore.listeners;

import java.util.ArrayList;
import java.util.Random;

import org.antagon.acore.core.ConfigManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBurnController implements Listener {
    private final boolean fireAdjustmentEnabled;
    private final ConfigurationSection blockDrops;
    private final ArrayList<String> blocks;

    private final int hsr; // horizontal search radius
    private final int vsr; // vertical search radius

    private final Random generator = new Random();
    
    public BlockBurnController() {

        ConfigManager config = ConfigManager.getInstance();

        this.fireAdjustmentEnabled = config.getBoolean("fireAdjustment.enabled", true);
        this.blocks = new ArrayList<String>();      
        this.blockDrops = config.getSection("fireAdjustment.block-drops");

        this.hsr = config.getInt("fireAdjustment.horizontal-search-radius");
        this.vsr = config.getInt("fireAdjustment.vertical-search-radius");

        // Добавляем каждый блок в массив чтобы можно было потом с ним работать
        for (String blockType : blockDrops.getKeys(false)) {
            for (String block : blockDrops.getConfigurationSection(blockType).getStringList("blocks")) {
                blocks.add(block);
            }
        }   
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {

        Block eventBlock = event.getBlock();
        String eventBlockType = eventBlock.getType().toString();

        if (!fireAdjustmentEnabled || !(blocks.contains(eventBlockType))) return;

        ConfigurationSection blockDrop = null;
        int minAmount = 0;
        int maxAmount = 0;
        double dropChance = 0.0;
        String item = null;

        for (String blockType : blockDrops.getKeys(false)) {
            if (blockDrops.getConfigurationSection(blockType).getStringList("blocks").contains(eventBlockType)) blockDrop = blockDrops.getConfigurationSection(blockType);

            minAmount  = blockDrop.getInt("min-amount");
            maxAmount  = blockDrop.getInt("max-amount");
            item       = blockDrop.getString("item");
            dropChance = blockDrop.getDouble("drop-chance");
            
        }

        if (dropChance > generator.nextDouble(0, 1)) {
            eventBlock.getWorld().dropItemNaturally(eventBlock.getLocation(), new ItemStack(Material.matchMaterial(item), generator.nextInt(minAmount, maxAmount)));
        }
    }
}
