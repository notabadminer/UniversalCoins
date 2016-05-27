package universalcoins.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.net.ATMWithdrawalMessage;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;

public class TileATM extends TileEntity implements IInventory, ISidedInventory {
	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemCoinSlot = 0;
	public static final int itemCardSlot = 1;
	public String blockOwner = "";
	public String playerName = "";
	public String playerUID = "";
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

	// @Override
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
				FMLLog.info("in setInventorySlotContents"); // TODO
				switch (stack.getItem().getUnlocalizedName()) {
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
				FMLLog.info("setting card info"); // TODO
				accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
				cardOwner = inventory[itemCardSlot].getTagCompound().getString("Owner");
				accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
			}
			FMLLog.info("accountNumber: " + accountNumber); // TODO
			FMLLog.info("depositCoins: " + depositCoins); // TODO
		}
	}

	@Override
	public String getName() {
		return UniversalCoins.proxy.atm.getLocalizedName();
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
		UniversalCoins.snw
				.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), functionID, shiftPressed));
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new SPacketUpdateTileEntity(pos, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public void sendServerUpdatePacket(int withdrawalAmount) {
		UniversalCoins.snw.sendToServer(new ATMWithdrawalMessage(pos.getX(), pos.getY(), pos.getZ(), withdrawalAmount));
	}

	public void updateTE() {
		markDirty();
		worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
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
			blockOwner = tagCompound.getString("BlockOwner");
		} catch (Throwable ex2) {
			blockOwner = null;
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
			accountNumber = tagCompound.getString("accountNumber");
		} catch (Throwable ex2) {
			accountNumber = "none";
		}
		try {
			accountBalance = tagCompound.getLong("accountBalance");
		} catch (Throwable ex2) {
			accountBalance = 0;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
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
		tagCompound.setString("BlockOwner", blockOwner);
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setBoolean("DepositCoins", depositCoins);
		tagCompound.setBoolean("WithdrawCoins", withdrawCoins);
		tagCompound.setInteger("CoinWithdrawalAmount", coinWithdrawalAmount);
		tagCompound.setString("CardOwner", cardOwner);
		tagCompound.setString("accountNumber", accountNumber);
		tagCompound.setLong("accountBalance", accountBalance);

		//TODO: Is this the right tag compound to return?
		return tagCompound;
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
		if (functionId == 1) {
			accountNumber = UniversalAccounts.getInstance().getOrCreatePlayerAccount(playerUID);
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.uc_card, 1);
			inventory[itemCardSlot].setTagCompound(new NBTTagCompound());
			inventory[itemCardSlot].getTagCompound().setString("Name", playerName);
			inventory[itemCardSlot].getTagCompound().setString("Owner", playerUID);
			inventory[itemCardSlot].getTagCompound().setString("Account", accountNumber);
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
			cardOwner = playerUID;
		}
		if (functionId == 2) {
			if (!(UniversalAccounts.getInstance().getPlayerAccount(playerUID).matches(""))) {
				UniversalAccounts.getInstance().transferPlayerAccount(playerUID);
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.uc_card, 1);
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
			if (accountNumber.matches("none") && inventory[itemCardSlot] != null) {
				FMLLog.info("updating card info in function 3");// TODO
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
			if (!storedAccount.matches("")) {
				accountNumber = storedAccount;
				cardOwner = playerUID; // needed for new card auth
				accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
			}
		} else
			accountNumber = "none";
		if (functionId == 6) {
			inventory[itemCardSlot] = null;
		}
	}

	public void fillCoinSlot() {
		if (inventory[itemCoinSlot] == null && coinWithdrawalAmount > 0) {
			if (coinWithdrawalAmount > UniversalCoins.coinValues[4]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.obsidian_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[4],
						64);
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[3]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.diamond_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[3],
						64);
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[2]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.emerald_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[2],
						64);
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[1]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.gold_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[1],
						64);
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[0]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.proxy.iron_coin);
				inventory[itemCoinSlot].stackSize = (int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[0],
						64);
			}
		}
		if (coinWithdrawalAmount <= 0) {
			withdrawCoins = false;
			coinWithdrawalAmount = 0;
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.proxy.atm.getLocalizedName());
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
	public ItemStack removeStackFromSlot(int index) {
		// TODO Auto-generated method stub
		return null;
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
