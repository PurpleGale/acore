package org.antagon.acore.listener;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.util.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBurnListener implements Listener {
    private final boolean fireAdjustmentEnabled;
    private final Set<Material> validBlocks = new HashSet<>();
    private final Map<Material, BlockDropConfig> blockDropConfigs = new EnumMap<>(Material.class);
    private final Random generator = new Random();

    public BlockBurnListener() {
        ConfigManager config = ConfigManager.getInstance();
        this.fireAdjustmentEnabled = config.getBoolean("fireAdjustment.enabled", true);
        
        ConfigurationSection blockDrops = config.getSection("fireAdjustment.block-drops");
        if (blockDrops == null) return;
        
        for (String blockType : blockDrops.getKeys(false)) {
            ConfigurationSection section = blockDrops.getConfigurationSection(blockType);
            if (section == null) continue;
            
            BlockDropConfig dropConfig = new BlockDropConfig(
                section.getString("item"),
                section.getInt("min-amount"),
                section.getInt("max-amount"),
                section.getDouble("drop-chance")
            );
            
            for (String blockName : section.getStringList("blocks")) {
                try {
                    Material material = MaterialValidator.validateMaterial(blockName);

                    validBlocks.add(material);
                    blockDropConfigs.put(material, dropConfig);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!fireAdjustmentEnabled) return;
        
        Block eventBlock = event.getBlock();
        Material blockType = eventBlock.getType();
        
        if (!validBlocks.contains(blockType)) return;
        
        BlockDropConfig dropConfig = blockDropConfigs.get(blockType);
        if (dropConfig == null) return;
        
        if (generator.nextDouble() < dropConfig.dropChance) {
            Material material = Material.matchMaterial(dropConfig.item);
            if (material != null) {
                if (dropConfig.dropChance > generator.nextDouble(0, 1)) {
                    eventBlock.getWorld().dropItemNaturally(
                        eventBlock.getLocation(), 
                        new ItemStack(material, generator.nextInt(dropConfig.minAmount, dropConfig.maxAmount))
                    );
                }
            }
        }
    }
    
    private static class BlockDropConfig {
        final String item;
        final int minAmount;
        final int maxAmount;
        final double dropChance;
        
        BlockDropConfig(String item, int minAmount, int maxAmount, double dropChance) {
            this.item = item;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.dropChance = dropChance;
        }
    }
}