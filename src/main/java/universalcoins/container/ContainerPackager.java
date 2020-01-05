package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalcoins.tileentity.TilePackager;

public class ContainerPackager extends Container {

	private TilePackager tEntity;
	private long lastCoinSum;
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
		return tEntity.isUsableByPlayer(player);
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
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 11) {
				if (!this.mergeItemStack(itemstack1, 11, 47, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 4 - tEntity.packageSize * 2, 10, false)) {
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

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tEntity.inUseCleanup();
	}
}
