package io.github.alathra.horsecombat.utility.itemutil;

import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import io.github.alathra.horsecombat.config.Settings;
import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An {@link ItemProvider} contains logic for accessing
 * and using item api from multiple plugins and vanilla.
 */
public enum ItemProvider {
    ORAXEN("Oraxen", List.of("oraxen")),
    NEXO("Nexo", List.of("nexo", "oraxen")),
    ITEMSADDER("ItemsAdder", List.of("itemsadder")),
    MMOITEMS("MMOItems", List.of("mmoitems")),
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
            case VANILLA -> Settings.getVanillaLanceMap().get(cleanItemId);
            case ORAXEN -> OraxenItems.getItemById(cleanItemId).build();
            case NEXO -> Objects.requireNonNull(NexoItems.itemFromId(cleanItemId)).build();
            case ITEMSADDER -> CustomStack.getInstance(cleanItemId).getItemStack();
            case MMOITEMS -> {
                if (cleanItemId == null || !cleanItemId.contains(".")) yield null;
                String[] parts = cleanItemId.split("\\.", 2);
                yield MMOItems.plugin.getItem(parts[0], parts[1]);
            }
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
                for (Map.Entry<String, ItemStack> vanillaItemEntry : Settings.getVanillaLanceMap().entrySet()) {
                    ItemStack lanceItem = vanillaItemEntry.getValue();
                    if (lanceItem.getType() == item.getType()) {
                        if (item.hasItemMeta() &&
                            item.getItemMeta().getCustomModelDataComponent().equals(lanceItem.getItemMeta().getCustomModelDataComponent())
                        ) yield vanillaItemEntry.getKey();
                    }
                }
                yield null;
            }
            case ORAXEN -> OraxenItems.getIdByItem(item);
            case NEXO -> NexoItems.idFromItem(item);
            case ITEMSADDER -> {
                CustomStack customStack = CustomStack.byItemStack(item);
                yield customStack != null ? customStack.getNamespacedID() : null;
            }
            case MMOITEMS -> {
                String type = String.valueOf(MMOItems.getType(item));
                String id = MMOItems.getID(item);
                yield type != null && id != null ? type + id : null;
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
                return Settings.getVanillaLanceMap().containsKey(itemId);
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

    public List<ItemStack> getAllItems() {
        switch (this) {
            case VANILLA -> {
                return Settings.getVanillaLanceMap().values().stream().toList();
            }
            case ORAXEN, NEXO, ITEMSADDER -> {
                List<ItemStack> lances = new ArrayList<>();
                for (String itemId : Settings.getLanceIDList()) {
                    ItemStack item = parseItem(itemId);
                    if (item != null) lances.add(item);
                }
                return lances;
            }
        }
        return List.of();
    }
}
