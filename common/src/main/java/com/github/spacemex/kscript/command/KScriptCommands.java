package com.github.spacemex.kscript.command;

import com.github.spacemex.kscript.scripting.KScriptRuntime;
import com.github.spacemex.kscript.scripting.ScriptLoadResult;
import com.github.spacemex.kscript.scripting.ScriptType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class KScriptCommands {

    private KScriptCommands() {}

    public static void initialize() {
        CommandRegistrationEvent.EVENT.register((d, r, s) -> {
            d.register(Commands.literal("kscript")
                    .then(Commands.literal("reload")
                            .then(Commands.literal("server")
                                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                    .executes(c -> reloadServer(c.getSource())))
//                            .then(Commands.literal("client")
//                                    .executes(c -> reloadClient(c.getSource())))
                    )
            );
        });
    }

    private static int reloadServer(CommandSourceStack source) throws CommandSyntaxException {
        ScriptLoadResult result = KScriptRuntime.loadScripts(ScriptType.SERVER);

        if (result.discoveryFailed()) {
            source.sendFailure(Component.translatable("kscript.chat.reload.server.error.discovery_failed"));
            return 0;
        }

        if (!result.successful()) {
            source.sendFailure(Component.translatable("kscript.chat.reload.server.error.some_failed",
                    result.loaded(), result.failed()));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("kscript.chat.reload.server.successful", result.loaded()),
                false);
        return 1;
    }

    private static int reloadClient(CommandSourceStack source) throws CommandSyntaxException {
        ScriptLoadResult result = KScriptRuntime.loadScripts(ScriptType.CLIENT);

        if (result.discoveryFailed()) {
            source.sendFailure(Component.translatable("kscript.chat.reload.client.error.discovery_failed"));
        }

        if (!result.successful()) {
            source.sendFailure(Component.translatable("kscript.chat.reload.client.error.some_failed",
                    result.loaded(), result.failed()));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("kscript.chat.reload.client.successful", result.loaded()),
                false);
        return 1;
    }
}
