package org.antagon.acore.integrations.ConditionalEvents.actions;

import ce.ajneb97.api.ConditionalEventsAction;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpawnMythicMob extends ConditionalEventsAction {

    public SpawnMythicMob() {
        super("spawn_mythic_mob");
    }

    @Override
    public void execute(Player player, String s) {
        // Format: spawn_mythic_mob: <mythic_mob_type>;<world>;<x>;<y>;<z>;<amount>
        String[] sep = s.split(";");

        String mobType = sep[0];
        Location location = new Location(
                Bukkit.getWorld(sep[1]),
                Double.parseDouble(sep[2]),
                Double.parseDouble(sep[3]),
                Double.parseDouble(sep[4])
        );
        int amount = sep.length > 5 ? Integer.parseInt(sep[5]) : 1;

        for (int i = 0; i < amount; i++) {
            MythicBukkit.inst().getMobManager().spawnMob(mobType, location);
        }
    }
}
