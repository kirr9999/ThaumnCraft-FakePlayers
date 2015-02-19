package thaumcraft.common.items.equipment;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemElementalPickaxe extends ItemPickaxe implements IRepairable
{
	public IIcon icon;

	public ItemElementalPickaxe(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	public Set<String> getToolClasses(ItemStack stack)
	{
		return ImmutableSet.of("pickaxe");
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:elementalpick");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return this.icon;
	}

	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.rare;
	}

	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	{
		// TODO gamerforEA code replace, old code: if (!player.worldObj.isRemote && (!(entity instanceof EntityPlayer) || MinecraftServer.getServer().isPVPEnabled()))
		if (!player.worldObj.isRemote && !(entity instanceof EntityPlayer))
		{
			entity.setFire(2);
		}

		return super.onLeftClickEntity(stack, player, entity);
	}

	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		itemstack.damageItem(5, player);
		if (!world.isRemote)
		{
			world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:wandfail", 0.2F, 0.2F + world.rand.nextFloat() * 0.2F);
			return super.onItemUse(itemstack, player, world, x, y, z, side, par8, par9, par10);
		}
		else
		{
			Minecraft mc = Minecraft.getMinecraft();
			Thaumcraft.instance.renderEventHandler.startScan(player, x, y, z, System.currentTimeMillis() + 5000L, 8);
			player.swingItem();
			return super.onItemUse(itemstack, player, world, x, y, z, side, par8, par9, par10);
		}
	}
}
