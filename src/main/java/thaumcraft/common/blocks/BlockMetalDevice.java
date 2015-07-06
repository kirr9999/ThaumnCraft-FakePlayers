package thaumcraft.common.blocks;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.items.ItemShard;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileArcaneLamp;
import thaumcraft.common.tiles.TileArcaneLampFertility;
import thaumcraft.common.tiles.TileArcaneLampGrowth;
import thaumcraft.common.tiles.TileBrainbox;
import thaumcraft.common.tiles.TileCrucible;
import thaumcraft.common.tiles.TileGrate;
import thaumcraft.common.tiles.TileMagicWorkbenchCharger;
import thaumcraft.common.tiles.TileThaumatorium;
import thaumcraft.common.tiles.TileThaumatoriumTop;
import thaumcraft.common.tiles.TileVisRelay;

public class BlockMetalDevice extends BlockContainer
{
	public IIcon[] icon = new IIcon[23];
	public IIcon iconGlow;
	private int delay = 0;

	public BlockMetalDevice()
	{
		super(Material.iron);
		this.setHardness(3F);
		this.setResistance(17F);
		this.setStepSound(Block.soundTypeMetal);
		this.setBlockBounds(0F, 0F, 0F, 1F, 1F, 1F);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		this.icon[0] = ir.registerIcon("thaumcraft:metalbase");

		for (int a = 1; a <= 6; ++a)
		{
			this.icon[a] = ir.registerIcon("thaumcraft:crucible" + a);
		}

		this.icon[7] = ir.registerIcon("thaumcraft:goldbase");
		this.icon[8] = ir.registerIcon("thaumcraft:grate");
		this.icon[9] = ir.registerIcon("thaumcraft:grate_hatch");
		this.icon[10] = ir.registerIcon("thaumcraft:lamp_side");
		this.icon[11] = ir.registerIcon("thaumcraft:lamp_top");
		this.icon[12] = ir.registerIcon("thaumcraft:lamp_grow_side");
		this.icon[13] = ir.registerIcon("thaumcraft:lamp_grow_top");
		this.icon[14] = ir.registerIcon("thaumcraft:lamp_grow_side_off");
		this.icon[15] = ir.registerIcon("thaumcraft:lamp_grow_top_off");
		this.icon[16] = ir.registerIcon("thaumcraft:alchemyblock");
		this.icon[17] = ir.registerIcon("thaumcraft:brainbox");
		this.icon[18] = ir.registerIcon("thaumcraft:lamp_fert_side");
		this.icon[19] = ir.registerIcon("thaumcraft:lamp_fert_top");
		this.icon[20] = ir.registerIcon("thaumcraft:lamp_fert_side_off");
		this.icon[21] = ir.registerIcon("thaumcraft:lamp_fert_top_off");
		this.icon[22] = ir.registerIcon("thaumcraft:alchemyblockadv");
		this.iconGlow = ir.registerIcon("thaumcraft:animatedglow");
	}

	public IIcon getIcon(int i, int md)
	{
		return md == 3 ? this.icon[22] : (md == 7 ? this.icon[10] : (md == 8 ? this.icon[12] : (md != 10 && md != 9 && md != 11 ? (md == 12 ? this.icon[17] : (md == 13 ? this.icon[18] : (md != 14 && md != 2 ? (md != 0 && md != 1 && md != 5 && md != 6 ? this.icon[7] : this.icon[0]) : this.icon[0]))) : this.icon[16])));
	}

	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int side)
	{
		int metadata = iblockaccess.getBlockMetadata(i, j, k);
		if (metadata != 5 && metadata != 6)
		{
			if (metadata == 7)
			{
				return side <= 1 ? this.icon[11] : this.icon[10];
			}
			else
			{
				TileEntity te;
				if (metadata == 8)
				{
					te = iblockaccess.getTileEntity(i, j, k);
					if (te != null && te instanceof TileArcaneLampGrowth)
					{
						if (((TileArcaneLampGrowth) te).charges > 0)
						{
							if (side <= 1)
							{
								return this.icon[13];
							}

							return this.icon[12];
						}

						if (side <= 1)
						{
							return this.icon[15];
						}

						return this.icon[14];
					}
				}
				else if (metadata == 13)
				{
					te = iblockaccess.getTileEntity(i, j, k);
					if (te != null && te instanceof TileArcaneLampFertility)
					{
						if (((TileArcaneLampFertility) te).charges > 0)
						{
							if (side <= 1)
							{
								return this.icon[19];
							}

							return this.icon[18];
						}

						if (side <= 1)
						{
							return this.icon[21];
						}

						return this.icon[20];
					}
				}
				else
				{
					if (metadata == 10 || metadata == 9 || metadata == 11)
					{
						return this.icon[16];
					}

					if (metadata == 12)
					{
						return this.icon[17];
					}

					if (metadata == 3)
					{
						return this.icon[22];
					}
				}

				return side == 1 ? this.icon[1] : (side == 0 ? this.icon[2] : this.icon[3]);
			}
		}
		else
		{
			return this.icon[8];
		}
	}

	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 5));
		par3List.add(new ItemStack(par1, 1, 7));
		par3List.add(new ItemStack(par1, 1, 8));
		par3List.add(new ItemStack(par1, 1, 13));
		par3List.add(new ItemStack(par1, 1, 9));
		par3List.add(new ItemStack(par1, 1, 3));
		par3List.add(new ItemStack(par1, 1, 12));
		par3List.add(new ItemStack(par1, 1, 14));
		par3List.add(new ItemStack(par1, 1, 2));
	}

	public int getRenderType()
	{
		return ConfigBlocks.blockMetalDeviceRI;
	}

	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean renderAsNormalBlock()
	{
		return false;
	}

	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity)
	{
		if (!world.isRemote)
		{
			int metadata = world.getBlockMetadata(i, j, k);
			if (metadata == 0)
			{
				TileCrucible tile = (TileCrucible) world.getTileEntity(i, j, k);
				if (tile != null && entity instanceof EntityItem && !(entity instanceof EntitySpecialItem) && tile.heat > 150 && tile.tank.getFluidAmount() > 0)
				{
					tile.attemptSmelt((EntityItem) entity);
				}
				else
				{
					++this.delay;
					if (this.delay < 10)
					{
						return;
					}

					this.delay = 0;
					if (entity instanceof EntityLivingBase && tile != null && tile.heat > 150 && tile.tank.getFluidAmount() > 0)
					{
						entity.attackEntityFrom(DamageSource.inFire, 1.0F);
						world.playSoundEffect((double) i, (double) j, (double) k, "random.fizz", 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
					}
				}
			}
		}

	}

	public void setBlockBoundsBasedOnState(IBlockAccess world, int i, int j, int k)
	{
		int metadata = world.getBlockMetadata(i, j, k);
		if (metadata != 5 && metadata != 6)
		{
			if (metadata != 7 && metadata != 8 && metadata != 13)
			{
				if (metadata == 10)
				{
					this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
				}
				else if (metadata == 11)
				{
					this.setBlockBounds(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				}
				else if (metadata == 12)
				{
					this.setBlockBounds(BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W13, BlockRenderer.W13);
				}
				else if (metadata == 2)
				{
					this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
				}
				else if (metadata == 14)
				{
					TileEntity te = world.getTileEntity(i, j, k);
					if (te != null && te instanceof TileVisRelay)
					{
						switch (BlockMetalDevice.SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.getOrientation(((TileVisRelay) te).orientation).getOpposite().ordinal()])
						{
							case 1:
								this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
								break;
							case 2:
								this.setBlockBounds(BlockRenderer.W5, 0.0F, BlockRenderer.W5, BlockRenderer.W11, 0.5F, BlockRenderer.W11);
								break;
							case 3:
								this.setBlockBounds(0.5F, BlockRenderer.W5, BlockRenderer.W5, 1.0F, BlockRenderer.W11, BlockRenderer.W11);
								break;
							case 4:
								this.setBlockBounds(0.0F, BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11);
								break;
							case 5:
								this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11, 1.0F);
								break;
							case 6:
								this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.0F, BlockRenderer.W11, BlockRenderer.W11, 0.5F);
						}
					}
				}
				else
				{
					this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				}
			}
			else
			{
				this.setBlockBounds(BlockRenderer.W4, BlockRenderer.W2, BlockRenderer.W4, BlockRenderer.W12, BlockRenderer.W14, BlockRenderer.W12);
			}
		}
		else
		{
			this.setBlockBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
		}

		super.setBlockBoundsBasedOnState(world, i, j, k);
	}

	public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity)
	{
		int metadata = world.getBlockMetadata(i, j, k);
		if (metadata == 0)
		{
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3125F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			float te = 0.125F;
			this.setBlockBounds(0.0F, 0.0F, 0.0F, te, 0.85F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.85F, te);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			this.setBlockBounds(1.0F - te, 0.0F, 0.0F, 1.0F, 0.85F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			this.setBlockBounds(0.0F, 0.0F, 1.0F - te, 1.0F, 0.85F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}
		else if (metadata == 2)
		{
			this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}
		else if (metadata == 5)
		{
			if (par7Entity != null && !(par7Entity instanceof EntityItem))
			{
				this.setBlockBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
		}
		else if (metadata == 6)
		{
			this.setBlockBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}
		else if (metadata != 7 && metadata != 8 && metadata != 13)
		{
			if (metadata == 12)
			{
				this.setBlockBounds(BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W13, BlockRenderer.W13);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
			else if (metadata == 14)
			{
				TileEntity te1 = world.getTileEntity(i, j, k);
				if (te1 != null && te1 instanceof TileVisRelay)
				{
					switch (BlockMetalDevice.SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.getOrientation(((TileVisRelay) te1).orientation).getOpposite().ordinal()])
					{
						case 1:
							this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
							break;
						case 2:
							this.setBlockBounds(BlockRenderer.W5, 0.0F, BlockRenderer.W5, BlockRenderer.W11, 0.5F, BlockRenderer.W11);
							break;
						case 3:
							this.setBlockBounds(0.5F, BlockRenderer.W5, BlockRenderer.W5, 1.0F, BlockRenderer.W11, BlockRenderer.W11);
							break;
						case 4:
							this.setBlockBounds(0.0F, BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11);
							break;
						case 5:
							this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11, 1.0F);
							break;
						case 6:
							this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.0F, BlockRenderer.W11, BlockRenderer.W11, 0.5F);
					}

					super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
				}
			}
			else
			{
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
		}
		else
		{
			this.setBlockBounds(BlockRenderer.W4, BlockRenderer.W2, BlockRenderer.W4, BlockRenderer.W12, BlockRenderer.W14, BlockRenderer.W12);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int i, int j, int k, Random r)
	{
		if (r.nextInt(10) == 0)
		{
			TileEntity te = w.getTileEntity(i, j, k);
			if (te != null && te instanceof TileCrucible && ((TileCrucible) te).tank.getFluidAmount() > 0 && ((TileCrucible) te).heat > 150)
			{
				w.playSound((double) i, (double) j, (double) k, "liquid.lavapop", 0.1F + r.nextFloat() * 0.1F, 1.2F + r.nextFloat() * 0.2F, false);
			}
		}

	}

	public int damageDropped(int metadata)
	{
		return metadata == 6 ? 5 : (metadata != 10 && metadata != 11 ? metadata : 9);
	}

	public TileEntity createTileEntity(World world, int metadata)
	{
		return (TileEntity) (metadata == 0 ? new TileCrucible() : (metadata == 5 ? new TileGrate() : (metadata == 6 ? new TileGrate() : (metadata == 1 ? new TileAlembic() : (metadata == 7 ? new TileArcaneLamp() : (metadata == 8 ? new TileArcaneLampGrowth() : (metadata == 10 ? new TileThaumatorium() : (metadata == 11 ? new TileThaumatoriumTop() : (metadata == 12 ? new TileBrainbox() : (metadata == 13 ? new TileArcaneLampFertility() : (metadata == 14 ? new TileVisRelay() : (metadata == 2 ? new TileMagicWorkbenchCharger() : super.createTileEntity(world, metadata)))))))))))));
	}

	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	public int getComparatorInputOverride(World world, int x, int y, int z, int rs)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TileThaumatorium)
		{
			return Container.calcRedstoneFromInventory((IInventory) te);
		}
		else
		{
			float r;
			if (te != null && te instanceof TileAlembic)
			{
				r = (float) ((TileAlembic) te).amount / (float) ((TileAlembic) te).maxAmount;
				return MathHelper.floor_float(r * 14.0F) + (((TileAlembic) te).amount > 0 ? 1 : 0);
			}
			else if (te != null && te instanceof TileCrucible)
			{
				float var10000 = (float) ((TileCrucible) te).aspects.visSize();
				((TileCrucible) te).getClass();
				r = var10000 / 100.0F;
				return MathHelper.floor_float(r * 14.0F) + (((TileCrucible) te).aspects.visSize() > 0 ? 1 : 0);
			}
			else
			{
				return 0;
			}
		}
	}

	public TileEntity createNewTileEntity(World var1, int md)
	{
		return null;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block nbid)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		int md = world.getBlockMetadata(x, y, z);
		if (te != null && te instanceof TileCrucible)
		{
			((TileCrucible) te).getBellows();
		}

		if (!world.isRemote)
		{
			if (te != null && te instanceof TileAlembic)
			{
				world.markBlockForUpdate(x, y, z);
			}
			else if (te != null && te instanceof TileArcaneLamp)
			{
				TileArcaneLamp flag4 = (TileArcaneLamp) te;
				if (world.isAirBlock(x + flag4.facing.offsetX, y + flag4.facing.offsetY, z + flag4.facing.offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 7, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else if (te != null && te instanceof TileArcaneLampGrowth)
			{
				TileArcaneLampGrowth flag3 = (TileArcaneLampGrowth) te;
				if (world.isAirBlock(x + flag3.facing.offsetX, y + flag3.facing.offsetY, z + flag3.facing.offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 8, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else if (te != null && te instanceof TileBrainbox)
			{
				TileBrainbox flag2 = (TileBrainbox) te;
				if (world.isAirBlock(x + flag2.facing.offsetX, y + flag2.facing.offsetY, z + flag2.facing.offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 12, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else if (te != null && te instanceof TileVisRelay && md == 14)
			{
				TileVisRelay flag1 = (TileVisRelay) te;
				if (world.isAirBlock(x + ForgeDirection.getOrientation(flag1.orientation).getOpposite().offsetX, y + ForgeDirection.getOrientation(flag1.orientation).getOpposite().offsetY, z + ForgeDirection.getOrientation(flag1.orientation).getOpposite().offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 14, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else
			{
				TileEntity flag;
				if (md == 10)
				{
					if (world.getBlock(x, y + 1, z) != this || world.getBlockMetadata(x, y + 1, z) != 11 || world.getBlock(x, y - 1, z) != this || world.getBlockMetadata(x, y - 1, z) != 0)
					{
						InventoryUtils.dropItems(world, x, y, z);
						world.setBlockToAir(x, y, z);
						world.setBlock(x, y, z, this, 9, 3);
						return;
					}

					flag = world.getTileEntity(x, y, z);
					if (flag != null && flag instanceof TileThaumatorium)
					{
						((TileThaumatorium) flag).getUpgrades();
					}
				}
				else if (md == 11)
				{
					if (world.getBlock(x, y - 1, z) != this || world.getBlockMetadata(x, y - 1, z) != 10)
					{
						world.setBlockToAir(x, y, z);
						world.setBlock(x, y, z, this, 9, 3);
						return;
					}

					flag = world.getTileEntity(x, y - 1, z);
					if (flag != null && flag instanceof TileThaumatorium)
					{
						((TileThaumatorium) flag).getUpgrades();
					}
				}
			}

			boolean flag5 = world.isBlockIndirectlyGettingPowered(x, y, z);
			if (flag5 || nbid.canProvidePower())
			{
				this.onPoweredBlockChange(world, x, y, z, flag5);
			}
		}

		super.onNeighborBlockChange(world, x, y, z, nbid);
	}

	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6)
	{
		InventoryUtils.dropItems(par1World, par2, par3, par4);
		TileEntity te = par1World.getTileEntity(par2, par3, par4);
		if (te != null && te instanceof TileCrucible)
		{
			((TileCrucible) te).spillRemnants();
		}
		else if (te != null && te instanceof TileAlembic && ((TileAlembic) te).aspectFilter != null)
		{
			par1World.spawnEntityInWorld(new EntityItem(par1World, (double) ((float) par2 + 0.5F), (double) ((float) par3 + 0.5F), (double) ((float) par4 + 0.5F), new ItemStack(ConfigItems.itemResource, 1, 13)));
		}
		else if (te != null && te instanceof TileArcaneLamp)
		{
			((TileArcaneLamp) te).removeLights();
		}

		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
	{
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity tile;
		if (meta == 0 && !world.isRemote)
		{
			FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(player.inventory.getCurrentItem());
			if (fluid != null && fluid.isFluidEqual(new FluidStack(FluidRegistry.WATER, 1000)))
			{
				tile = world.getTileEntity(x, y, z);
				if (tile != null && tile instanceof TileCrucible)
				{
					TileCrucible crucible = (TileCrucible) tile;
					if (crucible.tank.getFluidAmount() >= crucible.tank.getCapacity()) return true;

					crucible.fill(ForgeDirection.UNKNOWN, fluid, true);
					/* TODO gamerforEA fix dupe, old code:
					ItemStack stack = null;
					
					for (FluidContainerData fluidData : FluidContainerRegistry.getRegisteredFluidContainerData())
						if (fluidData.filledContainer.isItemEqual(player.inventory.getCurrentItem())) stack = fluidData.emptyContainer.copy();
					
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
					if (stack != null && !player.inventory.addItemStackToInventory(stack))
					{
						player.dropPlayerItemWithRandomChoice(stack, false);
					} */
					player.inventory.mainInventory[player.inventory.currentItem] = FluidContainerRegistry.drainFluidContainer(player.inventory.getCurrentItem());
					// TODO gamerforEA code end

					player.inventoryContainer.detectAndSendChanges();
					tile.markDirty();
					world.markBlockForUpdate(x, y, z);
					world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "game.neutral.swim", 0.33F, 1F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F);
				}
			}
		}

		TileEntity tile1;
		if (meta == 1 && !world.isRemote && !player.isSneaking() && player.getHeldItem() == null)
		{
			tile1 = world.getTileEntity(x, y, z);
			if (tile1 != null && tile1 instanceof TileAlembic)
			{
				TileAlembic alembic = (TileAlembic) tile1;
				String var24 = "";
				if (alembic.aspect != null && alembic.amount != 0)
				{
					if ((double) alembic.amount < (double) alembic.maxAmount * 0.4D)
					{
						var24 = StatCollector.translateToLocal("tile.alembic.msg.2");
					}
					else if ((double) alembic.amount < (double) alembic.maxAmount * 0.8D)
					{
						var24 = StatCollector.translateToLocal("tile.alembic.msg.3");
					}
					else if (alembic.amount < alembic.maxAmount)
					{
						var24 = StatCollector.translateToLocal("tile.alembic.msg.4");
					}
					else if (alembic.amount == alembic.maxAmount)
					{
						var24 = StatCollector.translateToLocal("tile.alembic.msg.5");
					}
				}
				else
				{
					var24 = StatCollector.translateToLocal("tile.alembic.msg.1");
				}

				player.addChatMessage(new ChatComponentTranslation("§3" + var24, new Object[0]));
				world.playSoundEffect((double) x, (double) y, (double) z, "thaumcraft:alembicknock", 0.2F, 1.0F);
			}
		}

		if (meta == 1)
		{
			tile1 = world.getTileEntity(x, y, z);
			if (tile1 != null && tile1 instanceof TileAlembic)
			{
				if (player.isSneaking() && ((TileAlembic) tile1).aspectFilter != null)
				{
					((TileAlembic) tile1).aspectFilter = null;
					world.markBlockForUpdate(x, y, z);
					tile1.markDirty();
					if (world.isRemote)
					{
						world.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "thaumcraft:page", 1.0F, 1.1F, false);
					}
					else
					{
						ForgeDirection var26 = ForgeDirection.getOrientation(side);
						world.spawnEntityInWorld(new EntityItem(world, (double) ((float) x + 0.5F + (float) var26.offsetX / 3.0F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F + (float) var26.offsetZ / 3.0F), new ItemStack(ConfigItems.itemResource, 1, 13)));
					}

					return true;
				}

				if (player.isSneaking() && player.getHeldItem() == null)
				{
					((TileAlembic) tile1).amount = 0;
					((TileAlembic) tile1).aspect = null;
					if (world.isRemote)
					{
						world.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "thaumcraft:alembicknock", 0.2F, 1.0F, false);
						world.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
					}
				}
				else
				{
					if (player.getHeldItem() != null && ((TileAlembic) tile1).aspectFilter == null && player.getHeldItem().getItem() == ConfigItems.itemResource && player.getHeldItem().getItemDamage() == 13)
					{
						if (((TileAlembic) tile1).amount == 0 && ((IEssentiaContainerItem) ((IEssentiaContainerItem) player.getHeldItem().getItem())).getAspects(player.getHeldItem()) == null)
						{
							return true;
						}

						if (((TileAlembic) tile1).amount == 0 && ((IEssentiaContainerItem) ((IEssentiaContainerItem) player.getHeldItem().getItem())).getAspects(player.getHeldItem()) != null)
						{
							((TileAlembic) tile1).aspect = ((IEssentiaContainerItem) ((IEssentiaContainerItem) player.getHeldItem().getItem())).getAspects(player.getHeldItem()).getAspects()[0];
						}

						--player.getHeldItem().stackSize;
						((TileAlembic) tile1).aspectFilter = ((TileAlembic) tile1).aspect;
						world.markBlockForUpdate(x, y, z);
						tile1.markDirty();
						if (world.isRemote)
						{
							world.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "thaumcraft:page", 1.0F, 0.9F, false);
						}

						return true;
					}

					if (player.getHeldItem() != null && ((TileAlembic) tile1).amount > 0 && (player.getHeldItem().getItem() == ConfigItems.itemJarFilled || player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 0)) || player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 3))))
					{
						boolean var23 = false;
						tile = null;
						ItemStack var29;
						if (!player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 0)) && !player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 3)))
						{
							var29 = player.getHeldItem();
							if ((((ItemJarFilled) var29.getItem()).getAspects(var29) == null || ((ItemJarFilled) var29.getItem()).getAspects(var29).visSize() == 0 || ((ItemJarFilled) var29.getItem()).getAspects(var29).getAmount(((TileAlembic) tile1).aspect) > 0) && (((ItemJarFilled) var29.getItem()).getFilter(var29) == null || ((ItemJarFilled) var29.getItem()).getFilter(var29) == ((TileAlembic) tile1).aspect))
							{
								int var27 = Math.min(((ItemJarFilled) var29.getItem()).getAspects(var29) == null ? 64 : 64 - ((ItemJarFilled) var29.getItem()).getAspects(var29).visSize(), ((TileAlembic) tile1).amount);
								if (var29.getItemDamage() == 3)
								{
									var27 = ((TileAlembic) tile1).amount;
								}

								if (var27 > 0)
								{
									((TileAlembic) tile1).amount -= var27;
									AspectList var28 = ((ItemJarFilled) var29.getItem()).getAspects(var29);
									if (var28 == null)
									{
										var28 = new AspectList();
									}

									var28.add(((TileAlembic) tile1).aspect, var27);
									if (var28.getAmount(((TileAlembic) tile1).aspect) > 64)
									{
										int var30 = var28.getAmount(((TileAlembic) tile1).aspect) - 64;
										var28.reduce(((TileAlembic) tile1).aspect, var30);
									}

									((ItemJarFilled) var29.getItem()).setAspects(var29, var28);
									if (((TileAlembic) tile1).amount <= 0)
									{
										((TileAlembic) tile1).aspect = null;
									}

									var23 = true;
									player.setCurrentItemOrArmor(0, var29);
								}
							}
						}
						else
						{
							var29 = new ItemStack(ConfigItems.itemJarFilled, 1, player.getHeldItem().getItemDamage());
							var23 = true;
							((ItemJarFilled) var29.getItem()).setAspects(var29, (new AspectList()).add(((TileAlembic) tile1).aspect, ((TileAlembic) tile1).amount));
							((TileAlembic) tile1).amount = 0;
							((TileAlembic) tile1).aspect = null;
							--player.getHeldItem().stackSize;
							if (!player.inventory.addItemStackToInventory(var29) && !world.isRemote)
							{
								world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, var29));
							}
						}

						if (var23)
						{
							tile1.markDirty();
							world.markBlockForUpdate(x, y, z);
							if (world.isRemote)
							{
								world.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
							}
						}

						return true;
					}
				}
			}
		}

		if (meta == 5)
		{
			world.setBlockMetadataWithNotify(x, y, z, 6, 2);
			world.playAuxSFXAtEntity(player, 1003, x, y, z, 0);
			return true;
		}
		else if (meta == 6)
		{
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
			world.playAuxSFXAtEntity(player, 1003, x, y, z, 0);
			return true;
		}
		else if (world.isRemote)
		{
			return true;
		}
		else
		{
			if (meta == 10)
			{
				tile1 = world.getTileEntity(x, y, z);
				if (tile1 instanceof TileThaumatorium && !player.isSneaking())
				{
					player.openGui(Thaumcraft.instance, 3, world, x, y, z);
					return true;
				}
			}

			if (meta == 11)
			{
				tile1 = world.getTileEntity(x, y - 1, z);
				if (tile1 instanceof TileThaumatorium && !player.isSneaking())
				{
					player.openGui(Thaumcraft.instance, 3, world, x, y - 1, z);
					return true;
				}
			}

			if ((meta == 14 || meta == 2) && !world.isRemote && !player.isSneaking() && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemShard)
			{
				tile1 = world.getTileEntity(x, y, z);
				if (tile1 != null && tile1 instanceof TileVisRelay)
				{
					TileVisRelay var25 = (TileVisRelay) tile1;
					byte var31 = (byte) player.getHeldItem().getItemDamage();
					if (var31 != var25.color && var31 != 6)
					{
						var25.color = var31;
					}
					else
					{
						var25.color = -1;
					}

					var25.removeThisNode();
					var25.nodeRefresh = true;
					var25.markDirty();
					world.markBlockForUpdate(x, y, z);
					world.playSoundEffect((double) x, (double) y, (double) z, "thaumcraft:crystal", 0.2F, 1.0F);
				}
			}

			return super.onBlockActivated(world, x, y, z, player, side, par7, par8, par9);
		}
	}

	public void onPoweredBlockChange(World par1World, int par2, int par3, int par4, boolean flag)
	{
		int l = par1World.getBlockMetadata(par2, par3, par4);
		if (l == 5 && flag)
		{
			par1World.setBlockMetadataWithNotify(par2, par3, par4, 6, 2);
			par1World.playAuxSFXAtEntity((EntityPlayer) null, 1003, par2, par3, par4, 0);
		}
		else if (l == 6 && !flag)
		{
			par1World.setBlockMetadataWithNotify(par2, par3, par4, 5, 2);
			par1World.playAuxSFXAtEntity((EntityPlayer) null, 1003, par2, par3, par4, 0);
		}

	}

	public void onBlockPlacedBy(World world, int par2, int par3, int par4, EntityLivingBase ent, ItemStack stack)
	{
		int l = MathHelper.floor_double((double) (ent.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		if (stack.getItemDamage() == 1)
		{
			TileEntity tile = world.getTileEntity(par2, par3, par4);
			if (tile instanceof TileAlembic)
			{
				if (l == 0)
				{
					((TileAlembic) tile).facing = 2;
				}

				if (l == 1)
				{
					((TileAlembic) tile).facing = 5;
				}

				if (l == 2)
				{
					((TileAlembic) tile).facing = 3;
				}

				if (l == 3)
				{
					((TileAlembic) tile).facing = 4;
				}
			}
		}

	}

	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		int md = world.getBlockMetadata(x, y, z);
		if (md == 3)
		{
			return 11;
		}
		else if (md == 7)
		{
			return 15;
		}
		else
		{
			TileEntity te;
			if (md == 8)
			{
				te = world.getTileEntity(x, y, z);
				if (te != null && te instanceof TileArcaneLampGrowth)
				{
					if (((TileArcaneLampGrowth) te).charges > 0)
					{
						return 15;
					}

					return 8;
				}
			}
			else if (md == 13)
			{
				te = world.getTileEntity(x, y, z);
				if (te != null && te instanceof TileArcaneLampFertility)
				{
					if (((TileArcaneLampFertility) te).charges > 0)
					{
						return 15;
					}

					return 8;
				}
			}
			else if (md == 14)
			{
				te = world.getTileEntity(x, y, z);
				if (te != null && te instanceof TileVisRelay)
				{
					if (VisNetHandler.isNodeValid(((TileVisRelay) te).getParent()))
					{
						return 10;
					}

					return 2;
				}
			}

			return super.getLightValue(world, x, y, z);
		}
	}

	// $FF: synthetic class
	static class SyntheticClass_1
	{
		// $FF: synthetic field
		static final int[] $SwitchMap$net$minecraftforge$common$util$ForgeDirection = new int[ForgeDirection.values().length];

		static
		{
			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.UP.ordinal()] = 1;
			}
			catch (NoSuchFieldError var6)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.DOWN.ordinal()] = 2;
			}
			catch (NoSuchFieldError var5)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.EAST.ordinal()] = 3;
			}
			catch (NoSuchFieldError var4)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.WEST.ordinal()] = 4;
			}
			catch (NoSuchFieldError var3)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.SOUTH.ordinal()] = 5;
			}
			catch (NoSuchFieldError var2)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.NORTH.ordinal()] = 6;
			}
			catch (NoSuchFieldError var1)
			{
				;
			}

		}
	}
}
