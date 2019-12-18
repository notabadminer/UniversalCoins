package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalcoins.tileentity.TileSafe;

public class ContainerSafe extends Container {
	private String lastOwner;
	private long lastAccountBalance;
	private TileSafe tEntity;

	public ContainerSafe(InventoryPlayer inventoryPlayer, TileSafe tileEntity) {
		tEntity = tileEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotCoinInput(tileEntity, tEntity.itemInputSlot, 27, 37));
		addSlotToContainer(new UCSlotOutput(tileEntity, tEntity.itemOutputSlot, 134, 37));

		// now that the slots are updated, fill the slots
		tEntity.fillOutputSlot();

		// commonly used vanilla code that adds the player's inventory
		bindPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tEntity.isUsableByPlayer(player);
	}

	void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 70 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 128));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			ItemStack stackFromSlot = stackInSlot.copy();

			if (index < 2) {
				if (!this.mergeItemStack(stackInSlot, 2, 38, true)) {
					return ItemStack.EMPTY;
				} else {
					// Let TE know coins were taken
					tEntity.coinsTaken(stackFromSlot);
				}
			} else if (!this.mergeItemStack(stackInSlot, 0, 1, false)) {
				return ItemStack.EMPTY;
			}

			if (stackInSlot.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (this.lastOwner != tEntity.blockOwner || this.lastAccountBalance != tEntity.accountBalance) {
			tEntity.updateTE();
		}

		this.lastOwner = tEntity.blockOwner;
		this.lastAccountBalance = tEntity.accountBalance;

	}
}
