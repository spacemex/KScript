package com.github.spacemex.kscript.api;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerQuitEvent {

    private final ServerPlayer player;

    public PlayerQuitEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return player.getName().getString();
    }
}
