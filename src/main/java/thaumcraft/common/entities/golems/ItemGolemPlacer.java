package thaumcraft.common.entities.golems;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGolemPlacer extends Item
{
	public IIcon[] iconGolem = new IIcon[8];
	public IIcon iconAdvanced;
	public IIcon iconCore;
	private IIcon iconBlank;

	public ItemGolemPlacer()
	{
		this.setHasSubtypes(true);
		this.setCreativeTab(Thaumcraft.tabTC);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.iconGolem[0] = ir.registerIcon("thaumcraft:golem_straw");
		this.iconGolem[1] = ir.registerIcon("thaumcraft:golem_wood");
		this.iconGolem[2] = ir.registerIcon("thaumcraft:golem_tallow");
		this.iconGolem[3] = ir.registerIcon("thaumcraft:golem_clay");
		this.iconGolem[4] = ir.registerIcon("thaumcraft:golem_flesh");
		this.iconGolem[5] = ir.registerIcon("thaumcraft:golem_stone");
		this.iconGolem[6] = ir.registerIcon("thaumcraft:golem_iron");
		this.iconGolem[7] = ir.registerIcon("thaumcraft:golem_thaumium");
		this.iconAdvanced = ir.registerIcon("thaumcraft:golem_over_adv");
		this.iconCore = ir.registerIcon("thaumcraft:golem_over_core");
		this.iconBlank = ir.registerIcon("thaumcraft:blank");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderPasses(int metadata)
	{
		return 3;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return this.iconGolem[par1];
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass)
	{
		return pass == 0 ? super.getIcon(stack, pass) : (pass == 1 && stack.hasTagCompound() && stack.stackTagCompound.hasKey("advanced") ? this.iconAdvanced : (pass == 2 && stack.hasTagCompound() && stack.stackTagCompound.hasKey("core") ? this.iconCore : this.iconBlank));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	{
		return super.getUnlocalizedName() + "." + par1ItemStack.getItemDamage();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		if (stack.hasTagCompound())
		{
			if (stack.stackTagCompound.hasKey("core"))
			{
				list.add(StatCollector.translateToLocal("item.ItemGolemCore.name") + ": §6" + StatCollector.translateToLocal("item.ItemGolemCore." + stack.stackTagCompound.getByte("core") + ".name"));
			}

			if (stack.stackTagCompound.hasKey("advanced"))
			{
				list.add(StatCollector.translateToLocal("tc.adv"));
			}

			String deco;
			if (stack.stackTagCompound.hasKey("upgrades"))
			{
				byte[] decoDesc = stack.stackTagCompound.getByteArray("upgrades");
				deco = "§9";
				byte[] arr$ = decoDesc;
				int len$ = decoDesc.length;

				for (int i$ = 0; i$ < len$; ++i$)
				{
					byte b = arr$[i$];
					if (b > -1)
					{
						deco = deco + StatCollector.translateToLocal("item.ItemGolemUpgrade." + b + ".name") + " ";
					}
				}

				list.add(deco);
			}

			if (stack.stackTagCompound.hasKey("markers"))
			{
				NBTTagList var11 = stack.stackTagCompound.getTagList("markers", 10);
				list.add("§5" + var11.tagCount() + " " + StatCollector.translateToLocal("tc.markedloc"));
			}

			if (stack.stackTagCompound.hasKey("deco"))
			{
				String var12 = "§2";
				deco = stack.stackTagCompound.getString("deco");
				if (deco.contains("H"))
				{
					var12 = var12 + StatCollector.translateToLocal("item.ItemGolemDecoration.0.name") + " ";
				}

				if (deco.contains("G"))
				{
					var12 = var12 + StatCollector.translateToLocal("item.ItemGolemDecoration.1.name") + " ";
				}

				if (deco.contains("B"))
				{
					var12 = var12 + StatCollector.translateToLocal("item.ItemGolemDecoration.2.name") + " ";
				}

				if (deco.contains("F"))
				{
					var12 = var12 + StatCollector.translateToLocal("item.ItemGolemDecoration.3.name") + " ";
				}

				if (deco.contains("R"))
				{
					var12 = var12 + StatCollector.translateToLocal("item.ItemGolemDecoration.4.name") + " ";
				}

				if (deco.contains("V"))
				{
					var12 = var12 + StatCollector.translateToLocal("item.ItemGolemDecoration.5.name") + " ";
				}

				if (deco.contains("P"))
				{
					var12 = var12 + StatCollector.translateToLocal("item.ItemGolemDecoration.6.name") + " ";
				}

				list.add(var12);
			}
		}
	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int par4, int par5, int par6, int side, float par8, float par9, float par10)
	{
		if (!world.isRemote && !player.isSneaking())
		{
			Block var11 = world.getBlock(par4, par5, par6);
			par4 += Facing.offsetsXForSide[side];
			par5 += Facing.offsetsYForSide[side];
			par6 += Facing.offsetsZForSide[side];
			double var12 = 0.0D;
			if (side == 1 && var11 == Blocks.fence || var11 == Blocks.nether_brick_fence)
			{
				var12 = 0.5D;
			}

			if (this.spawnCreature(world, (double) par4 + 0.5D, (double) par5 + var12, (double) par6 + 0.5D, side, stack, player) && !player.capabilities.isCreativeMode)
			{
				--stack.stackSize;
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int a = 0; a <= 7; ++a)
		{
			par3List.add(new ItemStack(this, 1, a));
		}

	}

	public boolean spawnCreature(World par0World, double par2, double par4, double par6, int side, ItemStack stack, EntityPlayer player)
	{
		boolean adv = false;
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("advanced"))
		{
			adv = true;
		}

		EntityGolemBase golem = new EntityGolemBase(par0World, EnumGolemType.getType(stack.getItemDamage()), adv);
		if (golem != null)
		{
			golem.setLocationAndAngles(par2, par4, par6, par0World.rand.nextFloat() * 360.0F, 0.0F);
			golem.playLivingSound();
			golem.setHomeArea(MathHelper.floor_double(par2), MathHelper.floor_double(par4), MathHelper.floor_double(par6), 32);
			if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("core"))
			{
				golem.setCore(stack.stackTagCompound.getByte("core"));
			}

			if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("upgrades"))
			{
				int upgrade = golem.upgrades.length;
				golem.upgrades = stack.stackTagCompound.getByteArray("upgrades");
				if (upgrade != golem.upgrades.length)
				{
					byte[] a = new byte[upgrade];

					int i;
					for (i = 0; i < upgrade; ++i)
					{
						a[i] = -1;
					}

					for (i = 0; i < golem.upgrades.length; ++i)
					{
						if (i < upgrade)
						{
							a[i] = golem.upgrades[i];
						}
					}

					golem.upgrades = a;
				}
			}

			String deco = "";
			if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("deco"))
			{
				deco = stack.stackTagCompound.getString("deco");
				golem.decoration = deco;
			}

			golem.setup(side);
			par0World.spawnEntityInWorld(golem);
			golem.setGolemDecoration(deco);
			golem.setOwner(player.getCommandSenderName());
			golem.setMarkers(ItemGolemBell.getMarkers(stack));
			int j = 0;

			for (int i = 0; i < golem.upgrades.length; ++i)
			{
				byte b = golem.upgrades[i];
				golem.setUpgrade(j, b);
				++j;
			}

			if (stack.hasDisplayName())
			{
				golem.setCustomNameTag(stack.getDisplayName());
				golem.func_110163_bv();
			}

			if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("Inventory"))
			{
				NBTTagList nbtList = stack.stackTagCompound.getTagList("Inventory", 10);
				golem.inventory.readFromNBT(nbtList);
			}

			// TODO gamerforEA code start
			golem.ownerName = player.getGameProfile().getName();
			golem.ownerUUID = player.getGameProfile().getId();
			// TODO gamerforEA code end
		}

		return golem != null;
	}
}