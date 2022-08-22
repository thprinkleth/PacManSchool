package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JoinListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();

    private int cooldownTask;

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

        if (INSTANCE.getPlayerList().size() == INSTANCE.getConfigFile().getIntValue("Game.PlayersNeededToStart")) {
            cooldownTask = Bukkit.getScheduler().scheduleAsyncRepeatingTask(INSTANCE, new Runnable() {

                int countdown = INSTANCE.getConfigFile().getIntValue("Game.Countdown.Value");

                @Override
                public void run() {

                    if (countdown == 0) {
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Lobby.Countdown.Starting", player));
                        INSTANCE.getGameStateManager().setCurrent(GameState.INGAME_STATE);
                        Bukkit.getScheduler().cancelTask(cooldownTask);
                        return;
                    }

                    if (countdown % 60 == 0 || countdown == 30 || countdown == 20 || countdown == 15 || countdown == 10 ||
                            countdown == 5 || countdown == 4 || countdown == 3 || countdown == 2 || countdown == 1) {

                        if (Bukkit.getServer().getOnlinePlayers().size() < INSTANCE.getConfigFile().getIntValue("Game.PlayersNeededToStart")) {
                            player.sendMessage(INSTANCE.getMessageFile().getValue("Lobby.Countdown.NotEnoughPlayers", player));
                            Bukkit.getScheduler().cancelTask(cooldownTask);
                            return;
                        }

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendMessage(INSTANCE.getMessageFile().getValue("Lobby.Countdown.Counting", player, countdown));
                        }
                    }
                    countdown--;
                }
            }, 0, 20);
        }
    }
}
