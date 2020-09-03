package me.jaackson.speedrunners.game.event;

import me.jaackson.speedrunners.SpeedRunners;
import me.jaackson.speedrunners.game.manager.team.TeamManager;
import me.jaackson.speedrunners.game.manager.team.Teams;
import me.jaackson.speedrunners.game.util.GameHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = SpeedRunners.MOD_ID)
public class GameEvents {

    @SubscribeEvent
    public static void onEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (player.getTeam() == SpeedRunners.getTeamManager().getTeam(Teams.RUNNER) && player.interactionManager.getGameType().isSurvivalOrAdventure()) {
                SpeedRunners.getTeamManager().setDead(player, true);
            }
        }
    }

    @SubscribeEvent
    public static void onEvent(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        TeamManager tm = SpeedRunners.getTeamManager();
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (!(player.getTeam() == tm.getTeam(Teams.HUNTER)) && !(player.getTeam() == tm.getTeam(Teams.DEAD))) {
                Scoreboard scoreboard = player.getWorldScoreboard();
                scoreboard.addPlayerToTeam(player.getScoreboardName(), SpeedRunners.getTeamManager().getTeam(Teams.RUNNER));
            }
        }
    }

    @SubscribeEvent
    public static void onEvent(TickEvent.ServerTickEvent event) {
        for (ServerPlayerEntity player : SpeedRunners.getServer().getPlayerList().getPlayers()) {
            List<ItemStack> compasses = GameHelper.getCompasses(player);
            Team team = player.getTeam();
            TeamManager tm = SpeedRunners.getTeamManager();

            if (compasses.isEmpty()) {
                GameHelper.setCompassPos(player,
                        new BlockPos(
                                player.world.getWorldInfo().getSpawnX(),
                                player.world.getWorldInfo().getSpawnY(),
                                player.world.getWorldInfo().getSpawnZ()
                        ));
                return;
            }

            if (team == tm.getTeam(Teams.HUNTER)) {
                PlayerEntity nearestPlayer = GameHelper.getNearestRunner(player);

                for (ItemStack stack : compasses) GameHelper.createHunterCompass(stack);

                if (nearestPlayer != null) {
                    ITextComponent tracking = new StringTextComponent(TextFormatting.BOLD + "TRACKING ").mergeStyle(TextFormatting.RED).append(new StringTextComponent("" + TextFormatting.RESET)).append(nearestPlayer.getDisplayName());
                    GameHelper.setCompassPos(player, nearestPlayer.getPosition());
                    player.connection.sendPacket(new STitlePacket(STitlePacket.Type.ACTIONBAR, tracking));

                } else GameHelper.makeCompassGoBatshitCrazy(player);
            } else {
                for (ItemStack stack : compasses) GameHelper.clearHunterCompass(stack);
                GameHelper.setCompassPos(player,
                        new BlockPos(
                                player.world.getWorldInfo().getSpawnX(),
                                player.world.getWorldInfo().getSpawnY(),
                                player.world.getWorldInfo().getSpawnZ()
                        ));
            }
        }
    }

    @SubscribeEvent
    public static void onEvent(ItemTossEvent event) {
        ItemStack stack = event.getEntityItem().getItem();
        if (GameHelper.isHunterCompass(stack)) {
            GameHelper.clearHunterCompass(stack);
            event.getEntityItem().playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + event.getEntityItem().world.rand.nextFloat() * 0.4F);
        }
    }
}
