package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalcoins.tileentity.TileATM;

public class ContainerATM extends Container {
	private String lastPlayerName;
	private String lastPlayerUID;
	private boolean lastInUse;
	private boolean lastDepositCoins;
	private boolean lastWithdrawCoins;
	private boolean lastAccountError;
	private int lastCoinWithdrawalAmount;
	private String lastCardOwner;
	private String lastAccountNumber;
	private long lastAccountBalance;
	private TileATM tEntity;

	public ContainerATM(InventoryPlayer inventoryPlayer, TileATM tileEntity) {
		tEntity = tileEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotCoinInput(tEntity, TileATM.itemCoinSlot, 172, 40));
		addSlotToContainer(new UCSlotCard(tEntity, TileATM.itemCardSlot, 172, 60));

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
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 18 + j * 18, 119 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 18 + i * 18, 177));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 2) {
				if (!this.mergeItemStack(itemstack1, 2, this.inventorySlots.size(), true)) {
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
			tEntity.fillCoinSlot();
		}

		return itemstack;
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (this.lastPlayerName != tEntity.playerName || this.lastPlayerUID != tEntity.playerUID
				|| this.lastInUse != tEntity.inUse || this.lastDepositCoins != tEntity.depositCoins
				|| this.lastWithdrawCoins != tEntity.withdrawCoins || this.lastAccountError != tEntity.accountError
				|| this.lastCoinWithdrawalAmount != tEntity.coinWithdrawalAmount
				|| this.lastCardOwner != tEntity.cardOwner || this.lastAccountNumber != tEntity.accountNumber
				|| this.lastAccountBalance != tEntity.accountBalance) {
			tEntity.updateTE();
		}

		this.lastPlayerName = tEntity.playerName;
		this.lastPlayerUID = tEntity.playerUID;
		this.lastInUse = tEntity.inUse;
		this.lastDepositCoins = tEntity.depositCoins;
		this.lastWithdrawCoins = tEntity.withdrawCoins;
		this.lastAccountError = tEntity.accountError;
		this.lastCoinWithdrawalAmount = tEntity.coinWithdrawalAmount;
		this.lastCardOwner = tEntity.cardOwner;
		this.lastAccountNumber = tEntity.accountNumber;
		this.lastAccountBalance = tEntity.accountBalance;

	}

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tEntity.inUseCleanup();
	}
}
