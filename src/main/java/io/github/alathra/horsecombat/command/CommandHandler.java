package io.github.alathra.horsecombat.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.Reloadable;

/**
 * A class to handle registration of commands.
 */
public class CommandHandler implements Reloadable {
    private final AlathraHorseCombat plugin;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(AlathraHorseCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathraHorseCombat plugin) {
        CommandAPI.onLoad(
            new CommandAPIBukkitConfig(plugin)
                .shouldHookPaperReload(true)
                .silentLogs(true)
                .usePluginNamespace()
                .beLenientForMinorVersions(true)
        );
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        CommandAPI.onEnable();

        // Register commands here
        new ExampleCommand();
    }

    @Override
    public void onDisable(AlathraHorseCombat plugin) {
        CommandAPI.getRegisteredCommands().forEach(registeredCommand -> CommandAPI.unregister(registeredCommand.namespace() + ':' + registeredCommand.commandName(), true));
        CommandAPI.onDisable();
    }
}