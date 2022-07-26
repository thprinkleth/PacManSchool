package de.minecraft.plugin.spigot.powerup;

import de.minecraft.plugin.spigot.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PickupableItemStacks {

    public ItemStack coinItemStack() {
        ItemStack itemStack = new ItemBuilder(Material.GOLD_NUGGET).toItemStack();
        return itemStack;
    }

    public ItemStack getPowerUpItemStack(int id) {
        switch (id) {
            case 0:
                return invincibilityPowerUpItemStack();
            case 1:
                return eatingGhostPowerUpItemStack();
            case 2:
                return speedPowerUpItemStack();
            case 3:
                return freezeGhostPowerUpItemStack();
            case 4:
                return doubleScorePowerUpItemStack();
            case 5:
                return extraLifePowerUpItemStack();
        }
        return null;
    }

    public ItemStack speedPowerUpItemStack() {
        ItemStack itemStack = new ItemBuilder(Material.DIAMOND_HOE).toItemStack();
        return itemStack;
    }

    public ItemStack eatingGhostPowerUpItemStack() {
        ItemStack itemStack = new ItemBuilder(Material.DIAMOND_AXE).toItemStack();
        return itemStack;
    }

    public ItemStack invincibilityPowerUpItemStack() {
        ItemStack itemStack = new ItemBuilder(Material.DIAMOND_SWORD).toItemStack();
        return itemStack;
    }

    public ItemStack freezeGhostPowerUpItemStack() {
        ItemStack itemStack = new ItemBuilder(Material.DIAMOND_SPADE).toItemStack();
        return itemStack;
    }

    public ItemStack doubleScorePowerUpItemStack() {
        ItemStack itemStack = new ItemBuilder(Material.DIAMOND_PICKAXE).toItemStack();
        return itemStack;
    }

    public ItemStack extraLifePowerUpItemStack() {
        ItemStack itemStack = new ItemBuilder(Material.APPLE).toItemStack();
        return itemStack;
    }
}
