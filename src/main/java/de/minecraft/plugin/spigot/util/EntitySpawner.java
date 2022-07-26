package de.minecraft.plugin.spigot.util;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EntitySpawner extends BukkitRunnable {

    Location location;
    ItemStack itemStack;

    public EntitySpawner(Location location, ItemStack itemStack) {
        this.location = location;
        this.itemStack = itemStack;
    }

    @Override
    public void run() {
        location.getWorld().dropItem(location, itemStack).setVelocity(new Vector(0, 0, 0));
    }
}
