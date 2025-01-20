package your.package.name.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.inventory.ItemStack
import your.package.name.config.ConfigManager
import kotlin.random.Random

class BlockBurnDropListener : Listener {

    @EventHandler
    fun onBlockBurn(event: BlockBurnEvent) {
        val fireConfig = ConfigManager.fireConfig
        if (!fireConfig.isEnabled) return

        val burnedBlock = event.block.type

        for (dropConfig in fireConfig.blockDrops) {
            if (burnedBlock in dropConfig.blocks) {
                if (Random.nextDouble() > dropConfig.chance) return

                val amount = (dropConfig.minAmount..dropConfig.maxAmount).random()
                val itemStack = ItemStack(dropConfig.item, amount)
                burnedBlock.location.world?.dropItemNaturally(burnedBlock.location, itemStack)
                return
            }
        }
    }
}
