package br.com.beegacha.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player cooldowns in memory.
 * Cooldown state is derived from the last-use timestamp stored in
 * {@link PlayerDataManager}, so this class delegates persistence there.
 */
public class CooldownManager {

    /** Cooldown duration in milliseconds (default 2 hours). */
    private final long cooldownMillis;

    /** In-memory cache: playerUUID -> last use timestamp (ms). */
    private final Map<UUID, Long> lastUseMap = new HashMap<>();

    public CooldownManager(long cooldownHours) {
        this.cooldownMillis = cooldownHours * 3600_000L;
    }

    /**
     * Records the current time as the last use for the given player.
     */
    public void setLastUse(UUID uuid, long timestamp) {
        lastUseMap.put(uuid, timestamp);
    }

    /**
     * Returns the timestamp of the player's last use, or {@code 0} if never used.
     */
    public long getLastUse(UUID uuid) {
        return lastUseMap.getOrDefault(uuid, 0L);
    }

    /**
     * Returns whether the player is currently in cooldown.
     */
    public boolean isOnCooldown(UUID uuid) {
        long lastUse = getLastUse(uuid);
        return lastUse > 0 && (System.currentTimeMillis() - lastUse) < cooldownMillis;
    }

    /**
     * Returns the remaining cooldown time in milliseconds for the player.
     * Returns {@code 0} if the player is not on cooldown.
     */
    public long getRemainingMillis(UUID uuid) {
        if (!isOnCooldown(uuid)) return 0;
        long elapsed = System.currentTimeMillis() - getLastUse(uuid);
        return cooldownMillis - elapsed;
    }
}
