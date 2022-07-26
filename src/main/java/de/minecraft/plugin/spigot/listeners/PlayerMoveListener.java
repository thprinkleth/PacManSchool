package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.IngameState;
import de.minecraft.plugin.spigot.util.BlockSetter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();

    @EventHandler
    public void onMovement(PlayerMoveEvent event){

        Player player = event.getPlayer();

        if(!(INSTANCE.getGameStateManager().getCurrent() instanceof IngameState)){
            return;
        }

        String rolePlayer = INSTANCE.getRoleHandler().getPlayerRoles().get(player);

        if(rolePlayer == null || !rolePlayer.equalsIgnoreCase("Ghost")) {
            return;
        }

        Location locFrom = event.getFrom();
        Location locTo = event.getTo();

        if (locFrom.getBlockX() == locTo.getBlockX() && locFrom.getBlockZ() == locTo.getBlockZ()) {
            return;
        }

        Location fromBlockLoc1 = new Location(locFrom.getWorld(), locFrom.getBlockX(), locFrom.getBlockY() + 10, locFrom.getBlockZ());
        Location fromBlockLoc2 = new Location(locFrom.getWorld(), locFrom.getBlockX() + 1, locFrom.getBlockY() + 10, locFrom.getBlockZ());
        Location fromBlockLoc3 = new Location(locFrom.getWorld(), locFrom.getBlockX() + 1, locFrom.getBlockY() + 10, locFrom.getBlockZ() + 1);
        Location fromBlockLoc4 = new Location(locFrom.getWorld(), locFrom.getBlockX(), locFrom.getBlockY() + 10, locFrom.getBlockZ() + 1);

        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(fromBlockLoc1, Material.AIR), 3);
        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(fromBlockLoc2, Material.AIR), 3);
        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(fromBlockLoc3, Material.AIR), 3);
        Bukkit.getScheduler().runTaskLater(INSTANCE, new BlockSetter(fromBlockLoc4, Material.AIR), 3);

        Location toBlockLoc1 = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY() + 10, locTo.getBlockZ());
        Location toBlockLoc2 = new Location(locTo.getWorld(), locTo.getBlockX() + 1, locTo.getBlockY() + 10, locTo.getBlockZ());
        Location toBlockLoc3 = new Location(locTo.getWorld(), locTo.getBlockX() + 1, locTo.getBlockY() + 10, locTo.getBlockZ() + 1);
        Location toBlockLoc4 = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY() + 10, locTo.getBlockZ() + 1);

        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(toBlockLoc1, 251, (byte) 14));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(toBlockLoc2, 251, (byte) 14));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(toBlockLoc3, 251, (byte) 14));
        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(toBlockLoc4, 251, (byte) 14));
    }
}
