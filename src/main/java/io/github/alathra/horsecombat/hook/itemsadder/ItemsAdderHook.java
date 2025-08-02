package io.github.alathra.horsecombat.hook.itemsadder;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;
import io.github.alathra.horsecombat.hook.Hook;

public class ItemsAdderHook extends AbstractHook {
    public ItemsAdderHook(AlathraHorseCombat plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.ItemsAdder.getPluginName()) && isPluginEnabled(Hook.ItemsAdder.getPluginName());
    }
}
