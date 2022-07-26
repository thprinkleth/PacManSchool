package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.GameState;
import de.minecraft.plugin.spigot.gamestate.IngameState;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemPickUpListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();

    private int repeatingTask;

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {

        Player player = event.getPlayer();

        event.setCancelled(true);

        if (INSTANCE.getRoleHandler().getPlayerRoles().isEmpty()) {
            return;
        }

        if (INSTANCE.getRoleHandler().getPlayerRoles().get(player).equalsIgnoreCase("Ghost")) {
            return;
        }

        if (!(INSTANCE.getGameStateManager().getCurrent() instanceof IngameState)) {
            return;
        }

        Item item = event.getItem();

        if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().coinItemStack().getType()) {

            INSTANCE.getScoreHandler().addScore(INSTANCE.getPowerUpHandler().getPowerUpList()[4]);

            if (INSTANCE.getScoreHandler().getScore() % INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.Coins") == 0) {

                if (INSTANCE.getPowerUpHandler().getLevel() == 3) {

                    INSTANCE.getGameStateManager().setCurrent(GameState.POSTGAME_STATE);

                    for (Player current : INSTANCE.getPlayerList()) {

                        if (INSTANCE.getRoleHandler().getPlayerRoles().get(current).equalsIgnoreCase("PacMan")) {

                            INSTANCE.getMySQL().addWinsPacMan(current);
                            current.sendTitle(INSTANCE.getMessageFile().getValue("Game.Finish.Win.PacMan.Title", current), INSTANCE.getMessageFile().getValue("Game.Finish.Win.PacMan.SubTitle", current));
                            current.playSound(current.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                        } else {

                            INSTANCE.getMySQL().addLosesGhost(current);
                            current.sendTitle(INSTANCE.getMessageFile().getValue("Game.Finish.Lose.Ghost.Title", current), INSTANCE.getMessageFile().getValue("Game.Finish.Lose.Ghost.SubTitle", current));
                            current.playSound(current.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1);
                        }
                    }
                    return;
                }

                INSTANCE.getPowerUpHandler().addLevel();
                INSTANCE.getGameStateManager().setCurrent(GameState.INGAME_STATE);
            }

            for (Player current : INSTANCE.getPlayerList()) {

                float progress = ((float) INSTANCE.getScoreHandler().getScore() % (float) INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.Coins")) / (float) INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.Coins");

                player.setLevel(INSTANCE.getPowerUpHandler().getLevel());
                current.setExp(progress);
            }
            INSTANCE.getCoinDotHandler().deleteCoinDot(item.getLocation());

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().invincibilityPowerUpItemStack().getType()) {

            activatePowerUp(0, false);
            INSTANCE.getPowerUpDotHandler().deleteDotOnMap(item.getLocation());

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().eatingGhostPowerUpItemStack().getType()) {

            activatePowerUp(1, false);
            INSTANCE.getPowerUpDotHandler().deleteDotOnMap(item.getLocation());

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().speedPowerUpItemStack().getType()) {

            activatePowerUp(2, false);

            int amplifier = 2;

            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                amplifier += player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, INSTANCE.getPowerUpHandler().getDuration(false), amplifier));
            INSTANCE.getPowerUpDotHandler().deleteDotOnMap(item.getLocation());

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().freezeGhostPowerUpItemStack().getType()) {

            activatePowerUp(3, false);

            for (Player current : INSTANCE.getPlayerList()) {

                if (INSTANCE.getRoleHandler().getPlayerRoles().get(current).equalsIgnoreCase("Ghost")) {
                    current.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, INSTANCE.getPowerUpHandler().getDuration(false), 200));
                    current.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, INSTANCE.getPowerUpHandler().getDuration(false), 200));
                }
            }

            INSTANCE.getPowerUpDotHandler().deleteDotOnMap(item.getLocation());

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().doubleScorePowerUpItemStack().getType()) {

            activatePowerUp(4, true);
            INSTANCE.getPowerUpDotHandler().deleteDotOnMap(item.getLocation());

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().extraLifePowerUpItemStack().getType()) {

            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(player.getHealth() + 2);
            }
            INSTANCE.getPowerUpDotHandler().deleteDotOnMap(item.getLocation());
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        event.getItem().remove();
    }

    private void activatePowerUp(int id, boolean doubleScore) {

        INSTANCE.getPowerUpHandler().activatePowerUp(id);

        Bukkit.getScheduler().runTaskLaterAsynchronously(INSTANCE, () -> {
            INSTANCE.getPowerUpHandler().deactivatePowerUp(id);
        }, INSTANCE.getPowerUpHandler().getDuration(doubleScore));
    }
}
