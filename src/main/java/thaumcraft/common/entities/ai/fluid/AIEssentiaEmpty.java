package thaumcraft.common.entities.ai.fluid;

import java.util.Iterator;

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

	@Override
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

	@Override
	public boolean continueExecuting()
	{
		return false;
	}

	@Override
	public void startExecuting()
	{
		TileEntity tile = this.theWorld.getTileEntity(this.jarX, this.jarY, this.jarZ);
		// TODO gamerforEA code start
		if (tile != null)
		{
			EntityPlayer player = null;
			if (this.theGolem.ownerName != null && this.theGolem.ownerUUID != null)
			{
				if (this.theGolem.fakePlayer == null) this.theGolem.fakePlayer = FakePlayerFactory.get((WorldServer) this.theWorld, new GameProfile(this.theGolem.ownerUUID, this.theGolem.ownerName));
				player = this.theGolem.fakePlayer;
			}
			else player = FakePlayerGetter.getPlayer((WorldServer) this.theWorld).get();
			
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(this.jarX, this.jarY, this.jarZ, this.theWorld, this.theWorld.getBlock(this.jarX, this.jarY, this.jarZ), this.theWorld.getBlockMetadata(this.jarX, this.jarY, this.jarZ), player);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) return;
		}
		// TODO gamerforEA code end
		if (tile != null && tile instanceof TileJarFillable)
		{
			TileJarFillable jar = (TileJarFillable) tile;
			this.theGolem.essentiaAmount = jar.addToContainer(this.theGolem.essentia, this.theGolem.essentiaAmount);
			if (this.theGolem.essentiaAmount == 0)
			{
				this.theGolem.essentia = null;
			}

			this.theWorld.playSoundAtEntity(this.theGolem, "game.neutral.swim", 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
			this.theGolem.updateCarried();
			this.theWorld.markBlockForUpdate(this.jarX, this.jarY, this.jarZ);
		}
		else if (tile != null && tile instanceof TileEssentiaReservoir)
		{
			TileEssentiaReservoir reservoir = (TileEssentiaReservoir) tile;
			if (reservoir.getSuctionAmount(reservoir.facing) > 0 && (reservoir.getSuctionType(reservoir.facing) == null || reservoir.getSuctionType(reservoir.facing) == this.theGolem.essentia))
			{
				int side1 = reservoir.addEssentia(this.theGolem.essentia, this.theGolem.essentiaAmount, reservoir.facing);
				if (side1 > 0)
				{
					this.theGolem.essentiaAmount -= side1;
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
		else if (tile != null && tile instanceof IEssentiaTransport)
		{
			for (Integer side : GolemHelper.getMarkedSides(this.theGolem, tile, (byte) -1))
			{
				IEssentiaTransport trans = (IEssentiaTransport) tile;
				if (trans.canInputFrom(ForgeDirection.getOrientation(side.intValue())) && trans.getSuctionAmount(ForgeDirection.getOrientation(side.intValue())) > 0 && (trans.getSuctionType(ForgeDirection.getOrientation(side.intValue())) == null || trans.getSuctionType(ForgeDirection.getOrientation(side.intValue())) == this.theGolem.essentia))
				{
					int added = trans.addEssentia(this.theGolem.essentia, this.theGolem.essentiaAmount, ForgeDirection.getOrientation(side.intValue()));
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