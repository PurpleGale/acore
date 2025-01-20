package org.antagon.acore.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.inventory.ItemStack
import org.antagon.acore.config.ConfigManager
import kotlin.random.Random

class BlockBurnListener : Listener {

    @EventHandler
    fun onBlockBurn(event: BlockBurnEvent) {
        if (!ConfigManager.fireConfig.isEnabled) return
        
        val burnedBlock = event.block
        val burnedBlockType = burnedBlock.type

        for (dropConfig in ConfigManager.fireConfig.blockDrops) {
            if (burnedBlockType in dropConfig.blocks) {
                if (Random.nextDouble() > dropConfig.chance) return

                val amount = (dropConfig.minAmount..dropConfig.maxAmount).random()
                val itemStack = ItemStack(dropConfig.item, amount)
                burnedBlock.location.world?.dropItemNaturally(burnedBlock.location, itemStack)
                return
            }
        }
    }
}
