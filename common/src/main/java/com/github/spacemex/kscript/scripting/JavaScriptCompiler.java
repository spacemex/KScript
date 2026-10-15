
package com.github.spacemex.kscript.scripting;

import com.github.spacemex.kscript.KScript;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class JavaScriptCompiler {

    private JavaScriptCompiler() {
    }

    public static boolean validateDirectory(Path directory) {
        List<Path> sources;

        try {
            Files.createDirectories(directory);

            try (Stream<Path> stream = Files.walk(directory)) {
                sources = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .sorted()
                        .toList();
            }

        } catch (IOException e) {
            KScript.LOGGER.error(
                    "Unable to discover Java scripts in {}",
                    directory,
                    e
            );
            return false;
        }

        if (sources.isEmpty()) {
            KScript.LOGGER.debug("No Java scripts found in {}", directory);
            return true;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            KScript.LOGGER.error(
                    "Java compiler unavailable. KScript requires " +
                            "a Java 25 installation containing jdk.compiler."
            );
            return false;
        }

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        Path output = null;

        try {
            output = Files.createTempDirectory("kscript-java-check-");

            try (StandardJavaFileManager manager =
                         compiler.getStandardFileManager(
                                 diagnostics,
                                 Locale.ROOT,
                                 StandardCharsets.UTF_8
                         )) {

                var units = manager.getJavaFileObjectsFromPaths(
                        sources
                );

                List<String> options = List.of(
                        "--release", "25",
                        "-proc:none",
                        "-encoding", "UTF-8",
                        "-d", output.toString()
                );

                boolean success = Boolean.TRUE.equals(
                        compiler.getTask(
                                null,
                                manager,
                                diagnostics,
                                options,
                                null,
                                units
                        ).call()
                );

                for (Diagnostic<? extends JavaFileObject> diagnostic
                        : diagnostics.getDiagnostics()) {

                    String source = diagnostic.getSource() == null
                            ? "<unknown>"
                            : Path.of(diagnostic.getSource().toUri())
                            .getFileName()
                            .toString();

                    String message = diagnostic.getMessage(
                            Locale.ROOT
                    );

                    if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                        KScript.LOGGER.error(
                                "{}:{}:{}: {}",
                                source,
                                diagnostic.getLineNumber(),
                                diagnostic.getColumnNumber(),
                                message
                        );
                    } else {
                        KScript.LOGGER.debug(
                                "{}:{}: {}",
                                source,
                                diagnostic.getLineNumber(),
                                message
                        );
                    }
                }

                KScript.LOGGER.info(
                        "Java validation: {} source file(s), success={}",
                        sources.size(),
                        success
                );

                return success;
            }

        } catch (IOException | RuntimeException e) {
            KScript.LOGGER.error(
                    "Failed to compile Java scripts",
                    e
            );
            return false;

        } finally {
            if (output != null) {
                deleteTemporaryOutput(output);
            }
        }
    }

    private static void deleteTemporaryOutput(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths
                    .sorted(Comparator.reverseOrder())
                    .toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            KScript.LOGGER.warn(
                    "Unable to clean Java compilation directory {}",
                    directory,
                    e
            );
        }
    }
}
