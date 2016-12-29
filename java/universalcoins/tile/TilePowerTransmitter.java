package universalcoins.tile;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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
import universalcoins.gui.PowerTransmitterGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerTransmitter extends TileEntity implements IInventory, IEnergyReceiver {

	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemCardSlot = 0;
	public static final int itemOutputSlot = 1;
	public long coinSum = 0;
	public int rfLevel = 0;
	public int krfSold = 0;
	public boolean publicAccess = false;
	public String blockOwner = "nobody";
	public String playerName = "";

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
		if (stack != null) {
			if (creditAccount(coinSum)) {
				coinSum = 0;
			}
		}
	}

	@Override
	public String getInventoryName() {
		return UniversalCoins.proxy.power_transmitter.getLocalizedName();
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
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if (!simulate) {
			int rfChunks = 0;
			rfLevel += maxReceive;
			if (rfLevel >= 10000) {
				// calculate how many 10k chunks we can sell
				rfChunks = (int) Math.floor(rfLevel / 10000);
				rfLevel -= rfChunks * 10000;
				boolean playerCredited = false;
				if (creditAccount(UniversalCoins.rfWholesaleRate * rfChunks)) {
					krfSold += Math.min(10 * rfChunks, Integer.MAX_VALUE - krfSold);
					UniversalPower.getInstance().receiveEnergy(10 * rfChunks, false);
				} else if (coinSum + UniversalCoins.rfWholesaleRate * rfChunks <= Integer.MAX_VALUE) {
					coinSum += UniversalCoins.rfWholesaleRate * rfChunks;
					krfSold += Math.min(10 * rfChunks, Integer.MAX_VALUE - krfSold);
					UniversalPower.getInstance().receiveEnergy(10 * rfChunks, false);
				}
			}
		}
		return maxReceive;
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

	private boolean creditAccount(long i) {
		if (worldObj.isRemote || inventory[itemCardSlot] == null
				|| inventory[itemCardSlot].getItem() != UniversalCoins.proxy.ender_card
				|| !inventory[itemCardSlot].hasTagCompound())
			return false;
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, i);
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
		tagCompound.setInteger("krfSold", krfSold);
		tagCompound.setString("blockOwner", blockOwner);
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
			krfSold = tagCompound.getInteger("krfSold");
		} catch (Throwable ex2) {
			krfSold = 0;
		}
		try {
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "nobody";
		}
		try {
			publicAccess = tagCompound.getBoolean("publicAccess");
		} catch (Throwable ex2) {
			publicAccess = false;
		}
	}

	public void onButtonPressed(int buttonId) {
		if (buttonId == PowerTransmitterGUI.idCoinButton) {
			fillOutputSlot();
		} else if (buttonId == PowerTransmitterGUI.idAccessModeButton && blockOwner.matches(playerName)) {
			publicAccess ^= true;
		}
	}

	public void fillOutputSlot() {
		if (inventory[itemOutputSlot] == null && coinSum > 0) {
			if (coinSum > UniversalCoins.coinValues[4]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.obsidian_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[4], 64);
				coinSum -= UniversalCoins.coinValues[4] * inventory[itemOutputSlot].stackSize;
			} else if (coinSum > UniversalCoins.coinValues[3]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.diamond_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[3], 64);
				coinSum -= UniversalCoins.coinValues[3] * inventory[itemOutputSlot].stackSize;
			} else if (coinSum > UniversalCoins.coinValues[2]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.emerald_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[2], 64);
				coinSum -= UniversalCoins.coinValues[2] * inventory[itemOutputSlot].stackSize;
			} else if (coinSum > UniversalCoins.coinValues[1]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.gold_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[1], 64);
				coinSum -= UniversalCoins.coinValues[1] * inventory[itemOutputSlot].stackSize;
			} else if (coinSum > UniversalCoins.coinValues[0]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.iron_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[0], 64);
				coinSum -= UniversalCoins.coinValues[0] * inventory[itemOutputSlot].stackSize;
			}
		}
		if (coinSum <= 0) {
			coinSum = 0;
		}
	}

}
