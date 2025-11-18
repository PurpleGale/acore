package org.antagon.acore.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CurseManager {

    private final JavaPlugin plugin;
    private final File curseFile;
    private final Set<UUID> cursedPlayers;
    private YamlConfiguration curseConfig;

    public CurseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.curseFile = new File(plugin.getDataFolder(), "cursed_players.yml");
        this.cursedPlayers = new HashSet<>();
        loadCursedPlayers();
    }

    /**
     * Load cursed players from file
     */
    private void loadCursedPlayers() {
        if (!curseFile.exists()) {
            try {
                curseFile.createNewFile();
                curseConfig = YamlConfiguration.loadConfiguration(curseFile);
                curseConfig.set("cursed-players", new ArrayList<String>());
                curseConfig.save(curseFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create cursed players file: " + e.getMessage());
                return;
            }
        }

        curseConfig = YamlConfiguration.loadConfiguration(curseFile);
        List<String> cursedUUIDs = curseConfig.getStringList("cursed-players");

        for (String uuidStr : cursedUUIDs) {
            try {
                cursedPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in cursed players file: " + uuidStr);
            }
        }

        plugin.getLogger().info("Loaded " + cursedPlayers.size() + " cursed players");
    }

    /**
     * Save cursed players to file
     */
    private void saveCursedPlayers() {
        List<String> cursedUUIDs = new ArrayList<>();
        for (UUID uuid : cursedPlayers) {
            cursedUUIDs.add(uuid.toString());
        }

        curseConfig.set("cursed-players", cursedUUIDs);

        try {
            curseConfig.save(curseFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save cursed players file: " + e.getMessage());
        }
    }

    /**
     * Add player to cursed list
     */
    public void addCursedPlayer(UUID playerId) {
        cursedPlayers.add(playerId);
        saveCursedPlayers();
    }

    /**
     * Remove player from cursed list
     */
    public void removeCursedPlayer(UUID playerId) {
        cursedPlayers.remove(playerId);
        saveCursedPlayers();
    }

    /**
     * Check if player is cursed
     */
    public boolean isPlayerCursed(UUID playerId) {
        return cursedPlayers.contains(playerId);
    }

    /**
     * Get all cursed players
     */
    public Set<UUID> getCursedPlayers() {
        return new HashSet<>(cursedPlayers);
    }
}
