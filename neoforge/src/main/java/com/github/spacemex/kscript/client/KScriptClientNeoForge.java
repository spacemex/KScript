package com.github.spacemex.kscript.client;

import com.github.spacemex.kscript.KScript;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = KScript.MOD_ID, dist = Dist.CLIENT)
public final class KScriptClientNeoForge {

    public KScriptClientNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::clientInitialization);
    }

    private void clientInitialization(FMLClientSetupEvent event) {
        KScript.initializeClientOnly();
    }
}
