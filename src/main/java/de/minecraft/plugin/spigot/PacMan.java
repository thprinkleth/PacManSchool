package de.minecraft.plugin.spigot;

import de.minecraft.plugin.spigot.cmds.CmdGm;
import de.minecraft.plugin.spigot.cmds.CmdSetup;
import de.minecraft.plugin.spigot.cmds.CmdStart;
import de.minecraft.plugin.spigot.cmds.CmdStats;
import de.minecraft.plugin.spigot.gamestate.GameState;
import de.minecraft.plugin.spigot.gamestate.GameStateManager;
import de.minecraft.plugin.spigot.listeners.*;
import de.minecraft.plugin.spigot.minimap.CoinDotHandler;
import de.minecraft.plugin.spigot.minimap.PowerUpDotHandler;
import de.minecraft.plugin.spigot.powerup.PickupableItemStacks;
import de.minecraft.plugin.spigot.powerup.PowerUpHandler;
import de.minecraft.plugin.spigot.role.RoleHandler;
import de.minecraft.plugin.spigot.score.ScoreHandler;
import de.minecraft.plugin.spigot.util.FileManager;
import de.minecraft.plugin.spigot.util.ItemBuilder;
import de.minecraft.plugin.spigot.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;

public class PacMan extends JavaPlugin {

    /**
     * TODO:
     * - Ranking command
     * - Game breakup on disconnect
     * - PlayerMoveListener migrated into GhostDotHandler
     * - Rework replacement params
     * - Fix ghost dot handler
     * - Rework game state design (when to start, when to end, etc.)
     * - PowerUp timer
     * - Smoother transitions between GameStates/overall teleports
     * - Feedback when PacMan got hit by a ghost and vice versa
     * - Automatic game start
     */

    private static PacMan instance;

    private FileManager messageFile;
    private FileManager locationFile;
    private FileManager configFile;
    private FileManager mySqlFile;
    private MySQL mySQL;

    private RoleHandler roleHandler;
    private GameStateManager gameStateManager;
    private PickupableItemStacks pickupableItemStacks;
    private PowerUpHandler powerUpHandler;
    private ScoreHandler scoreHandler;
    private CoinDotHandler coinDotHandler;
    private PowerUpDotHandler powerUpDotHandler;

    private ArrayList <Player> playerList;
    private ArrayList <Player> setupPlayerList;

    private Inventory setupInventory;


    @Override
    public void onEnable() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("");
        }

        instance = this;

        messageFile = new FileManager("messages.yml");
        locationFile = new FileManager("locations.yml");
        configFile = new FileManager("config.yml");
        mySqlFile = new FileManager("MySQL.yml");
        mySQL = new MySQL();

        defaultFileSetup();


        roleHandler = new RoleHandler();
        playerList = new ArrayList<>();
        setupPlayerList = new ArrayList<>();

        gameStateManager = new GameStateManager();
        pickupableItemStacks = new PickupableItemStacks();
        scoreHandler = new ScoreHandler();
        powerUpHandler = new PowerUpHandler();
        coinDotHandler = new CoinDotHandler();
        powerUpDotHandler = new PowerUpDotHandler();

        gameStateManager.setCurrent(GameState.PREGAME_STATE);

        initInventories();

        registerCommands();
        registerListeners();

        Bukkit.getConsoleSender().sendMessage(messageFile.getValue("Server.StartUp.Message"));
    }

    @Override
    public void onDisable() {

        mySQL.disconnect();
        Bukkit.getConsoleSender().sendMessage(messageFile.getValue("Server.ShutDown.Message"));
    }

    private void registerCommands() {
        getCommand("setup").setExecutor(new CmdSetup());
        getCommand("start").setExecutor(new CmdStart());
        getCommand("stats").setExecutor(new CmdStats());
        getCommand("gm").setExecutor(new CmdGm());
    }

    private void registerListeners() {

        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new JoinListener(), this);
        pluginManager.registerEvents(new CancelledListeners(), this);
        pluginManager.registerEvents(new DamageListener(), this);
        pluginManager.registerEvents(new InteractListener(), this);
        pluginManager.registerEvents(new InventoryClickListener(), this);
        pluginManager.registerEvents(new PlayerLoginListener(), this);
        pluginManager.registerEvents(new QuitListener(), this);
        pluginManager.registerEvents(new PlayerMoveListener(), this);
        pluginManager.registerEvents(new ItemPickUpListener(), this);
    }

    private void defaultFileSetup() {

        messageFile.getFileConfig().options().copyDefaults(true);
        messageFile.getFileConfig().options().header("All strings contained in the plugin.");

        mySqlFile.getFileConfig().addDefault("Login.Username", "sql8505251");
        mySqlFile.getFileConfig().addDefault("Login.Port", 3306);
        mySqlFile.getFileConfig().addDefault("Login.Database", "sql8505251");
        mySqlFile.getFileConfig().addDefault("Login.Host", "sql8.freemysqlhosting.net");
        mySqlFile.getFileConfig().addDefault("Login.Password", "iKETWBjSd6");

        messageFile.getFileConfig().addDefault("Server.Prefix", "&bPacMan &7|");

        messageFile.getFileConfig().addDefault("MySQL.Connect.Wait", "{Prefix} &6Connecting with MySQL-Database...");
        messageFile.getFileConfig().addDefault("MySQL.Connect.Success", "{Prefix} &aConnection with MySQL-Database established.");
        messageFile.getFileConfig().addDefault("MySQL.Disconnect.Success", "{Prefix} &cConnection with MySQL-Database aborted.");

        messageFile.getFileConfig().addDefault("Server.StartUp.Message", "{Prefix} &aPlugin started.");
        messageFile.getFileConfig().addDefault("Server.ShutDown.Message", "{Prefix} &cPlugin stopped.");

        messageFile.getFileConfig().addDefault("Commands.NoPlayer", "{Prefix} &cThe command can only be performed by a player.");

        messageFile.getFileConfig().addDefault("Commands.Setup.Syntax", "{Prefix} &cSyntax: &6/setup");
        messageFile.getFileConfig().addDefault("Commands.Start.Syntax", "{Prefix} &cSyntax: &6/start");
        messageFile.getFileConfig().addDefault("Commands.Ranking.Syntax", "{Prefix} &cSyntax: &6/ranking");
        messageFile.getFileConfig().addDefault("Commands.Stats.Syntax", "{Prefix} &cSyntax: &6/stats <playername>");
        messageFile.getFileConfig().addDefault("Commands.Gm.Syntax", "{Prefix} &cSyntax: &6/gm <0-3>");

        messageFile.getFileConfig().addDefault("Commands.Gm.Exec.Survival", "{Prefix} &aYou switched to Survival-Mode&a.");
        messageFile.getFileConfig().addDefault("Commands.Gm.Exec.Creative", "{Prefix} &aYou switched to Creative-Mode&a.");
        messageFile.getFileConfig().addDefault("Commands.Gm.Exec.Adventure", "{Prefix} &aYou switched to Adventure-Mode&a.");
        messageFile.getFileConfig().addDefault("Commands.Gm.Exec.Spectator", "{Prefix} &aYou switched to Spectator-Mode&a.");
        messageFile.getFileConfig().addDefault("Commands.Gm.Exec.Error", "{Prefix} &cAn error occurred while trying to change the gamemode.");

        messageFile.getFileConfig().addDefault("Commands.Setup.Exec.Activated", "{Prefix} &aYou got the item for the setup.");
        messageFile.getFileConfig().addDefault("Commands.Setup.Exec.Deactivated", "{Prefix} &cYou lost the item for the setup.");

        messageFile.getFileConfig().addDefault("Commands.Stats.PlayerNotFound", "{Prefix} &aThe player &6{String} &awas not found.");

        messageFile.getFileConfig().addDefault("Commands.NoPerm", "{Prefix} &cYou don't have the permission to perform this command.");

        messageFile.getFileConfig().addDefault("World.Join", "{Prefix} &6{PlayerName} &ajoined the server. &7({ServerPlayers}/&8{MaxPlayers}&7)");
        messageFile.getFileConfig().addDefault("World.Quit", "{Prefix} &6{PlayerName} &cleft the server. &7({Number}/&8{MaxPlayers}&7)");

        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.Lobby.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6lobby&a.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.Lobby.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for the &6lobby&c.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.Ghosts.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6ghosts&a.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.Ghosts.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for the &6ghost&c.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.PacMan.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6PacMan&a.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.PacMan.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for &6PacMan&c.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.Coin.Success", "{Prefix} &aA position for a coin has been set. &7({Number])");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.Coin.NotSuccess", "{Prefix} &cAn error occurred while trying to set a position for a coin.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.AutoCoin.Success", "{Prefix} &6{Number} &apositions have been set for a coin position.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.PowerUp.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6PowerUp&a.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.PowerUp.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for the &6PowerUp&c.");

        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.AutoCoin.FirstLocation", "{Prefix} &aSuccessfully set the &6first location &afor &6AutoCoin&a.");
        messageFile.getFileConfig().addDefault("Setup.Spawn.Set.AutoCoin.SecondLocation", "{Prefix} &aSuccessfully set the &6second location &afor &6AutoCoin&a. &8Calculating...");

        messageFile.getFileConfig().addDefault("Lobby.Countdown.Counting", "{Prefix} &aThe game will start in &6{Number}&a seconds.");
        messageFile.getFileConfig().addDefault("Lobby.Countdown.Starting", "{Prefix} &aThe game starts now.");
        messageFile.getFileConfig().addDefault("Lobby.Countdown.NotEnoughLocations", "{Prefix} &cDu musst alle Positionen setzten bevor das Spiel gestartet werden kann.");
        messageFile.getFileConfig().addDefault("Lobby.Countdown.NotEnoughPlayers", "{Prefix} &cEs müssen &6{PlayersNeededToStart} Spieler &cin der Lobby sein, um das Spiel zu starten.");

        messageFile.getFileConfig().addDefault("Game.NextLevel.PacMan.Title", "&aDu hast das nächste Level erreicht.");
        messageFile.getFileConfig().addDefault("Game.NextLevel.PacMan.SubTitle", "&7Neues Level: &6{Number}");
        messageFile.getFileConfig().addDefault("Game.NextLevel.Ghost.Title", "&cPacMan hat das nächste Level erreicht.");
        messageFile.getFileConfig().addDefault("Game.NextLevel.Ghost.SubTitle", "&7Neues Level: &6{Number}");

        messageFile.getFileConfig().addDefault("Game.Finish.Win.PacMan.Title", "&aDu hast als &6PacMan &agewonnen!");
        messageFile.getFileConfig().addDefault("Game.Finish.Win.PacMan.SubTitle", "&7Herzlichen Glückwunsch!!");
        messageFile.getFileConfig().addDefault("Game.Finish.Lose.PacMan.Title", "&cDu hast als &6PacMan &cverloren!");
        messageFile.getFileConfig().addDefault("Game.Finish.Lose.PacMan.SubTitle", "&7Viel Glück nächstes Mal.");
        messageFile.getFileConfig().addDefault("Game.Finish.Win.Ghost.Title", "&aDu hast als &6Geist &agewonnen!");
        messageFile.getFileConfig().addDefault("Game.Finish.Win.Ghost.SubTitle", "&7Herzlichen Glückwunsch!!");
        messageFile.getFileConfig().addDefault("Game.Finish.Lose.Ghost.Title", "&cDu hast als &6Geist &cverloren!");
        messageFile.getFileConfig().addDefault("Game.Finish.Lose.Ghost.SubTitle", "&7Viel Glück nächstes Mal.");

        messageFile.getFileConfig().addDefault("Game.Score.PacMan", "&aDein Score: &6{Number}");
        messageFile.getFileConfig().addDefault("Game.Score.Ghost", "&bScore von PacMan: &6{Number}");

        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Name", "&aSetup-Inventar");

        configFile.getFileConfig().addDefault("Game.Amount.Locations.Coins", 0);
        configFile.getFileConfig().addDefault("Game.Amount.Locations.PowerUps", 0);
        configFile.getFileConfig().addDefault("Game.PlayersNeededToStart", 1);
        configFile.getFileConfig().addDefault("Game.Countdown.Value", 5);

        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.LobbySpawn.Name", "&bLobby-Spawnpunkt");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.LobbySpawn.Lore", "&7Setze die Position, wo die Spieler vor und nach dem Spiel erscheinen sollen.");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.GhostSpawn.Name", "&bGhost-Spawnpunkt");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.GhostSpawn.Lore", "&7Setze die Position, wo die Geister in dem Spiel erscheinen sollen.");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.PacManSpawn.Name", "&bPacMan-Spawnpunkt");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.PacManSpawn.Lore", "&7Setze die Position, wo PacMan in dem Spiel erscheinen soll.");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.PowerUpSpawn.Name", "&bPowerUp-Spawnpunkt");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.PowerUpSpawn.Lore", "&7Setze die Position, wo ein PowerUp liegen soll.");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.CoinSpawn.Name", "&bCoin-Spawnpunkt");
        configFile.getFileConfig().addDefault("Inventory.SetupInventory.Items.CoinSpawn.Lore", "&7Setze die Position, wo ein Coin liegen soll.");

        configFile.getFileConfig().addDefault("Items.Setup.Name", "&bSetupItem");
        configFile.getFileConfig().addDefault("Items.Setup.Lore", "&7Rechtsklick auf einen Block um das Location-Inventar zu öffnen.");
        configFile.getFileConfig().addDefault("Items.SetupCoin.Name", "&bSetupCoins");
        configFile.getFileConfig().addDefault("Items.SetupCoin.Lore", "&7Rechtsklick auf einen Block um einen Coin zu setzen.");

        configFile.getFileConfig().addDefault("Inventory.StatsInventory.Self.Name", "&7Stats von &8dir");
        configFile.getFileConfig().addDefault("Inventory.StatsInventory.Other.Name", "&7Stats von &8{String}");

        try {
            messageFile.getFileConfig().save(messageFile.getFile());
            configFile.getFileConfig().save(configFile.getFile());
            mySqlFile.getFileConfig().save(mySqlFile.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initInventories() {

        setupInventory = Bukkit.getServer().createInventory(null, 3 * 9, getConfigFile().getValue("Inventory.SetupInventory.Name"));

        ItemStack lobbySpawnItemStack = new ItemBuilder(Material.COMPASS)
                .setName(getConfigFile().getValue("Inventory.SetupInventory.Items.LobbySpawn.Name"))
                .addLoreLine(getConfigFile().getValue("Inventory.SetupInventory.Items.LobbySpawn.Lore")).toItemStack();
        setupInventory.setItem(2, lobbySpawnItemStack);

        ItemStack ghostSpawnItemStack = new ItemBuilder(Material.TOTEM)
                .setName(getConfigFile().getValue("Inventory.SetupInventory.Items.GhostSpawn.Name"))
                .addLoreLine(getConfigFile().getValue("Inventory.SetupInventory.Items.GhostSpawn.Lore")).toItemStack();
        setupInventory.setItem(4, ghostSpawnItemStack);

        ItemStack pacManSpawnItemStack = new ItemBuilder(Material.GOLD_BLOCK)
                .setName(getConfigFile().getValue("Inventory.SetupInventory.Items.PacManSpawn.Name"))
                .addLoreLine(getConfigFile().getValue("Inventory.SetupInventory.Items.PacManSpawn.Lore")).toItemStack();
        setupInventory.setItem(6, pacManSpawnItemStack);

        ItemStack powerUpSpawnItemStack = new ItemBuilder(Material.IRON_NUGGET)
                .setName(getConfigFile().getValue("Inventory.SetupInventory.Items.PowerUpSpawn.Name"))
                .addLoreLine(getConfigFile().getValue("Inventory.SetupInventory.Items.PowerUpSpawn.Lore")).toItemStack();
        setupInventory.setItem(12, powerUpSpawnItemStack);

        ItemStack coinSpawnItemStack = new ItemBuilder(Material.GOLD_NUGGET)
                .setName(getConfigFile().getValue("Inventory.SetupInventory.Items.CoinSpawn.Name"))
                .addLoreLine(getConfigFile().getValue("Inventory.SetupInventory.Items.CoinSpawn.Lore")).toItemStack();
        setupInventory.setItem(14, coinSpawnItemStack);
    }

    public static PacMan getInstance() {
        return instance;
    }

    public FileManager getMessageFile() {
        return messageFile;
    }

    public FileManager getLocationFile() {
        return locationFile;
    }

    public Inventory getSetupInventory() {
        return setupInventory;
    }

    public RoleHandler getRoleHandler() {
        return roleHandler;
    }

    public ArrayList<Player> getPlayerList() {
        return playerList;
    }

    public void addToPlayerList(Player player) {
        playerList.add(player);
    }

    public void removeFromPlayerList(Player player) {
        playerList.remove(player);
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public PickupableItemStacks getPickupableItemStacks() {
        return pickupableItemStacks;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public PowerUpHandler getPowerUpHandler() {
        return powerUpHandler;
    }

    public ScoreHandler getScoreHandler() {
        return scoreHandler;
    }
    public CoinDotHandler getCoinDotHandler() {
        return coinDotHandler;
    }

    public PowerUpDotHandler getPowerUpDotHandler() {
        return powerUpDotHandler;
    }

    public FileManager getConfigFile() {
        return configFile;
    }

    public FileManager getMySqlFile() {
        return mySqlFile;
    }

    public ArrayList<Player> getSetupPlayerList() {
        return setupPlayerList;
    }

    public void addToSetupPlayerList(Player player) {
        setupPlayerList.add(player);
    }

    public void removeFromSetupPlayerList(Player player) {
        setupPlayerList.remove(player);
    }
}