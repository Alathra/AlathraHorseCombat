package io.github.alathra.horsecombat.translation;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.Reloadable;
import io.github.alathra.horsecombat.config.ConfigHandler;
import io.github.milkdrinkers.colorparser.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;

import java.nio.file.Path;

/**
 * A wrapper handler class for handling WordWeaver lifecycle.
 */
public class TranslationHandler implements Reloadable {
    private final ConfigHandler configHandler;

    public TranslationHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    @Override
    public void onLoad(AlathraHorseCombat plugin) {

    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {
        Translation.initialize(TranslationConfig.builder() // Initialize word-weaver
            .translationDirectory(plugin.getDataPath().resolve("lang"))
            .resourcesDirectory(Path.of("lang"))
            .extractLanguages(true)
            .updateLanguages(true)
            .language(configHandler.getConfig().get("language", "en_US"))
            .defaultLanguage("en_US")
            .componentConverter(s -> ColorParser.of(s).parseLegacy().build()) // Use color parser for components by default
            .build()
        );
    }

    @Override
    public void onDisable(AlathraHorseCombat plugin) {
    }
}
