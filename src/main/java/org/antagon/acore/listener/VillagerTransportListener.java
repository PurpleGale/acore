package org.antagon.acore.listener;

import org.antagon.acore.Acore;
import org.antagon.acore.api.IConfig;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class VillagerTransportListener implements Listener {
    private final Acore plugin;
    private final IConfig config;
    private final Logger logger;
    private final double villagerDetectionRange;
    private final boolean allowCamelTransport;
    private final boolean allowLlamaTransport;
    private final boolean teleportOnDismount;

    public VillagerTransportListener(Acore plugin, IConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.logger = plugin.getLogger();
        
        this.villagerDetectionRange = config.getInt("villagerTransport.detectionRange", 3);
        this.allowCamelTransport = config.getBoolean("villagerTransport.camel.enabled", true);
        this.allowLlamaTransport = config.getBoolean("villagerTransport.llama.enabled", true);
        this.teleportOnDismount = config.getBoolean("villagerTransport.teleportOnDismount", true);
        
        if (allowLlamaTransport) {
            startLlamaDetectionTask();
        }
        
        if (allowCamelTransport) {
            startCamelDetectionTask();
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!allowCamelTransport) return;
        
        if (event.getEntered() instanceof Player && event.getVehicle() instanceof Camel camel) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!camel.isValid() || camel.getPassengers().size() != 1) return;
                    
                    camel.getNearbyEntities(villagerDetectionRange, villagerDetectionRange, villagerDetectionRange).stream()
                        .filter(entity -> entity instanceof Villager)
                        .map(entity -> (Villager) entity)
                        .findFirst()
                        .ifPresent(villager -> {
                            if (villager.getVehicle() == null) {
                                camel.addPassenger(villager);
                                logger.info("Villager mounted on camel");
                            }
                        });
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager) {
            Entity vehicle = villager.getVehicle();
            
            if (vehicle != null && (vehicle instanceof Camel || vehicle instanceof Llama || 
                                   vehicle instanceof Boat || vehicle instanceof Minecart)) {
                vehicle.removePassenger(villager);
                
                if (teleportOnDismount) {
                    villager.teleport(event.getPlayer().getLocation());
                }
                
                event.setCancelled(true);
                logger.info("Villager dismounted from " + vehicle.getType().name());
            }
        }
    }

    private void startCamelDetectionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().getWorlds().forEach(world -> {
                    world.getEntitiesByClass(Camel.class).forEach(camel -> {
                        if (camel.getPassengers().size() == 1 && 
                            camel.getPassengers().get(0) instanceof Player) {
                            
                            camel.getNearbyEntities(villagerDetectionRange, villagerDetectionRange, villagerDetectionRange).stream()
                                .filter(entity -> entity instanceof Villager)
                                .map(entity -> (Villager) entity)
                                .findFirst()
                                .ifPresent(villager -> {
                                    if (villager.getVehicle() == null) {
                                        camel.addPassenger(villager);
                                        logger.info("Villager mounted on camel (from detection task)");
                                    }
                                });
                        }
                    });
                });
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startLlamaDetectionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().getWorlds().forEach(world -> {
                    world.getEntitiesByClass(Llama.class).forEach(llama -> {
                        if (hasCarpet(llama) && llama.getPassengers().isEmpty()) {
                            llama.getNearbyEntities(villagerDetectionRange, villagerDetectionRange, villagerDetectionRange).stream()
                                .filter(entity -> entity instanceof Villager)
                                .map(entity -> (Villager) entity)
                                .findFirst()
                                .ifPresent(villager -> {
                                    if (villager.getVehicle() == null) {
                                        llama.addPassenger(villager);
                                        logger.info("Villager mounted on llama");
                                    }
                                });
                        }
                    });
                });
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private boolean hasCarpet(Llama llama) {
        ItemStack decor = llama.getInventory().getDecor();
        if (decor == null) return false;
        
        Material material = decor.getType();
        return material.name().endsWith("_CARPET");
    }
}
