package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class UCSlotGhost extends Slot {

	public UCSlotGhost(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		this.putStack(ItemStack.EMPTY);
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		// copy itemstack held
		if (stack != null) {
			this.putStack(ItemStack.EMPTY);
			this.putStack(stack.copy());
		} else {
			this.putStack(ItemStack.EMPTY);
		}
		// return false so user keeps itemstack
		return false;
	}
}