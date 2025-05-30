package io.github.alathra.horsecombat.utility;


import io.github.alathra.horsecombat.AlathraHorseCombat;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides shorthand access to {@link AlathraHorseCombat#getComponentLogger}.
 */
public class Logger {
    /**
     * Get component logger. Shorthand for:
     *
     * @return the component logger {@link AlathraHorseCombat#getComponentLogger}.
     */
    @NotNull
    public static ComponentLogger get() {
        return AlathraHorseCombat.getInstance().getComponentLogger();
    }
}
