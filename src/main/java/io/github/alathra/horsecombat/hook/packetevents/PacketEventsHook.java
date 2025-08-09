package io.github.alathra.horsecombat.hook.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.hook.AbstractHook;
import io.github.alathra.horsecombat.hook.Hook;
import io.github.alathra.horsecombat.packets.CombatPacketListener;

public class PacketEventsHook extends AbstractHook {
    public PacketEventsHook(AlathraHorseCombat plugin) {
        super(plugin);
    }

    /**
     * On plugin load.
     */
    @Override
    public void onLoad(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;
    }

    /**
     * On plugin enable.
     */
    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;
        PacketEvents.getAPI().getEventManager().registerListener(new CombatPacketListener(), PacketListenerPriority.NORMAL);
    }

    /**
     * On plugin disable.
     */
    @Override
    public void onDisable(AlathraHorseCombat plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.PacketEvents.getPluginName());
    }
}
