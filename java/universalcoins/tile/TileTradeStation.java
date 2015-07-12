package universalcoins.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.gui.TradeStationGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UCItemPricer;
import universalcoins.util.UniversalAccounts;

public class TileTradeStation extends TileEntity implements IInventory, ISidedInventory, IUpdatePlayerListBox {

	private ItemStack[] inventory = new ItemStack[4];
	public static final int itemInputSlot = 0;
	public static final int itemOutputSlot = 1;
	public static final int itemCardSlot = 2;
	public static final int itemCoinSlot = 3;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
	public int coinSum = 0;
	public int itemPrice = 0;
	public boolean buyButtonActive = false;
	public boolean sellButtonActive = false;
	public boolean coinButtonActive = false;
	public boolean isSStackButtonActive = false;
	public boolean isLStackButtonActive = false;
	public boolean isSBagButtonActive = false;
	public boolean isLBagButtonActive = false;
	public boolean autoModeButtonActive = UniversalCoins.autoModeEnabled;
	private static final int[] slots_top = new int[] { 0, 1, 2, 3 };
	private static final int[] slots_bottom = new int[] { 0, 1, 2, 3 };
	private static final int[] slots_sides = new int[] { 0, 1, 2, 3 };

	public int autoMode = 0;
	public int coinMode = 0;
	public String customName;
	public boolean inUse = false;
	public String playerName = "";

	public TileTradeStation() {
		super();
	}

	public void update() {
		if (!worldObj.isRemote) {
			runAutoMode();
			runCoinMode();
		}
	}

	public void inUseCleanup() {
		if (worldObj.isRemote) return;
		inUse = false;
	}

	private void activateBuySellButtons() {
		if (inventory[itemInputSlot] == null) {
			itemPrice = 0;
			buyButtonActive = false;
			sellButtonActive = false;
		} else {
			itemPrice = UCItemPricer.getInstance().getItemPrice(inventory[itemInputSlot]);
			if (itemPrice <= -1 || itemPrice == 0) {
				itemPrice = 0;
				buyButtonActive = false;
				sellButtonActive = false;
			} else {
				sellButtonActive = true;
				// disable sell button if coinSum is near max
				// recast into long so value doesn't go negative
				if ((long) coinSum + (long) itemPrice > Integer.MAX_VALUE) {
					sellButtonActive = false;
				}
				// disable sell button if item is enchanted
				if (inventory[itemInputSlot].isItemEnchanted()) {
					sellButtonActive = false;
				}

				buyButtonActive = (UniversalCoins.tradeStationBuyEnabled
						&& (inventory[itemOutputSlot] == null || (inventory[itemOutputSlot]).getItem() 
						== inventory[itemInputSlot].getItem() && inventory[itemOutputSlot].stackSize 
						< inventory[itemInputSlot].getMaxStackSize()) && (coinSum >= itemPrice 
						|| (inventory[itemCardSlot] != null
						&& !worldObj.isRemote && getAccountBalance() > itemPrice)));
			}
		}
	}

	private void activateRetrieveButtons() {
		coinButtonActive = false;
		isSStackButtonActive = false;
		isLStackButtonActive = false;
		isSBagButtonActive = false;
		isLBagButtonActive = false;
		if (coinSum > 0) {
			coinButtonActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.itemCoin && inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= 9) {
			isSStackButtonActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.itemSmallCoinStack && inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= 81) {
			isLStackButtonActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.itemLargeCoinStack && inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= 729) {
			isSBagButtonActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.itemSmallCoinBag && inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= 6561) {
			isLBagButtonActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.itemLargeCoinBag && inventory[itemOutputSlot].stackSize != 64);
		}
	}

	public void onSellPressed() {
		onSellPressed(1);
	}

	public void onSellPressed(int amount) {
		if (inventory[itemInputSlot] == null) {
			sellButtonActive = false;
			return;
		}
		if (amount > inventory[itemInputSlot].stackSize) {
			return;
		}
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory[itemInputSlot]);
		if (itemPrice == -1) {
			sellButtonActive = false;
			return;
		}
		// handle damaged items
		if (inventory[itemInputSlot].isItemDamaged()) {
			itemPrice = itemPrice
					* (inventory[itemInputSlot].getMaxDamage() - inventory[itemInputSlot].getItemDamage())
					/ inventory[itemInputSlot].getMaxDamage();
		}
		inventory[itemInputSlot].stackSize -= amount;
		if (inventory[itemInputSlot].stackSize <= 0) {
			inventory[itemInputSlot] = null;
		}
		if (inventory[itemCardSlot] != null && inventory[itemCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard
				&& getAccountBalance() + (itemPrice * amount * UniversalCoins.itemSellRatio) < Integer.MAX_VALUE) {
			creditAccount((int) (itemPrice * amount * UniversalCoins.itemSellRatio));
		} else {
			coinSum += itemPrice * amount * UniversalCoins.itemSellRatio;
		}
	}

	public void onSellMaxPressed() {
		int amount = 0;
		if (inventory[itemInputSlot] == null) {
			if (inventory[itemInputSlot] == null) {
				sellButtonActive = false;
				return;
			}
		}
		// disable selling if item is enchanted
		if (inventory[itemInputSlot].isItemEnchanted()) {
			sellButtonActive = false;
			return;
		}
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory[itemInputSlot]);
		if (itemPrice == -1) {
			sellButtonActive = false;
			return;
		}

		amount = Math.min(inventory[itemInputSlot].stackSize, (Integer.MAX_VALUE - coinSum) / itemPrice);

		if (amount != 0) {
			onSellPressed(amount);
		}
	}

	public void onBuyPressed() {
		onBuyPressed(1);
	}

	public void onBuyPressed(int amount) {
		boolean useCard = false;
		if (inventory[itemInputSlot] == null || !UniversalCoins.tradeStationBuyEnabled) {
			buyButtonActive = false;
			return;
		}
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory[itemInputSlot]);
		// use the card if we have it
		if (inventory[itemCardSlot] != null && getAccountBalance() > itemPrice * amount) {
			useCard = true;
		}
		if (itemPrice == -1 || (coinSum < itemPrice * amount && !useCard)) {
			// not enough coins, do we have a card?
			buyButtonActive = false;
			return;
		}
		if (inventory[itemOutputSlot] == null && inventory[itemInputSlot].getMaxStackSize() >= amount) {
			if (useCard && inventory[itemCardSlot] != null) {
				debitAccount(itemPrice * amount);
			} else {
				coinSum -= itemPrice * amount;
			}
			if (inventory[itemInputSlot].isItemDamaged() || inventory[itemInputSlot].isItemEnchanted()) {
				inventory[itemOutputSlot] = new ItemStack(inventory[itemInputSlot].getItem(), 1);
			} else
				inventory[itemOutputSlot] = inventory[itemInputSlot].copy();
			inventory[itemOutputSlot].stackSize = amount;
		} else if (inventory[itemOutputSlot].getItem() == inventory[itemInputSlot].getItem()
				&& inventory[itemOutputSlot].getItemDamage() == inventory[itemInputSlot].getItemDamage()
				&& inventory[itemOutputSlot].stackSize + amount <= inventory[itemInputSlot].getMaxStackSize()) {
			if (useCard && inventory[itemCardSlot] != null) {
				debitAccount(itemPrice * amount);
			} else {
				coinSum -= itemPrice * amount;
			}
			inventory[itemOutputSlot].stackSize += amount;
		} else {
			buyButtonActive = false;
		}
	}

	public void onBuyMaxPressed() {
		boolean useCard = false;
		int amount = 0;
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory[itemInputSlot]);
		// use the card if we have it
		if (inventory[itemCardSlot] != null && getAccountBalance() > itemPrice) {
			useCard = true;
		}
		if (itemPrice == -1 || (coinSum < itemPrice && !useCard)) {
			buyButtonActive = false;
			return;
		}
		if (inventory[itemOutputSlot] == null) { // empty stack
			if (inventory[itemInputSlot].getMaxStackSize() * itemPrice <= (useCard ? getAccountBalance() : coinSum)) {
				amount = inventory[itemInputSlot].getMaxStackSize(); // buy one stack
			} else {
				amount = (useCard ? getAccountBalance() : coinSum) / itemPrice;
			}
		} else if (inventory[itemOutputSlot].getItem() == inventory[itemInputSlot].getItem()
				&& inventory[itemOutputSlot].getItemDamage() == inventory[itemInputSlot].getItemDamage()
				&& inventory[itemOutputSlot].stackSize < inventory[itemInputSlot].getMaxStackSize()) {

			if ((inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize) * 
					itemPrice <= (useCard ? getAccountBalance()	: coinSum)) {
				amount = inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize;
				// buy as much as i can fit in a stack
			} else {
				amount = (useCard ? getAccountBalance() : coinSum) / itemPrice; 
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
		if (autoMode == 0) {
			return;
		} else if (autoMode == 1) {
			onBuyMaxPressed();
		} else if (autoMode == 2) {
			onSellMaxPressed();
			// FMLLog.info("UC: coins = " + coinSum);
		}
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
		int multiplier = 1;
		for (int i = 0; i < absoluteButton; i++) {
			multiplier *= 9;
		}
		Item itemOnButton = coins[absoluteButton];
		if (coinSum < multiplier
				|| (inventory[itemOutputSlot] != null && inventory[itemOutputSlot].getItem() != itemOnButton)
				|| (inventory[itemOutputSlot] != null && inventory[itemOutputSlot].stackSize == 64)) {
			return;
		}
		if (shiftPressed) {
			if (inventory[itemOutputSlot] == null) {
				int amount = coinSum / multiplier;
				if (amount >= 64) {
					coinSum -= multiplier * 64;
					inventory[itemOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemOutputSlot].stackSize = 64;
				} else {
					coinSum -= multiplier * amount;
					inventory[itemOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemOutputSlot].stackSize = amount;
				}
			} else {
				int amount = Math.min(coinSum / multiplier, inventory[itemOutputSlot].getMaxStackSize()
						- inventory[itemOutputSlot].stackSize);
				inventory[itemOutputSlot].stackSize += amount;
				coinSum -= multiplier * amount;
			}
		} else {
			coinSum -= multiplier;
			if (inventory[itemOutputSlot] == null) {
				inventory[itemOutputSlot] = new ItemStack(itemOnButton);
			} else {
				inventory[itemOutputSlot].stackSize++;
			}
		}
	}
	
	public void onButtonPressed(int buttonId, boolean shiftPressed){
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
		}
		activateRetrieveButtons();
		activateBuySellButtons();
		updateTE();
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
			coinSum = tagCompound.getInteger("CoinsLeft");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
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
			itemPrice = tagCompound.getInteger("ItemPrice");
		} catch (Throwable ex2) {
			itemPrice = 0;
		}
		try {
			customName = tagCompound.getString("CustomName");
		} catch (Throwable ex2) {
			customName = null;
		}
		try {
			inUse = tagCompound.getBoolean("InUse");
		} catch (Throwable ex2) {
			inUse = false;
		}
		try {
			buyButtonActive = tagCompound.getBoolean("buyButtonActive");
		} catch (Throwable ex2) {
			buyButtonActive = false;
		}
		try {
			sellButtonActive = tagCompound.getBoolean("sellButtonActive");
		} catch (Throwable ex2) {
			sellButtonActive = false;
		}
		activateRetrieveButtons();
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
		tagCompound.removeTag("CoinsLeft");
		tagCompound.setTag("Inventory", itemList);
		tagCompound.setInteger("CoinsLeft", coinSum);
		tagCompound.setInteger("AutoMode", autoMode);
		tagCompound.setInteger("CoinMode", coinMode);
		tagCompound.setInteger("ItemPrice", itemPrice);
		tagCompound.setString("CustomName", getName());
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setBoolean("buyButtonActive", buyButtonActive);
		tagCompound.setBoolean("sellButtonActive", sellButtonActive);
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(pos);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(pos, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	public String getName() {
		return this.hasCustomName() ? this.customName : UniversalCoins.proxy.blockTradeStation.getLocalizedName();
	}

	public void setName(String name) {
		customName = name;
	}

	public boolean isNameLocalized() {
		return false;
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
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
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(size);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		activateBuySellButtons();
		activateRetrieveButtons();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		inUse = false;
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			if (slot == itemCoinSlot || slot == itemInputSlot) {
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
					if (inventory[itemCardSlot] != null
							&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard
							&& getAccountBalance() + (itemPrice * depositAmount) < Integer.MAX_VALUE) {
						creditAccount(depositAmount * itemValue);
					} else {
						coinSum += depositAmount * itemValue;
					}
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
				}
			}
			activateRetrieveButtons();
			activateBuySellButtons();
		}
	}

	private int getCoinType(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(pos) == this
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
			return stackItem == UniversalCoins.proxy.itemCoin || stackItem == UniversalCoins.proxy.itemSmallCoinStack
					|| stackItem == UniversalCoins.proxy.itemLargeCoinStack
					|| stackItem == UniversalCoins.proxy.itemSmallCoinBag
					|| stackItem == UniversalCoins.proxy.itemLargeCoinBag;
		} else { // noinspection RedundantIfStatement
			return slot == itemInputSlot || slot == itemCoinSlot;
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return null;
		// return side == 0 ? slots_bottom : (side == 1 ? slots_top :
		// slots_sides);
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		// first check if items inserted are coins. put them in the coin input
		// slot if they are.
		if (index == itemCoinSlot
				&& (itemStackIn.getItem() == (UniversalCoins.proxy.itemCoin)
						|| itemStackIn.getItem() == (UniversalCoins.proxy.itemSmallCoinStack)
						|| itemStackIn.getItem() == (UniversalCoins.proxy.itemLargeCoinStack)
						|| itemStackIn.getItem() == (UniversalCoins.proxy.itemSmallCoinBag) || itemStackIn.getItem() == (UniversalCoins.proxy.itemLargeCoinBag))) {
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

	public int getAccountBalance() {
		if (inventory[itemCardSlot] != null) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().getAccountBalance(worldObj, accountNumber);
		}
		return -1;
	}

	public void debitAccount(int amount) {
		if (inventory[itemCardSlot] != null) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().debitAccount(worldObj, accountNumber, amount);
		}
	}

	public void creditAccount(int amount) {
		if (inventory[itemCardSlot] != null) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().creditAccount(worldObj, accountNumber, amount);
		}
	}

	@Override
	public IChatComponent getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
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
