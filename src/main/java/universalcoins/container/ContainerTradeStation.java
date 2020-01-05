package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tileentity.TileTradeStation;

public class ContainerTradeStation extends Container {
	private TileTradeStation tileEntity;
	private long lastCoinSum;
	private int lastItemPrice, lastAutoMode, lastCoinMode;
	private boolean lastBuyButtonActive, lastPublicAccess;

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
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 3) {
				if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
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
		}
		return itemstack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int data) {
		this.tileEntity.setField(id, data);
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.listeners.size(); ++i) {
			IContainerListener icontainerlistener = this.listeners.get(i);

			if (this.lastCoinSum != this.tileEntity.getField(0)) {
				tileEntity.updateTE();
			}

			if (this.lastItemPrice != this.tileEntity.getField(1)) {
				tileEntity.updateTE();
			}

			if (this.lastAutoMode != this.tileEntity.getField(2)) {
				icontainerlistener.sendWindowProperty(this, 2, this.tileEntity.getField(2));
			}
			if (this.lastCoinMode != this.tileEntity.getField(3)) {
				icontainerlistener.sendWindowProperty(this, 3, this.tileEntity.getField(3));
			}

			if (this.lastPublicAccess != (this.tileEntity.getField(4) == 0 ? false : true)) {
				icontainerlistener.sendWindowProperty(this, 4, this.tileEntity.getField(4));
			}
			if (this.lastBuyButtonActive != (this.tileEntity.getField(5) == 0 ? false : true)) {
				icontainerlistener.sendWindowProperty(this, 5, this.tileEntity.getField(5));
			}
		}

		this.lastCoinSum = this.tileEntity.getField(0);
		this.lastItemPrice = this.tileEntity.getField(1);
		this.lastAutoMode = this.tileEntity.getField(2);
		this.lastCoinMode = this.tileEntity.getField(3);
		this.lastPublicAccess = this.tileEntity.getField(4) == 0;
		this.lastBuyButtonActive = this.tileEntity.getField(5) == 0;
	}

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tileEntity.inUseCleanup();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileEntity.isUsableByPlayer(player);
	}
}
