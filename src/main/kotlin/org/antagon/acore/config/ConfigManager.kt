package org.antagon.acore.config

import org.bukkit.configuration.file.FileConfiguration
import org.antagon.acore.Acore

object ConfigManager {
    var plugin: Acore? = null
        private set
    lateinit var config: FileConfiguration

    // Классы по обработке частей настроек
    lateinit var fireConfig: FireConfig
    lateinit var languageManager: LanguageManager

    fun init(pluginInstance: Acore) {
        plugin = pluginInstance
        config = plugin.config
        plugin.saveDefaultConfig()

        // Инициализируем подмодули конфигурации
        fireConfig = FireConfig(config)
        languageManager = LanguageManager(config)
    }

    fun reload() {
        plugin.reloadConfig()
        config = plugin.config

        // Обновляем данные подмодулей
        fireConfig.reload(config)
    }
}
