package org.antagon.acore;

import ce.ajneb97.api.ConditionalEventsAPI;
import org.antagon.acore.api.ConditionalEvents.actions.DropMythicItem;
import org.antagon.acore.api.ConditionalEvents.actions.SpawnMythicMob;
import org.bukkit.plugin.java.JavaPlugin;

public final class Acore extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ConditionalEventsAPI.registerApiActions(this,new SpawnMythicMob(), new DropMythicItem());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ConditionalEventsAPI.unregisterApiActions(this);
    }
}
