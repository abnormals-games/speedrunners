package me.jaackson.speedrunners.game.manager;

import me.jaackson.speedrunners.SpeedRunners;
import net.minecraft.scoreboard.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ScoreboardManager {

    public static void createScoreboard() {
        ServerScoreboard scoreboard = SpeedRunners.getTeamManager().getScoreboard();
        ScoreObjective objective = scoreboard.getObjective("sr_info");
        if(objective == null) {
            ScoreObjective infoObjective = scoreboard.addObjective(
                    "sr_info",
                    ScoreCriteria.DUMMY,
                    new StringTextComponent("SPEEDRUNNERS").mergeStyle(TextFormatting.AQUA, TextFormatting.BOLD),
                    ScoreCriteria.RenderType.INTEGER
            );
            scoreboard.setObjectiveInDisplaySlot(1, infoObjective);
            updateSidebar(scoreboard, infoObjective, new String[]{
                    TextFormatting.RED + "H " + TextFormatting.RESET + "Hunters" + TextFormatting.DARK_GRAY + "»" + TextFormatting.RESET + " 0",
                    TextFormatting.AQUA + "R " + TextFormatting.RESET + "Runners" + TextFormatting.DARK_GRAY + " » " + TextFormatting.RESET + "0"
            });
        } else {
            updateSidebar(scoreboard, objective, new String[]{
                    TextFormatting.RED + "H " + TextFormatting.RESET + "Hunters" + TextFormatting.DARK_GRAY + "»" + TextFormatting.RESET + " 0",
                    TextFormatting.AQUA + "R " + TextFormatting.RESET + "Runners" + TextFormatting.DARK_GRAY + " » " + TextFormatting.RESET + "0"
            });
        }
    }

    private static void updateSidebar(ServerScoreboard scoreboard, ScoreObjective objective, String[] lines) {
        for (Score score : scoreboard.getSortedScores(objective))
            scoreboard.removeObjectiveFromEntity(score.getPlayerName(), objective);
        for (int i = 0; i < lines.length; i++) {
            scoreboard.getOrCreateScore(lines[i], objective).setScorePoints(lines.length - i);
        }
    }
}
