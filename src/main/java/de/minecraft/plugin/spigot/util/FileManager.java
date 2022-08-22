package de.minecraft.plugin.spigot.util;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private final PacMan INSTANCE = PacMan.getInstance();

    public File folder;
    public File file;
    public YamlConfiguration fileConfig;

    public FileManager(String name) {

        folder = new File("plugins/PacMan");

        if (!folder.exists()) {
            folder.mkdir();
        }

        file = new File(folder, name);
        fileConfig = YamlConfiguration.loadConfiguration(file);

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(String path, Object value) {
        fileConfig.set(path, value);
        saveFile();
    }

    public String getValue(String path) {

        String string = fileConfig.get(path).toString();

        string = string.replace('&', '§');
        string = string.replace("{Prefix}", INSTANCE.getMessageFile().getFileConfig().get("Server.Prefix").toString().replace('&', '§'));

        return string;
    }

    public String getValue(String path, Player player, Object... args) {

        String string = getValue(path);

        string = string.replace("{XValue}", String.valueOf(player.getLocation().getX()));
        string = string.replace("{YValue}", String.valueOf(player.getLocation().getY()));
        string = string.replace("{ZValue}", String.valueOf(player.getLocation().getZ()));
        string = string.replace("{YawValue}", String.valueOf(player.getLocation().getYaw()));
        string = string.replace("{PitchValue}", String.valueOf(player.getLocation().getPitch()));
        string = string.replace("{PitchValue}", String.valueOf(player.getLocation().getPitch()));
        string = string.replace("{WorldValue}", String.valueOf(player.getLocation().getWorld().getName()));
        string = string.replace("{ServerPlayers}", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()));
        string = string.replace("{PlayersNeededToStart}", String.valueOf(INSTANCE.getConfigFile().getValue("Game.PlayersNeededToStart")));
        string = string.replace("{PlayerName}", String.valueOf(player.getDisplayName()));
        string = string.replace("{MaxPlayers}", String.valueOf(Bukkit.getServer().getMaxPlayers()));

        for (int i = 0; i < args.length; i++) {
            string = string.replace("{args[" + i + "]}", args[i].toString());
        }

        return string;
    }

    public int getIntValue(String path) {
        return fileConfig.getInt(path);
    }

    public void setLocation(String path, Location location) {

        fileConfig.set(path + ".x", location.getX());
        fileConfig.set(path + ".y", location.getY());
        fileConfig.set(path + ".z", location.getZ());
        fileConfig.set(path + ".yaw", Double.valueOf(location.getYaw()));
        fileConfig.set(path + ".pitch", Double.valueOf(location.getPitch()));
        fileConfig.set(path + ".world", location.getWorld().getName());

        saveFile();
    }

    public Location getSpawn(String path) {

        double x = (double) fileConfig.get(path + ".x") + 0.5;
        double y = (double) fileConfig.get(path + ".y") + 1;
        double z = (double) fileConfig.get(path + ".z") + 0.5;
        double yaw = (double) fileConfig.get(path + ".yaw");
        double pitch = (double) fileConfig.get(path + ".pitch");
        String world = (String) fileConfig.get(path + ".world");

        return new Location(Bukkit.getWorld(world), x, y, z, Float.valueOf(String.valueOf(yaw)), Float.valueOf(String.valueOf(pitch)));
    }

    public Location getLocation(String path) {

        double x = (double) fileConfig.get(path + ".x");
        double y = (double) fileConfig.get(path + ".y");
        double z = (double) fileConfig.get(path + ".z");
        double yaw = (double) fileConfig.get(path + ".yaw");
        double pitch = (double) fileConfig.get(path + ".pitch");
        String world = (String) fileConfig.get(path + ".world");

        return new Location(Bukkit.getWorld(world), x, y, z, Float.valueOf(String.valueOf(yaw)), Float.valueOf(String.valueOf(pitch)));
    }

    public boolean getBooleanValue(String path) {
        return fileConfig.getBoolean(path);
    }

    public void saveFile() {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFile() {
        return file;
    }

    public YamlConfiguration getFileConfig() {
        return fileConfig;
    }
}
