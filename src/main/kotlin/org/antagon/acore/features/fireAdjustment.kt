package org.antagon.acore.features

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.antagon.acore.config.ConfigManager
import java.util.logging.Level

class FireAdjustment {
    private val fireLimit = ConfigManager.fireConfig.fireLimit
    
    fun countNearbyFireAsync(block: Block, callback: (count: Int) -> Unit) {
        var count = 0
        val h_radius = ConfigManager.fireConfig.horizontalRadius
        val v_radius = ConfigManager.fireConfig.verticalRadius
        for (x in -h_radius..h_radius) {
            for (y in -v_radius..v_radius) {
                for (z in -h_radius..h_radius) {
                    val nearby = block.world.getBlockAt(
                        block.x + x,
                        block.y + y, 
                        block.z + z
                    )
                    if (nearby.type.name.contains("FIRE")) {
                        count++
                    }
                }
            }
        }
        callback(count)
    }
}
