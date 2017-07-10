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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.item.ItemEnderCard;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCVendorServerMessage;
import universalcoins.util.CoinUtils;
import universalcoins.util.UniversalAccounts;

public class TileVendor extends TileProtected implements IInventory, ISidedInventory {
	protected NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(17, ItemStack.EMPTY);
	// owner slots
	public static final int itemStorageSlot1 = 0;
	public static final int itemStorageSlot2 = 1;
	public static final int itemStorageSlot3 = 2;
	public static final int itemStorageSlot4 = 3;
	public static final int itemStorageSlot5 = 4;
	public static final int itemStorageSlot6 = 5;
	public static final int itemStorageSlot7 = 6;
	public static final int itemStorageSlot8 = 7;
	public static final int itemStorageSlot9 = 8;
	public static final int itemTradeSlot = 9;
	public static final int itemCardSlot = 10;
	// sale slots
	public static final int itemSellSlot = 11;
	public static final int itemOutputSlot = 12;
	public static final int itemCoinOutputSlot = 13;
	public static final int itemCoinInputSlot = 14;
	public static final int itemUserCoinInputSlot = 15;
	// card slot
	public static final int itemUserCardSlot = 16;

	public int coinSum = 0;
	public int userCoinSum = 0;
	public int itemPrice = 0;
	public boolean infiniteMode = false;
	public boolean sellMode = true;
	public boolean ooStockWarning = false;
	public boolean ooCoinsWarning = false;
	public boolean inventoryFullWarning = false;
	public boolean buyButtonActive = false;
	public boolean sellButtonActive = false;
	public boolean ironCoinBtnActive = false;
	public boolean goldCoinBtnActive = false;
	public boolean emeraldCoinBtnActive = false;
	public boolean diamondCoinBtnActive = false;
	public boolean obsidianCoinBtnActive = false;
	public boolean usellButtonActive = false;
	public boolean uironCoinBtnActive = false;
	public boolean ugoldCoinBtnActive = false;
	public boolean uemeraldCoinBtnActive = false;
	public boolean udiamondCoinBtnActive = false;
	public boolean uobsidianCoinBtnActive = false;
	public int textColor = 0x0;
	private int remoteX = 0;
	private int remoteY = 0;
	private int remoteZ = 0;

	public void update() {
		if (!world.isRemote) {
			activateRetrieveButtons();
			activateUserRetrieveButtons();
			activateBuyButton();
			activateSellButton();
			if (ooStockWarning)
				checkRemoteStorage();
		}
	}

	public void inUseCleanup() {
		if (world.isRemote)
			return;
		inUse = false;
	}

	private void activateBuyButton() {
		if ((userCoinSum >= itemPrice && coinSum + itemPrice < Integer.MAX_VALUE && (!ooStockWarning || infiniteMode))
				|| (!inventory.get(itemUserCardSlot).isEmpty() && getUserAccountBalance() > itemPrice
						&& (!ooStockWarning || infiniteMode))) {
			if (inventory.get(itemOutputSlot).isEmpty()) {
				if (inventory.get(itemOutputSlot).getMaxStackSize() == inventory.get(itemOutputSlot).getCount()) {
					buyButtonActive = false;
					return;
				}
			}
			buyButtonActive = true;
		} else
			buyButtonActive = false;
	}

	private void activateSellButton() {
		if (!inventory.get(itemSellSlot).isEmpty()
				&& inventory.get(itemTradeSlot).getItem() == inventory.get(itemSellSlot).getItem()
				&& (hasInventorySpace() && (getOwnerAccountBalance() >= itemPrice || coinSum >= itemPrice)
						|| infiniteMode)) {
			sellButtonActive = true;
		} else
			sellButtonActive = false;
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

	public void onRetrieveButtonsPressed(int buttonClickedID, boolean shiftPressed) {
		if (buttonClickedID <= VendorGUI.idObsidianCoinBtn) {
			// get owner coins
			coinSum = retrieveCoins(coinSum, buttonClickedID, shiftPressed);
			updateCoinsForPurchase();
		} else {
			// get buyer coins
			userCoinSum = retrieveCoins(userCoinSum, buttonClickedID, shiftPressed);
		}
		update();
	}

	public int retrieveCoins(int coinField, int buttonClickedID, boolean shiftPressed) {
		int absoluteButton = 0;
		if (buttonClickedID > 9) {
			absoluteButton = buttonClickedID - 12;
		} else {
			absoluteButton = buttonClickedID - 3;
		}
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
			return coinField;
		if (coinField < UniversalCoins.coinValues[absoluteButton]
				|| (!inventory.get(itemCoinOutputSlot).isEmpty()
						&& inventory.get(itemCoinOutputSlot).getItem() != itemOnButton)
				|| (!inventory.get(itemCoinOutputSlot).isEmpty()
						&& inventory.get(itemCoinOutputSlot).getCount() == 64)) {
			return coinField;
		}
		if (shiftPressed) {
			if (inventory.get(itemCoinOutputSlot).isEmpty()) {
				int amount = coinField / UniversalCoins.coinValues[absoluteButton];
				if (amount >= 64) {
					coinField -= UniversalCoins.coinValues[absoluteButton] * 64;
					inventory.set(itemCoinOutputSlot, new ItemStack(itemOnButton));
					inventory.get(itemCoinOutputSlot).setCount(64);
				} else {
					coinField -= UniversalCoins.coinValues[absoluteButton] * amount;
					inventory.set(itemCoinOutputSlot, new ItemStack(itemOnButton));
					inventory.get(itemCoinOutputSlot).setCount(amount);
				}
			} else {
				int amount = Math.min(coinSum / UniversalCoins.coinValues[absoluteButton],
						inventory.get(itemCoinOutputSlot).getMaxStackSize()
								- inventory.get(itemCoinOutputSlot).getCount());
				inventory.get(itemCoinOutputSlot).grow(amount);
				coinField -= UniversalCoins.coinValues[absoluteButton] * amount;
			}
		} else {
			coinField -= UniversalCoins.coinValues[absoluteButton];
			if (inventory.get(itemCoinOutputSlot).isEmpty()) {
				inventory.set(itemCoinOutputSlot, new ItemStack(itemOnButton));
			} else {
				inventory.get(itemCoinOutputSlot).grow(1);
				;
			}
		}
		return coinField;
	}

	private void activateUserRetrieveButtons() {
		uironCoinBtnActive = false;
		ugoldCoinBtnActive = false;
		uemeraldCoinBtnActive = false;
		udiamondCoinBtnActive = false;
		uobsidianCoinBtnActive = false;
		if (userCoinSum > 0) {
			uironCoinBtnActive = inventory.get(itemCoinOutputSlot).isEmpty()
					|| (inventory.get(itemCoinOutputSlot).getItem() == UniversalCoins.Items.iron_coin
							&& inventory.get(itemCoinOutputSlot).getCount() != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[1]) {
			ugoldCoinBtnActive = inventory.get(itemCoinOutputSlot).isEmpty()
					|| (inventory.get(itemCoinOutputSlot).getItem() == UniversalCoins.Items.gold_coin
							&& inventory.get(itemCoinOutputSlot).getCount() != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[2]) {
			uemeraldCoinBtnActive = inventory.get(itemCoinOutputSlot).isEmpty()
					|| (inventory.get(itemCoinOutputSlot).getItem() == UniversalCoins.Items.emerald_coin
							&& inventory.get(itemCoinOutputSlot).getCount() != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[3]) {
			udiamondCoinBtnActive = inventory.get(itemCoinOutputSlot).isEmpty()
					|| (inventory.get(itemCoinOutputSlot).getItem() == UniversalCoins.Items.diamond_coin
							&& inventory.get(itemCoinOutputSlot).getCount() != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[4]) {
			uobsidianCoinBtnActive = inventory.get(itemCoinOutputSlot).isEmpty()
					|| (inventory.get(itemCoinOutputSlot).getItem() == UniversalCoins.Items.obsidian_coin
							&& inventory.get(itemCoinOutputSlot).getCount() != 64);
		}
	}

	public void onBuyPressed() {
		onBuyPressed(1);
	}

	public void onBuyPressed(int amount) {
		boolean useCard = false;
		// use the card if we have it
		if (!inventory.get(itemUserCardSlot).isEmpty() && getUserAccountBalance() > itemPrice * amount) {
			useCard = true;
		}
		if (!useCard && (inventory.get(itemTradeSlot).isEmpty() || userCoinSum < itemPrice * amount)) {
			buyButtonActive = false;
			return;
		}
		int totalSale = inventory.get(itemTradeSlot).getCount() * amount;
		if (!inventory.get(itemOutputSlot).isEmpty() && inventory.get(itemOutputSlot).getCount() + totalSale > inventory
				.get(itemTradeSlot).getMaxStackSize()) {
			buyButtonActive = false;
			return;
		}
		if (infiniteMode) {
			if (inventory.get(itemOutputSlot).isEmpty()) {
				inventory.set(itemOutputSlot, inventory.get(itemTradeSlot).copy());
				inventory.get(itemOutputSlot).setCount(totalSale);
				if (useCard && !inventory.get(itemUserCardSlot).isEmpty()) {
					debitUserAccount(itemPrice * amount);
				} else {
					userCoinSum -= itemPrice * amount;
				}
			} else {
				totalSale = Math.min(inventory.get(itemTradeSlot).getCount() * amount,
						inventory.get(itemTradeSlot).getMaxStackSize() - inventory.get(itemOutputSlot).getCount());
				inventory.get(itemOutputSlot).grow(totalSale);
				if (useCard && !inventory.get(itemUserCardSlot).isEmpty()) {
					debitUserAccount(itemPrice * amount);
				} else {
					userCoinSum -= itemPrice * amount;
				}
			}
			if (infiniteMode) {
				coinSum = 0;
			} else {
				if (!inventory.get(itemCardSlot).isEmpty()
						&& inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card
						&& getOwnerAccountBalance() != -1
						&& getOwnerAccountBalance() + (itemPrice * amount) < Integer.MAX_VALUE) {
					creditOwnerAccount(itemPrice * amount);
				} else {
					coinSum += itemPrice * amount;
				}
			}
		} else {
			// find matching item in inventory
			// we need to match the item, damage, and tags to make sure the
			// stacks are equal
			for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
				if (!inventory.get(i).isEmpty() && inventory.get(i).getItem() == inventory.get(itemTradeSlot).getItem()
						&& inventory.get(i).getItemDamage() == inventory.get(itemTradeSlot).getItemDamage()
						&& ItemStack.areItemStackTagsEqual(inventory.get(i), inventory.get(itemTradeSlot))) {
					// copy itemstack if null. We'll set the amount to 0 to
					// start.
					if (inventory.get(itemOutputSlot).isEmpty()) {
						inventory.set(itemOutputSlot, inventory.get(i).copy());
						inventory.get(itemOutputSlot).setCount(0);
					}
					int thisSale = Math.min(inventory.get(i).getCount(), totalSale);
					inventory.get(itemOutputSlot).grow(thisSale);
					inventory.get(i).shrink(thisSale);
					totalSale -= thisSale;
					if (useCard && inventory.get(itemCardSlot).isEmpty()) {
						debitUserAccount(itemPrice * thisSale / inventory.get(itemTradeSlot).getCount());
					} else {
						userCoinSum -= itemPrice * thisSale / inventory.get(itemTradeSlot).getCount();
					}
					if (infiniteMode) {
						coinSum = 0;
					} else {
						if (!inventory.get(itemCardSlot).isEmpty()
								&& inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card
								&& getOwnerAccountBalance() != -1
								&& getOwnerAccountBalance() + (itemPrice * amount) < Integer.MAX_VALUE) {
							creditOwnerAccount(itemPrice * thisSale / inventory.get(itemTradeSlot).getCount());
						} else {
							coinSum += itemPrice * thisSale / inventory.get(itemTradeSlot).getCount();
						}
					}
				}
				// cleanup empty stacks
				if (inventory.get(i).isEmpty() || inventory.get(i).getCount() == 0) {
					inventory.set(i, ItemStack.EMPTY);
				}
			}
		}
		checkSellingInventory();
		update();
	}

	public void onBuyMaxPressed() {
		boolean useCard = false;
		int amount = 0;
		if (inventory.get(itemTradeSlot).isEmpty()) {
			buyButtonActive = false;
			return;
		}
		// use the card if we have it
		if (!inventory.get(itemUserCardSlot).isEmpty() && getUserAccountBalance() > itemPrice) {
			useCard = true;
		}
		if (userCoinSum < itemPrice && !useCard) { // can't buy even one
			buyButtonActive = false;
			return;
		}
		if (inventory.get(itemOutputSlot).isEmpty()) { // empty stack
			if (inventory.get(itemTradeSlot).getMaxStackSize() * itemPrice
					/ inventory.get(itemTradeSlot).getCount() <= (useCard ? getUserAccountBalance() : userCoinSum)) {
				// buy as many as will fit in a stack
				amount = inventory.get(itemTradeSlot).getMaxStackSize() / inventory.get(itemTradeSlot).getCount();
			} else {
				// buy as many as i have coins for.
				if (useCard) {
					amount = (int) (getUserAccountBalance() > itemPrice * 64 ? 64
							: getUserAccountBalance() / itemPrice);
				} else {
					amount = userCoinSum / itemPrice;
				}
			}
		} else if (inventory.get(itemOutputSlot).getItem() == inventory.get(itemTradeSlot).getItem()
				&& inventory.get(itemOutputSlot).getItemDamage() == inventory.get(itemTradeSlot).getItemDamage()
				&& ItemStack.areItemStackTagsEqual(inventory.get(itemOutputSlot), inventory.get(itemTradeSlot))
				&& inventory.get(itemOutputSlot).getCount() < inventory.get(itemTradeSlot).getMaxStackSize()) {
			if ((inventory.get(itemOutputSlot).getMaxStackSize() - inventory.get(itemOutputSlot).getCount())
					* itemPrice <= userCoinSum) {
				// buy as much as i can fit in a stack since we have enough
				// coins
				amount = (inventory.get(itemTradeSlot).getMaxStackSize() - inventory.get(itemOutputSlot).getCount())
						/ inventory.get(itemOutputSlot).getCount();
			} else {
				if (useCard) {
					amount = (int) (getUserAccountBalance() > itemPrice * 64 ? 64
							: getUserAccountBalance() / itemPrice);
				} else {
					amount = userCoinSum / itemPrice;
				}
			}
		} else {
			buyButtonActive = false;
		}
		onBuyPressed(amount);
	}

	public void onSellPressed() {
		onSellPressed(1);
	}

	public void onSellMaxPressed() {
		onSellPressed(inventory.get(itemSellSlot).getCount());
	}

	public void onSellPressed(int amount) {
		boolean useCard = false;
		// if infinite mode, we can handle it here and skip the complicated
		// stuff
		if (infiniteMode) {
			if (!inventory.get(itemUserCardSlot).isEmpty()
					&& inventory.get(itemUserCardSlot).getItem() == UniversalCoins.Items.ender_card
					&& getUserAccountBalance() != -1 && getUserAccountBalance()
							+ (itemPrice * amount / inventory.get(itemTradeSlot).getCount()) < Integer.MAX_VALUE) {
				creditUserAccount(itemPrice * amount / inventory.get(itemTradeSlot).getCount());
			} else {
				userCoinSum += itemPrice * amount / inventory.get(itemTradeSlot).getCount();
			}
			inventory.get(itemSellSlot).shrink(amount);
			if (inventory.get(itemSellSlot).getCount() == 0) {
				inventory.set(itemSellSlot, ItemStack.EMPTY);
			}
			return;
		}
		// use the card if we have it
		if (!inventory.get(itemCardSlot).isEmpty() && getOwnerAccountBalance() > itemPrice) {
			useCard = true;
		}
		if (inventory.get(itemSellSlot).isEmpty() || coinSum < itemPrice && !useCard) {
			sellButtonActive = false;
			return;
		}
		// adjust the amount to the lesser of max the available coins will buy
		// or the amount requested
		if (useCard) {
			amount = (int) Math.min(getOwnerAccountBalance() / (itemPrice * inventory.get(itemTradeSlot).getCount()),
					amount);
		} else {
			amount = Math.min(coinSum / (itemPrice * inventory.get(itemTradeSlot).getCount()), amount);
		}
		// find empty slot or matching item in inventory
		// we need to match the item, damage, and tags to make sure the
		// stacks are equal
		for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
			if (!inventory.get(itemSellSlot).isEmpty()) {
				// copy itemstack if null. We'll set the amount to 0 to start.
				int thisSale = 0;
				if (!inventory.get(i).isEmpty() && inventory.get(i).getItem() == inventory.get(itemTradeSlot).getItem()
						&& inventory.get(i).getItemDamage() == inventory.get(itemTradeSlot).getItemDamage()
						&& ItemStack.areItemStackTagsEqual(inventory.get(i), inventory.get(itemTradeSlot))) {
					thisSale = Math.min(inventory.get(i).getMaxStackSize() - inventory.get(i).getCount(), amount);
					inventory.get(i).grow(thisSale);
					inventory.get(itemSellSlot).shrink(thisSale);
					amount -= thisSale;
				} else if (inventory.get(i).isEmpty()) {
					thisSale = Math.min(inventory.get(itemSellSlot).getCount(), amount);
					inventory.set(i, inventory.get(itemSellSlot).copy());
					inventory.get(i).setCount(thisSale);
					inventory.get(itemSellSlot).shrink(thisSale);
					amount -= thisSale;
				}
				if (useCard && !inventory.get(itemCardSlot).isEmpty()) {
					debitOwnerAccount(itemPrice * thisSale / inventory.get(itemTradeSlot).getCount());
				} else {
					coinSum -= itemPrice * thisSale / inventory.get(itemTradeSlot).getCount();
				}
				if (!inventory.get(itemUserCardSlot).isEmpty()
						&& inventory.get(itemUserCardSlot).getItem() instanceof ItemEnderCard
						&& getUserAccountBalance() != -1 && getUserAccountBalance() + (itemPrice * thisSale
								/ inventory.get(itemTradeSlot).getCount()) < Integer.MAX_VALUE) {
					creditUserAccount(itemPrice * thisSale / inventory.get(itemTradeSlot).getCount());
				} else {
					userCoinSum += itemPrice * thisSale / inventory.get(itemTradeSlot).getCount();
				}
				// cleanup empty stacks
				if (inventory.get(itemSellSlot).isEmpty() || inventory.get(itemSellSlot).getCount() == 0) {
					inventory.set(itemSellSlot, ItemStack.EMPTY);
				}
				if (amount == 0) {
					updateCoinsForPurchase(); // we bought stuff. Make sure we
												// have coins left.
					return; // we are done here. exit the loop.
				}
			}
		}
		update();
	}

	public void onModeButtonPressed() {
		sellMode ^= true;
		updateSigns();
	}

	public void checkSellingInventory() {
		for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
			if (!inventory.get(i).isEmpty() && !inventory.get(itemTradeSlot).isEmpty()
					&& inventory.get(i).getItem() == inventory.get(itemTradeSlot).getItem()) {
				if (ooStockWarning) {
					this.ooStockWarning = false;
					updateSigns();
				}
				return;
			}
		}
		if (!ooStockWarning) {
			this.ooStockWarning = true; // if we reach this point, we are OOS.
			updateSigns();
		}
	}

	public boolean hasInventorySpace() {
		if (!inventory.get(itemTradeSlot).isEmpty()) {
			for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
				if (inventory.get(i).isEmpty() || (inventory.get(i).getItem() == inventory.get(itemTradeSlot).getItem()
						&& inventory.get(i).getCount() < inventory.get(i).getMaxStackSize())) {
					if (inventoryFullWarning) {
						this.inventoryFullWarning = false;
						updateSigns();
					}
					return true;
				}
			}
			if (!inventoryFullWarning) {
				this.inventoryFullWarning = true; // if we reach this point, we
													// have no space left.
				updateSigns();
			}
			return false;
		} else
			return true;
	}

	public void updateCoinsForPurchase() {
		if (coinSum >= itemPrice || (!inventory.get(itemCardSlot).isEmpty() && getOwnerAccountBalance() >= itemPrice)) {
			this.ooCoinsWarning = false;
			updateSigns();
		} else {
			this.ooCoinsWarning = true;
			updateSigns();
		}
	}

	public ItemStack getSellItem() {
		return inventory.get(itemTradeSlot);
	}

	public void setSellItem(ItemStack stack) {
		inventory.set(itemTradeSlot, stack);
	}

	@Override
	public int getSizeInventory() {
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i >= inventory.size()) {
			return ItemStack.EMPTY;
		}
		return inventory.get(i);
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (size < stack.getCount()) {
			stack = stack.splitStack(size);
		} else {
			setInventorySlotContents(slot, ItemStack.EMPTY);
		}
		if (slot < itemStorageSlot9) { // update inventory status
			checkSellingInventory();
			hasInventorySpace();
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
		if (stack.isEmpty()) return;
		inventory.set(slot, stack);
		int coinValue = CoinUtils.getCoinValue(stack);

		int depositAmount = 0;
		if (slot == itemCoinInputSlot) {
			depositAmount = Math.min(stack.getCount(), (Integer.MAX_VALUE - coinSum) / coinValue);
			if (!inventory.get(itemCardSlot).isEmpty() && inventory.get(itemCardSlot).hasTagCompound()
					&& inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card
					&& getOwnerAccountBalance() != -1
					&& getOwnerAccountBalance() + (depositAmount * coinValue) < Integer.MAX_VALUE) {
				creditOwnerAccount(depositAmount * coinValue);
			} else {
				coinSum += depositAmount * coinValue;
			}
			updateCoinsForPurchase();
		}
		if (slot == itemUserCoinInputSlot) {
			depositAmount = Math.min(stack.getCount(), (Integer.MAX_VALUE - userCoinSum) / coinValue);
			if (!inventory.get(itemUserCardSlot).isEmpty() && inventory.get(itemUserCardSlot).hasTagCompound()
					&& inventory.get(itemUserCardSlot).getItem() == UniversalCoins.Items.ender_card
					&& getUserAccountBalance() != -1
					&& getUserAccountBalance() + (depositAmount * coinValue) < Integer.MAX_VALUE) {
				creditUserAccount(depositAmount * coinValue);
			} else {
				userCoinSum += depositAmount * coinValue;
			}
		}
		inventory.get(slot).shrink(depositAmount);
		if (inventory.get(slot).getCount() == 0) {
			inventory.set(slot, ItemStack.EMPTY);
		}
		if (slot == itemCardSlot) {
			updateCoinsForPurchase();
		}
		if (slot == itemUserCardSlot && !inventory.get(itemUserCardSlot).isEmpty()
				&& inventory.get(itemUserCardSlot).getItem() instanceof ItemEnderCard && getUserAccountBalance() != -1
				&& getUserAccountBalance() + userCoinSum < Integer.MAX_VALUE) {
			creditUserAccount(userCoinSum);
			userCoinSum = 0;
		}
		checkSellingInventory(); // update inventory status
		hasInventorySpace();
		update();
	}

	public String getName() {
		return UniversalCoins.Blocks.vendor_block.getLocalizedName();
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
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == itemTradeSlot) {
			inventory.set(itemTradeSlot, stack.copy());
			return false;
		} else
			return true;
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

	public void sendButtonMessage(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
	}

	public void sendServerUpdateMessage() {
		UniversalCoins.snw.sendToServer(
				new UCVendorServerMessage(pos.getX(), pos.getY(), pos.getZ(), itemPrice, blockOwner, infiniteMode));
	}

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
			coinSum = tagCompound.getInteger("CoinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			userCoinSum = tagCompound.getInteger("UserCoinSum");
		} catch (Throwable ex2) {
			userCoinSum = 0;
		}
		try {
			itemPrice = tagCompound.getInteger("ItemPrice");
		} catch (Throwable ex2) {
			itemPrice = 0;
		}
		try {
			infiniteMode = tagCompound.getBoolean("Infinite");
		} catch (Throwable ex2) {
			infiniteMode = false;
		}
		try {
			sellMode = tagCompound.getBoolean("Mode");
		} catch (Throwable ex2) {
			sellMode = false;
		}
		try {
			ooStockWarning = tagCompound.getBoolean("OutOfStock");
		} catch (Throwable ex2) {
			ooStockWarning = false;
		}
		try {
			ooCoinsWarning = tagCompound.getBoolean("OutOfCoins");
		} catch (Throwable ex2) {
			ooCoinsWarning = false;
		}
		try {
			inventoryFullWarning = tagCompound.getBoolean("InventoryFull");
		} catch (Throwable ex2) {
			inventoryFullWarning = false;
		}
		try {
			buyButtonActive = tagCompound.getBoolean("BuyButtonActive");
		} catch (Throwable ex2) {
			buyButtonActive = false;
		}
		try {
			sellButtonActive = tagCompound.getBoolean("SellButtonActive");
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
		try {
			uironCoinBtnActive = tagCompound.getBoolean("uironCoinBtnActive");
		} catch (Throwable ex2) {
			uironCoinBtnActive = false;
		}
		try {
			ugoldCoinBtnActive = tagCompound.getBoolean("ugoldCoinBtnActive");
		} catch (Throwable ex2) {
			ugoldCoinBtnActive = false;
		}
		try {
			uemeraldCoinBtnActive = tagCompound.getBoolean("uemeraldCoinBtnActive");
		} catch (Throwable ex2) {
			uemeraldCoinBtnActive = false;
		}
		try {
			udiamondCoinBtnActive = tagCompound.getBoolean("udiamondCoinBtnActive");
		} catch (Throwable ex2) {
			udiamondCoinBtnActive = false;
		}
		try {
			uobsidianCoinBtnActive = tagCompound.getBoolean("uobsidianCoinBtnActive");
		} catch (Throwable ex2) {
			uobsidianCoinBtnActive = false;
		}
		try {
			textColor = tagCompound.getInteger("TextColor");
		} catch (Throwable ex2) {
			textColor = 0x0;
		}
		try {
			remoteX = tagCompound.getInteger("remoteX");
		} catch (Throwable ex2) {
			remoteX = 0;
		}
		try {
			remoteY = tagCompound.getInteger("remoteY");
		} catch (Throwable ex2) {
			remoteY = 0;
		}
		try {
			remoteZ = tagCompound.getInteger("remoteZ");
		} catch (Throwable ex2) {
			remoteZ = 0;
		}
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
		tagCompound.setInteger("CoinSum", coinSum);
		tagCompound.setInteger("UserCoinSum", userCoinSum);
		tagCompound.setInteger("ItemPrice", itemPrice);
		tagCompound.setBoolean("Infinite", infiniteMode);
		tagCompound.setBoolean("Mode", sellMode);
		tagCompound.setBoolean("OutOfStock", ooStockWarning);
		tagCompound.setBoolean("OutOfCoins", ooCoinsWarning);
		tagCompound.setBoolean("InventoryFull", inventoryFullWarning);
		tagCompound.setBoolean("BuyButtonActive", buyButtonActive);
		tagCompound.setBoolean("SellButtonActive", sellButtonActive);
		tagCompound.setBoolean("ironCoinBtnActive", ironCoinBtnActive);
		tagCompound.setBoolean("goldCoinBtnActive", goldCoinBtnActive);
		tagCompound.setBoolean("emeraldCoinBtnActive", emeraldCoinBtnActive);
		tagCompound.setBoolean("diamondCoinBtnActive", diamondCoinBtnActive);
		tagCompound.setBoolean("obsidianCoinBtnActive", obsidianCoinBtnActive);
		tagCompound.setBoolean("uironCoinBtnActive", uironCoinBtnActive);
		tagCompound.setBoolean("ugoldCoinBtnActive", ugoldCoinBtnActive);
		tagCompound.setBoolean("uemeraldCoinBtnActive", uemeraldCoinBtnActive);
		tagCompound.setBoolean("udiamondCoinBtnActive", udiamondCoinBtnActive);
		tagCompound.setBoolean("uobsidianCoinBtnActive", uobsidianCoinBtnActive);
		tagCompound.setInteger("TextColor", textColor);
		tagCompound.setInteger("remoteX", remoteX);
		tagCompound.setInteger("remoteY", remoteY);
		tagCompound.setInteger("remoteZ", remoteZ);
		return tagCompound;
	}

	public void updateSigns() {
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 11 };
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		// put everything in the item storage slots
		if (index >= itemStorageSlot1 && index <= itemStorageSlot9) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		// allow pulling items from output slot only
		if (index == itemCoinOutputSlot) {
			return true;
		} else {
			return false;
		}
	}

	public void setRemoteStorage(int[] storageLocation) {
		remoteX = storageLocation[0];
		remoteY = storageLocation[1];
		remoteZ = storageLocation[2];
	}

	public void checkRemoteStorage() {
		if (remoteX != 0 && remoteY != 0 && remoteZ != 0 && !inventory.get(itemTradeSlot).isEmpty()) {
			for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
				if (inventory.get(i).isEmpty()) {
					loadRemoteChunk(remoteX, remoteY, remoteZ);
					TileEntity te = world.getTileEntity(new BlockPos(remoteX, remoteY, remoteZ));
					if (te != null && te instanceof TileEntityChest) {
						TileEntityChest chest = (TileEntityChest) te;
						for (int j = 0; j < chest.getSizeInventory(); j++) {
							if (inventory.get(i).isEmpty() && !chest.getStackInSlot(j).isEmpty()
									&& chest.getStackInSlot(j).getItem() == inventory.get(itemTradeSlot).getItem()) {
								inventory.set(i, chest.getStackInSlot(j));
								chest.setInventorySlotContents(j, ItemStack.EMPTY);
								checkSellingInventory();
							}
						}
					}
				}
			}
		}
	}

	public long getUserAccountBalance() {
		if (!inventory.get(itemUserCardSlot).isEmpty() && inventory.get(itemUserCardSlot).hasTagCompound()
				&& !world.isRemote) {
			String accountNumber = inventory.get(itemUserCardSlot).getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
		return -1;
	}

	public void debitUserAccount(int amount) {
		if (!inventory.get(itemUserCardSlot).isEmpty() && inventory.get(itemUserCardSlot).hasTagCompound()
				&& !world.isRemote) {
			String accountNumber = inventory.get(itemUserCardSlot).getTagCompound().getString("Account");
			UniversalAccounts.getInstance().debitAccount(accountNumber, amount, false);
		}
	}

	public void creditUserAccount(int amount) {
		if (!inventory.get(itemUserCardSlot).isEmpty() && inventory.get(itemUserCardSlot).hasTagCompound()
				&& !world.isRemote) {
			String accountNumber = inventory.get(itemUserCardSlot).getTagCompound().getString("Account");
			UniversalAccounts.getInstance().creditAccount(accountNumber, amount, false);
		}
	}

	public long getOwnerAccountBalance() {
		if (!inventory.get(itemCardSlot).isEmpty() && inventory.get(itemCardSlot).hasTagCompound() && !world.isRemote) {
			String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
		return -1;
	}

	public void debitOwnerAccount(int amount) {
		if (!inventory.get(itemCardSlot).isEmpty() && inventory.get(itemCardSlot).hasTagCompound() && !world.isRemote) {
			String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			UniversalAccounts.getInstance().debitAccount(accountNumber, amount, false);
		}
	}

	public void creditOwnerAccount(int amount) {
		if (!inventory.get(itemCardSlot).isEmpty() && inventory.get(itemCardSlot).hasTagCompound() && !world.isRemote) {
			String accountNumber = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			UniversalAccounts.getInstance().creditAccount(accountNumber, amount, false);
		}
	}

	private void loadRemoteChunk(int x, int y, int z) {
		Chunk ch = world.getChunkFromChunkCoords(x, y);
		world.getChunkProvider().provideChunk(ch.x, ch.z);
	}

	public void onButtonPressed(int buttonId, boolean shiftPressed) {
		if (buttonId == VendorGUI.idModeButton) {
			onModeButtonPressed();
		} else if (buttonId >= VendorGUI.idIronCoinBtn && buttonId <= VendorGUI.idObsidianCoinBtn) {
			onRetrieveButtonsPressed(buttonId, shiftPressed);
		} else if (buttonId == VendorGUI.idtcmButton) {
			if (textColor > 0)
				textColor--;
		} else if (buttonId == VendorGUI.idtcpButton) {
			if (textColor < 15)
				textColor++;
		} else if (buttonId == VendorBuyGUI.idSellButton) {
			if (shiftPressed) {
				onSellMaxPressed();
			} else {
				onSellPressed();
			}
		} else if (buttonId == VendorSellGUI.idBuyButton) {
			if (shiftPressed) {
				onBuyMaxPressed();
			} else {
				onBuyPressed();
			}
		} else if (buttonId >= VendorBuyGUI.idIronCoinBtn && buttonId <= VendorBuyGUI.idObsidianCoinBtn) {
			onRetrieveButtonsPressed(buttonId, shiftPressed);
		}
		update();
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.Blocks.vendor_block.getLocalizedName());
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
	public ItemStack removeStackFromSlot(int index) {
		return inventory.get(index);
	}
}
