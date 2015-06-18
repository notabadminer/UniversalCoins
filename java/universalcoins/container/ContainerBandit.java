package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tile.TileBandit;

public class ContainerBandit extends Container {

	private TileBandit tEntity;
	private int lastCoinSum, lastSpinFee, lastFourMatchPayout, lastFiveMatchPayout;
	private boolean lastCardAvailable;
	private String lastCustomName;
	private boolean lastInUse = false;
	private int[] lastReelPos = { 0, 0, 0, 0 };

	public ContainerBandit(InventoryPlayer inventoryPlayer, TileBandit tileEntity) {
		tEntity = tileEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotCard(tEntity, tEntity.itemCardSlot, 13, 73));
		addSlotToContainer(new UCSlotCoinInput(tEntity, tEntity.itemCoinSlot, 31, 73));
		addSlotToContainer(new UCSlotOutput(tEntity, tEntity.itemOutputSlot, 148, 73));

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
			if (slot < 3) {
				if (!this.mergeItemStack(stackInSlot, 3, 39, true)) {
					return null;
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

			if (this.lastCoinSum != tEntity.coinSum || this.lastCardAvailable != tEntity.cardAvailable
					|| this.lastInUse != tEntity.inUse || this.lastSpinFee != tEntity.spinFee
					|| this.lastFourMatchPayout != tEntity.fourMatchPayout
					|| this.lastFiveMatchPayout != tEntity.fiveMatchPayout || this.lastReelPos[0] != tEntity.reelPos[0]
					|| this.lastReelPos[1] != tEntity.reelPos[1] || this.lastReelPos[2] != tEntity.reelPos[2]
					|| this.lastReelPos[3] != tEntity.reelPos[3]) {
				tEntity.updateTE();
			}

			this.lastCoinSum = tEntity.coinSum;
			this.lastCardAvailable = tEntity.cardAvailable;
			this.lastInUse = tEntity.inUse;
			this.lastSpinFee = tEntity.spinFee;
			this.lastFourMatchPayout = tEntity.fourMatchPayout;
			this.lastFiveMatchPayout = tEntity.fiveMatchPayout;
			this.lastReelPos[0] = tEntity.reelPos[0];
			this.lastReelPos[1] = tEntity.reelPos[1];
			this.lastReelPos[2] = tEntity.reelPos[2];
			this.lastReelPos[3] = tEntity.reelPos[3];
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
