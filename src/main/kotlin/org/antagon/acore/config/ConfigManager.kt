package your.package.name.config

import org.bukkit.configuration.file.FileConfiguration
import your.package.name.YourPlugin

object ConfigManager {
    private lateinit var plugin: YourPlugin
    lateinit var config: FileConfiguration

    // Классы по обработке частей настроек
    lateinit var fireConfig: FireConfig

    fun init(pluginInstance: YourPlugin) {
        plugin = pluginInstance
        config = plugin.config
        plugin.saveDefaultConfig()

        // Инициализируем подмодули конфигурации
        fireConfig = FireConfig(config)
    }

    fun reload() {
        plugin.reloadConfig()
        config = plugin.config

        // Обновляем данные подмодулей
        fireConfig.reload(config)
    }
}
