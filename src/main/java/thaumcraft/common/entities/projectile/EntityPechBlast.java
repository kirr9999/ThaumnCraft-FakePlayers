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
			for (int list = 0; list < 9; ++list)
			{
				float i = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				float entity1 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				float e = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) i, this.posY + (double) entity1, this.posZ + (double) e, this.posX + (double) (i * 8.0F), this.posY + (double) (entity1 * 8.0F), this.posZ + (double) (e * 8.0F), 0.3F, 3, true, 0.02F);
				i = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				entity1 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				e = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) i, this.posY + (double) entity1, this.posZ + (double) e, this.posX + (double) (i * 8.0F), this.posY + (double) (entity1 * 8.0F), this.posZ + (double) (e * 8.0F), 0.3F, 2, true, 0.02F);
				i = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				entity1 = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				e = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.3F;
				Thaumcraft.proxy.wispFX3(this.worldObj, this.posX + (double) i, this.posY + (double) entity1, this.posZ + (double) e, this.posX + (double) (i * 8.0F), this.posY + (double) (entity1 * 8.0F), this.posZ + (double) (e * 8.0F), 0.3F, 0, true, 0.02F);
			}
		}

		if (!this.worldObj.isRemote)
		{
			List var7 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.getThrower(), this.boundingBox.expand(2.0D, 2.0D, 2.0D));

			for (int var8 = 0; var8 < var7.size(); ++var8)
			{
				Entity var9 = (Entity) var7.get(var8);
				// TODO gamerforEA code start
				if (var9 instanceof EntityPlayer) continue;
				// TODO gamerforEA code end
				if (!(var9 instanceof EntityPech) && var9 instanceof EntityLivingBase)
				{
					((EntityLivingBase) var9).attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float) (this.strength + 2));

					try
					{
						if (this.nightshade)
						{
							((EntityLivingBase) var9).addPotionEffect(new PotionEffect(Potion.poison.id, 100 + this.duration * 40, this.strength));
							((EntityLivingBase) var9).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100 + this.duration * 40, this.strength + 1));
							((EntityLivingBase) var9).addPotionEffect(new PotionEffect(Potion.weakness.id, 100 + this.duration * 40, this.strength));
						}
						else
						{
							switch (this.rand.nextInt(3))
							{
								case 0:
									((EntityLivingBase) var9).addPotionEffect(new PotionEffect(Potion.poison.id, 100 + this.duration * 40, this.strength));
									break;
								case 1:
									((EntityLivingBase) var9).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100 + this.duration * 40, this.strength + 1));
									break;
								case 2:
									((EntityLivingBase) var9).addPotionEffect(new PotionEffect(Potion.weakness.id, 100 + this.duration * 40, this.strength));
							}
						}
					}
					catch (Exception e)
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