package com.github.spacemex.kscript.scripting;

public record ScriptLoadResult(int discovered, int loaded, int failed, boolean discoveryFailed) {

    public boolean successful() {
        return !discoveryFailed && failed == 0;
    }
}
