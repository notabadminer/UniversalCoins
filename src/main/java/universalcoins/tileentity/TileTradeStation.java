package universalcoins.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
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
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.gui.TradeStationGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.CoinUtils;
import universalcoins.util.UCItemPricer;
import universalcoins.util.UniversalAccounts;

public class TileTradeStation extends TileProtected implements IInventory, ISidedInventory {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(4, ItemStack.EMPTY);
	public static final int itemInputSlot = 0;
	public static final int itemOutputSlot = 1;
	public static final int itemCardSlot = 2;
	public static final int itemCoinSlot = 3;
	public int coinSum = 0;
	public int itemPrice = 0;
	public boolean buyButtonActive = false;
	public boolean sellButtonActive = false;
	public boolean ironCoinBtnActive = false;
	public boolean goldCoinBtnActive = false;
	public boolean emeraldCoinBtnActive = false;
	public boolean diamondCoinBtnActive = false;
	public boolean obsidianCoinBtnActive = false;
	public boolean autoModeButtonActive = UniversalCoins.autoModeEnabled;
	public boolean publicAccess = true;

	public int autoMode = 0;
	private int autoDelayCounter = 0;
	public int coinMode = 0;

	private ItemStack previousStack = ItemStack.EMPTY;

	public TileTradeStation() {
		super();
	}

	public void update() {
		activateBuySellButtons();
		activateRetrieveButtons();

		if (!world.isRemote) {
			// runAutoMode();
			runCoinMode();
		}
	}

	public void inUseCleanup() {
		inUse = false;
	}

	private void activateBuySellButtons() {
		if (getStackInSlot(itemInputSlot) == ItemStack.EMPTY) {
			itemPrice = 0;
			buyButtonActive = false;
			sellButtonActive = false;
		} else {
			itemPrice = UCItemPricer.getInstance().getItemPrice(getStackInSlot(itemInputSlot));
			if (itemPrice <= -1 || itemPrice == 0) {
				itemPrice = 0;
				buyButtonActive = false;
				sellButtonActive = false;
			} else {
				sellButtonActive = true;
				// disable sell button if coinSum is near max
				if (Integer.MAX_VALUE - coinSum < itemPrice) {
					sellButtonActive = false;
				}
				buyButtonActive = (UniversalCoins.tradeStationBuyEnabled
						&& (inventory.get(itemOutputSlot).isEmpty()
								|| (inventory.get(itemOutputSlot)).getItem() == inventory.get(itemInputSlot).getItem()
										&& inventory.get(itemOutputSlot).getCount() < inventory.get(itemInputSlot)
												.getMaxStackSize()
										&& !UCItemPricer.getInstance().hasEnchantment(inventory.get(itemOutputSlot)))
						&& (coinSum >= itemPrice || (!inventory.get(itemCardSlot).isEmpty() && !world.isRemote
								&& getAccountBalance() >= itemPrice)));
			}
		}
	}

	private void activateRetrieveButtons() {
		ironCoinBtnActive = false;
		goldCoinBtnActive = false;
		emeraldCoinBtnActive = false;
		diamondCoinBtnActive = false;
		obsidianCoinBtnActive = false;
		if (coinSum > 0) {
			ironCoinBtnActive = inventory.get(itemOutputSlot).isEmpty()
					|| (inventory.get(itemOutputSlot).getItem() == UniversalCoins.Items.iron_coin
							&& inventory.get(itemOutputSlot).getCount() != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[1]) {
			goldCoinBtnActive = inventory.get(itemOutputSlot).isEmpty()
					|| (inventory.get(itemOutputSlot).getItem() == UniversalCoins.Items.gold_coin
							&& inventory.get(itemOutputSlot).getCount() != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[2]) {
			emeraldCoinBtnActive = inventory.get(itemOutputSlot).isEmpty()
					|| (inventory.get(itemOutputSlot).getItem() == UniversalCoins.Items.emerald_coin
							&& inventory.get(itemOutputSlot).getCount() != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[3]) {
			diamondCoinBtnActive = inventory.get(itemOutputSlot).isEmpty()
					|| (inventory.get(itemOutputSlot).getItem() == UniversalCoins.Items.diamond_coin
							&& inventory.get(itemOutputSlot).getCount() != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[4]) {
			obsidianCoinBtnActive = inventory.get(itemOutputSlot).isEmpty()
					|| (inventory.get(itemOutputSlot).getItem() == UniversalCoins.Items.obsidian_coin
							&& inventory.get(itemOutputSlot).getCount() != 64);
		}
	}

	public void onSellPressed() {
		onSellPressed(1);
	}

	private void onSellPressed(int amount) {
		if (inventory.get(itemInputSlot).isEmpty()) {
			sellButtonActive = false;
			return;
		}
		if (amount > inventory.get(itemInputSlot).getCount()) {
			return;
		}
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory.get(itemInputSlot));
		if (itemPrice == -1) {
			sellButtonActive = false;
			return;
		}
		// handle damaged items
		if (inventory.get(itemInputSlot).isItemDamaged()) {
			itemPrice = itemPrice
					* (inventory.get(itemInputSlot).getMaxDamage() - inventory.get(itemInputSlot).getItemDamage())
					/ inventory.get(itemInputSlot).getMaxDamage();
		}
		inventory.get(itemInputSlot).shrink(amount);
		if (inventory.get(itemInputSlot).getCount() <= 0) {
			inventory.set(itemInputSlot, ItemStack.EMPTY);
		}
		int coinFromSale = (int) (itemPrice * amount * UniversalCoins.itemSellRatio);
		if (!inventory.get(itemCardSlot).isEmpty()
				&& inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card
				&& getAccountBalance() + (itemPrice * amount * UniversalCoins.itemSellRatio) < Long.MAX_VALUE) {
			creditAccount(coinFromSale);
		} else {
			coinSum += coinFromSale;
		}
		this.updateTE();
	}

	public void onSellMaxPressed() {
		int amount = 0;
		if (inventory.get(itemInputSlot).isEmpty()) {
			sellButtonActive = false;
			return;
		}
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory.get(itemInputSlot));
		if (itemPrice == -1 || itemPrice == 0) {
			sellButtonActive = false;
			return;
		}
		amount = (int) Math.min(inventory.get(itemInputSlot).getCount(),
				Math.min((Integer.MAX_VALUE - coinSum) / itemPrice, Integer.MAX_VALUE / itemPrice));
		if (amount > 0) {
			onSellPressed(amount);
		}
	}

	public void onBuyPressed() {
		onBuyPressed(1);
	}

	private void onBuyPressed(int amount) {
		boolean useCard = false;
		if (inventory.get(itemInputSlot).isEmpty() || !UniversalCoins.tradeStationBuyEnabled) {
			buyButtonActive = false;
			return;
		}
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory.get(itemInputSlot));
		// use the card if we have it
		if (!inventory.get(itemCardSlot).isEmpty() && getAccountBalance() > itemPrice * amount) {
			useCard = true;
		}
		if (itemPrice == -1 || (coinSum < itemPrice * amount && !useCard)) {
			// not enough coins, do we have a card?
			buyButtonActive = false;
			return;
		}
		if (inventory.get(itemOutputSlot).isEmpty() && inventory.get(itemInputSlot).getMaxStackSize() >= amount) {
			if (!debitAccount(itemPrice * amount)) {
				coinSum -= itemPrice * amount;
			}
			if (inventory.get(itemInputSlot).isItemStackDamageable()) {
				inventory.set(itemOutputSlot, new ItemStack(inventory.get(itemInputSlot).getItem(), amount, 0));
			} else {
				inventory.set(itemOutputSlot, new ItemStack(inventory.get(itemInputSlot).getItem(), amount,
						inventory.get(itemInputSlot).getItemDamage()));
			}
			if (UCItemPricer.getInstance().hasEnchantment(inventory.get(itemInputSlot))) {
				inventory.get(itemOutputSlot).setTagCompound(inventory.get(itemInputSlot).getTagCompound());
			}
		} else if (inventory.get(itemOutputSlot).getItem() == inventory.get(itemInputSlot).getItem()
				&& inventory.get(itemOutputSlot).getItemDamage() == inventory.get(itemInputSlot).getItemDamage()
				&& inventory.get(itemOutputSlot).getCount() + amount <= inventory.get(itemInputSlot)
						.getMaxStackSize()) {
			if (!debitAccount(itemPrice * amount)) {
				coinSum -= itemPrice * amount;
			}
			inventory.get(itemOutputSlot).grow(amount);
		} else {
			buyButtonActive = false;
		}
		this.updateTE();
	}

	public void onBuyMaxPressed() {
		boolean useCard = false;
		int amount = 0;
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory.get(itemInputSlot));
		// use the card if we have it
		if (!inventory.get(itemCardSlot).isEmpty() && getAccountBalance() > itemPrice) {
			useCard = true;
		}
		if (itemPrice == -1 || (coinSum < itemPrice && !useCard)) {
			buyButtonActive = false;
			return;
		}
		if (inventory.get(itemOutputSlot).isEmpty()) {
			if (inventory.get(itemInputSlot).getMaxStackSize()
					* itemPrice <= (useCard ? getAccountBalance() : coinSum)) {
				amount = inventory.get(itemInputSlot).getMaxStackSize();
			} else {
				amount = (int) ((useCard ? getAccountBalance() : coinSum) / itemPrice);
			}
		} else if (inventory.get(itemOutputSlot).getItem() == inventory.get(itemInputSlot).getItem()
				&& inventory.get(itemOutputSlot).getItemDamage() == inventory.get(itemInputSlot).getItemDamage()
				&& inventory.get(itemOutputSlot).getCount() < inventory.get(itemInputSlot).getMaxStackSize()) {
			if ((inventory.get(itemOutputSlot).getMaxStackSize() - inventory.get(itemOutputSlot).getCount())
					* itemPrice <= (useCard ? getAccountBalance() : coinSum)) {
				amount = inventory.get(itemOutputSlot).getMaxStackSize() - inventory.get(itemOutputSlot).getCount();
			} else {
				amount = (int) ((useCard ? getAccountBalance() : coinSum) / itemPrice);
			}
		} else {
			buyButtonActive = false;
		}
		onBuyPressed(amount);
	}

	public void onAutoModeButtonPressed() {
		if (autoMode == 2) {
			autoMode = 0;
		} else {
			autoMode++;
			if (autoMode == 1 && !UniversalCoins.tradeStationBuyEnabled) {
				autoMode++;
			}
		}
	}

	public void onCoinModeButtonPressed() {
		if (coinMode == 0) {
			coinMode = 5;
		} else
			coinMode--;
	}

	public void runAutoMode() {
		// Delay autosell to workaround thermal expansion's auto output issue
		if (autoDelayCounter == 3) {
			autoDelayCounter = 0;
			if (autoMode == 0) {
				return;
			} else if (autoMode == 1) {
				onBuyMaxPressed();
			} else if (autoMode == 2) {
				onSellMaxPressed();
			}
		}
		autoDelayCounter++;
	}

	public void runCoinMode() {
		if (coinMode == 0) {
			return;
		} else {
			onRetrieveButtonsPressed(coinMode + 1, true);
		}
	}

	public void onRetrieveButtonsPressed(int buttonClickedID, boolean shiftPressed) {
		int absoluteButton = buttonClickedID - TradeStationGUI.idCoinButton;
		Item itemOnButton = null;
		switch (absoluteButton) {
		case 0:
			itemOnButton = UniversalCoins.Items.iron_coin;
			break;
		case 1:
			itemOnButton = UniversalCoins.Items.gold_coin;
			break;
		case 2:
			itemOnButton = UniversalCoins.Items.emerald_coin;
			break;
		case 3:
			itemOnButton = UniversalCoins.Items.diamond_coin;
			break;
		case 4:
			itemOnButton = UniversalCoins.Items.obsidian_coin;
			break;
		}
		if (itemOnButton == null)
			return;
		if (coinSum < UniversalCoins.coinValues[absoluteButton]
				|| (!inventory.get(itemOutputSlot).isEmpty() && inventory.get(itemOutputSlot).getItem() != itemOnButton)
				|| (!inventory.get(itemOutputSlot).isEmpty() && inventory.get(itemOutputSlot).getCount() == 64)) {
			return;
		}
		if (shiftPressed) {
			if (inventory.get(itemOutputSlot).isEmpty()) {
				int amount = (int) (coinSum / UniversalCoins.coinValues[absoluteButton]);
				if (amount >= 64) {
					coinSum -= UniversalCoins.coinValues[absoluteButton] * 64;
					inventory.set(itemOutputSlot, new ItemStack(itemOnButton));
					inventory.get(itemOutputSlot).setCount(64);
				} else {
					coinSum -= UniversalCoins.coinValues[absoluteButton] * amount;
					inventory.set(itemOutputSlot, new ItemStack(itemOnButton));
					inventory.get(itemOutputSlot).setCount(amount);
				}
			} else {
				int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[absoluteButton],
						inventory.get(itemOutputSlot).getMaxStackSize() - inventory.get(itemOutputSlot).getCount());
				inventory.get(itemOutputSlot).grow(amount);
				coinSum -= UniversalCoins.coinValues[absoluteButton] * amount;
			}
		} else {
			coinSum -= UniversalCoins.coinValues[absoluteButton];
			if (inventory.get(itemOutputSlot).isEmpty()) {
				inventory.set(itemOutputSlot, new ItemStack(itemOnButton));
			} else {
				inventory.get(itemOutputSlot).grow(1);
			}
		}
	}

	public void onButtonPressed(int buttonId, boolean shiftPressed) {
		if (buttonId == TradeStationGUI.idBuyButton) {
			if (shiftPressed) {
				onBuyMaxPressed();
			} else {
				onBuyPressed();
			}
		} else if (buttonId == TradeStationGUI.idSellButton) {
			if (shiftPressed) {
				onSellMaxPressed();
			} else {
				onSellPressed();
			}
		} else if (buttonId == TradeStationGUI.idAutoModeButton) {
			onAutoModeButtonPressed();
		} else if (buttonId == TradeStationGUI.idCoinModeButton) {
			onCoinModeButtonPressed();
		} else if (buttonId <= TradeStationGUI.idLBagButton) {
			onRetrieveButtonsPressed(buttonId, shiftPressed);
		} else if (buttonId == TradeStationGUI.idAccessModeButton && blockOwner.matches(playerName)) {
			publicAccess ^= true;
		}
		update();
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.inventory = NonNullList.<ItemStack> withSize(this.getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(tagCompound, this.inventory);
		try {
			autoMode = tagCompound.getInteger("AutoMode");
		} catch (Throwable ex2) {
			autoMode = 0;
		}
		try {
			coinMode = tagCompound.getInteger("CoinMode");
		} catch (Throwable ex2) {
			coinMode = 0;
		}
		try {
			coinSum = tagCompound.getInteger("CoinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			autoModeButtonActive = tagCompound.getBoolean("AutoModeButtonActive");
		} catch (Throwable ex2) {
			autoModeButtonActive = true;
		}
		try {
			inUse = tagCompound.getBoolean("InUse");
		} catch (Throwable ex2) {
			inUse = false;
		}
		try {
			publicAccess = tagCompound.getBoolean("PublicAccess");
		} catch (Throwable ex2) {
			publicAccess = true;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		ItemStackHelper.saveAllItems(tagCompound, this.inventory);
		tagCompound.setInteger("AutoMode", autoMode);
		tagCompound.setInteger("CoinMode", coinMode);
		tagCompound.setInteger("CoinSum", coinSum);
		tagCompound.setBoolean("AutoModeButtonActive", autoModeButtonActive);
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setBoolean("PublicAccess", publicAccess);
		return tagCompound;
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

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
	}

	public void updateTE() {
		final IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 3);
	}

	@Override
	public int getSizeInventory() {
		return inventory.size();
	}

	@Override
	public String getName() {
		return UniversalCoins.Blocks.tradestation.getLocalizedName();
	}

	public void setName(String name) {
	}

	public boolean isNameLocalized() {
		return false;
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
	public ItemStack getStackInSlot(int index) {
		runAutoMode();
		return (ItemStack) inventory.get(index);
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(inventory, slot, size);
		if (!itemstack.isEmpty()) {
			this.markDirty();
		}
		return itemstack;
	}

	// @Override
	public ItemStack getStackInSlotOnClosing(int index) {
		inUse = false;
		return (ItemStack) inventory.get(index);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		autoDelayCounter = 0;
		inventory.set(slot, stack);
		if (slot == itemCoinSlot || slot == itemInputSlot) {
			int coinValue = 0;
			coinValue = CoinUtils.getCoinValue(stack);
			if (coinValue > 0) {
				int depositAmount = (int) Math.min(Integer.MAX_VALUE - coinSum / coinValue, stack.getCount());
				if (inventory.get(itemCardSlot) != null && inventory.get(itemCardSlot).hasTagCompound()
						&& inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card) {
					creditAccount(depositAmount * coinValue);
				} else {
					coinSum += depositAmount * coinValue;
				}
				ItemStack newStack = stack;
				newStack.shrink((int) depositAmount);
				if (!newStack.isEmpty()) {
					inventory.set(slot, newStack);
					return;
				} else {
					inventory.set(slot, ItemStack.EMPTY);
					return;
				}
			}
		}
		if (slot == itemCardSlot && inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card) {
			if (!world.isRemote) {
				String accountNumber = stack.getTagCompound().getString("Account");
				if (UniversalAccounts.getInstance().creditAccount(accountNumber, coinSum, false)) {
					coinSum = 0;
				}
			}
		}
		update();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return world.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring
	 * stack size) into the given slot.
	 */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		Item stackItem = itemstack.getItem();
		if (slot == itemCoinSlot) {
			return stackItem == UniversalCoins.Items.iron_coin || stackItem == UniversalCoins.Items.gold_coin
					|| stackItem == UniversalCoins.Items.emerald_coin || stackItem == UniversalCoins.Items.diamond_coin
					|| stackItem == UniversalCoins.Items.obsidian_coin;
		} else {
			return slot == itemInputSlot;
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
		// first check if items inserted are coins. put them in the coin input
		// slot if they are.
		if (index == itemCoinSlot && (stack.getItem() == (UniversalCoins.Items.iron_coin)
				|| stack.getItem() == (UniversalCoins.Items.gold_coin)
				|| stack.getItem() == (UniversalCoins.Items.emerald_coin)
				|| stack.getItem() == (UniversalCoins.Items.diamond_coin)
				|| stack.getItem() == (UniversalCoins.Items.obsidian_coin))) {
			return true;
			// put everything else in the item input slot
		} else if (index == itemInputSlot) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		// allow pulling items from output slot only
		if (index == itemOutputSlot) {
			return true;
		} else {
			return false;
		}
	}

	public long getAccountBalance() {
		if (!inventory.get(itemCardSlot).isEmpty() && inventory.get(itemCardSlot).hasTagCompound()) {
			String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
		return -1;
	}

	public boolean debitAccount(int amount) {
		if (!inventory.get(itemCardSlot).isEmpty() && inventory.get(itemCardSlot).hasTagCompound()) {
			String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().debitAccount(accountNumber, amount, false);
		}
		return false;
	}

	public boolean creditAccount(int amount) {
		if (!inventory.get(itemCardSlot).isEmpty() && inventory.get(itemCardSlot).hasTagCompound()) {
			String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().creditAccount(accountNumber, amount, false);
		}
		return false;
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
		switch (id) {
		case 0:
			return coinSum;
		case 1:
			return itemPrice;
		case 2:
			return autoMode;
		case 3:
			return coinMode;
		case 4:
			return publicAccess == true ? 1 : 0;
		case 5:
			return buyButtonActive == true ? 1 : 0;
		default:
			return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
		case 0:
			coinSum = value;
			break;
		case 1:
			itemPrice = value;
			break;
		case 2:
			autoMode = value;
			break;
		case 3:
			coinMode = value;
			break;
		case 4:
			publicAccess = value == 1 ? true : false;
			break;
		case 5:
			buyButtonActive = value == 1 ? true : false;
			break;
		default:
			break;
		}
	}

	@Override
	public int getFieldCount() {
		return 6;
	}

	@Override
	public void clear() {

	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.Blocks.tradestation.getLocalizedName());
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventory) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
