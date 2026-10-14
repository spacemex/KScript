package com.github.spacemex.kscript;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(KScript.MOD_ID)
public final class KScriptNeoForge {

    public KScriptNeoForge(IEventBus eventBus) {
        KScript.initialize();
    }
}
