package net.teamio.familiars.gui;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SavableInventory extends InventoryBasic {

	public SavableInventory(String title, boolean customName, int slotCount) {
		super(title, customName, slotCount);
	}

	/**
	 * Reads from the given tag list and fills the slots in the inventory with
	 * the correct items.
	 */
	public void readFromNBT(NBTTagList tagList) {
		int invSize = getSizeInventory();

		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;
			ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

			if (j >= 0 && j < invSize) {
				if (itemstack == null) {
					this.setInventorySlotContents(j, null);
				} else {
					this.setInventorySlotContents(j, itemstack);
				}
			}
		}
	}

	/**
	 * Writes the inventory out as a list of compound tags. This is where the
	 * slot indices are used (+100 for armor, +80 for crafting).
	 */
	public NBTTagList writeToNBT(NBTTagList tagList) {
		int i;
		NBTTagCompound nbttagcompound;

		int invSize = getSizeInventory();

		for (i = 0; i < invSize; ++i) {
			ItemStack stack = this.getStackInSlot(i);
			if (stack != null) {
				nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				stack.writeToNBT(nbttagcompound);
				tagList.appendTag(nbttagcompound);
			}
		}

		return tagList;
	}
}
