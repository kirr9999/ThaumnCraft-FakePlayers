package com.gamerforea.thaumcraft;

import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraftforge.common.util.FakePlayer;

public final class FastUtils
{
	public static final EntityPlayer getThrowerPlayer(EntityThrowable entity)
	{
		EntityLivingBase thrower = entity.getThrower();
		return thrower instanceof EntityPlayer ? (EntityPlayer) thrower : FakePlayerUtils.getModFake(entity.worldObj);
	}

	public static final EntityLivingBase getThrower(EntityThrowable entity)
	{
		EntityLivingBase thrower = entity.getThrower();
		return thrower != null ? thrower : FakePlayerUtils.getModFake(entity.worldObj);
	}

	public static final boolean isOnline(EntityPlayer player)
	{
		if (player instanceof FakePlayer)
			return true;

		List<EntityPlayer> playersOnline = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
		for (EntityPlayer playerOnline : playersOnline)
			if (playerOnline.equals(player))
				return true;

		return false;
	}
}