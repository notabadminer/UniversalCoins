package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;

public class UCSlotGhost extends Slot {

	public UCSlotGhost(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		FMLLog.info("canTakeStack");
		this.putStack(ItemStack.EMPTY);
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		FMLLog.info("isItemValid");
		// copy itemstack held
		this.putStack(stack.copy());
		// return false so user keeps itemstack
		return false;
	}

	@Override
	public void putStack(ItemStack stack) {
		FMLLog.info("putStack: " + stack.getUnlocalizedName());
		this.inventory.setInventorySlotContents(getSlotIndex(), stack);
		FMLLog.info("Current stack: " + this.inventory.getStackInSlot(getSlotIndex()));
		this.onSlotChanged();
	}
}