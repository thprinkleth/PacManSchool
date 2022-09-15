package de.minecraft.plugin.spigot.powerup;

import de.minecraft.plugin.spigot.PacMan;
import de.minecraft.plugin.spigot.util.EntitySpawner;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class PowerUpHandler {

    /**
     * powerUpList[0] = invincibility
     * powerUpList[1] = eating ghosts
     * powerUpList[2] = speed
     * powerUpList[3] = freezing ghosts
     * powerUpList[4] = double coins
     * powerUpList[5] = extra life
     */
    private boolean[] powerUpList;

    private final PacMan INSTANCE = PacMan.getInstance();
    private final HashMap<Integer, String> powerUpHashMap;

    int level, maxDuration, minDuration, maxDurationDoubleScore, minDurationDoubleScore;

    public PowerUpHandler() {
        powerUpList = new boolean[6];
        powerUpHashMap = new HashMap<>();
        level = 0;
        maxDuration = 7 * 20;
        minDuration = 7 * 10;
        maxDurationDoubleScore = maxDuration * 2;
        minDurationDoubleScore = minDuration * 2;
    }

    public boolean[] getPowerUpList() {
        return powerUpList;
    }

    public void deactivatePowerUps() {
        Arrays.fill(powerUpList, false);
    }

    public void deactivatePowerUp(int id) {
        powerUpList[id] = false;
    }

    public void activatePowerUp(int id) {
        deactivatePowerUps();
        powerUpList[id] = true;
        INSTANCE.getBossBarHandler().resetProgress();
    }

    public int getDuration(boolean doubleScore) {
        if (doubleScore) {
             return maxDurationDoubleScore - (((maxDurationDoubleScore - minDurationDoubleScore) / 3) * level);
        } else {
            return maxDuration - (((maxDuration - minDuration) / 3) * level);
        }
    }

    public int getMaxLifes() {

        // ((1 - maxLifes) / maxLevel) * x + maxLifes

        if (level == 0 || level == 1) {
            return 6;
        } else if (level == 2) {
            return 4;
        } else if (level == 3) {
            return 2;
        }

        return -1;
    }

    public int getLevel() {
        return level;
    }

    public void addLevel() {
        level++;
    }

    public void spawnPowerUps() {

        for (int i : powerUpHashMap.keySet()) {
            INSTANCE.getPowerUpDotHandler().deleteDotOnMap(INSTANCE.getLocationFile().getLocation("Game.Location.PowerUp." + i));
        }

        resetPowerUpHashMap();

        for (int i = 0; i < INSTANCE.getConfigFile().getIntValue("Game.Amount.Locations.PowerUps"); i++) {

            int powerUp = new Random().nextInt(6);
            Bukkit.getScheduler().runTask(INSTANCE, new EntitySpawner(INSTANCE.getLocationFile().getSpawn("Game.Location.PowerUp." + i), INSTANCE.getPickupableItemStacks().getPowerUpItemStack(powerUp)));

            switch (powerUp) {
                case 0:
                    powerUpHashMap.put(i, "Invincibility");
                    break;
                case 1:
                    powerUpHashMap.put(i, "GhostEating");
                    break;
                case 2:
                    powerUpHashMap.put(i, "Speed");
                    break;
                case 3:
                    powerUpHashMap.put(i, "GhostFreezing");
                    break;
                case 4:
                    powerUpHashMap.put(i, "DoubleCoins");
                    break;
                case 5:
                    powerUpHashMap.put(i, "ExtraLife");
                    break;
            }
        }

    }

    public HashMap<Integer, String> getPowerUpHashMap() {
        return powerUpHashMap;
    }

    public void resetPowerUpHashMap() {
        powerUpHashMap.clear();
    }

    public int getCurrentPowerUp() {
        int current = -1;
        for (int i = 0; i < powerUpList.length; i++) {
            if (powerUpList[i])
                current = i;
        }
        return current;
    }

    public boolean isDoubleScore() {
        return powerUpList[4];
    }
}
