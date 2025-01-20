package your.package.name.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.Material

class FireConfig(private var config: FileConfiguration) {
    var isEnabled: Boolean = true
        private set
    var fireLimit: Int = 20
        private set
    var horizontalRadius: Int = 10
        private set
    var verticalRadius: Int = 5
        private set
    val blockDrops: List<BlockDropConfig> = mutableListOf()

    init {
        load()
    }

    fun reload(newConfig: FileConfiguration) {
        config = newConfig
        load()
    }

    private fun load() {
        isEnabled = config.getBoolean("fireAdjustment.enabled", true)
        fireLimit = config.getInt("fireAdjustment.fire-limit", 20)
        horizontalRadius = config.getInt("fireAdjustment.horizontal-search-radius", 10)
        verticalRadius = config.getInt("fireAdjustment.vertical-search-radius", 5)

        // Загружаем блоки и их дропы
        val blockDropsSection = config.getConfigurationSection("fireAdjustment.block-drops") ?: return
        (blockDrops as MutableList).clear()

        for (categoryKey in blockDropsSection.getKeys(false)) {
            val categorySection = blockDropsSection.getConfigurationSection(categoryKey) ?: continue
            val blocks = categorySection.getStringList("blocks").mapNotNull { Material.matchMaterial(it) }
            val item = Material.matchMaterial(categorySection.getString("item") ?: "") ?: continue
            val minAmount = categorySection.getInt("min-amount", 1)
            val maxAmount = categorySection.getInt("max-amount", 1)
            val chance = categorySection.getDouble("chance", 1.0)

            blockDrops.add(BlockDropConfig(blocks, item, minAmount, maxAmount, chance))
        }
    }
}

data class BlockDropConfig(
    val blocks: List<Material>,
    val item: Material,
    val minAmount: Int,
    val maxAmount: Int,
    val chance: Double
)
