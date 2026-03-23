package br.com.beegacha;

import br.com.beegacha.commands.BeeGachaCommand;
import br.com.beegacha.gui.GachaGUI;
import br.com.beegacha.gui.GachaListener;
import br.com.beegacha.manager.CooldownManager;
import br.com.beegacha.manager.EconomyManager;
import br.com.beegacha.manager.PlayerDataManager;
import br.com.beegacha.manager.RewardManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entry point for the BeeGacha plugin.
 *
 * <p>Wires up all managers and registers commands / event listeners.
 */
public class BeeGachaPlugin extends JavaPlugin {

    // -----------------------------------------------------------------------
    // Managers (accessible via getters so they can be injected elsewhere)
    // -----------------------------------------------------------------------

    private EconomyManager  economyManager;
    private RewardManager   rewardManager;
    private CooldownManager cooldownManager;
    private PlayerDataManager playerDataManager;

    // -----------------------------------------------------------------------
    // GUI components
    // -----------------------------------------------------------------------

    private GachaGUI      gachaGUI;
    private GachaListener gachaListener;

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist yet
        saveDefaultConfig();

        // Set up economy (requires Vault)
        economyManager = new EconomyManager();
        if (!economyManager.setup()) {
            getLogger().warning("Vault economy not found. Economy features will be disabled.");
        }

        // Reward manager – load from config
        rewardManager = new RewardManager();
        rewardManager.load(getConfig());

        // Cooldown manager – read cooldown hours from config
        long cooldownHours = getConfig().getLong("cooldown-hours", 2L);
        cooldownManager = new CooldownManager(cooldownHours);

        // Player data manager – load persistent YAML storage
        playerDataManager = new PlayerDataManager(this);
        playerDataManager.load();

        // Restore in-memory cooldown state from persisted data so that
        // cooldowns survive server restarts.
        restoreCooldowns();

        // GUI
        gachaListener = new GachaListener();
        gachaGUI      = new GachaGUI(this);

        // Register events
        getServer().getPluginManager().registerEvents(gachaListener, this);

        // Register commands
        BeeGachaCommand cmdExecutor = new BeeGachaCommand(this);
        var cmd = getCommand("beegacha");
        if (cmd != null) {
            cmd.setExecutor(cmdExecutor);
        }

        getLogger().info("BeeGacha enabled! 🐝");
    }

    @Override
    public void onDisable() {
        playerDataManager.save();
        getLogger().info("BeeGacha disabled.");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Loads every stored last-use timestamp back into the in-memory
     * {@link CooldownManager} so cooldowns persist across restarts.
     */
    private void restoreCooldowns() {
        var yaml = playerDataManager;
        // Iterate all keys in playerdata.yml
        // The YAML root contains UUID strings as keys
        var dataConfig = getYamlDataConfig();
        if (dataConfig == null) return;

        for (String key : dataConfig.getKeys(false)) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(key);
                long lastUse = yaml.getLastUse(uuid);
                if (lastUse > 0) {
                    cooldownManager.setLastUse(uuid, lastUse);
                }
            } catch (IllegalArgumentException ignored) {
                // key is not a UUID – skip
            }
        }
    }

    /**
     * Exposes the raw playerdata YAML config for iteration.
     * Returns {@code null} if the file hasn't been loaded yet.
     */
    private org.bukkit.configuration.file.FileConfiguration getYamlDataConfig() {
        java.io.File file = new java.io.File(getDataFolder(), "playerdata.yml");
        if (!file.exists()) return null;
        return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public EconomyManager getEconomyManager()       { return economyManager;    }
    public RewardManager getRewardManager()          { return rewardManager;     }
    public CooldownManager getCooldownManager()      { return cooldownManager;   }
    public PlayerDataManager getPlayerDataManager()  { return playerDataManager; }
    public GachaGUI getGachaGUI()                    { return gachaGUI;          }
    public GachaListener getGachaListener()          { return gachaListener;     }
}
