package org.antagon.acore

import org.bukkit.plugin.java.JavaPlugin
import org.antagon.acore.listeners.BlockBurnDropListener
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.Material
import org.antagon.acore.config.BlockDropConfig

class Acore : JavaPlugin() {
    private fun parseBlockDrops(config: FileConfiguration): List<BlockDropConfig> {
        val drops = mutableListOf<BlockDropConfig>()
        val dropsSection = config.getConfigurationSection("fireAdjustment.blockDrops") ?: return drops
        
        for (key in dropsSection.getKeys(false)) {
            val section = dropsSection.getConfigurationSection(key) ?: continue
            val blocks = section.getStringList("blocks").mapNotNull { Material.getMaterial(it) }
            val item = Material.getMaterial(section.getString("item", "") ?: "") ?: continue
            val chance = section.getDouble("chance", 0.0)
            val minAmount = section.getInt("minAmount", 1)
            val maxAmount = section.getInt("maxAmount", 1)
            
            drops.add(BlockDropConfig(blocks, item, chance, minAmount, maxAmount))
        }
        return drops
    }

    override fun onEnable() {
        // Saving default configuration if it doesn't exist
        saveDefaultConfig()

        // Loading fire adjustment configuration
        val isEnabled = config.getBoolean("fireAdjustment.enabled", true)
        if (!isEnabled) {
            logger.info("Fire adjustment is disabled in config.")
            return
        }

        // Parsing block drops configuration
        val blockDropConfigs = parseBlockDrops(config)

        // Registering event handler
        server.pluginManager.registerEvents(BlockBurnDropListener(blockDropConfigs), this)

        logger.info("Fire Block Drop Plugin enabled!")
    }

    override fun onDisable() {
        logger.info("Fire Block Drop Plugin disabled!")
    }
}
