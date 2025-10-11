package org.antagon.acore;

import java.lang.reflect.Constructor;

import org.antagon.acore.commands.ShowInfoCommand;
import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.listener.BannerHeadListener;
import org.antagon.acore.listener.BlockInteractionListener;
import org.antagon.acore.listener.FogPotionListener;
import org.antagon.acore.listener.ItemFrameListener;
import org.antagon.acore.listener.MinecartSpeedListener;
import org.antagon.acore.listener.VillagerTransportListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Acore extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Initialize config
        this.configManager = ConfigManager.initialize(getDataFolder(), getLogger());

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        getLogger().info("Acore plugin has been enabled successfully!");

        // !!! С этим полем плагин не заводится, хз почему, поэтому пока так !!!
        // потом почекаю
        //ConditionalEventsAPI.registerApiActions(this,new SpawnMythicMob(), new DropMythicItem());
    }

    private void registerListeners() {
        // Register VillagerTransportListener if enabled in config
        if (configManager.getBoolean("villagerTransport.enabled", true)) {
            getServer().getPluginManager().registerEvents(
                new VillagerTransportListener(this, configManager), this);
            getLogger().info("Villager Transportation feature enabled");
        }

        // Register MinecartSpeedListener if enabled in config
        if (configManager.getBoolean("minecartSpeed.enabled", true)) {
            getServer().getPluginManager().registerEvents(new MinecartSpeedListener(), this);
            getLogger().info("Minecart Speed feature enabled");
        }

        getServer().getPluginManager().registerEvents(new ItemFrameListener(configManager), this);

        // Register BlockInteractionListener for tracking player block interactions
        getServer().getPluginManager().registerEvents(new BlockInteractionListener(), this);
        getLogger().info("Block Interaction Tracker enabled");

        // Register FogPotionListener if enabled in config
        if (configManager.getBoolean("fogPotion.enabled", true)) {
            getServer().getPluginManager().registerEvents(new FogPotionListener(this, configManager), this);
            getLogger().info("Fog Potion feature enabled");
        }

        // Register BannerHeadListener if enabled in config
        if (configManager.getBoolean("bannerHead.enabled", true)) {
            getServer().getPluginManager().registerEvents(new BannerHeadListener(configManager), this);
            getLogger().info("Banner Head feature enabled");
        }
    }

    private void registerCommands() {
        // Register showinfo command using Paper API
        try {
            var commandMap = getServer().getCommandMap();
            var command = commandMap.getCommand("showinfo");
            if (command == null) {
                // Create command if it doesn't exist using reflection
                Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                constructor.setAccessible(true);
                command = constructor.newInstance("showinfo", this);
                command.setDescription("Переключить отображение боссбара");
                command.setUsage("/showinfo");
                ((PluginCommand) command).setExecutor(new ShowInfoCommand());
                commandMap.register("acore", command);
            } else {
                ((PluginCommand) command).setExecutor(new ShowInfoCommand());
            }
            getLogger().info("ShowInfo command registered");
        } catch (Exception e) {
            getLogger().warning("Failed to register showinfo command: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Acore plugin has been disabled");

        // !!! Жиза !!!
        //ConditionalEventsAPI.unregisterApiActions(this);
    }
}
