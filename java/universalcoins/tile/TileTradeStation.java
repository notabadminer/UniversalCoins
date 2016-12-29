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
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.gui.TradeStationGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UCItemPricer;
import universalcoins.util.UniversalAccounts;

public class TileTradeStation extends TileEntity implements IInventory, ISidedInventory {

	private ItemStack[] inventory = new ItemStack[4];
	public static final int itemInputSlot = 0;
	public static final int itemOutputSlot = 1;
	public static final int itemCardSlot = 2;
	public static final int itemCoinSlot = 3;
	public long coinSum = 0;
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
	private static final int[] slots_top = new int[] { 0, 1, 2, 3 };
	private static final int[] slots_bottom = new int[] { 0, 1, 2, 3 };
	private static final int[] slots_sides = new int[] { 0, 1, 2, 3 };

	public int autoMode = 0;
	public int coinMode = 0;
	public String customName;
	public boolean inUse = false;
	public String blockOwner = "none";
	public String playerName = "";

	public TileTradeStation() {
		super();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!worldObj.isRemote) {
			activateBuySellButtons();
			runAutoMode();
			runCoinMode();
		}
		activateRetrieveButtons();
	}

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
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
				// if card not available
				if ((inventory[itemCardSlot] != null && Long.MAX_VALUE - getAccountBalance() < itemPrice)
						&& (long) coinSum + (long) itemPrice > Integer.MAX_VALUE) {
					sellButtonActive = false;
				}
				if (inventory[itemCardSlot] == null && (long) coinSum + (long) itemPrice > Integer.MAX_VALUE) {
					sellButtonActive = false;
				}
				// disable sell button if item is enchanted
				if (inventory[itemInputSlot].isItemEnchanted()) {
					sellButtonActive = false;
				}

				buyButtonActive = (UniversalCoins.tradeStationBuyEnabled
						&& (inventory[itemOutputSlot] == null || (inventory[itemOutputSlot])
								.getItem() == inventory[itemInputSlot].getItem()
								&& inventory[itemOutputSlot].stackSize < inventory[itemInputSlot].getMaxStackSize())
						&& (coinSum >= itemPrice || getAccountBalance() > itemPrice));
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
			ironCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.iron_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[1]) {
			goldCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.gold_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[2]) {
			emeraldCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.emerald_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[3]) {
			diamondCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.diamond_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[4]) {
			obsidianCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.obsidian_coin
							&& inventory[itemOutputSlot].stackSize != 64);
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
			itemPrice = itemPrice * (inventory[itemInputSlot].getMaxDamage() - inventory[itemInputSlot].getItemDamage())
					/ inventory[itemInputSlot].getMaxDamage();
		}
		boolean playerCredited = false;
		if (inventory[itemCardSlot] != null) {
			playerCredited = creditAccount((int) (itemPrice * amount * UniversalCoins.itemSellRatio));
		}
		if (!playerCredited && coinSum + itemPrice * amount * UniversalCoins.itemSellRatio <= Integer.MAX_VALUE) {
			coinSum += itemPrice * amount * UniversalCoins.itemSellRatio;
			playerCredited = true;
		}
		if (playerCredited) {
			inventory[itemInputSlot].stackSize -= amount;
		}
		if (inventory[itemInputSlot].stackSize <= 0) {
			inventory[itemInputSlot] = null;
		}
	}

	public void onSellMaxPressed() {
		boolean useCard = false;
		int amount = 0;
		// use card if available
		if (inventory[itemCardSlot] != null && Long.MAX_VALUE - getAccountBalance() > itemPrice) {
			useCard = true;
		}
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
		if (itemPrice == -1 || itemPrice == 0) {
			sellButtonActive = false;
			return;
		}

		if (useCard) {
			if (Long.MAX_VALUE - getAccountBalance() > Integer.MAX_VALUE) {
				amount = Math.min(inventory[itemInputSlot].stackSize, Integer.MAX_VALUE / itemPrice);
			} else {
				amount = Math.min(inventory[itemInputSlot].stackSize,
						(int) (Long.MAX_VALUE - getAccountBalance() / itemPrice));
			}

		} else {
			amount = (int) Math.min(inventory[itemInputSlot].stackSize, (Long.MAX_VALUE - coinSum) / itemPrice);
		}

		if (amount != 0) {
			onSellPressed(amount);
		}
	}

	public void onBuyPressed() {
		onBuyPressed(1);
	}

	public void onBuyPressed(int amount) {
		if (inventory[itemInputSlot] == null || !UniversalCoins.tradeStationBuyEnabled) {
			buyButtonActive = false;
			return;
		}
		itemPrice = UCItemPricer.getInstance().getItemPrice(inventory[itemInputSlot]);
		if (itemPrice == -1 || (coinSum < itemPrice * amount && getAccountBalance() < itemPrice * amount)) {
			buyButtonActive = false;
			return;
		}
		if (inventory[itemOutputSlot] == null && inventory[itemInputSlot].getMaxStackSize() >= amount) {
			if (!debitAccount(itemPrice * amount)) {
				coinSum -= itemPrice * amount;
			}
			inventory[itemOutputSlot] = new ItemStack(inventory[itemInputSlot].getItem(), amount,
					inventory[itemInputSlot].getItemDamage());
		} else if (inventory[itemOutputSlot].getItem() == inventory[itemInputSlot].getItem()
				&& inventory[itemOutputSlot].getItemDamage() == inventory[itemInputSlot].getItemDamage()
				&& inventory[itemOutputSlot].stackSize + amount <= inventory[itemInputSlot].getMaxStackSize()) {
			if (!debitAccount(itemPrice * amount)) {
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
			if (useCard && getAccountBalance() > Integer.MAX_VALUE) {
				amount = Integer.MAX_VALUE / itemPrice;
			} else {
				amount = (int) ((useCard ? getAccountBalance() : coinSum) / itemPrice);
			}
			if (amount > inventory[itemInputSlot].getMaxStackSize()) {
				amount = inventory[itemInputSlot].getMaxStackSize();
			}
		} else if (inventory[itemOutputSlot].getItem() == inventory[itemInputSlot].getItem()
				&& inventory[itemOutputSlot].getItemDamage() == inventory[itemInputSlot].getItemDamage()
				&& inventory[itemOutputSlot].stackSize < inventory[itemInputSlot].getMaxStackSize()) {
			if (useCard && getAccountBalance() > Integer.MAX_VALUE) {
				amount = Integer.MAX_VALUE / itemPrice;
			} else {
				amount = (int) ((useCard ? getAccountBalance() : coinSum) / itemPrice);
			}
			if (amount > inventory[itemInputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize) {
				amount = inventory[itemInputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize;
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
		Item itemOnButton = null;
		switch (absoluteButton) {
		case 0:
			itemOnButton = UniversalCoins.proxy.iron_coin;
			break;
		case 1:
			itemOnButton = UniversalCoins.proxy.gold_coin;
			break;
		case 2:
			itemOnButton = UniversalCoins.proxy.emerald_coin;
			break;
		case 3:
			itemOnButton = UniversalCoins.proxy.diamond_coin;
			break;
		case 4:
			itemOnButton = UniversalCoins.proxy.obsidian_coin;
			break;
		}
		if (itemOnButton == null)
			return;
		if (coinSum < UniversalCoins.coinValues[absoluteButton]
				|| (inventory[itemOutputSlot] != null && inventory[itemOutputSlot].getItem() != itemOnButton)
				|| (inventory[itemOutputSlot] != null && inventory[itemOutputSlot].stackSize == 64)) {
			return;
		}
		if (shiftPressed) {
			if (inventory[itemOutputSlot] == null) {
				int amount = (int) (coinSum / UniversalCoins.coinValues[absoluteButton]);
				if (amount >= 64) {
					coinSum -= UniversalCoins.coinValues[absoluteButton] * 64;
					inventory[itemOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemOutputSlot].stackSize = 64;
				} else {
					coinSum -= UniversalCoins.coinValues[absoluteButton] * amount;
					inventory[itemOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemOutputSlot].stackSize = amount;
				}
			} else {
				int amount = (int) Math.min(coinSum / UniversalCoins.coinValues[absoluteButton],
						inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize);
				inventory[itemOutputSlot].stackSize += amount;
				coinSum -= UniversalCoins.coinValues[absoluteButton] * amount;
			}
		} else {
			coinSum -= UniversalCoins.coinValues[absoluteButton];
			if (inventory[itemOutputSlot] == null) {
				inventory[itemOutputSlot] = new ItemStack(itemOnButton);
			} else {
				inventory[itemOutputSlot].stackSize++;
			}
		}
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
			coinSum = tagCompound.getLong("CoinSum");
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
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "none";
		}
		try {
			publicAccess = tagCompound.getBoolean("publicAccess");
		} catch (Throwable ex2) {
			publicAccess = true;
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
		tagCompound.setLong("CoinSum", coinSum);
		tagCompound.setInteger("AutoMode", autoMode);
		tagCompound.setInteger("CoinMode", coinMode);
		tagCompound.setInteger("ItemPrice", itemPrice);
		tagCompound.setString("CustomName", getInventoryName());
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setString("blockOwner", blockOwner);
		tagCompound.setBoolean("publicAccess", publicAccess);
		tagCompound.setBoolean("buyButtonActive", buyButtonActive);
		tagCompound.setBoolean("sellButtonActive", sellButtonActive);
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, button, shiftPressed));
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : UniversalCoins.proxy.trade_station.getLocalizedName();
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

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		int coinValue = 0;
		if (stack != null) {
			if (slot == itemCoinSlot || slot == itemInputSlot) {
				switch (stack.getUnlocalizedName()) {
				case "item.iron_coin":
					coinValue = UniversalCoins.coinValues[0];
					break;
				case "item.gold_coin":
					coinValue = UniversalCoins.coinValues[1];
					break;
				case "item.emerald_coin":
					coinValue = UniversalCoins.coinValues[2];
					break;
				case "item.diamond_coin":
					coinValue = UniversalCoins.coinValues[3];
					break;
				case "item.obsidian_coin":
					coinValue = UniversalCoins.coinValues[4];
					break;
				}
			}
			if (coinValue > 0) {
				int depositAmount = (int) Math.min(Long.MAX_VALUE - coinSum / coinValue, stack.stackSize);
				if (inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound()
						&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.ender_card) {
					creditAccount(depositAmount * coinValue);
				} else {
					coinSum += depositAmount * coinValue;
				}
				inventory[slot].stackSize -= depositAmount;
				if (inventory[slot].stackSize == 0) {
					inventory[slot] = null;
				}
			}
			if(slot == itemCardSlot) {
				if (creditAccount(coinSum)) {
					coinSum = 0;
				}
			}
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring
	 * stack size) into the given slot.
	 */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		Item stackItem = itemstack.getItem();
		if (slot == itemCoinSlot) {
			return stackItem == UniversalCoins.proxy.iron_coin || stackItem == UniversalCoins.proxy.gold_coin
					|| stackItem == UniversalCoins.proxy.emerald_coin || stackItem == UniversalCoins.proxy.diamond_coin
					|| stackItem == UniversalCoins.proxy.obsidian_coin;
		} else { // noinspection RedundantIfStatement
			return slot == itemInputSlot || slot == itemCoinSlot;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return var1 == 0 ? slots_bottom : (var1 == 1 ? slots_top : slots_sides);
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, int var3) {
		// first check if items inserted are coins. put them in the coin input
		// slot if they are.
		if (index == itemCoinSlot && (stack.getItem() == (UniversalCoins.proxy.iron_coin)
				|| stack.getItem() == (UniversalCoins.proxy.gold_coin)
				|| stack.getItem() == (UniversalCoins.proxy.emerald_coin)
				|| stack.getItem() == (UniversalCoins.proxy.diamond_coin)
				|| stack.getItem() == (UniversalCoins.proxy.obsidian_coin))) {
			return true;
			// put everything else in the item input slot
		} else if (index == itemInputSlot) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3) {
		// allow pulling items from output slot only
		if (var1 == itemOutputSlot) {
			return true;
		} else {
			return false;
		}
	}

	private long getAccountBalance() {
		if (worldObj.isRemote || inventory[itemCardSlot] == null || !inventory[itemCardSlot].hasTagCompound()) {
			return 0;
		}
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		if (accountNumber == "") {
			return 0;
		}
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private boolean creditAccount(long i) {
		if (worldObj.isRemote || inventory[itemCardSlot] == null
				|| inventory[itemCardSlot].getItem() != UniversalCoins.proxy.ender_card
				|| !inventory[itemCardSlot].hasTagCompound())
			return false;
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		if (accountNumber == "") {
			return false;
		}
		return UniversalAccounts.getInstance().creditAccount(accountNumber, i);
	}

	private boolean debitAccount(long i) {
		if (worldObj.isRemote || inventory[itemCardSlot] == null || !inventory[itemCardSlot].hasTagCompound())
			return false;
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		if (accountNumber.matches("")) {
			return false;
		}
		return UniversalAccounts.getInstance().debitAccount(accountNumber, i);
	}
}
