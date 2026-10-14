package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;
import dev.architectury.platform.Platform;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
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

            ScriptEngineManager manager = new ScriptEngineManager(KScriptRuntime.class.getClassLoader());

            ScriptEngine engine = manager.getEngineByExtension("kts");

            if (engine == null) {
                KScript.LOGGER.error("Failed to discover the kotlin scripting engine!");
                return;
            }

            KScript.LOGGER.info("Kotlin scripting engine: {}", engine.getFactory().getEngineName());
            execute(engine, example);
        } catch (IOException | ScriptException e) {
            KScript.LOGGER.error("Failed to initialize KScript runtime", e);
        }
    }

    private static void execute(ScriptEngine engine, Path script) throws IOException, ScriptException {
        KScript.LOGGER.debug("Executing script: {}", script.getFileName());

        try (Reader reader = Files.newBufferedReader(script, StandardCharsets.UTF_8)) {
            Object result = engine.eval(reader);

            KScript.LOGGER.debug("Script {} returned: {}", script.getFileName(), result);
        }
    }
}
