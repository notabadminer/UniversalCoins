package universalcoins.tileentity;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.gui.TradeStationGUI;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UCItemPricer;
import universalcoins.util.UniversalAccounts;

public class TileTradeStation extends TileProtected implements IInventory, ISidedInventory {

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

	public TileTradeStation() {
		super();
	}

	public void update() {
		if (!worldObj.isRemote) {
			runAutoMode();
			runCoinMode();
			activateRetrieveButtons();
			activateBuySellButtons();
		}
	}

	public void inUseCleanup() {
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
						&& (inventory[itemOutputSlot] == null || (inventory[itemOutputSlot])
								.getItem() == inventory[itemInputSlot].getItem()
								&& inventory[itemOutputSlot].stackSize < inventory[itemInputSlot].getMaxStackSize())
						&& (coinSum >= itemPrice || (inventory[itemCardSlot] != null && !worldObj.isRemote
								&& getAccountBalance() > itemPrice)));
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
		inventory[itemInputSlot].stackSize -= amount;
		if (inventory[itemInputSlot].stackSize <= 0) {
			inventory[itemInputSlot] = null;
		}
		if (inventory[itemCardSlot] != null && inventory[itemCardSlot].getItem() == UniversalCoins.proxy.ender_card
				&& getAccountBalance() + (itemPrice * amount * UniversalCoins.itemSellRatio) < Integer.MAX_VALUE) {
			creditAccount((int) (itemPrice * amount * UniversalCoins.itemSellRatio));
		} else {
			coinSum += itemPrice * amount * UniversalCoins.itemSellRatio;
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
			inventory[itemOutputSlot] = new ItemStack(inventory[itemInputSlot].getItem(), amount,
					inventory[itemInputSlot].getItemDamage());
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
				amount = inventory[itemInputSlot].getMaxStackSize(); // buy one
																		// stack
			} else {
				amount = (int) ((useCard ? getAccountBalance() : coinSum) / itemPrice);
			}
		} else if (inventory[itemOutputSlot].getItem() == inventory[itemInputSlot].getItem()
				&& inventory[itemOutputSlot].getItemDamage() == inventory[itemInputSlot].getItemDamage()
				&& inventory[itemOutputSlot].stackSize < inventory[itemInputSlot].getMaxStackSize()) {

			if ((inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize)
					* itemPrice <= (useCard ? getAccountBalance() : coinSum)) {
				amount = inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize;
				// buy as much as i can fit in a stack
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
		try {
			ironCoinBtnActive = tagCompound.getBoolean("ironCoinBtnActive");
		} catch (Throwable ex2) {
			ironCoinBtnActive = false;
		}
		try {
			goldCoinBtnActive = tagCompound.getBoolean("goldCoinBtnActive");
		} catch (Throwable ex2) {
			goldCoinBtnActive = false;
		}
		try {
			emeraldCoinBtnActive = tagCompound.getBoolean("emeraldCoinBtnActive");
		} catch (Throwable ex2) {
			emeraldCoinBtnActive = false;
		}
		try {
			diamondCoinBtnActive = tagCompound.getBoolean("diamondCoinBtnActive");
		} catch (Throwable ex2) {
			diamondCoinBtnActive = false;
		}
		try {
			obsidianCoinBtnActive = tagCompound.getBoolean("obsidianCoinBtnActive");
		} catch (Throwable ex2) {
			obsidianCoinBtnActive = false;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
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
		tagCompound.removeTag("CoinSum");
		tagCompound.setTag("Inventory", itemList);
		tagCompound.setLong("CoinSum", coinSum);
		tagCompound.setInteger("AutoMode", autoMode);
		tagCompound.setInteger("CoinMode", coinMode);
		tagCompound.setInteger("ItemPrice", itemPrice);
		tagCompound.setString("CustomName", getName());
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setBoolean("publicAccess", publicAccess);
		tagCompound.setBoolean("buyButtonActive", buyButtonActive);
		tagCompound.setBoolean("sellButtonActive", sellButtonActive);
		tagCompound.setBoolean("ironCoinBtnActive", ironCoinBtnActive);
		tagCompound.setBoolean("goldCoinBtnActive", goldCoinBtnActive);
		tagCompound.setBoolean("emeraldCoinBtnActive", emeraldCoinBtnActive);
		tagCompound.setBoolean("diamondCoinBtnActive", diamondCoinBtnActive);
		tagCompound.setBoolean("obsidianCoinBtnActive", obsidianCoinBtnActive);

		return tagCompound;
	}

	public void updateTE() {
		markDirty();
		worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
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

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	public String getName() {
		return this.hasCustomName() ? this.customName : UniversalCoins.proxy.tradestation.getLocalizedName();
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
		update();
		return stack;
	}

	// @Override
	public ItemStack getStackInSlotOnClosing(int i) {
		inUse = false;
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
		}
		update();
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
			return stackItem == UniversalCoins.proxy.iron_coin || stackItem == UniversalCoins.proxy.gold_coin
					|| stackItem == UniversalCoins.proxy.emerald_coin || stackItem == UniversalCoins.proxy.diamond_coin
					|| stackItem == UniversalCoins.proxy.obsidian_coin;
		} else { // noinspection RedundantIfStatement
			return slot == itemInputSlot || slot == itemCoinSlot;
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return side == EnumFacing.DOWN ? slots_bottom : (side == EnumFacing.UP ? slots_top : slots_sides);
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		// first check if items inserted are coins. put them in the coin input
		// slot if they are.
		if (index == itemCoinSlot && (itemStackIn.getItem() == (UniversalCoins.proxy.iron_coin)
				|| itemStackIn.getItem() == (UniversalCoins.proxy.gold_coin)
				|| itemStackIn.getItem() == (UniversalCoins.proxy.emerald_coin)
				|| itemStackIn.getItem() == (UniversalCoins.proxy.diamond_coin)
				|| itemStackIn.getItem() == (UniversalCoins.proxy.obsidian_coin))) {
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
		if (inventory[itemCardSlot] != null) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
		return -1;
	}

	public void debitAccount(int amount) {
		if (inventory[itemCardSlot] != null) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().debitAccount(accountNumber, amount);
		}
	}

	public void creditAccount(int amount) {
		if (inventory[itemCardSlot] != null) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().creditAccount(accountNumber, amount);
		}
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
		return new TextComponentString(UniversalCoins.proxy.tradestation.getLocalizedName());
	}
}
