package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ScriptManager {

    private final ScriptEngineManager engineManager;

    public ScriptManager() {
        this.engineManager = new ScriptEngineManager(ScriptManager.class.getClassLoader());
    }


    public ScriptLoadResult loadDirectory(Path directory, ScriptType type) {
        List<Path> scripts;

        try (Stream<Path> stream = Files.walk(directory)) {
            scripts = stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString()
                            .endsWith(".kts")).sorted(Comparator.comparing(path ->
                            directory.relativize(path).toString().replace('\\', '/'))).toList();

        } catch (IOException | UncheckedIOException e) {
            KScript.LOGGER.error("Failed to discover scripts in {}", directory, e);
            return new ScriptLoadResult(0, 0, 0, true);
        }

        Map<String, ScriptEventRegistry.Registrations> prepared = new LinkedHashMap<>();

        int loaded = 0;
        int failed = 0;

        KScript.LOGGER.debug("Discovered {} {} script(s)", scripts.size(), type);

        for (Path script : scripts) {
            String relativePath = directory.relativize(script).toString().replace('\\', '/');

            ScriptExecutionResult result = executeScript(type, script, relativePath);

            if (result.successful()) {
                loaded++;

                if (type == ScriptType.SERVER) {
                    prepared.put(relativePath, result.registrations());
                }
            } else {
                failed++;
            }
        }

        if (type == ScriptType.SERVER) {
            if (failed == 0) {
                ScriptEventRegistry.replaceServerListeners(prepared);
            } else {
                KScript.LOGGER.warn("Server script reload failed. " + "Previous event handlers remain active.");
            }
        }

        KScript.LOGGER.debug("{} script loading complete: {} loaded, {} failed", type, loaded, failed);

        return new ScriptLoadResult(scripts.size(), loaded, failed, false);
    }


    private ScriptExecutionResult executeScript(ScriptType type, Path script, String relativePath) {
        boolean registrationStarted = false;

        try {
            ScriptEngine engine = engineManager.getEngineByExtension("kts");

            if (engine == null) {
                KScript.LOGGER.error("Kotlin scripting engine unavailable for {}", relativePath);
                return new ScriptExecutionResult(false, null);
            }

            KScript.LOGGER.debug("Executing {} script: {}", type, relativePath);

            if (type == ScriptType.SERVER) {
                ScriptEventRegistry.beginScript(relativePath);
                registrationStarted = true;
            }

            try (Reader reader = Files.newBufferedReader(script, StandardCharsets.UTF_8)) {
                engine.eval(reader);
            }

            ScriptEventRegistry.Registrations registrations = null;

            if (registrationStarted) {
                registrations = ScriptEventRegistry.completeScript();
                registrationStarted = false;
            }

            KScript.LOGGER.debug("Successfully executed script: {}", relativePath);
            return new ScriptExecutionResult(true, registrations);
        } catch (ScriptException e) {
            KScript.LOGGER.error("Script error in {} (line {}): {}", relativePath, e.getLineNumber(), e.getMessage());
        } catch (IOException | RuntimeException | LinkageError e) {
            KScript.LOGGER.error("Failed to execute script: {}", relativePath, e);
        } finally {
            if (registrationStarted) {
                ScriptEventRegistry.discardScript();
            }
        }

        return new ScriptExecutionResult(false, null);
    }

    private record ScriptExecutionResult(boolean successful, ScriptEventRegistry.Registrations registrations) {}
}
