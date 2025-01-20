package org.antagon.acore.config

import org.bukkit.configuration.file.FileConfiguration
import kotlin.text.startsWith
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration

class LanguageManager(private var config: FileConfiguration) {
    private val defaultLanguage = "en-US"
    private var currentLanguage: String = config.getString("language", defaultLanguage) ?: defaultLanguage
    
    fun getMessage(key: String): String {
        // Trying to get message from configured language
        val message = config.getString(key)
        if (message != null) return message
        
        // If message not found, trying to get message from default language
        if (currentLanguage != defaultLanguage) {
            // Loading default language configuration
            val defaultLangConfig = loadLanguageFile(defaultLanguage)
            if (defaultLangConfig != null) {
                return defaultLangConfig.getString(key) ?: key
            }
        }
        
        return key
    }
    
    private fun loadLanguageFile(lang: String): FileConfiguration? {
        val langFile = File(config.getCurrentPath(), "languages/$lang.yml")
        if (!langFile.exists()) return null
        
        return YamlConfiguration.loadConfiguration(langFile)
    }
    
    fun setLanguage(lang: String) {
        val langFile = loadLanguageFile(lang)
        if (langFile != null) {
            currentLanguage = lang
            config = langFile
        } else {
            // If language file not found, using default language
            val defaultLangFile = loadLanguageFile(defaultLanguage)
            if (defaultLangFile != null) {
                currentLanguage = defaultLanguage
                config = defaultLangFile
            }
        }
    }
}