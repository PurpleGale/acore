package org.antagon.acore.config;

public interface ConfigInterface {
    String getString(String path, String def);
    int getInt(String path, int def);
    boolean getBoolean(String path, boolean def);

    String getStringOrDefault(String path, String def);
    int getIntOrDefault(String path, int def);
    boolean getBooleanOrDefault(String path, boolean def);

    void set(String path, Object value);
}
