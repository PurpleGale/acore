package org.antagon.acore

import org.bukkit.plugin.java.JavaPlugin

class Acore : JavaPlugin() {
    override fun onEnable() {
        // Сохраняем дефолтную конфигурацию, если она не существует
        saveDefaultConfig()

        // Загружаем настройку включения функционала
        val isEnabled = config.getBoolean("fireAdjustment.enabled", true)
        if (!isEnabled) {
            logger.info("Fire adjustment is disabled in config.")
            return
        }

        // Парсим конфигурацию дропов
        val blockDropConfigs = parseBlockDrops(config)

        // Регистрируем обработчик событий
        server.pluginManager.registerEvents(BlockBurnDropListener(blockDropConfigs), this)

        logger.info("Fire Block Drop Plugin enabled!")
    }

    override fun onDisable() {
        logger.info("Fire Block Drop Plugin disabled!")
    }
}
