package br.com.beegacha.model;

import org.bukkit.Material;

/**
 * Represents a single gacha reward with its display name, material, chance and command.
 */
public class Reward {

    private final String id;
    private final Material material;
    private final String displayName;
    private final double chance;
    private final String command;

    public Reward(String id, Material material, String displayName, double chance, String command) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.chance = chance;
        this.command = command;
    }

    /** Unique key used in config.yml (e.g. "common", "legendary"). */
    public String getId() {
        return id;
    }

    /** The Bukkit Material shown on the GUI reel slot. */
    public Material getMaterial() {
        return material;
    }

    /** Colour-formatted display name shown in the item lore/name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Weight used for weighted random selection (0–100). */
    public double getChance() {
        return chance;
    }

    /** Console command to run when this reward is selected (%player% placeholder). */
    public String getCommand() {
        return command;
    }
}
