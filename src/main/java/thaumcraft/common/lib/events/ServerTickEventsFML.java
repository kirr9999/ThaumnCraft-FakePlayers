package thaumcraft.common.lib.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Level;

import com.gamerforea.eventhelper.util.EventUtils;
import com.gamerforea.eventhelper.util.FastUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
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

public class ServerTickEventsFML
{
	public static Map<Integer, LinkedBlockingQueue<VirtualSwapper>> swapList = new HashMap();
	public static HashMap<Integer, ArrayList<ChunkLoc>> chunksToGenerate = new HashMap();

	@SubscribeEvent
	public void serverWorldTick(WorldTickEvent event)
	{
		if (event.side != Side.CLIENT && event.phase != Phase.START)
		{
			this.tickChunkRegeneration(event);
			this.tickBlockSwap(event.world);
			if (TileSensor.noteBlockEvents.get(event.world) != null)
				TileSensor.noteBlockEvents.get(event.world).clear();
		}
	}

	public void tickChunkRegeneration(WorldTickEvent event)
	{
		int dim = event.world.provider.dimensionId;
		int count = 0;
		ArrayList<ChunkLoc> chunks = chunksToGenerate.get(dim);
		if (chunks != null && chunks.size() > 0)
			for (int a = 0; a < 10; ++a)
			{
				chunks = chunksToGenerate.get(dim);
				if (chunks == null || chunks.size() <= 0)
					break;

				++count;
				ChunkLoc loc = chunks.get(0);
				long worldSeed = event.world.getSeed();
				Random fmlRandom = new Random(worldSeed);
				long xSeed = fmlRandom.nextLong() >> 3;
				long zSeed = fmlRandom.nextLong() >> 3;
				fmlRandom.setSeed(xSeed * loc.chunkXPos + zSeed * loc.chunkZPos ^ worldSeed);
				Thaumcraft.instance.worldGen.worldGeneration(fmlRandom, loc.chunkXPos, loc.chunkZPos, event.world, false);
				chunks.remove(0);
				chunksToGenerate.put(dim, chunks);
			}

		if (count > 0)
			FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, "[Thaumcraft] Regenerated " + count + " chunks. " + Math.max(0, chunks.size()) + " chunks left");
	}

	private void tickBlockSwap(World world)
	{
		int dim = world.provider.dimensionId;
		LinkedBlockingQueue<VirtualSwapper> queue = swapList.get(dim);
		if (queue != null)
		{
			boolean didSomething = false;

			while (!didSomething)
			{
				VirtualSwapper vs = queue.poll();
				if (vs != null)
				{
					// TODO gamerforEA code start
					if (Block.getBlockFromItem(vs.target.getItem()).getClass().getName().contains("BlockArmorStand"))
						continue;

					if (!FastUtils.isOnline(vs.player) || EventUtils.cantBreak(vs.player, vs.x, vs.y, vs.z))
						continue;
					// TODO gamerforEA code end

					Block block = world.getBlock(vs.x, vs.y, vs.z);
					int meta = world.getBlockMetadata(vs.x, vs.y, vs.z);
					ItemWandCasting wand = null;
					ItemFocusBasic focus = null;
					ItemStack focusStack = null;
					if (vs.player.inventory.getStackInSlot(vs.wand) != null && vs.player.inventory.getStackInSlot(vs.wand).getItem() instanceof ItemWandCasting)
					{
						wand = (ItemWandCasting) vs.player.inventory.getStackInSlot(vs.wand).getItem();
						focusStack = wand.getFocusItem(vs.player.inventory.getStackInSlot(vs.wand));
						focus = wand.getFocus(vs.player.inventory.getStackInSlot(vs.wand));
					}

					if (world.canMineBlock(vs.player, vs.x, vs.y, vs.z) && !vs.target.isItemEqual(new ItemStack(block, 1, meta)) && wand != null && focus != null && !ForgeEventFactory.onPlayerInteract(vs.player, Action.RIGHT_CLICK_BLOCK, vs.x, vs.y, vs.z, 1, world).isCanceled() && wand.consumeAllVis(vs.player.inventory.getStackInSlot(vs.wand), vs.player, focus.getVisCost(focusStack), false, false))
					{
						int slot = InventoryUtils.isPlayerCarrying(vs.player, vs.target);
						if (vs.player.capabilities.isCreativeMode)
							slot = 1;

						if (vs.bSource == block && vs.mSource == meta && slot >= 0)
						{
							didSomething = true;
							if (!vs.player.capabilities.isCreativeMode)
							{
								int furtune = wand.getFocusTreasure(vs.player.inventory.getStackInSlot(vs.wand));
								boolean silktouch = wand.getFocus(vs.player.inventory.getStackInSlot(vs.wand)).isUpgradedWith(wand.getFocusItem(vs.player.inventory.getStackInSlot(vs.wand)), FocusUpgradeType.silktouch);
								vs.player.inventory.decrStackSize(slot, 1);
								List<ItemStack> drops = new ArrayList();
								if (silktouch && block.canSilkHarvest(world, vs.player, vs.x, vs.y, vs.z, meta))
								{
									ItemStack stack = BlockUtils.createStackedBlock(block, meta);
									if (stack != null)
										drops.add(stack);
								}
								else
									drops = block.getDrops(world, vs.x, vs.y, vs.z, meta, furtune);

								if (drops.size() > 0)
									for (ItemStack stack : drops)
										if (!vs.player.inventory.addItemStackToInventory(stack))
											world.spawnEntityInWorld(new EntityItem(world, vs.x + 0.5D, vs.y + 0.5D, vs.z + 0.5D, stack));

								wand.consumeAllVis(vs.player.inventory.getStackInSlot(vs.wand), vs.player, focus.getVisCost(focusStack), true, false);
							}

							world.setBlock(vs.x, vs.y, vs.z, Block.getBlockFromItem(vs.target.getItem()), vs.target.getItemDamage(), 3);
							Block.getBlockFromItem(vs.target.getItem()).onBlockPlacedBy(world, vs.x, vs.y, vs.z, vs.player, vs.target);
							PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(vs.x, vs.y, vs.z, 12632319), new TargetPoint(world.provider.dimensionId, vs.x, vs.y, vs.z, 32.0D));
							world.playAuxSFX(2001, vs.x, vs.y, vs.z, Block.getIdFromBlock(vs.bSource) + (vs.mSource << 12));
							if (vs.lifespan > 0)
								for (int x = -1; x <= 1; ++x)
									for (int y = -1; y <= 1; ++y)
										for (int z = -1; z <= 1; ++z)
											if ((x != 0 || y != 0 || z != 0) && world.getBlock(vs.x + x, vs.y + y, vs.z + z) == vs.bSource && world.getBlockMetadata(vs.x + x, vs.y + y, vs.z + z) == vs.mSource && BlockUtils.isBlockExposed(world, vs.x + x, vs.y + y, vs.z + z))
												queue.offer(new VirtualSwapper(vs.x + x, vs.y + y, vs.z + z, vs.bSource, vs.mSource, vs.target, vs.lifespan - 1, vs.player, vs.wand));
						}
					}
				}
				else
					didSomething = true;
			}

			swapList.put(dim, queue);
		}
	}

	public static void addSwapper(World world, int x, int y, int z, Block bs, int ms, ItemStack target, int life, EntityPlayer player, int wand)
	{
		int dim = world.provider.dimensionId;
		if (bs != Blocks.air && bs.getBlockHardness(world, x, y, z) >= 0.0F && !target.isItemEqual(new ItemStack(bs, 1, ms)))
		{
			LinkedBlockingQueue queue = swapList.get(dim);
			if (queue == null)
			{
				swapList.put(dim, new LinkedBlockingQueue());
				queue = swapList.get(dim);
			}

			queue.offer(new VirtualSwapper(x, y, z, bs, ms, target, life, player, wand));
			world.playSoundAtEntity(player, "thaumcraft:wand", 0.25F, 1.0F);
			swapList.put(dim, queue);
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
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile != null)
			{
				this.nbt = new NBTTagCompound();
				tile.writeToNBT(this.nbt);
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