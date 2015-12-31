package thaumcraft.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.tiles.TileArcaneWorkbench;

public class ContainerArcaneWorkbench extends Container
{
	private TileArcaneWorkbench tileEntity;
	private InventoryPlayer ip;

	public ContainerArcaneWorkbench(InventoryPlayer par1InventoryPlayer, TileArcaneWorkbench e)
	{
		this.tileEntity = e;
		this.tileEntity.eventHandler = this;
		this.ip = par1InventoryPlayer;

		// TODO gamerforEA code start
		this.tileEntity.containers.put(this.ip.player, this);
		// TODO gamerforEA code end

		this.addSlotToContainer(new SlotCraftingArcaneWorkbench(par1InventoryPlayer.player, this.tileEntity, this.tileEntity, 9, 160, 64));
		this.addSlotToContainer(new SlotLimitedByWand(this.tileEntity, 10, 160, 24));

		for (int var6 = 0; var6 < 3; ++var6)
			for (int var7 = 0; var7 < 3; ++var7)
				this.addSlotToContainer(new Slot(this.tileEntity, var7 + var6 * 3, 40 + var7 * 24, 40 + var6 * 24));

		for (int var5 = 0; var5 < 3; ++var5)
			for (int var7 = 0; var7 < 9; ++var7)
				this.addSlotToContainer(new Slot(par1InventoryPlayer, var7 + var5 * 9 + 9, 16 + var7 * 18, 151 + var5 * 18));

		for (int var61 = 0; var61 < 9; ++var61)
			this.addSlotToContainer(new Slot(par1InventoryPlayer, var61, 16 + var61 * 18, 209));

		this.onCraftMatrixChanged(this.tileEntity);
	}

	@Override
	public void onCraftMatrixChanged(IInventory par1IInventory)
	{
		InventoryCrafting ic = new InventoryCrafting(new ContainerDummy(), 3, 3);

		for (int a = 0; a < 9; ++a)
			ic.setInventorySlotContents(a, this.tileEntity.getStackInSlot(a));

		this.tileEntity.setInventorySlotContentsSoftly(9, CraftingManager.getInstance().findMatchingRecipe(ic, this.tileEntity.getWorldObj()));
		if (this.tileEntity.getStackInSlot(9) == null && this.tileEntity.getStackInSlot(10) != null && this.tileEntity.getStackInSlot(10).getItem() instanceof ItemWandCasting)
		{
			ItemWandCasting wand = (ItemWandCasting) this.tileEntity.getStackInSlot(10).getItem();
			if (wand.consumeAllVisCrafting(this.tileEntity.getStackInSlot(10), this.ip.player, ThaumcraftCraftingManager.findMatchingArcaneRecipeAspects(this.tileEntity, this.ip.player), false))
				this.tileEntity.setInventorySlotContentsSoftly(9, ThaumcraftCraftingManager.findMatchingArcaneRecipe(this.tileEntity, this.ip.player));
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if (!this.tileEntity.getWorldObj().isRemote)
		{
			this.tileEntity.eventHandler = null;

			// TODO gamerforEA code start
			this.tileEntity.containers.remove(this.ip.player);
			// TODO gamerforEA code end
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
	{
		return this.tileEntity.getWorldObj().getTileEntity(this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord) != this.tileEntity ? false : par1EntityPlayer.getDistanceSq(this.tileEntity.xCoord + 0.5D, this.tileEntity.yCoord + 0.5D, this.tileEntity.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1)
	{
		ItemStack var2 = null;
		Slot var3 = (Slot) this.inventorySlots.get(par1);
		if (var3 != null && var3.getHasStack())
		{
			ItemStack var4 = var3.getStack();
			var2 = var4.copy();
			if (par1 == 0)
			{
				if (!this.mergeItemStack(var4, 11, 47, true))
					return null;

				var3.onSlotChange(var4, var2);
			}
			else if (par1 >= 11 && par1 < 38)
			{
				if (var4.getItem() instanceof ItemWandCasting && !((ItemWandCasting) var4.getItem()).isStaff(var4))
				{
					if (!this.mergeItemStack(var4, 1, 2, false))
						return null;

					var3.onSlotChange(var4, var2);
				}
				else if (!this.mergeItemStack(var4, 38, 47, false))
					return null;
			}
			else if (par1 >= 38 && par1 < 47)
			{
				if (var4.getItem() instanceof ItemWandCasting && !((ItemWandCasting) var4.getItem()).isStaff(var4))
				{
					if (!this.mergeItemStack(var4, 1, 2, false))
						return null;

					var3.onSlotChange(var4, var2);
				}
				else if (!this.mergeItemStack(var4, 11, 38, false))
					return null;
			}
			else if (!this.mergeItemStack(var4, 11, 47, false))
				return null;

			if (var4.stackSize == 0)
				var3.putStack((ItemStack) null);
			else
				var3.onSlotChanged();

			if (var4.stackSize == var2.stackSize)
				return null;

			var3.onPickupFromSlot(this.ip.player, var4);
		}

		return var2;
	}

	@Override
	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer par4EntityPlayer)
	{
		if (par3 == 4)
		{
			par2 = 1;
			return super.slotClick(par1, par2, par3, par4EntityPlayer);
		}
		else
		{
			if ((par1 == 0 || par1 == 1) && par2 > 0)
				par2 = 0;

			return super.slotClick(par1, par2, par3, par4EntityPlayer);
		}
	}

	@Override
	public boolean func_94530_a(ItemStack par1ItemStack, Slot par2Slot)
	{
		return par2Slot.inventory != this.tileEntity && super.func_94530_a(par1ItemStack, par2Slot);
	}
}
