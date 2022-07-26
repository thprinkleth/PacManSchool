package de.minecraft.plugin.spigot.cmds;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.gamestate.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdStart implements CommandExecutor {

    private final PacMan INSTANCE = PacMan.getInstance();
    private int cooldownScheduler = 0;

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String s, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("start")) {
            return true;
        }

        if (!(cs instanceof Player)) {
            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("Commands.NoPlayer"));
            return true;
        }

        Player player = ((Player) cs);

        if (!player.hasPermission("commands.start")) {
            player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.NoPerm", player));
            return true;
        }

        if (args.length != 0) {
            player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Start.Syntax", player));
            return true;
        }

        if (!INSTANCE.getGameStateManager().canGameStart()) {
            player.sendMessage(INSTANCE.getMessageFile().getValue("Lobby.Countdown.NotEnoughLocations", player));
            return true;
        }

        cooldownScheduler = Bukkit.getScheduler().scheduleAsyncRepeatingTask(INSTANCE, new Runnable() {

            int countdown = INSTANCE.getConfigFile().getIntValue("Game.Countdown.Value");

            @Override
            public void run() {

                if (countdown == 0) {
                    player.sendMessage(INSTANCE.getMessageFile().getValue("Lobby.Countdown.Starting", player));
                    INSTANCE.getGameStateManager().setCurrent(GameState.INGAME_STATE);
                    Bukkit.getScheduler().cancelTask(cooldownScheduler);
                    return;
                }

                if (countdown % 60 == 0 || countdown == 30 || countdown == 20 || countdown == 15 || countdown == 10 ||
                        countdown == 5 || countdown == 4 || countdown == 3 || countdown == 2 || countdown == 1) {

                    if (Bukkit.getServer().getOnlinePlayers().size() < INSTANCE.getConfigFile().getIntValue("Game.PlayersNeededToStart")) {

                        player.sendMessage(INSTANCE.getMessageFile().getValue("Lobby.Countdown.NotEnoughPlayers", player));
                        Bukkit.getScheduler().cancelTask(cooldownScheduler);
                        return;
                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(INSTANCE.getMessageFile().getValue("Lobby.Countdown.Counting", player, countdown));
                    }
                }
                countdown--;
            }
        }, 0, 20);

        return false;
    }
}
