package io.github.alathra.horsecombat.hook.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;

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

    public TownyAPI getTownyAPI() {
        return townyAPI;
    }
}
