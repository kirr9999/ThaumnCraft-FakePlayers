package com.gamerforea.thaumcraft;

import java.lang.ref.WeakReference;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

public final class FakePlayerGetter
{
	private static WeakReference<EntityPlayer> player = new WeakReference<EntityPlayer>(null);

	private static WeakReference<EntityPlayer> createNewPlayer(WorldServer world)
	{
		EntityPlayer player = FakePlayerFactory.get(world, new GameProfile(UUID.fromString("com.gamerforea.thaumcraft"), "[ThaumCraft]"));

		return new WeakReference<EntityPlayer>(player);
	}

	public static final WeakReference<EntityPlayer> getPlayer(WorldServer world)
	{
		if (player.get() == null)
		{
			player = createNewPlayer(world);
		}
		else
		{
			player.get().worldObj = world;
		}

		return player;
	}
}