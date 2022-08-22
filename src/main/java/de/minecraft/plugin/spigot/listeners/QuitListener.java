package de.minecraft.plugin.spigot.listeners;


import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.GameState;
import de.minecraft.plugin.spigot.gamestate.IngameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();

    @EventHandler
    public void onQuit(PlayerQuitEvent event){

        Player player = event.getPlayer();

        event.setQuitMessage(INSTANCE.getMessageFile().getValue("World.Quit", player, INSTANCE.getPlayerList().size() - 1));

        INSTANCE.removeFromPlayerList(player);

        if (INSTANCE.getSetupPlayerList().contains(player)) {
            INSTANCE.removeFromSetupPlayerList(player);
        }

        if (INSTANCE.getGameStateManager().getCurrent() instanceof IngameState) {

            INSTANCE.getMySQL().addLosesGhost(player);
            INSTANCE.getMySQL().addLosesPacMan(player);

            for (Player current : INSTANCE.getPlayerList()) {
                INSTANCE.getMySQL().addWinsGhost(current);
                INSTANCE.getMySQL().addWinsPacMan(current);
            }

            INSTANCE.getGameStateManager().setCurrent(GameState.POSTGAME_STATE);
        }
    }
}
