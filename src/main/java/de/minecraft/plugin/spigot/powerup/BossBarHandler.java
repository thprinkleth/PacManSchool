package de.minecraft.plugin.spigot.powerup;

import de.minecraft.plugin.spigot.PacMan;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarHandler {

    private final PacMan INSTANCE = PacMan.getInstance();
    private int progress;

    private BossBar bossbar;

    public BossBarHandler() {

        this.bossbar = Bukkit.createBossBar("Test", BarColor.BLUE, BarStyle.SOLID);

        addAllPlayers();

        this.bossbar.setVisible(true);
        this.bossbar.setProgress(0);

        updateProgress();
    }

    private void addAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.bossbar.addPlayer(player);
        }
    }

    public int calculateProgress() {

        int currentPowerUp = INSTANCE.getPowerUpHandler().getCurrentPowerUp();
        boolean isDoubleScore = INSTANCE.getPowerUpHandler().isDoubleScore();

        if (currentPowerUp == -1) {
            progress = 0;
            return progress;
        }

        int maxDuration = INSTANCE.getPowerUpHandler().getDuration(isDoubleScore);

        progress++;

        if (progress >= maxDuration || !INSTANCE.getPowerUpHandler().getPowerUpList()[currentPowerUp]) {
            progress = maxDuration;
        }

        return Math.abs(1 - (progress / maxDuration));
    }

    public void updateProgress() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(INSTANCE, new Runnable() {

            @Override
            public void run() {
                bossbar.setProgress(calculateProgress());
                bossbar.setVisible(progress != 0);
            }
        }, 0, 1);
    }

    public void resetProgress() {
        progress = 0;
    }
}
