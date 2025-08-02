package io.github.alathra.horsecombat.hook.nexo;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;
import io.github.alathra.horsecombat.hook.Hook;

public class NexoHook extends AbstractHook {
    public NexoHook(AlathraHorseCombat plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Nexo.getPluginName()) && isPluginEnabled(Hook.Nexo.getPluginName());
    }
}
