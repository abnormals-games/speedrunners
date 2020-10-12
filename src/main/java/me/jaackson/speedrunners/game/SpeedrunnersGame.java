package me.jaackson.speedrunners.game;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.jaackson.speedrunners.game.util.GameUtil;
import me.jaackson.speedrunners.game.util.Scheduler;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class SpeedrunnersGame {
	private static SpeedrunnersGame instance;
	private final MinecraftServer server;
	private final TeamManager teamManager;
	private final Scheduler scheduler;

	private boolean running;
	private Phase phase;
	private ScheduledFuture<?> countdown;
	private ScheduledFuture<?> countdownMessages;

	public SpeedrunnersGame(MinecraftServer server) {
		this.server = server;
		this.teamManager = new TeamManager(server);
		this.scheduler = new Scheduler(server);

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

	public Scheduler getScheduler() {
		return scheduler;
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
		MinecraftServer server = this.getServer();
		ServerWorld world = server.getWorld(World.OVERWORLD);

		this.setRunning(false);
		this.setPhase(Phase.WAITING);

		server.getGameRules().get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
		server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);

		if(world == null)
			return;

		world.func_241114_a_(6000);

		BlockPos pos = new BlockPos(world.getSpawnPoint().getX(), world.getSpawnPoint().getY(), world.getSpawnPoint().getZ());
		world.func_241124_a__(pos);
		world.getWorldBorder().setCenter(pos.getX(), pos.getZ());
		world.getWorldBorder().setTransition(16);
		world.getWorldBorder().setWarningDistance(0);

		server.getPlayerList().getPlayers().forEach(GameUtil::resetPlayer);
	}

	public void start() {
		MinecraftServer server = this.getServer();
		ServerWorld world = server.getWorld(World.OVERWORLD);
		TeamManager tm = this.getTeamManager();

		server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);

		if(world == null || !tm.getSpeedrunners().findAny().isPresent() || !tm.getHunters().findAny().isPresent())
			return;

		world.getWorldBorder().setTransition(50000);
		server.getPlayerList().getPlayers().forEach(GameUtil::resetPlayer);

		this.setRunning(true);
		this.setPhase(Phase.STARTING);

		this.countdown = this.getScheduler().schedule(() -> {
			this.setPhase(Phase.STARTED);
			this.countdown = null;

			if(this.countdownMessages != null) {
				this.countdownMessages.cancel(false);
				this.countdownMessages = null;
			}
		}, 60, TimeUnit.SECONDS);

		this.countdownMessages = this.getScheduler().scheduleAtFixedRate(() -> server.getPlayerList().getPlayers().forEach(player -> player.sendMessage(new StringTextComponent("Starting in " + this.countdown.getDelay(TimeUnit.SECONDS) + " seconds!"), Util.DUMMY_UUID)), 0, 5, TimeUnit.SECONDS);
	}

	public void stop() {
		MinecraftServer server = this.getServer();
		ServerWorld world = server.getWorld(World.OVERWORLD);
		TeamManager tm = this.getTeamManager();

		boolean speedrunnersWin = tm.getSpeedrunners().findAny().isPresent();
		ITextComponent winningMessage = new StringTextComponent(TextFormatting.GOLD + "" + TextFormatting.BOLD + "WINNERS: " + TextFormatting.RESET + "" + (speedrunnersWin ? TextFormatting.AQUA + "Speedrunners" : TextFormatting.RED + "Hunters"));

		server.getPlayerList().getPlayers().forEach(player -> {
			try {
				player.connection.sendPacket(new STitlePacket(STitlePacket.Type.TITLE, TextComponentUtils.func_240645_a_(null, winningMessage, player, 0)));
			} catch (CommandSyntaxException ignored) {}
		});


		this.setRunning(false);
		this.setPhase(Phase.ENDING);

		setup();

		if(world != null) {
			BlockPos pos = new BlockPos(world.getWorldInfo().getSpawnX(), world.getWorldInfo().getSpawnY(), world.getWorldInfo().getSpawnZ());
			world.playSound(null, pos, (speedrunnersWin ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.ENTITY_ENDER_DRAGON_DEATH), SoundCategory.AMBIENT, 1.0F, 1.0F);
		}
	}

	public enum Phase {
		WAITING, STARTING, STARTED, ENDING;
	}
}
