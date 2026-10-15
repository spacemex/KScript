
package com.github.spacemex.kscript.api.event;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public final class ScriptEventFactory {

    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private static final ConcurrentMap<String, ScriptEvent<?>> SERVER_EVENTS = new ConcurrentHashMap<>();

    private ScriptEventFactory() {}

    public static <T> ScriptEvent<T> server(String id, Class<T> eventType) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(eventType);

        if (!VALID_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid script event ID: " + id);
        }

        ScriptEvent<?> event = SERVER_EVENTS.compute(id, (key, existing) -> {
                    if (existing == null) {
                        return new ScriptEvent<>(key, eventType);
                    }

                    if (!existing.payloadType().equals(eventType)) {
                        throw new IllegalStateException("Event " + key + " is already registered with type "
                                + existing.payloadType().getName());
                    }
                    return existing;
                });

        @SuppressWarnings("unchecked")
        ScriptEvent<T> typedEvent = (ScriptEvent<T>) event;
        return typedEvent;
    }
}
