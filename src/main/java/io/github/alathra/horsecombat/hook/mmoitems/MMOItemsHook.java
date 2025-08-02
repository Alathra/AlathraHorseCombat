package io.github.alathra.horsecombat.hook.mmoitems;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;
import io.github.alathra.horsecombat.hook.Hook;

public class MMOItemsHook extends AbstractHook {
    public MMOItemsHook(AlathraHorseCombat plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.MMOItems.getPluginName()) && isPluginEnabled(Hook.MMOItems.getPluginName());
    }
}
