package com.gamerforea.thaumcraft;

import java.lang.ref.WeakReference;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gamerforea.wgew.cauldron.event.CauldronBlockBreakEvent;
import com.gamerforea.wgew.cauldron.event.CauldronEntityDamageByEntityEvent;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public final class FakePlayerUtils
{
	private static WeakReference<FakePlayer> player = new WeakReference<FakePlayer>(null);

	public static final FakePlayer getModFake(World world)
	{
		FakePlayer fake = player.get();

		if (fake == null)
		{
			GameProfile profile = new GameProfile(UUID.fromString("745dd166-13e9-41db-999d-6af5bacba7fd"), "[ThaumCraft]");
			player = new WeakReference<FakePlayer>(fake = create(world, profile));
		}
		else
			fake.worldObj = world;

		return fake;
	}

	public static final FakePlayer create(World world, GameProfile profile)
	{
		return FakePlayerFactory.get((WorldServer) world, profile);
	}

	public static final boolean cantBreak(int x, int y, int z, EntityPlayer player)
	{
		try
		{
			CauldronBlockBreakEvent event = new CauldronBlockBreakEvent(player, x, y, z);
			Bukkit.getServer().getPluginManager().callEvent(event);
			return event.getBukkitEvent().isCancelled();
		}
		catch (Throwable throwable)
		{
			GameProfile profile = player.getGameProfile();
			System.err.println(String.format("Failed call CauldronBlockBreakEvent [Name: %s, UUID: %s, X: %d, Y: %d, Z: %d]", profile.getName(), profile.getId().toString(), x, y, z));
			return true;
		}
	}

	public static final boolean cantDamage(Entity damager, Entity damagee)
	{
		try
		{
			CauldronEntityDamageByEntityEvent event = new CauldronEntityDamageByEntityEvent(damager, damagee, DamageCause.ENTITY_ATTACK, 1D);
			Bukkit.getServer().getPluginManager().callEvent(event);
			return event.getBukkitEvent().isCancelled();
		}
		catch (Throwable throwable)
		{
			System.err.println(String.format("Failed call CauldronEntityDamageByEntityEvent [Damager UUID: %s, Damagee UUID: %s]", damager.getUniqueID().toString(), damagee.getUniqueID().toString()));
			return true;
		}
	}
}