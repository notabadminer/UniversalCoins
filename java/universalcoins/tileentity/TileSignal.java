package universalcoins.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.blocks.BlockSignal;
import universalcoins.net.UCButtonMessage;

public class TileSignal extends TileEntity implements IInventory, ITickable {

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
	public void update() {
		if (!worldObj.isRemote) {
			if (counter > 0) {
				counter--;
				FMLLog.info("counter: " + counter);
				secondsLeft = counter / 20;
				if (secondsLeft != lastSecondsLeft) {
					lastSecondsLeft = secondsLeft;
					updateTE();
				}
			} else {
				canProvidePower = false;
				updateTE();
				updateNeighbors();
			}
		}
	}

	public void onButtonPressed(int buttonId, boolean shift) {
		if (buttonId == 0) {
			fillOutputSlot();
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
		updateTE();
	}

	public void activateSignal() {
		canProvidePower = true;
		counter += duration * 20;
		coinSum += fee;
		updateNeighbors();
	}

	private void updateNeighbors() {
		this.blockType = this.getBlockType();
		if (this.blockType instanceof BlockSignal) {
			((BlockSignal) blockType).updatePower(worldObj, pos);
		}
	}

	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	public String getName() {
		return this.hasCustomName() ? this.customName : UniversalCoins.proxy.signalblock.getLocalizedName();
	}

	public void setName(String name) {
		customName = name;
	}

	public boolean isInventoryNameLocalized() {
		return false;
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
	}

	@Override
	public Packet getDescriptionPacket() {
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

	public void fillOutputSlot() { //TODO fix this
//		inventory[itemOutputSlot] = null;
//		if (coinSum > 0) {
//			// use logarithm to find largest cointype for the balance
//			int logVal = Math.min((int) (Math.log(coinSum) / Math.log(9)), 4);
//			int stackSize = Math.min((int) (coinSum / Math.pow(9, logVal)), 64);
//			// add a stack to the slot
//			inventory[itemOutputSlot] = new ItemStack(coins[logVal], stackSize);
//			int itemValue = multiplier[logVal];
//			int debitAmount = 0;
//			debitAmount = Math.min(stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
//			if (!worldObj.isRemote) {
//				coinSum -= debitAmount * itemValue;
//			}
//		}
	}

	// @Override
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
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
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
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.proxy.signalblock.getLocalizedName());
	}
}
