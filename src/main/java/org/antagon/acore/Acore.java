package org.antagon.acore;

import org.antagon.acore.core.ConfigManager;
import org.antagon.acore.listener.BannerHeadListener;
import org.antagon.acore.listener.BlockInteractionListener;
import org.antagon.acore.listener.FogPotionListener;
import org.antagon.acore.listener.ItemFrameListener;
import org.antagon.acore.listener.MinecartSpeedListener;
import org.antagon.acore.listener.VillagerTransportListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Acore extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Initialize config
        this.configManager = ConfigManager.initialize(getDataFolder(), getLogger());
        
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

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Acore plugin has been disabled");

        // !!! Жиза !!!
        //ConditionalEventsAPI.unregisterApiActions(this);
    }
}
