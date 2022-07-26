package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();
    private static Location clickedBlockLocation;
    private Location firstLocationAutoCoin, secondLocationAutoCoin;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        try {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

                clickedBlockLocation = event.getClickedBlock().getLocation();

                if (event.getItem().getType() == Material.FEATHER && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(INSTANCE.getConfigFile().getValue("Items.Setup.Name"))) {

                    player.openInventory(INSTANCE.getSetupInventory());

                } else if (event.getItem().getType() == Material.STICK && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(INSTANCE.getConfigFile().getValue("Items.SetupCoin.Name"))) {

                    if (firstLocationAutoCoin == null) {

                        firstLocationAutoCoin = clickedBlockLocation;
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.AutoCoin.FirstLocation", player));

                        return;
                    }

                    secondLocationAutoCoin = event.getClickedBlock().getLocation();

                    player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.AutoCoin.SecondLocation", player));

                    int amountLocationSet = 0;
                    int amountCoinLocations = INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.Coins");

                    int xTop, xBottom, zTop, zBottom;

                    xBottom = Math.min(firstLocationAutoCoin.getBlockX(), secondLocationAutoCoin.getBlockX());
                    xTop = Math.max(firstLocationAutoCoin.getBlockX(), secondLocationAutoCoin.getBlockX());
                    zBottom = Math.min(firstLocationAutoCoin.getBlockZ(), secondLocationAutoCoin.getBlockZ());
                    zTop = Math.max(firstLocationAutoCoin.getBlockZ(), secondLocationAutoCoin.getBlockZ());

                    for (int x = xBottom; x < xTop; x++) {
                        for (int z = zBottom; z < zTop; z++) {

                            Location posFloor = new Location(player.getWorld(), x, firstLocationAutoCoin.getBlockY(), z);

                            if (Bukkit.getWorld(player.getWorld().getName()).getBlockAt(posFloor).getType() != Material.STAINED_GLASS) {
                                continue;
                            }

                            Location posUnderFloor = new Location(player.getWorld(), x, firstLocationAutoCoin.getBlockY() - 1, z);

                            if (Bukkit.getWorld(player.getWorld().getName()).getBlockAt(posUnderFloor).getType() != Material.GOLD_BLOCK) {
                                continue;
                            }

                            INSTANCE.getLocationFile().setLocation("Game.Location.Coin." + amountCoinLocations, posFloor);

                            amountCoinLocations++;
                            amountLocationSet++;
                        }

                        INSTANCE.getConfigFile().setValue("Game.Amount.Locations.Coins", amountCoinLocations);
                    }

                    firstLocationAutoCoin = null;
                    secondLocationAutoCoin = null;

                    player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.AutoCoin.Success", player, amountLocationSet));
                }
            }
        } catch (NullPointerException ex) {
        }
    }

    public static Location getClickedBlockLocation() {
        return clickedBlockLocation;
    }
}
