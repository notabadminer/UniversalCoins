package universalcoins.tileentity;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.gui.PowerTransmitterGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerTransmitter extends TileProtected implements IInventory, IEnergyReceiver {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(2, ItemStack.EMPTY);
	public static final int itemCardSlot = 0;
	public static final int itemOutputSlot = 1;
	public long coinSum = 0;
	public int rfLevel = 0;
	public int rfOutput = 0;
	public int krfSold = 0;
	public String blockOwner = "nobody";
	public boolean publicAccess;

	@Override
	public int getSizeInventory() {
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot >= inventory.size()) {
			return null;
		}
		return inventory.get(slot);
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
		return stack;
	}

	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (stack != null) {
			if (slot == itemCardSlot && inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card) {
				if (creditAccount(coinSum)) {
					coinSum = 0;
				}
			}
		}
	}

	@Override
	public String getName() {
		return UniversalCoins.Blocks.power_transmitter.getLocalizedName();
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
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return world.getTileEntity(pos) == this
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
			int rfChunks = 0;
			rfLevel += maxReceive;
			if (rfLevel >= 10000) {
				// calculate how many 10k chunks we can sell
				rfChunks = (int) Math.floor(rfLevel / 10000);
				boolean playerCredited = false;
				if (creditAccount(UniversalCoins.rfWholesaleRate * rfChunks)) {
					krfSold += Math.min(10 * rfChunks, Integer.MAX_VALUE - krfSold);
					UniversalPower.getInstance().receiveEnergy(10 * rfChunks, false);
					rfLevel -= 10000 * rfChunks;
				} else if (coinSum + UniversalCoins.rfWholesaleRate * rfChunks <= Integer.MAX_VALUE) {
					coinSum += UniversalCoins.rfWholesaleRate * rfChunks;
					krfSold += Math.min(10 * rfChunks, Integer.MAX_VALUE - krfSold);
					UniversalPower.getInstance().receiveEnergy(10 * rfChunks, false);
					rfLevel -= 10000 * rfChunks;
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
		if (world.isRemote || inventory.get(itemCardSlot) == null || !inventory.get(itemCardSlot).hasTagCompound()) {
			return 0;
		}
		String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return 0;
		}
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private boolean creditAccount(long amount) {
		if (world.isRemote || inventory.get(itemCardSlot) == null
				|| inventory.get(itemCardSlot).getItem() != UniversalCoins.Items.ender_card
				|| !inventory.get(itemCardSlot).hasTagCompound())
			return false;
		String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, amount, false);
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
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

	public void updateTE() {
		final IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 3);
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
		tagCompound.setLong("coinSum", coinSum);
		tagCompound.setInteger("rfLevel", rfLevel);
		tagCompound.setInteger("krfSold", krfSold);
		tagCompound.setString("blockOwner", blockOwner);
		tagCompound.setBoolean("publicAccess", publicAccess);
		return tagCompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.size()) {
				inventory.set(slot, new ItemStack(tag));
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

	public void onButtonPressed(int buttonId, boolean shift) {
		if (buttonId == PowerTransmitterGUI.idCoinButton) {
			fillOutputSlot();
		}
		if (buttonId == PowerTransmitterGUI.idAccessModeButton && blockOwner.matches(blockOwner)) {
			publicAccess ^= true;
		}
	}

	public void fillOutputSlot() {
		if (inventory.get(itemOutputSlot) == null && coinSum > 0) {
			if (coinSum > UniversalCoins.coinValues[4]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.obsidian_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[4], 64));
				coinSum -= UniversalCoins.coinValues[4] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum > UniversalCoins.coinValues[3]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.diamond_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[3], 64));
				coinSum -= UniversalCoins.coinValues[3] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum > UniversalCoins.coinValues[2]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.emerald_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[2], 64));
				coinSum -= UniversalCoins.coinValues[2] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum > UniversalCoins.coinValues[1]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.gold_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[1], 64));
				coinSum -= UniversalCoins.coinValues[1] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum > UniversalCoins.coinValues[0]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.iron_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[0], 64));
				coinSum -= UniversalCoins.coinValues[0] * inventory.get(itemOutputSlot).getCount();
			}
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.Blocks.power_transmitter.getLocalizedName());
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inventory.get(index);
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
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

}
