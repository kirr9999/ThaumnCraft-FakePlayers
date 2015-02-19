package com.gamerforea.thaumcraft;

import java.lang.ref.WeakReference;
import java.util.UUID;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.mojang.authlib.GameProfile;

public final class FakePlayerGetter
{
	private static WeakReference<FakePlayer> player = new WeakReference<FakePlayer>(null);

	private static WeakReference<FakePlayer> createNewPlayer(WorldServer world)
	{
		return new WeakReference<FakePlayer>(FakePlayerFactory.get(world, new GameProfile(UUID.fromString("com.gamerforea.thaumcraft"), "[ThaumCraft]")));
	}

	public static final WeakReference<FakePlayer> getPlayer(WorldServer world)
	{
		if (player.get() == null) player = createNewPlayer(world);
		else player.get().worldObj = world;

		return player;
	}
}