package org.antagon.acore.core;

import org.antagon.acore.api.IConfig;
import org.antagon.acore.config.ConfigFileCreator;
import org.antagon.acore.config.ConfigUpdater;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ConfigManager implements IConfig {
    private static ConfigManager instance;

    private final File configFile;
    private final Logger logger;
    private FileConfiguration config;

    private final ConfigFileCreator configFileCreator;
    private final ConfigUpdater configUpdater;

    private ConfigManager(File dataFolder, Logger pluginLogger) {
        this.logger = pluginLogger;
        this.configFileCreator = new ConfigFileCreator(dataFolder, pluginLogger);
        this.configUpdater = new ConfigUpdater(pluginLogger);

        this.configFile = configFileCreator.getConfigFile();
        this.config = configFileCreator.loadConfig(configFile);

        FileConfiguration defaultConfig = configFileCreator.getDefaultConfig();
        this.config = configUpdater.updateConfiguration(config, defaultConfig, configFile);
    }

    public static ConfigManager initialize(File dataFolder, Logger pluginLogger) {
        if (instance == null) instance = new ConfigManager(dataFolder, pluginLogger);
        return instance;
    }

    public static ConfigManager getInstance() {
        if (instance == null) throw new IllegalStateException("ConfigManager has not been initialized.");
        return instance;
    }

    public void load() {
        try {
            config = configFileCreator.loadConfig(configFile);
        } catch (Exception e) {
            logger.severe("Failed to load configuration from " + configFile.getName() + ": " + e.getMessage());
        }
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            logger.severe("Failed to save configuration to " + configFile.getPath() + ": " + e.getMessage());
        }
    }

    public void reload() {
        try {
            this.config = configFileCreator.loadConfig(configFile);
        } catch (Exception e) {
            logger.severe("Failed to reload configuration: " + e.getMessage());
        }
    }

    public FileConfiguration updateConfigVersion() {
        FileConfiguration defaultConfig = configFileCreator.getDefaultConfig();
        this.config = configUpdater.updateConfiguration(config, defaultConfig, configFile);
        return this.config;
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public double getDouble(String path, Double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public Set<String> getKeys(boolean deep) {
        return config.getKeys(deep);
    }
}
