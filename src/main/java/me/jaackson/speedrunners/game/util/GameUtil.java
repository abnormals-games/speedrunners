package me.jaackson.speedrunners.game.util;

import me.jaackson.speedrunners.compat.SeekerCompassCompat;
import me.jaackson.speedrunners.game.SpeedrunnersGame;
import me.jaackson.speedrunners.game.TeamManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class GameUtil {

	public static Stream<ItemStack> getInventory(ServerPlayerEntity player) {
		List<ItemStack> inventory = new ArrayList<>();

		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			inventory.add(stack);
		}

		return inventory.stream();
	}

	public static PlayerEntity getNearestRunner(PlayerEntity player) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager manager = game.getTeamManager();

		List<ServerPlayerEntity> players = game.getServer().getPlayerList().getPlayers();

		double distNear = 0.0D;
		PlayerEntity playerNear = null;
		for (PlayerEntity player2 : players) {
			if (player == player2) {
				continue;
			}
			if (players.stream().noneMatch(p -> p.getUniqueID().equals(player2.getUniqueID()) && manager.getRole(p) == TeamManager.Role.SPEEDRUNNER))
				continue;
			if (player.getEntityWorld() != player2.getEntityWorld()) {
				continue;
			}

			BlockPos pos = player.getPosition();
			double dist = Math.sqrt(player.getPosition().distanceSq(pos));
			if (playerNear == null || dist < distNear) {
				playerNear = player2;
				distNear = dist;
			}
		}
		return playerNear;
	}

	public static void resetPlayer(ServerPlayerEntity player) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		ServerWorld world = game.getServer().getWorld(World.OVERWORLD);
		TeamManager tm = game.getTeamManager();

		if(world == null)
			return;

		IWorldInfo info = world.getWorldInfo();
		BlockPos pos = new BlockPos(world.getSpawnPoint().getX(), world.getSpawnPoint().getY(), world.getSpawnPoint().getZ());

		player.inventory.func_234564_a_(predicate -> true, -1, player.container.func_234641_j_());
		player.openContainer.detectAndSendChanges();
		player.container.onCraftMatrixChanged(player.inventory);
		player.updateHeldItem();

		player.func_241153_a_(world.getDimensionKey(), pos, true, false);
		player.teleport(world, info.getSpawnX(), info.getSpawnY(), info.getSpawnZ(), 0, 0);

		TeamManager.Role role = tm.getRole(player);

		if(role == TeamManager.Role.SPECTATOR || (tm.getRole(player) != TeamManager.Role.SPEEDRUNNER && role != TeamManager.Role.HUNTER))
			tm.setRole(player, TeamManager.Role.SPEEDRUNNER);
	}
}
