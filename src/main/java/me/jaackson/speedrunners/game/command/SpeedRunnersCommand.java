package me.jaackson.speedrunners.game.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.util.Collections;

public class SpeedRunnersCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("speedrunners")
                .requires(source -> source.hasPermissionLevel(4))
                .then(AddPlayerCommand.register())
                .then(Commands.literal("leave").executes(context -> {
                    AddPlayerCommand.removeFromAll(context.getSource(), Collections.singleton(context.getSource().assertIsEntity().getScoreboardName()));
                    return Command.SINGLE_SUCCESS;
                })));
    }
}
