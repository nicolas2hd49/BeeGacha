package br.com.beegacha.model;

import java.util.UUID;

/**
 * Represents the persistent data stored for a single player.
 *
 * <p>Instances are created by {@link br.com.beegacha.manager.PlayerDataManager}
 * when loading from {@code playerdata.yml} or when a new player interacts with
 * the gacha for the first time.
 */
public class PlayerData {

    private final UUID uuid;
    private int  purchases;
    private long lastUse;

    /**
     * Creates a PlayerData record for the given player.
     *
     * @param uuid      the player's unique identifier
     * @param purchases number of gacha purchases already made (default 0)
     * @param lastUse   Unix timestamp (ms) of the last gacha use, or 0 if never
     */
    public PlayerData(UUID uuid, int purchases, long lastUse) {
        this.uuid      = uuid;
        this.purchases = purchases;
        this.lastUse   = lastUse;
    }

    /**
     * Convenience constructor that creates a fresh record with zero purchases
     * and no last-use timestamp.
     *
     * @param uuid the player's unique identifier
     */
    public PlayerData(UUID uuid) {
        this(uuid, 0, 0L);
    }

    // -----------------------------------------------------------------------
    // Getters / mutators
    // -----------------------------------------------------------------------

    /** Returns the player's UUID. */
    public UUID getUuid() {
        return uuid;
    }

    /** Returns how many gacha purchases the player has made. */
    public int getPurchases() {
        return purchases;
    }

    /** Sets the purchase count directly (use {@link #incrementPurchases()} for incrementing). */
    public void setPurchases(int purchases) {
        this.purchases = purchases;
    }

    /** Increments the purchase counter by one and returns the new value. */
    public int incrementPurchases() {
        return ++purchases;
    }

    /**
     * Returns the Unix timestamp (ms) of the player's last gacha use,
     * or {@code 0} if the player has never used the gacha.
     */
    public long getLastUse() {
        return lastUse;
    }

    /** Updates the last-use timestamp. */
    public void setLastUse(long lastUse) {
        this.lastUse = lastUse;
    }

    @Override
    public String toString() {
        return "PlayerData{uuid=" + uuid + ", purchases=" + purchases + ", lastUse=" + lastUse + '}';
    }
}
