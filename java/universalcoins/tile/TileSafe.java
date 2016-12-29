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
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.util.UniversalAccounts;

public class TileSafe extends TileEntity implements IInventory, ISidedInventory {
	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemInputSlot = 0;
	public static final int itemOutputSlot = 1;
	public String blockOwner = "nobody";
	public String accountNumber = "0";
	public long accountBalance = 0;

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0 };
	}

	public boolean canInsertItem(int slot, ItemStack stack, int var3) {
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int var3) {
		return false;
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
			if (slot == itemOutputSlot) {
				coinsTaken(stack);
			}
		}
		return stack;
	}

	public void coinsTaken(ItemStack stack) {
		int coinValue = 0;
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
		int debitAmount = 0;
		long accountCapacity = (Long.MAX_VALUE - accountBalance);
		debitAmount = (int) Math.min(stack.stackSize, accountCapacity / coinValue);
		if (!worldObj.isRemote) {
			UniversalAccounts.getInstance().debitAccount(accountNumber, debitAmount * coinValue);
			updateAccountBalance();
		}
		fillOutputSlot();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return getStackInSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		int coinValue = 0;
		if (stack != null) {
			if (slot == itemInputSlot) {
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
				if (coinValue > 0) {
					long depositAmount = Math.min(Long.MAX_VALUE - accountBalance / coinValue, stack.stackSize);
					if (!worldObj.isRemote)
						UniversalAccounts.getInstance().creditAccount(accountNumber, depositAmount * coinValue);
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
					fillOutputSlot();
					if (!worldObj.isRemote)
						updateAccountBalance();
				}
			}
		}
	}

	public void updateAccountBalance() {
		accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	public void fillOutputSlot() {
		if (accountBalance > UniversalCoins.coinValues[4]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.obsidian_coin);
			inventory[itemOutputSlot].stackSize = (int) Math.min(accountBalance / UniversalCoins.coinValues[4], 64);
		} else if (accountBalance > UniversalCoins.coinValues[3]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.diamond_coin);
			inventory[itemOutputSlot].stackSize = (int) Math.min(accountBalance / UniversalCoins.coinValues[3], 64);
		} else if (accountBalance > UniversalCoins.coinValues[2]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.emerald_coin);
			inventory[itemOutputSlot].stackSize = (int) Math.min(accountBalance / UniversalCoins.coinValues[2], 64);
		} else if (accountBalance > UniversalCoins.coinValues[1]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.gold_coin);
			inventory[itemOutputSlot].stackSize = (int) Math.min(accountBalance / UniversalCoins.coinValues[1], 64);
		} else if (accountBalance > UniversalCoins.coinValues[0]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.iron_coin);
			inventory[itemOutputSlot].stackSize = (int) Math.min(accountBalance / UniversalCoins.coinValues[0], 64);
		}
	}

	@Override
	public String getInventoryName() {
		return UniversalCoins.proxy.safe.getLocalizedName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
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
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		// we only have a coin input slot
		return true;
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
		tagCompound.setString("Owner", blockOwner);
		tagCompound.setString("AccountNumber", accountNumber);
		tagCompound.setLong("accountBalance", accountBalance);
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
			blockOwner = tagCompound.getString("Owner");
		} catch (Throwable ex2) {
			blockOwner = "nobody";
		}
		try {
			accountNumber = tagCompound.getString("AccountNumber");
		} catch (Throwable ex2) {
			accountNumber = "0";
		}
		try {
			accountBalance = tagCompound.getLong("accountBalance");
		} catch (Throwable ex2) {
			accountBalance = 0;
		}
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
		if (accountBalance == 0)
			inventory[itemOutputSlot] = null;
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void setSafeAccount(EntityPlayer player) {
		accountNumber = UniversalAccounts.getInstance().getOrCreatePlayerAccount(player.getUniqueID().toString());
	}
}
