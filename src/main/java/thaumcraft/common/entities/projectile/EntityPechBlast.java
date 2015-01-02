package thaumcraft.common.entities.projectile;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.monster.EntityPech;

public class EntityPechBlast extends EntityThrowable
{
	int strength = 0;
	int duration = 0;
	boolean nightshade = false;

	public EntityPechBlast(World par1World)
	{
		super(par1World);
	}

	public EntityPechBlast(World par1World, EntityLivingBase par2EntityLiving, int strength, int duration, boolean nightshade)
	{
		super(par1World, par2EntityLiving);
		this.strength = strength;
		this.nightshade = nightshade;
		this.duration = duration;
	}

	public EntityPechBlast(World par1World, double par2, double par4, double par6, int strength, int duration, boolean nightshade)
	{
		super(par1World, par2, par4, par6);
		this.strength = strength;
		this.nightshade = nightshade;
		this.duration = duration;
	}

	@Override
	protected float getGravityVelocity()
	{
		return 0.025F;
	}

	@Override
	protected float func_70182_d()
	{
		return 1.5F;
	}

	@Override
	public void onUpdate()
	{
		if (this.worldObj.isRemote)
		{
			for (int a = 0; a < 3; ++a)
			{
				Thaumcraft.proxy.wispFX2(this.worldObj, this.posX + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), this.posY + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), this.posZ + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F), 0.3F, 3, true, true, 0.02F);
				double x2 = (this.posX + this.prevPosX) / 2.0D + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F);
				double y2 = (this.posY + this.prevPosY) / 2.0D + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F);
				double z2 = (this.posZ + this.prevPosZ) / 2.0D + (double) ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F);
				Thaumcraft.proxy.wispFX2(this.worldObj, x2, y2, z2, 0.3F, 2, true, true, 0.02F);
				Thaumcraft.proxy.sparkle((float) this.posX + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.1F, (float) this.posY + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.1F, (float) this.posZ + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.1F, 5);
			}
		}

		super.onUpdate();
		if (this.ticksExisted > 500)
		{
			this.setDead();
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
	{
		if (this.worldObj.isRemote)
		{
			for (int i = 0; i < 9; ++i)
			{
				float f1 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				float f2 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				float f3 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) f1, this.posY + (double) f2, this.posZ + (double) f3, this.posX + (double) (f1 * 8.0F), this.posY + (double) (f2 * 8.0F), this.posZ + (double) (f3 * 8.0F), 0.3F, 3, true, 0.02F);
				f1 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				f2 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				f3 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) f1, this.posY + (double) f2, this.posZ + (double) f3, this.posX + (double) (f1 * 8.0F), this.posY + (double) (f2 * 8.0F), this.posZ + (double) (f3 * 8.0F), 0.3F, 2, true, 0.02F);
				f1 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				f2 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				f3 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) f1, this.posY + (double) f2, this.posZ + (double) f3, this.posX + (double) (f1 * 8.0F), this.posY + (double) (f2 * 8.0F), this.posZ + (double) (f3 * 8.0F), 0.3F, 0, true, 0.02F);
			}
		}

		if (!this.worldObj.isRemote)
		{
			for (Entity entity : (List<Entity>) this.worldObj.getEntitiesWithinAABBExcludingEntity(this.getThrower(), this.boundingBox.expand(2.0D, 2.0D, 2.0D)))
			{
				// TODO gamerforEA code start
				if (entity instanceof EntityPlayer) continue;
				// TODO gamerforEA code end
				if (!(entity instanceof EntityPech) && entity instanceof EntityLivingBase)
				{
					((EntityLivingBase) entity).attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float) (this.strength + 2));

					try
					{
						if (this.nightshade)
						{
							((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.poison.id, 100 + this.duration * 40, this.strength));
							((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100 + this.duration * 40, this.strength + 1));
							((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.weakness.id, 100 + this.duration * 40, this.strength));
						}
						else
						{
							switch (this.rand.nextInt(3))
							{
								case 0:
									((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.poison.id, 100 + this.duration * 40, this.strength));
									break;
								case 1:
									((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100 + this.duration * 40, this.strength + 1));
									break;
								case 2:
									((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.weakness.id, 100 + this.duration * 40, this.strength));
							}
						}
					}
					catch (Exception var6)
					{
					}
				}
			}

			this.setDead();
		}
	}

	@Override
	public float getShadowSize()
	{
		return 0.1F;
	}
}