package io.github.alathra.horsecombat.hook.oraxen;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;
import io.github.alathra.horsecombat.hook.Hook;

public class OraxenHook extends AbstractHook {
    public OraxenHook(AlathraHorseCombat plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Oraxen.getPluginName()) && isPluginEnabled(Hook.Oraxen.getPluginName());
    }
}
