package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JoinListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        player.setMaxHealth(20);
        player.setHealthScale(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setLevel(0);
        player.setExp(0);

        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }

        INSTANCE.getPlayerList().add(player);
        player.setGameMode(GameMode.ADVENTURE);
        event.setJoinMessage(INSTANCE.getMessageFile().getValue("World.Join", player));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));

        try {
            player.teleport(INSTANCE.getLocationFile().getSpawn("Game.Location.Lobby"));
        } catch (NullPointerException ex) {
        }
    }
}
