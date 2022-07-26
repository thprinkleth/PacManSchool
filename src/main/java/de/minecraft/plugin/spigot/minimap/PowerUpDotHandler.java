package de.minecraft.plugin.spigot.minimap;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class PowerUpDotHandler {

    private final PacMan INSTANCE = PacMan.getInstance();

    public void createPowerUpDots() {

        for (int i = 0; i < INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.PowerUps"); i++) {

            Location location = INSTANCE.getLocationFile().getSpawn("Game.Location.PowerUp." + i);

            switch (INSTANCE.getPowerUpHandler().getPowerUpHashMap().get(i)) {
                case "Invincibility":
                    createDotOnMap(location, (byte) 0); // Weiß
                    break;
                case "GhostEating":
                    createDotOnMap(location, (byte) 5); // Limette
                    break;
                case "Speed":
                    createDotOnMap(location, (byte) 3); // Hellblau
                    break;
                case "GhostFreezing":
                    createDotOnMap(location, (byte) 9); // Türkies
                    break;
                case "DoubleCoins":
                    createDotOnMap(location, (byte) 1); // Orange
                    break;
                case "ExtraLife":
                    createDotOnMap(location, (byte) 14); // Rot
                    break;

            }
        }
    }

    public void createDotOnMap(Location location, byte data) {

        location.setY(location.getBlockY() + 11);

        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();

        Bukkit.getScheduler().runTask(INSTANCE, new BukkitRunnable() {
            @Override
            public void run() {
                location.getWorld().getBlockAt(new Location(location.getWorld(), x, y, z)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x, y, z + 1)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x + 1, y, z + 1)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x + 1, y, z)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x + 1, y, z - 1)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x, y, z - 1)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x - 1, y, z - 1)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x - 1, y, z)).setTypeIdAndData(251, data, false);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x - 1, y, z + 1)).setTypeIdAndData(251, data, false);
            }
        });
    }

    public void deleteDotOnMap(Location location) {

        location.setY(location.getBlockY() + 11);

        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();

        Bukkit.getScheduler().runTask(INSTANCE, new BukkitRunnable() {
            @Override
            public void run() {
                location.getWorld().getBlockAt(new Location(location.getWorld(), x, y, z)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x, y, z + 1)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x + 1, y, z + 1)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x + 1, y, z)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x + 1, y, z - 1)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x, y, z - 1)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x - 1, y, z - 1)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x - 1, y, z)).setType(Material.AIR);
                location.getWorld().getBlockAt(new Location(location.getWorld(), x - 1, y, z + 1)).setType(Material.AIR);
            }
        });
    }
}
