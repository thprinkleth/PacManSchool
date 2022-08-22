package de.minecraft.plugin.spigot.cmds;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CmdSetup implements CommandExecutor {

    private final PacMan INSTANCE = PacMan.getInstance();

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String s, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("setup")) {
            return true;
        }

        if (!(cs instanceof Player)) {
            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("Commands.NoPlayer"));
            return true;
        }

        Player player = (Player) cs;

        if (!player.hasPermission("commands.setup")) {
            player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.NoPerm", player));
            return true;
        }

        if (args.length != 0) {
            player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Setup.Syntax", player));
            return true;
        }

        if (INSTANCE.getSetupPlayerList().contains(player)) {
            player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Setup.Exec.Deactivated", player));
            return true;
        }

        String setupItemName = INSTANCE.getConfigFile().getValue("Items.Setup.Name", player);
        String setupItemLore = INSTANCE.getConfigFile().getValue("Items.Setup.Lore", player);

        ItemStack setupItem = new ItemBuilder(Material.FEATHER).setName(setupItemName).addLoreLine(setupItemLore).toItemStack();

        ItemStack autoCoinSetup = new ItemBuilder(Material.STICK)
                .setName(INSTANCE.getConfigFile().getValue("Items.SetupCoin.Name"))
                .addLoreLine(INSTANCE.getConfigFile().getValue("Items.SetupCoin.Lore")).toItemStack();

        player.getInventory().setItem(0, setupItem);
        player.getInventory().setItem(1, autoCoinSetup);
        INSTANCE.addToSetupPlayerList(player);
        player.performCommand("gm 1");

        player.sendMessage(INSTANCE.getMessageFile().getValue("Commands.Setup.Exec.Activated", player));

        return false;
    }
}
