package io.github.alathra.horsecombat.config;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.Reloadable;
import io.github.milkdrinkers.crate.Config;
import io.github.milkdrinkers.crate.ConfigBuilder;
import io.github.milkdrinkers.crate.internal.settings.ReloadSetting;

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
        cfg = ConfigBuilder
            .fromPath("config", plugin.getDataFolder().getPath())
            .addInputStream(plugin.getResource("config.yml"))
            .setReloadSetting(ReloadSetting.MANUALLY)
            .create();
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
    }

    @Override
    public void onDisable(AlathraHorseCombat plugin) {
    }

    public void reloadConfig() {
        cfg.forceReload();
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
