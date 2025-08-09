package io.github.alathra.horsecombat.listener;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.Reloadable;
import io.github.alathra.horsecombat.listener.combat.HorseDismountListener;
import io.github.alathra.horsecombat.listener.combat.LanceStrikeListener;
import io.github.alathra.horsecombat.listener.combat.MomentumListener;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final AlathraHorseCombat plugin;
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Instantiates the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(AlathraHorseCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathraHorseCombat plugin) {
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        listeners.clear(); // Clear the list to avoid duplicate listeners when reloading the plugin
        listeners.add(new LanceStrikeListener(plugin));
        listeners.add(new MomentumListener(plugin));
        listeners.add(new HorseDismountListener());

        // Register listeners here
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(AlathraHorseCombat plugin) {
    }
}
