package de.minecraft.plugin.spigot;

import de.minecraft.plugin.spigot.cmds.*;
import de.minecraft.plugin.spigot.gamestate.GameState;
import de.minecraft.plugin.spigot.gamestate.GameStateManager;
import de.minecraft.plugin.spigot.listeners.*;
import de.minecraft.plugin.spigot.minimap.CoinDotHandler;
import de.minecraft.plugin.spigot.minimap.GhostDotHandler;
import de.minecraft.plugin.spigot.minimap.PowerUpDotHandler;
import de.minecraft.plugin.spigot.powerup.BossBarHandler;
import de.minecraft.plugin.spigot.powerup.PickupableItemStacks;
import de.minecraft.plugin.spigot.powerup.PowerUpHandler;
import de.minecraft.plugin.spigot.role.RoleHandler;
import de.minecraft.plugin.spigot.score.ScoreHandler;
import de.minecraft.plugin.spigot.util.FileManager;
import de.minecraft.plugin.spigot.util.ItemBuilder;
import de.minecraft.plugin.spigot.util.MySQL;
import de.minecraft.plugin.spigot.util.TeleportHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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
     * - Rework replacement params
     * - Rework game state design (when to start, when to end, etc.)
     * - Smoother transitions between GameStates/overall teleports (Sounds, Particles)
     * - Feedback when PacMan got hit by a ghost and vice versa
     * - Automatic game start
     * - Bugfix Bossbar doesn't disappear when new powerup is picked up
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
    private GhostDotHandler ghostDotHandler;
    private BossBarHandler bossBarHandler;
    private TeleportHandler teleportHandler;

    private ArrayList <Player> playerList;
    private ArrayList <Player> setupPlayerList;

    private Inventory setupInventory;

    @Override
    public void onEnable() {

        instance = this;

        messageFile = new FileManager("messages.yml");
        locationFile = new FileManager("locations.yml");
        configFile = new FileManager("config.yml");
        mySqlFile = new FileManager("mysql.yml");

        defaultFileSetup();

        mySQL = new MySQL();

        roleHandler = new RoleHandler();
        playerList = new ArrayList<>();
        setupPlayerList = new ArrayList<>();

        gameStateManager = new GameStateManager();
        pickupableItemStacks = new PickupableItemStacks();
        scoreHandler = new ScoreHandler();
        powerUpHandler = new PowerUpHandler();
        coinDotHandler = new CoinDotHandler();
        powerUpDotHandler = new PowerUpDotHandler();
        ghostDotHandler = new GhostDotHandler();
        teleportHandler = new TeleportHandler();

        gameStateManager.setCurrent(GameState.PREGAME_STATE);

        initInventories();

        registerCommands();
        registerListeners();

        Bukkit.getConsoleSender().sendMessage(messageFile.getValue("Server.StartUp.Message"));
    }

    @Override
    public void onDisable() {

        mySQL.disconnect();


        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("Server closing.");
        }

        Bukkit.getConsoleSender().sendMessage(messageFile.getValue("Server.ShutDown.Message"));
    }

    private void registerCommands() {
        getCommand("setup").setExecutor(new CmdSetup());
        getCommand("start").setExecutor(new CmdStart());
        getCommand("stats").setExecutor(new CmdStats());
        getCommand("gm").setExecutor(new CmdGm());
        getCommand("ranking").setExecutor(new CmdRanking());
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

        FileConfiguration mySqlConfig = mySqlFile.getFileConfig();
        FileConfiguration configConfig = configFile.getFileConfig();
        FileConfiguration messageConfig = messageFile.getFileConfig();

        mySqlConfig.options().header("MySQL-Database connection.");
        messageConfig.options().header("All strings contained in the plugin.");
        configConfig.options().header("All changeable values for the behaviour of the game.");

        addDefault(mySqlConfig, "MySQL.Enabled", "false");
        addDefault(mySqlConfig, "MySQL.Username", "sql11509406");
        addDefault(mySqlConfig, "MySQL.Port", 3306);
        addDefault(mySqlConfig, "MySQL.Database", "sql11509406");
        addDefault(mySqlConfig, "MySQL.Host", "sql11.freemysqlhosting.net");
        addDefault(mySqlConfig, "MySQL.Password", "1ww5RG1tnM");

        addDefault(messageConfig, "Server.Prefix", "&bPacMan &7|");

        addDefault(messageConfig, "MySQL.Connect.Wait", "{Prefix} &6Connecting with MySQL-Database...");
        addDefault(messageConfig, "MySQL.Connect.Success", "{Prefix} &aConnection with MySQL-Database established.");
        addDefault(messageConfig, "MySQL.Connect.Error", "{Prefix} &cConnection with MySQL-Database failed.");
        addDefault(messageConfig, "MySQL.Disconnect.Success", "{Prefix} &cConnection with MySQL-Database aborted.");
        addDefault(messageConfig, "MySQL.Disconnect.Error", "{Prefix} &cConnection with MySQL-Database couldn't be aborted.");

        addDefault(messageConfig, "Server.StartUp.Message", "{Prefix} &aPlugin started.");
        addDefault(messageConfig, "Server.ShutDown.Message", "{Prefix} &cPlugin stopped.");

        addDefault(messageConfig, "Commands.NoPlayer", "{Prefix} &cThe command can only be performed by a player.");

        addDefault(messageConfig, "Commands.Setup.Syntax", "{Prefix} &cSyntax: &6/setup");
        addDefault(messageConfig, "Commands.Start.Syntax", "{Prefix} &cSyntax: &6/start");
        addDefault(messageConfig, "Commands.Ranking.Syntax", "{Prefix} &cSyntax: &6/ranking");
        addDefault(messageConfig, "Commands.Stats.Syntax", "{Prefix} &cSyntax: &6/stats <playername>");
        addDefault(messageConfig, "Commands.Gm.Syntax", "{Prefix} &cSyntax: &6/gm <0-3>");

        addDefault(messageConfig, "Commands.Gm.Exec.Survival", "{Prefix} &aYou switched to Survival-Mode&a.");
        addDefault(messageConfig, "Commands.Gm.Exec.Creative", "{Prefix} &aYou switched to Creative-Mode&a.");
        addDefault(messageConfig, "Commands.Gm.Exec.Adventure", "{Prefix} &aYou switched to Adventure-Mode&a.");
        addDefault(messageConfig, "Commands.Gm.Exec.Spectator", "{Prefix} &aYou switched to Spectator-Mode&a.");

        addDefault(messageConfig, "Commands.Setup.Exec.Activated", "{Prefix} &aYou got the item for the setup.");
        addDefault(messageConfig, "Commands.Setup.Exec.Deactivated", "{Prefix} &cYou lost the item for the setup.");

        addDefault(messageConfig, "Commands.Stats.PlayerNotFound", "{Prefix} &aThe player &6{args[0]} &awas not found.");

        addDefault(messageConfig, "Commands.NoPerm", "{Prefix} &cYou don't have the permission to perform this command.");

        addDefault(messageConfig, "World.Join", "{Prefix} &6{PlayerName} &ajoined the server. &7({ServerPlayers}/&8{MaxPlayers}&7)");
        addDefault(messageConfig, "World.Quit", "{Prefix} &6{PlayerName} &cleft the server. &7({args[0]}/&8{MaxPlayers}&7)");

        addDefault(messageConfig, "Setup.Spawn.Set.Lobby.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6lobby&a.");
        addDefault(messageConfig, "Setup.Spawn.Set.Lobby.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for the &6lobby&c.");
        addDefault(messageConfig, "Setup.Spawn.Set.Ghosts.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6ghosts&a.");
        addDefault(messageConfig, "Setup.Spawn.Set.Ghosts.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for the &6ghost&c.");
        addDefault(messageConfig, "Setup.Spawn.Set.PacMan.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6PacMan&a.");
        addDefault(messageConfig, "Setup.Spawn.Set.PacMan.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for &6PacMan&c.");
        addDefault(messageConfig, "Setup.Spawn.Set.Coin.Success", "{Prefix} &aA position for a coin has been set. &7({args[0])");
        addDefault(messageConfig, "Setup.Spawn.Set.Coin.NotSuccess", "{Prefix} &cAn error occurred while trying to set a position for a coin.");
        addDefault(messageConfig, "Setup.Spawn.Set.AutoCoin.Success", "{Prefix} &6{args[0]} &apositions have been set for a coin position.");
        addDefault(messageConfig, "Setup.Spawn.Set.PowerUp.Success", "{Prefix} &aSuccessfully set the spawnpoint for the &6PowerUp&a.");
        addDefault(messageConfig, "Setup.Spawn.Set.PowerUp.NotSuccess", "{Prefix} &cAn error occurred while trying to set the spawnpoint for the &6PowerUp&c.");

        addDefault(messageConfig, "Setup.Spawn.Set.AutoCoin.FirstLocation", "{Prefix} &aSuccessfully set the &6first location &afor &6AutoCoin&a.");
        addDefault(messageConfig, "Setup.Spawn.Set.AutoCoin.SecondLocation", "{Prefix} &aSuccessfully set the &6second location &afor &6AutoCoin&a. &8Calculating...");

        addDefault(messageConfig, "Lobby.Countdown.Counting", "{Prefix} &aThe game will start in &6{args[0]}&a seconds.");
        addDefault(messageConfig, "Lobby.Countdown.Starting", "{Prefix} &aThe game starts now.");
        addDefault(messageConfig, "Lobby.Countdown.NotEnoughLocations", "{Prefix} &cYou must set all positions in order to start the game.");
        addDefault(messageConfig, "Lobby.Countdown.NotEnoughPlayers", "{Prefix} &cThere must be &6{PlayersNeededToStart} §cplayers online in order to start the game..");

        addDefault(messageConfig, "Game.NextLevel.PacMan.Title", "&aYou reached the next Level!");
        addDefault(messageConfig, "Game.NextLevel.PacMan.SubTitle", "&7New Level: &6{args[0]}");
        addDefault(messageConfig, "Game.NextLevel.Ghost.Title", "&cPacMan reached the next level.");
        addDefault(messageConfig, "Game.NextLevel.Ghost.SubTitle", "&7New Level: &6{args[0]}");

        addDefault(messageConfig, "Game.Finish.Win.PacMan.Title", "&aYou won as &6PacMan&a!");
        addDefault(messageConfig, "Game.Finish.Win.PacMan.SubTitle", "&7Congratulations!");
        addDefault(messageConfig, "Game.Finish.Lose.PacMan.Title", "&cYou lost as &6PacMan&c!");
        addDefault(messageConfig, "Game.Finish.Lose.PacMan.SubTitle", "&7Better luck next time!");
        addDefault(messageConfig, "Game.Finish.Win.Ghost.Title", "&aYou won as a &6Ghost&a!");
        addDefault(messageConfig, "Game.Finish.Win.Ghost.SubTitle", "&7Congratulations!");
        addDefault(messageConfig, "Game.Finish.Lose.Ghost.Title", "&cYou lost as a &6Ghost&c!");
        addDefault(messageConfig, "Game.Finish.Lose.Ghost.SubTitle", "&7Better luck next time!");

        addDefault(messageConfig, "Game.Ghost.Eaten.Title", "&cYou have been eaten by a Ghost!");
        addDefault(messageConfig, "Game.Ghost.Eaten.SubTitle", "&7You've been send back to spawn");

        addDefault(messageConfig, "Game.Score.PacMan", "&aYour score: &6{args[0]}");
        addDefault(messageConfig, "Game.Score.Ghost", "&bScore of PacMan: &6{args[0]}");

        addDefault(configConfig, "Inventory.SetupInventory.Name", "&aSetup-Inventory");

        addDefault(configConfig, "Game.Amount.Locations.Coins", 0);
        addDefault(configConfig, "Game.Amount.Locations.PowerUps", 0);
        addDefault(configConfig, "Game.PlayersNeededToStart", 1);
        addDefault(configConfig, "Game.Countdown.Value", 5);

        addDefault(configConfig, "Inventory.SetupInventory.Items.LobbySpawn.Name", "&bLobby-Spawnpoint");
        addDefault(configConfig, "Inventory.SetupInventory.Items.LobbySpawn.Lore", "&7Sets the position where the players will spawn before and after the game.");
        addDefault(configConfig, "Inventory.SetupInventory.Items.GhostSpawn.Name", "&bGhost-SpawnPoint");
        addDefault(configConfig, "Inventory.SetupInventory.Items.GhostSpawn.Lore", "&7Sets the position where the ghosts will spawn.");
        addDefault(configConfig, "Inventory.SetupInventory.Items.PacManSpawn.Name", "&bPacMan-Spawnpoint");
        addDefault(configConfig, "Inventory.SetupInventory.Items.PacManSpawn.Lore", "&7Sets the position where the PacMan will spawn.");
        addDefault(configConfig, "Inventory.SetupInventory.Items.PowerUpSpawn.Name", "&bPowerUp-Spawnpoint");
        addDefault(configConfig, "Inventory.SetupInventory.Items.PowerUpSpawn.Lore", "&7Sets the position where a PowerUp will spawn.");
        addDefault(configConfig, "Inventory.SetupInventory.Items.CoinSpawn.Name", "&bCoin-Spawnpoint");
        addDefault(configConfig, "Inventory.SetupInventory.Items.CoinSpawn.Lore", "&7Sets the position where a Coin will spawn.");

        addDefault(configConfig, "Items.Setup.Name", "&bSetupItem");
        addDefault(configConfig, "Items.Setup.Lore", "&6Rightclick on a block to open the setup-inventory.");
        addDefault(configConfig, "Items.SetupCoin.Name", "&bSetupCoins");
        addDefault(configConfig, "Items.SetupCoin.Lore", "&7Rightclick on a block to set the location for a coin.");

        addDefault(configConfig, "Inventory.StatsInventory.Self.Name", "&7Your &8stats");
        addDefault(configConfig, "Inventory.StatsInventory.Other.Name", "&8{args[0]}'s &7stats");

        try {
            messageConfig.save(messageFile.getFile());
            configConfig.save(configFile.getFile());
            mySqlConfig.save(mySqlFile.getFile());
        } catch (IOException e) {
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

    private void addDefault(FileConfiguration config, String path, Object value) {

        if (config == null) return;
        if (path == null) return;
        if (value == null) return;

        if (!config.contains(path)) {
            config.set(path, value);
        }
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
    public GhostDotHandler getGhostDotHandler() {
        return ghostDotHandler;
    }
    public void setBossBarHandler(BossBarHandler bossBarHandler) {
        this.bossBarHandler = bossBarHandler;
    }
    public BossBarHandler getBossBarHandler() {
        return bossBarHandler;
    }
    public TeleportHandler getTeleportHandler() {
        return teleportHandler;
    }
}