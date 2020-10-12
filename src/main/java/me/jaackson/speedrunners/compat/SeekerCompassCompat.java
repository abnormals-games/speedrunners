package me.jaackson.speedrunners.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;
import smelly.seekercompass.SeekerCompass;
import smelly.seekercompass.SeekerCompassItem;

import java.util.HashSet;
import java.util.Set;

public class SeekerCompassCompat {

	public static boolean isSeekerEnabled() {
		return ModList.get().isLoaded("seeker_compass"); //put config check here
	}

	public static ItemStack createCompass() {
		ItemStack stack = new ItemStack(SeekerCompass.SEEKER_COMPASS.get());
		CompoundNBT tag = stack.getOrCreateTag();
		tag.putBoolean("Hunter", true);
		tag.putBoolean("TrackingOnly", true);
		tag.putBoolean("Unbreakable", true);

		stack.setDisplayName(new StringTextComponent("Hunter's Compass").mergeStyle(TextFormatting.RED));

		return stack;
	}

	public static boolean isHuntingSeekerCompass(ItemStack stack) {
		return stack.getTag() != null && stack.getTag().getBoolean("TrackingOnly") && stack.getItem() instanceof SeekerCompassItem;
	}

	public static Set<ItemStack> getCompasses(ServerPlayerEntity player) {
		Set<ItemStack> compasses = new HashSet<>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty() && isHuntingSeekerCompass(stack)) {
				compasses.add(stack);
			}
		}

		return compasses;
	}
}
