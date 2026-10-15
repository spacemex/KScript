package com.github.spacemex.kscript.api;

import com.github.spacemex.kscript.scripting.ScriptEventRegistry;

import java.util.function.Consumer;

public final class ServerEvents {

    private ServerEvents() {}

    public static void playerJoin(Consumer<PlayerJoinEvent> handler) {
        ScriptEventRegistry.registerPlayerJoin(handler);
    }
}
