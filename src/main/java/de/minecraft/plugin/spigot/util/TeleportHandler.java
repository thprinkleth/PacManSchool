package de.minecraft.plugin.spigot.util;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class TeleportHandler {

    private final PacMan INSTANCE = PacMan.getInstance();
    private HashMap<String, Location> teleportLocations = new HashMap<>();

    public TeleportHandler() {
        teleportLocations.put("Lobby", INSTANCE.getLocationFile().getSpawn("Game.Location.Lobby"));
        teleportLocations.put("Ghost", INSTANCE.getLocationFile().getSpawn("Game.Location.Ghost"));
        teleportLocations.put("PacMan", INSTANCE.getLocationFile().getSpawn("Game.Location.PacMan"));
    }

    public void gameInitTeleports() {
        for (Player player : INSTANCE.getPlayerList()) {
            resetPlayer(player);
        }
    }

    public void resetPlayer(Player player) {

        if (!INSTANCE.getRoleHandler().getPlayerRoles().containsKey(player)) {
            player.teleport(teleportLocations.get("Lobby"));
            return;
        }

        switch (INSTANCE.getRoleHandler().getPlayerRoles().get(player)) {

            case "PacMan":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1 * 20, 200));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1 * 20, 200));
                player.teleport(teleportLocations.get("PacMan"));
                break;

            case "Ghost":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2 * 20, 200));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 200));
                player.teleport(teleportLocations.get("Ghost"));
                break;
        }
    }
}
