package org.antagon.acore.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager implements ConfigInterface {
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

    public static void resetInstance() {
        instance = null;
    }

    private <T> T getValue(String path, T def, boolean useDefault) {
        if (useDefault || config.contains(path)) {
            Object value = config.get(path, def);
            return (T) value;
        }
        return def;
    }

    @Override
    public String getString(String path, String def) {
        return getValue(path, def, false);
    }

    @Override
    public int getInt(String path, int def) {
        return getValue(path, def, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return getValue(path, def, false);
    }

    @Override
    public String getStringOrDefault(String path, String def) {
        return getValue(path, def, true);
    }

    @Override
    public int getIntOrDefault(String path, int def) {
        return getValue(path, def, true);
    }

    @Override
    public boolean getBooleanOrDefault(String path, boolean def) {
        return getValue(path, def, true);
    }

    @Override
    public void set(String path, Object value) {
        config.set(path, value);
        configFileCreator.saveConfig(config, configFile);
    }
}
