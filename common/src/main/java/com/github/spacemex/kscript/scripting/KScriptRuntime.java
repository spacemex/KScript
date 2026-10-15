package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class KScriptRuntime {

    private static final Path SCRIPTS_ROOT = Platform.getConfigFolder().resolve("kscript").resolve("scripts");

    private static final String DEFAULT_SCRIPT = """
            val result: Int = 21 * 2
            
            println("[KScript] Hello from Kotlin!")
            println("[KScript] Result: $result")
            
            result
            """;

    private static final ScriptManager SCRIPT_MANAGER = new ScriptManager();

    private KScriptRuntime() {}

    public static void initialize() {
        try {
            for (ScriptType type : ScriptType.values()) {
                Files.createDirectories(type.resolve(SCRIPTS_ROOT));
            }

            Path example = ScriptType.STARTUP.resolve(SCRIPTS_ROOT).resolve("hello.kts");

            if (Files.notExists(example)) {
                Files.writeString(example, DEFAULT_SCRIPT, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            KScript.LOGGER.error("Failed to initialize KScript directories", e);
            return;
        }

        LifecycleEvent.SERVER_STARTED.register(s -> loadScripts(ScriptType.SERVER));
        loadScripts(ScriptType.STARTUP);
    }

    public static ScriptLoadResult loadScripts(ScriptType type) {
        Path directory = type.resolve(SCRIPTS_ROOT);

        KScript.LOGGER.debug("Loading {} scripts from {}", type, directory);
        return SCRIPT_MANAGER.loadDirectory(directory);
    }
}
