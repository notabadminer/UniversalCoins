package universalcoins.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.ATMWithdrawalMessage;
import universalcoins.util.UniversalAccounts;

public class TileATM extends TileEntity implements IInventory, ISidedInventory {
	private ItemStack[] inventory = new ItemStack[2];
	public String customName = "";
	public static final int itemCoinSlot = 0;
	public static final int itemCardSlot = 1;
	public String playerName = "";
	public String playerUID = "";
	public String blockOwner = "none";
	public boolean inUse = false;
	public boolean depositCoins = false;
	public boolean withdrawCoins = false;
	public boolean accountError = false;
	public int coinWithdrawalAmount = 0;
	public String cardOwner = "";
	public String accountNumber = "none";
	public long accountBalance = 0;

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
		inUse = false;
		withdrawCoins = false;
		depositCoins = false;
		accountNumber = "none";
		accountBalance = 0;
		updateTE();
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
		int coinValue = 0;
		if (stack != null) {
			if (slot == itemCoinSlot && depositCoins && !accountNumber.matches("none")) {
				switch (stack.getUnlocalizedName()) {
				case "item.iron_coin":
					coinValue = UniversalCoins.coinValues[0];
					break;
				case "item.gold_coin":
					coinValue = UniversalCoins.coinValues[1];
					break;
				case "item.emerald_coin":
					coinValue = UniversalCoins.coinValues[2];
					break;
				case "item.diamond_coin":
					coinValue = UniversalCoins.coinValues[3];
					break;
				case "item.obsidian_coin":
					coinValue = UniversalCoins.coinValues[4];
					break;
				}
				long depositAmount = Math.min(stack.stackSize, (Long.MAX_VALUE - accountBalance) / coinValue);
				if (!worldObj.isRemote) {
					UniversalAccounts.getInstance().creditAccount(accountNumber, depositAmount * coinValue);
					accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
				}
				inventory[slot].stackSize -= depositAmount;
				if (inventory[slot].stackSize == 0) {
					inventory[slot] = null;
				}
			}
			if (slot == itemCardSlot && !worldObj.isRemote) {
				if (!inventory[itemCardSlot].hasTagCompound()) {
					return;
				}
				accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
				cardOwner = inventory[itemCardSlot].getTagCompound().getString("Owner");
				accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
			}
		}
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : UniversalCoins.proxy.atm.getLocalizedName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public void sendButtonMessage(int functionID, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, functionID, shiftPressed));
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.func_148857_g());
	}

	public void sendServerUpdatePacket(int withdrawalAmount) {
		UniversalCoins.snw
				.sendToServer(new ATMWithdrawalMessage(xCoord, yCoord, zCoord, withdrawalAmount));
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
			customName = tagCompound.getString("customName");
		} catch (Throwable ex2) {
			customName = "";
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
			accountBalance = tagCompound.getLong("accountBalance");
		} catch (Throwable ex2) {
			accountBalance = 0;
		}
		try {
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "none";
		}
		try {
			cardOwner = tagCompound.getString("cardOwner");
		} catch (Throwable ex2) {
			cardOwner = "none";
		}
		try {
			accountNumber = tagCompound.getString("accountNumber");
		} catch (Throwable ex2) {
			accountNumber = "";
		}
		try {
			playerUID = tagCompound.getString("playerUID");
		} catch (Throwable ex2) {
			playerUID = "";
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
		tagCompound.setString("customName", customName);
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setBoolean("DepositCoins", depositCoins);
		tagCompound.setBoolean("WithdrawCoins", withdrawCoins);
		tagCompound.setInteger("CoinWithdrawalAmount", coinWithdrawalAmount);
		tagCompound.setLong("accountBalance", accountBalance);
		tagCompound.setString("blockOwner", blockOwner);
		tagCompound.setString("cardOwner", cardOwner);
		tagCompound.setString("accountNumber", accountNumber);
		tagCompound.setString("playerUID", playerUID);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openInventory() {

	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}

	public void onButtonPressed(int functionId) {
		if (worldObj.isRemote)
			return;
		accountError = false; // reset error state
		// handle function IDs sent from CardStationGUI
		// function1 - new card
		// function2 - transfer account
		// function3 - deposit
		// function4 - withdraw
		// function5 - get account info
		// function6 - destroy invalid card
		// function7 - account error reset
		if (functionId == 1) {
			if (getPlayerAccount(playerUID) == "") {
				addPlayerAccount(playerUID);
				accountNumber = getPlayerAccount(playerUID);
			}
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.uc_card, 1);
			inventory[itemCardSlot].stackTagCompound = new NBTTagCompound();
			inventory[itemCardSlot].stackTagCompound.setString("Name", playerName);
			inventory[itemCardSlot].stackTagCompound.setString("Owner", playerUID);
			inventory[itemCardSlot].stackTagCompound.setString("Account", accountNumber);
			accountBalance = getAccountBalance(accountNumber);
		}
		if (functionId == 2) {
			if (getPlayerAccount(playerUID) == "") {
			} else {
				transferPlayerAccount(playerUID);
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.uc_card, 1);
				inventory[itemCardSlot].stackTagCompound = new NBTTagCompound();
				inventory[itemCardSlot].stackTagCompound.setString("Name", playerName);
				inventory[itemCardSlot].stackTagCompound.setString("Owner", playerUID);
				inventory[itemCardSlot].stackTagCompound.setString("Account", getPlayerAccount(playerUID));
			}
		}
		if (functionId == 3) {
			// set to true if player presses deposit button, reset on any other
			// button press
			depositCoins = true;
			withdrawCoins = false;
			// set account number if not already set and we have a card present
			if (accountNumber.matches("none") && inventory[itemCardSlot] != null) {
				accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
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
			String storedAccount = getPlayerAccount(playerUID);
			if (!storedAccount.matches("")) {
				accountNumber = storedAccount;
				cardOwner = playerUID; // needed for new card auth
				accountBalance = getAccountBalance(accountNumber);
			}
		}
		if (functionId == 6) {
			inventory[itemCardSlot] = null;
			inUseCleanup();
		}
	}

	public void startCoinWithdrawal(int amount) {
		if (UniversalAccounts.getInstance().debitAccount(accountNumber, amount)) {
			coinWithdrawalAmount = amount;
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
	}

	public void fillCoinSlot() {
		if (inventory[itemCoinSlot] == null && coinWithdrawalAmount > 0) {
			if (coinWithdrawalAmount > UniversalCoins.coinValues[4]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.obsidian_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[4],
						64);
				coinWithdrawalAmount -= inventory[itemCoinSlot].stackSize * UniversalCoins.coinValues[4];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[3]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.diamond_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[3],
						64);
				coinWithdrawalAmount -= inventory[itemCoinSlot].stackSize * UniversalCoins.coinValues[3];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[2]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.emerald_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[2],
						64);
				coinWithdrawalAmount -= inventory[itemCoinSlot].stackSize * UniversalCoins.coinValues[2];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[1]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.gold_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[1],
						64);
				coinWithdrawalAmount -= inventory[itemCoinSlot].stackSize * UniversalCoins.coinValues[1];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[0]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.iron_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[0],
						64);
				coinWithdrawalAmount -= inventory[itemCoinSlot].stackSize * UniversalCoins.coinValues[0];
			}
		}
		if (coinWithdrawalAmount <= 0) {
			withdrawCoins = false;
			coinWithdrawalAmount = 0;
		}
	}

	private String getPlayerAccount(String playerUID) {
		return UniversalAccounts.getInstance().getPlayerAccount(playerUID);
	}

	private void addPlayerAccount(String playerUID) {
		UniversalAccounts.getInstance().addPlayerAccount(playerUID);
	}

	private long getAccountBalance(String accountNumber) {
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private void creditAccount(String accountNumber, long amount) {
		UniversalAccounts.getInstance().creditAccount(accountNumber, amount);
	}

	private void transferPlayerAccount(String playerUID) {
		UniversalAccounts.getInstance().transferPlayerAccount(playerUID);
	}

	private boolean debitAccount(String accountNumber, long amount) {
		return UniversalAccounts.getInstance().debitAccount(accountNumber, amount);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return null;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3) {
		return false;
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3) {
		return false;
	}
}
