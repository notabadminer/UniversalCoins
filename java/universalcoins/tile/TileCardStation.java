package universalcoins.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.inventory.ContainerCardStation;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCCardStationServerCustomNameMessage;
import universalcoins.net.UCCardStationServerWithdrawalMessage;
import universalcoins.net.UCTileCardStationMessage;
import universalcoins.util.UCWorldData;

public class TileCardStation extends TileEntity implements IInventory, ISidedInventory {
	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemCoinSlot = 0;
	public static final int itemCardSlot = 1;
	private static final int[] multiplier = new int[] {1, 9, 81, 729, 6561};
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack, 
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
	public String playerName = "";
	public String playerUID = "";	
	public boolean inUse = false;
	public boolean depositCoins = false;
	public boolean withdrawCoins = false;
	public boolean accountError = false;
	public int coinWithdrawalAmount = 0;
	public String cardOwner = "";
	public String accountNumber = "none";
	public int accountBalance = 0;
	public String customAccountName = "none";
	public String customAccountNumber = "none";
	
	/*@Override
	public void updateEntity() {
		super.updateEntity();
		if (withdrawCoins) {
			withdrawCoins();
		}
		updateInUse();
	}*/
	
	private void updateInUse() {
		if (worldObj.isRemote) return;
		EntityPlayer playerTest = this.worldObj.getPlayerEntityByName(playerName);
		if (playerTest != null && playerTest.openContainer != null &&
				this.worldObj.getPlayerEntityByName(playerName).openContainer instanceof ContainerCardStation) {
			inUse = true;
		} else {
			inUse = false;
		}
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
		}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot >= inventory.length) {
			return null;
		}
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
				ItemStack newStack;
				if (inventory[slot] == null) {
					return null;
				}
				if (inventory[slot].stackSize <= count) {
					newStack = inventory[slot];
					inventory[slot] = null;

					return newStack;
				}
				newStack = ItemStack.copyItemStack(inventory[slot]);
				newStack.stackSize = count;
				inventory[slot].stackSize -= count;
				return newStack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		inUse = false;
		if (this.inventory[slot] != null) {
            ItemStack itemstack = this.inventory[slot];
            this.inventory[slot] = null;
            return itemstack;
        }
        else {
            return null;
        }
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			if (slot == itemCoinSlot && depositCoins) {
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - accountBalance) / itemValue);
					if (!worldObj.isRemote) {
						creditAccount(accountNumber, depositAmount * itemValue);
						accountBalance = getAccountBalance(accountNumber);
					}
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
				}
			}
			if (slot == itemCardSlot && !worldObj.isRemote) {
				if (inventory[itemCardSlot].getTagCompound().getInteger("CoinSum") != 0 && 
						inventory[itemCardSlot].getTagCompound().getString("Owner").contentEquals(playerName)) {
					addPlayerAccount(playerUID);
					accountNumber = getPlayerAccount(playerUID);
					creditAccount(accountNumber, inventory[itemCardSlot].getTagCompound().getInteger("CoinSum"));
					inventory[itemCardSlot].getTagCompound().removeTag("CoinSum");
					inventory[itemCardSlot].getTagCompound().setString("Account", accountNumber);
				}
				accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
				cardOwner = inventory[itemCardSlot].getTagCompound().getString("Owner");
				if (getCustomAccount(playerUID) != "") customAccountName = getCustomAccount(playerUID);
				accountBalance = getAccountBalance(accountNumber);
				}
		}
	}

	@Override
	public String getName() {
		return UniversalCoins.proxy.blockCardStation.getLocalizedName();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public void sendButtonMessage(int functionID, boolean shiftPressed) {
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, functionID, shiftPressed));
	}
	
	@Override
    public Packet getDescriptionPacket() {
        return UniversalCoins.snw.getPacketFrom(new UCTileCardStationMessage(this));
    }
	
	public void sendServerUpdatePacket(int withdrawalAmount) {
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		UniversalCoins.snw.sendToServer(new UCCardStationServerWithdrawalMessage(xCoord, yCoord, zCoord, withdrawalAmount));
	}
	
	public void sendServerUpdatePacket(String customName) {
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		UniversalCoins.snw.sendToServer(new UCCardStationServerCustomNameMessage(xCoord, yCoord, zCoord, customName));
	}
	
	public void updateTE() {
		 worldObj.markBlockForUpdate(pos);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory",
				Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
		try {
			inUse = tagCompound.getBoolean("InUse");
		} catch (Throwable ex2) {
			inUse = false;
		}
		try {
			depositCoins = tagCompound.getBoolean("DepositCoins");
		} catch (Throwable ex2) {
			depositCoins = false;
		}
		try {
			withdrawCoins = tagCompound.getBoolean("WithdrawCoins");
		} catch (Throwable ex2) {
			withdrawCoins = false;
		}
		try {
			coinWithdrawalAmount = tagCompound.getInteger("CoinWithdrawalAmount");
		} catch (Throwable ex2) {
			coinWithdrawalAmount = 0;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = inventory[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Inventory", itemList);
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setBoolean("DepositCoins", depositCoins);
		tagCompound.setBoolean("WithdrawCoins", withdrawCoins);
		tagCompound.setInteger("CoinWithdrawalAmount", coinWithdrawalAmount);
	}
	

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		return worldObj.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5,
						zCoord + 0.5) < 64;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}
	
	private int getCoinType(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public void onButtonPressed(int functionId) {
		if (worldObj.isRemote) return;
		accountError = false; //reset error state
		//handle function IDs sent from CardStationGUI
		//function1 - new card
		//function2 - transfer account
		//function3 - deposit
		//function4 - withdraw
		//function5 - get account info
		//function6 - destroy invalid card
		//function7 - new custom account
		//function8 - new custom card
		//function9 - transfer custom account
		//function10 - account error reset
		if (functionId == 1) {
			if (getPlayerAccount(playerUID) == "") {
				addPlayerAccount(playerUID);
			}
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].getTagCompound().setString("Name", playerName);
			inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
			inventory[itemCardSlot].getTagCompound().setString("Account", accountNumber);
			accountBalance = getAccountBalance(accountNumber);
		}
		if (functionId == 2) {
			if (getPlayerAccount(playerUID) == "") {
			} else {
				transferPlayerAccount(playerUID);
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
				inventory[itemCardSlot].getTagCompound().setString("Name", playerName);
				inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
				inventory[itemCardSlot].getTagCompound().setString("Account", getPlayerAccount(playerUID));
				accountBalance = getAccountBalance(accountNumber);
			}
		}
		if (functionId == 3) {
			//set to true if player presses deposit button, reset on any other button press
			depositCoins = true;
		} else depositCoins = false;
		if (functionId == 4) {
			withdrawCoins = true;
		}
		if (functionId == 5) {
			String storedAccount = getPlayerAccount(playerUID);
			if (storedAccount != "") { 
				accountNumber = storedAccount;
				cardOwner = playerUID; //needed for new card auth
				accountBalance = getAccountBalance(accountNumber);
				if (getCustomAccount(playerUID) != ""){
					customAccountName = getCustomAccount(playerUID);
					customAccountNumber = getPlayerAccount(customAccountName);
				}
			} else accountNumber = "none";
		}
		if (functionId == 6) {
			inventory[itemCardSlot] = null;
		}
		if (functionId == 7) {
			if (getPlayerAccount(customAccountName) != "" && !getCustomAccount(playerUID).contentEquals(customAccountName)) {
				accountError = true;
				//we need to reset this so that that function 7 is called again on next attempt at getting an account
				customAccountName = "none";
				return;
			} else if (getCustomAccount(playerUID) == "") {
				addCustomAccount(customAccountName);
			}
			customAccountName = getCustomAccount(playerUID);
			customAccountNumber = getPlayerAccount(customAccountName);
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].getTagCompound().setString("Name", customAccountName);
			inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
			inventory[itemCardSlot].getTagCompound().setString("Account", customAccountNumber);			
		}
		if (functionId == 8) {
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].getTagCompound().setString("Name", customAccountName);
			inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
			inventory[itemCardSlot].getTagCompound().setString("Account", customAccountNumber);
			accountBalance = getAccountBalance(customAccountNumber);
		}
		if (functionId == 9) {
			if (getCustomAccount(playerUID) == "" || getPlayerAccount(customAccountName) != "") {
				accountError = true;
			} else {
				accountError = false;
				transferCustomAccount();
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
				inventory[itemCardSlot].getTagCompound().setString("Name", customAccountName);
				inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
				inventory[itemCardSlot].getTagCompound().setString("Account", customAccountNumber);
				accountBalance = getAccountBalance(customAccountNumber);
			}
		}
	}

	private void withdrawCoins() {
		if (inventory[itemCoinSlot] == null && coinWithdrawalAmount > 0) {
			// use logarithm to find largest cointype for coins being withdrawn
			int logVal = Math.min((int) (Math.log(coinWithdrawalAmount) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinWithdrawalAmount / Math.pow(9, logVal)), 64);
			inventory[itemCoinSlot] = (new ItemStack(coins[logVal], stackSize));
			coinWithdrawalAmount -= (stackSize * Math.pow(9, logVal));
			debitAccount(accountNumber, (int) (stackSize * Math.pow(9, logVal)));
			accountBalance = getAccountBalance(accountNumber);
		}
		if (coinWithdrawalAmount <= 0) {
			withdrawCoins = false;
			coinWithdrawalAmount = 0;
		}
	}
	
	private int getAccountBalance(String accountNumber) {
		if (getWorldString(accountNumber) != "") {
			return getWorldInt(accountNumber);
		} else return -1;	
	}
	
	private void debitAccount(String accountNumber, int amount) {
		if (getWorldString(accountNumber) != "") {
			int balance = getWorldInt(accountNumber);
			balance -= amount;
			setWorldData(accountNumber, balance);
		}
	}
	
	private void creditAccount(String accountNumber, int amount) {
		if (getWorldString(accountNumber) != "") {
			int balance = getWorldInt(accountNumber);
			balance += amount;
			setWorldData(accountNumber, balance);
		}
	}
	
	private String getPlayerAccount(String playerUID) {
		//returns an empty string if no account found
		return getWorldString(playerUID);
	}
	
	private void addPlayerAccount(String playerUID) {
		if (getWorldString(playerUID) == "") {
			while (getWorldString(accountNumber) == "") {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(accountNumber) == "") {
					setWorldData(playerUID, accountNumber);
					setWorldData(accountNumber, 0);
				}
			}
		} else {
			//we have a problem we need to clear stale account data
			
		}
	}
	
	private String getCustomAccount(String playerUID){
		return getWorldString("¿" + playerUID);
	}
	
	private boolean addCustomAccount(String customName) {
		//custom accounts are added as a relation of playername to customname
		//customnames are then associated with an account number
		if (getWorldString("¿" + playerUID) == "" && getWorldString(customName) == "") {
			while (getWorldString(customAccountNumber) == "") {
				customAccountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(customAccountNumber) == "") {
					setWorldData("¿" + playerUID, customName);
					setWorldData(customName, customAccountNumber);
					setWorldData(customAccountNumber, 0);
					return true;
				}
			}
		}
		return false;
	}
	
	private void transferCustomAccount() {
		String oldName = getWorldString("¿" + playerUID);
		String oldAccount = getWorldString(oldName);
		int oldBalance = getAccountBalance(oldAccount);
		delWorldData("¿" + playerUID);
		delWorldData(oldName);
		delWorldData(oldAccount);
		if (getWorldString("¿" + playerUID) == "") {
			customAccountNumber = "none";
			while (getWorldString(customAccountNumber) == "") {
				customAccountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(customAccountNumber) == "") {
					setWorldData("¿" + playerUID, customAccountName);
					setWorldData(customAccountName, customAccountNumber);
					setWorldData(customAccountNumber, oldBalance);
				}
				if (getWorldString(oldAccount) != "") {
					delWorldData(oldAccount);
					delWorldData(oldName);
				}
			}
		}
	}
	
	private void transferPlayerAccount(String playerUID) {
		String oldAccount = getWorldString(playerUID);
		int oldBalance = getAccountBalance(oldAccount);
		delWorldData(playerUID);
		if (getWorldString(playerUID) == "") {
			accountNumber = "none";
			while (getWorldString(accountNumber) == "") {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(accountNumber) == "") {
					setWorldData(playerUID, accountNumber);
					setWorldData(accountNumber, oldBalance);
				}
			}
		}
		delWorldData(oldAccount);
	}
	
	private int generateAccountNumber() {
		return (int) (Math.floor(Math.random() * 99999999) + 11111111);
	}
	
	private void setWorldData(String tag, String data) {
		UCWorldData wData = UCWorldData.get(super.worldObj);
		NBTTagCompound wdTag = wData.getData();
		wdTag.setString(tag, data);
		wData.markDirty();
	}
	
	private void setWorldData(String tag, int data) {
		UCWorldData wData = UCWorldData.get(super.worldObj);
		NBTTagCompound wdTag = wData.getData();
		wdTag.setInteger(tag, data);
		wData.markDirty();
	}
	
	private int getWorldInt(String tag) {
		UCWorldData wData = UCWorldData.get(super.worldObj);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getInteger(tag);
	}
	
	private String getWorldString(String tag) {
		UCWorldData wData = UCWorldData.get(super.worldObj);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getString(tag);
	}
	
	private void delWorldData(String tag) {
		UCWorldData wData = UCWorldData.get(super.worldObj);
		NBTTagCompound wdTag = wData.getData();
		wdTag.removeTag(tag);
		wData.markDirty();
	}

	public String getInventoryName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCustomName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn,
			EnumFacing direction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack,
			EnumFacing direction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getField(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}
