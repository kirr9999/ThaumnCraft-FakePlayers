package com.gamerforea.thaumcraft;

import java.util.UUID;

import com.gamerforea.eventhelper.util.FastUtils;
import com.mojang.authlib.GameProfile;

import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("745dd166-13e9-41db-999d-6af5bacba7fd"), "[ThaumCraft]");
	private static FakePlayer player = null;

	public static final FakePlayer getModFake(World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}
}