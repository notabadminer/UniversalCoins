package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tile.TileCardStation;

public class ContainerCardStation extends Container {
	private String lastPlayerName;
	private String lastPlayerUID;
	private boolean lastInUse;
	private boolean lastDepositCoins;
	private boolean lastWithdrawCoins;
	private boolean lastAccountError;
	private int lastCoinWithdrawalAmount;
	private String lastCardOwner;
	private String lastAccountNumber;
	private int lastAccountBalance;
	private String lastGroupAccountName;
	private String lastGroupAccountNumber;
	private TileCardStation tEntity;

	public ContainerCardStation(InventoryPlayer inventoryPlayer, TileCardStation tileEntity) {
		tEntity = tileEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotCard(tEntity, tEntity.itemCardSlot, 152, 60));
		addSlotToContainer(new UCSlotCoinInput(tEntity, tEntity.itemCoinSlot, 152, 40));

		// commonly used vanilla code that adds the player's inventory
		bindPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tEntity.isUseableByPlayer(player);
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
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			// merges the item into player inventory since its in the tileEntity
			if (slot < 2) {
				if (!this.mergeItemStack(stackInSlot, 2, 38, true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else {
				boolean foundSlot = false;
				for (int i = 0; i < 2; i++) {
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
			tEntity.fillCoinSlot();
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

			if (this.lastPlayerName != tEntity.playerName || this.lastPlayerUID != tEntity.playerUID
					|| this.lastInUse != tEntity.inUse || this.lastDepositCoins != tEntity.depositCoins
					|| this.lastWithdrawCoins != tEntity.withdrawCoins || this.lastAccountError != tEntity.accountError
					|| this.lastCoinWithdrawalAmount != tEntity.coinWithdrawalAmount
					|| this.lastCardOwner != tEntity.cardOwner || this.lastAccountNumber != tEntity.accountNumber
					|| this.lastAccountBalance != tEntity.accountBalance
					|| this.lastGroupAccountName != tEntity.customAccountName
					|| this.lastGroupAccountNumber != tEntity.customAccountNumber) {
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
			this.lastGroupAccountName = tEntity.customAccountName;
			this.lastGroupAccountNumber = tEntity.customAccountNumber;
		}
	}

	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2) {
		if (par1 == 0) {
			// this.tileEntity.autoMode = par2;
		}
	}

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tEntity.inUseCleanup();
	}
}
