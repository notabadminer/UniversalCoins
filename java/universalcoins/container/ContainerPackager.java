package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tileentity.TilePackager;

public class ContainerPackager extends Container {

	private TilePackager tEntity;
	private int lastCoinSum;
	private int lastPackageSize;
	private String lastPackageTarget = "";
	private boolean lastCardAvailable;
	private String lastCustomName;
	private boolean lastInUse = false;

	public ContainerPackager(InventoryPlayer inventoryPlayer, TilePackager tileEntity) {
		tEntity = tileEntity;

		// add package slots
		// we draw top to bottom instead of the typical left to right
		// this makes the slot hiding easier
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2; j++) {
				addSlotToContainer(new Slot(tEntity, tEntity.itemPackageSlot[i * 2 + j], 8 + i * 18, 22 + j * 18));
			}
		}

		addSlotToContainer(new UCSlotCard(tEntity, tEntity.itemCardSlot, 8, 73));
		addSlotToContainer(new UCSlotCoinInput(tEntity, tEntity.itemCoinSlot, 26, 73));
		addSlotToContainer(new UCSlotOutput(tEntity, tEntity.itemOutputSlot, 152, 73));
		addSlotToContainer(new UCSlotPackage(tEntity, tEntity.itemPackageInputSlot, Integer.MAX_VALUE, 26));

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
			if (slot < 11) {
				if (!this.mergeItemStack(stackInSlot, 11, 47, true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else {
				boolean foundSlot = false;
				for (int i = 4 - tEntity.packageSize * 2; i < 11; i++) {
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

			if (this.lastCoinSum != tEntity.coinSum || this.lastPackageSize != tEntity.packageSize
					|| this.lastCardAvailable != tEntity.cardAvailable || this.lastInUse != tEntity.inUse
					|| this.lastPackageTarget != tEntity.packageTarget) {
				tEntity.updateTE();
			}

			this.lastCoinSum = tEntity.coinSum;
			this.lastPackageSize = tEntity.packageSize;
			this.lastPackageTarget = tEntity.packageTarget;
			this.lastCardAvailable = tEntity.cardAvailable;
			this.lastInUse = tEntity.inUse;
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
