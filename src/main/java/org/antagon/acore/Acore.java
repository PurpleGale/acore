package org.antagon.acore;

import ce.ajneb97.api.ConditionalEventsAPI;

import org.antagon.acore.integration.ConditionalEvent.action.DropMythicItem;
import org.antagon.acore.integration.ConditionalEvent.action.SpawnMythicMob;
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
