package me.jaackson.speedrunners;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("unused")
public class SpeedRunnersConfig {
    public static final SpeedRunnersConfig INSTANCE;
    private static final ForgeConfigSpec SPEC;

    static {
        Pair<SpeedRunnersConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SpeedRunnersConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public ForgeConfigSpec.BooleanValue enabled;

    private SpeedRunnersConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("SpeedRunners Server Config");
        this.enabled = builder
                .worldRestart()
                .comment("Whether or not SpeedRunners is enabled.")
                .translation("config." + SpeedRunners.MOD_ID + ".enabled")
                .define("enabled", true);
    }

    public static void init(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, SPEC);
    }
}