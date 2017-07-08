package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
		addSlotToContainer(new UCSlotCoinInput(tEntity, tEntity.itemCoinSlot, 172, 40));
		addSlotToContainer(new UCSlotCard(tEntity, tEntity.itemCardSlot, 172, 60));

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
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			// merges the item into player inventory since its in the tileEntity
			if (slot < 2) {
				if (!this.mergeItemStack(stackInSlot, 2, 38, true)) {
					return ItemStack.EMPTY;
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

		return stack;
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
