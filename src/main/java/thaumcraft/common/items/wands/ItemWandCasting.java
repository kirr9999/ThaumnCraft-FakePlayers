package thaumcraft.common.items.wands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.api.wands.StaffRod;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.api.wands.WandTriggerRegistry;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileOwned;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWandCasting extends Item implements IArchitect
{
	private IIcon icon;
	DecimalFormat myFormatter = new DecimalFormat("#######.##");
	public ItemFocusBasic.WandFocusAnimation animation = null;

	public ItemWandCasting()
	{
		this.maxStackSize = 1;
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	public boolean isDamageable()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.icon = par1IconRegister.registerIcon("thaumcraft:blank");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass)
	{
		return this.icon;
	}

	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	public int getMaxVis(ItemStack stack)
	{
		return this.getRod(stack).getCapacity() * (this.isSceptre(stack) ? 150 : 100);
	}

	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.uncommon;
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		ItemStack w1 = new ItemStack(this, 1, 0);
		ItemStack w2 = new ItemStack(this, 1, 9);
		ItemStack w3 = new ItemStack(this, 1, 54);
		((ItemWandCasting) w2.getItem()).setCap(w2, ConfigItems.WAND_CAP_GOLD);
		((ItemWandCasting) w3.getItem()).setCap(w3, ConfigItems.WAND_CAP_THAUMIUM);
		((ItemWandCasting) w2.getItem()).setRod(w2, ConfigItems.WAND_ROD_GREATWOOD);
		((ItemWandCasting) w3.getItem()).setRod(w3, ConfigItems.WAND_ROD_SILVERWOOD);
		ItemStack sceptre = new ItemStack(ConfigItems.itemWandCasting, 1, 128);
		((ItemWandCasting) sceptre.getItem()).setCap(sceptre, ConfigItems.WAND_CAP_THAUMIUM);
		((ItemWandCasting) sceptre.getItem()).setRod(sceptre, ConfigItems.WAND_ROD_SILVERWOOD);
		sceptre.setTagInfo("sceptre", new NBTTagByte((byte) 1));
		Iterator i$ = Aspect.getPrimalAspects().iterator();

		while (i$.hasNext())
		{
			Aspect aspect = (Aspect) i$.next();
			((ItemWandCasting) w1.getItem()).addVis(w1, aspect, ((ItemWandCasting) w1.getItem()).getMaxVis(w1), true);
			((ItemWandCasting) w2.getItem()).addVis(w2, aspect, ((ItemWandCasting) w2.getItem()).getMaxVis(w2), true);
			((ItemWandCasting) w3.getItem()).addVis(w3, aspect, ((ItemWandCasting) w3.getItem()).getMaxVis(w3), true);
			((ItemWandCasting) sceptre.getItem()).addVis(sceptre, aspect, ((ItemWandCasting) sceptre.getItem()).getMaxVis(sceptre), true);
		}

		par3List.add(w1);
		par3List.add(w2);
		par3List.add(w3);
		par3List.add(sceptre);
	}

	public String getItemStackDisplayName(ItemStack is)
	{
		String name = StatCollector.translateToLocal("item.Wand.name");
		name = name.replace("%CAP", StatCollector.translateToLocal("item.Wand." + this.getCap(is).getTag() + ".cap"));
		String rod = this.getRod(is).getTag();
		if (rod.indexOf("_staff") >= 0)
		{
			rod = rod.substring(0, this.getRod(is).getTag().indexOf("_staff"));
		}

		name = name.replace("%ROD", StatCollector.translateToLocal("item.Wand." + rod + ".rod"));
		name = name.replace("%OBJ", this.isStaff(is) ? StatCollector.translateToLocal("item.Wand.staff.obj") : (this.isSceptre(is) ? StatCollector.translateToLocal("item.Wand.sceptre.obj") : StatCollector.translateToLocal("item.Wand.wand.obj")));
		return name;
	}

	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		int pos = list.size();
		String tt2 = "";
		if (stack.hasTagCompound())
		{
			String tt = "";
			int tot = 0;
			int num = 0;
			Iterator i$ = Aspect.getPrimalAspects().iterator();

			while (i$.hasNext())
			{
				Aspect aspect = (Aspect) i$.next();
				if (stack.stackTagCompound.hasKey(aspect.getTag()))
				{
					String amount = this.myFormatter.format((double) ((float) stack.stackTagCompound.getInteger(aspect.getTag()) / 100.0F));
					float mod = this.getConsumptionModifier(stack, player, aspect, false);
					String consumption = this.myFormatter.format((double) (mod * 100.0F));
					++num;
					tot = (int) ((float) tot + mod * 100.0F);
					String text = "";
					ItemStack focus = this.getFocusItem(stack);
					if (focus != null)
					{
						int amt = ((ItemFocusBasic) focus.getItem()).getVisCost(focus).getAmount(aspect);
						if (amt > 0)
						{
							text = "§r, " + this.myFormatter.format((double) ((float) amt * mod / 100.0F)) + " " + StatCollector.translateToLocal(((ItemFocusBasic) focus.getItem()).isVisCostPerTick(focus) ? "item.Focus.cost2" : "item.Focus.cost1");
						}
					}

					if (Thaumcraft.proxy.isShiftKeyDown())
					{
						list.add(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount + ", §o(" + consumption + "% " + StatCollector.translateToLocal("tc.vis.cost") + ")" + text);
					}
					else
					{
						if (tt.length() > 0)
						{
							tt = tt + " | ";
						}

						tt = tt + "§" + aspect.getChatcolor() + amount + "§r";
					}
				}
			}

			if (!Thaumcraft.proxy.isShiftKeyDown() && num > 0)
			{
				list.add(tt);
				tot /= num;
				tt2 = " (" + tot + "% " + StatCollector.translateToLocal("tc.vis.costavg") + ")";
			}
		}

		list.add(pos, EnumChatFormatting.GOLD + StatCollector.translateToLocal("item.capacity.text") + " " + this.getMaxVis(stack) / 100 + "§r" + tt2);
		if (this.getFocus(stack) != null)
		{
			list.add(EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC + "" + EnumChatFormatting.GREEN + this.getFocus(stack).getItemStackDisplayName(this.getFocusItem(stack)));
			if (Thaumcraft.proxy.isShiftKeyDown())
			{
				this.getFocus(stack).addFocusInformation(this.getFocusItem(stack), player, list, par4);
			}
		}

	}

	public AspectList getAllVis(ItemStack is)
	{
		AspectList out = new AspectList();
		Iterator i$ = Aspect.getPrimalAspects().iterator();

		while (i$.hasNext())
		{
			Aspect aspect = (Aspect) i$.next();
			if (is.hasTagCompound() && is.stackTagCompound.hasKey(aspect.getTag()))
			{
				out.merge(aspect, is.stackTagCompound.getInteger(aspect.getTag()));
			}
			else
			{
				out.merge(aspect, 0);
			}
		}

		return out;
	}

	public AspectList getAspectsWithRoom(ItemStack wandstack)
	{
		AspectList out = new AspectList();
		AspectList cur = this.getAllVis(wandstack);
		Aspect[] arr$ = cur.getAspects();
		int len$ = arr$.length;

		for (int i$ = 0; i$ < len$; ++i$)
		{
			Aspect aspect = arr$[i$];
			if (cur.getAmount(aspect) < this.getMaxVis(wandstack))
			{
				out.add(aspect, 1);
			}
		}

		return out;
	}

	public void storeAllVis(ItemStack is, AspectList in)
	{
		Aspect[] arr$ = in.getAspects();
		int len$ = arr$.length;

		for (int i$ = 0; i$ < len$; ++i$)
		{
			Aspect aspect = arr$[i$];
			is.setTagInfo(aspect.getTag(), new NBTTagInt(in.getAmount(aspect)));
		}

	}

	public int getVis(ItemStack is, Aspect aspect)
	{
		int out = 0;
		if (is != null && aspect != null && is.hasTagCompound() && is.stackTagCompound.hasKey(aspect.getTag()))
		{
			out = is.stackTagCompound.getInteger(aspect.getTag());
		}

		return out;
	}

	public void storeVis(ItemStack is, Aspect aspect, int amount)
	{
		is.setTagInfo(aspect.getTag(), new NBTTagInt(amount));
	}

	public float getConsumptionModifier(ItemStack is, EntityPlayer player, Aspect aspect, boolean crafting)
	{
		float consumptionModifier = 1.0F;
		if (this.getCap(is).getSpecialCostModifierAspects() != null && this.getCap(is).getSpecialCostModifierAspects().contains(aspect))
		{
			consumptionModifier = this.getCap(is).getSpecialCostModifier();
		}
		else
		{
			consumptionModifier = this.getCap(is).getBaseCostModifier();
		}

		if (player != null)
		{
			consumptionModifier -= WandManager.getTotalVisDiscount(player, aspect);
		}

		if (this.getFocus(is) != null && !crafting)
		{
			consumptionModifier -= (float) this.getFocusFrugal(is) / 10.0F;
		}

		if (this.isSceptre(is))
		{
			consumptionModifier -= 0.1F;
		}

		return Math.max(consumptionModifier, 0.1F);
	}

	public int getFocusPotency(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.potency) + (this.hasRunes(itemstack) ? 1 : 0);
	}

	public int getFocusTreasure(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.treasure);
	}

	public int getFocusFrugal(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.frugal);
	}

	public int getFocusEnlarge(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.enlarge);
	}

	public int getFocusExtend(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.extend);
	}

	public boolean consumeVis(ItemStack is, EntityPlayer player, Aspect aspect, int amount, boolean crafting)
	{
		amount = (int) ((float) amount * this.getConsumptionModifier(is, player, aspect, crafting));
		if (this.getVis(is, aspect) >= amount)
		{
			this.storeVis(is, aspect, this.getVis(is, aspect) - amount);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean consumeAllVisCrafting(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit)
	{
		if (aspects != null && aspects.size() != 0)
		{
			AspectList nl = new AspectList();
			Aspect[] arr$ = aspects.getAspects();
			int len$ = arr$.length;

			for (int i$ = 0; i$ < len$; ++i$)
			{
				Aspect aspect = arr$[i$];
				int cost = aspects.getAmount(aspect) * 100;
				nl.add(aspect, cost);
			}

			return this.consumeAllVis(is, player, nl, doit, true);
		}
		else
		{
			return false;
		}
	}

	public boolean consumeAllVis(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit, boolean crafting)
	{
		if (aspects != null && aspects.size() != 0)
		{
			AspectList nl = new AspectList();
			Aspect[] arr$ = aspects.getAspects();
			int len$ = arr$.length;

			int i$;
			Aspect aspect;
			for (i$ = 0; i$ < len$; ++i$)
			{
				aspect = arr$[i$];
				int cost = aspects.getAmount(aspect);
				cost = (int) ((float) cost * this.getConsumptionModifier(is, player, aspect, crafting));
				nl.add(aspect, cost);
			}

			arr$ = nl.getAspects();
			len$ = arr$.length;

			for (i$ = 0; i$ < len$; ++i$)
			{
				aspect = arr$[i$];
				if (this.getVis(is, aspect) < nl.getAmount(aspect))
				{
					return false;
				}
			}

			if (doit && !player.worldObj.isRemote)
			{
				arr$ = nl.getAspects();
				len$ = arr$.length;

				for (i$ = 0; i$ < len$; ++i$)
				{
					aspect = arr$[i$];
					this.storeVis(is, aspect, this.getVis(is, aspect) - nl.getAmount(aspect));
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	public int addVis(ItemStack is, Aspect aspect, int amount, boolean doit)
	{
		if (!aspect.isPrimal())
		{
			return 0;
		}
		else
		{
			int storeAmount = this.getVis(is, aspect) + amount * 100;
			int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
			if (doit)
			{
				this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
			}

			return leftover / 100;
		}
	}

	public int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit)
	{
		if (!aspect.isPrimal())
		{
			return 0;
		}
		else
		{
			int storeAmount = this.getVis(is, aspect) + amount;
			int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
			if (doit)
			{
				this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
			}

			return leftover;
		}
	}

	public void onUpdate(ItemStack is, World w, Entity e, int slot, boolean currentItem)
	{
		if (!w.isRemote)
		{
			EntityPlayer player = (EntityPlayer) e;
			if (this.getRod(is).getOnUpdate() != null)
			{
				this.getRod(is).getOnUpdate().onUpdate(is, player);
			}
		}

	}

	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		Block bi = world.getBlock(x, y, z);
		int md = world.getBlockMetadata(x, y, z);
		// TODO gamerforEA code start
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(x, y, z, world, bi, md, player);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) return super.onItemUseFirst(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
		// TODO gamerforEA code end
		boolean result = false;
		ForgeDirection direction = ForgeDirection.getOrientation(side);
		if (bi instanceof IWandable)
		{
			int tile = ((IWandable) bi).onWandRightClick(world, itemstack, player, x, y, z, side, md);
			if (tile >= 0)
			{
				return tile == 1;
			}
		}

		TileEntity tile1 = world.getTileEntity(x, y, z);
		if (tile1 != null && tile1 instanceof IWandable)
		{
			int ret = ((IWandable) tile1).onWandRightClick(world, itemstack, player, x, y, z, side, md);
			if (ret >= 0)
			{
				return ret == 1;
			}
		}

		if (WandTriggerRegistry.hasTrigger(bi, md))
		{
			return WandTriggerRegistry.performTrigger(world, itemstack, player, x, y, z, side, bi, md);
		}
		else
		{
			if ((bi == ConfigBlocks.blockWoodenDevice && md == 2 || bi == ConfigBlocks.blockCosmeticOpaque && md == 2) && (!Config.wardedStone || tile1 != null && tile1 instanceof TileOwned && player.getCommandSenderName().equals(((TileOwned) tile1).owner)))
			{
				if (!world.isRemote)
				{
					((TileOwned) tile1).safeToRemove = true;
					world.spawnEntityInWorld(new EntityItem(world, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, new ItemStack(bi, 1, md)));
					world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(bi) + (md << 12));
					world.setBlockToAir(x, y, z);
				}
				else
				{
					player.swingItem();
				}
			}

			if (bi == ConfigBlocks.blockArcaneDoor && (!Config.wardedStone || tile1 != null && tile1 instanceof TileOwned && player.getCommandSenderName().equals(((TileOwned) tile1).owner)))
			{
				if (!world.isRemote)
				{
					((TileOwned) tile1).safeToRemove = true;
					if ((md & 8) == 0)
					{
						tile1 = world.getTileEntity(x, y + 1, z);
					}
					else
					{
						tile1 = world.getTileEntity(x, y - 1, z);
					}

					if (tile1 != null && tile1 instanceof TileOwned)
					{
						((TileOwned) tile1).safeToRemove = true;
					}

					if (Config.wardedStone || !Config.wardedStone && (md & 8) == 0)
					{
						world.spawnEntityInWorld(new EntityItem(world, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, new ItemStack(ConfigItems.itemArcaneDoor)));
					}

					world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(bi) + (md << 12));
					world.setBlockToAir(x, y, z);
				}
				else
				{
					player.swingItem();
				}
			}

			return result;
		}
	}

	public ItemFocusBasic getFocus(ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("focus"))
		{
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("focus");
			return (ItemFocusBasic) ItemStack.loadItemStackFromNBT(nbt).getItem();
		}
		else
		{
			return null;
		}
	}

	public ItemStack getFocusItem(ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("focus"))
		{
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("focus");
			return ItemStack.loadItemStackFromNBT(nbt);
		}
		else
		{
			return null;
		}
	}

	public void setFocus(ItemStack stack, ItemStack focus)
	{
		if (focus == null)
		{
			stack.stackTagCompound.removeTag("focus");
		}
		else
		{
			stack.setTagInfo("focus", focus.writeToNBT(new NBTTagCompound()));
		}
	}

	public WandRod getRod(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("rod") ? (WandRod) WandRod.rods.get(stack.stackTagCompound.getString("rod")) : ConfigItems.WAND_ROD_WOOD;
	}

	public boolean isStaff(ItemStack stack)
	{
		WandRod rod = this.getRod(stack);
		return rod != null && rod instanceof StaffRod;
	}

	public boolean isSceptre(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("sceptre");
	}

	public boolean hasRunes(ItemStack stack)
	{
		WandRod rod = this.getRod(stack);
		return rod != null && rod instanceof StaffRod && ((StaffRod) rod).hasRunes();
	}

	public void setRod(ItemStack stack, WandRod rod)
	{
		stack.setTagInfo("rod", new NBTTagString(rod.getTag()));
		if (rod instanceof StaffRod)
		{
			NBTTagList tags = new NBTTagList();
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("AttributeName", SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
			AttributeModifier am = new AttributeModifier(field_111210_e, "Weapon modifier", 6.0D, 0);
			tag.setString("Name", am.getName());
			tag.setDouble("Amount", am.getAmount());
			tag.setInteger("Operation", am.getOperation());
			tag.setLong("UUIDMost", am.getID().getMostSignificantBits());
			tag.setLong("UUIDLeast", am.getID().getLeastSignificantBits());
			tags.appendTag(tag);
			stack.stackTagCompound.setTag("AttributeModifiers", tags);
		}

	}

	public WandCap getCap(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("cap") ? (WandCap) WandCap.caps.get(stack.stackTagCompound.getString("cap")) : ConfigItems.WAND_CAP_IRON;
	}

	public void setCap(ItemStack stack, WandCap cap)
	{
		stack.setTagInfo("cap", new NBTTagString(cap.getTag()));
	}

	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
	{
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
		if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectType.BLOCK)
		{
			int x = movingobjectposition.blockX;
			int y = movingobjectposition.blockY;
			int z = movingobjectposition.blockZ;
			Block bi = world.getBlock(x, y, z);
			int meta = world.getBlockMetadata(x, y, z);
			// TODO gamerforEA code start
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(x, y, z, world, bi, meta, player);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) return super.onItemRightClick(itemstack, world, player);
			// TODO gamerforEA code end
			if (bi instanceof IWandable)
			{
				ItemStack stack = ((IWandable) bi).onWandRightClick(world, itemstack, player);
				if (stack != null)
				{
					return stack;
				}
			}

			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile != null && tile instanceof IWandable)
			{
				ItemStack stack = ((IWandable) tile).onWandRightClick(world, itemstack, player);
				if (stack != null)
				{
					return stack;
				}
			}
		}

		ItemFocusBasic focus1 = this.getFocus(itemstack);
		if (focus1 != null && !WandManager.isOnCooldown(player))
		{
			WandManager.setCooldown(player, focus1.getActivationCooldown(this.getFocusItem(itemstack)));
			ItemStack ret1 = focus1.onFocusRightClick(itemstack, world, player, movingobjectposition);
			if (ret1 != null)
			{
				return ret1;
			}
		}

		return super.onItemRightClick(itemstack, world, player);
	}

	public void setObjectInUse(ItemStack stack, int x, int y, int z)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}

		stack.stackTagCompound.setInteger("IIUX", x);
		stack.stackTagCompound.setInteger("IIUY", y);
		stack.stackTagCompound.setInteger("IIUZ", z);
	}

	public void clearObjectInUse(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}

		stack.stackTagCompound.removeTag("IIUX");
		stack.stackTagCompound.removeTag("IIUY");
		stack.stackTagCompound.removeTag("IIUZ");
	}

	public IWandable getObjectInUse(ItemStack stack, World world)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("IIUX"))
		{
			TileEntity te = world.getTileEntity(stack.stackTagCompound.getInteger("IIUX"), stack.stackTagCompound.getInteger("IIUY"), stack.stackTagCompound.getInteger("IIUZ"));
			if (te != null && te instanceof IWandable)
			{
				return (IWandable) te;
			}
		}

		return null;
	}

	public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
	{
		IWandable tv = this.getObjectInUse(stack, player.worldObj);
		if (tv != null)
		{
			this.animation = ItemFocusBasic.WandFocusAnimation.WAVE;
			tv.onUsingWandTick(stack, player, count);
		}
		else
		{
			ItemFocusBasic focus = this.getFocus(stack);
			if (focus != null && !WandManager.isOnCooldown(player))
			{
				WandManager.setCooldown(player, focus.getActivationCooldown(this.getFocusItem(stack)));
				focus.onUsingFocusTick(stack, player, count);
			}
		}
	}

	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int count)
	{
		IWandable tv = this.getObjectInUse(stack, player.worldObj);
		if (tv != null)
		{
			tv.onWandStoppedUsing(stack, world, player, count);
			this.animation = null;
		}
		else
		{
			ItemFocusBasic focus = this.getFocus(stack);
			if (focus != null)
			{
				focus.onPlayerStoppedUsingFocus(stack, world, player, count);
			}
		}

		this.clearObjectInUse(stack);
	}

	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.bow;
	}

	public int getMaxItemUseDuration(ItemStack itemstack)
	{
		return Integer.MAX_VALUE;
	}

	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
	{
		ItemStack focus = this.getFocusItem(stack);
		if (focus != null && !WandManager.isOnCooldown(entityLiving))
		{
			WandManager.setCooldown(entityLiving, this.getFocus(stack).getActivationCooldown(focus));
			return focus.getItem().onEntitySwing(entityLiving, stack);
		}
		else
		{
			return super.onEntitySwing(entityLiving, stack);
		}
	}

	public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player)
	{
		ItemFocusBasic focus = this.getFocus(itemstack);
		if (focus != null && !WandManager.isOnCooldown(player))
		{
			WandManager.setCooldown(player, focus.getActivationCooldown(this.getFocusItem(itemstack)));
			return focus.onFocusBlockStartBreak(itemstack, x, y, z, player);
		}
		else
		{
			return false;
		}
	}

	public boolean canHarvestBlock(Block par1Block, ItemStack itemstack)
	{
		ItemFocusBasic focus = this.getFocus(itemstack);
		return focus != null ? this.getFocusItem(itemstack).getItem().canHarvestBlock(par1Block, itemstack) : false;
	}

	public float func_150893_a(ItemStack itemstack, Block block)
	{
		ItemFocusBasic focus = this.getFocus(itemstack);
		return focus != null ? this.getFocusItem(itemstack).getItem().func_150893_a(itemstack, (Block) null) : super.func_150893_a(itemstack, block);
	}

	public ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, int side, EntityPlayer player)
	{
		ItemFocusBasic focus = this.getFocus(stack);
		return focus != null && focus instanceof IArchitect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect) ? ((IArchitect) focus).getArchitectBlocks(stack, world, x, y, z, side, player) : null;
	}

	public boolean showAxis(ItemStack stack, World world, EntityPlayer player, int side, IArchitect.EnumAxis axis)
	{
		ItemFocusBasic focus = this.getFocus(stack);
		return focus != null && focus instanceof IArchitect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect) ? ((IArchitect) focus).showAxis(stack, world, player, side, axis) : false;
	}
}