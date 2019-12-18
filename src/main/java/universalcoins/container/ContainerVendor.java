package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tileentity.TileVendor;

public class ContainerVendor extends Container {
	private TileVendor tileEntity;
	private boolean lastOoStock, lastOoCoins, lastInvFull, lastSellMode, lastInUse, lastIronCoinBtnActive,
			lastGoldCoinBtnActive, lastEmeraldCoinBtnActive, lastDiamondCoinBtnActive, lastObsidianCoinBtnActive;
	private int lastCoinSum, lastUserCoinSum, lastItemPrice, lastTextColor;

	public ContainerVendor(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		tileEntity = tEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotGhost(tileEntity, TileVendor.itemTradeSlot, 9, 17));
		addSlotToContainer(new UCSlotCoinInput(tileEntity, TileVendor.itemCoinInputSlot, 35, 55));
		addSlotToContainer(new UCSlotCard(tileEntity, TileVendor.itemCardSlot, 17, 55));
		addSlotToContainer(new UCSlotOutput(tileEntity, TileVendor.itemCoinOutputSlot, 152, 55));

		// add all the inventory storage slots
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(tileEntity, TileVendor.itemStorageSlot1 + i, 8 + i * 18, 96));
		}

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
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 119 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 177));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 13) {
				if (!this.mergeItemStack(itemstack1, 13, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 1, 13, false)) {
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

		if (this.lastOoStock != this.tileEntity.ooStockWarning || this.lastOoCoins != this.tileEntity.ooCoinsWarning
				|| this.lastInvFull != this.tileEntity.inventoryFullWarning
				|| this.lastCoinSum != this.tileEntity.coinSum || this.lastUserCoinSum != this.tileEntity.userCoinSum
				|| this.lastItemPrice != this.tileEntity.itemPrice || this.lastSellMode != this.tileEntity.sellMode
				|| this.lastTextColor != this.tileEntity.textColor
				|| this.lastIronCoinBtnActive != this.tileEntity.ironCoinBtnActive
				|| this.lastGoldCoinBtnActive != this.tileEntity.goldCoinBtnActive
				|| this.lastEmeraldCoinBtnActive != this.tileEntity.emeraldCoinBtnActive
				|| this.lastDiamondCoinBtnActive != this.tileEntity.diamondCoinBtnActive
				|| this.lastObsidianCoinBtnActive != this.tileEntity.obsidianCoinBtnActive
				|| this.lastInUse != this.tileEntity.inUse) {
			// update
			tileEntity.updateTE();

			this.lastOoStock = this.tileEntity.ooStockWarning;
			this.lastOoCoins = this.tileEntity.ooCoinsWarning;
			this.lastInvFull = this.tileEntity.inventoryFullWarning;
			this.lastCoinSum = this.tileEntity.coinSum;
			this.lastUserCoinSum = this.tileEntity.userCoinSum;
			this.lastItemPrice = this.tileEntity.itemPrice;
			this.lastSellMode = this.tileEntity.sellMode;
			this.lastTextColor = this.tileEntity.textColor;
			this.lastIronCoinBtnActive = this.tileEntity.ironCoinBtnActive;
			this.lastGoldCoinBtnActive = this.tileEntity.goldCoinBtnActive;
			this.lastEmeraldCoinBtnActive = this.tileEntity.emeraldCoinBtnActive;
			this.lastDiamondCoinBtnActive = this.tileEntity.diamondCoinBtnActive;
			this.lastObsidianCoinBtnActive = this.tileEntity.obsidianCoinBtnActive;
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
