package me.jaackson.speedrunners;

import me.jaackson.speedrunners.game.SpeedrunnersGame;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(Speedrunners.MOD_ID)
public class Speedrunners {
	public static final String MOD_ID = "speedrunners";

	public Speedrunners() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::setup);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(FMLCommonSetupEvent event) {}

    @SubscribeEvent
    public void setupServer(FMLServerStartedEvent event) {
		new SpeedrunnersGame(event.getServer());
	}
}
