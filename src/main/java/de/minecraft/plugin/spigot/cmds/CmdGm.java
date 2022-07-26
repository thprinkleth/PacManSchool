package de.minecraft.plugin.spigot.cmds;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

public class CmdGm implements CommandExecutor {

    private final PacMan INSTANCE = PacMan.getInstance();

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender cs, org.bukkit.command.Command cmd, String label, String[] args) {


        if (!cmd.getName().equalsIgnoreCase("gm")) {
            return true;
        }

        if (!(cs instanceof Player)) {
            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("Commands.NoPlayer"));
            return true;
        }

        Player player = (Player) cs;

        if (!player.hasPermission("minecraft.cmd.gm")) {
            player.sendMessage("§cDu hast keine Rechte dafür!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Gm.Syntax", player));
            return true;
        }

        int gm = Integer.parseInt(args[0]);

        if (gm < 0 || gm > 3) {
            player.sendMessage("§cSyntax: /gm <0-3>");
            return true;
        }

        switch (gm) {

            case 0:
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Gm.Exec.Survival", player));
                break;
            case 1:
                player.setGameMode(GameMode.CREATIVE);
                player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Gm.Exec.Creative", player));
                break;
            case 2:
                player.setGameMode(GameMode.ADVENTURE);
                player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Gm.Exec.Adventure", player));
                break;
            case 3:
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Gm.Exec.Spectator", player));
                break;
            default:
                player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Gm.Exec.Error", player));
                break;
        }


        return false;
    }
}
