package io.github.alathra.horsecombat.threadutil;

import io.github.alathra.horsecombat.AlathraHorseCombat;
import io.github.alathra.horsecombat.Reloadable;
import io.github.milkdrinkers.threadutil.PlatformBukkit;
import io.github.milkdrinkers.threadutil.Scheduler;

import java.time.Duration;

/**
 * A wrapper handler class for handling thread-util lifecycle.
 */
public class SchedulerHandler implements Reloadable {
    @Override
    public void onLoad(AlathraHorseCombat plugin) {
        Scheduler.init(new PlatformBukkit(plugin)); // Initialize thread-util
    }

    @Override
    public void onEnable(AlathraHorseCombat plugin) {

    }

    @Override
    public void onDisable(AlathraHorseCombat plugin) {
        Scheduler.shutdown(Duration.ofSeconds(60));
    }
}
