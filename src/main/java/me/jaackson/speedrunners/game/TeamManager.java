package me.jaackson.speedrunners.game;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public final class TeamManager {
	private final Map<UUID, Role> roleSet = new HashMap<>();

	public TeamManager() {}

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
		this.roleSet.put(player.getUniqueID(), role);
	}

	enum Role {
		HUNTER(TextFormatting.RED, GameType.SURVIVAL),
		SPEEDRUNNER(TextFormatting.AQUA, GameType.SURVIVAL),
		SPECTATOR(TextFormatting.GRAY, GameType.SPECTATOR);

		private final int color;
		private final GameType mode;

		Role(TextFormatting color, GameType mode) {
			this.color = color.getColor() != null ? color.getColor() : 0;
			this.mode = mode;
		}

		Role(int color, GameType mode) {
			this.color = color;
			this.mode = mode;
		}

		public int getColor() {
			return this.color;
		}

		public GameType getMode() {
			return this.mode;
		}
	}
}
