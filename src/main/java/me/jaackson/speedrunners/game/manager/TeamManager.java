package me.jaackson.speedrunners.game.manager;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class TeamManager {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void initTeams(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        for(Team type : Team.values()) {
            if(!scoreboard.getTeamNames().contains(type.getString())) {
                ScorePlayerTeam team = scoreboard.createTeam(type.getString());
                team.setColor(type.getColor());
                LOGGER.debug("Created team " + team);
            }
        }
    }

    public enum Team implements IStringSerializable {
        DEAD("dead", TextFormatting.GRAY),
        RUNNER("runner", TextFormatting.AQUA),
        HUNTER("hunter", TextFormatting.RED);

        private final String id;
        private final TextFormatting color;

        Team(String id, TextFormatting color) {
            this.id = id;
            this.color = color;
        }

        @Override
        public String getString() {
            return id;
        }

        public TextFormatting getColor() {
            return color;
        }
    }
}
