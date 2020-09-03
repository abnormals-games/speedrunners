package me.jaackson.speedrunners.game.manager;

import me.jaackson.speedrunners.SpeedRunners;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ScoreboardManager {
    public static ScoreObjective infoObjective;


    public static void createScoreboard() {
        ServerScoreboard scoreboard = SpeedRunners.getTeamManager().getScoreboard();
        ScoreObjective objective = scoreboard.getObjective("sr_info");
        if (objective == null) {
            ScoreObjective infoObjective = scoreboard.addObjective(
                    "sr_info",
                    ScoreCriteria.DUMMY,
                    new StringTextComponent("SPEEDRUNNERS").mergeStyle(TextFormatting.AQUA, TextFormatting.BOLD),
                    ScoreCriteria.RenderType.INTEGER
            );
            scoreboard.setObjectiveInDisplaySlot(1, infoObjective);
            ScoreboardManager.infoObjective = infoObjective;
        } else {
            ScoreboardManager.infoObjective = objective;
        }
        
        updateSidebar(scoreboard, infoObjective, new String[]{
                "  ",
                "Time Elapsed: " + TextFormatting.GRAY + "1:00",
                "",
                TextFormatting.RED + "H " + TextFormatting.RESET + "Hunters: " + TextFormatting.GRAY + "0",
                TextFormatting.AQUA + "R " + TextFormatting.RESET + "Runners: " + TextFormatting.GRAY + "0",
                " ",
                TextFormatting.GRAY + "games.minecraftabnormals.com"
        });
    }

    public static void updateSidebar(ServerScoreboard scoreboard, ScoreObjective objective, String[] lines) {
        for (Score score : scoreboard.getSortedScores(objective))
            scoreboard.removeObjectiveFromEntity(score.getPlayerName(), objective);
        for (int i = 0; i < lines.length; i++) {
            scoreboard.getOrCreateScore(lines[i], objective).setScorePoints(lines.length - i);
        }
    }
}
