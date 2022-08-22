package de.minecraft.plugin.spigot.gamestate;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.util.BlockSetter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PostGameState extends GameState {

    private final PacMan INSTANCE = PacMan.getInstance();

    @Override
    public void start() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            player.getInventory().clear();

            player.setMaxHealth(20);
            player.setHealthScale(20);
            player.setHealth(20);

            player.teleport(INSTANCE.getLocationFile().getSpawn("Game.Location.Lobby"));

            for (PotionEffect potion : player.getActivePotionEffects()) {
                player.removePotionEffect(potion.getType());
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 200));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 200));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));

            Bukkit.getScheduler().runTaskLater(INSTANCE, new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            }, 120 * 20);
        }
    }

    @Override
    public void stop() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("Server rebooting...");
        }

        INSTANCE.getGameStateManager().setCurrent(GameState.PREGAME_STATE);
    }
}
