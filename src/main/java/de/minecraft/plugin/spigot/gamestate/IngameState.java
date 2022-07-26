package de.minecraft.plugin.spigot.gamestate;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.util.BlockSetter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class IngameState extends GameState {

    private final PacMan INSTANCE = PacMan.getInstance();

    private int scheduler;

    @Override
    public void start() {

        for (Entity entity : Bukkit.getWorld("world").getNearbyEntities(INSTANCE.getLocationFile().getLocation("Game.Location.Ghost"), 50, 50, 50)) {

            if (!(entity instanceof Player)) {
                entity.remove();
            }
        }

        INSTANCE.getScoreHandler().spawnCoins();
        INSTANCE.getPowerUpHandler().spawnPowerUps();
        INSTANCE.getCoinDotHandler().createCoinDots();
        INSTANCE.getPowerUpDotHandler().createPowerUpDots();

        if (INSTANCE.getPowerUpHandler().getLevel() == 0) {

            int playerAmount = INSTANCE.getPlayerList().size();

            Random random = new Random();
            int pacManNumberPlayer = random.nextInt(playerAmount);
            Player pacMan = INSTANCE.getPlayerList().get(pacManNumberPlayer);
            INSTANCE.getRoleHandler().getPlayerRoles().put(pacMan, "PacMan");

            pacMan.setHealthScale(INSTANCE.getPowerUpHandler().getMaxLifes());
            pacMan.setMaxHealth(INSTANCE.getPowerUpHandler().getMaxLifes());

            pacMan.setHealth(2);

            MapView map = INSTANCE.getServer().createMap(pacMan.getWorld());
            map.setCenterX(INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockX());
            map.setCenterZ(INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockZ());

            pacMan.getInventory().setItemInOffHand(new ItemStack(Material.EMPTY_MAP));

            scheduler = Bukkit.getScheduler().scheduleAsyncRepeatingTask(INSTANCE, new Runnable() {
                @Override
                public void run() {

                    if (!(INSTANCE.getGameStateManager().getCurrent() instanceof IngameState)) {
                        Bukkit.getScheduler().cancelTask(scheduler);
                    }

                    for (Player player : INSTANCE.getPlayerList()) {

                        if (player == pacMan) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(INSTANCE.getMessageFile().getValue("Game.Score.PacMan", player, INSTANCE.getScoreHandler().getScore())));
                        } else {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(INSTANCE.getMessageFile().getValue("Game.Score.Ghost", player, INSTANCE.getScoreHandler().getScore())));
                        }
                    }
                }
            }, 0, 5);

            for (Player player : INSTANCE.getPlayerList()) {

                if (!INSTANCE.getRoleHandler().getPlayerRoles().containsKey(player)) {
                    INSTANCE.getRoleHandler().getPlayerRoles().put(player, "Ghost");
                }

                Bukkit.getScheduler().runTask(INSTANCE, () -> {

                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 251));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));

                    player.teleport(INSTANCE.getLocationFile().getSpawn("Game.Location." + INSTANCE.getRoleHandler().getPlayerRoles().get(player)));
                });

                INSTANCE.getMySQL().addGame(player);
            }
        } else {

            for (Player player : INSTANCE.getPlayerList()) {

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                String title = INSTANCE.getMessageFile().getValue("Game.NextLevel." + INSTANCE.getRoleHandler().getPlayerRoles().get(player) + ".Title", player, INSTANCE.getPowerUpHandler().getLevel() + 1);
                String subTitle = INSTANCE.getMessageFile().getValue("Game.NextLevel." + INSTANCE.getRoleHandler().getPlayerRoles().get(player) + ".SubTitle", player, INSTANCE.getPowerUpHandler().getLevel() + 1);
                player.sendTitle(title, subTitle);

                for (PotionEffect potion : player.getActivePotionEffects()) {
                    player.removePotionEffect(potion.getType());
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 200));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 200));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 251));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, INSTANCE.getPowerUpHandler().getLevel()));

                if (INSTANCE.getRoleHandler().getPlayerRoles().get(player).equalsIgnoreCase("PacMan")) {
                    player.setHealthScale(INSTANCE.getPowerUpHandler().getMaxLifes());
                    player.setMaxHealth(INSTANCE.getPowerUpHandler().getMaxLifes());
                }

                player.teleport(INSTANCE.getLocationFile().getSpawn("Game.Location." + INSTANCE.getRoleHandler().getPlayerRoles().get(player)));
            }
        }
    }

    @Override
    public void stop() {

        int oldY = INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockY() + 9;

        for (int x = INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockX() - 50; x < INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockX() + 50; x++) {
            for (int y = oldY; y < oldY + 2; y++) {
                for (int z = INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockZ() - 50; z < INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockZ() + 50; z++) {

                    Location loc = new Location(INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getWorld(), x, y, z);

                    if (loc.getBlock().getType() == Material.CONCRETE && loc.getBlock().getMetadata("red").size() > 0) {
                        Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(loc, Material.AIR));
                    }
                }
            }
        }
    }
}
