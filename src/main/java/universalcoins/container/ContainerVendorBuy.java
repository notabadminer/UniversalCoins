package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tileentity.TileVendor;

public class ContainerVendorBuy extends Container {
	private TileVendor tileEntity;
	private int lastUserCoinSum, lastItemPrice;
	private boolean lastOoStock, lastOoCoins, lastInvFull, lastSellButtonActive, lastUCoinButtonActive,
			lastUSStackButtonActive, lastULStackButtonActive, lastUSBagButtonActive, lastULBagButtonActive, lastInUse;

	public ContainerVendorBuy(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		tileEntity = tEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotTradeItem(tileEntity, TileVendor.itemTradeSlot, 8, 24));
		addSlotToContainer(new Slot(tileEntity, TileVendor.itemSellSlot, 26, 24));
		addSlotToContainer(new UCSlotOutput(tileEntity, TileVendor.itemCoinOutputSlot, 152, 64));
		addSlotToContainer(new UCSlotCard(tileEntity, TileVendor.itemUserCardSlot, 43, 64));

		// commonly used vanilla code that adds the player's inventory
		bindPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileEntity.isUsableByPlayer(player);
	}

	void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 117 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 175));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			// merges the item into player inventory since its in the tileEntity
			if (slot < 4) {
				if (!this.mergeItemStack(stackInSlot, 4, 40, true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else {
				boolean foundSlot = false;
				for (int i = 0; i < 4; i++) {
					if (((Slot) inventorySlots.get(i)).isItemValid(stackInSlot)
							&& this.mergeItemStack(stackInSlot, i, i + 1, false)) {
						foundSlot = true;
						break;
					}
				}
				if (!foundSlot) {
					return null;
				}
			}

			if (stackInSlot.getCount() == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.getCount() == stack.getCount()) {
				return null;
			}
			slotObject.onTake(player, stackInSlot);
		}

		return stack;
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (this.lastItemPrice != tileEntity.itemPrice || this.lastUserCoinSum != tileEntity.userCoinSum
				|| this.lastOoStock != this.tileEntity.ooStockWarning
				|| this.lastOoCoins != this.tileEntity.ooCoinsWarning
				|| this.lastInvFull != this.tileEntity.inventoryFullWarning
				|| this.lastSellButtonActive != tileEntity.sellButtonActive
				|| this.lastUCoinButtonActive != this.tileEntity.uironCoinBtnActive
				|| this.lastUSStackButtonActive != this.tileEntity.ugoldCoinBtnActive
				|| this.lastULStackButtonActive != this.tileEntity.uemeraldCoinBtnActive
				|| this.lastUSBagButtonActive != this.tileEntity.udiamondCoinBtnActive
				|| this.lastULBagButtonActive != this.tileEntity.uobsidianCoinBtnActive
				|| this.lastInUse != this.tileEntity.inUse) {
			// update
			tileEntity.updateTE();

			this.lastUserCoinSum = tileEntity.userCoinSum;
			this.lastItemPrice = tileEntity.itemPrice;
			this.lastOoStock = this.tileEntity.ooStockWarning;
			this.lastOoCoins = this.tileEntity.ooCoinsWarning;
			this.lastInvFull = this.tileEntity.inventoryFullWarning;
			this.lastSellButtonActive = tileEntity.sellButtonActive;
			this.lastUCoinButtonActive = this.tileEntity.uironCoinBtnActive;
			this.lastUSStackButtonActive = this.tileEntity.ugoldCoinBtnActive;
			this.lastULStackButtonActive = this.tileEntity.uemeraldCoinBtnActive;
			this.lastUSBagButtonActive = this.tileEntity.udiamondCoinBtnActive;
			this.lastULBagButtonActive = this.tileEntity.uobsidianCoinBtnActive;
			this.lastInUse = this.tileEntity.inUse;
		}

	}

	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2) {
		if (par1 == 0) {
			// this.tileEntity.autoMode = par2;
		}
	}

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tileEntity.inUseCleanup();
	}
}
