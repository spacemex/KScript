package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;
import com.github.spacemex.kscript.api.event.ScriptEvent;

import java.util.*;
import java.util.function.Consumer;

public final class ScriptEventRegistry {

    private static final ThreadLocal<Registrations> CURRENT = new ThreadLocal<>();

    private static volatile Map<ScriptEvent<?>, List<ListenerEntry>> activeServerListeners = Map.of();
    private ScriptEventRegistry() {}

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

    public static void replaceServerListeners(Map<String, Registrations> prepared) {
        Map<ScriptEvent<?>, List<ListenerEntry>> next = new LinkedHashMap<>();

        for (var scriptEntry : prepared.entrySet()) {
            String scriptName = scriptEntry.getKey();
            Registrations registrations = scriptEntry.getValue();

            for (var eventEntry : registrations.eventHandlers.entrySet()) {
                ScriptEvent<?> event = eventEntry.getKey();

                List<ListenerEntry> listeners = next.computeIfAbsent(event, key -> new ArrayList<>());

                for (Consumer<Object> callback : eventEntry.getValue()) {
                    listeners.add(new ListenerEntry(scriptName, callback));
                }
            }
        }

        Map<ScriptEvent<?>, List<ListenerEntry>> snapshot = new LinkedHashMap<>();

        int totalHandlers = 0;

        for (var entry : next.entrySet()) {
            snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
            totalHandlers += entry.getValue().size();
        }

        activeServerListeners = Map.copyOf(snapshot);
        KScript.LOGGER.info("Activated {} handler(s) across {} event type(s)", totalHandlers, snapshot.size());
    }

    public static void clearServerListeners() {
        activeServerListeners = Map.of();
        KScript.LOGGER.debug("Cleared all server script event handlers");
    }

    public static <T> void register(ScriptEvent<T> event, Consumer<T> listener) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(listener);

        Registrations registrations = CURRENT.get();

        if (registrations == null) {
            throw new IllegalStateException("Events can only be registered while " + "loading a server script");
        }

        registrations.eventHandlers.computeIfAbsent(event, key -> new ArrayList<>())
                .add(payload -> listener.accept(event.payloadType().cast(payload)));
    }

    public static <T> void dispatch(ScriptEvent<T> event, T payload) {
        List<ListenerEntry> listeners = activeServerListeners.get(event);

        if (listeners == null) {
            return;
        }

        for (ListenerEntry entry : listeners) {
            try {
                entry.handler().accept(payload);
            } catch (RuntimeException | LinkageError e) {
                KScript.LOGGER.error("Event {} failed in script {}", event.id(), entry.scriptName(), e);
            }
        }
    }

    public static final class Registrations {

        private final String scriptName;

        private final Map<ScriptEvent<?>, List<Consumer<Object>>> eventHandlers = new LinkedHashMap<>();

        private Registrations(String scriptName) {
            this.scriptName = scriptName;
        }
    }

    private record ListenerEntry(String scriptName, Consumer<Object> handler) {}
}
