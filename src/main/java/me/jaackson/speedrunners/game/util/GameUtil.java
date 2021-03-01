package me.jaackson.speedrunners.game.util;

import me.jaackson.speedrunners.game.SpeedrunnersGame;
import me.jaackson.speedrunners.game.TeamManager;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SWorldSpawnChangedPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class GameUtil {

	public static void createHunterCompass(PlayerEntity player, ItemStack stack) {
		setHunterCompassNbt(stack);
		player.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.8F, 0.8F + player.world.rand.nextFloat() * 0.4F);
	}

	public static void setHunterCompassNbt(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		ITextComponent name = new StringTextComponent(TextFormatting.RED + "Hunter's Compass");

		if (!stack.isEnchanted())
			stack.addEnchantment(Enchantments.UNBREAKING, 1);

		if (stack.getDisplayName() != name)
			stack.setDisplayName(name);

		if (!nbt.contains("HideFlags", Constants.NBT.TAG_BYTE)) nbt.putByte("HideFlags", (byte) 1);
		if (!nbt.contains("HunterCompass")) nbt.putBoolean("HunterCompass", true);
	}

	public static void clearHunterCompass(ItemStack stack) {
		stack.setDisplayName(null);
		stack.removeChildTag("Enchantments");
		stack.removeChildTag("StoredEnchantments");
		stack.removeChildTag("HideFlags");
		stack.removeChildTag("HunterCompass");
	}

	public static boolean isHunterCompass(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		return nbt != null && nbt.contains("HunterCompass") && nbt.getBoolean("HunterCompass");
	}

	public static Set<ItemStack> getCompasses(PlayerEntity player) {
		Set<ItemStack> compasses = new HashSet<>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() == Items.COMPASS) {
				compasses.add(stack);
			}
		}

		return compasses;
	}

	public static void setCompassPos(ServerPlayerEntity player, BlockPos pos) {
		player.connection.sendPacket(new SWorldSpawnChangedPacket(pos, 0.0F));
	}

	public static void makeCompassGoBatshitCrazy(ServerPlayerEntity player) {
		float angle = (float) (Math.random() * Math.PI * 2);
		float dx = (float) (Math.cos(angle) * 5);
		float dz = (float) (Math.sin(angle) * 5);

		BlockPos randPos = new BlockPos(dx, 0, dz);
		player.connection.sendPacket(new SWorldSpawnChangedPacket(player.getPosition().add(randPos), 0.0F));
	}

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

		player.func_242111_a(world.getDimensionKey(), pos, 0.0F, true, false);
		player.teleport(world, info.getSpawnX(), info.getSpawnY(), info.getSpawnZ(), 0, 0);

		TeamManager.Role role = tm.getRole(player);

		if(role == TeamManager.Role.SPECTATOR || (tm.getRole(player) != TeamManager.Role.SPEEDRUNNER && role != TeamManager.Role.HUNTER))
			tm.setRole(player, TeamManager.Role.SPEEDRUNNER);
	}
}
