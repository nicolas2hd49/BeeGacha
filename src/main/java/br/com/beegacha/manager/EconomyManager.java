package br.com.beegacha.manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Manages Vault economy integration.
 * Wrap all economy calls through this class to keep the rest of the plugin
 * decoupled from the Vault API.
 */
public class EconomyManager {

    private Economy economy;

    /**
     * Attempts to hook into Vault's economy service.
     *
     * @return {@code true} if a valid economy provider was found.
     */
    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    /** Returns whether the economy hook is active. */
    public boolean isAvailable() {
        return economy != null;
    }

    /**
     * Checks whether the player has at least {@code amount} in their account.
     */
    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    /**
     * Withdraws {@code amount} from the player's account.
     *
     * @return {@code true} if the transaction succeeded.
     */
    public boolean withdraw(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /** Returns the player's current balance. */
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    /** Returns the formatted currency string for a given amount. */
    public String format(double amount) {
        return economy.format(amount);
    }
}
