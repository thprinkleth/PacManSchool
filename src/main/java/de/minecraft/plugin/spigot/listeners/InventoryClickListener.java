package de.minecraft.plugin.spigot.listeners;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final PacMan INSTANCE = PacMan.getInstance();

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        Player player = ((Player) event.getWhoClicked());

        event.setCancelled(true);

        try {

            if (event.getClickedInventory().getName().equalsIgnoreCase(INSTANCE.getSetupInventory().getName())) {

                int slot = event.getRawSlot();

                int amountCoinLocations = INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.Coins");
                int amountPowerupLocations = INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.PowerUps");

                final int lobbyLocationSlot = 2;
                final int ghostLocationSlot = 4;
                final int pacmanLocationSlot = 6;
                final int powerupLocationSlot = 8 + 4;
                final int coinLocationSlot = 8 + 6;

                Location location = InteractListener.getClickedBlockLocation();

                switch (slot) {

                    case lobbyLocationSlot:

                        INSTANCE.getLocationFile().setLocation("Game.Location.Lobby", location);
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.Lobby.Success", player));

                        player.closeInventory();

                        break;

                    case ghostLocationSlot:

                        INSTANCE.getLocationFile().setLocation("Game.Location.Ghost", location);
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.Ghosts.Success", player));

                        player.closeInventory();

                        break;

                    case pacmanLocationSlot:

                        INSTANCE.getLocationFile().setLocation("Game.Location.PacMan", location);
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.PacMan.Success", player));

                        player.closeInventory();

                        break;

                    case coinLocationSlot:

                        for (int i = 0; i < amountCoinLocations; i++) {

                            if (location.getBlockX() == INSTANCE.getLocationFile().getLocation("Game.Location.Coin." + i).getBlockX()
                                    && location.getBlockY() == INSTANCE.getLocationFile().getLocation("Game.Location.Coin." + i).getBlockY()
                                    && location.getBlockZ() == INSTANCE.getLocationFile().getLocation("Game.Location.Coin." + i).getBlockZ()) {

                                player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.Coin.NotSuccess", player));

                                return;
                            }
                        }

                        INSTANCE.getLocationFile().setLocation("Game.Location.Coin." + amountCoinLocations, location);
                        INSTANCE.getConfigFile().setValue("Game.Amount.Locations.Coins", amountCoinLocations + 1);
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.Coin.Success", player, amountCoinLocations + 1));

                        player.closeInventory();

                        break;

                    case powerupLocationSlot:

                        for (int i = 0; i < amountPowerupLocations; i++) {

                            if (location.getBlockX() == INSTANCE.getLocationFile().getLocation("Game.Location.PowerUp." + i).getBlockX()
                                    && location.getBlockY() == INSTANCE.getLocationFile().getLocation("Game.Location.PowerUp." + i).getBlockY()
                                    && location.getBlockZ() == INSTANCE.getLocationFile().getLocation("Game.Location.PowerUp." + i).getBlockZ()) {

                                player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.PowerUp.NotSuccess", player));

                                return;
                            }
                        }

                        INSTANCE.getLocationFile().setLocation("Game.Location.PowerUp." + amountPowerupLocations, location);
                        INSTANCE.getConfigFile().setValue("Game.Amount.Locations.PowerUps", amountPowerupLocations + 1);
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Setup.Spawn.Set.PowerUp.Success", player, amountPowerupLocations + 1));

                        player.closeInventory();

                        break;
                }
            }
        } catch (NullPointerException ex) {
        }
    }
}