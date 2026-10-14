package com.github.spacemex.kscript.client;

import com.github.spacemex.kscript.KScript;
import net.fabricmc.api.ClientModInitializer;

public class KScriptFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KScript.initializeClientOnly();
    }
}
