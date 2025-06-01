package io.github.alathra.horsecombat.utility.itemutil;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The {@link ItemUtils} class provides utils for
 * provisioning {@literal &} interacting with item stack
 * from other plugins {@literal &} vanilla.
 */
public abstract class ItemUtils {
    private static final List<ItemProvider> providers = List.of(ItemProvider.values());

    /**
     * Gets a cached list of all {@link ItemProvider}'s.
     *
     * @return item providers
     */
    public static List<ItemProvider> getProviders() {
        return providers;
    }

    /**
     * Gets the item provider from item namespace or fallbacks to {@link ItemProvider#VANILLA}.
     *
     * @param itemId the item id
     * @return an item provider
     */
    public static ItemProvider getProvider(final String itemId) {
        final Optional<ItemProvider> itemProvider = getProviders().stream()
            .filter(provider -> provider.isUsingNamespace(itemId))
            .findFirst();

        return itemProvider.orElse(ItemProvider.VANILLA);
    }

    /**
     * Creates an item stack from the item id, if the item id
     * returns true for {@link #exists(String)}.
     *
     * @param itemId the item id
     * @return item stack or null
     * @apiNote returns {@link ItemProvider#parseItem(String)}
     */
    public static @Nullable ItemStack parse(final String itemId) {
        final ItemProvider provider = getProvider(itemId);
        return provider.parseItem(itemId);
    }

    /**
     * Check if an item/material exists with this item id.
     *
     * @param itemId the item id
     * @return boolean
     * @apiNote returns {@link ItemProvider#isValidItem(String)}
     */
    public static boolean exists(final String itemId) {
        final ItemProvider provider = getProvider(itemId);
        return provider.isValidItem(itemId);
    }

    /**
     * Strip the namespace out of the item id.
     *
     * @param itemId the item id
     * @return stripped item id
     */
    public String stripNamespace(final String itemId) {
        final ItemProvider provider = getProvider(itemId);
        return provider.stripNamespace(itemId);
    }
}
