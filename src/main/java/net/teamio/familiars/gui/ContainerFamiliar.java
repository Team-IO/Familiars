package net.teamio.familiars.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.teamio.familiars.entities.EntityFamiliar;

public class ContainerFamiliar extends Container {

	private EntityFamiliar familiar;
	private IInventory inventory;
	
	public ContainerFamiliar(InventoryPlayer inventoryPlayer, EntityFamiliar familiar) {
		
		this.familiar = familiar;
		
		inventory = familiar.inventory;
		
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			addSlotToContainer(new Slot(inventory, i, 44 + i * 18, 20));
		}

		bindPlayerInventory(inventoryPlayer);
		
		//TODO: Register Slots for inventory of familiar & player
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
	
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, /*84*/51 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 109));
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		ItemStack stack = null;
		Slot slot = (Slot) inventorySlots.get(slotID);

		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();
			
			// merges the item into player inventory since its in the tileEntity
			if (slotID < inventory.getSizeInventory()) {
				if (!this.mergeItemStack(stackInSlot, inventory.getSizeInventory(), this.inventorySlots.size(), true)) {
					return null;
				}
			}
			// merge into tileEntity inventory, since it is in player's inventory
			else if (!this.mergeItemStack(stackInSlot, 0, inventory.getSizeInventory(), false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0)
            {
                slot.putStack(null);
            }
            else
            {
                slot.onSlotChanged();
            }
		}
		return stack;
	}

}
