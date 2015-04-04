package thaumcraft.common.entities.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;

import com.gamerforea.thaumcraft.FakePlayerUtils;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityFrostShard extends EntityThrowable implements IEntityAdditionalSpawnData
{
	public double bounce = 0.5D;
	public int bounceLimit = 3;
	public boolean fragile = false;

	public EntityFrostShard(World par1World)
	{
		super(par1World);
	}

	public EntityFrostShard(World par1World, EntityLivingBase par2EntityLiving, float scatter)
	{
		super(par1World, par2EntityLiving);
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, this.func_70182_d(), scatter);
	}

	@Override
	protected float getGravityVelocity()
	{
		return this.fragile ? 0.015F : 0.05F;
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		data.writeDouble(this.bounce);
		data.writeInt(this.bounceLimit);
		data.writeBoolean(this.fragile);
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		this.bounce = data.readDouble();
		this.bounceLimit = data.readInt();
		this.fragile = data.readBoolean();
	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		// TODO gamerforEA code start
		if (mop.entityHit != null)
		{
			if (this.getThrower() != null)
			{
				if (FakePlayerUtils.callEntityDamageByEntityEvent(this.getThrower(), mop.entityHit, DamageCause.ENTITY_ATTACK, this.getDamage()).isCancelled())
				{
					this.setDead();
					return;
				}
			}
			else
			{
				this.setDead();
				return;
			}
		}
		// TODO gamerforEA code end
		int a;
		int a1;
		if (mop.entityHit != null)
		{
			int var20 = MathHelper.floor_double(this.posX) - MathHelper.floor_double(mop.entityHit.posX);
			a = MathHelper.floor_double(this.posY) - MathHelper.floor_double(mop.entityHit.posY);
			a1 = MathHelper.floor_double(this.posZ) - MathHelper.floor_double(mop.entityHit.posZ);
			if (a1 != 0)
			{
				this.motionZ *= -1.0D;
			}

			if (var20 != 0)
			{
				this.motionX *= -1.0D;
			}

			if (a != 0)
			{
				this.motionY *= -0.9D;
			}

			this.motionX *= 0.66D;
			this.motionY *= 0.66D;
			this.motionZ *= 0.66D;

			for (int my = 0; (float) my < this.getDamage(); ++my)
			{
				this.worldObj.spawnParticle("blockcrack_" + Block.getIdFromBlock(ConfigBlocks.blockCustomOre) + "_15", this.posX, this.posY, this.posZ, 4.0D * ((double) this.rand.nextFloat() - 0.5D), 0.5D, ((double) this.rand.nextFloat() - 0.5D) * 4.0D);
			}
		}
		else if (mop.typeOfHit == MovingObjectType.BLOCK)
		{
			ForgeDirection var10 = ForgeDirection.getOrientation(mop.sideHit);
			if (var10.offsetZ != 0)
			{
				this.motionZ *= -1.0D;
			}

			if (var10.offsetX != 0)
			{
				this.motionX *= -1.0D;
			}

			if (var10.offsetY != 0)
			{
				this.motionY *= -0.9D;
			}

			Block var12 = this.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);

			try
			{
				this.playSound(var12.stepSound.getBreakSound(), 0.3F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
			}
			catch (Exception var9)
			{
				;
			}

			for (a1 = 0; (float) a1 < this.getDamage(); ++a1)
			{
				this.worldObj.spawnParticle("blockcrack_" + Block.getIdFromBlock(var12) + "_" + this.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ), this.posX, this.posY, this.posZ, 4.0D * ((double) this.rand.nextFloat() - 0.5D), 0.5D, ((double) this.rand.nextFloat() - 0.5D) * 4.0D);
			}
		}

		this.motionX *= this.bounce;
		this.motionY *= this.bounce;
		this.motionZ *= this.bounce;
		float var11 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		this.posX -= this.motionX / (double) var11 * 0.05000000074505806D;
		this.posY -= this.motionY / (double) var11 * 0.05000000074505806D;
		this.posZ -= this.motionZ / (double) var11 * 0.05000000074505806D;
		this.setBeenAttacked();
		if (!this.worldObj.isRemote && mop.entityHit != null)
		{
			double var13 = mop.entityHit.motionX;
			double var14 = mop.entityHit.motionY;
			double mz = mop.entityHit.motionZ;
			mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), this.getDamage());
			if (mop.entityHit instanceof EntityLivingBase && this.getFrosty() > 0)
			{
				((EntityLivingBase) mop.entityHit).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, this.getFrosty() - 1));
			}

			if (this.fragile)
			{
				mop.entityHit.hurtResistantTime = 0;
				this.setDead();
				this.playSound(Blocks.ice.stepSound.getBreakSound(), 0.3F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
				mop.entityHit.motionX = var13 + (mop.entityHit.motionX - var13) / 10.0D;
				mop.entityHit.motionY = var14 + (mop.entityHit.motionY - var14) / 10.0D;
				mop.entityHit.motionZ = mz + (mop.entityHit.motionZ - mz) / 10.0D;
			}
		}

		if (this.bounceLimit-- <= 0)
		{
			this.setDead();
			this.playSound(Blocks.ice.stepSound.getBreakSound(), 0.3F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

			for (a = 0; (float) a < 8.0F * this.getDamage(); ++a)
			{
				this.worldObj.spawnParticle("blockcrack_" + Block.getIdFromBlock(ConfigBlocks.blockCustomOre) + "_15", this.posX, this.posY, this.posZ, 4.0D * ((double) this.rand.nextFloat() - 0.5D), 0.5D, ((double) this.rand.nextFloat() - 0.5D) * 4.0D);
			}
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		float var20;
		if (this.worldObj.isRemote && this.getFrosty() > 0)
		{
			var20 = this.getDamage() / 10.0F;

			for (int a = 0; a < this.getFrosty(); ++a)
			{
				Thaumcraft.proxy.sparkle((float) this.posX - var20 + this.rand.nextFloat() * var20 * 2.0F, (float) this.posY - var20 + this.rand.nextFloat() * var20 * 2.0F, (float) this.posZ - var20 + this.rand.nextFloat() * var20 * 2.0F, 0.4F, 6, 0.005F);
			}
		}

		var20 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.141592653589793D);

		for (this.rotationPitch = (float) (Math.atan2(this.motionY, (double) var20) * 180.0D / 3.141592653589793D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
		{
			;
		}

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
		{
			this.prevRotationPitch += 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw < -180.0F)
		{
			this.prevRotationYaw -= 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
		{
			this.prevRotationYaw += 360.0F;
		}

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setFloat("damage", this.getDamage());
		par1NBTTagCompound.setBoolean("fragile", this.fragile);
		par1NBTTagCompound.setInteger("frost", this.getFrosty());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT(par1NBTTagCompound);
		this.setDamage(par1NBTTagCompound.getFloat("damage"));
		this.fragile = par1NBTTagCompound.getBoolean("fragile");
		this.setFrosty(par1NBTTagCompound.getInteger("frost"));
	}

	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getShadowSize()
	{
		return 0.0F;
	}

	@Override
	public void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, new Float(0.0F));
		this.dataWatcher.addObject(17, new Byte((byte) 0));
	}

	public void setDamage(float par1)
	{
		this.dataWatcher.updateObject(16, Float.valueOf(par1));
		this.setSize(0.15F + par1 * 0.15F, 0.15F + par1 * 0.15F);
	}

	public float getDamage()
	{
		return this.dataWatcher.getWatchableObjectFloat(16);
	}

	public void setFrosty(int frosty)
	{
		this.dataWatcher.updateObject(17, Byte.valueOf((byte) frosty));
	}

	public int getFrosty()
	{
		return this.dataWatcher.getWatchableObjectByte(17);
	}
}