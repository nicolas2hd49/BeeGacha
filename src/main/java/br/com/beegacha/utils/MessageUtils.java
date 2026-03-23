package br.com.beegacha.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility methods for translating legacy colour codes and formatting messages.
 */
public final class MessageUtils {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private MessageUtils() {}

    /**
     * Translates {@code &} colour codes in the given string and returns an
     * Adventure {@link Component}.
     */
    public static Component colorize(String text) {
        return SERIALIZER.deserialize(text);
    }

    /**
     * Translates {@code &} colour codes and returns a plain {@link String}
     * with section-sign (§) colour codes suitable for legacy APIs.
     */
    public static String colorizeString(String text) {
        return text.replace("&", "§");
    }

    /**
     * Formats a remaining-time duration (in milliseconds) into the Portuguese
     * string "X horas, X minutos e X segundos".
     */
    public static String formatCooldown(long millis) {
        long seconds = millis / 1000;
        long hours   = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs    = seconds % 60;
        return hours + " horas, " + minutes + " minutos e " + secs + " segundos";
    }
}
