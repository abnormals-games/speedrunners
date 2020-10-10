package me.jaackson.speedrunners.game;

import me.jaackson.speedrunners.Speedrunners;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Speedrunners.MOD_ID)
public class EventListener {

	@SubscribeEvent
	public static void onEvent(TickEvent.ServerTickEvent event) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager tm = game.getTeamManager();
		MinecraftServer server = game.getServer();

		
	}

	@SubscribeEvent
	public static void onEvent(ServerChatEvent event) {
		TeamManager manager = SpeedrunnersGame.getInstance().getTeamManager();
		ServerPlayerEntity player = event.getPlayer();
		if(!manager.isPlaying(player)) {
			UUID sender = player.getUniqueID();
			manager.getSpectators().forEach(spectator -> spectator.sendMessage(new StringTextComponent(TextFormatting.GRAY + "[SPECTATOR]" + TextFormatting.RESET).append(event.getComponent()), sender));
		}
	}

	@SubscribeEvent
	public static void onEvent(PlayerEvent.NameFormat event) {
		SpeedrunnersGame game = SpeedrunnersGame.getInstance();
		TeamManager manager = game.getTeamManager();
		PlayerEntity player = event.getPlayer();

		if(!(player instanceof ServerPlayerEntity)) return;
		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
		TeamManager.Role role = manager.getRole(serverPlayer);

		event.setDisplayname(event.getDisplayname().deepCopy().modifyStyle(style -> style.setColor(Color.func_240743_a_(role.getColor()))));
	}
}
