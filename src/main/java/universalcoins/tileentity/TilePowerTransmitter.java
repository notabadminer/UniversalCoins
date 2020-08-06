package universalcoins.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import universalcoins.UniversalCoins;
import universalcoins.gui.PowerTransmitterGUI;
import universalcoins.util.UniversalAccounts;
import universalcoins.util.UniversalPower;

public class TilePowerTransmitter extends TileProtected implements IInventory, IEnergyStorage {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(2, ItemStack.EMPTY);
	public static final int itemCardSlot = 0;
	public static final int itemOutputSlot = 1;
	public long coinSum = 0;
	public int feLevel = 0;
	public int feOutput = 0;
	public int kfeSold = 0;
	public String blockOwner = "nobody";
	public boolean publicAccess;

	@CapabilityInject(IEnergyStorage.class)
	public static Capability<IEnergyStorage> CAPABILITY_FORGE_ENERGYSTORAGE = null;

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
		if (slot == itemCardSlot && inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card) {
			if (creditAccount(coinSum)) {
				coinSum = 0;
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
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@SuppressWarnings("unused")
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
		if (world.isRemote || inventory.get(itemCardSlot) == ItemStack.EMPTY
				|| inventory.get(itemCardSlot).getItem() != UniversalCoins.Items.ender_card
				|| !inventory.get(itemCardSlot).hasTagCompound())
			return false;
		String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, amount, false);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		ItemStackHelper.saveAllItems(tagCompound, this.inventory);
		tagCompound.setLong("coinSum", coinSum);
		tagCompound.setInteger("feLevel", feLevel);
		tagCompound.setInteger("kfeSold", kfeSold);
		tagCompound.setString("blockOwner", blockOwner);
		tagCompound.setBoolean("publicAccess", publicAccess);
		return tagCompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.inventory = NonNullList.<ItemStack> withSize(this.getSizeInventory(), ItemStack.EMPTY);
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
			kfeSold = tagCompound.getInteger("kfeSold");
		} catch (Throwable ex2) {
			kfeSold = 0;
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

	private void fillOutputSlot() {
		if (inventory.get(itemOutputSlot).isEmpty()) {
			if (coinSum >= UniversalCoins.coinValues[4]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.obsidian_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[4], 64));
				coinSum -= UniversalCoins.coinValues[4] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[3]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.diamond_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[3], 64));
				coinSum -= UniversalCoins.coinValues[3] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[2]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.emerald_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[2], 64));
				coinSum -= UniversalCoins.coinValues[2] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[1]) {
				inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.gold_coin));
				inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[1], 64));
				coinSum -= UniversalCoins.coinValues[1] * inventory.get(itemOutputSlot).getCount();
			} else if (coinSum >= UniversalCoins.coinValues[0]) {
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
		if (!simulate) {
			int rfChunks = 0;
			feLevel += maxReceive;
			if (feLevel >= 10000) {
				// calculate how many 10k chunks we can sell
				rfChunks = (int) Math.floor(feLevel / 10000);
				if (creditAccount(UniversalCoins.rfWholesaleRate * rfChunks)) {
					kfeSold += Math.min(10 * rfChunks, Integer.MAX_VALUE - kfeSold);
					UniversalPower.getInstance().receiveEnergy(10 * rfChunks, false);
					feLevel -= 10000 * rfChunks;
				} else if (coinSum + UniversalCoins.rfWholesaleRate * rfChunks <= Integer.MAX_VALUE) {
					coinSum += UniversalCoins.rfWholesaleRate * rfChunks;
					kfeSold += Math.min(10 * rfChunks, Integer.MAX_VALUE - kfeSold);
					UniversalPower.getInstance().receiveEnergy(10 * rfChunks, false);
					feLevel -= 10000 * rfChunks;
				}
			}
		}

		return maxReceive;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
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
		return false;
	}

	@Override
	public boolean canReceive() {
		return feLevel < Integer.MAX_VALUE;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
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
