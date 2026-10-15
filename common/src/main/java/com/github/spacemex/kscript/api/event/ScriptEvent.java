package com.github.spacemex.kscript.api.event;

import com.github.spacemex.kscript.scripting.ScriptEventRegistry;

import java.util.Objects;
import java.util.function.Consumer;

public final class ScriptEvent<T> {

    private final String id;
    private final Class<T> payloadType;


    ScriptEvent(String id, Class<T> payloadType) {
        this.id = Objects.requireNonNull(id);
        this.payloadType = Objects.requireNonNull(payloadType);
    }

    public String id() {
        return id;
    }

    public Class<T> payloadType() {
        return payloadType;
    }

    /**
     * Registers a listener for the currently loading server script
     */
    public void listen(Consumer<T> handler) {
        ScriptEventRegistry.register(this, handler);
    }

    /**
     * Fires this event using the active script listeners
     * Must be invoked on the appropriaate Minecraft thread
     */
    public void fire(T payload) {
        ScriptEventRegistry.dispatch(this, payloadType.cast(Objects.requireNonNull(payload)));
    }
}
