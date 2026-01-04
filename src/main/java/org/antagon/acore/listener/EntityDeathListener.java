package org.antagon.acore.listener;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Check if MythicMobs is loaded
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            return;
        }

        // Check if the killer is a player
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player killer = (Player) event.getEntity().getKiller();

        try {
            // Use reflection to access MythicMobs classes
            var mythicMobsPlugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
            Class<?> mythicBukkitClass = null;

            if (mythicMobsPlugin != null) {
                try {
                    mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit", true, mythicMobsPlugin.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    // Fallback to system classloader
                    mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
                }
            } else {
                mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            }

            if (mythicBukkitClass == null) {
                return;
            }

            Method instMethod = mythicBukkitClass.getMethod("inst");
            Object mythicBukkit = instMethod.invoke(null);

            if (mythicBukkit == null) {
                return;
            }

            // Get mob manager
            Method getMobManagerMethod = mythicBukkitClass.getMethod("getMobManager");
            Object mobManager = getMobManagerMethod.invoke(mythicBukkit);

            if (mobManager == null) {
                return;
            }

            // Get active mob
            Method getActiveMobMethod = mobManager.getClass().getMethod("getActiveMob", java.util.UUID.class);
            Object activeMobOptional = getActiveMobMethod.invoke(mobManager, event.getEntity().getUniqueId());

            if (activeMobOptional == null) {
                return;
            }

            // Check if present
            Method isPresentMethod = activeMobOptional.getClass().getMethod("isPresent");
            boolean isPresent = (boolean) isPresentMethod.invoke(activeMobOptional);

            if (!isPresent) {
                return;
            }

            // Get the active mob
            Method getMethod = activeMobOptional.getClass().getMethod("get");
            Object activeMob = getMethod.invoke(activeMobOptional);

            if (activeMob == null) {
                return;
            }

            // Get mob type
            Method getMobTypeMethod = activeMob.getClass().getMethod("getMobType");
            String mobType = (String) getMobTypeMethod.invoke(activeMob);

            // Check if the mob type is "Klaus"
            if ("Klaus".equalsIgnoreCase(mobType)) {
                // Award the achievement
                awardAchievement(killer, "acore:new_year/holiday_savior");
            }
        } catch (Exception e) {
            // Log error if needed, but don't crash
            Bukkit.getLogger().warning("Error checking MythicMobs mob on death: " + e.getMessage());
        }
    }

    /**
     * Award achievement to player
     */
    private void awardAchievement(Player player, String advancementKey) {
        try {
            // Use console command to grant advancement
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "advancement grant " + player.getName() + " only " + advancementKey
            );
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error awarding achievement " + advancementKey + ": " + e.getMessage());
        }
    }
}
