package universalcoins.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCCardStationServerCustomNameMessage;
import universalcoins.net.UCCardStationServerWithdrawalMessage;
import universalcoins.util.UniversalAccounts;

public class TileCardStation extends TileEntity implements IInventory, ISidedInventory {
	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemCoinSlot = 0;
	public static final int itemCardSlot = 1;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
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

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
		inUse = false;
		withdrawCoins = false;
		depositCoins = false;
		accountNumber = "none";
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
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= size) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(size);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		fillCoinSlot();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.inventory[slot] != null) {
			ItemStack itemstack = this.inventory[slot];
			this.inventory[slot] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			if (slot == itemCoinSlot && depositCoins && !accountNumber.contentEquals("none")) {
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - accountBalance) / itemValue);
					if (!worldObj.isRemote) {
						UniversalAccounts.getInstance().creditAccount(accountNumber,
								depositAmount * itemValue);
						accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
					}
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
				}
			}
			if (slot == itemCardSlot && !worldObj.isRemote) {
				accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
				cardOwner = inventory[itemCardSlot].getTagCompound().getString("Owner");
				if (UniversalAccounts.getInstance().getCustomAccount(playerUID) != "")
					customAccountName = UniversalAccounts.getInstance().getCustomAccount(playerUID);
				accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
			}
		}
	}

	@Override
	public String getName() {
		return UniversalCoins.proxy.blockCardStation.getLocalizedName();
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public void sendButtonMessage(int functionID, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), functionID,
				shiftPressed));
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(pos, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public void sendServerUpdatePacket(int withdrawalAmount) {
		UniversalCoins.snw.sendToServer(new UCCardStationServerWithdrawalMessage(pos.getX(), pos.getY(), pos.getZ(),
				withdrawalAmount));
	}

	public void sendServerUpdatePacket(String customName) {
		UniversalCoins.snw.sendToServer(new UCCardStationServerCustomNameMessage(pos.getX(), pos.getY(), pos.getZ(),
				customName));
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(pos);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
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
		try {
			cardOwner = tagCompound.getString("CardOwner");
		} catch (Throwable ex2) {
			cardOwner = "";
		}
		try {
			accountBalance = tagCompound.getInteger("accountBalance");
		} catch (Throwable ex2) {
			accountBalance = 0;
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
		tagCompound.setString("CardOwner", cardOwner);
		tagCompound.setInteger("accountBalance", accountBalance);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
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
		accountError = false; // reset error state
		// handle function IDs sent from CardStationGUI
		// function1 - new card
		// function2 - transfer account
		// function3 - deposit
		// function4 - withdraw
		// function5 - get account info
		// function6 - destroy invalid card
		// function7 - new custom account
		// function8 - new custom card
		// function9 - transfer custom account
		// function10 - account error reset
		if (functionId == 1) {
			accountNumber = UniversalAccounts.getInstance().getOrCreatePlayerAccount(playerUID);
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].setTagCompound(new NBTTagCompound());
			inventory[itemCardSlot].getTagCompound().setString("Name", playerName);
			inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
			inventory[itemCardSlot].getTagCompound().setString("Account", accountNumber);
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
			cardOwner = playerUID;
		}
		if (functionId == 2) {
			if (UniversalAccounts.getInstance().getPlayerAccount(playerUID) == "") {
			} else {
				UniversalAccounts.getInstance().transferPlayerAccount(playerUID);
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
				inventory[itemCardSlot].setTagCompound(new NBTTagCompound());
				inventory[itemCardSlot].getTagCompound().setString("Name", playerName);
				inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
				inventory[itemCardSlot].getTagCompound().setString("Account",
						UniversalAccounts.getInstance().getPlayerAccount(playerUID));
				accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
				cardOwner = playerUID;
			}
		}
		if (functionId == 3) {
			// set to true if player presses deposit button, reset on any other
			// button press
			depositCoins = true;
			withdrawCoins = false;
			// set account number if not already set and we have a card present
			if (accountNumber.contentEquals("none") && inventory[itemCardSlot] != null) {
				accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			}
		} else {
			depositCoins = false;
		}
		if (functionId == 4) {
			withdrawCoins = true;
			depositCoins = false;
			fillCoinSlot();
		} else
			withdrawCoins = false;
		if (functionId == 5) {
			String storedAccount = UniversalAccounts.getInstance().getPlayerAccount(playerUID);
			if (storedAccount != "") {
				accountNumber = storedAccount;
				cardOwner = playerUID; // needed for new card auth
				accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
				if (UniversalAccounts.getInstance().getCustomAccount(playerUID) != "") {
					customAccountName = UniversalAccounts.getInstance().getCustomAccount(playerUID);
					customAccountNumber = UniversalAccounts.getInstance().getPlayerAccount(customAccountName);
				}
			} else
				accountNumber = "none";
		}
		if (functionId == 6) {
			inventory[itemCardSlot] = null;
		}
		if (functionId == 7) {
			if (UniversalAccounts.getInstance().getPlayerAccount(customAccountName) != ""
					&& !UniversalAccounts.getInstance().getCustomAccount(playerUID)
							.contentEquals(customAccountName)) {
				accountError = true;
				// we need to reset this so that that function 7 is called again
				// on next attempt at getting an account
				customAccountName = "none";
				return;
			} else if (UniversalAccounts.getInstance().getCustomAccount(playerUID) == "") {
				UniversalAccounts.getInstance().addCustomAccount(customAccountName, accountNumber);
			}
			customAccountName = UniversalAccounts.getInstance().getCustomAccount(playerUID);
			customAccountNumber = UniversalAccounts.getInstance().getPlayerAccount(customAccountName);
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].setTagCompound(new NBTTagCompound());
			inventory[itemCardSlot].getTagCompound().setString("Name", customAccountName);
			inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
			inventory[itemCardSlot].getTagCompound().setString("Account", customAccountNumber);
		}
		if (functionId == 8) {
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].setTagCompound(new NBTTagCompound());
			inventory[itemCardSlot].getTagCompound().setString("Name", customAccountName);
			inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
			inventory[itemCardSlot].getTagCompound().setString("Account", customAccountNumber);
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(customAccountNumber);
		}
		if (functionId == 9) {
			if (UniversalAccounts.getInstance().getCustomAccount(playerUID) == ""
					|| UniversalAccounts.getInstance().getPlayerAccount(customAccountName) != "") {
				accountError = true;
			} else {
				accountError = false;
				UniversalAccounts.getInstance().transferCustomAccount(accountNumber, accountNumber);
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
				inventory[itemCardSlot].setTagCompound(new NBTTagCompound());
				inventory[itemCardSlot].getTagCompound().setString("Name", customAccountName);
				inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
				inventory[itemCardSlot].getTagCompound().setString("Account", customAccountNumber);
				accountBalance = UniversalAccounts.getInstance().getAccountBalance(customAccountNumber);
			}
		}
	}

	private void fillCoinSlot() {
		if (inventory[itemCoinSlot] == null && coinWithdrawalAmount > 0) {
			// use logarithm to find largest cointype for coins being withdrawn
			int logVal = Math.min((int) (Math.log(coinWithdrawalAmount) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinWithdrawalAmount / Math.pow(9, logVal)), 64);
			inventory[itemCoinSlot] = (new ItemStack(coins[logVal], stackSize));
			coinWithdrawalAmount -= (stackSize * Math.pow(9, logVal));
			UniversalAccounts.getInstance().debitAccount(accountNumber,
					(int) (stackSize * Math.pow(9, logVal)));
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
		if (coinWithdrawalAmount <= 0) {
			withdrawCoins = false;
			coinWithdrawalAmount = 0;
		}
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
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
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
