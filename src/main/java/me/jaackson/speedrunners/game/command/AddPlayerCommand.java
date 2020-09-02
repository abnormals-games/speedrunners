package me.jaackson.speedrunners.game.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.arguments.TeamArgument;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;
import java.util.Collections;

public class AddPlayerCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
//        return Commands.literal("player")
//                .then(Commands.literal("join")
//                        .then(Commands.argument("team", TeamArgument.team()).executes(context -> {
//                            joinTeam(context.getSource(), TeamArgument.getTeam(context, "team"), Collections.singleton(context.getSource().assertIsEntity().getScoreboardName()));
//                            return Command.SINGLE_SUCCESS;
//                        })
//                                .then(Commands.argument("players", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).executes(context -> {
//                                    joinTeam(context.getSource(), TeamArgument.getTeam(context, "team"), ScoreHolderArgument.getScoreHolder(context, "players"));
//                                    return Command.SINGLE_SUCCESS;
//                                }))))
//                .then(Commands.literal("remove")
//                        .then(Commands.argument("players", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).executes(context -> {
//                            removeFromAll(context.getSource(), ScoreHolderArgument.getScoreHolder(context, "players"));
//                            return Command.SINGLE_SUCCESS;
//                        })));
        return Commands.literal("player")
                .then(Commands.literal("add")
                        .then(Commands.argument("players", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR)
                                .then(Commands.argument("team", TeamArgument.team()).executes(context -> {
                                    joinTeam(context.getSource(), TeamArgument.getTeam(context, "team"), Collections.singleton(context.getSource().assertIsEntity().getScoreboardName()));
                                    return Command.SINGLE_SUCCESS;
                                }))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("players", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).executes(context -> {
                            removeFromAll(context.getSource(), ScoreHolderArgument.getScoreHolder(context, "players"));
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    public static int removeFromAll(CommandSource source, Collection<String> players) {
        Scoreboard scoreboard = source.getServer().getScoreboard();

        for (String s : players) {
            scoreboard.removePlayerFromTeams(s);
        }

        if (players.size() == 1) {
            source.sendFeedback(new StringTextComponent("Removed " + players.iterator().next() + " from the game"), true);
        } else {
            source.sendFeedback(new StringTextComponent("Removed " + players.size() + " from the game"), true);
        }

        return players.size();
    }

    public static int joinTeam(CommandSource source, ScorePlayerTeam team, Collection<String> players) {
        Scoreboard scoreboard = source.getServer().getScoreboard();

        for (String s : players) {
            scoreboard.addPlayerToTeam(s, team);
        }

        if (players.size() == 1) {
            source.sendFeedback(new StringTextComponent("Added " + players.iterator().next() + " to " + team.getDisplayName().getString()), true);
        } else {
            source.sendFeedback(new StringTextComponent("Added " + players.size() + " to " + team.func_237501_d_()), true);
        }

        return players.size();
    }
}