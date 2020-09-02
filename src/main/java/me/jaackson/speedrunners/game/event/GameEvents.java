package me.jaackson.speedrunners.game.event;

import me.jaackson.speedrunners.SpeedRunners;
import me.jaackson.speedrunners.game.manager.team.Teams;
import me.jaackson.speedrunners.game.util.GameHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

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
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (!(player.getTeam() == SpeedRunners.getTeamManager().getTeam(Teams.HUNTER)) && !(player.getTeam() == SpeedRunners.getTeamManager().getTeam(Teams.DEAD))) {
                Scoreboard scoreboard = player.getWorldScoreboard();
                scoreboard.addPlayerToTeam(player.getScoreboardName(), SpeedRunners.getTeamManager().getTeam(Teams.RUNNER));
            }
        }
    }

    @SubscribeEvent
    public static void onEvent(TickEvent.ServerTickEvent event) {
        if (event.side == LogicalSide.CLIENT)
            return;

        for (ServerPlayerEntity player : SpeedRunners.getServer().getPlayerList().getPlayers()) {
            ItemStack stack = GameHelper.getHunterCompass(player);
            if (stack == null) return;

            if (player.getTeam() == SpeedRunners.getTeamManager().getTeam(Teams.RUNNER))
                GameHelper.setCompassPos(player, new BlockPos(player.world.getWorldInfo().getSpawnX(), player.world.getWorldInfo().getSpawnY(), player.world.getWorldInfo().getSpawnZ()));
            else if (player.getTeam() == SpeedRunners.getTeamManager().getTeam(Teams.HUNTER)) {
                PlayerEntity nearestPlayer = GameHelper.getNearestRunner(player);
                if (nearestPlayer != null) {
                    GameHelper.setCompassPos(player, nearestPlayer.getPosition());
                    stack.addEnchantment(Enchantments.UNBREAKING, 1);
                } else {
                    float angle = (float) (Math.random() * Math.PI * 2);
                    float dx = (float) (Math.cos(angle) * 5);
                    float dz = (float) (Math.sin(angle) * 5);

                    BlockPos randPos = new BlockPos(dx, 0, dz);
                    GameHelper.setCompassPos(player, player.getPosition().add(randPos));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEvent(ItemTossEvent event) {
//        ItemStack stack = event.getEntityItem().getItem();
//
//        if (GameHelper.isHunterCompass(stack)) {
//            event.getEntityItem().getItem().
//        }
    }
}
