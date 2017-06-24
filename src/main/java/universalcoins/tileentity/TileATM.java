package universalcoins.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.ATMWithdrawalMessage;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;

public class TileATM extends TileProtected implements IInventory, ISidedInventory {
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
		if (world.isRemote)
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
			if (stack.getCount() <= size) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(size);
				if (stack.getCount() == 0) {
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
				long depositAmount = Math.min(stack.getCount(), (Long.MAX_VALUE - accountBalance) / coinValue);
				if (!world.isRemote) {
					UniversalAccounts.getInstance().creditAccount(accountNumber, depositAmount * coinValue, false);
					accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
				}
				inventory[slot].shrink((int) depositAmount);
				if (inventory[slot].getCount() == 0) {
					inventory[slot] = null;
				}
			}
			if (slot == itemCardSlot && !world.isRemote) {
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
	public String getName() {
		return UniversalCoins.Blocks.atm.getLocalizedName();
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
		return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), getUpdateTag());
	}

	// required for sync on chunk load
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return nbt;
	}

	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public void sendServerUpdatePacket(int withdrawalAmount) {
		UniversalCoins.snw.sendToServer(new ATMWithdrawalMessage(pos.getX(), pos.getY(), pos.getZ(), withdrawalAmount));
	}

	public void updateTE() {
		final IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 3);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot].deserializeNBT(tag);
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

		return tagCompound;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return world.getTileEntity(pos) == this
				&& player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}

	public void onButtonPressed(int functionId) {
		if (world.isRemote)
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
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.Items.uc_card, 1);
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
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.Items.uc_card, 1);
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
		}
		if (functionId == 6) {
			inventory[itemCardSlot] = null;
			inUseCleanup();
		}
	}

	public void startCoinWithdrawal(int amount) {
		if (UniversalAccounts.getInstance().debitAccount(accountNumber, amount, false)) {
			coinWithdrawalAmount = amount;
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
	}

	public void fillCoinSlot() {
		if (inventory[itemCoinSlot] == null && coinWithdrawalAmount > 0) {
			if (coinWithdrawalAmount > UniversalCoins.coinValues[4]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.Items.obsidian_coin);
				inventory[itemCoinSlot]
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[4], 64));
				coinWithdrawalAmount -= inventory[itemCoinSlot].getCount() * UniversalCoins.coinValues[4];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[3]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.Items.diamond_coin);
				inventory[itemCoinSlot]
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[3], 64));
				coinWithdrawalAmount -= inventory[itemCoinSlot].getCount() * UniversalCoins.coinValues[3];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[2]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.Items.emerald_coin);
				inventory[itemCoinSlot]
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[2], 64));
				coinWithdrawalAmount -= inventory[itemCoinSlot].getCount() * UniversalCoins.coinValues[2];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[1]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.Items.gold_coin);
				inventory[itemCoinSlot]
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[1], 64));
				coinWithdrawalAmount -= inventory[itemCoinSlot].getCount() * UniversalCoins.coinValues[1];
			} else if (coinWithdrawalAmount > UniversalCoins.coinValues[0]) {
				inventory[itemCoinSlot] = new ItemStack(UniversalCoins.Items.iron_coin);
				inventory[itemCoinSlot]
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[0], 64));
				coinWithdrawalAmount -= inventory[itemCoinSlot].getCount() * UniversalCoins.coinValues[0];
			}
		}
		if (coinWithdrawalAmount <= 0) {
			withdrawCoins = false;
			coinWithdrawalAmount = 0;
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.Blocks.atm.getLocalizedName());
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

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
}
