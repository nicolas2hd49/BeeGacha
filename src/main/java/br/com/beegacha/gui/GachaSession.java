package br.com.beegacha.gui;

import br.com.beegacha.BeeGachaPlugin;
import br.com.beegacha.model.Reward;
import br.com.beegacha.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages a single gacha roulette session for one player.
 *
 * <p>The middle row (slots 9–17) is used as the reel. Items scroll from right
 * to left each tick; the spin starts fast and gradually slows down (ease-out).
 * When the spin ends, the winning reward lands in the result slot (slot 13).
 *
 * <p>If the player closes the inventory early, the session detects this via
 * {@link GachaListener} and calls {@link #forceFinish()} so the reward is
 * granted exactly once.
 */
public class GachaSession {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    private static final int REEL_START  = 9;
    private static final int REEL_END    = 17;
    private static final int RESULT_SLOT = 13;
    private static final int REEL_SIZE   = REEL_END - REEL_START + 1; // 9 slots

    /** Glass-pane used for decorative border slots. */
    private static final Material BORDER_MATERIAL = Material.GRAY_STAINED_GLASS_PANE;
    /** Glass-pane used to highlight the result slot. */
    private static final Material HIGHLIGHT_MATERIAL = Material.LIME_STAINED_GLASS_PANE;

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final BeeGachaPlugin plugin;
    private final Player player;
    private final Reward winningReward;
    private final Inventory inventory;

    /** Scrolling buffer of reward items shown in the reel. */
    private final List<ItemStack> reelBuffer = new ArrayList<>();

    private BukkitRunnable spinTask;

    /** True once the reward has been granted (prevents double-granting). */
    private volatile boolean finished = false;

    /** Total ticks of animation. Random between 3–5 seconds (20 tps). */
    private final int totalTicks;

    /** Current tick counter. */
    private int currentTick = 0;

    private final Random random = new Random();

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public GachaSession(BeeGachaPlugin plugin, Player player, Reward winningReward) {
        this.plugin        = plugin;
        this.player        = player;
        this.winningReward = winningReward;

        // Random duration 3–5 seconds at 20 tps = 60–100 ticks
        this.totalTicks = 60 + random.nextInt(41);

        // Create the inventory with a title
        int rows = plugin.getConfig().getInt("gui-rows", 3);
        this.inventory = Bukkit.createInventory(null, rows * 9,
                MessageUtils.colorize("&6&lBeeGacha &e🐝"));

        initInventory();
        fillReelBuffer();
    }

    // -----------------------------------------------------------------------
    // Inventory setup
    // -----------------------------------------------------------------------

    /**
     * Fills every non-reel slot with decorative glass panes and highlights the
     * result slot with a green pane.
     */
    private void initInventory() {
        ItemStack border    = makeGlass(BORDER_MATERIAL, " ");
        ItemStack highlight = makeGlass(HIGHLIGHT_MATERIAL, "&a► Resultado ◄");

        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (i >= REEL_START && i <= REEL_END) continue; // reel slots handled separately
            inventory.setItem(i, border);
        }

        // Highlight columns around the result slot
        inventory.setItem(RESULT_SLOT - 9, highlight); // top
        inventory.setItem(RESULT_SLOT + 9, highlight); // bottom
    }

    /** Creates a named, display-only glass-pane item. */
    private ItemStack makeGlass(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    // -----------------------------------------------------------------------
    // Reel buffer
    // -----------------------------------------------------------------------

    /**
     * Populates the scrolling buffer with a mix of random rewards followed by
     * the winning reward in the correct final position so it lands on
     * {@link #RESULT_SLOT} when the spin ends.
     */
    private void fillReelBuffer() {
        List<Reward> allRewards = plugin.getRewardManager().getRewards();
        if (allRewards.isEmpty()) return;

        // Fill with random items for the spinning phase
        int spinItems = totalTicks + REEL_SIZE + 10;
        for (int i = 0; i < spinItems; i++) {
            Reward r = allRewards.get(random.nextInt(allRewards.size()));
            reelBuffer.add(makeRewardItem(r));
        }

        // Override the item that will be centred at the end to be the winner
        // The result slot offset within the reel: RESULT_SLOT - REEL_START = 4
        int resultOffset = RESULT_SLOT - REEL_START;
        int finalIndex   = spinItems - REEL_SIZE + resultOffset;
        if (finalIndex >= 0 && finalIndex < reelBuffer.size()) {
            reelBuffer.set(finalIndex, makeRewardItem(winningReward));
        }
    }

    /** Builds an {@link ItemStack} that represents a reward in the reel. */
    private ItemStack makeRewardItem(Reward reward) {
        ItemStack item = new ItemStack(reward.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.colorize(reward.getDisplayName()));
            item.setItemMeta(meta);
        }
        return item;
    }

    // -----------------------------------------------------------------------
    // Spin logic
    // -----------------------------------------------------------------------

    /** Opens the inventory for the player and starts the animation task. */
    public void start() {
        player.openInventory(inventory);
        scheduleNextStep();
    }

    /**
     * Schedules the next animation step after the appropriate delay.
     * Each step renders the reel once and then self-reschedules, creating an
     * ease-out effect: delay grows from 1 tick (fast) to 6 ticks (slow).
     */
    private void scheduleNextStep() {
        if (finished) return;

        long delay = computeDelay();

        spinTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (finished) return;

                if (currentTick >= totalTicks) {
                    // Animation complete – grant reward
                    grantReward();
                    return;
                }

                renderReel();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                currentTick++;

                // Schedule the next step with the updated delay
                scheduleNextStep();
            }
        };

        spinTask.runTaskLater(plugin, delay);
    }

    /**
     * Computes how many ticks to wait before the next reel update.
     * Uses a quadratic ease-out: starts at 1 tick (fast), ramps to 6 ticks (slow).
     */
    private long computeDelay() {
        double progress = (double) currentTick / totalTicks; // 0.0 → 1.0
        // Quadratic ease-out: 1 tick at start, 6 ticks at end
        long delay = 1 + Math.round(progress * progress * 5);
        return Math.max(1, delay);
    }

    /**
     * Updates the reel slots to show the next window of the reel buffer,
     * creating the illusion of items scrolling left.
     */
    private void renderReel() {
        int offset = Math.min(currentTick, reelBuffer.size() - REEL_SIZE);
        for (int i = 0; i < REEL_SIZE; i++) {
            int bufIndex = offset + i;
            ItemStack item = (bufIndex < reelBuffer.size())
                    ? reelBuffer.get(bufIndex)
                    : new ItemStack(Material.AIR);
            inventory.setItem(REEL_START + i, item);
        }
    }

    // -----------------------------------------------------------------------
    // Reward granting
    // -----------------------------------------------------------------------

    /**
     * Grants the winning reward to the player exactly once and closes the GUI.
     * Safe to call from both normal completion and forced finish (inventory close).
     */
    public synchronized void grantReward() {
        if (finished) return;
        finished = true;

        // Cancel the animation task if still running
        if (spinTask != null && !spinTask.isCancelled()) {
            spinTask.cancel();
        }

        // Execute the reward command on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            // Close the inventory (no-op if already closed)
            player.closeInventory();

            String cmd = winningReward.getCommand()
                    .replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);

            // Send win message
            String wonMsg = plugin.getConfig().getString("messages.won",
                    "&6Você ganhou: &e{reward}!")
                    .replace("{reward}", MessageUtils.colorizeString(winningReward.getDisplayName()));
            player.sendMessage(MessageUtils.colorize(wonMsg));

            // Play win sound
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        });
    }

    /**
     * Called by {@link GachaListener} when the player closes the inventory
     * while the spin is still running. Cancels the task and immediately grants
     * the pre-determined reward.
     */
    public void forceFinish() {
        grantReward();
    }

    /** Returns {@code true} if the session has already finished. */
    public boolean isFinished() {
        return finished;
    }

    /** Returns the inventory associated with this session. */
    public Inventory getInventory() {
        return inventory;
    }
}
