package br.com.beegacha.manager;

import br.com.beegacha.utils.TimeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player cooldowns in memory.
 * Cooldown state is derived from the last-use timestamp stored in
 * {@link PlayerDataManager}, so this class delegates persistence there.
 * Time helpers are provided by {@link TimeUtil}.
 */
public class CooldownManager {

    /** Cooldown duration in milliseconds (default 2 hours). */
    private final long cooldownMillis;

    /** In-memory cache: playerUUID -> last use timestamp (ms). */
    private final Map<UUID, Long> lastUseMap = new HashMap<>();

    public CooldownManager(long cooldownHours) {
        this.cooldownMillis = TimeUtil.hoursToMillis(cooldownHours);
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
        return TimeUtil.isOnCooldown(getLastUse(uuid), cooldownMillis);
    }

    /**
     * Returns the remaining cooldown time in milliseconds for the player.
     * Returns {@code 0} if the player is not on cooldown.
     */
    public long getRemainingMillis(UUID uuid) {
        return TimeUtil.remainingMillis(getLastUse(uuid), cooldownMillis);
    }
}
