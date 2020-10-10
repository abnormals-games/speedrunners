package me.jaackson.speedrunners.compat;

import net.minecraftforge.fml.ModList;

public class SeekerCompassCompat {

	public static boolean isSeekerEnabled() {
		return ModList.get().isLoaded("seeker_compass"); //put config check here
	}
}
