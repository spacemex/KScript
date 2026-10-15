package com.github.spacemex.kscript.api;

import com.github.spacemex.kscript.api.event.ScriptEvent;
import com.github.spacemex.kscript.api.event.ScriptEventFactory;

import java.util.function.Consumer;

public final class ServerEvents {

    public static final ScriptEvent<PlayerJoinEvent> PLAYER_JOIN =
            ScriptEventFactory.server("kscript:player_join", PlayerJoinEvent.class);
    public static final ScriptEvent<PlayerQuitEvent> PLAYER_QUIT =
            ScriptEventFactory.server("kscript:player_quit", PlayerQuitEvent.class);

    private ServerEvents() {}

    public static void playerJoin(Consumer<PlayerJoinEvent> handler) {
        PLAYER_JOIN.listen(handler);
    }

    public static void playerQuit(Consumer<PlayerQuitEvent> handler) {
        PLAYER_QUIT.listen(handler);
    }
}
