package br.com.beegacha.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility class for translating {@code &} colour codes into Adventure
 * {@link Component}s or legacy section-sign (§) strings.
 */
public final class ColorUtil {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private ColorUtil() {}

    /**
     * Translates {@code &} colour codes and returns an Adventure
     * {@link Component}.
     *
     * @param text raw text with {@code &} colour codes
     * @return colourised {@link Component}
     */
    public static Component colorize(String text) {
        if (text == null) return Component.empty();
        return SERIALIZER.deserialize(text);
    }

    /**
     * Translates {@code &} colour codes into section-sign (§) codes
     * and returns a plain {@link String} suitable for legacy APIs.
     *
     * @param text raw text with {@code &} colour codes
     * @return colourised plain string
     */
    public static String colorizeString(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }

    /**
     * Strips all colour codes (both {@code &x} and {@code §x} variants)
     * from the given text.
     *
     * @param text text that may contain colour codes
     * @return plain text without colour codes
     */
    public static String strip(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)[&§][0-9a-fk-or]", "");
    }
}
