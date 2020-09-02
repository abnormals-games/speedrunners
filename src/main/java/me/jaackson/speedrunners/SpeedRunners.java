package me.jaackson.speedrunners;

import me.jaackson.speedrunners.game.manager.ScoreboardManager;
import me.jaackson.speedrunners.game.manager.team.TeamManager;
import me.jaackson.speedrunners.game.util.GameHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(SpeedRunners.MOD_ID)
public class SpeedRunners
{
	public static final String MOD_ID = "speedrunners";
	private static TeamManager teamManager;
    private static MinecraftServer server;

    public SpeedRunners() {
    	IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::crashClient);
        modBus.addListener(this::setup);

        SpeedRunnersConfig.init(ModLoadingContext.get());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void setupServerPre(FMLServerAboutToStartEvent event)
    {
        server = event.getServer();
    }

    @SubscribeEvent
    public void setupServer(FMLServerStartedEvent event)
    {
        if(SpeedRunnersConfig.INSTANCE.enabled.get()) {
            teamManager = new TeamManager(event.getServer());
            ScoreboardManager.createScoreboard();
//            GameHelper.setupBorder(event.getServer());
        }
    }

    private void setup(FMLCommonSetupEvent event) {
        SharedConstants.developmentMode = true;
    }

    private void crashClient(FMLClientSetupEvent event) 
    {
        throw new UnsupportedOperationException("This mod is not meant to be run on the client, please start this on a dedicated server.");
    }

    public static MinecraftServer getServer()
    {
        return server;
    }

    public static TeamManager getTeamManager()
    {
        return teamManager;
    }


}
