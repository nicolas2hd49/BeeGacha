package br.com.beegacha.utils;

/**
 * Utility class for mathematical helpers used by the plugin's pricing and
 * animation systems.
 */
public final class MathUtil {

    private MathUtil() {}

    /**
     * Computes the progressive gacha price using the formula:
     * <pre>price = basePrice * (10 ^ purchases)</pre>
     *
     * @param basePrice base price for the first spin
     * @param purchases number of spins the player has already made
     * @return calculated price (always &ge; basePrice)
     */
    public static double calculatePrice(double basePrice, int purchases) {
        return basePrice * Math.pow(10, purchases);
    }

    /**
     * Performs a quadratic ease-out interpolation.
     *
     * <p>Returns a value in the range {@code [minValue, maxValue]} based on
     * {@code progress} (0.0 = start, 1.0 = end), where the output grows
     * quickly at first and decelerates toward {@code maxValue} towards the
     * end of the range.
     *
     * @param progress  normalised progress in range [0.0, 1.0]
     * @param minValue  value returned when progress is 0.0
     * @param maxValue  value returned when progress is 1.0
     * @return interpolated value
     */
    public static double easeOutQuadratic(double progress, double minValue, double maxValue) {
        double t = Math.min(1.0, Math.max(0.0, progress));
        return minValue + t * t * (maxValue - minValue);
    }

    /**
     * Clamps {@code value} between {@code min} and {@code max} (inclusive).
     *
     * @param value value to clamp
     * @param min   minimum bound
     * @param max   maximum bound
     * @return clamped value
     */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}
