package universalcoins.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.ATMWithdrawalMessage;
import universalcoins.util.CoinUtils;
import universalcoins.util.UniversalAccounts;

public class TileATM extends TileProtected implements IInventory, ISidedInventory {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
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
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventory.get(index);
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (size < stack.getCount()) {
			stack = stack.splitStack(size);
		} else {
			setInventorySlotContents(slot, ItemStack.EMPTY);
		}
		fillCoinSlot();
		return stack;
	}

	// @Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		ItemStack itemstack = this.inventory.get(slot);
		inventory.remove(slot);
		return itemstack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (slot == itemCoinSlot) {
			int coinValue = CoinUtils.getCoinValue(stack);
			if (depositCoins && coinValue > 0) {
				long depositAmount = Math.min(stack.getCount(), (Long.MAX_VALUE - accountBalance) / coinValue);
				if (!world.isRemote) {
					UniversalAccounts.getInstance().creditAccount(accountNumber, depositAmount * coinValue, false);
					accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
				}
				ItemStack newStack = inventory.get(slot);
				newStack.shrink((int) depositAmount);
				inventory.set(slot, newStack);
				if (inventory.get(slot).getCount() == 0) {
					inventory.set(slot, ItemStack.EMPTY);
				}
			}
		}
		if (slot == itemCardSlot && !world.isRemote)

		{
			if (!inventory.get(itemCardSlot).hasTagCompound()) {
				return;
			}
			accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			cardOwner = inventory.get(itemCardSlot).getTagCompound().getString("Owner");
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
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

	public void sendServerUpdatePacket(int withdrawalAmount) {
		UniversalCoins.snw.sendToServer(new ATMWithdrawalMessage(pos.getX(), pos.getY(), pos.getZ(), withdrawalAmount));
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.size()) {
				inventory.get(slot).deserializeNBT(tag);
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
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.get(i);
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

		markDirty();
		return tagCompound;
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
			if (UniversalAccounts.getInstance().getAccountBalance(accountNumber) > 1000000) {
				inventory.set(itemCardSlot, new ItemStack(UniversalCoins.Items.ender_card, 1));
			} else {
				inventory.set(itemCardSlot, new ItemStack(UniversalCoins.Items.uc_card, 1));
			}
			inventory.get(itemCardSlot).setTagCompound(new NBTTagCompound());
			inventory.get(itemCardSlot).getTagCompound().setString("Name", playerName);
			inventory.get(itemCardSlot).getTagCompound().setString("Owner", playerUID);
			inventory.get(itemCardSlot).getTagCompound().setString("Account", accountNumber);
			accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
			cardOwner = playerUID;
		}
		if (functionId == 2) {
			if (!(UniversalAccounts.getInstance().getPlayerAccount(playerUID).matches(""))) {
				UniversalAccounts.getInstance().transferPlayerAccount(playerUID);
				inventory.set(itemCardSlot, new ItemStack(UniversalCoins.Items.uc_card, 1));
				inventory.get(itemCardSlot).setTagCompound(new NBTTagCompound());
				inventory.get(itemCardSlot).getTagCompound().setString("Name", playerName);
				inventory.get(itemCardSlot).getTagCompound().setString("Owner", playerUID);
				inventory.get(itemCardSlot).getTagCompound().setString("Account",
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
			if (accountNumber.matches("none") && inventory.get(itemCardSlot) != null) {
				accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
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
			inventory.set(itemCardSlot, ItemStack.EMPTY);
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
		if (inventory.get(itemCoinSlot) == ItemStack.EMPTY && coinWithdrawalAmount > 0) {
			if (coinWithdrawalAmount >= UniversalCoins.coinValues[4]) {
				inventory.set(itemCoinSlot, new ItemStack(UniversalCoins.Items.obsidian_coin));
				inventory.get(itemCoinSlot)
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[4], 64));
				coinWithdrawalAmount -= inventory.get(itemCoinSlot).getCount() * UniversalCoins.coinValues[4];
			} else if (coinWithdrawalAmount >= UniversalCoins.coinValues[3]) {
				inventory.set(itemCoinSlot, new ItemStack(UniversalCoins.Items.diamond_coin));
				inventory.get(itemCoinSlot)
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[3], 64));
				coinWithdrawalAmount -= inventory.get(itemCoinSlot).getCount() * UniversalCoins.coinValues[3];
			} else if (coinWithdrawalAmount >= UniversalCoins.coinValues[2]) {
				inventory.set(itemCoinSlot, new ItemStack(UniversalCoins.Items.emerald_coin));
				inventory.get(itemCoinSlot)
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[2], 64));
				coinWithdrawalAmount -= inventory.get(itemCoinSlot).getCount() * UniversalCoins.coinValues[2];
			} else if (coinWithdrawalAmount >= UniversalCoins.coinValues[1]) {
				inventory.set(itemCoinSlot, new ItemStack(UniversalCoins.Items.gold_coin));
				inventory.get(itemCoinSlot)
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[1], 64));
				coinWithdrawalAmount -= inventory.get(itemCoinSlot).getCount() * UniversalCoins.coinValues[1];
			} else if (coinWithdrawalAmount >= UniversalCoins.coinValues[0]) {
				inventory.set(itemCoinSlot, new ItemStack(UniversalCoins.Items.iron_coin));
				inventory.get(itemCoinSlot)
						.setCount((int) Math.min(coinWithdrawalAmount / UniversalCoins.coinValues[0], 64));
				coinWithdrawalAmount -= inventory.get(itemCoinSlot).getCount() * UniversalCoins.coinValues[0];
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
		return null;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inventory.get(index);
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	public boolean isUsableByPlayer(EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this || player == null) {
			return false;
		} else {
			return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
					(double) this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}
}
