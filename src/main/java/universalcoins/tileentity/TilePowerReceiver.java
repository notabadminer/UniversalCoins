package universalcoins.tileentity;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.gui.PowerReceiverGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.CoinUtils;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerReceiver extends TileProtected implements ITickable, IInventory, IEnergyProvider {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(3, ItemStack.EMPTY);
	public static final int itemCardSlot = 0;
	public static final int itemCoinSlot = 1;
	public static final int itemOutputSlot = 2;
	public long coinSum = 0;
	public int rfLevel = 0;
	public int rfOutput = 0;
	public long wrfLevel = 0;
	public String blockOwner = "nobody";
	public EnumFacing orientation = null;
	public boolean publicAccess;

	@Override
	public void update() {
		if (!world.isRemote) {
			buyPower();
			sendPower();
		}
	}

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
		int coinValue = 0;
		coinValue = CoinUtils.getCoinValue(stack);
		if (coinValue > 0) {
			int depositAmount = (int) Math.min(stack.getCount(), (Long.MAX_VALUE - coinSum) / coinValue);
			inventory.get(slot).shrink(depositAmount);
			coinSum += depositAmount * coinValue;
			if (inventory.get(slot).getCount() == 0) {
				inventory.set(slot, ItemStack.EMPTY);
			}
		}
		if (slot == itemCardSlot && inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card) {
			if (!world.isRemote && stack.hasTagCompound()) {
				String accountNumber = stack.getTagCompound().getString("Account");
				UniversalAccounts.getInstance().creditAccount(accountNumber, coinSum, false);
				coinSum = 0;
			}
		}
	}

	@Override
	public String getName() {
		return UniversalCoins.Blocks.power_receiver.getLocalizedName();
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

	private boolean creditAccount(int i) {
		if (world.isRemote || inventory.get(itemCardSlot) == null
				|| inventory.get(itemCardSlot).getItem() != UniversalCoins.Items.ender_card
				|| !inventory.get(itemCardSlot).hasTagCompound())
			return false;
		String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, i, false);
	}

	private boolean debitAccount(int i) {
		if (world.isRemote || inventory.get(itemCardSlot) == null || !inventory.get(itemCardSlot).hasTagCompound())
			return false;
		String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().debitAccount(accountNumber, i, false);
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
		tagCompound.setInteger("rfOutput", rfOutput);
		tagCompound.setLong("wrfLevel", wrfLevel);
		tagCompound.setString("blockOwner", blockOwner);
		if (orientation != null) {
			tagCompound.setInteger("orientation", orientation.ordinal());
		}
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
			orientation = orientation.getFront(tagCompound.getInteger("orientation"));
		} catch (Throwable ex2) {
			orientation = null;
		}
		try {
			publicAccess = tagCompound.getBoolean("publicAccess");
		} catch (Throwable ex2) {
			publicAccess = false;
		}
	}

	public void onButtonPressed(int buttonId, boolean shift) {
		if (buttonId == PowerReceiverGUI.idCoinButton) {
			fillOutputSlot();
		}
		if (buttonId == PowerReceiverGUI.idAccessModeButton && blockOwner.matches(blockOwner)) {
			publicAccess ^= true;
		}
	}

	public void fillOutputSlot() {
		if (inventory.get(itemOutputSlot) == null && coinSum > 0) {
			if (coinSum > UniversalCoins.coinValues[4]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.obsidian_coin));
				int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[4], 64);
				inventory.get(itemOutputSlot).setCount(amount);
				coinSum -= amount * UniversalCoins.coinValues[4];
			} else if (coinSum > UniversalCoins.coinValues[3]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.diamond_coin));
				int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[3], 64);
				inventory.get(itemOutputSlot).setCount(amount);
				coinSum -= amount * UniversalCoins.coinValues[3];
			} else if (coinSum > UniversalCoins.coinValues[2]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.emerald_coin));
				int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[2], 64);
				inventory.get(itemOutputSlot).setCount(amount);
				coinSum -= amount * UniversalCoins.coinValues[2];
			} else if (coinSum > UniversalCoins.coinValues[1]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.gold_coin));
				int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[1], 64);
				inventory.get(itemOutputSlot).setCount(amount);
				coinSum -= amount * UniversalCoins.coinValues[1];
			} else if (coinSum > UniversalCoins.coinValues[0]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.iron_coin));
				int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[0], 64);
				inventory.get(itemOutputSlot).setCount(amount);
				coinSum -= amount * UniversalCoins.coinValues[0];
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
		TileEntity tile = world.getTileEntity(new BlockPos(pos.getX() + orientation.getFrontOffsetX(),
				pos.getY() + orientation.getFrontOffsetX(), pos.getZ() + orientation.getFrontOffsetZ()));
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
			TileEntity tile = world.getTileEntity((new BlockPos(pos.getX() + direction.getFrontOffsetX(),
					pos.getY() + direction.getFrontOffsetX(), pos.getZ() + direction.getFrontOffsetZ())));
			if (tile instanceof IEnergyReceiver) {
				orientation = direction;
			}
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.Blocks.power_receiver.getLocalizedName());
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
