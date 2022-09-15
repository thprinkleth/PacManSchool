package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.GameState;
import de.minecraft.plugin.spigot.gamestate.IngameState;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        event.setCancelled(true);

        if (!(INSTANCE.getGameStateManager().getCurrent() instanceof IngameState)) {
            return;
        }

        Player damager = ((Player) event.getDamager());
        Player player = ((Player) event.getEntity());

        String roleDamager = INSTANCE.getRoleHandler().getPlayerRoles().get(damager);
        String rolePlayer = INSTANCE.getRoleHandler().getPlayerRoles().get(player);

        if (roleDamager.equalsIgnoreCase("Ghost")) {

            if (rolePlayer.equalsIgnoreCase("Ghost")) {
                return;
            }

            if (INSTANCE.getPowerUpHandler().getPowerUpList()[0]) {
                return;
            }

            INSTANCE.getMySQL().addPacmanEaten(damager);

            if (player.getHealth() <= 2) {

                INSTANCE.getGameStateManager().setCurrent(GameState.POSTGAME_STATE);

                for (Player current : INSTANCE.getPlayerList()) {

                    if (INSTANCE.getRoleHandler().getPlayerRoles().get(current).equalsIgnoreCase("PacMan")) {

                        INSTANCE.getMySQL().addLosesPacMan(current);
                        current.sendTitle(INSTANCE.getMessageFile().getValue("Game.Finish.Lose.PacMan.Title", current), INSTANCE.getMessageFile().getValue("Game.Finish.Lose.PacMan.SubTitle", current));
                        current.playSound(current.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1);

                    } else {

                        INSTANCE.getMySQL().addWinsGhost(current);
                        current.sendTitle(INSTANCE.getMessageFile().getValue("Game.Finish.Win.Ghost.Title", current), INSTANCE.getMessageFile().getValue("Game.Finish.Win.Ghost.SubTitle", current));
                        current.playSound(current.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    }
                }

            } else {

                player.setHealth(player.getHealth() - 2);

                for (Player current : INSTANCE.getPlayerList()) {
                    INSTANCE.getTeleportHandler().resetPlayer(current);
                }
            }
        } else if (roleDamager.equalsIgnoreCase("PacMan")) {

            if (!INSTANCE.getPowerUpHandler().getPowerUpList()[1]) {
                return;
            }

            INSTANCE.getTeleportHandler().resetPlayer(player);
            player.sendTitle(INSTANCE.getMessageFile().getValue("Game.Ghost.Eaten.Title", player), INSTANCE.getMessageFile().getValue("Game.Ghost.Eaten.SubTitle", player));
        }
    }
}
