package universalcoins.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalcoins.tile.TileVendor;

public class ContainerVendorSell extends Container {
	private TileVendor tileEntity;
	private int lastUserCoinSum, lastItemPrice;
	private boolean lastOoStock, lastOoCoins, lastInvFull, lastInUse, lastUserIronCoinButtonActive,
			lastUserGoldCoinButtonActive, lastUserEmeraldCoinButtonActive, lastUserDiamondCoinButtonActive,
			lastUserObsidianCoinButtonActive;

	public ContainerVendorSell(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		tileEntity = tEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotTradeItem(tileEntity, TileVendor.itemTradeSlot, 35, 24));
		addSlotToContainer(new UCSlotOutput(tileEntity, TileVendor.itemOutputSlot, 152, 24));
		addSlotToContainer(new UCSlotCard(tileEntity, TileVendor.itemUserCardSlot, 17, 57));
		addSlotToContainer(new UCSlotCoinInput(tileEntity, TileVendor.itemUserCoinInputSlot, 35, 57));
		addSlotToContainer(new UCSlotOutput(tileEntity, TileVendor.itemCoinOutputSlot, 152, 57));

		// commonly used vanilla code that adds the player's inventory
		bindPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileEntity.isUseableByPlayer(player);
	}

	void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 108 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 166));
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
			if (slot < 5) {
				if (!this.mergeItemStack(stackInSlot, 5, 41, true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else {
				boolean foundSlot = false;
				for (int i = 0; i < 5; i++) {
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

			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
		}

		return stack;
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i) {
			ICrafting icrafting = (ICrafting) this.crafters.get(i);

			if (this.lastUserCoinSum != this.tileEntity.userCoinSum || this.lastItemPrice != this.tileEntity.itemPrice
					|| this.lastOoStock != this.tileEntity.ooStockWarning
					|| this.lastOoCoins != this.tileEntity.ooCoinsWarning
					|| this.lastInvFull != this.tileEntity.inventoryFullWarning
					|| this.lastInUse != this.tileEntity.inUse
					|| this.lastUserIronCoinButtonActive != this.tileEntity.uIronCoinBtnActive
					|| this.lastUserGoldCoinButtonActive != this.tileEntity.uGoldCoinBtnActive
					|| this.lastUserEmeraldCoinButtonActive != this.tileEntity.uEmeraldCoinBtnActive
					|| this.lastUserDiamondCoinButtonActive != this.tileEntity.uDiamondCoinBtnActive
					|| this.lastUserObsidianCoinButtonActive != this.tileEntity.uObsidianCoinBtnActive) {
				// update
				tileEntity.updateTE();

				this.lastUserCoinSum = this.tileEntity.userCoinSum;
				this.lastItemPrice = this.tileEntity.itemPrice;
				this.lastOoStock = this.tileEntity.ooStockWarning;
				this.lastOoCoins = this.tileEntity.ooCoinsWarning;
				this.lastInvFull = this.tileEntity.inventoryFullWarning;
				this.lastInUse = this.tileEntity.inUse;
				this.lastUserIronCoinButtonActive = this.tileEntity.uIronCoinBtnActive;
				this.lastUserGoldCoinButtonActive = this.tileEntity.uGoldCoinBtnActive;
				this.lastUserEmeraldCoinButtonActive = this.tileEntity.uEmeraldCoinBtnActive;
				this.lastUserDiamondCoinButtonActive = this.tileEntity.uDiamondCoinBtnActive;
				this.lastUserObsidianCoinButtonActive = this.tileEntity.uObsidianCoinBtnActive;
			}
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
