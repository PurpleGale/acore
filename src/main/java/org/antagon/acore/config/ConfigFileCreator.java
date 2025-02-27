package org.antagon.acore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigFileCreator {
    private final File configFile;
    private final Logger logger;

    public ConfigFileCreator(File dataFolder, Logger logger) {
        this.logger = logger;
        this.configFile = new File(dataFolder, "config.yml");
        createConfigIfAbsent();
    }

    public File getConfigFile() {
        return configFile;
    }

    public FileConfiguration getDefaultConfig() {
        InputStream resourceStream = getClass().getResourceAsStream("/config.yml");
        if (resourceStream == null) {
            logger.severe("Default configuration resource '/config.yml' not found.");
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream));
    }

    public FileConfiguration loadConfig(File configFile) {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void createConfigIfAbsent() {
        if (!configFile.exists()) {
            try {
                if (configFile.createNewFile()) {
                    logger.info("Created a new configuration file.");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create config file: " + e.getMessage(), e);
            }
        }
    }

    public void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save config file: " + e.getMessage(), e);
        }
    }
}
