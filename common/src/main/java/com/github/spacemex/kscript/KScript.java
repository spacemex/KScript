package com.github.spacemex.kscript;

import com.github.spacemex.kscript.scripting.KScriptRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KScript {

    public static final String MOD_ID = "kscript";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void initialize() {
        LOGGER.info("Initializing KScript...");

        KScriptRuntime.initialize();
    }

    public static void initializeClientOnly() {
        LOGGER.info("Hello Client!");
    }
}
