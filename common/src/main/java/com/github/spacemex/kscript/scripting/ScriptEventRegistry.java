package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;
import com.github.spacemex.kscript.api.PlayerJoinEvent;
import com.github.spacemex.kscript.api.PlayerQuitEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class ScriptEventRegistry {

    private static final ThreadLocal<Registrations> CURRENT = new ThreadLocal<>();

    private static volatile ActiveListeners activeListeners = ActiveListeners.EMPTY;
    private static boolean initialized = false;

    private ScriptEventRegistry() {}

    public static synchronized void initialize() {
        if (initialized){
            return;
        }

        PlayerEvent.PLAYER_JOIN.register(ScriptEventRegistry::handlePlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(ScriptEventRegistry::handlePlayerQuit);

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

    public static void registerPlayerQuit(Consumer<PlayerQuitEvent> listener) {
        Objects.requireNonNull(listener);

        Registrations registrations = CURRENT.get();

        if (registrations == null) {
            throw new IllegalStateException("Server events can only be registered " + "while loading a server script");
        }

        registrations.playerQuitHandlers.add(listener);
    }

    public static void replaceServerListeners(Map<String, Registrations> prepared) {
        List<ListenerEntry> nextJoins = new ArrayList<>();
        List<QuitListenerEntry> nextQuits = new ArrayList<>();

        for (var entry : prepared.entrySet()) {
            String scriptName = entry.getKey();
            Registrations registrations = entry.getValue();

            for (var listener : registrations.playerJoinHandlers) {
                nextJoins.add(new ListenerEntry(scriptName, listener));
            }

            for (var listener : registrations.playerQuitHandlers) {
                nextQuits.add(new QuitListenerEntry(scriptName, listener));
            }
        }

        activeListeners = new ActiveListeners(List.copyOf(nextJoins), List.copyOf(nextQuits));

        KScript.LOGGER.debug("Activated {} player-join and {} player-quit handler(s)",
                nextJoins.size(), nextQuits.size());
    }

    public static void clearServerListeners() {
        activeListeners = ActiveListeners.EMPTY;
        KScript.LOGGER.debug("Cleared server script event handlers");
    }

    private static void handlePlayerJoin(ServerPlayer player) {
        PlayerJoinEvent event = new PlayerJoinEvent(player);

        for (ListenerEntry entry : activeListeners.playerJoins()) {
            try {
                entry.listener().accept(event);
            } catch (RuntimeException | LinkageError e) {
                KScript.LOGGER.error("Player-join handler failed in script {}", entry.scriptName(), e);
            }
        }
    }

    private static void handlePlayerQuit(ServerPlayer player) {
        PlayerQuitEvent event = new PlayerQuitEvent(player);

        for (QuitListenerEntry entry : activeListeners.playerQuits()) {
            try {
                entry.listener().accept(event);
            } catch (RuntimeException | LinkageError e) {
                KScript.LOGGER.error("Player-quit handler failed in script {}", entry.scriptName(), e);
            }
        }
    }

    public static final class Registrations {

        private final String scriptName;

        private final List<Consumer<PlayerJoinEvent>> playerJoinHandlers = new ArrayList<>();
        private final List<Consumer<PlayerQuitEvent>> playerQuitHandlers = new ArrayList<>();

        private Registrations(String scriptName) {
            this.scriptName = scriptName;
        }
    }

    private record ListenerEntry(String scriptName, Consumer<PlayerJoinEvent> listener) {}
    private record QuitListenerEntry(String scriptName, Consumer<PlayerQuitEvent> listener) {}
    private record ActiveListeners(List<ListenerEntry> playerJoins, List<QuitListenerEntry> playerQuits) {
        private static final ActiveListeners EMPTY = new ActiveListeners(List.of(), List.of());
    }
}
