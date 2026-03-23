package br.com.beegacha.gui;

import br.com.beegacha.BeeGachaPlugin;
import br.com.beegacha.model.Reward;
import org.bukkit.entity.Player;

/**
 * Factory / facade for creating and opening {@link GachaSession}s.
 * Keeps command logic clean by centralising GUI creation here.
 */
public class GachaGUI {

    private final BeeGachaPlugin plugin;

    public GachaGUI(BeeGachaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Picks a winning reward, creates a new {@link GachaSession} for the
     * player, registers it with the {@link GachaListener}, and starts the
     * animation.
     *
     * @param player the player who triggered the gacha
     */
    public void open(Player player) {
        // Pre-determine the winning reward
        Reward winning = plugin.getRewardManager().selectReward();
        if (winning == null) {
            plugin.getLogger().warning("No rewards configured – aborting gacha for " + player.getName());
            return;
        }

        GachaSession session = new GachaSession(plugin, player, winning);
        plugin.getGachaListener().registerSession(player, session);
        session.start();
    }
}
