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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import universalcoins.UniversalCoins;
import universalcoins.gui.PowerReceiverGUI;
import universalcoins.gui.TradeStationGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerReceiver extends TileEntity implements IInventory, IEnergyProvider {

	private ItemStack[] inventory = new ItemStack[3];
	public static final int itemCardSlot = 0;
	public static final int itemCoinSlot = 1;
	public static final int itemOutputSlot = 2;
	public long coinSum = 0;
	public int rfLevel = 0;
	public int rfOutput = 0;
	public long wrfLevel = 0;
	public String blockOwner = "nobody";
	public String playerName = "";
	public boolean publicAccess = false;
	public ForgeDirection orientation = null;

	@Override
	public void updateEntity() {
		super.updateEntity();
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
				inventory[slot] = null;
			} else {
				stack = stack.splitStack(size);
				if (stack.stackSize == 0) {
					inventory[slot] = null;
				}
			}
		}
		return stack;
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
			if (slot == itemCoinSlot) {
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
				long depositAmount = Math.min(stack.stackSize, (Long.MAX_VALUE - coinSum) / coinValue);
				inventory[slot].stackSize -= depositAmount;
				coinSum += depositAmount * coinValue;
				if (inventory[slot].stackSize == 0) {
					inventory[slot] = null;
				}
			}
		}
	}

	@Override
	public String getInventoryName() {
		return UniversalCoins.proxy.power_receiver.getLocalizedName();
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
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return rfLevel;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return Integer.MAX_VALUE;
	}

	private long getAccountBalance() {
		if (worldObj.isRemote || inventory[itemCardSlot] == null || !inventory[itemCardSlot].hasTagCompound()) {
			return 0;
		}
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		if (accountNumber == "") {
			return 0;
		}
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private boolean debitAccount(int i) {
		if (worldObj.isRemote || inventory[itemCardSlot] == null || !inventory[itemCardSlot].hasTagCompound())
			return false;
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().debitAccount(accountNumber, i);
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, button, shiftPressed));
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

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
		tagCompound.setLong("coinSum", coinSum);
		tagCompound.setInteger("rfLevel", rfLevel);
		tagCompound.setInteger("rfOutput", rfOutput);
		tagCompound.setLong("wrfLevel", wrfLevel);
		tagCompound.setString("blockOwner", blockOwner);
		if (orientation != null) {
			tagCompound.setInteger("orientation", orientation.ordinal());
		}
		tagCompound.setBoolean("publicAccess", publicAccess);
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
			coinSum = tagCompound.getLong("coinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			rfLevel = tagCompound.getInteger("rfLevel");
		} catch (Throwable ex2) {
			rfLevel = 0;
		}
		try {
			rfOutput = tagCompound.getInteger("rfOutput");
		} catch (Throwable ex2) {
			rfOutput = 0;
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
			orientation = orientation.getOrientation(tagCompound.getInteger("orientation"));
		} catch (Throwable ex2) {
			orientation = null;
		}
		try {
			publicAccess = tagCompound.getBoolean("publicAccess");
		} catch (Throwable ex2) {
			publicAccess = false;
		}
	}

	public void onButtonPressed(int buttonId) {
		if (buttonId == PowerReceiverGUI.idCoinButton) {
			fillOutputSlot();
		} else if (buttonId == PowerReceiverGUI.idAccessModeButton && blockOwner.matches(playerName)) {
			publicAccess ^= true;
		}	
	}

	public void fillOutputSlot() {
		if (inventory[itemOutputSlot] == null && coinSum > 0) {
			if (coinSum > UniversalCoins.coinValues[4]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.obsidian_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[4], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[4];
			} else if (coinSum > UniversalCoins.coinValues[3]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.diamond_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[3], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[3];
			} else if (coinSum > UniversalCoins.coinValues[2]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.emerald_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[2], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[2];
			} else if (coinSum > UniversalCoins.coinValues[1]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.gold_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[1], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[1];
			} else if (coinSum > UniversalCoins.coinValues[0]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.iron_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[0], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[0];
			}
		}
		if (coinSum <= 0) {
			coinSum = 0;
		}
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if (!simulate) {
			rfLevel -= maxExtract;
			if (coinSum - UniversalCoins.rfRetailRate >= 0) {
				coinSum -= UniversalCoins.rfRetailRate;
			}
		}
		return Math.min(rfLevel, maxExtract);
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
		rfOutput = 0;
		TileEntity tile = worldObj.getTileEntity(xCoord + orientation.offsetX, yCoord + orientation.offsetY,
				zCoord + orientation.offsetZ);
		if (tile != null && tile instanceof IEnergyReceiver) {
			IEnergyReceiver handler = (IEnergyReceiver) tile;
			rfOutput = handler.receiveEnergy(orientation.getOpposite(), rfLevel, true);
			rfLevel -= handler.receiveEnergy(orientation.getOpposite(), rfOutput, false);

		} else {
			orientation = null;
		}
	}

	public void resetPowerDirection() {
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldObj.getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY,
					zCoord + direction.offsetZ);
			if (tile instanceof IEnergyReceiver) {
				orientation = direction;
			}
		}
	}
}
