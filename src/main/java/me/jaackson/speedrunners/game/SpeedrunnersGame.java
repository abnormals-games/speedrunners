package me.jaackson.speedrunners.game;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;

import java.util.Objects;

public final class SpeedrunnersGame {
	private static SpeedrunnersGame instance;
	private final MinecraftServer server;
	private final TeamManager teamManager;

	private boolean running;
	private Phase phase;

	public SpeedrunnersGame(MinecraftServer server) {
		this.server = server;
		this.teamManager = new TeamManager();
		this.phase = Phase.WAITING;

		this.setup();

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

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	private void setup() {
		ServerWorld world = this.getServer().getWorld(World.OVERWORLD);

		this.setRunning(false);
		this.setPhase(Phase.WAITING);

		this.getServer().getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, this.getServer());

		if(world == null)
			return;

		BlockPos pos = new BlockPos(world.getSpawnPoint().getX(), world.getSpawnPoint().getY(), world.getSpawnPoint().getZ());
		world.func_241124_a__(pos);
		world.getWorldBorder().setCenter(pos.getX(), pos.getZ());
		world.getWorldBorder().setTransition(15);
		world.getWorldBorder().setWarningDistance(0);

		server.getPlayerList().getPlayers().forEach(player -> {
			player.func_241153_a_(world.getDimensionKey(), pos, true, false);
		});
	}

	public void start() {
		MinecraftServer server = this.getServer();
		ServerWorld world = server.getWorld(World.OVERWORLD);

		if(world == null)
			return;

		IWorldInfo info = world.getWorldInfo();
		world.getWorldBorder().setTransition(world.getWorldBorder().getSize(), 50000, 30 * 1000L);

		server.getPlayerList().getPlayers().forEach(player -> {
			player.inventory.func_234564_a_(predicate -> true, -1, player.container.func_234641_j_());
			player.openContainer.detectAndSendChanges();
			player.container.onCraftMatrixChanged(player.inventory);
			player.updateHeldItem();

			player.connection.setPlayerLocation(info.getSpawnX(), info.getSpawnY(), info.getSpawnZ(), 0, 90);
		});

		server.sendMessage(new StringTextComponent("Game has started!"), Util.DUMMY_UUID);

		this.setRunning(true);
		this.setPhase(Phase.STARTING);
		EventListener.timer = 400;
	}

	public void stop() {
		MinecraftServer server = this.getServer();
		TeamManager tm = this.getTeamManager();
		ITextComponent winningMessage = new StringTextComponent(TextFormatting.GOLD + "" + TextFormatting.BOLD + "WINNERS: " + TextFormatting.RESET + "" + (tm.getSpeedrunners().findAny().isPresent() ? TextFormatting.AQUA + "Speedrunners" : TextFormatting.RED + "Hunters"));

		server.getPlayerList().getPlayers().forEach(player -> {
			try {
				player.connection.sendPacket(new STitlePacket(STitlePacket.Type.TITLE, TextComponentUtils.func_240645_a_(null, winningMessage, player, 0)));
			} catch (CommandSyntaxException ignored) {}
		});

		this.setRunning(false);
		this.setPhase(Phase.ENDING);

		setup();
	}

	public enum Phase {
		WAITING, STARTING, STARTED, ENDING;
	}
}
