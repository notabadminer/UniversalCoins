package universalcoins.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import universalcoins.UniversalCoins;
import universalcoins.gui.PowerReceiverGUI;
import universalcoins.util.CoinUtils;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerReceiver extends TileProtected implements ITickable, IInventory, IEnergyStorage {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
	public static final int itemCardSlot = 0;
	public static final int itemCoinSlot = 1;
	public static final int itemOutputSlot = 2;
	public long coinSum = 0;
	public int feLevel = 0;
	public int feOutput = 0;
	public long wfeLevel = 0;
	public String blockOwner = "nobody";
	public EnumFacing orientation = null;
	public boolean publicAccess;

	@CapabilityInject(IEnergyStorage.class)
	public static Capability<IEnergyStorage> CAPABILITY_FORGE_ENERGYSTORAGE = null;

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
	public ItemStack getStackInSlot(int index) {
		return inventory.get(index);
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != ItemStack.EMPTY) {
			if (stack.getCount() <= size) {
				setInventorySlotContents(slot, ItemStack.EMPTY);
			} else {
				stack = stack.splitStack(size);
				if (stack.getCount() == 0) {
					setInventorySlotContents(slot, ItemStack.EMPTY);
				}
			}
		}
		return stack;
	}

	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (slot == itemCoinSlot) {
			int coinValue = 0;
			coinValue = CoinUtils.getCoinValue(stack);
			if (coinValue > 0) {
				int depositAmount = (int) Math.min(stack.getCount(), (Long.MAX_VALUE - coinSum) / coinValue);
				getStackInSlot(slot).shrink(depositAmount);
				coinSum += depositAmount * coinValue;
				if (getStackInSlot(slot).getCount() == 0) {
					inventory.set(slot, ItemStack.EMPTY);
				}
			}
		}
		if (slot == itemCardSlot && getStackInSlot(itemCardSlot).getItem() == UniversalCoins.Items.ender_card
				&& !world.isRemote && stack.hasTagCompound()) {
			String accountNumber = stack.getTagCompound().getString("Account");
			if (UniversalAccounts.getInstance().creditAccount(accountNumber, coinSum, false))
				coinSum = 0;
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
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	private long getAccountBalance() {
		if (world.isRemote || getStackInSlot(itemCardSlot) == null || !getStackInSlot(itemCardSlot).hasTagCompound()) {
			return 0;
		}
		String accountNumber = getStackInSlot(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return 0;
		}
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private boolean creditAccount(int i) {
		if (world.isRemote || getStackInSlot(itemCardSlot) == null
				|| getStackInSlot(itemCardSlot).getItem() != UniversalCoins.Items.ender_card
				|| !getStackInSlot(itemCardSlot).hasTagCompound())
			return false;
		String accountNumber = getStackInSlot(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, i, false);
	}

	private boolean debitAccount(int i) {
		if (world.isRemote || getStackInSlot(itemCardSlot) == null || !getStackInSlot(itemCardSlot).hasTagCompound())
			return false;
		String accountNumber = getStackInSlot(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().debitAccount(accountNumber, i, false);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		ItemStackHelper.saveAllItems(tagCompound, this.inventory);
		tagCompound.setLong("coinSum", coinSum);
		tagCompound.setInteger("feLevel", feLevel);
		tagCompound.setInteger("feOutput", feOutput);
		tagCompound.setLong("wfeLevel", wfeLevel);
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
		this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(tagCompound, this.inventory);
		try {
			coinSum = tagCompound.getLong("coinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			feLevel = tagCompound.getInteger("feLevel");
		} catch (Throwable ex2) {
			feLevel = 0;
		}
		try {
			feOutput = tagCompound.getInteger("feOutput");
		} catch (Throwable ex2) {
			feOutput = 0;
		}
		try {
			wfeLevel = tagCompound.getLong("wfeLevel");
		} catch (Throwable ex2) {
			wfeLevel = 0;
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

	private void fillOutputSlot() {
		if (getStackInSlot(itemOutputSlot).isEmpty()) {
			if (coinSum >= UniversalCoins.coinValues[4]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.obsidian_coin));
				getStackInSlot(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[4], 64));
				coinSum -= UniversalCoins.coinValues[4] * getStackInSlot(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[3]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.diamond_coin));
				getStackInSlot(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[3], 64));
				coinSum -= UniversalCoins.coinValues[3] * getStackInSlot(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[2]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.emerald_coin));
				getStackInSlot(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[2], 64));
				coinSum -= UniversalCoins.coinValues[2] * getStackInSlot(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[1]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.gold_coin));
				getStackInSlot(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[1], 64));
				coinSum -= UniversalCoins.coinValues[1] * getStackInSlot(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[0]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.iron_coin));
				getStackInSlot(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[0], 64));
				coinSum -= UniversalCoins.coinValues[0] * getStackInSlot(itemOutputSlot).getCount();
			}
		}
	}

	protected void buyPower() {
		if (feLevel == 0 && UniversalPower.getInstance().extractEnergy(10, true) > 0
				&& debitAccount(UniversalCoins.rfRetailRate)) {
			UniversalPower.getInstance().extractEnergy(10, false);
			feLevel += 10000;
		} else if (feLevel == 0 && UniversalPower.getInstance().extractEnergy(10, true) > 0
				&& coinSum - UniversalCoins.rfRetailRate >= 0) {
			coinSum -= UniversalCoins.rfRetailRate;
			UniversalPower.getInstance().extractEnergy(10, false);
			feLevel += 10000;
		}
		wfeLevel = UniversalPower.getInstance().getFeLevel();
	}

	protected void sendPower() {
		if (orientation == null) {
			resetPowerDirection();
			return;
		}
		feOutput = 0;
		TileEntity tile = world.getTileEntity(new BlockPos(pos.getX() + orientation.getFrontOffsetX(),
				pos.getY() + orientation.getFrontOffsetY(), pos.getZ() + orientation.getFrontOffsetZ()));
		if (tile != null) {
			IEnergyStorage energyStorage = tile.getCapability(CapabilityEnergy.ENERGY, orientation);
			if (energyStorage != null) {
				int maxFE = energyStorage.receiveEnergy(Math.min(1000, feLevel), true);
				feOutput = maxFE;
				feLevel -= energyStorage.receiveEnergy(maxFE, false);
			} else {
				orientation = null;
			}
		} else {
			orientation = null;
		}
	}

	protected void resetPowerDirection() {
		for (EnumFacing direction : EnumFacing.VALUES) {
			TileEntity tile = world.getTileEntity((new BlockPos(pos.getX() + direction.getFrontOffsetX(),
					pos.getY() + direction.getFrontOffsetY(), pos.getZ() + direction.getFrontOffsetZ())));
			if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, direction)) {
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
		return getStackInSlot(index);
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (!simulate) {
			feLevel -= maxExtract;
			if (coinSum - UniversalCoins.rfRetailRate >= 0) {
				coinSum -= UniversalCoins.rfRetailRate;
			}
		}
		return Math.min(feLevel, 1000);
	}

	@Override
	public int getEnergyStored() {
		return feLevel;
	}

	@Override
	public int getMaxEnergyStored() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean canExtract() {
		return feLevel > 0;
	}

	@Override
	public boolean canReceive() {
		return false;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return (T) this;
		}
		return super.getCapability(capability, facing);
	}

	public boolean isUsableByPlayer(EntityPlayer player) {
		if (this.world.getTileEntity(this.pos) != this || player == null) {
			return false;
		} else {
			return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
					(double) this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}
}
