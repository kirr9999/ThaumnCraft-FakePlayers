package thaumcraft.common.items.equipment;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityFollowingItem;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockBubble;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemElementalAxe extends ItemAxe implements IRepairable
{
	public IIcon icon;
	boolean alternateServer = false;
	boolean alternateClient = false;
	public static ArrayList<List> oreDictLogs = new ArrayList();

	public ItemElementalAxe(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		return ImmutableSet.of("axe");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:elementalaxe");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return this.icon;
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
	public EnumAction getItemUseAction(ItemStack itemstack)
	{
		return EnumAction.bow;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack p_77626_1_)
	{
		return 72000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_)
	{
		p_77659_3_.setItemInUse(p_77659_1_, this.getMaxItemUseDuration(p_77659_1_));
		return p_77659_1_;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
	{
		List<Entity> stuff = EntityUtils.getEntitiesInRange(player.worldObj, player.posX, player.posY, player.posZ, player, EntityItem.class, 10.0D);
		if (stuff != null)
		{
			for (Entity entity : stuff)
			{
				if ((!(entity instanceof EntityFollowingItem) || ((EntityFollowingItem) entity).target == null) && !entity.isDead && entity instanceof EntityItem)
				{
					double d1 = entity.posX - player.posX;
					double d2 = entity.posY - player.posY + (double) (player.height / 2.0F);
					double d3 = entity.posZ - player.posZ;
					double d4 = (double) MathHelper.sqrt_double(d1 * d1 + d2 * d2 + d3 * d3);
					d1 /= d4;
					d2 /= d4;
					d3 /= d4;
					double d5 = 0.3D;
					entity.motionX -= d1 * d5;
					entity.motionY -= d2 * d5;
					entity.motionZ -= d3 * d5;
					if (entity.motionX > 0.35D)
					{
						entity.motionX = 0.35D;
					}

					if (entity.motionX < -0.35D)
					{
						entity.motionX = -0.35D;
					}

					if (entity.motionY > 0.35D)
					{
						entity.motionY = 0.35D;
					}

					if (entity.motionY < -0.35D)
					{
						entity.motionY = -0.35D;
					}

					if (entity.motionZ > 0.35D)
					{
						entity.motionZ = 0.35D;
					}

					if (entity.motionZ < -0.35D)
					{
						entity.motionZ = -0.35D;
					}

					Thaumcraft.proxy.crucibleBubble(player.worldObj, (float) entity.posX + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.125F, (float) entity.posY + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.125F, (float) entity.posZ + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.125F, 0.33F, 0.33F, 1.0F);
				}
			}
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player)
	{
		World world = player.worldObj;
		Block block = world.getBlock(x, y, z);
		if (!player.isSneaking() && Utils.isWoodLog(world, x, y, z))
		{
			if (!world.isRemote)
			{
				// TODO gamerforEA code start
				BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(x, y, z, world, block, world.getBlockMetadata(x, y, z), player);
				MinecraftForge.EVENT_BUS.post(breakEvent);
				// TODO gamerforEA code end
				if (!breakEvent.isCanceled())
				{
					BlockUtils.breakFurthestBlock(world, x, y, z, block, player, true, 10);
					PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockBubble(x, y, z, (new Color(0.33F, 0.33F, 1.0F)).getRGB()), new TargetPoint(world.provider.dimensionId, (double) x, (double) y, (double) z, 32.0D));
					world.playSoundEffect((double) x, (double) y, (double) z, "thaumcraft:bubble", 0.15F, 1.0F);
				}
			}

			itemstack.damageItem(1, player);
			return true;
		}
		else
		{
			return super.onBlockStartBreak(itemstack, x, y, z, player);
		}
	}
}