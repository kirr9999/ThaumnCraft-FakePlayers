package thaumcraft.common.entities.ai.fluid;

import java.util.ArrayList;
import java.util.Iterator;

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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;

import com.gamerforea.thaumcraft.FakePlayerGetter;
import com.mojang.authlib.GameProfile;

public class AILiquidEmpty extends EntityAIBase
{
	private EntityGolemBase theGolem;
	private int waterX;
	private int waterY;
	private int waterZ;
	private ForgeDirection markerOrientation;
	private World theWorld;

	public AILiquidEmpty(EntityGolemBase par1EntityCreature)
	{
		this.theGolem = par1EntityCreature;
		this.theWorld = par1EntityCreature.worldObj;
		this.setMutexBits(3);
	}

	public boolean shouldExecute()
	{
		ChunkCoordinates home = this.theGolem.getHomePosition();
		if (this.theGolem.getNavigator().noPath() && this.theGolem.fluidCarried != null && this.theGolem.fluidCarried.amount != 0 && this.theGolem.getDistanceSq((double) ((float) home.posX + 0.5F), (double) ((float) home.posY + 0.5F), (double) ((float) home.posZ + 0.5F)) <= 5.0D)
		{
			ArrayList fluids = GolemHelper.getMissingLiquids(this.theGolem);
			if (fluids == null)
			{
				return false;
			}
			else
			{
				Iterator i$ = fluids.iterator();

				FluidStack fluid;
				do
				{
					if (!i$.hasNext())
					{
						return false;
					}

					fluid = (FluidStack) i$.next();
				}
				while (!fluid.isFluidEqual(this.theGolem.fluidCarried));

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
		ForgeDirection facing = ForgeDirection.getOrientation(this.theGolem.homeFacing);
		ChunkCoordinates home = this.theGolem.getHomePosition();
		int cX = home.posX - facing.offsetX;
		int cY = home.posY - facing.offsetY;
		int cZ = home.posZ - facing.offsetZ;
		TileEntity tile = this.theWorld.getTileEntity(cX, cY, cZ);
		if (tile != null && tile instanceof IFluidHandler)
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
			IFluidHandler fh = (IFluidHandler) tile;
			int amt = fh.fill(ForgeDirection.getOrientation(this.theGolem.homeFacing), this.theGolem.fluidCarried, true);
			this.theGolem.fluidCarried.amount -= amt;
			if (this.theGolem.fluidCarried.amount <= 0)
			{
				this.theGolem.fluidCarried = null;
			}

			if (amt > 200)
			{
				this.theWorld.playSoundAtEntity(this.theGolem, "game.neutral.swim", Math.min(0.2F, 0.2F * ((float) amt / (float) this.theGolem.getFluidCarryLimit())), 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
			}

			this.theGolem.updateCarried();
			this.theWorld.markBlockForUpdate(cX, cY, cZ);
			this.theGolem.itemWatched = null;
		}

	}
}
