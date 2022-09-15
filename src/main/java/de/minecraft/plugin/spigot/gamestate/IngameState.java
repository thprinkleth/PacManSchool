package de.minecraft.plugin.spigot.gamestate;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.powerup.BossBarHandler;
import de.minecraft.plugin.spigot.util.BlockSetter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
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
            map.getRenderers().clear();

            map.addRenderer(new MapRenderer() {
                @Override
                public void render(MapView map, MapCanvas canvas, Player player) {
                    map.setCenterX(INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockX());
                    map.setCenterZ(INSTANCE.getLocationFile().getLocation("Game.Location.Ghost").getBlockZ());
                    map.setScale(MapView.Scale.CLOSEST);
                    map.setUnlimitedTracking(true);
                }
            });

            ItemStack mapItemStack = new ItemStack(Material.EMPTY_MAP, 1, (byte) 5);
            pacMan.getInventory().setItemInOffHand(mapItemStack);
            pacMan.sendMap(map);

            for (Player player : INSTANCE.getPlayerList()) {

                if (!INSTANCE.getRoleHandler().getPlayerRoles().containsKey(player)) {
                    INSTANCE.getRoleHandler().getPlayerRoles().put(player, "Ghost");
                    player.setGlowing(true);
                }

                Bukkit.getScheduler().runTask(INSTANCE, () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 251));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
                });

                INSTANCE.getMySQL().addGame(player);

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

                INSTANCE.setBossBarHandler(new BossBarHandler());
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

                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 251));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));

                if (INSTANCE.getRoleHandler().getPlayerRoles().get(player).equalsIgnoreCase("PacMan")) {
                    player.setHealthScale(INSTANCE.getPowerUpHandler().getMaxLifes());
                    player.setMaxHealth(INSTANCE.getPowerUpHandler().getMaxLifes());
                }
            }
        }
    }

    @Override
    public void stop() {
        for (int i = 0; i < INSTANCE.getGhostDotHandler().getLocations().length; i++) {
            Bukkit.getScheduler().runTask(INSTANCE, new BlockSetter(INSTANCE.getGhostDotHandler().getLocations()[i], Material.AIR));
        }
    }
}
