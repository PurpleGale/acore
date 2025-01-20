package org.antagon.acore.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.block.Block
import java.util.logging.Level
import org.antagon.acore.config.ConfigManager
import org.antagon.acore.config.LanguageManager
import kotlin.random.Random
import org.antagon.acore.features.FireAdjustment

class BlockIgniteListener : Listener {
    private val fireAdjustment = FireAdjustment()
    private val fireLimit = ConfigManager.fireConfig.fireLimit

    @EventHandler
    fun onBlockIgnite(event: BlockIgniteEvent) {
        if (!ConfigManager.fireConfig.isEnabled) return
        if (event.isCancelled) return

        val block = event.block
        fireAdjustment.countNearbyFireAsync(block) { count ->
            if (count >= fireLimit) {
                event.isCancelled = true
            }
        }
    }
}
