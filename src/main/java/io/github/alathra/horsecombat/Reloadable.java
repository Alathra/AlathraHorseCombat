package io.github.alathra.horsecombat;

/**
 * Implemented in classes that should support being reloaded IE executing the methods during runtime after startup.
 */
public interface Reloadable {
    /**
     * On plugin load.
     */
    void onLoad(AlathraHorseCombat plugin);

    /**
     * On plugin enable.
     */
    void onEnable(AlathraHorseCombat plugin);

    /**
     * On plugin disable.
     */
    void onDisable(AlathraHorseCombat plugin);
}
