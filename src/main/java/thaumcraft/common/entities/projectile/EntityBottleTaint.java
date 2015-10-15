package thaumcraft.common.entities.projectile;

import java.util.List;

import com.gamerforea.eventhelper.util.EventUtils;
import com.gamerforea.eventhelper.util.FastUtils;
import com.gamerforea.thaumcraft.ModUtils;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class EntityBottleTaint extends EntityThrowable
{
	public EntityBottleTaint(World p_i1788_1_)
	{
		super(p_i1788_1_);
	}

	public EntityBottleTaint(World p_i1790_1_, EntityLivingBase p_i1790_2)
	{
		super(p_i1790_1_, p_i1790_2);
	}

	@Override
	protected float getGravityVelocity()
	{
		return 0.05F;
	}

	@Override
	protected float func_70182_d()
	{
		return 0.5F;
	}

	@Override
	protected float func_70183_g()
	{
		return -20.0F;
	}

	@Override
	protected void onImpact(MovingObjectPosition p_70184_1_)
	{
		if (!this.worldObj.isRemote)
		{
			// TODO gamerforEA code start
			EntityPlayer player = FastUtils.getThrowerPlayer(this, ModUtils.profile);
			// TODO gamerforEA cod end

			List<EntityLivingBase> entities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).expand(5.0D, 5.0D, 5.0D));
			if (entities.size() > 0)
				for (EntityLivingBase entity : entities)
					if (!(entity instanceof ITaintedMob) && !entity.isEntityUndead())
					{
						// TODO gamerforEA code start
						if (EventUtils.cantDamage(player, entity))
							continue;
						// TODO gamerforEA cod end

						entity.addPotionEffect(new PotionEffect(Config.potionTaintPoisonID, 100, 0, false));
					}

			int x = (int) this.posX;
			int y = (int) this.posY;
			int z = (int) this.posZ;

			for (int i = 0; i < 10; ++i)
			{
				int chunkX = x + (int) ((this.rand.nextFloat() - this.rand.nextFloat()) * 5.0F);
				int chunkZ = z + (int) ((this.rand.nextFloat() - this.rand.nextFloat()) * 5.0F);

				// TODO gamerforEA code start
				if (EventUtils.cantBreak(player, chunkX, y, chunkZ))
					continue;
				// TODO gamerforEA code end

				if (this.worldObj.rand.nextBoolean() && this.worldObj.getBiomeGenForCoords(chunkX, chunkZ) != ThaumcraftWorldGenerator.biomeTaint)
				{
					Utils.setBiomeAt(this.worldObj, chunkX, chunkZ, ThaumcraftWorldGenerator.biomeTaint);
					if (this.worldObj.isBlockNormalCubeDefault(chunkX, y - 1, chunkZ, false) && this.worldObj.getBlock(chunkX, y, chunkZ).isReplaceable(this.worldObj, chunkX, y, chunkZ))
						this.worldObj.setBlock(chunkX, y, chunkZ, ConfigBlocks.blockTaintFibres, 0, 3);
				}
			}

			this.setDead();
		}
		else
		{
			for (int i = 0; i < Thaumcraft.proxy.particleCount(100); ++i)
				Thaumcraft.proxy.taintsplosionFX(this);

			Thaumcraft.proxy.bottleTaintBreak(this.worldObj, this.posX, this.posY, this.posZ);
		}
	}
}