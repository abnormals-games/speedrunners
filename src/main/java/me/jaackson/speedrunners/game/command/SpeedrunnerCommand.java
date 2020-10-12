package me.jaackson.speedrunners.game.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.jaackson.speedrunners.Speedrunners;
import me.jaackson.speedrunners.game.SpeedrunnersGame;
import me.jaackson.speedrunners.game.TeamManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Collections;

public class SpeedrunnerCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("speedrunners")
				.requires(source -> source.hasPermissionLevel(4))
				.then(Commands.literal("add")
						.then(Commands.argument("players", EntityArgument.players())
								.then(Commands.argument("role", EnumArgument.enumArgument(TeamManager.Role.class))
										.executes(context -> {
											EntityArgument.getPlayers(context, "players").forEach(player -> SpeedrunnersGame.getInstance().getTeamManager().setRole(player, context.getArgument("role", TeamManager.Role.class)));
											return Command.SINGLE_SUCCESS;
										}))))
				.then(Commands.literal("start").executes(context -> {
					SpeedrunnersGame.getInstance().start();
					return Command.SINGLE_SUCCESS;
				}))
				.then(Commands.literal("stop").executes(context -> {
					SpeedrunnersGame.getInstance().stop();
					return Command.SINGLE_SUCCESS;
				})));
	}
}