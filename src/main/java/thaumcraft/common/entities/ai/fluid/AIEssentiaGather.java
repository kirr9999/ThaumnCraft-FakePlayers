package thaumcraft.common.entities.ai.fluid;

import com.gamerforea.thaumcraft.FakePlayerUtils;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillable;

public class AIEssentiaGather extends EntityAIBase
{
	private EntityGolemBase theGolem;
	private double crucX;
	private double crucY;
	private double crucZ;
	private World theWorld;
	private long delay = 0L;
	int start = 0;

	public AIEssentiaGather(EntityGolemBase par1EntityCreature)
	{
		this.theGolem = par1EntityCreature;
		this.theWorld = par1EntityCreature.worldObj;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute()
	{
		if (this.theGolem.getNavigator().noPath() && this.delay <= System.currentTimeMillis())
		{
			ChunkCoordinates home = this.theGolem.getHomePosition();
			ForgeDirection facing = ForgeDirection.getOrientation(this.theGolem.homeFacing);
			int cX = home.posX - facing.offsetX;
			int cY = home.posY - facing.offsetY;
			int cZ = home.posZ - facing.offsetZ;
			if (this.theGolem.getDistanceSq(cX + 0.5F, cY + 0.5F, cZ + 0.5F) > 6.0D)
				return false;
			else
			{
				this.start = 0;
				TileEntity te = this.theWorld.getTileEntity(cX, cY, cZ);
				if (te != null)
					if (te instanceof IEssentiaTransport)
					{
						IEssentiaTransport a = (IEssentiaTransport) te;
						if ((te instanceof TileJarFillable || te instanceof TileEssentiaReservoir || a.canOutputTo(facing)) && a.getEssentiaAmount(facing) > 0 && (this.theGolem.essentiaAmount == 0 || (this.theGolem.essentia == null || this.theGolem.essentia.equals(a.getEssentiaType(facing)) || this.theGolem.essentia.equals(a.getEssentiaType(ForgeDirection.UNKNOWN))) && this.theGolem.essentiaAmount < this.theGolem.getCarryLimit()))
						{
							this.delay = System.currentTimeMillis() + 1000L;
							this.start = 0;
							return true;
						}
					}
					else
					{
						int var10 = 5;
						this.start = -1;

						for (int prevTot = -1; var10 >= 0; --var10)
						{
							te = this.theWorld.getTileEntity(cX, cY + var10, cZ);
							if (te != null && te instanceof TileAlembic)
							{
								TileAlembic ta = (TileAlembic) te;
								if ((this.theGolem.essentiaAmount == 0 || (this.theGolem.essentia == null || this.theGolem.essentia.equals(ta.aspect)) && this.theGolem.essentiaAmount < this.theGolem.getCarryLimit()) && ta.amount > prevTot)
								{
									this.delay = System.currentTimeMillis() + 1000L;
									this.start = var10;
									prevTot = ta.amount;
								}
							}
						}

						if (this.start >= 0)
							return true;
					}

				return false;
			}
		}
		else
			return false;
	}

	@Override
	public void startExecuting()
	{
		ChunkCoordinates home = this.theGolem.getHomePosition();
		ForgeDirection direction = ForgeDirection.getOrientation(this.theGolem.homeFacing);
		int cX = home.posX - direction.offsetX;
		int cY = home.posY - direction.offsetY;
		int cZ = home.posZ - direction.offsetZ;
		TileEntity tile = this.theWorld.getTileEntity(cX, cY + this.start, cZ);
		if (tile instanceof IEssentiaTransport)
		{
			// TODO gamerforEA code start
			if (FakePlayerUtils.cantBreak(cX, cY, cZ, this.theGolem.getOwnerFake()))
				return;
			// TODO gamerforEA code end

			if (tile instanceof TileAlembic || tile instanceof TileJarFillable)
				direction = ForgeDirection.UP;

			if (tile instanceof TileEssentiaReservoir)
				direction = ((TileEssentiaReservoir) tile).facing;

			IEssentiaTransport ta = (IEssentiaTransport) tile;
			if (ta.getEssentiaAmount(direction) == 0)
				return;

			if (ta.canOutputTo(direction) && ta.getEssentiaAmount(direction) > 0 && (this.theGolem.essentiaAmount == 0 || (this.theGolem.essentia == null || this.theGolem.essentia.equals(ta.getEssentiaType(direction)) || this.theGolem.essentia.equals(ta.getEssentiaType(ForgeDirection.UNKNOWN))) && this.theGolem.essentiaAmount < this.theGolem.getCarryLimit()))
			{
				Aspect aspect = ta.getEssentiaType(direction);
				if (aspect == null)
					aspect = ta.getEssentiaType(ForgeDirection.UNKNOWN);

				int amount = tile instanceof TileEssentiaReservoir ? ((TileEssentiaReservoir) tile).containerContains(aspect) : ta.getEssentiaAmount(direction);
				int am = Math.min(amount, this.theGolem.getCarryLimit() - this.theGolem.essentiaAmount);
				this.theGolem.essentia = aspect;
				int taken = ta.takeEssentia(aspect, am, direction);

				if (taken > 0)
				{
					this.theGolem.essentiaAmount += taken;
					this.theWorld.playSoundAtEntity(this.theGolem, "game.neutral.swim", 0.05F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
					this.theGolem.updateCarried();
				}
				else
					this.theGolem.essentia = null;

				this.delay = System.currentTimeMillis() + 100L;
			}
		}

	}
}
