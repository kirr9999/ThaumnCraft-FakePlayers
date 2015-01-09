package thaumcraft.common.entities.golems;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGolemBell extends Item
{
	public IIcon icon;

	public ItemGolemBell()
	{
		this.setHasSubtypes(false);
		this.setCreativeTab(Thaumcraft.tabTC);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:ironbell");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return this.icon;
	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	public static int getGolemId(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("golemid") ? stack.stackTagCompound.getInteger("golemid") : -1;
	}

	public static int getGolemHomeFace(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("golemhomeface") ? stack.stackTagCompound.getInteger("golemhomeface") : -1;
	}

	public static ChunkCoordinates getGolemHomeCoords(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("golemhomex") ? new ChunkCoordinates(stack.stackTagCompound.getInteger("golemhomex"), stack.stackTagCompound.getInteger("golemhomey"), stack.stackTagCompound.getInteger("golemhomez")) : null;
	}

	public static ArrayList<Marker> getMarkers(ItemStack stack)
	{
		ArrayList<Marker> markers = new ArrayList();
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("markers"))
		{
			NBTTagList tl = stack.stackTagCompound.getTagList("markers", 10);

			for (int i = 0; i < tl.tagCount(); ++i)
			{
				NBTTagCompound nbt = tl.getCompoundTagAt(i);
				int x = nbt.getInteger("x");
				int y = nbt.getInteger("y");
				int z = nbt.getInteger("z");
				int dim = nbt.getInteger("dim");
				byte s = nbt.getByte("side");
				byte c = nbt.getByte("color");
				markers.add(new Marker(x, y, z, (byte) dim, s, c));
			}
		}

		return markers;
	}

	public static void resetMarkers(ItemStack stack, World world, EntityPlayer player)
	{
		Entity golem = null;
		int id = getGolemId(stack);
		if (id > -1)
		{
			golem = world.getEntityByID(id);
			if (golem != null && golem instanceof EntityGolemBase)
			{
				stack.setTagInfo("markers", new NBTTagList());
				((EntityGolemBase) golem).setMarkers(new ArrayList());
				world.playSoundAtEntity(player, "random.orb", 0.7F, 1.0F + world.rand.nextFloat() * 0.1F);
			}
		}
	}

	public static void changeMarkers(ItemStack stack, EntityPlayer player, World world, int par4, int par5, int par6, int side)
	{
		Entity golem = null;
		ArrayList<Marker> markers = getMarkers(stack);
		boolean markMultipleColors = false;
		int id = getGolemId(stack);
		if (id > -1)
		{
			golem = world.getEntityByID(id);
			if (golem != null && golem instanceof EntityGolemBase && ((EntityGolemBase) golem).getUpgradeAmount(4) > 0)
			{
				markMultipleColors = true;
			}
		}

		int count = markers.size();
		int index = -1;
		int color = 0;
		if (!markMultipleColors)
		{
			index = markers.indexOf(new Marker(par4, par5, par6, world.provider.dimensionId, (byte) side, (byte) -1));
		}
		else
		{
			for (int i = -1; i < 16; ++i)
			{
				index = markers.indexOf(new Marker(par4, par5, par6, world.provider.dimensionId, (byte) side, (byte) i));
				color = i;
				if (index != -1)
				{
					break;
				}
			}
		}

		if (index >= 0)
		{
			markers.remove(index);
			if (markMultipleColors && !player.isSneaking())
			{
				++color;
				if (color <= 15)
				{
					markers.add(new Marker(par4, par5, par6, world.provider.dimensionId, (byte) side, (byte) color));
					++count;
					if (world.isRemote)
					{
						String s = StatCollector.translateToLocal("tc.markerchange");
						if (color > -1)
						{
							s = s.replaceAll("%n", UtilsFX.colorNames[color]);
						}
						else
						{
							s = StatCollector.translateToLocal("tc.markerchangeany");
						}

						PlayerNotifications.addNotification(s);
					}
				}
			}
		}
		else
		{
			markers.add(new Marker(par4, par5, par6, world.provider.dimensionId, (byte) side, (byte) -1));
		}

		if (count != markers.size())
		{
			NBTTagList nbtList = new NBTTagList();
			
			for (Marker marker : markers)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("x", marker.x);
				nbt.setInteger("y", marker.y);
				nbt.setInteger("z", marker.z);
				nbt.setInteger("dim", marker.dim);
				nbt.setByte("side", marker.side);
				nbt.setByte("color", marker.color);
				nbtList.appendTag(nbt);
			}

			stack.setTagInfo("markers", nbtList);
			if (id > -1)
			{
				if (golem != null && golem instanceof EntityGolemBase)
				{
					((EntityGolemBase) golem).setMarkers(markers);
				}
				else
				{
					stack.getTagCompound().removeTag("golemid");
					stack.getTagCompound().removeTag("markers");
					stack.getTagCompound().removeTag("golemhomex");
					stack.getTagCompound().removeTag("golemhomey");
					stack.getTagCompound().removeTag("golemhomez");
					stack.getTagCompound().removeTag("golemhomeface");
				}
			}
		}

		world.playSoundEffect((double) par4, (double) par5, (double) par6, "random.orb", 0.7F, 1.0F + world.rand.nextFloat() * 0.1F);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int par4, int par5, int par6, int side, float par8, float par9, float par10)
	{
		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);
		if (mop == null)
		{
			return true;
		}
		else
		{
			if (mop.typeOfHit == MovingObjectType.BLOCK)
			{
				int x = mop.blockX;
				int y = mop.blockY;
				int z = mop.blockZ;
				changeMarkers(stack, player, world, x, y, z, mop.sideHit);
			}

			return !world.isRemote;
		}
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target)
	{
		if (target instanceof EntityGolemBase)
		{
			// TODO gamerforEA code start
			EntityGolemBase golem = (EntityGolemBase) target;
			if (!(player.getGameProfile().getName().equals(golem.ownerName) && player.getGameProfile().getId().equals(golem.ownerUUID))) return false;
			// TODO gamerforEA code end
			if (stack.hasTagCompound())
			{
				stack.getTagCompound().removeTag("golemid");
				stack.getTagCompound().removeTag("markers");
				stack.getTagCompound().removeTag("golemhomex");
				stack.getTagCompound().removeTag("golemhomey");
				stack.getTagCompound().removeTag("golemhomez");
				stack.getTagCompound().removeTag("golemhomeface");
			}

			if (target.worldObj.isRemote)
			{
				if (player != null)
				{
					player.swingItem();
				}
			}
			else
			{
				ArrayList<Marker> markers = ((EntityGolemBase) target).getMarkers();
				NBTTagList nbtList = new NBTTagList();
				
				for (Marker marker : markers)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setInteger("x", marker.x);
					nbt.setInteger("y", marker.y);
					nbt.setInteger("z", marker.z);
					nbt.setInteger("dim", marker.dim);
					nbt.setByte("side", marker.side);
					nbt.setByte("color", marker.color);
					nbtList.appendTag(nbt);
				}

				stack.setTagInfo("markers", nbtList);
				stack.getTagCompound().setInteger("golemid", target.getEntityId());
				stack.getTagCompound().setInteger("golemhomex", ((EntityGolemBase) target).getHomePosition().posX);
				stack.getTagCompound().setInteger("golemhomey", ((EntityGolemBase) target).getHomePosition().posY);
				stack.getTagCompound().setInteger("golemhomez", ((EntityGolemBase) target).getHomePosition().posZ);
				stack.getTagCompound().setInteger("golemhomeface", ((EntityGolemBase) target).homeFacing);
				target.worldObj.playSoundAtEntity(target, "random.orb", 0.7F, 1.0F + target.worldObj.rand.nextFloat() * 0.1F);
				if (player != null && player.capabilities.isCreativeMode)
				{
					player.setCurrentItemOrArmor(0, stack.copy());
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	{
		if (entity instanceof EntityTravelingTrunk && !entity.isDead)
		{
			byte upgrade = (byte) ((EntityTravelingTrunk) entity).getUpgrade();
			if (upgrade == 3 && !((EntityTravelingTrunk) entity).func_152113_b().equals(player.getCommandSenderName()))
			{
				return false;
			}
			else if (entity.worldObj.isRemote && entity instanceof EntityLiving)
			{
				((EntityLiving) entity).spawnExplosionParticle();
				return false;
			}
			else
			{
				ItemStack stack1 = new ItemStack(ConfigItems.itemTrunkSpawner);
				if (player.isSneaking())
				{
					if (upgrade > -1 && entity.worldObj.rand.nextBoolean())
					{
						((EntityTravelingTrunk) entity).entityDropItem(new ItemStack(ConfigItems.itemGolemUpgrade, 1, upgrade), 0.5F);
					}
				}
				else
				{
					if (((EntityTravelingTrunk) entity).hasCustomNameTag())
					{
						stack1.setStackDisplayName(((EntityTravelingTrunk) entity).getCustomNameTag());
					}

					stack1.setTagInfo("upgrade", new NBTTagByte(upgrade));
					if (upgrade == 4)
					{
						stack1.setTagInfo("inventory", ((EntityTravelingTrunk) entity).inventory.writeToNBT(new NBTTagList()));
					}
				}

				((EntityTravelingTrunk) entity).entityDropItem(stack1, 0.5F);
				if (upgrade != 4 || player.isSneaking())
				{
					((EntityTravelingTrunk) entity).inventory.dropAllItems();
				}

				entity.worldObj.playSoundAtEntity(entity, "thaumcraft:zap", 0.5F, 1.0F);
				entity.setDead();
				return true;
			}
		}
		else if (entity instanceof EntityGolemBase && !entity.isDead)
		{
			if (entity.worldObj.isRemote && entity instanceof EntityLiving)
			{
				((EntityLiving) entity).spawnExplosionParticle();
				return false;
			}
			else
			{
				// TODO gamerforEA code start
				EntityGolemBase golem = (EntityGolemBase) entity;
				if (!(player.getGameProfile().getName().equals(golem.ownerName) && player.getGameProfile().getId().equals(golem.ownerUUID))) return false;
				// TODO gamerforEA code end
				int type = ((EntityGolemBase) entity).golemType.ordinal();
				String deco = ((EntityGolemBase) entity).decoration;
				byte core = ((EntityGolemBase) entity).getCore();
				byte[] upgrades = ((EntityGolemBase) entity).upgrades;
				boolean advanced = ((EntityGolemBase) entity).advanced;
				ItemStack stack1 = new ItemStack(ConfigItems.itemGolemPlacer, 1, type);
				if (advanced)
				{
					stack1.setTagInfo("advanced", new NBTTagByte((byte) 1));
				}

				if (player.isSneaking())
				{
					if (core > -1)
					{
						((EntityGolemBase) entity).entityDropItem(new ItemStack(ConfigItems.itemGolemCore, 1, core), 0.5F);
					}

					for (int i = 0; i < upgrades.length; ++i)
					{
						byte l = upgrades[i];
						if (l > -1 && entity.worldObj.rand.nextBoolean())
						{
							((EntityGolemBase) entity).entityDropItem(new ItemStack(ConfigItems.itemGolemUpgrade, 1, l), 0.5F);
						}
					}
				}
				else
				{
					if (((EntityGolemBase) entity).hasCustomNameTag())
					{
						stack1.setStackDisplayName(((EntityGolemBase) entity).getCustomNameTag());
					}

					if (deco.length() > 0)
					{
						stack1.setTagInfo("deco", new NBTTagString(deco));
					}

					if (core > -1)
					{
						stack1.setTagInfo("core", new NBTTagByte(core));
					}

					stack1.setTagInfo("upgrades", new NBTTagByteArray(upgrades));
					ArrayList<Marker> markers = ((EntityGolemBase) entity).getMarkers();
					NBTTagList nbtList = new NBTTagList();
					
					for (Marker marker : markers)
					{
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setInteger("x", marker.x);
						nbt.setInteger("y", marker.y);
						nbt.setInteger("z", marker.z);
						nbt.setInteger("dim", marker.dim);
						nbt.setByte("side", marker.side);
						nbt.setByte("color", marker.color);
						nbtList.appendTag(nbt);
					}

					stack1.setTagInfo("markers", nbtList);
					stack1.setTagInfo("Inventory", ((EntityGolemBase) entity).inventory.writeToNBT(new NBTTagList()));
				}

				((EntityGolemBase) entity).entityDropItem(stack1, 0.5F);
				((EntityGolemBase) entity).dropStuff();
				entity.worldObj.playSoundAtEntity(entity, "thaumcraft:zap", 0.5F, 1.0F);
				entity.setDead();
				return true;
			}
		}
		else
		{
			return false;
		}
	}
}