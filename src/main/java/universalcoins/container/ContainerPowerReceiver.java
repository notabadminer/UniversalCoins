package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalcoins.tileentity.TilePowerReceiver;

public class ContainerPowerReceiver extends Container {
	private String lastOwner;
	private long lastCoinSum, lastwrfLevel;
	private int lastrfLevel, lastrfOutput;
	private boolean lastPublicAccess;
	private TilePowerReceiver tEntity;

	public ContainerPowerReceiver(InventoryPlayer inventoryPlayer, TilePowerReceiver tileEntity) {
		tEntity = tileEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotCard(tileEntity, tEntity.itemCardSlot, 14, 70));
		addSlotToContainer(new UCSlotCoinInput(tileEntity, tEntity.itemCoinSlot, 32, 70));
		addSlotToContainer(new UCSlotOutput(tileEntity, tEntity.itemOutputSlot, 148, 70));

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
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 103 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 161));
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 3) {
				if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, 2, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (this.lastOwner != tEntity.blockOwner || this.lastCoinSum != tEntity.coinSum
				|| this.lastrfLevel != tEntity.feLevel || this.lastrfOutput != tEntity.feOutput
				|| this.lastwrfLevel != tEntity.wfeLevel || this.lastPublicAccess != this.tEntity.publicAccess) {
			tEntity.updateTE();

			this.lastOwner = tEntity.blockOwner;
			this.lastCoinSum = tEntity.coinSum;
			this.lastrfLevel = tEntity.feLevel;
			this.lastrfOutput = tEntity.feOutput;
			this.lastwrfLevel = tEntity.wfeLevel;
			this.lastPublicAccess = this.tEntity.publicAccess;
		}
	}
}
