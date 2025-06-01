package io.github.alathra.horsecombat.utility.itemutil;

import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * An {@link ItemProvider} contains logic for accessing
 * and using item api from multiple plugins and vanilla.
 */
public enum ItemProvider {
    ORAXEN("Oraxen", List.of("oraxen")),
    NEXO("Nexo", List.of("nexo", "oraxen")),
    ITEMSADDER("ItemsAdder", List.of("itemsadder")),
    VANILLA(List.of("minecraft")); // Order matters here! The order is important for iterating the enum.

    private final String pluginName;
    private final List<String> namespaces;

    ItemProvider(final List<String> namespaces) {
        this.pluginName = "";
        this.namespaces = namespaces;
    }

    ItemProvider(final String name, final List<String> namespaces) {
        this.pluginName = name;
        this.namespaces = namespaces;
    }

    /**
     * Returns the Plugin Name for this {@link ItemProvider}. This is empty on the {@link ItemProvider#VANILLA} provider.
     *
     * @return name
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Get all valid namespaces for this {@link ItemProvider}.
     *
     * @return list of namespaces
     */
    public List<String> getNamespaces() {
        return namespaces;
    }

    /**
     * Check if this {@link ItemProvider} is available/loaded.
     *
     * @return boolean
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLoaded() {
        return isVanilla() || Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    /**
     * Check if the item id contains a namespace for this {@link ItemProvider}.
     *
     * @param itemId the item id
     * @return boolean
     */
    public boolean isUsingNamespace(final String itemId) {
        if (!isLoaded())
            return false;

        // A item with no prefix is using vanilla namespace
        if (isVanilla())
            return true;

        // Special check for Vanilla (items without a namespace)
        return getNamespaces()
            .stream()
            .anyMatch(namespace -> itemId.startsWith("%s:".formatted(namespace)));
    }

    /**
     * Strip the namespace out of the item id.
     *
     * @param itemId the item id
     * @return stripped item id
     */
    public String stripNamespace(final String itemId) {
        String tempItemId = itemId;
        for (final String namespace : getNamespaces()) {
            tempItemId = tempItemId.replace("%s:".formatted(namespace), "");
        }
        return tempItemId;
    }

    /**
     * Parse this item id into an item stack.
     *
     * @param itemId the item id
     * @return item stack or null
     */
    public @Nullable ItemStack parseItem(final String itemId) {
        if (!isLoaded())
            return null;

        final String cleanItemId = stripNamespace(itemId);

        if (!isValidItem(itemId))
            return null;

        return switch (this) {
            case VANILLA -> {
                try {
                    final @Nullable Material material = Material.matchMaterial(cleanItemId);
                    if (material == null)
                        yield null;

                    yield ItemStack.of(material, 1);
                } catch (IllegalArgumentException _ignored) {
                    yield null;
                }
            }
            case ORAXEN -> OraxenItems.getItemById(cleanItemId).build();
            case NEXO -> Objects.requireNonNull(NexoItems.itemFromId(cleanItemId)).build();
            case ITEMSADDER -> CustomStack.getInstance(cleanItemId).getItemStack();
        };
    }

    /**
     * Parse this ItemStack into a String itemId
     *
     * @param item the item
     * @return String itemId or null
     */
    public @Nullable String parseItemID(final ItemStack item) {
        if (!isLoaded())
            return null;

        return switch (this) {
            case VANILLA -> {
                NamespacedKey key = Registry.MATERIAL.getKey(item.getType());

                if (key != null) {
                    yield key.asString();
                }

                yield null;
            }
            case ORAXEN -> OraxenItems.getIdByItem(item);
            case NEXO -> Objects.requireNonNull(NexoItems.idFromItem(item));
            case ITEMSADDER -> {
                CustomStack customStack = CustomStack.byItemStack(item);
                yield customStack != null ? customStack.getNamespacedID() : null;
            }
        };
    }

    /**
     * Check whether this item id maps to an existing item.
     *
     * @param itemId the item id
     * @return boolean
     */
    public boolean isValidItem(final String itemId) {
        if (!isLoaded())
            return false;

        final String cleanItemId = stripNamespace(itemId);

        switch (this) {
            case VANILLA -> {
                return Material.matchMaterial(cleanItemId) != null;
            }
            case ORAXEN -> {
                return OraxenItems.exists(cleanItemId);
            }
            case NEXO -> {
                return NexoItems.exists(cleanItemId);
            }
            case ITEMSADDER -> {
                return CustomStack.getInstance(cleanItemId) != null;
            }
        }

        return false;
    }

    private boolean isVanilla() {
        return this == VANILLA;
    }
}
