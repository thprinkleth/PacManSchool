package de.minecraft.plugin.spigot.minimap;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.util.BlockSetter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerMoveEvent;

public class GhostDotHandler {

    private final PacMan INSTANCE = PacMan.getInstance();
    private Location[] locations = new Location[4];

    public void handleGhostDot(PlayerMoveEvent event) throws NullPointerException {

        Location locFrom = event.getFrom();
        Location locTo = event.getTo();

        if (locFrom.getBlockX() == locTo.getBlockX() && locFrom.getBlockZ() == locTo.getBlockZ()) {
            return;
        }

        locations[0] = new Location(locFrom.getWorld(), locFrom.getBlockX(), locFrom.getBlockY() + 10, locFrom.getBlockZ());
        locations[1] = new Location(locFrom.getWorld(), locFrom.getBlockX() + 1, locFrom.getBlockY() + 10, locFrom.getBlockZ());
        locations[2] = new Location(locFrom.getWorld(), locFrom.getBlockX() + 1, locFrom.getBlockY() + 10, locFrom.getBlockZ() + 1);
        locations[3] = new Location(locFrom.getWorld(), locFrom.getBlockX(), locFrom.getBlockY() + 10, locFrom.getBlockZ() + 1);

        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(locations[0], Material.AIR), 3);
        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(locations[1], Material.AIR), 3);
        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(locations[2], Material.AIR), 3);
        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(locations[3], Material.AIR), 3);

        locations[0] = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY() + 10, locTo.getBlockZ());
        locations[1] = new Location(locTo.getWorld(), locTo.getBlockX() + 1, locTo.getBlockY() + 10, locTo.getBlockZ());
        locations[2] = new Location(locTo.getWorld(), locTo.getBlockX() + 1, locTo.getBlockY() + 10, locTo.getBlockZ() + 1);
        locations[3] = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY() + 10, locTo.getBlockZ() + 1);

        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(locations[0], 251, (byte) 14));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(locations[1], 251, (byte) 14));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(locations[2], 251, (byte) 14));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(locations[3], 251, (byte) 14));
    }

    public Location[] getLocations() {
        return locations;
    }
}
