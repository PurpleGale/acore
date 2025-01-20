import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class FireLimiter extends JavaPlugin implements Listener {

    private int fireLimit;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        fireLimit = getConfig().getInt("fire-limit");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        countNearbyFireAsync(block, (count) -> {
            if (count >= fireLimit) {
                event.setCancelled(true);
                getLogger().log(Level.INFO, "Fire spread has been prevented near block at: " + block.getLocation());
            }
        });
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        countNearbyFireAsync(block, (count) -> {
            if (count >= fireLimit) {
                event.setCancelled(true);
                getLogger().log(Level.INFO, "Fire burning has been prevented near block at: " + block.getLocation());
            }
        });
    }

    private void countNearbyFireAsync(Block block, FireCountCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int count = 0;
                int radius = getConfig().getInt("search-radius");
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block nearbyBlock = block.getRelative(x, y, z);
                            // Проверяем, является ли блок огнем
                            if (nearbyBlock.getType() == Material.FIRE) {
                                count++;
                            }
                        }
                    }
                }
                callback.onCountComplete(count);
            }
        }.runTaskAsynchronously(this);
    }

    private interface FireCountCallback {
        void onCountComplete(int count);
    }
}
