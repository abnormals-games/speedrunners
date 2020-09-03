package me.jaackson.speedrunners.game.util;

import me.jaackson.speedrunners.SpeedRunners;
import me.jaackson.speedrunners.SpeedRunnersConfig;
import me.jaackson.speedrunners.game.manager.team.Teams;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SWorldSpawnChangedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GameHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void createHunterCompass(ItemStack stack) {
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

    public static List<ItemStack> getCompasses(PlayerEntity player) {
        List<ItemStack> compasses = new ArrayList<>();
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() == Items.COMPASS) {
                compasses.add(stack);
            }
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            if (stack.getItem() == Items.COMPASS) {
                compasses.add(stack);
            }
        }
        return compasses;
    }

    public static void setCompassPos(ServerPlayerEntity player, BlockPos pos) {
        player.connection.sendPacket(new SWorldSpawnChangedPacket(pos));
    }

    public static void makeCompassGoBatshitCrazy(ServerPlayerEntity player) {
        float angle = (float) (Math.random() * Math.PI * 2);
        float dx = (float) (Math.cos(angle) * 5);
        float dz = (float) (Math.sin(angle) * 5);

        BlockPos randPos = new BlockPos(dx, 0, dz);
        player.connection.sendPacket(new SWorldSpawnChangedPacket(player.getPosition().add(randPos)));
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
