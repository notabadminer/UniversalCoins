package universalcoins.tileentity;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerTransmitter extends TileEntity implements IInventory, IEnergyReceiver {

	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemCardSlot = 0;
	public static final int itemOutputSlot = 1;
	public int coinSum = 0;
	public int rfLevel = 0;
	public int krfSold = 0;
	public String blockOwner = "nobody";

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
			if (slot == itemCardSlot && inventory[itemCardSlot].getItem() == UniversalCoins.proxy.ender_card) {
				if (creditAccount(coinSum)) {
					coinSum = 0;
				}
			}
		}
	}

	@Override
	public String getName() {
		return UniversalCoins.proxy.power_transmitter.getLocalizedName();
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
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		if (!simulate) {
			rfLevel += maxReceive;
			if (rfLevel >= 10000) {
				rfLevel -= 10000;
				boolean playerCredited = false;
				if (inventory[itemCardSlot] != null) {
					playerCredited = creditAccount(UniversalCoins.rfWholesaleRate);
					if (playerCredited) {
						krfSold += 10;
						UniversalPower.getInstance().receiveEnergy(10, false);
					}
				}
				if (!playerCredited && coinSum + UniversalCoins.rfWholesaleRate < Integer.MAX_VALUE) {
					coinSum += UniversalCoins.rfWholesaleRate;
					krfSold += 10;
					UniversalPower.getInstance().receiveEnergy(10, false);
				}
			}
		}

		return maxReceive;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return rfLevel;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return Integer.MAX_VALUE;
	}

	private long getAccountBalance() {
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
				|| inventory[itemCardSlot].getItem() != UniversalCoins.proxy.ender_card
				|| !inventory[itemCardSlot].hasTagCompound())
			return false;
		String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, i);
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
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

	public void updateTE() {
		markDirty();
		worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
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
		tagCompound.setInteger("coinSum", coinSum);
		tagCompound.setInteger("rfLevel", rfLevel);
		tagCompound.setInteger("krfSold", krfSold);
		tagCompound.setString("blockOwner", blockOwner);
		return tagCompound;
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
			krfSold = tagCompound.getInteger("krfSold");
		} catch (Throwable ex2) {
			krfSold = 0;
		}
		try {
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "nobody";
		}
	}

	public void onButtonPressed(int buttonId) {
		if (buttonId == 0) {
			fillOutputSlot();
		}
	}

	public void fillOutputSlot() {
		inventory[itemOutputSlot] = null;
		if (coinSum > UniversalCoins.coinValues[4]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.obsidian_coin);
			 int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[4], 64);
			 inventory[itemOutputSlot].stackSize = amount;
			 coinSum -= amount * UniversalCoins.coinValues[4];
		} else if (coinSum > UniversalCoins.coinValues[3]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.diamond_coin);
			int amount  = (int) Math.min(coinSum / UniversalCoins.coinValues[3], 64);
			inventory[itemOutputSlot].stackSize = amount;
			 coinSum -= amount * UniversalCoins.coinValues[3];
		} else if (coinSum > UniversalCoins.coinValues[2]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.emerald_coin);
			int amount  = (int) Math.min(coinSum / UniversalCoins.coinValues[2], 64);
			inventory[itemOutputSlot].stackSize = amount;
			 coinSum -= amount * UniversalCoins.coinValues[2];
		} else if (coinSum > UniversalCoins.coinValues[1]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.gold_coin);
			int amount  = (int) Math.min(coinSum / UniversalCoins.coinValues[1], 64);
			inventory[itemOutputSlot].stackSize = amount;
			 coinSum -= amount * UniversalCoins.coinValues[1];
		} else if (coinSum > UniversalCoins.coinValues[0]) {
			inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.iron_coin);
			int amount  = (int) Math.min(coinSum / UniversalCoins.coinValues[0], 64);
			inventory[itemOutputSlot].stackSize = amount;
			 coinSum -= amount * UniversalCoins.coinValues[0];
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.proxy.power_transmitter.getLocalizedName());
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
