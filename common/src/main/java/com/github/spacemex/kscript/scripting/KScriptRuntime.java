package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class KScriptRuntime {

    private static final Path STARTUP_DIR = Platform.getConfigFolder()
            .resolve("kscript").resolve("scripts").resolve("startup");

    private static final String DEFAULT_SCRIPT = """
            val result: Int = 21 * 2
            
            println("[KScript] Hello from Kotlin!")
            println("[KScript] Result: $result")
            
            result
            """;

    private KScriptRuntime() {}

    public static void initialize() {
        try {
            Files.createDirectories(STARTUP_DIR);

            Path example = STARTUP_DIR.resolve("hello.kts");

            if (Files.notExists(example)) {
                Files.writeString(example, DEFAULT_SCRIPT, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }

            ScriptManager manager = new ScriptManager();
            manager.loadDirectory(STARTUP_DIR);
        } catch (IOException e) {
            KScript.LOGGER.error("Failed to initialize KScript runtime", e);
        }
    }
}
