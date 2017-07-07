package universalcoins.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.UniversalCoins;
import universalcoins.util.CoinUtils;
import universalcoins.util.UniversalAccounts;

public class TileSafe extends TileProtected implements IInventory, ISidedInventory {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(2, ItemStack.EMPTY);
	public static final int itemInputSlot = 0;
	public static final int itemOutputSlot = 1;
	public String blockOwner = "nobody";
	public String accountNumber = "0";
	public long accountBalance = 0;

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] { 0 };
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStack, EnumFacing direction) {
		if (index == 0) {
			Item itemInStack = itemStack.getItem();
			return (itemInStack == UniversalCoins.Items.iron_coin || itemInStack == UniversalCoins.Items.emerald_coin
					|| itemInStack == UniversalCoins.Items.gold_coin || itemInStack == UniversalCoins.Items.diamond_coin
					|| itemInStack == UniversalCoins.Items.obsidian_coin);
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot >= inventory.size()) {
			return ItemStack.EMPTY;
		}
		return inventory.get(slot);
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
		coinsTaken(stack);
		return stack;
	}

	public void coinsTaken(ItemStack stack) {
		int coinValue = 0;
		coinValue = CoinUtils.getCoinValue(stack);
		int debitAmount = 0;
		int accountCapacity = (int) (Long.MAX_VALUE - accountBalance > Integer.MAX_VALUE ? Integer.MAX_VALUE
				: Long.MAX_VALUE - accountBalance);
		debitAmount = Math.min(stack.getCount(), accountCapacity / coinValue);
		if (!world.isRemote) {
			UniversalAccounts.getInstance().debitAccount(accountNumber, debitAmount * coinValue, false);
			updateAccountBalance();
		}
		fillOutputSlot();
	}

	// @Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return getStackInSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		int coinValue = 0;
		if (slot == itemInputSlot) {
			switch (stack.getUnlocalizedName()) {
			case "item.universalcoins.iron_coin":
				coinValue = UniversalCoins.coinValues[0];
				break;
			case "item.universalcoins.gold_coin":
				coinValue = UniversalCoins.coinValues[1];
				break;
			case "item.universalcoins.emerald_coin":
				coinValue = UniversalCoins.coinValues[2];
				break;
			case "item.universalcoins.diamond_coin":
				coinValue = UniversalCoins.coinValues[3];
				break;
			case "item.universalcoins.obsidian_coin":
				coinValue = UniversalCoins.coinValues[4];
				break;
			}
			if (coinValue > 0) {
				int accountCapacity = (int) (Long.MAX_VALUE - accountBalance > Integer.MAX_VALUE ? Integer.MAX_VALUE
						: Long.MAX_VALUE - accountBalance);
				int depositAmount = Math.min(accountCapacity / coinValue, stack.getCount());
				if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
					// Only deposit on server side, otherwise we deposit
					// twice.
					UniversalAccounts.getInstance().creditAccount(accountNumber, depositAmount * coinValue, false);
				}
				inventory.get(slot).shrink(depositAmount);
				if (inventory.get(slot).getCount() == 0) {
					inventory.set(slot, ItemStack.EMPTY);
				}
				fillOutputSlot();
				if (!world.isRemote)
					updateAccountBalance();
			}
		}
	}

	public void fillOutputSlot() {
		if (accountBalance > UniversalCoins.coinValues[4]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.obsidian_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(accountBalance / UniversalCoins.coinValues[4], 64));
		} else if (accountBalance > UniversalCoins.coinValues[3]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.diamond_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(accountBalance / UniversalCoins.coinValues[3], 64));
		} else if (accountBalance > UniversalCoins.coinValues[2]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.emerald_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(accountBalance / UniversalCoins.coinValues[2], 64));
		} else if (accountBalance > UniversalCoins.coinValues[1]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.gold_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(accountBalance / UniversalCoins.coinValues[1], 64));
		} else if (accountBalance > UniversalCoins.coinValues[0]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.iron_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(accountBalance / UniversalCoins.coinValues[0], 64));
		}
	}

	public void updateAccountBalance() {
		accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.Blocks.safe.getLocalizedName());
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
	public boolean isUsableByPlayer(EntityPlayer player) {
		return world.getTileEntity(pos) == this
				&& player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		// we only have a coin input slot
		return true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.get(i);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setByte("Slot", (byte) i);
			stack.writeToNBT(tag);
			itemList.appendTag(tag);
		}
		tagCompound.setTag("Inventory", itemList);
		tagCompound.setString("Owner", blockOwner);
		tagCompound.setString("AccountNumber", accountNumber);
		tagCompound.setLong("Balance", accountBalance);

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
			blockOwner = tagCompound.getString("Owner");
		} catch (Throwable ex2) {
			blockOwner = "nobody";
		}
		try {
			accountNumber = tagCompound.getString("AccountNumber");
		} catch (Throwable ex2) {
			accountNumber = "0";
		}
		try {
			accountBalance = tagCompound.getLong("Balance");
		} catch (Throwable ex2) {
			accountBalance = 0;
		}
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
		if (accountBalance == 0)
			inventory.set(itemOutputSlot, ItemStack.EMPTY);
	}

	public void updateTE() {
		fillOutputSlot();
		final IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 3);
	}

	public void setSafeAccount(String playerName) {
		accountNumber = UniversalAccounts.getInstance().getOrCreatePlayerAccount(getPlayerUID(playerName));
	}

	private String getPlayerUID(String playerName) {
		EntityPlayer player = world.getPlayerEntityByName(playerName);
		return player.getUniqueID().toString();
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
	public String getName() {
		return UniversalCoins.Blocks.safe.getLocalizedName();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
}
