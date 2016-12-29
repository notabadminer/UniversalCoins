package universalcoins.tile;

import net.minecraft.block.Block;
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
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;

public class TileUCSignal extends TileEntity implements IInventory {

	private ItemStack[] inventory = new ItemStack[1];
	public static final int itemOutputSlot = 0;
	public String blockOwner = "";
	public int coinSum = 0;
	public int fee = 1;
	public int duration = 1;
	public int counter = 0;
	public int secondsLeft = 0;
	public int lastSecondsLeft = 0;
	public String customName = "";
	public boolean canProvidePower = false;

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!worldObj.isRemote) {
			if (counter >= 0) {
				counter--;
				secondsLeft = counter / 20;
				if (secondsLeft != lastSecondsLeft) {
					lastSecondsLeft = secondsLeft;
					updateTE();
				}
				if (counter == 0) {
					canProvidePower = false;
					updateNeighbors();
				}
			}
		}
	}

	public void onButtonPressed(int buttonId, boolean shift) {
		if (buttonId == 0) {
			fillOutputSlot();
			updateTE();
		}
		if (buttonId == 1) {
			if (shift) {
				if (duration - 10 > 0) {
					duration -= 10;
				}
			} else {
				if (duration - 1 > 0) {
					duration--;
				}
			}
		}
		if (buttonId == 2) {
			if (shift) {
				if (duration + 10 < Integer.MAX_VALUE) {
					duration += 10;
				}
			} else {
				if (duration + 1 < Integer.MAX_VALUE) {
					duration++;
				}
			}
		}
		if (buttonId == 3) {
			if (shift) {
				if (fee - 10 > 0) {
					fee -= 10;
				}
			} else {
				if (fee - 1 > 0) {
					fee--;
				}
			}
		}
		if (buttonId == 4) {
			if (shift) {
				if (fee + 10 < Integer.MAX_VALUE) {
					fee += 10;
				}
			} else {
				if (fee + 1 < Integer.MAX_VALUE) {
					fee++;
				}
			}
		}
	}

	public void activateSignal() {
		canProvidePower = true;
		counter = Math.min(counter + duration * 20, Integer.MAX_VALUE);
		coinSum = Math.min(coinSum + fee, Integer.MAX_VALUE);
		updateNeighbors();
	}

	private void updateNeighbors() {
		Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, block);
	}

	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : UniversalCoins.proxy.signal_block.getLocalizedName();
	}

	public void setInventoryName(String name) {
		customName = name;
	}

	public boolean isInventoryNameLocalized() {
		return false;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
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
		tagCompound.setString("blockOwner", blockOwner);
		tagCompound.setInteger("coinSum", coinSum);
		tagCompound.setInteger("fee", fee);
		tagCompound.setInteger("duration", duration);
		tagCompound.setInteger("secondsLeft", secondsLeft);
		tagCompound.setString("customName", customName);
		tagCompound.setBoolean("canProvidePower", canProvidePower);
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
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "";
		}
		try {
			coinSum = tagCompound.getInteger("coinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			fee = tagCompound.getInteger("fee");
		} catch (Throwable ex2) {
			fee = 1;
		}
		try {
			duration = tagCompound.getInteger("duration");
		} catch (Throwable ex2) {
			duration = 1;
		}
		try {
			secondsLeft = tagCompound.getInteger("secondsLeft");
		} catch (Throwable ex2) {
			secondsLeft = 0;
		}
		try {
			customName = tagCompound.getString("customName");
		} catch (Throwable ex2) {
			customName = "";
		}
		try {
			canProvidePower = tagCompound.getBoolean("canProvidePower");
		} catch (Throwable ex2) {
			canProvidePower = false;
		}
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i >= inventory.length) {
			return null;
		}
		return inventory[i];
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
	public ItemStack getStackInSlotOnClosing(int i) {
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
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
}
