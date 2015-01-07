package universalcoins.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tile.TileVendor;

public class ContainerVendorBuy extends Container {
	private TileVendor tileEntity;
	private int lastUserCoinSum;
	private int lastItemPrice;
	private boolean lastOoStock;
	private boolean lastOoCoins;
	private boolean lastInvFull;	
	private boolean lastSellButtonActive;
	private boolean lastUCoinButtonActive;
	private boolean lastUSStackButtonActive;
	private boolean lastULStackButtonActive;
	private boolean lastUSBagButtonActive;
	private boolean lastULBagButtonActive;
	
	public ContainerVendorBuy(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		tileEntity = tEntity;
		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to and the x-y coordinates it resides on-screen
		addSlotToContainer(new UCSlotTradeItem(tileEntity, TileVendor.itemTradeSlot, 8, 24));
		addSlotToContainer(new Slot(tileEntity, TileVendor.itemSellSlot, 26, 24));
		addSlotToContainer(new UCSlotOutput(tileEntity, TileVendor.itemCoinOutputSlot, 152, 64));
		
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
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						8 + j * 18, 117 + i * 18));
			}
		}
		
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 175));
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
			if (slot < 4) {
				if (!this.mergeItemStack(stackInSlot, 4, 39, true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else {
				boolean foundSlot = false;
				for (int i = 0; i < 4; i++){
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
		
		for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if ( this.lastItemPrice != tileEntity.itemPrice
                    || this.lastUserCoinSum != tileEntity.userCoinSum 
            		|| this.lastOoStock != this.tileEntity.ooStockWarning
            		|| this.lastOoCoins != this.tileEntity.ooCoinsWarning
                    || this.lastInvFull != this.tileEntity.inventoryFullWarning
            		|| this.lastSellButtonActive != tileEntity.sellButtonActive
            		|| this.lastUCoinButtonActive != this.tileEntity.uCoinButtonActive
            		|| this.lastUSStackButtonActive != this.tileEntity.uSStackButtonActive
            		|| this.lastULStackButtonActive != this.tileEntity.uLStackButtonActive
            		|| this.lastUSBagButtonActive != this.tileEntity.uSBagButtonActive
            		|| this.lastULBagButtonActive != this.tileEntity.uLBagButtonActive) {
                //update
            	tileEntity.updateTE();
            	
            	this.lastUserCoinSum = tileEntity.userCoinSum;
        		this.lastItemPrice = tileEntity.itemPrice;
        		this.lastOoStock = this.tileEntity.ooStockWarning;
                this.lastOoCoins = this.tileEntity.ooCoinsWarning;
                this.lastInvFull = this.tileEntity.inventoryFullWarning;
        		this.lastSellButtonActive = tileEntity.sellButtonActive;
        		this.lastUCoinButtonActive = this.tileEntity.uCoinButtonActive;
                this.lastUSStackButtonActive = this.tileEntity.uSStackButtonActive;
                this.lastULStackButtonActive = this.tileEntity.uLStackButtonActive;
                this.lastUSBagButtonActive = this.tileEntity.uSBagButtonActive;
                this.lastULBagButtonActive = this.tileEntity.uLBagButtonActive;
            }
        }
	}
	
	@SideOnly(Side.CLIENT)
    public void updateProgressBar(int par1, int par2)
    {
        if (par1 == 0)
        {
            //this.tileEntity.autoMode = par2;
        }
    }

}
