package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.GameState;
import de.minecraft.plugin.spigot.gamestate.PreGameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event){

        GameState current = PacMan.getInstance().getGameStateManager().getCurrent();

        if(!(current instanceof PreGameState)) {
            event.disallow(null, "Game is already running!");
        }
    }
}
