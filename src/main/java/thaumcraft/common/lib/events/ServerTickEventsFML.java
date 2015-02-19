package thaumcraft.common.lib.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;

import org.apache.logging.log4j.Level;

import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.world.ChunkLoc;
import thaumcraft.common.tiles.TileSensor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class ServerTickEventsFML
{
	public static Map<Integer, LinkedBlockingQueue<ServerTickEventsFML.VirtualSwapper>> swapList = new HashMap();
	public static HashMap<Integer, ArrayList<ChunkLoc>> chunksToGenerate = new HashMap();

	@SubscribeEvent
	public void serverWorldTick(WorldTickEvent event)
	{
		if (event.side != Side.CLIENT)
		{
			if (event.phase != Phase.START)
			{
				this.tickChunkRegeneration(event);
				this.tickBlockSwap(event.world);
				if (TileSensor.noteBlockEvents.get(event.world) != null)
				{
					((ArrayList) TileSensor.noteBlockEvents.get(event.world)).clear();
				}
			}
		}
	}

	public void tickChunkRegeneration(WorldTickEvent event)
	{
		int dim = event.world.provider.dimensionId;
		int count = 0;
		ArrayList chunks = (ArrayList) chunksToGenerate.get(Integer.valueOf(dim));
		if (chunks != null && chunks.size() > 0)
		{
			for (int a = 0; a < 10; ++a)
			{
				chunks = (ArrayList) chunksToGenerate.get(Integer.valueOf(dim));
				if (chunks == null || chunks.size() <= 0)
				{
					break;
				}

				++count;
				ChunkLoc loc = (ChunkLoc) chunks.get(0);
				long worldSeed = event.world.getSeed();
				Random fmlRandom = new Random(worldSeed);
				long xSeed = fmlRandom.nextLong() >> 3;
				long zSeed = fmlRandom.nextLong() >> 3;
				fmlRandom.setSeed(xSeed * (long) loc.chunkXPos + zSeed * (long) loc.chunkZPos ^ worldSeed);
				Thaumcraft.instance.worldGen.worldGeneration(fmlRandom, loc.chunkXPos, loc.chunkZPos, event.world, false);
				chunks.remove(0);
				chunksToGenerate.put(Integer.valueOf(dim), chunks);
			}
		}

		if (count > 0)
		{
			FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, "[Thaumcraft] Regenerated " + count + " chunks. " + Math.max(0, chunks.size()) + " chunks left");
		}
	}

	private void tickBlockSwap(World world)
	{
		int dim = world.provider.dimensionId;
		LinkedBlockingQueue queue = (LinkedBlockingQueue) swapList.get(Integer.valueOf(dim));
		if (queue != null)
		{
			boolean didSomething = false;

			while (!didSomething)
			{
				ServerTickEventsFML.VirtualSwapper vs = (ServerTickEventsFML.VirtualSwapper) queue.poll();
				if (vs != null)
				{
					Block bi = world.getBlock(vs.x, vs.y, vs.z);
					int md = world.getBlockMetadata(vs.x, vs.y, vs.z);
					// TODO gamerforEA code start
					BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(vs.x, vs.y, vs.z, world, bi, md, vs.player);
					MinecraftForge.EVENT_BUS.post(event);
					if (event.isCanceled()) continue;
					// TODO gamerforEA code end
					ItemWandCasting wand = null;
					ItemFocusBasic focus = null;
					ItemStack focusStack = null;
					if (vs.player.inventory.getStackInSlot(vs.wand) != null && vs.player.inventory.getStackInSlot(vs.wand).getItem() instanceof ItemWandCasting)
					{
						wand = (ItemWandCasting) vs.player.inventory.getStackInSlot(vs.wand).getItem();
						focusStack = wand.getFocusItem(vs.player.inventory.getStackInSlot(vs.wand));
						focus = wand.getFocus(vs.player.inventory.getStackInSlot(vs.wand));
					}

					if (world.canMineBlock(vs.player, vs.x, vs.y, vs.z) && !vs.target.isItemEqual(new ItemStack(bi, 1, md)) && wand != null && focus != null && !ForgeEventFactory.onPlayerInteract(vs.player, Action.RIGHT_CLICK_BLOCK, vs.x, vs.y, vs.z, 1, world).isCanceled() && wand.consumeAllVis(vs.player.inventory.getStackInSlot(vs.wand), vs.player, focus.getVisCost(focusStack), false, false))
					{
						int slot = InventoryUtils.isPlayerCarrying(vs.player, vs.target);
						if (vs.player.capabilities.isCreativeMode)
						{
							slot = 1;
						}

						if (vs.bSource == bi && vs.mSource == md && slot >= 0)
						{
							didSomething = true;
							int xx;
							if (!vs.player.capabilities.isCreativeMode)
							{
								xx = wand.getFocusTreasure(vs.player.inventory.getStackInSlot(vs.wand));
								boolean yy = wand.getFocus(vs.player.inventory.getStackInSlot(vs.wand)).isUpgradedWith(wand.getFocusItem(vs.player.inventory.getStackInSlot(vs.wand)), FocusUpgradeType.silktouch);
								vs.player.inventory.decrStackSize(slot, 1);
								ArrayList zz = new ArrayList();
								if (yy && bi.canSilkHarvest(world, vs.player, vs.x, vs.y, vs.z, md))
								{
									ItemStack i$ = BlockUtils.createStackedBlock(bi, md);
									if (i$ != null)
									{
										zz.add(i$);
									}
								}
								else
								{
									zz = bi.getDrops(world, vs.x, vs.y, vs.z, md, xx);
								}

								if (zz.size() > 0)
								{
									Iterator var19 = zz.iterator();

									while (var19.hasNext())
									{
										ItemStack is = (ItemStack) var19.next();
										if (!vs.player.inventory.addItemStackToInventory(is))
										{
											world.spawnEntityInWorld(new EntityItem(world, (double) vs.x + 0.5D, (double) vs.y + 0.5D, (double) vs.z + 0.5D, is));
										}
									}
								}

								wand.consumeAllVis(vs.player.inventory.getStackInSlot(vs.wand), vs.player, focus.getVisCost(focusStack), true, false);
							}

							world.setBlock(vs.x, vs.y, vs.z, Block.getBlockFromItem(vs.target.getItem()), vs.target.getItemDamage(), 3);
							Block.getBlockFromItem(vs.target.getItem()).onBlockPlacedBy(world, vs.x, vs.y, vs.z, vs.player, vs.target);
							PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(vs.x, vs.y, vs.z, 12632319), new TargetPoint(world.provider.dimensionId, (double) vs.x, (double) vs.y, (double) vs.z, 32.0D));
							world.playAuxSFX(2001, vs.x, vs.y, vs.z, Block.getIdFromBlock(vs.bSource) + (vs.mSource << 12));
							if (vs.lifespan > 0)
							{
								for (xx = -1; xx <= 1; ++xx)
								{
									for (int var17 = -1; var17 <= 1; ++var17)
									{
										for (int var18 = -1; var18 <= 1; ++var18)
										{
											if ((xx != 0 || var17 != 0 || var18 != 0) && world.getBlock(vs.x + xx, vs.y + var17, vs.z + var18) == vs.bSource && world.getBlockMetadata(vs.x + xx, vs.y + var17, vs.z + var18) == vs.mSource && BlockUtils.isBlockExposed(world, vs.x + xx, vs.y + var17, vs.z + var18))
											{
												queue.offer(new ServerTickEventsFML.VirtualSwapper(vs.x + xx, vs.y + var17, vs.z + var18, vs.bSource, vs.mSource, vs.target, vs.lifespan - 1, vs.player, vs.wand));
											}
										}
									}
								}
							}
						}
					}
				}
				else
				{
					didSomething = true;
				}
			}

			swapList.put(Integer.valueOf(dim), queue);
		}
	}

	public static void addSwapper(World world, int x, int y, int z, Block bs, int ms, ItemStack target, int life, EntityPlayer player, int wand)
	{
		int dim = world.provider.dimensionId;
		if (bs != Blocks.air && bs.getBlockHardness(world, x, y, z) >= 0.0F && !target.isItemEqual(new ItemStack(bs, 1, ms)))
		{
			LinkedBlockingQueue queue = (LinkedBlockingQueue) swapList.get(Integer.valueOf(dim));
			if (queue == null)
			{
				swapList.put(Integer.valueOf(dim), new LinkedBlockingQueue());
				queue = (LinkedBlockingQueue) swapList.get(Integer.valueOf(dim));
			}

			queue.offer(new ServerTickEventsFML.VirtualSwapper(x, y, z, bs, ms, target, life, player, wand));
			world.playSoundAtEntity(player, "thaumcraft:wand", 0.25F, 1.0F);
			swapList.put(Integer.valueOf(dim), queue);
		}
	}

	public static class RestorableWardedBlock
	{
		int x = 0;
		int y = 0;
		int z = 0;
		Block bi;
		int md = 0;
		NBTTagCompound nbt = null;

		RestorableWardedBlock(World world, int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.bi = world.getBlock(x, y, z);
			this.md = world.getBlockMetadata(x, y, z);
			TileEntity te = world.getTileEntity(x, y, z);
			if (te != null)
			{
				this.nbt = new NBTTagCompound();
				te.writeToNBT(this.nbt);
			}
		}
	}

	public static class VirtualSwapper
	{
		int lifespan = 0;
		int x = 0;
		int y = 0;
		int z = 0;
		Block bSource;
		int mSource = 0;
		ItemStack target;
		int wand = 0;
		EntityPlayer player = null;

		VirtualSwapper(int x, int y, int z, Block bs, int ms, ItemStack t, int life, EntityPlayer p, int wand)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.bSource = bs;
			this.mSource = ms;
			this.target = t;
			this.lifespan = life;
			this.player = p;
			this.wand = wand;
		}
	}
}