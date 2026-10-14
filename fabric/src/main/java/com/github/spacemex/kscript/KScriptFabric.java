package com.github.spacemex.kscript;

import net.fabricmc.api.ModInitializer;

public class KScriptFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        KScript.initialize();
    }
}
