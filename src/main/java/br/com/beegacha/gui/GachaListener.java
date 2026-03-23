package br.com.beegacha.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bukkit event listener for the gacha GUI.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Block all clicks and drags inside the gacha inventory.</li>
 *   <li>Detect inventory-close events and call
 *       {@link GachaSession#forceFinish()} when the player closes early, so
 *       the reward is granted exactly once.</li>
 * </ul>
 */
public class GachaListener implements Listener {

    /** Active sessions keyed by player UUID. */
    private final Map<UUID, GachaSession> sessions = new HashMap<>();

    /**
     * Registers a new session for the given player.
     * Any previous session for that player is replaced (edge case safety).
     */
    public void registerSession(Player player, GachaSession session) {
        sessions.put(player.getUniqueId(), session);
    }

    /**
     * Removes and returns the session for the given player, or {@code null}
     * if no session exists.
     */
    public GachaSession removeSession(Player player) {
        return sessions.remove(player.getUniqueId());
    }

    // -----------------------------------------------------------------------
    // Event handlers
    // -----------------------------------------------------------------------

    /**
     * Cancels all click events while a gacha inventory is open, preventing
     * item theft or GUI manipulation.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GachaSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        // Only block if the clicked inventory is the gacha GUI
        if (event.getInventory().equals(session.getInventory())) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels all drag events inside the gacha inventory.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GachaSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        if (event.getInventory().equals(session.getInventory())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the player closing the gacha inventory.
     *
     * <p>If the session is still running (not yet finished), the reward is
     * immediately granted via {@link GachaSession#forceFinish()}.
     * The session is removed from the map regardless.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        GachaSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        // Only act if this close event is for the gacha inventory
        if (!event.getInventory().equals(session.getInventory())) return;

        sessions.remove(player.getUniqueId());

        if (!session.isFinished()) {
            // Player closed early – grant reward immediately
            session.forceFinish();
        }
    }
}
