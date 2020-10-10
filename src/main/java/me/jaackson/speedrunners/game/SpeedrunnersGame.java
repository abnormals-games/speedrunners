package me.jaackson.speedrunners.game;

import net.minecraft.server.MinecraftServer;

public final class SpeedrunnersGame {
	private static SpeedrunnersGame instance;
	private final MinecraftServer server;
	private final TeamManager teamManager;

	public SpeedrunnersGame(MinecraftServer server) {
		this.server = server;
		this.teamManager = new TeamManager();

		instance = this;
	}

	public static SpeedrunnersGame getInstance() {
		return instance;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public TeamManager getTeamManager() {
		return teamManager;
	}
}
