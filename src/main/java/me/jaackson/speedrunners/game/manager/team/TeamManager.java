package me.jaackson.speedrunners.game.manager.team;

import me.jaackson.speedrunners.SpeedRunners;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TeamManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerScoreboard scoreboard;

    public TeamManager(MinecraftServer server) {
        this.scoreboard = server.getScoreboard();
        createTeams(server);
    }

    public static void createTeams(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        for (Teams type : Teams.values()) {
            if (!scoreboard.getTeamNames().contains(type.getId())) {
                ScorePlayerTeam team = scoreboard.createTeam(type.getId());
                team.setColor(type.getColor());
                team.setDisplayName(type.getName());
                LOGGER.debug("Created team " + team);
            }
        }
    }

    public void setDead(PlayerEntity player, boolean changeMode) {
        if (SpeedRunners.getServer() == null)
            return;


        player.stopRiding();
        if (this.scoreboard.getTeamNames().contains(Teams.DEAD.getId()))
            this.scoreboard.addPlayerToTeam(player.getScoreboardName(), SpeedRunners.getTeamManager().getTeam(Teams.DEAD));
        if (changeMode)
            player.setGameType(GameType.SPECTATOR);
    }

    public ScorePlayerTeam getTeam(Teams team) {
        return this.scoreboard.getTeam(team.getId());
    }

    public void addPlayer(Teams team, PlayerEntity player) {
        this.scoreboard.addPlayerToTeam(player.getScoreboardName(), getTeam(team));
    }

    public void removePlayer(Teams team, PlayerEntity player) {
        this.scoreboard.removePlayerFromTeam(player.getScoreboardName(), getTeam(team));
    }

    public void removePlayer(PlayerEntity player) {
        this.scoreboard.removePlayerFromTeams(player.getScoreboardName());
    }

    public ServerScoreboard getScoreboard() {
        return scoreboard;
    }
}
