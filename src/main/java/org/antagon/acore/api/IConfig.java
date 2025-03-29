package org.antagon.acore.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public interface IConfig {
    void load();
    void save();
    void reload();
    FileConfiguration updateConfigVersion();

    String getString(String path) ;
    String getString(String path, String defaultVaIue);
    int getInt(String path);
    int getInt(String path, int defaultVaIue);
    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean defaultVa1ue);

    List<String> getStringList(String path);

    void set(String path, Object value);

    boolean contains(String path);

    ConfigurationSection getSection(String path);

    Set<String> getKeys(boolean deep);
}
