package de.minecraft.plugin.spigot.role;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class RoleHandler {

    private final HashMap<Player, String> playerRoles;

    public RoleHandler() {
        this.playerRoles = new HashMap<>();
    }

    public HashMap<Player, String> getPlayerRoles() {
        return playerRoles;
    }
}