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
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.util.UCWorldData;

public class TileSafe extends TileEntity implements IInventory, ISidedInventory {
	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemInputSlot = 0;
	public static final int itemOutputSlot = 1;
	private static final int[] multiplier = new int[] {1, 9, 81, 729, 6561};
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack, 
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
	public String blockOwner = "nobody";
	public int accountBalance = 0;


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
		coinsTaken(stack);
		return stack;
	}
	
	public void coinsTaken(ItemStack stack) {
		int coinType = getCoinType(stack.getItem());
		if (coinType != -1) {
			int itemValue = multiplier[coinType];
			int debitAmount = 0;
			debitAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - accountBalance) / itemValue);
			if(!worldObj.isRemote) {
				debitAccount(debitAmount * itemValue);
				updateAccountBalance();
			}
			fillOutputSlot();
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return getStackInSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			int coinType = getCoinType(stack.getItem());
			if (coinType != -1) {
				int itemValue = multiplier[coinType];
				int depositAmount = 0;
				depositAmount = Math.min(stack.stackSize,(Integer.MAX_VALUE - accountBalance) / itemValue);
				if (!worldObj.isRemote) {
					creditAccount(depositAmount * itemValue);
					updateAccountBalance();
				}
				inventory[slot].stackSize -= depositAmount;
				if (inventory[slot].stackSize == 0) {
					inventory[slot] = null;
				}
				fillOutputSlot();
			}
		}
	}
	
	public void fillOutputSlot() {
		if (accountBalance > 0) {
			// use logarithm to find largest cointype for the balance
			int logVal = Math.min((int) (Math.log(accountBalance) / Math.log(9)), 4);
			int stackSize = Math.min((int) (accountBalance / Math.pow(9, logVal)), 64);
			// add a stack to the slot
			inventory[itemOutputSlot] = new ItemStack(coins[logVal], stackSize);
		} 
	}

	public String getInventoryName() {
		return StatCollector.translateToLocal("tile.blockSafe.name");
	}

	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5,
						pos.getZ() + 0.5) < 64;
	}
	
	private int getCoinType(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		//we only have a coin input slot
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
		tagCompound.setInteger("Balance", accountBalance);
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
			blockOwner = tagCompound.getString("Owner");
		} catch (Throwable ex2) {
			blockOwner = "nobody";
		}
		try {
			accountBalance = tagCompound.getInteger("Balance");
		} catch (Throwable ex2) {
			accountBalance = 0;
		}
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
	if (accountBalance == 0) inventory[itemOutputSlot] = null;
	}
    
	public void updateTE() {
		worldObj.markBlockForUpdate(pos);
	}
	
	private String getPlayerUID(String playerName) {
		World world = super.getWorld();
		EntityPlayer player = world.getPlayerEntityByName(playerName);
		return player.getUniqueID().toString();
	}
	
	private String getPlayerAccount(String playerUID) {
		//creates new account if none found
		//always returns an account number
		String accountNumber = getWorldString(playerUID);
		if (accountNumber == "") {
			addPlayerAccount(playerUID);
		}
		return getWorldString(playerUID);
	}
	
	private void addPlayerAccount(String playerUID) {
		String accountNumber = "";
		if (getWorldString(playerUID) == "") {
			while (getWorldString(accountNumber) == "") {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(accountNumber) == "") {
					setWorldData(playerUID, accountNumber);
					setWorldData(accountNumber, 0);
				}
			}
		}
	}
	
	private int generateAccountNumber() {
		return (int) (Math.floor(Math.random() * 99999999) + 11111111);
	}
	
	public void updateAccountBalance() {
		if (blockOwner != "") {
			String accountNumber = getPlayerAccount(getPlayerUID(blockOwner));
			if (getWorldString(accountNumber) != "") {
				accountBalance = getWorldInt(accountNumber);
			}
		}
	}
	
	private void creditAccount(int amount) {
		String accountNumber = getPlayerAccount(getPlayerUID(blockOwner));
		if (getWorldString(accountNumber) != "") {
			int balance = getWorldInt(accountNumber);
			balance += amount;
			setWorldData(accountNumber, balance);
		}
	}
	
	public boolean debitAccount(int amount) {
		if (blockOwner != "") {
			String accountNumber = getPlayerAccount(getPlayerUID(blockOwner));
			if (getWorldString(accountNumber) != "") {
				int balance = getWorldInt(accountNumber);
				balance -= amount;
				setWorldData(accountNumber, balance);
				return true;
			}
		} return false;
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

	@Override
	public String getName() {
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
		return new int[] {0};
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack,
			EnumFacing direction) {
		if (index == 0) {
			int coinType = getCoinType(stack.getItem());
			if (coinType != -1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack,
			EnumFacing direction) {
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