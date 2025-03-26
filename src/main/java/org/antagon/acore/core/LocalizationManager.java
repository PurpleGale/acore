package org.antagon.acore.core;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocalizationManager {
    private final File languageFolder;
    private final String defaultLanguage;
    private final Map<String, YamlConfiguration> translations = new HashMap<>();

    public LocalizationManager(File languageFolder, String defaultLanguage) {
        this.languageFolder = languageFolder;
        this.defaultLanguage = defaultLanguage;
    }

    public void loadLanguages() {
        if (!languageFolder.exists()) languageFolder.mkdirs();
        for (File file : languageFolder.listFiles((dir, name) -> name.endsWith(".yml"))) {
            String langCode = file.getName().replace(".yml", "");
            translations.put(langCode, YamlConfiguration.loadConfiguration(file));
        }
    }

    public String translate(String key, String language) {
        YamlConfiguration langConfig = translations.getOrDefault(language, translations.get(defaultLanguage));
        return langConfig != null ? langConfig.getString(key, key) : key;
    }
}
