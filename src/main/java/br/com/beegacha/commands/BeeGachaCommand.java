package br.com.beegacha.commands;

import br.com.beegacha.BeeGachaPlugin;
import br.com.beegacha.utils.MessageUtils;
import br.com.beegacha.utils.MathUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Executor for the {@code /beegacha} command.
 *
 * <p>Flow:
 * <ol>
 *   <li>Verify the sender is a player.</li>
 *   <li>Check {@code beegacha.use} permission.</li>
 *   <li>Verify economy is available.</li>
 *   <li>Check cooldown (2 hours).</li>
 *   <li>Compute progressive price and check funds.</li>
 *   <li>Withdraw funds and update player data.</li>
 *   <li>Open the animated GUI roulette.</li>
 * </ol>
 */
public class BeeGachaCommand implements CommandExecutor {

    private final BeeGachaPlugin plugin;

    public BeeGachaCommand(BeeGachaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Must be a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /beegacha.");
            return true;
        }

        // Permission check
        if (!player.hasPermission("beegacha.use")) {
            player.sendMessage(MessageUtils.colorize(
                    plugin.getConfig().getString("messages.no-permission",
                            "&cVocê não tem permissão.")));
            return true;
        }

        // Economy availability check
        if (!plugin.getEconomyManager().isAvailable()) {
            player.sendMessage(MessageUtils.colorize(
                    plugin.getConfig().getString("messages.economy-missing",
                            "&cEconomia indisponível.")));
            return true;
        }

        UUID uuid = player.getUniqueId();

        // Cooldown check
        if (plugin.getCooldownManager().isOnCooldown(uuid)) {
            long remaining = plugin.getCooldownManager().getRemainingMillis(uuid);
            String timeStr = MessageUtils.formatCooldown(remaining);
            String msg = plugin.getConfig()
                    .getString("messages.cooldown", "&cAguarde {time}.")
                    .replace("{time}", timeStr);
            player.sendMessage(MessageUtils.colorize(msg));
            return true;
        }

        // Compute progressive price using MathUtil
        double basePrice  = plugin.getConfig().getDouble("base-price", 1000.0);
        int purchases     = plugin.getPlayerDataManager().getPurchases(uuid);
        double price      = MathUtil.calculatePrice(basePrice, purchases);

        // Funds check
        if (!plugin.getEconomyManager().has(player, price)) {
            String msg = plugin.getConfig()
                    .getString("messages.no-money", "&cDinheiro insuficiente.")
                    .replace("{price}", plugin.getEconomyManager().format(price));
            player.sendMessage(MessageUtils.colorize(msg));
            return true;
        }

        // Withdraw
        plugin.getEconomyManager().withdraw(player, price);

        // Update player data
        plugin.getPlayerDataManager().incrementPurchases(uuid);
        long now = System.currentTimeMillis();
        plugin.getPlayerDataManager().setLastUse(uuid, now);
        plugin.getCooldownManager().setLastUse(uuid, now);

        // Opening message
        player.sendMessage(MessageUtils.colorize(
                plugin.getConfig().getString("messages.opening",
                        "&aBoa sorte na roleta!")));

        // Open the GUI roulette
        plugin.getGachaGUI().open(player);

        return true;
    }
}
