package io.github.alathra.horsecombat.config;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.Reloadable;
import io.github.milkdrinkers.crate.Config;

/**
 * A class that generates/loads {@literal &} provides access to a configuration file.
 */
public class ConfigHandler implements Reloadable {
    private final AlathraHorseCombat plugin;
    private Config cfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(AlathraHorseCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathraHorseCombat plugin) {
        cfg = new Config("config", plugin.getDataFolder().getPath(), plugin.getResource("config.yml")); // Create a config file from the template in our resources folder
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
    }

    @Override
    public void onDisable(AlathraHorseCombat plugin) {
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public Config getConfig() {
        return cfg;
    }
}
