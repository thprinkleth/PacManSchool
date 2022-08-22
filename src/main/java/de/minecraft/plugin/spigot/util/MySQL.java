package de.minecraft.plugin.spigot.util;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class MySQL {

    private final PacMan INSTANCE = PacMan.getInstance();

    private Connection connection;
    private final String USERNAME;
    private final int PORT;
    private final String DATABASE;
    private final String HOST;
    private final String PASSWORD;

    public MySQL() {

        this.USERNAME = INSTANCE.getMySqlFile().getValue("MySQL.Username");
        this.PORT = INSTANCE.getMySqlFile().getIntValue("MySQL.Port");
        this.DATABASE = INSTANCE.getMySqlFile().getValue("MySQL.Database");
        this.HOST = INSTANCE.getMySqlFile().getValue("MySQL.Host");
        this.PASSWORD = INSTANCE.getMySqlFile().getValue("MySQL.Password");

        connect();
    }

    private boolean isEnabled() {
        return INSTANCE.getMySqlFile().getBooleanValue("Login.Enabled");
    }

    private boolean isConnected() {
        return (connection != null);
    }

    public void connect() {

        if (isConnected()) {
            return;
        }

        try {

            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("MySQL.Connect.Wait"));
            connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?autoReconnect=true", USERNAME, PASSWORD);
            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("MySQL.Connect.Success"));

            createTable();

        } catch (SQLException ex) {

            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("MySQL.Connect.Error"));
        }

    }

    public void disconnect() {

        if (!isConnected() || !isEnabled()) {
            return;
        }

        try {
            connection.close();
            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("MySQL.Disconnect.Success"));
        } catch (SQLException ex) {
            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("MySQL.Disconnect.Error"));
        }

    }

    public void update(String query) {

        if (!isEnabled())
            return;

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void createTable() {

        if (!isEnabled())
            return;

        update("CREATE TABLE IF NOT EXISTS PacMan (uid VARCHAR(64), uuid VARCHAR(64), playedGames INT, winsPacMan INT, losesPacMan INT, winsGhost INT, losesGhost INT, pacManEaten INT)");
    }

    public ResultSet query(String query) {

        ResultSet resultSet = null;

        try {

            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return resultSet;
    }

    public boolean exists(Player player) {

        if (!isEnabled())
            return false;

        ResultSet resultSet = query("SELECT * FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");

        try {
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public void addGame(Player player) {

        if (!isEnabled())
            return;

        if (exists(player)) {
            update("UPDATE PacMan SET playedGames = " + (getGames(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
        } else {
            update("INSERT INTO PacMan (uid, uuid, playedGames, wins, loses, winsPacMan, losesPacMan, winsGhost, losesGhost, pacManEaten) VALUES ('" + player.getName() + "', '" + player.getUniqueId().toString() + "', 1, 0, 0, 0, 0, 0, 0, 0)");
        }
    }

    public int getGames(Player player) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT playedGames FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("playedGames");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int getGames(String uuid) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT playedGames FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("playedGames");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public void addWins(Player player) {

        if (!isEnabled())
            return;

        if (exists(player)) {
            update("UPDATE PacMan SET wins = " + (getWins(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
        } else {
            update("INSERT INTO PacMan (uid, uuid, playedGames, wins, loses, winsPacMan, losesPacMan, winsGhost, losesGhost, pacManEaten) VALUES ('" + player.getName() + "', '" + player.getUniqueId().toString() + "', 1, 1, 0, 0, 0, 0, 0, 0)");
        }
    }

    public int getWins(Player player) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT wins FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("wins");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public void addLoses(Player player) {

        if (!isEnabled())
            return;

        if (exists(player)) {
            update("UPDATE PacMan SET loses = " + (getLoses(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
        } else {
            update("INSERT INTO PacMan (uid, uuid, playedGames, wins, loses, winsPacMan, losesPacMan, winsGhost, losesGhost, pacManEaten) VALUES ('" + player.getName() + "', '" + player.getUniqueId().toString() + "', 1, 0, 1, 0, 0, 0, 0, 0)");
        }
    }

    public int getLoses(Player player) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT loses FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("loses");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public void addWinsPacMan(Player player) {

        if (!isEnabled())
            return;

        update("UPDATE PacMan SET winsPacMan = " + (getWinsPacMan(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getWinsPacMan(Player player) {

        if (!isEnabled())
            return 0;

        try {

            ResultSet resultSet = query("SELECT winsPacMan FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");

            if (resultSet == null) {
                return 0;
            }

            if (resultSet.next()) {
                return resultSet.getInt("winsPacMan");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public int getWinsPacMan(String uuid) {

        if (!isEnabled())
            return 0;

        try {

            ResultSet resultSet = query("SELECT winsPacMan FROM PacMan WHERE uuid = '" + uuid + "'");

            if (resultSet == null) {
                return 0;
            }

            if (resultSet.next()) {
                return resultSet.getInt("winsPacMan");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public void addLosesPacMan(Player player) {

        if (!isEnabled())
            return;

        update("UPDATE PacMan SET losesPacMan = " + (getLosesPacMan(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getLosesPacMan(Player player) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT losesPacMan FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesPacMan");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public int getLosesPacMan(String uuid) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT losesPacMan FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesPacMan");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public void addWinsGhost(Player player) {

        if (!isEnabled())
            return;

        update("UPDATE PacMan SET winsGhost = " + (getWinsGhost(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getWinsGhost(Player player) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT winsGhost FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("winsGhost");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public int getWinsGhost(String uuid) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT winsGhost FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("winsGhost");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public void addLosesGhost(Player player) {

        if (!isEnabled())
            return;

        update("UPDATE PacMan SET losesGhost = " + (getLosesPacMan(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getLosesGhost(Player player) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT losesGhost FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesGhost");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public int getLosesGhost(String uuid) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT losesGhost FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesGhost");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public void addPacmanEaten(Player player) {

        if (!isEnabled())
            return;

        update("UPDATE PacMan SET pacManEaten = " + (getPacmanEaten(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getPacmanEaten(Player player) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT pacManEaten FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("pacManEaten");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    public int getPacmanEaten(String uuid) {

        if (!isEnabled())
            return 0;

        try {
            ResultSet resultSet = query("SELECT pacManEaten FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("pacManEaten");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    // Returns the 10 best players in the database sorted by winsPacMan and winsGhost
    public ArrayList<OfflinePlayer> getBestPlayers() {

        if (!isEnabled())
            return new ArrayList<OfflinePlayer>();

        ArrayList<OfflinePlayer> players = new ArrayList<>();

        try {
            ResultSet resultSet = query("SELECT * FROM PacMan ORDER BY wins DESC LIMIT 5");
            while (resultSet.next()) {
                players.add(Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("uuid"))));
            }
        } catch (SQLException ex) {
        }
        return players;
    }
}
