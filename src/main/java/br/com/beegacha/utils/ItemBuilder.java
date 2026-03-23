package br.com.beegacha.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fluent builder for {@link ItemStack} objects.
 *
 * <p>Usage example:
 * <pre>{@code
 * ItemStack item = new ItemBuilder(Material.DIAMOND)
 *         .name("&bShiny Diamond")
 *         .lore("&7Worth a lot", "&7Handle with care")
 *         .amount(5)
 *         .hideFlags()
 *         .build();
 * }</pre>
 */
public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta  meta;

    /**
     * Creates a new builder for a single item of the given material.
     *
     * @param material the material of the item
     */
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    /**
     * Creates a new builder wrapping an existing {@link ItemStack}.
     * The stack is cloned to avoid mutating the original.
     *
     * @param stack the item to copy
     */
    public ItemBuilder(ItemStack stack) {
        this.item = stack.clone();
        this.meta = this.item.getItemMeta();
    }

    /**
     * Sets the display name of the item, translating {@code &} colour codes.
     *
     * @param name raw name with optional colour codes
     * @return this builder
     */
    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.displayName(ColorUtil.colorize(name));
        }
        return this;
    }

    /**
     * Sets the display name directly as an Adventure {@link Component}.
     *
     * @param component the display name component
     * @return this builder
     */
    public ItemBuilder name(Component component) {
        if (meta != null) {
            meta.displayName(component);
        }
        return this;
    }

    /**
     * Sets the lore lines, translating {@code &} colour codes in each line.
     *
     * @param lines lore lines (varargs)
     * @return this builder
     */
    public ItemBuilder lore(String... lines) {
        if (meta != null) {
            List<Component> loreComponents = Arrays.stream(lines)
                    .map(ColorUtil::colorize)
                    .collect(Collectors.toList());
            meta.lore(loreComponents);
        }
        return this;
    }

    /**
     * Sets the lore lines as Adventure {@link Component} objects.
     *
     * @param lines lore components
     * @return this builder
     */
    public ItemBuilder lore(List<Component> lines) {
        if (meta != null) {
            meta.lore(lines);
        }
        return this;
    }

    /**
     * Sets the stack size.
     *
     * @param amount quantity (1–64)
     * @return this builder
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(Math.min(64, Math.max(1, amount)));
        return this;
    }

    /**
     * Hides all {@link ItemFlag}s so that enchantments, attributes, etc. are
     * not shown in the item tooltip.
     *
     * @return this builder
     */
    public ItemBuilder hideFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    /**
     * Builds and returns the finished {@link ItemStack}.
     *
     * @return the constructed item
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
