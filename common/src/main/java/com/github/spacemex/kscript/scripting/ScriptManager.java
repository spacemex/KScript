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
import java.util.List;
import java.util.stream.Stream;

public final class ScriptManager {

    private final ScriptEngineManager engineManager;

    public ScriptManager() {
        this.engineManager = new ScriptEngineManager(ScriptManager.class.getClassLoader());
    }

    public void loadDirectory(Path directory) {
        List<Path> scripts;

        try (Stream<Path> stream = Files.walk(directory)) {
            scripts = stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString()
                    .endsWith(".kts")).sorted(Comparator.comparing(path -> directory.relativize(path).toString()
                    .replace('\\', '/'))
            ).toList();
        } catch (IOException | UncheckedIOException e) {
            KScript.LOGGER.error("Failed to discover scripts in {}", directory, e);
            return;
        }

        if (scripts.isEmpty()) {
            KScript.LOGGER.debug("No scripts found in {}", directory);
            return;
        }

        KScript.LOGGER.debug("Discovered {} kotlin script(s) in {}", scripts.size(), directory);

        int loaded = 0;
        int failed = 0;

        for (Path script : scripts) {
            if (executeScript(directory, script)) {
                loaded++;
            } else {
                failed++;
            }
        }

        KScript.LOGGER.debug("Script loading complete: {} loaded, {} failed", loaded, failed);
    }

    private boolean executeScript(Path directory, Path script) {
        String relativePath = directory.relativize(script).toString().replace('\\', '/');

        try {
            ScriptEngine engine = engineManager.getEngineByExtension("kts");

            if (engine == null) {
                KScript.LOGGER.error("Kotlin scripting engine unavailable for {}", relativePath);
                return false;
            }

            KScript.LOGGER.debug("Executing kotlin script: {}", relativePath);

            try (Reader reader = Files.newBufferedReader(script, StandardCharsets.UTF_8)) {
                engine.eval(reader);
            }

            KScript.LOGGER.debug("Successfully loaded Script: {}", relativePath);
            return true;
        } catch (ScriptException e) {
            KScript.LOGGER.error("Script error in {} (line {}): {}", relativePath, e.getLineNumber(), e);
            return false;
        } catch (IOException | RuntimeException e) {
            KScript.LOGGER.error("Failed to execute script: {}", relativePath, e);
            return false;
        }
    }
}
