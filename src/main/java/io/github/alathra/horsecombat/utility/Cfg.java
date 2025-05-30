package io.github.alathra.horsecombat.utility;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.config.ConfigHandler;
import io.github.milkdrinkers.crate.Config;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link ConfigHandler#getConfig}
 */
public abstract class Cfg {
    /**
     * Convenience method for {@link ConfigHandler#getConfig} to getConnection {@link Config}
     *
     * @return the config
     */
    @NotNull
    public static Config get() {
        return AlathraHorseCombat.getInstance().getConfigHandler().getConfig();
    }
}
