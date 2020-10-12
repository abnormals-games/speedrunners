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
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
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

	@SubscribeEvent
	public static void onEvent(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager manager = game.getTeamManager();

		if(!game.isRunning()) {
			GameUtil.resetPlayer(player);
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
				hunter.connection.setPlayerLocation(hunter.getPosX(), hunter.getPosY(), hunter.getPosZ(), 0, 0);
			}

			// Seeker Compass Compat
			if (SeekerCompassCompat.isSeekerEnabled()) {

				//TODO: Prevent compass duplication
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
	 * Remove all hunter items
	 */
	@SubscribeEvent
	public static void onEvent(LivingDropsEvent event) {
		event.getDrops().removeIf(itemEntity -> {
			try {
				return itemEntity.getItem().getTag() != null && itemEntity.getItem().getTag().getBoolean("Hunter");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		});
	}

	@SubscribeEvent
	public static void onEvent(ItemTossEvent event) {
		ItemStack stack = event.getEntityItem().getItem();
		if(SeekerCompassCompat.isSeekerEnabled()) {
			if (SeekerCompassCompat.isHuntingSeekerCompass(stack)) {
				event.getEntityItem().remove();
			}
		}
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
}
