package me.jaackson.speedrunners.game;

import me.jaackson.speedrunners.Speedrunners;
import me.jaackson.speedrunners.compat.SeekerCompassCompat;
import me.jaackson.speedrunners.game.util.GameUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Speedrunners.MOD_ID)
public class EventListener {

	private static final Map<UUID, Boolean> TRAVELLING_MAP = new HashMap<>();
	public static int timer;

	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager manager = game.getTeamManager();

		if(!game.isRunning()) {
			IWorldInfo info = player.getEntityWorld().getWorldInfo();

			player.inventory.func_234564_a_(predicate -> true, -1, player.container.func_234641_j_());
			player.openContainer.detectAndSendChanges();
			player.container.onCraftMatrixChanged(player.inventory);
			player.updateHeldItem();

			player.connection.setPlayerLocation(info.getSpawnX(), info.getSpawnY(), info.getSpawnZ(), 0, 90);

			if(manager.getRole(player) != TeamManager.Role.HUNTER)
				manager.setRole(player, TeamManager.Role.SPEEDRUNNER);
			return;
		}

		manager.setRole(player, TeamManager.Role.SPECTATOR);
	}

	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerLoggedOutEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();

		if(game.isRunning()) {
			LightningBoltEntity lightning = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, player.getEntityWorld());
			lightning.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
			player.getEntityWorld().addEntity(lightning);
			player.attackEntityFrom(DamageSource.GENERIC, Float.MAX_VALUE);
		}
	}

	/*
	 * Main game tick.
	 */
	@SubscribeEvent
	public static void onEvent(TickEvent.ServerTickEvent event) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager tm = game.getTeamManager();
		MinecraftServer server = game.getServer();

		if(!game.isRunning()) {
			server.getPlayerList().getPlayers().forEach(player -> {
				if(player.interactionManager.getGameType() != GameType.ADVENTURE && !player.hasPermissionLevel(4))
					player.setGameType(GameType.ADVENTURE);

				player.addPotionEffect(new EffectInstance(Effects.SATURATION, 20, 255, false, false));
			});
			return;
		}

		server.getPlayerList().getPlayers().forEach(player -> {
			if(timer == 400) player.sendMessage(new StringTextComponent("20 seconds until hunters are released!"), Util.DUMMY_UUID);
			if(timer == 200) player.sendMessage(new StringTextComponent("10 seconds until hunters are released!"), Util.DUMMY_UUID);
			if(timer == 100) player.sendMessage(new StringTextComponent("5 seconds until hunters are released!"), Util.DUMMY_UUID);
			if(timer == 0) player.sendMessage(new StringTextComponent("Hunters have been released!"), Util.DUMMY_UUID);
		});

		if(timer >= 0) timer--;
		if(timer < 0) if(game.getPhase() != SpeedrunnersGame.Phase.STARTED && game.getPhase() != SpeedrunnersGame.Phase.ENDING) game.setPhase(SpeedrunnersGame.Phase.STARTED);

		// Spectator Tick
		tm.getSpectators().forEach(spectator -> {
			if(!spectator.isSpectator()) spectator.setGameType(GameType.SPECTATOR);
		});

		// Hunter Tick
		tm.getHunters().forEach(hunter -> {
			PlayerEntity target = GameUtil.getNearestRunner(hunter);

			if(hunter.interactionManager.getGameType() != GameType.SURVIVAL) hunter.setGameType(GameType.SURVIVAL);

			if(game.getPhase() == SpeedrunnersGame.Phase.STARTING) {
				hunter.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 40, 255, false, false));
				hunter.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20, 255, false, false));
			}

			// Seeker Compass Compat
			if (SeekerCompassCompat.isSeekerEnabled()) {
				if(SeekerCompassCompat.getCompasses(hunter).isEmpty()) hunter.inventory.addItemStackToInventory(SeekerCompassCompat.createCompass());
				else if(target == null) SeekerCompassCompat.getCompasses(hunter).forEach(stack -> stack.getOrCreateTag().remove("TrackingEntity"));
				else SeekerCompassCompat.getCompasses(hunter).forEach(stack -> stack.getOrCreateTag().put("TrackingEntity", NBTUtil.func_240626_a_(target.getUniqueID())));
			}

			hunter.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "" + TextFormatting.BOLD + "TRACKING: " + TextFormatting.RESET).append(target == null ? new StringTextComponent("No-one") : target.getDisplayName()), true);
		});

		// Speedrunner Tick
		tm.getSpeedrunners().forEach(speedrunner -> {
			if(speedrunner.interactionManager.getGameType() != GameType.SURVIVAL) speedrunner.setGameType(GameType.SURVIVAL);
		});

		if(!tm.getSpeedrunners().findAny().isPresent() && game.isRunning()) {
			game.stop();
		}
	}

	/*
	 * Prevent dead players from talking to speedrunners.
	 */
	@SubscribeEvent
	public static void onEvent(ServerChatEvent event) {
		TeamManager manager = SpeedrunnersGame.getInstance().getTeamManager();
		ServerPlayerEntity player = event.getPlayer();
		if (!manager.isPlaying(player)) {
			UUID sender = player.getUniqueID();
			manager.getSpectators().forEach(spectator -> spectator.sendMessage(new StringTextComponent(TextFormatting.GRAY + "[SPECTATOR] " + TextFormatting.RESET).append(event.getComponent()), sender));
		}
	}

	/*
	 * Set player name color based on role.
	 */
	@SubscribeEvent
	public static void onEvent(PlayerEvent.NameFormat event) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager manager = game.getTeamManager();
		PlayerEntity player = event.getPlayer();

		if (!(player instanceof ServerPlayerEntity)) return;
		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
		TeamManager.Role role = manager.getRole(serverPlayer);

		event.setDisplayname(event.getDisplayname().deepCopy().modifyStyle(style -> style.setColor(Color.func_240743_a_(role.getColor()))));
	}

	/*
	 * Remove all hunter items
	 */
	@SubscribeEvent
	public static void onEvent(LivingDropsEvent event) {
		event.getDrops().removeIf(itemEntity -> itemEntity.getItem().getTag() != null && itemEntity.getItem().getTag().getBoolean("Hunter"));
	}

	/*
	 * Set a speedrunner to spectator when respawning.
	 */
	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerRespawnEvent event) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager manager = game.getTeamManager();
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

		if (event.isEndConquered() && manager.getRole(player) == TeamManager.Role.SPEEDRUNNER) {
			game.setRunning(false);
			game.stop();
			return;
		}

		if (manager.getRole(player) == TeamManager.Role.SPEEDRUNNER)
			manager.setRole(player, TeamManager.Role.SPECTATOR);
	}

	@SubscribeEvent
	public static void onEvent(LivingHurtEvent event) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();

		if(!game.isRunning() || game.getPhase() == SpeedrunnersGame.Phase.STARTING)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEvent(EntityTravelToDimensionEvent event) {
		if(event.getEntity() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();

			TRAVELLING_MAP.put(player.getUniqueID(), true);
		}
	}

	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
		if(event.getEntity() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();

			TRAVELLING_MAP.remove(player.getUniqueID());
		}
	}

	private static boolean isTravelling(ServerPlayerEntity player) {
		return TRAVELLING_MAP.containsKey(player.getUniqueID());
	}
}
