package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.IngameState;
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

        if (rolePlayer == null) {
            return;
        }

        if (!rolePlayer.equalsIgnoreCase("Ghost")) {
            return;
        }

        try {
            INSTANCE.getGhostDotHandler().handleGhostDot(event);
        } catch (NullPointerException ignored) {
        }
    }
}
