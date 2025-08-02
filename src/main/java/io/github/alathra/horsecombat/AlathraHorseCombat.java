package io.github.alathra.horsecombat;

import io.github.alathra.horsecombat.command.CommandHandler;
import io.github.alathra.horsecombat.config.ConfigHandler;
import io.github.alathra.horsecombat.config.Settings;
import io.github.alathra.horsecombat.hook.HookManager;
import io.github.alathra.horsecombat.listener.ListenerHandler;
import io.github.alathra.horsecombat.threadutil.SchedulerHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main class.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class AlathraHorseCombat extends JavaPlugin {
    private static AlathraHorseCombat instance;

    // Handlers/Managers
    private ConfigHandler configHandler;
    private HookManager hookManager;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private SchedulerHandler schedulerHandler;

    // Debug flag for more verbose logging
    private boolean debugMode = false;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    public static AlathraHorseCombat getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;

        configHandler = new ConfigHandler(this);
        hookManager = new HookManager(this);
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);
        schedulerHandler = new SchedulerHandler();

        handlers = List.of(
            configHandler,
            hookManager,
            commandHandler,
            listenerHandler,
            schedulerHandler
        );

        for (Reloadable handler : handlers)
            handler.onLoad(instance);

        // initialize Settings
        Settings.init(this);
    }

    @Override
    public void onEnable() {
        for (Reloadable handler : handlers)
            handler.onEnable(instance);
    }

    @Override
    public void onDisable() {
        for (Reloadable handler : handlers.reversed()) // If reverse doesn't work implement a new List with your desired disable order
            handler.onDisable(instance);
    }

    /**
     * Use to reload the entire plugin.
     */
    public void onReload() {
        onDisable();
        onLoad();
        onEnable();
    }

    // Method to check debug status
    public boolean isDebugEnabled(){
        return debugMode;
    }

    // Toggle debug mode
    public void toggleDebugMode() {
        debugMode = !debugMode;

        // TO DO - debugmode
        //config.set("debug", debugMode);
        //saveConfig()
    }
    

    /**
     * Gets hook manager.
     *
     * @return the hook manager
     */
    @NotNull
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }
}
