package com.github.spacemex.kscript.scripting;

import java.nio.file.Path;

public enum ScriptType {
    STARTUP("startup"),
    SERVER("server"),
    CLIENT("client");

    private final String folderName;

    ScriptType(String folderName) {
        this.folderName = folderName;
    }

    public Path resolve(Path root) {
        return root.resolve(folderName);
    }
}
