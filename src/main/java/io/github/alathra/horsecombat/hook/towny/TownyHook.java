package io.github.alathra.horsecombat.hook.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;
import io.github.alathra.horsecombat.hook.Hook;

public class TownyHook extends AbstractHook {
    private TownyAPI townyAPI;

    /**
     * Instantiates a new Towny hook.
     *
     * @param plugin the plugin instance
     */
    public TownyHook(AlathraHorseCombat plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;

        townyAPI = TownyAPI.getInstance();
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Towny.getPluginName()) && isPluginEnabled(Hook.Towny.getPluginName());
    }

    public TownyAPI getTownyAPI() {
        return townyAPI;
    }
}
