package de.minecraft.plugin.spigot.minimap;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.util.BlockSetter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class CoinDotHandler {

    private final PacMan INSTANCE = PacMan.getInstance();

    public void createCoinDots() {

        deleteCoinDots();

        for (int i = 0; i < INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.Coins"); i++) {

            Location location = INSTANCE.getLocationFile().getSpawn("Game.Location.Coin." + i);

            Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() + 9, location.getBlockZ()), 251, (byte) 4));
            Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()), 95, (byte) 15));
            Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() -2, location.getBlockZ()), Material.GOLD_BLOCK));
        }
    }

    public void deleteCoinDot(Location location) {
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() + 9, location.getBlockZ()), Material.AIR));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()), 95, (byte) 15));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 2, location.getBlockZ()), 251, (byte) 15));
    }

    public void deleteCoinDots() {
        for (int i = 0; i < INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.Coins"); i++) {
            deleteCoinDot(INSTANCE.getLocationFile().getSpawn("Game.Location.Coin." + i));
        }
    }
}
