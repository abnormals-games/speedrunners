package me.jaackson.speedrunners.game.util;

import me.jaackson.speedrunners.SpeedRunners;
import me.jaackson.speedrunners.SpeedRunnersConfig;
import me.jaackson.speedrunners.game.manager.team.Teams;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SWorldSpawnChangedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.List;

public class GameHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean isHunterCompass(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains("IsHunterCompass") & stack.getTag().getBoolean("IsHunterCompass");
    }

    public static ItemStack getHunterCompass(PlayerEntity player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (isHunterCompass(stack)) {
                return stack;
            }
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            if (isHunterCompass(stack)) {
                return stack;
            }
        }
        return null;
    }

    public static void setCompassPos(ServerPlayerEntity player, BlockPos pos) {
        player.connection.sendPacket(new SWorldSpawnChangedPacket(pos));
//        if (pos != null)
//            nbt.put("LodestonePos", NBTUtil.writeBlockPos(pos));
//        else
//            nbt.remove("LodestonePos");
//
//        if (dimensionType != null)
//            World.CODEC.encodeStart(NBTDynamicOps.INSTANCE, dimensionType).resultOrPartial(LOGGER::error).ifPresent((dimension) -> nbt.put("LodestoneDimension", dimension));
//        else
//            nbt.remove("LodestoneDimension");
//
//        nbt.putBoolean("LodestoneTracked", nbt.contains("LodestonePos") && nbt.contains("LodestoneDimension"));
    }

    public static PlayerEntity getNearestRunner(PlayerEntity player) {
        List<ServerPlayerEntity> players = SpeedRunners.getServer().getPlayerList().getPlayers();

        double distNear = 0.0D;
        PlayerEntity playerNear = null;
        for (PlayerEntity player2 : players) {
            if (player == player2) {
                continue;
            }
            if (players.stream().noneMatch(p -> p.getUniqueID().equals(player2.getUniqueID()) && p.getTeam() == SpeedRunners.getTeamManager().getTeam(Teams.RUNNER)))
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

    public static void setupBorder(MinecraftServer server) {
        World world = server.getWorld(World.OVERWORLD);
        if (world == null)
            return;

        world.getWorldBorder().setCenter(world.getWorldInfo().getSpawnX(), world.getWorldInfo().getSpawnZ());
        world.getWorldBorder().setSize(SpeedRunnersConfig.INSTANCE.startingBorderRadius.get());
    }
}
