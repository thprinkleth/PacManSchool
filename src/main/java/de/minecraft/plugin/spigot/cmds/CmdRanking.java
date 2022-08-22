package de.minecraft.plugin.spigot.cmds;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;

public class CmdRanking implements CommandExecutor {

    private final PacMan INSTANCE = PacMan.getInstance();

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("ranking")) {
            return true;
        }

        if (!(cs instanceof Player)) {
            Bukkit.getConsoleSender().sendMessage(PacMan.getInstance().getMessageFile().getValue("Commands.NoPlayer"));
            return true;
        }

        Player player = (Player) cs;

        if (args.length != 0) {
            player.sendMessage(PacMan.getInstance().getMessageFile().getValue("Commands.Ranking.Syntax", player));
            return true;
        }

        Inventory rankingInventory = Bukkit.getServer().createInventory(null, 9 * 3, INSTANCE.getConfigFile().getValue("Inventory.RankingInventory.Name", player)); // TODO:

        int rank = 1;

        for (OfflinePlayer top : INSTANCE.getMySQL().getBestPlayers()) {

            int playedGames = INSTANCE.getMySQL().getGames(top.getUniqueId().toString());
            int winsPacMan = INSTANCE.getMySQL().getWinsPacMan(top.getUniqueId().toString());
            int losesPacMan = INSTANCE.getMySQL().getLosesPacMan(top.getUniqueId().toString());
            int winsGhost = INSTANCE.getMySQL().getWinsGhost(top.getUniqueId().toString());
            int losesGhost = INSTANCE.getMySQL().getLosesGhost(top.getUniqueId().toString());

            float winrate = ((float) winsPacMan + (float) winsGhost) / (float) playedGames;
            winrate *= 100;

            DecimalFormat df = new DecimalFormat("#.##");
            String winrateString = df.format(winrate);

            String[] lore = {
                    "§7Gespielte Spiele: §6" + playedGames,
                    "§7Wins als PacMan: §6" + winsPacMan,
                    "§7Loses als PacMan: §6" + losesPacMan,
                    "§7Wins als Geist: §6" + winsGhost,
                    "§7Loses als Geist: §6" + losesGhost,
                    "§7Gewinn-wahrscheinlichkeit: §6" + winrateString + "%"
            };

            ItemStack skull = new ItemBuilder(Material.SKULL_ITEM).setLore(lore).setName("§b" + top.getName()).setDurability((short) 3).toItemStack(); // TODO:
            SkullMeta skullMeta = ((SkullMeta) skull.getItemMeta());

            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(args[0]));
            skull.setItemMeta(skullMeta);
            rankingInventory.setItem(8 + rank, skull);

            rank += 2;
        }

        player.openInventory(rankingInventory);

        return false;
    }
}
