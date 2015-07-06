package thaumcraft.common.entities.ai.fluid;

import com.gamerforea.thaumcraft.FakePlayerUtils;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillable;

public class AIEssentiaEmpty extends EntityAIBase
{
	private EntityGolemBase theGolem;
	private int jarX;
	private int jarY;
	private int jarZ;
	private ForgeDirection markerOrientation;
	private World theWorld;

	public AIEssentiaEmpty(EntityGolemBase par1EntityCreature)
	{
		this.theGolem = par1EntityCreature;
		this.theWorld = par1EntityCreature.worldObj;
		this.setMutexBits(3);
	}

	public boolean shouldExecute()
	{
		ChunkCoordinates home = this.theGolem.getHomePosition();
		if (this.theGolem.getNavigator().noPath() && this.theGolem.essentia != null && this.theGolem.essentiaAmount != 0)
		{
			ChunkCoordinates jarloc = GolemHelper.findJarWithRoom(this.theGolem);
			if (jarloc == null)
			{
				return false;
			}
			else if (this.theGolem.getDistanceSq((double) jarloc.posX + 0.5D, (double) jarloc.posY + 0.5D, (double) jarloc.posZ + 0.5D) > 4.0D)
			{
				return false;
			}
			else
			{
				this.jarX = jarloc.posX;
				this.jarY = jarloc.posY;
				this.jarZ = jarloc.posZ;
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	public boolean continueExecuting()
	{
		return false;
	}

	public void startExecuting()
	{
		TileEntity tile = this.theWorld.getTileEntity(this.jarX, this.jarY, this.jarZ);
		// TODO gamerforEA code start
		if (tile != null && FakePlayerUtils.cantBreak(this.jarX, this.jarY, this.jarZ, this.theGolem.getFakePlayer())) return;
		// TODO gamerforEA code end
		if (tile instanceof TileJarFillable)
		{
			TileJarFillable fillable = (TileJarFillable) tile;
			this.theGolem.essentiaAmount = fillable.addToContainer(this.theGolem.essentia, this.theGolem.essentiaAmount);
			if (this.theGolem.essentiaAmount == 0)
			{
				this.theGolem.essentia = null;
			}

			this.theWorld.playSoundAtEntity(this.theGolem, "game.neutral.swim", 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
			this.theGolem.updateCarried();
			this.theWorld.markBlockForUpdate(this.jarX, this.jarY, this.jarZ);
		}
		else if (tile instanceof TileEssentiaReservoir)
		{
			TileEssentiaReservoir reservoir = (TileEssentiaReservoir) tile;
			if (reservoir.getSuctionAmount(reservoir.facing) > 0 && (reservoir.getSuctionType(reservoir.facing) == null || reservoir.getSuctionType(reservoir.facing) == this.theGolem.essentia))
			{
				int amount = reservoir.addEssentia(this.theGolem.essentia, this.theGolem.essentiaAmount, reservoir.facing);
				if (amount > 0)
				{
					this.theGolem.essentiaAmount -= amount;
					if (this.theGolem.essentiaAmount == 0)
					{
						this.theGolem.essentia = null;
					}

					this.theWorld.playSoundAtEntity(this.theGolem, "game.neutral.swim", 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
					this.theGolem.updateCarried();
					this.theWorld.markBlockForUpdate(this.jarX, this.jarY, this.jarZ);
				}
			}
		}
		else if (tile instanceof IEssentiaTransport)
		{
			for (int side : GolemHelper.getMarkedSides(this.theGolem, tile, (byte) -1))
			{
				IEssentiaTransport trans = (IEssentiaTransport) tile;
				ForgeDirection direction = ForgeDirection.getOrientation(side);
				if (trans.canInputFrom(direction) && trans.getSuctionAmount(direction) > 0 && (trans.getSuctionType(direction) == null || trans.getSuctionType(direction) == this.theGolem.essentia))
				{
					int added = trans.addEssentia(this.theGolem.essentia, this.theGolem.essentiaAmount, direction);
					if (added > 0)
					{
						this.theGolem.essentiaAmount -= added;
						if (this.theGolem.essentiaAmount == 0)
						{
							this.theGolem.essentia = null;
						}

						this.theWorld.playSoundAtEntity(this.theGolem, "game.neutral.swim", 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
						this.theGolem.updateCarried();
						this.theWorld.markBlockForUpdate(this.jarX, this.jarY, this.jarZ);
						break;
					}
				}
			}
		}
	}
}
