package universalcoins.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalcoins.UniversalCoins;

public class UCSlotCard extends Slot {
	public UCSlotCard(IInventory parInventory, int parSlotIndex, int parX, int parY) {
		super(parInventory, parSlotIndex, parX, parY);
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {
		if (par1ItemStack == null) {
			return true;
		}
		Item itemInStack = par1ItemStack.getItem();
		return (itemInStack == UniversalCoins.Items.uc_card || itemInStack == UniversalCoins.Items.ender_card);
	}

	public ItemStack decrStackSize(int par1) {
		if (getStack() != null && getStack().getCount() != par1) {
			return new ItemStack(getStack().getItem(), -1);
		}
		return inventory.decrStackSize(getSlotIndex(), par1);
	}
}
