package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.GameState;
import de.minecraft.plugin.spigot.gamestate.IngameState;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Statistic;
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

    private BossBar bossbar;
    private int schedulerTask;

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {

        Player player = event.getPlayer();

        event.setCancelled(true);

        if (INSTANCE.getSetupPlayerList().contains(player)) {
            event.setCancelled(false);
        }

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

            handleCoin(player, event);

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().invincibilityPowerUpItemStack().getType()) {

            handleInvincibilityPowerUp(event);

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().eatingGhostPowerUpItemStack().getType()) {

            handleEatingGhostPowerUp(event);

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().speedPowerUpItemStack().getType()) {

            handleSpeedPowerUp(player, event);

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().freezeGhostPowerUpItemStack().getType()) {

            handleFreezeGhostPowerUp(player, event);

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().doubleScorePowerUpItemStack().getType()) {

            handleDoubleScorePowerUp(event);

        } else if (item.getItemStack().getType() == INSTANCE.getPickupableItemStacks().extraLifePowerUpItemStack().getType()) {

            handleExtraLifePowerUp(player, event);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        event.getItem().remove();
    }

    private void handleCoin(Player player, PlayerPickupItemEvent event) {

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
        INSTANCE.getCoinDotHandler().deleteCoinDot(event.getItem().getLocation());
    }

    private void handleInvincibilityPowerUp(PlayerPickupItemEvent event) {
        activatePowerUp(0, false);
        INSTANCE.getPowerUpDotHandler().deleteDotOnMap(event.getItem().getLocation());
    }


    private void handleEatingGhostPowerUp(PlayerPickupItemEvent event) {
        activatePowerUp(1, false);
        INSTANCE.getPowerUpDotHandler().deleteDotOnMap(event.getItem().getLocation());
    }

    private void handleSpeedPowerUp(Player player, PlayerPickupItemEvent event) {
        activatePowerUp(2, false);
        INSTANCE.getPowerUpDotHandler().deleteDotOnMap(event.getItem().getLocation());
    }

    public void handleFreezeGhostPowerUp(Player player, PlayerPickupItemEvent event) {

        activatePowerUp(3, false);

        for (Player current : INSTANCE.getPlayerList()) {

            if (INSTANCE.getRoleHandler().getPlayerRoles().get(current).equalsIgnoreCase("Ghost")) {
                current.setWalkSpeed(0.0F);
                current.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, INSTANCE.getPowerUpHandler().getDuration(false), 200));
            }
        }

        Bukkit.getScheduler().runTaskLater(INSTANCE, () -> {

            for (Player current : INSTANCE.getPlayerList()) {
                current.setWalkSpeed(0.2f); // TODO: No speed in game, just walk speed values -> ScoreHandler?
            }
        }, INSTANCE.getPowerUpHandler().getDuration(false));



        INSTANCE.getPowerUpDotHandler().deleteDotOnMap(event.getItem().getLocation());
    }


    private void handleDoubleScorePowerUp(PlayerPickupItemEvent event) {
        activatePowerUp(4, true);
        INSTANCE.getPowerUpDotHandler().deleteDotOnMap(event.getItem().getLocation());
    }


    public void handleExtraLifePowerUp(Player player, PlayerPickupItemEvent event) {

        if (player.getHealth() + 2 <= player.getMaxHealth()) {
            player.setHealth(player.getHealth() + 2);
        }

        INSTANCE.getPowerUpDotHandler().deleteDotOnMap(event.getItem().getLocation());
    }


    private void activatePowerUp(int id, boolean doubleScore) {

        INSTANCE.getPowerUpHandler().activatePowerUp(id);

        Bukkit.getScheduler().runTaskLaterAsynchronously(INSTANCE, () -> {
            INSTANCE.getPowerUpHandler().deactivatePowerUp(id);
        }, INSTANCE.getPowerUpHandler().getDuration(doubleScore));
    }
}
