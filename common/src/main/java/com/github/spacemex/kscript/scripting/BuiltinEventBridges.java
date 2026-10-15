
package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.api.PlayerJoinEvent;
import com.github.spacemex.kscript.api.PlayerQuitEvent;
import com.github.spacemex.kscript.api.ServerEvents;

import dev.architectury.event.events.common.PlayerEvent;

public final class BuiltinEventBridges {

    private static boolean initialized = false;

    private BuiltinEventBridges() {}

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        PlayerEvent.PLAYER_JOIN.register(player -> ServerEvents.PLAYER_JOIN.fire(new PlayerJoinEvent(player)));
        PlayerEvent.PLAYER_QUIT.register(player -> ServerEvents.PLAYER_QUIT.fire(new PlayerQuitEvent(player)));

        initialized = true;
    }
}
