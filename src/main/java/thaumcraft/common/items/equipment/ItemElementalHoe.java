package thaumcraft.common.items.equipment;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockCustomPlant;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemElementalHoe extends ItemHoe implements IRepairable
{
	public IIcon icon;

	public ItemElementalHoe(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:elementalhoe");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return this.icon;
	}

	@Override
	public int getItemEnchantability()
	{
		return 5;
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.rare;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (player.isSneaking())
		{
			return super.onItemUse(stack, player, world, x, y, z, par7, par8, par9, par10);
		}
		else
		{
			boolean did = false;

			int md;
			for (int bi = -1; bi <= 1; ++bi)
			{
				for (md = -1; md <= 1; ++md)
				{
					// TODO gamerforEA code start
					BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(x + bi, y, z + md, world, world.getBlock(x + bi, y, z + md), md, player);
					MinecraftForge.EVENT_BUS.post(breakEvent);
					// TODO gamerforEA code end
					if (!breakEvent.isCanceled())
					{
						if (super.onItemUse(stack, player, world, x + bi, y, z + md, par7, par8, par9, par10))
						{
							Thaumcraft.proxy.blockSparkle(world, x + bi, y, z + md, 8401408, 2);
							if (!did)
							{
								did = true;
							}
						}
					}
				}
			}

			if (!did)
			{
				did = Utils.useBonemealAtLoc(world, player, x, y, z);
				if (!did)
				{
					Block var14 = world.getBlock(x, y, z);
					md = world.getBlockMetadata(x, y, z);
					if (var14 == ConfigBlocks.blockCustomPlant && md == 0 && stack.getItemDamage() + 20 <= stack.getMaxDamage())
					{
						((BlockCustomPlant) var14).growGreatTree(world, x, y, z, world.rand);
						stack.damageItem(5, player);
						Thaumcraft.proxy.blockSparkle(world, x, y, z, 0, 2);
						did = true;
					}
					else if (var14 == ConfigBlocks.blockCustomPlant && md == 1 && stack.getItemDamage() + 150 <= stack.getMaxDamage())
					{
						((BlockCustomPlant) var14).growSilverTree(world, x, y, z, world.rand);
						stack.damageItem(25, player);
						Thaumcraft.proxy.blockSparkle(world, x, y, z, 0, 2);
						did = true;
					}
				}
				else
				{
					stack.damageItem(1, player);
					Thaumcraft.proxy.blockSparkle(world, x, y, z, 0, 3);
				}

				if (did)
				{
					world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:wand", 0.75F, 0.9F + world.rand.nextFloat() * 0.2F);
				}
			}

			return did;
		}
	}
}