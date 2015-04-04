package com.gamerforea.thaumcraft;

import java.lang.ref.WeakReference;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gamerforea.wgew.cauldron.event.CauldronBlockBreakEvent;
import com.gamerforea.wgew.cauldron.event.CauldronEntityDamageByEntityEvent;
import com.mojang.authlib.GameProfile;

public final class FakePlayerUtils
{
	private static WeakReference<FakePlayer> player = new WeakReference<FakePlayer>(null);

	public static final FakePlayer getPlayer(World world)
	{
		if (player.get() == null) player = new WeakReference<FakePlayer>(createNewPlayer(world, UUID.fromString("745dd166-13e9-41db-999d-6af5bacba7fd"), "[ThaumCraft]"));
		else player.get().worldObj = world;

		return player.get();
	}

	public static FakePlayer createNewPlayer(World world, GameProfile profile)
	{
		return FakePlayerFactory.get((WorldServer) world, profile);
	}

	public static FakePlayer createNewPlayer(World world, UUID uuid, String name)
	{
		return createNewPlayer(world, new GameProfile(uuid, name));
	}

	public static org.bukkit.event.block.BlockBreakEvent callBlockBreakEvent(int x, int y, int z, EntityPlayer player)
	{
		CauldronBlockBreakEvent event = new CauldronBlockBreakEvent(player, x, y, z);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getBukkitEvent();
	}

	public static org.bukkit.event.entity.EntityDamageByEntityEvent callEntityDamageByEntityEvent(Entity damager, Entity damagee, DamageCause cause, double damage)
	{
		CauldronEntityDamageByEntityEvent event = new CauldronEntityDamageByEntityEvent(damager, damagee, cause, damage);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getBukkitEvent();
	}
}