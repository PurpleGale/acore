package org.antagon.acore.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.antagon.acore.util.CurseManager;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class SchvapchichiListener implements Listener {

    private final JavaPlugin plugin;
    private final Random random;
    private final CurseManager curseManager;
    private final Map<UUID, Long> lastBeehiveCheck;

    public SchvapchichiListener(JavaPlugin plugin, CurseManager curseManager) {
        this.plugin = plugin;
        this.random = new Random();
        this.curseManager = curseManager;
        this.lastBeehiveCheck = new HashMap<>();
    }

    /**
     * Handle player join event - check for cursed players and potentially steal items
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if player is cursed (from file or metadata)
        if (player.hasMetadata("schvapchichi_cursed") || curseManager.isPlayerCursed(playerId)) {
            plugin.getLogger().info("Player " + player.getName() + " is cursed, checking for item steal...");

            if (shouldStealItem()) {
                plugin.getLogger().info("Stealing item from " + player.getName());
                stealRandomItem(player);
            } else {
                plugin.getLogger().info("No item stolen from " + player.getName() + " this time");
            }
        }
    }

    /**
     * Handle bee breeding event - chance to drop special item
     */
    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getEntity() instanceof Bee) || !(event.getMother() instanceof Bee)) {
            return;
        }

        // Check if feature is enabled
        if (!plugin.getConfig().getBoolean("schvapchichi.enabled", true)) {
            return;
        }

        double dropChance = plugin.getConfig().getDouble("schvapchichi.bee.dropChance", 0.001);

        if (random.nextDouble() < dropChance) {
            // Create special potion item
            ItemStack specialPotion = createSpecialPotion();

            // Drop the item at the breeding location
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), specialPotion);
        }
    }

    /**
     * Steal a random item from player's inventory
     */
    private void stealRandomItem(Player player) {
        ItemStack[] contents = player.getInventory().getContents();

        // Find non-empty slots
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
            }
        }

        if (!items.isEmpty()) {
            // Select random item
            ItemStack stolenItem = items.get(random.nextInt(items.size()));

            plugin.getLogger().info("Selected item to steal: " + stolenItem.getType() + " x" + stolenItem.getAmount());

            // Create a copy of the item to remove
            ItemStack itemToRemove = stolenItem.clone();

            // Remove one from the stack
            if (stolenItem.getAmount() > 1) {
                stolenItem.setAmount(stolenItem.getAmount() - 1);
                itemToRemove.setAmount(1);
            } else {
                // Remove the entire item from inventory
                player.getInventory().removeItem(stolenItem);
            }

            // Send message to player
            String message = plugin.getConfig().getString("schvapchichi.curse.message", "Швапчичи забирает это...");
            player.sendMessage("§c" + message);

            plugin.getLogger().info("Successfully stole item from " + player.getName());
        } else {
            plugin.getLogger().info("No items to steal from " + player.getName());
        }
    }

    /**
     * Determine if item should be stolen based on configured chance
     */
    private boolean shouldStealItem() {
        double stealChance = plugin.getConfig().getDouble("schvapchichi.curse.stealChance", 0.05);
        double roll = random.nextDouble();
        boolean shouldSteal = roll < stealChance;
        plugin.getLogger().info("Steal chance: " + stealChance + ", rolled: " + roll + ", should steal: " + shouldSteal);
        return shouldSteal;
    }

    /**
     * Create the special potion item with custom model data
     */
    private ItemStack createSpecialPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        ItemMeta meta = potion.getItemMeta();

        if (meta != null) {
            int customModelData = plugin.getConfig().getInt("schvapchichi.bee.customModelData", 1205);
            meta.setCustomModelData(customModelData);
            meta.setDisplayName("&fШвапчичи");
            potion.setItemMeta(meta);
        }

        return potion;
    }
}
