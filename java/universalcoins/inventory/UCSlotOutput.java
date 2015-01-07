package universalcoins.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class UCSlotOutput extends Slot {
	public UCSlotOutput(IInventory parInventory, int parSlotIndex, int parX, int parY) {
		super(parInventory, parSlotIndex, parX, parY);
	}

	public boolean isItemValid(ItemStack par1ItemStack){
		return false;
	}
	
	public ItemStack decrStackSize(int par1) {
		if (getStack() != null && getStack().stackSize != par1) {
			return new ItemStack(getStack().getItem(), -1);
		}
		return inventory.decrStackSize(getSlotIndex(), par1);
	}
}
