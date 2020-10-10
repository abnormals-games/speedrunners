package me.jaackson.speedrunners.game;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;

public final class ScoreboardManager {

	public void setScore(Score score, int points) {
		int i = score.scorePoints;
		score.scorePoints = points;
		if (i != points || score.forceUpdate) {
			score.forceUpdate = false;
			score.getScoreScoreboard().onScoreChanged(score);
		}
	}

	public void updateBoard(ServerScoreboard scoreboard, ScoreObjective objective, String[] lines) {
		for (Score score : scoreboard.getSortedScores(objective))
			scoreboard.removeObjectiveFromEntity(score.getPlayerName(), objective);
		for (int i = 0; i < lines.length; i++) {
			this.setScore(scoreboard.getOrCreateScore(lines[i], objective), lines.length - i)
		}
	}
}
