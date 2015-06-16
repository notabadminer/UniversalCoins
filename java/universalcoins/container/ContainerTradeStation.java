package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tile.TileTradeStation;


public class ContainerTradeStation extends Container {
	private TileTradeStation tileEntity;
	private int lastCoinSum, lastItemPrice, lastAutoMode, lastCoinMode;
	private String lastName;
	private boolean lastBuyButtonActive, lastSellButtonActive, lastCoinButtonActive, 
		lastSStackButtonActive, lastLStackButtonActive, lastSBagButtonActive, lastLBagButtonActive, 
		lastInUse;
	public ContainerTradeStation(InventoryPlayer inventoryPlayer, TileTradeStation tEntity) {
		tileEntity = tEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to
		// and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotCard(tileEntity, TileTradeStation.itemCardSlot, 12, 27));
		addSlotToContainer(new Slot(tileEntity, TileTradeStation.itemInputSlot, 31, 27));
		addSlotToContainer(new UCSlotOutput(tileEntity, TileTradeStation.itemOutputSlot, 155, 27));
		
		// commonly used vanilla code that adds the player's inventory
		bindPlayerInventory(inventoryPlayer);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return tileEntity.isUseableByPlayer(entityplayer);
	}
	
	void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						12 + j * 18, 119 + i * 18));
			}
		}
		
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 12 + i * 18, 177));
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
				for (int i = 0; i < 3; i++){
					if (((Slot)inventorySlots.get(i)).isItemValid(stackInSlot) && this.mergeItemStack(stackInSlot, i, i + 1, false)) {
						foundSlot = true;
						break;
					}
				}
				if (!foundSlot){
					return null;
				}
			}
			
			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			}
			else {
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
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.lastAutoMode != this.tileEntity.autoMode) {
                icrafting.sendProgressBarUpdate(this, 0, (int) this.tileEntity.autoMode);
            }
            if (this.lastCoinMode != this.tileEntity.coinMode) {
                icrafting.sendProgressBarUpdate(this, 1, (int) this.tileEntity.coinMode);
            }
            if (this.lastCoinSum != this.tileEntity.coinSum
				|| this.lastItemPrice != this.tileEntity.itemPrice 
				|| this.lastName != this.tileEntity.customName
				|| this.lastBuyButtonActive != this.tileEntity.buyButtonActive
				|| this.lastSellButtonActive != this.tileEntity.sellButtonActive
				|| this.lastCoinButtonActive != this.tileEntity.coinButtonActive
				|| this.lastSStackButtonActive != this.tileEntity.isSStackButtonActive
				|| this.lastLStackButtonActive != this.tileEntity.isLStackButtonActive
				|| this.lastSBagButtonActive != this.tileEntity.isSBagButtonActive
				|| this.lastInUse != this.tileEntity.inUse) {
            	tileEntity.updateTE();
            }

		this.lastAutoMode = this.tileEntity.autoMode;
		this.lastCoinMode = this.tileEntity.coinMode;
		this.lastCoinSum = this.tileEntity.coinSum;
		this.lastItemPrice = this.tileEntity.itemPrice;
		this.lastName = this.tileEntity.customName;
		this.lastBuyButtonActive = this.tileEntity.buyButtonActive;
		this.lastSellButtonActive = this.tileEntity.sellButtonActive;
		this.lastCoinButtonActive = this.tileEntity.coinButtonActive;
		this.lastSStackButtonActive = this.tileEntity.isSStackButtonActive;
		this.lastLStackButtonActive = this.tileEntity.isSStackButtonActive;
		this.lastSBagButtonActive = this.tileEntity.isSBagButtonActive;
		this.lastLBagButtonActive = this.tileEntity.isLBagButtonActive;
		this.lastInUse = this.tileEntity.inUse;
        }
	}
	
	@SideOnly(Side.CLIENT)
    public void updateProgressBar(int par1, int par2) {
        if (par1 == 0)
        {
            this.tileEntity.autoMode = par2;
        }
        if (par1 == 1)
        {
            this.tileEntity.coinMode = par2;
        }
    }
	
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tileEntity.inUseCleanup();
	}
}
