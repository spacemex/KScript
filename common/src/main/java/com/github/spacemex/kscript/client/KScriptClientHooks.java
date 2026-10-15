package com.github.spacemex.kscript.client;

import com.github.spacemex.kscript.scripting.KScriptRuntime;
import com.github.spacemex.kscript.scripting.ScriptType;
import dev.architectury.event.events.client.ClientLifecycleEvent;

public final class KScriptClientHooks {

    private KScriptClientHooks() {}

    public static void initialize() {
        ClientLifecycleEvent.CLIENT_STARTED.register(m -> KScriptRuntime.loadScripts(ScriptType.CLIENT));
    }
}
