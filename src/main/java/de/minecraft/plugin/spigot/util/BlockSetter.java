package de.minecraft.plugin.spigot.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockSetter extends BukkitRunnable {

    int type;
    byte data;

    Material material;
    Location location;

    public BlockSetter(Location location, Material material) {
        this.material = material;
        this.location = location;
    }

    public BlockSetter(Location location, int type, byte data) {
        this.location = location;
        this.type = type;
        this.data = data;
    }

    @Override
    public void run() {
        if (material != null) {
            location.getWorld().getBlockAt(location).setType(material);
        } else {
            location.getWorld().getBlockAt(location).setTypeIdAndData(type, data, false);
        }
    }
}
