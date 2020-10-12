package me.jaackson.speedrunners.game;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public final class TeamManager {
	private final Map<UUID, Role> roleSet = new HashMap<>();

	public TeamManager(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();

		for (Role role : Role.values()) {
			if (!scoreboard.getTeamNames().contains(role.toString())) {
				ScorePlayerTeam team = scoreboard.createTeam(role.toString());
				team.setColor(role.getColor());
			}
		}
	}

	public boolean isPlaying(ServerPlayerEntity player) {
		return this.getPlaying().anyMatch(serverPlayer -> serverPlayer.getUniqueID().equals(player.getUniqueID()));
	}

	public Stream<ServerPlayerEntity> getPlaying() {
		return Stream.concat(this.getHunters(), this.getSpeedrunners());
	}

	public Stream<ServerPlayerEntity> getHunters() {
		return this.roleSet.entrySet().stream().filter(entry -> entry.getValue() == Role.HUNTER).map(entry -> SpeedrunnersGame.getInstance().getServer().getPlayerList().getPlayerByUUID(entry.getKey())).filter(Objects::nonNull);
	}

	public Stream<ServerPlayerEntity> getSpeedrunners() {
		return this.roleSet.entrySet().stream().filter(entry -> entry.getValue() == Role.SPEEDRUNNER).map(entry -> SpeedrunnersGame.getInstance().getServer().getPlayerList().getPlayerByUUID(entry.getKey())).filter(Objects::nonNull);
	}

	public Stream<ServerPlayerEntity> getSpectators() {
		return this.roleSet.entrySet().stream().filter(entry -> entry.getValue() != Role.SPEEDRUNNER && entry.getValue() != Role.HUNTER).map(entry -> SpeedrunnersGame.getInstance().getServer().getPlayerList().getPlayerByUUID(entry.getKey())).filter(Objects::nonNull);
	}

	public Role getRole(ServerPlayerEntity player) {
		if (this.isPlaying(player))
			return this.roleSet.get(player.getUniqueID());
		return Role.SPECTATOR;
	}

	public void setRole(ServerPlayerEntity player, Role role) {
		Scoreboard scoreboard = SpeedrunnersGame.getInstance().getServer().getScoreboard();

		this.roleSet.put(player.getUniqueID(), role);
		player.refreshDisplayName();
		scoreboard.addPlayerToTeam(player.getScoreboardName(), scoreboard.getTeam(role.toString()));
	}

	public enum Role {
		HUNTER(TextFormatting.RED),
		SPEEDRUNNER(TextFormatting.AQUA),
		SPECTATOR(TextFormatting.GRAY);

		private final TextFormatting color;

		Role(TextFormatting color) {
			this.color = color;
		}

		public TextFormatting getColor() {
			return this.color;
		}
	}
}
