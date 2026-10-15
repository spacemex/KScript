package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;
import com.github.spacemex.kscript.api.PlayerJoinEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class ScriptEventRegistry {

    private static final ThreadLocal<Registrations> CURRENT = new ThreadLocal<>();

    private static volatile List<ListenerEntry> activePlayerJoins = List.of();

    private static boolean initialized = false;

    private ScriptEventRegistry() {}

    public static synchronized void initialize() {
        if (initialized){
            return;
        }

        PlayerEvent.PLAYER_JOIN.register(ScriptEventRegistry::handlePlayerJoin);

        initialized = true;
    }

    public static void beginScript(String scriptName) {
        if (CURRENT.get() != null) {
            throw new IllegalStateException("A script registration is already active");
        }

        CURRENT.set(new Registrations(scriptName));
    }

    public static Registrations completeScript() {
        Registrations registrations = CURRENT.get();

        if (registrations == null) {
            throw new IllegalStateException("No active script registration");
        }

        CURRENT.remove();
        return registrations;
    }

    public static void discardScript() {
        CURRENT.remove();
    }

    public static void registerPlayerJoin(Consumer<PlayerJoinEvent> listener) {
        Objects.requireNonNull(listener);

        Registrations registrations = CURRENT.get();

        if (registrations == null) {
            throw new IllegalStateException("Server events can only be registered while loading a server script");
        }

        registrations.playerJoinHandlers.add(listener);
    }

    public static void replaceServerListeners(Map<String, Registrations> prepared) {
        List<ListenerEntry> next = new ArrayList<>();

        for (var entry : prepared.entrySet()) {
            for (var listener : entry.getValue().playerJoinHandlers) {
                next.add(new ListenerEntry(entry.getKey(), listener));
            }
        }

        activePlayerJoins = List.copyOf(next);

        KScript.LOGGER.debug("Activated {} player-join script handler(s)", next.size());
    }

    public static void clearServerListeners() {
        activePlayerJoins = List.of();

        KScript.LOGGER.debug("Cleared server script event handlers");
    }

    public static void handlePlayerJoin(ServerPlayer player) {
        PlayerJoinEvent event = new PlayerJoinEvent(player);

        for (ListenerEntry entry : activePlayerJoins) {
            try {
                entry.listener.accept(event);
            } catch (RuntimeException | LinkageError e) {
                KScript.LOGGER.error("Player-Join handler failed in script {}", entry.scriptName(), e);
            }
        }
    }

    public static final class Registrations {

        private final String scriptName;

        private final List<Consumer<PlayerJoinEvent>> playerJoinHandlers = new ArrayList<>();

        private Registrations(String scriptName) {
            this.scriptName = scriptName;
        }
    }

    private record ListenerEntry(String scriptName, Consumer<PlayerJoinEvent> listener) {}
}
