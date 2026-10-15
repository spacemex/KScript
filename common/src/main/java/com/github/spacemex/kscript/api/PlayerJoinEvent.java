package com.github.spacemex.kscript.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerJoinEvent {

    private final ServerPlayer player;

    public PlayerJoinEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return player.getName().getString();
    }

    public String getPlayerUUID() {
        return player.getUUID().toString();
    }

    public void sendMessage(String message, boolean overlay) {
        player.sendSystemMessage(Component.literal(message), overlay);
    }

    public void sendMessage(String message) {
        sendMessage(message, false);
    }
}
