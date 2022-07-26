package de.minecraft.plugin.spigot.util;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;

public class MySQL {

    private final PacMan INSTANCE = PacMan.getInstance();

    private Connection connection;
    private final String USERNAME;
    private final int PORT;
    private final String DATABASE;
    private final String HOST;
    private final String PASSWORD;

    public MySQL() {

        this.USERNAME = INSTANCE.getMySqlFile().getValue("Login.Username");
        this.PORT = INSTANCE.getMySqlFile().getIntValue("Login.Port");
        this.DATABASE = INSTANCE.getMySqlFile().getValue("Login.Database");
        this.HOST = INSTANCE.getMySqlFile().getValue("Login.Host");
        this.PASSWORD = INSTANCE.getMySqlFile().getValue("Login.Password");

        connect();
        createTable();
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void disconnect() {

        if (!isConnected()) {
            return;
        }

        try {
            connection.close();
            Bukkit.getConsoleSender().sendMessage(INSTANCE.getMessageFile().getValue("MySQL.Disconnect.Success"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void update(String query) {

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void createTable() {
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

        if (exists(player)) {
            update("UPDATE PacMan SET playedGames = " + (getGames(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
        } else {
            update("INSERT INTO PacMan (uid, uuid, playedGames, winsPacMan, losesPacMan, winsGhost, losesGhost, pacManEaten) VALUES ('" + player.getName() + "', '" + player.getUniqueId().toString() + "', 1, 0, 0, 0, 0, 0)");
        }
    }

    public int getGames(Player player) {

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

    public void addWinsPacMan(Player player) {
        update("UPDATE PacMan SET winsPacMan = " + (getWinsPacMan(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getWinsPacMan(Player player) {

        try {

            ResultSet resultSet = query("SELECT winsPacMan FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");

            if (resultSet == null) {
                return 0;
            }

            if (resultSet.next()) {
                return resultSet.getInt("winsPacMan");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public int getWinsPacMan(String uuid) {

        try {

            ResultSet resultSet = query("SELECT winsPacMan FROM PacMan WHERE uuid = '" + uuid + "'");

            if (resultSet == null) {
                return 0;
            }

            if (resultSet.next()) {
                return resultSet.getInt("winsPacMan");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public void addLosesPacMan(Player player) {
        update("UPDATE PacMan SET losesPacMan = " + (getLosesPacMan(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getLosesPacMan(Player player) {

        try {
            ResultSet resultSet = query("SELECT losesPacMan FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesPacMan");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public int getLosesPacMan(String uuid) {

        try {
            ResultSet resultSet = query("SELECT losesPacMan FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesPacMan");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public void addWinsGhost(Player player) {
        update("UPDATE PacMan SET winsGhost = " + (getWinsGhost(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getWinsGhost(Player player) {

        try {
            ResultSet resultSet = query("SELECT winsGhost FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("winsGhost");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public int getWinsGhost(String uuid) {

        try {
            ResultSet resultSet = query("SELECT winsGhost FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("winsGhost");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public void addLosesGhost(Player player) {
        update("UPDATE PacMan SET losesGhost = " + (getLosesPacMan(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getLosesGhost(Player player) {

        try {
            ResultSet resultSet = query("SELECT losesGhost FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesGhost");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public int getLosesGhost(String uuid) {

        try {
            ResultSet resultSet = query("SELECT losesGhost FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("losesGhost");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public void addPacmanEaten(Player player) {
        update("UPDATE PacMan SET pacManEaten = " + (getPacmanEaten(player) + 1) + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
    }

    public int getPacmanEaten(Player player) {

        try {
            ResultSet resultSet = query("SELECT pacManEaten FROM PacMan WHERE uuid = '" + player.getUniqueId().toString() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("pacManEaten");
            }
        } catch (SQLException ex) {}

        return 0;
    }

    public int getPacmanEaten(String uuid) {

        try {
            ResultSet resultSet = query("SELECT pacManEaten FROM PacMan WHERE uuid = '" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt("pacManEaten");
            }
        } catch (SQLException ex) {}

        return 0;
    }
}
