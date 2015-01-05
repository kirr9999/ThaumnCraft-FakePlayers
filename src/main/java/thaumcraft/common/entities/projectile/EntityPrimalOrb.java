package thaumcraft.common.entities.projectile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityPrimalOrb extends EntityThrowable implements IEntityAdditionalSpawnData
{
	int count = 0;
	boolean seeker = false;
	int oi = 0;

	public EntityPrimalOrb(World par1World)
	{
		super(par1World);
	}

	public EntityPrimalOrb(World par1World, EntityLivingBase par2EntityLiving, boolean seeker)
	{
		super(par1World, par2EntityLiving);
		this.seeker = seeker;
		this.oi = par2EntityLiving.getEntityId();
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		data.writeBoolean(this.seeker);
		data.writeInt(this.oi);
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		this.seeker = data.readBoolean();
		this.oi = data.readInt();
	}

	@Override
	protected float getGravityVelocity()
	{
		return 0.001F;
	}

	@Override
	protected float func_70182_d()
	{
		return 0.5F;
	}

	@Override
	public void onUpdate()
	{
		// TODO gamerforEA code start
		if (this.worldObj != null)
		{
			this.setDead();
			return;
		}
		// TODO gamerforEA code end
		++this.count;
		if (this.isInsideOfMaterial(Material.portal))
		{
			this.onImpact(new MovingObjectPosition(this));
		}

		if (this.worldObj.isRemote)
		{
			for (int rr = 0; rr < 6; ++rr)
			{
				Thaumcraft.proxy.wispFX4(this.worldObj, (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), this, rr, true, 0.0F);
			}

			Thaumcraft.proxy.wispFX2(this.worldObj, this.posX + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), this.posY + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), this.posZ + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), 0.1F, this.rand.nextInt(6), true, true, 0.0F);
		}

		Random var14 = new Random((long) (this.getEntityId() + this.count));
		if (this.ticksExisted > 20)
		{
			if (!this.seeker)
			{
				this.motionX += (double) ((var14.nextFloat() - var14.nextFloat()) * 0.01F);
				this.motionY += (double) ((var14.nextFloat() - var14.nextFloat()) * 0.01F);
				this.motionZ += (double) ((var14.nextFloat() - var14.nextFloat()) * 0.01F);
			}
			else
			{
				ArrayList l = EntityUtils.getEntitiesInRange(this.worldObj, this.posX, this.posY, this.posZ, this, EntityLivingBase.class, 16.0D);
				double d = Double.MAX_VALUE;
				Entity t = null;
				Iterator dx = l.iterator();

				double dy;
				while (dx.hasNext())
				{
					Entity e = (Entity) dx.next();
					if (e.getEntityId() != this.oi && !e.isDead)
					{
						dy = this.getDistanceSqToEntity(e);
						if (dy < d)
						{
							d = dy;
							t = e;
						}
					}
				}

				if (t != null)
				{
					double var15 = t.posX - this.posX;
					dy = t.boundingBox.minY + (double) t.height * 0.9D - this.posY;
					double dz = t.posZ - this.posZ;
					double d13 = 0.2D;
					var15 /= d;
					dy /= d;
					dz /= d;
					this.motionX += var15 * d13;
					this.motionY += dy * d13;
					this.motionZ += dz * d13;
					this.motionX = (double) MathHelper.clamp_float((float) this.motionX, -0.2F, 0.2F);
					this.motionY = (double) MathHelper.clamp_float((float) this.motionY, -0.2F, 0.2F);
					this.motionZ = (double) MathHelper.clamp_float((float) this.motionZ, -0.2F, 0.2F);
				}
			}
		}

		super.onUpdate();
		if (this.ticksExisted > 5000)
		{
			this.setDead();
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		if (this.worldObj.isRemote)
		{
			for (int specialchance = 0; specialchance < 6; ++specialchance)
			{
				for (int expl = 0; expl < 6; ++expl)
				{
					float fx = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.5F;
					float fy = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.5F;
					float fz = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.5F;
					Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) fx, this.posY + (double) fy, this.posZ + (double) fz, this.posX + (double) (fx * 10.0F), this.posY + (double) (fy * 10.0F), this.posZ + (double) (fz * 10.0F), 0.4F, expl, true, 0.05F);
				}
			}
		}

		if (!this.worldObj.isRemote)
		{
			float var7 = 1.0F;
			float var8 = 2.0F;
			if (mop.typeOfHit == MovingObjectType.BLOCK && this.isInsideOfMaterial(Material.portal))
			{
				var8 = 4.0F;
				var7 = 10.0F;
			}

			this.worldObj.createExplosion((Entity) null, this.posX, this.posY, this.posZ, var8, true);
			if (!this.seeker && (float) this.rand.nextInt(100) <= var7)
			{
				if (this.rand.nextBoolean())
				{
					this.taintSplosion();
				}
				else
				{
					ThaumcraftWorldGenerator.createRandomNodeAt(this.worldObj, mop.blockX, mop.blockY, mop.blockZ, this.rand, false, false, true);
				}
			}

			this.setDead();
		}
	}

	public void taintSplosion()
	{
		int x = (int) this.posX;
		int y = (int) this.posY;
		int z = (int) this.posZ;

		for (int a = 0; a < 10; ++a)
		{
			int xx = x + (int) (this.rand.nextFloat() - this.rand.nextFloat() * 6.0F);
			int zz = z + (int) (this.rand.nextFloat() - this.rand.nextFloat() * 6.0F);
			if (this.rand.nextBoolean() && this.worldObj.getBiomeGenForCoords(xx, zz) != ThaumcraftWorldGenerator.biomeTaint)
			{
				Utils.setBiomeAt(this.worldObj, xx, zz, ThaumcraftWorldGenerator.biomeTaint);
				int yy = this.worldObj.getHeightValue(xx, zz);
				if (!this.worldObj.isAirBlock(xx, yy - 1, zz))
				{
					this.worldObj.setBlock(xx, yy, zz, ConfigBlocks.blockTaintFibres, 0, 3);
				}
			}
		}

	}

	@Override
	public float getShadowSize()
	{
		return 0.1F;
	}
}