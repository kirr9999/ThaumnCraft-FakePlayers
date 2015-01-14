package thaumcraft.common.entities.ai.fluid;

import com.gamerforea.thaumcraft.FakePlayerGetter;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;
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
			if (this.theGolem.getDistanceSq((double) ((float) cX + 0.5F), (double) ((float) cY + 0.5F), (double) ((float) cZ + 0.5F)) > 6.0D)
			{
				return false;
			}
			else
			{
				this.start = 0;
				TileEntity te = this.theWorld.getTileEntity(cX, cY, cZ);
				if (te != null)
				{
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
						{
							return true;
						}
					}
				}

				return false;
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public void startExecuting()
	{
		ChunkCoordinates home = this.theGolem.getHomePosition();
		ForgeDirection facing = ForgeDirection.getOrientation(this.theGolem.homeFacing);
		int cX = home.posX - facing.offsetX;
		int cY = home.posY - facing.offsetY;
		int cZ = home.posZ - facing.offsetZ;
		TileEntity te = this.theWorld.getTileEntity(cX, cY + this.start, cZ);
		if (te != null && te instanceof IEssentiaTransport)
		{
			// TODO gamerforEA code start
			EntityPlayer player = null;
			if (this.theGolem.ownerName != null && this.theGolem.ownerUUID != null)
			{
				if (this.theGolem.fakePlayer == null) this.theGolem.fakePlayer = FakePlayerFactory.get((WorldServer) this.theWorld, new GameProfile(this.theGolem.ownerUUID, this.theGolem.ownerName));
				player = this.theGolem.fakePlayer;
			}
			else player = FakePlayerGetter.getPlayer((WorldServer) this.theWorld).get();

			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(cX, cY, cZ, this.theWorld, this.theWorld.getBlock(cX, cY, cZ), this.theWorld.getBlockMetadata(cX, cY, cZ), player);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) return;
			// TODO gamerforEA code end

			if (te instanceof TileAlembic || te instanceof TileJarFillable)
			{
				facing = ForgeDirection.UP;
			}

			if (te instanceof TileEssentiaReservoir)
			{
				facing = ((TileEssentiaReservoir) te).facing;
			}

			IEssentiaTransport ta = (IEssentiaTransport) te;
			if (ta.getEssentiaAmount(facing) == 0)
			{
				return;
			}

			if (ta.canOutputTo(facing) && ta.getEssentiaAmount(facing) > 0 && (this.theGolem.essentiaAmount == 0 || (this.theGolem.essentia == null || this.theGolem.essentia.equals(ta.getEssentiaType(facing)) || this.theGolem.essentia.equals(ta.getEssentiaType(ForgeDirection.UNKNOWN))) && this.theGolem.essentiaAmount < this.theGolem.getCarryLimit()))
			{
				Aspect a = ta.getEssentiaType(facing);
				if (a == null)
				{
					a = ta.getEssentiaType(ForgeDirection.UNKNOWN);
				}

				int qq = ta.getEssentiaAmount(facing);
				if (te instanceof TileEssentiaReservoir)
				{
					qq = ((TileEssentiaReservoir) te).containerContains(a);
				}

				int am = Math.min(qq, this.theGolem.getCarryLimit() - this.theGolem.essentiaAmount);
				this.theGolem.essentia = a;
				int taken = ta.takeEssentia(a, am, facing);
				if (taken > 0)
				{
					this.theGolem.essentiaAmount += taken;
					this.theWorld.playSoundAtEntity(this.theGolem, "game.neutral.swim", 0.05F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
					this.theGolem.updateCarried();
				}
				else
				{
					this.theGolem.essentia = null;
				}

				this.delay = System.currentTimeMillis() + 100L;
			}
		}
	}
}