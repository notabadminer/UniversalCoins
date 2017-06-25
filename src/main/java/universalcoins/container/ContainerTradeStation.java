package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalcoins.tileentity.TileTradeStation;

public class ContainerTradeStation extends Container {
	private TileTradeStation tileEntity;
	private long lastCoinSum;
	private int lastItemPrice, lastAutoMode, lastCoinMode;
	private boolean lastBuyButtonActive, lastSellButtonActive, lastIronBtnActive, lastGoldBtnActive,
			lastEmeraldBtnActive, lastDiamondBtnActive, lastObsidianBtnActive, lastInUse, lastPublicAccess;

	public ContainerTradeStation(InventoryPlayer inventoryPlayer, TileTradeStation tEntity) {
		tileEntity = tEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotCard(tileEntity, TileTradeStation.itemCardSlot, 12, 27));
		addSlotToContainer(new Slot(tileEntity, TileTradeStation.itemInputSlot, 31, 27));
		addSlotToContainer(new UCSlotOutput(tileEntity, TileTradeStation.itemOutputSlot, 155, 27));

		// commonly used vanilla code that adds the player's inventory
		bindPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return tileEntity.isUsableByPlayer(entityplayer);
	}

	void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 12 + j * 18, 119 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 12 + i * 18, 177));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			// merges the item into player inventory since its in the tileEntity
			if (slot < 3) {
				if (!this.mergeItemStack(stackInSlot, 3, 39, true)) {
					return ItemStack.EMPTY;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else {
				boolean foundSlot = false;
				for (int i = 0; i < 3; i++) {
					if (((Slot) inventorySlots.get(i)).isItemValid(stackInSlot)
							&& this.mergeItemStack(stackInSlot, i, i + 1, false)) {
						foundSlot = true;
						break;
					}
				}
				if (!foundSlot) {
					return ItemStack.EMPTY;
				}
			}

			if (stackInSlot.getCount() == 0) {
				slotObject.putStack(ItemStack.EMPTY);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}
			slotObject.onTake(player, stackInSlot);
		}
		tileEntity.update();
		return stack;
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (this.lastCoinSum != this.tileEntity.coinSum || this.lastItemPrice != this.tileEntity.itemPrice
				|| this.lastBuyButtonActive != this.tileEntity.buyButtonActive
				|| this.lastSellButtonActive != this.tileEntity.sellButtonActive
				|| this.lastIronBtnActive != this.tileEntity.ironCoinBtnActive
				|| this.lastGoldBtnActive != this.tileEntity.goldCoinBtnActive
				|| this.lastEmeraldBtnActive != this.tileEntity.emeraldCoinBtnActive
				|| this.lastDiamondBtnActive != this.tileEntity.diamondCoinBtnActive
				|| this.lastInUse != this.tileEntity.inUse || this.lastAutoMode != this.tileEntity.autoMode
				|| this.lastCoinMode != this.tileEntity.coinMode
				|| this.lastPublicAccess != this.tileEntity.publicAccess) {
			tileEntity.updateTE();

			this.lastCoinSum = this.tileEntity.coinSum;
			this.lastItemPrice = this.tileEntity.itemPrice;
			this.lastBuyButtonActive = this.tileEntity.buyButtonActive;
			this.lastSellButtonActive = this.tileEntity.sellButtonActive;
			this.lastIronBtnActive = this.tileEntity.ironCoinBtnActive;
			this.lastGoldBtnActive = this.tileEntity.goldCoinBtnActive;
			this.lastEmeraldBtnActive = this.tileEntity.emeraldCoinBtnActive;
			this.lastDiamondBtnActive = this.tileEntity.diamondCoinBtnActive;
			this.lastObsidianBtnActive = this.tileEntity.obsidianCoinBtnActive;
			this.lastInUse = this.tileEntity.inUse;
			this.lastAutoMode = this.tileEntity.autoMode;
			this.lastCoinMode = this.tileEntity.coinMode;
			this.lastPublicAccess = this.tileEntity.publicAccess;
		}
	}

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tileEntity.inUseCleanup();
	}
}
