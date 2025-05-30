package io.github.alathra.horsecombat.hook.placeholderapi;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;
import io.github.alathra.horsecombat.hook.Hook;

/**
 * A hook to interface with <a href="https://wiki.placeholderapi.com/">PlaceholderAPI</a>.
 */
public class PAPIHook extends AbstractHook {
    private PAPIExpansion PAPIExpansion;

    /**
     * Instantiates a new PlaceholderAPI hook.
     *
     * @param plugin the plugin instance
     */
    public PAPIHook(AlathraHorseCombat plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;

        PAPIExpansion = new PAPIExpansion(super.getPlugin());
        PAPIExpansion.register();
    }

    @Override
    public void onDisable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;

        PAPIExpansion.unregister();
        PAPIExpansion = null;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.PAPI.getPluginName()) && isPluginEnabled(Hook.PAPI.getPluginName());
    }
}
