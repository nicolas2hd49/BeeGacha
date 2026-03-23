package br.com.beegacha.utils;

/**
 * Utility class for time-related formatting used throughout the plugin.
 */
public final class TimeUtil {

    private TimeUtil() {}

    /**
     * Formats a duration in milliseconds as a human-readable Portuguese
     * string of the form {@code "X horas, X minutos e X segundos"}.
     *
     * @param millis duration in milliseconds (must be &ge; 0)
     * @return formatted time string
     */
    public static String formatCooldown(long millis) {
        long seconds = Math.max(0, millis) / 1000;
        long hours   = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs    = seconds % 60;
        return hours + " horas, " + minutes + " minutos e " + secs + " segundos";
    }

    /**
     * Converts hours to milliseconds.
     *
     * @param hours number of hours
     * @return equivalent duration in milliseconds
     */
    public static long hoursToMillis(long hours) {
        return hours * 3_600_000L;
    }

    /**
     * Returns whether the elapsed time since {@code lastUse} (in milliseconds)
     * exceeds the given {@code cooldownMillis}.
     *
     * @param lastUse        timestamp of last use (ms since epoch), or 0 if never
     * @param cooldownMillis cooldown window in milliseconds
     * @return {@code true} if the player is still on cooldown
     */
    public static boolean isOnCooldown(long lastUse, long cooldownMillis) {
        return lastUse > 0 && (System.currentTimeMillis() - lastUse) < cooldownMillis;
    }

    /**
     * Returns the remaining cooldown time in milliseconds, or {@code 0} if
     * not on cooldown.
     *
     * @param lastUse        timestamp of last use (ms since epoch), or 0 if never
     * @param cooldownMillis cooldown window in milliseconds
     * @return remaining milliseconds, never negative
     */
    public static long remainingMillis(long lastUse, long cooldownMillis) {
        if (!isOnCooldown(lastUse, cooldownMillis)) return 0L;
        long elapsed = System.currentTimeMillis() - lastUse;
        return Math.max(0L, cooldownMillis - elapsed);
    }
}
