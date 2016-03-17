package universalcoins.tile;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerReceiver extends TileEntity implements ITickable, IInventory, IEnergyProvider {

	private ItemStack[] inventory = new ItemStack[3];
	public static final int itemCardSlot = 0;
	public static final int itemCoinSlot = 1;
	public static final int itemOutputSlot = 2;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
	public int coinSum = 0;
	public int rfLevel = 0;
	public long wrfLevel = 0;
	public String blockOwner = "nobody";
	public EnumFacing orientation = null;

	@Override
	public void update() {
		if (!worldObj.isRemote) {
			buyPower();
			sendPower();
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
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			if (slot == itemCardSlot && inventory[itemCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard) {
				if (creditAccount(coinSum)) {
					coinSum = 0;
				}
			}
			if (slot == itemCoinSlot) {
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = 0;
					depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
					coinSum += depositAmount * itemValue;
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
				}

			}
		}
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
	public String getName() {
		return UniversalCoins.proxy.blockPowerReceiver.getLocalizedName();
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return rfLevel;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return Integer.MAX_VALUE;
	}

	private int getAccountBalance() {
		if (worldObj.isRemote || inventory[itemCardSlot] == null || !inventory[itemCardSlot].hasTagCompound()) {
			return 0;
		}
		String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
		if (accountNumber == "") {
			return 0;
		}
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private boolean creditAccount(int i) {
		if (worldObj.isRemote || inventory[itemCardSlot] == null
				|| inventory[itemCardSlot].getItem() != UniversalCoins.proxy.itemEnderCard
				|| !inventory[itemCardSlot].hasTagCompound())
			return false;
		String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, i);
	}

	private boolean debitAccount(int i) {
		if (worldObj.isRemote || inventory[itemCardSlot] == null || !inventory[itemCardSlot].hasTagCompound())
			return false;
		String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().debitAccount(accountNumber, i);
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
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

	public void updateTE() {
		worldObj.markBlockForUpdate(pos);
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
		tagCompound.setInteger("coinSum", coinSum);
		tagCompound.setInteger("rfLevel", rfLevel);
		tagCompound.setLong("wrfLevel", wrfLevel);
		tagCompound.setString("blockOwner", blockOwner);
		if (orientation != null) {
			tagCompound.setInteger("orientation", orientation.ordinal());
		}
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
			coinSum = tagCompound.getInteger("coinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			rfLevel = tagCompound.getInteger("rfLevel");
		} catch (Throwable ex2) {
			rfLevel = 0;
		}
		try {
			wrfLevel = tagCompound.getLong("wrfLevel");
		} catch (Throwable ex2) {
			wrfLevel = 0;
		}
		try {
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "nobody";
		}
		try {
			orientation = orientation.getFront(tagCompound.getInteger("orientation"));
		} catch (Throwable ex2) {
			orientation = null;
		}
	}

	public void onButtonPressed(int buttonId) {
		if (buttonId == 0) {
			fillOutputSlot();
		}
	}

	public void fillOutputSlot() {
		inventory[itemOutputSlot] = null;
		if (coinSum > 0) {
			// use logarithm to find largest cointype for the balance
			int logVal = Math.min((int) (Math.log(coinSum) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinSum / Math.pow(9, logVal)), 64);
			// add a stack to the slot
			inventory[itemOutputSlot] = new ItemStack(coins[logVal], stackSize);
			int itemValue = multiplier[logVal];
			int debitAmount = 0;
			debitAmount = Math.min(stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
			if (!worldObj.isRemote) {
				coinSum -= debitAmount * itemValue;
			}
		}
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
		if (!simulate) {
			rfLevel -= maxExtract;
			if (coinSum - UniversalCoins.rfRetailRate >= 0) {
				coinSum -= UniversalCoins.rfRetailRate;
			}
		}
		return Math.min(rfLevel, 1000);
	}

	protected void buyPower() {
		if (!UniversalCoins.powerBaseRecipeEnabled) {
			//if we have no base, we use infinite power
			if (rfLevel == 0 && debitAccount(UniversalCoins.rfRetailRate)) {
				rfLevel += 10000;
			} else if (rfLevel == 0 && coinSum - UniversalCoins.rfRetailRate >= 0) {
					coinSum -= UniversalCoins.rfRetailRate;
					rfLevel += 10000;
			}
			wrfLevel = UniversalPower.getInstance().getRFLevel();
			return;
		}
		if (rfLevel == 0 && UniversalPower.getInstance().extractEnergy(10, true) > 0
				&& debitAccount(UniversalCoins.rfRetailRate)) {
			UniversalPower.getInstance().extractEnergy(10, false);
			rfLevel += 10000;
		} else if (rfLevel == 0 && UniversalPower.getInstance().extractEnergy(10, true) > 0 
				&& coinSum - UniversalCoins.rfRetailRate >= 0) {
				coinSum -= UniversalCoins.rfRetailRate;
				UniversalPower.getInstance().extractEnergy(10, false);
				rfLevel += 10000;
		}
		wrfLevel = UniversalPower.getInstance().getRFLevel();
	}

	protected void sendPower() {
		if (orientation == null) {
			return;
		}
		TileEntity tile = worldObj.getTileEntity(new BlockPos(pos.getX() + orientation.getFrontOffsetX(), pos.getY() + orientation.getFrontOffsetX(),
				pos.getZ() + orientation.getFrontOffsetZ()));
		if (tile != null && tile instanceof IEnergyReceiver) {
			IEnergyReceiver handler = (IEnergyReceiver) tile;
			int maxRF = handler.receiveEnergy(orientation.getOpposite(), Math.min(1000, rfLevel), true);
			rfLevel -= handler.receiveEnergy(orientation.getOpposite(), maxRF, false);

		} else {
			orientation = null;
		}
	}

	public void resetPowerDirection() {
		for (EnumFacing direction : EnumFacing.VALUES) {
			TileEntity tile = worldObj.getTileEntity((new BlockPos(pos.getX() + direction.getFrontOffsetX(), pos.getY() + direction.getFrontOffsetX(),
					pos.getZ() + direction.getFrontOffsetZ())));
			if (tile instanceof IEnergyReceiver) {
				orientation = direction;
			}
		}
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentText(UniversalCoins.proxy.blockPowerReceiver.getLocalizedName());
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inventory[index];
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
