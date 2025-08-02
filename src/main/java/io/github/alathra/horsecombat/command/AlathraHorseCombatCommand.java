package io.github.alathra.horsecombat.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.utility.Permissions;
import io.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.command.CommandSender;

public class AlathraHorseCombatCommand {
    protected AlathraHorseCombatCommand() {
        new CommandAPICommand("alathrahorsecombat")
            .withFullDescription("AlathraHorseCombat commands.")
            .withShortDescription("AlathraHorseCombat commands.")
            .withPermission(Permissions.getAdminPermissionNode())
            .withSubcommands(
                reloadCommand()
            )
            .executes(this::helpMenu)
            .register();
    }

    private void helpMenu(CommandSender sender, CommandArguments args) {
        sender.sendMessage(ColorParser.of("<yellow>SimpleLockpicking Commands:").build());
        sender.sendMessage(ColorParser.of("<yellow>/alathrahorsecombat reload <green>reloads config settings").build());
    }

    public CommandAPICommand reloadCommand() {
        return new CommandAPICommand("reload")
            .withPermission(Permissions.getAdminPermissionNode())
            .executes((CommandSender sender, CommandArguments args) -> {
                AlathraHorseCombat.getInstance().getConfigHandler().reloadConfig();
                sender.sendMessage(ColorParser.of("<yellow>Config settings reloaded").build());
            });
    }
}
