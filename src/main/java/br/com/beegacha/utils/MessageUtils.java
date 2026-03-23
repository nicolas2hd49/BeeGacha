package br.com.beegacha.utils;

import net.kyori.adventure.text.Component;

/**
 * Convenience façade that delegates to {@link ColorUtil} and {@link TimeUtil}.
 *
 * <p>Kept for backwards compatibility with existing code that calls
 * {@code MessageUtils.colorize()} or {@code MessageUtils.formatCooldown()}.
 * New code should prefer the dedicated utility classes directly.
 */
public final class MessageUtils {

    private MessageUtils() {}

    /**
     * Translates {@code &} colour codes and returns an Adventure {@link Component}.
     *
     * @see ColorUtil#colorize(String)
     */
    public static Component colorize(String text) {
        return ColorUtil.colorize(text);
    }

    /**
     * Translates {@code &} colour codes and returns a plain {@link String}
     * with section-sign (§) colour codes.
     *
     * @see ColorUtil#colorizeString(String)
     */
    public static String colorizeString(String text) {
        return ColorUtil.colorizeString(text);
    }

    /**
     * Formats a remaining-time duration (in milliseconds) as
     * "X horas, X minutos e X segundos".
     *
     * @see TimeUtil#formatCooldown(long)
     */
    public static String formatCooldown(long millis) {
        return TimeUtil.formatCooldown(millis);
    }
}
