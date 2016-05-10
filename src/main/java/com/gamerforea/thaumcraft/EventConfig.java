package com.gamerforea.thaumcraft;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

import com.gamerforea.eventhelper.util.FastUtils;

import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static boolean blockEldritchNothing = false;

	static
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("ThaumCraft");
			blockEldritchNothing = cfg.getBoolean("blockEldritchNothing", CATEGORY_GENERAL, blockEldritchNothing, "Включить BlockEldritchNothing");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}