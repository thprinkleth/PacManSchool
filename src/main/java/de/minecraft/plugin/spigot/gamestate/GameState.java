package de.minecraft.plugin.spigot.gamestate;

public abstract class GameState {

    public static final int PREGAME_STATE = 0;
    public static final int INGAME_STATE = 1;
    public static final int POSTGAME_STATE = 2;

    public abstract void start();
    public abstract void stop();
}