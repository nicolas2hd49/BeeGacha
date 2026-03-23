package br.com.beegacha.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Manages persistent per-player data stored in {@code playerdata.yml}.
 * Fields stored per player:
 * <ul>
 *   <li>{@code purchases} – number of gacha purchases made</li>
 *   <li>{@code last-use}  – Unix timestamp (ms) of last purchase</li>
 * </ul>
 *
 * <p>Writes are batched: in-memory mutations mark the config as <em>dirty</em>
 * and a periodic auto-save task flushes it to disk every 5 minutes.
 * {@link #save()} is also called on plugin disable.
 */
public class PlayerDataManager {

    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    /** True when in-memory data has unsaved changes. */
    private boolean dirty = false;

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Loads (or creates) the playerdata.yml file and starts the auto-save task. */
    public void load() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Auto-save every 5 minutes (6000 ticks) to flush any pending dirty data
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (dirty) save();
        }, 6000L, 6000L);
    }

    /** Saves the in-memory configuration back to disk (thread-safe). */
    public synchronized void save() {
        try {
            dataConfig.save(dataFile);
            dirty = false;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Purchases
    // -----------------------------------------------------------------------

    /** Returns how many gacha purchases the player has made. */
    public int getPurchases(UUID uuid) {
        return dataConfig.getInt(uuid + ".purchases", 0);
    }

    /**
     * Increments the player's purchase counter in memory and marks the
     * config dirty for the next auto-save cycle.
     */
    public synchronized void incrementPurchases(UUID uuid) {
        int current = getPurchases(uuid);
        dataConfig.set(uuid + ".purchases", current + 1);
        dirty = true;
    }

    // -----------------------------------------------------------------------
    // Last-use timestamp
    // -----------------------------------------------------------------------

    /** Returns the timestamp of the player's last gacha use (0 = never). */
    public long getLastUse(UUID uuid) {
        return dataConfig.getLong(uuid + ".last-use", 0L);
    }

    /**
     * Stores the given timestamp as the player's last-use time in memory and
     * marks the config dirty for the next auto-save cycle.
     */
    public synchronized void setLastUse(UUID uuid, long timestamp) {
        dataConfig.set(uuid + ".last-use", timestamp);
        dirty = true;
    }
}

