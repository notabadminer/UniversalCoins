package universalcoins.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.gui.TradeStationGUI;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.items.ItemEnderCard;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCVendorServerMessage;
import universalcoins.util.UniversalAccounts;

public class TileVendor extends TileProtected implements IInventory, ISidedInventory {

	protected ItemStack[] inventory = new ItemStack[17];
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

	public void updateEntity() {
		if (!worldObj.isRemote) {
			activateRetrieveButtons();
			activateUserRetrieveButtons();
			activateBuyButton();
			activateSellButton();
			if (ooStockWarning)
				checkRemoteStorage();
		}
	}

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
		inUse = false;
	}

	private void activateBuyButton() {
		if ((userCoinSum >= itemPrice && coinSum + itemPrice < Integer.MAX_VALUE && (!ooStockWarning || infiniteMode))
				|| (inventory[itemUserCardSlot] != null && getUserAccountBalance() > itemPrice
						&& (!ooStockWarning || infiniteMode))) {
			if (inventory[itemOutputSlot] != null) {
				if (inventory[itemOutputSlot].getMaxStackSize() == inventory[itemOutputSlot].stackSize) {
					buyButtonActive = false;
					return;
				}
			}
			buyButtonActive = true;
		} else
			buyButtonActive = false;
	}

	private void activateSellButton() {
		if (inventory[itemSellSlot] != null && inventory[itemTradeSlot].getItem() == inventory[itemSellSlot].getItem()
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

	public void onRetrieveButtonsPressed(int buttonClickedID, boolean shiftPressed) {
		if (buttonClickedID <= VendorGUI.idObsidianCoinBtn) {
			// get owner coins
			coinSum = retrieveCoins(coinSum, buttonClickedID, shiftPressed);
			updateCoinsForPurchase();
		} else {
			// get buyer coins
			userCoinSum = retrieveCoins(userCoinSum, buttonClickedID, shiftPressed);
		}
		updateEntity();
	}

	public int retrieveCoins(int coinField, int buttonClickedID, boolean shiftPressed) {
		if (buttonClickedID > 9)
			buttonClickedID -= 10;
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
			return coinField;
		if (coinField < UniversalCoins.coinValues[absoluteButton]
				|| (inventory[itemOutputSlot] != null && inventory[itemOutputSlot].getItem() != itemOnButton)
				|| (inventory[itemOutputSlot] != null && inventory[itemOutputSlot].stackSize == 64)) {
			return coinField;
		}
		if (shiftPressed) {
			if (inventory[itemOutputSlot] == null) {
				int amount = coinField / UniversalCoins.coinValues[absoluteButton];
				if (amount >= 64) {
					coinField -= UniversalCoins.coinValues[absoluteButton] * 64;
					inventory[itemOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemOutputSlot].stackSize = 64;
				} else {
					coinField -= UniversalCoins.coinValues[absoluteButton] * amount;
					inventory[itemOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemOutputSlot].stackSize = amount;
				}
			} else {
				int amount = Math.min(coinSum / UniversalCoins.coinValues[absoluteButton],
						inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize);
				inventory[itemOutputSlot].stackSize += amount;
				coinField -= UniversalCoins.coinValues[absoluteButton] * amount;
			}
		} else {
			coinField -= UniversalCoins.coinValues[absoluteButton];
			if (inventory[itemOutputSlot] == null) {
				inventory[itemOutputSlot] = new ItemStack(itemOnButton);
			} else {
				inventory[itemOutputSlot].stackSize++;
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
			uironCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.iron_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[1]) {
			ugoldCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.gold_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[2]) {
			uemeraldCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.emerald_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[3]) {
			udiamondCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.diamond_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[4]) {
			uobsidianCoinBtnActive = inventory[itemOutputSlot] == null
					|| (inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.obsidian_coin
							&& inventory[itemOutputSlot].stackSize != 64);
		}
	}

	public void onBuyPressed() {
		onBuyPressed(1);
	}

	public void onBuyPressed(int amount) {
		boolean useCard = false;
		// use the card if we have it
		if (inventory[itemUserCardSlot] != null && getUserAccountBalance() > itemPrice * amount) {
			useCard = true;
		}
		if (!useCard && (inventory[itemTradeSlot] == null || userCoinSum < itemPrice * amount)) {
			buyButtonActive = false;
			return;
		}
		int totalSale = inventory[itemTradeSlot].stackSize * amount;
		if (inventory[itemOutputSlot] != null
				&& inventory[itemOutputSlot].stackSize + totalSale > inventory[itemTradeSlot].getMaxStackSize()) {
			buyButtonActive = false;
			return;
		}
		if (infiniteMode) {
			if (inventory[itemOutputSlot] == null) {
				inventory[itemOutputSlot] = inventory[itemTradeSlot].copy();
				inventory[itemOutputSlot].stackSize = totalSale;
				if (useCard && inventory[itemUserCardSlot] != null) {
					debitUserAccount(itemPrice * amount);
				} else {
					userCoinSum -= itemPrice * amount;
				}
			} else {
				totalSale = Math.min(inventory[itemTradeSlot].stackSize * amount,
						inventory[itemTradeSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize);
				inventory[itemOutputSlot].stackSize += totalSale;
				if (useCard && inventory[itemUserCardSlot] != null) {
					debitUserAccount(itemPrice * amount);
				} else {
					userCoinSum -= itemPrice * amount;
				}
			}
			if (infiniteMode) {
				coinSum = 0;
			} else {
				if (inventory[itemCardSlot] != null
						&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.ender_card
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
				if (inventory[i] != null && inventory[i].getItem() == inventory[itemTradeSlot].getItem()
						&& inventory[i].getItemDamage() == inventory[itemTradeSlot].getItemDamage()
						&& ItemStack.areItemStackTagsEqual(inventory[i], inventory[itemTradeSlot])) {
					// copy itemstack if null. We'll set the amount to 0 to
					// start.
					if (inventory[itemOutputSlot] == null) {
						inventory[itemOutputSlot] = inventory[i].copy();
						inventory[itemOutputSlot].stackSize = 0;
					}
					int thisSale = Math.min(inventory[i].stackSize, totalSale);
					inventory[itemOutputSlot].stackSize += thisSale;
					inventory[i].stackSize -= thisSale;
					totalSale -= thisSale;
					if (useCard && inventory[itemCardSlot] == null) {
						debitUserAccount(itemPrice * thisSale / inventory[itemTradeSlot].stackSize);
					} else {
						userCoinSum -= itemPrice * thisSale / inventory[itemTradeSlot].stackSize;
					}
					if (infiniteMode) {
						coinSum = 0;
					} else {
						if (inventory[itemCardSlot] != null
								&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.ender_card
								&& getOwnerAccountBalance() != -1
								&& getOwnerAccountBalance() + (itemPrice * amount) < Integer.MAX_VALUE) {
							creditOwnerAccount(itemPrice * thisSale / inventory[itemTradeSlot].stackSize);
						} else {
							coinSum += itemPrice * thisSale / inventory[itemTradeSlot].stackSize;
						}
					}
				}
				// cleanup empty stacks
				if (inventory[i] == null || inventory[i].stackSize == 0) {
					inventory[i] = null;
				}
			}
		}
		checkSellingInventory();
		updateEntity();
	}

	public void onBuyMaxPressed() {
		boolean useCard = false;
		int amount = 0;
		if (inventory[itemTradeSlot] == null) {
			buyButtonActive = false;
			return;
		}
		// use the card if we have it
		if (inventory[itemUserCardSlot] != null && getUserAccountBalance() > itemPrice) {
			useCard = true;
		}
		if (userCoinSum < itemPrice && !useCard) { // can't buy even one
			buyButtonActive = false;
			return;
		}
		if (inventory[itemOutputSlot] == null) { // empty stack
			if (inventory[itemTradeSlot].getMaxStackSize() * itemPrice
					/ inventory[itemTradeSlot].stackSize <= (useCard ? getUserAccountBalance() : userCoinSum)) {
				// buy as many as will fit in a stack
				amount = inventory[itemTradeSlot].getMaxStackSize() / inventory[itemTradeSlot].stackSize;
			} else {
				// buy as many as i have coins for.
				if (useCard) {
					amount = (int) (getUserAccountBalance() > itemPrice * 64 ? 64
							: getUserAccountBalance() / itemPrice);
				} else {
					amount = userCoinSum / itemPrice;
				}
			}
		} else if (inventory[itemOutputSlot].getItem() == inventory[itemTradeSlot].getItem()
				&& inventory[itemOutputSlot].getItemDamage() == inventory[itemTradeSlot].getItemDamage()
				&& ItemStack.areItemStackTagsEqual(inventory[itemOutputSlot], inventory[itemTradeSlot])
				&& inventory[itemOutputSlot].stackSize < inventory[itemTradeSlot].getMaxStackSize()) {
			if ((inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize)
					* itemPrice <= userCoinSum) {
				// buy as much as i can fit in a stack since we have enough
				// coins
				amount = (inventory[itemTradeSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize)
						/ inventory[itemOutputSlot].stackSize;
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
		onSellPressed(inventory[itemSellSlot].stackSize);
	}

	public void onSellPressed(int amount) {
		boolean useCard = false;
		// if infinite mode, we can handle it here and skip the complicated
		// stuff
		if (infiniteMode) {
			if (inventory[itemUserCardSlot] != null
					&& inventory[itemUserCardSlot].getItem() == UniversalCoins.proxy.ender_card
					&& getUserAccountBalance() != -1 && getUserAccountBalance()
							+ (itemPrice * amount / inventory[itemTradeSlot].stackSize) < Integer.MAX_VALUE) {
				creditUserAccount(itemPrice * amount / inventory[itemTradeSlot].stackSize);
			} else {
				userCoinSum += itemPrice * amount / inventory[itemTradeSlot].stackSize;
			}
			inventory[itemSellSlot].stackSize -= amount;
			if (inventory[itemSellSlot].stackSize == 0) {
				inventory[itemSellSlot] = null;
			}
			return;
		}
		// use the card if we have it
		if (inventory[itemCardSlot] != null && getOwnerAccountBalance() > itemPrice) {
			useCard = true;
		}
		if (inventory[itemSellSlot] == null || coinSum < itemPrice && !useCard) {
			sellButtonActive = false;
			return;
		}
		// adjust the amount to the lesser of max the available coins will buy
		// or the amount requested
		if (useCard) {
			amount = (int) Math.min(getOwnerAccountBalance() / (itemPrice * inventory[itemTradeSlot].stackSize),
					amount);
		} else {
			amount = Math.min(coinSum / (itemPrice * inventory[itemTradeSlot].stackSize), amount);
		}
		// find empty slot or matching item in inventory
		// we need to match the item, damage, and tags to make sure the
		// stacks are equal
		for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
			if (inventory[itemSellSlot] != null) {
				// copy itemstack if null. We'll set the amount to 0 to start.
				int thisSale = 0;
				if (inventory[i] != null && inventory[i].getItem() == inventory[itemTradeSlot].getItem()
						&& inventory[i].getItemDamage() == inventory[itemTradeSlot].getItemDamage()
						&& ItemStack.areItemStackTagsEqual(inventory[i], inventory[itemTradeSlot])) {
					thisSale = Math.min(inventory[i].getMaxStackSize() - inventory[i].stackSize, amount);
					inventory[i].stackSize += thisSale;
					inventory[itemSellSlot].stackSize -= thisSale;
					amount -= thisSale;
				} else if (inventory[i] == null) {
					thisSale = Math.min(inventory[itemSellSlot].stackSize, amount);
					inventory[i] = inventory[itemSellSlot].copy();
					inventory[i].stackSize = thisSale;
					inventory[itemSellSlot].stackSize -= thisSale;
					amount -= thisSale;
				}
				if (useCard && inventory[itemCardSlot] != null) {
					debitOwnerAccount(itemPrice * thisSale / inventory[itemTradeSlot].stackSize);
				} else {
					coinSum -= itemPrice * thisSale / inventory[itemTradeSlot].stackSize;
				}
				if (inventory[itemUserCardSlot] != null
						&& inventory[itemUserCardSlot].getItem() instanceof ItemEnderCard
						&& getUserAccountBalance() != -1 && getUserAccountBalance()
								+ (itemPrice * thisSale / inventory[itemTradeSlot].stackSize) < Integer.MAX_VALUE) {
					creditUserAccount(itemPrice * thisSale / inventory[itemTradeSlot].stackSize);
				} else {
					userCoinSum += itemPrice * thisSale / inventory[itemTradeSlot].stackSize;
				}
				// cleanup empty stacks
				if (inventory[itemSellSlot] == null || inventory[itemSellSlot].stackSize == 0) {
					inventory[itemSellSlot] = null;
				}
				if (amount == 0) {
					updateCoinsForPurchase(); // we bought stuff. Make sure we
												// have coins left.
					return; // we are done here. exit the loop.
				}
			}
		}
		updateEntity();
	}

	public void onModeButtonPressed() {
		sellMode ^= true;
		updateSigns();
	}

	public void checkSellingInventory() {
		for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
			if (inventory[i] != null && inventory[itemTradeSlot] != null
					&& inventory[i].getItem() == inventory[itemTradeSlot].getItem()) {
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
		if (inventory[itemTradeSlot] != null) {
			for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
				if (inventory[i] == null || (inventory[i].getItem() == inventory[itemTradeSlot].getItem()
						&& inventory[i].stackSize < inventory[i].getMaxStackSize())) {
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
		if (coinSum >= itemPrice || (inventory[itemCardSlot] != null && getOwnerAccountBalance() >= itemPrice)) {
			this.ooCoinsWarning = false;
			updateSigns();
		} else {
			this.ooCoinsWarning = true;
			updateSigns();
		}
	}

	public ItemStack getSellItem() {
		return inventory[itemTradeSlot];
	}

	public void setSellItem(ItemStack stack) {
		inventory[itemTradeSlot] = stack;
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
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
		if (slot < itemStorageSlot9) { // update inventory status
			checkSellingInventory();
			hasInventorySpace();
		}
		updateEntity();
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
		if (stack != null) {
			if (slot == itemCoinInputSlot || slot == itemUserCoinInputSlot) {
				int coinValue = 0;
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
				int depositAmount = 0;
				if (slot == itemCoinInputSlot) {
					depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - coinSum) / coinValue);
					if (inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound()
							&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.ender_card
							&& getOwnerAccountBalance() != -1
							&& getOwnerAccountBalance() + (depositAmount * coinValue) < Integer.MAX_VALUE) {
						creditOwnerAccount(depositAmount * coinValue);
					} else {
						coinSum += depositAmount * coinValue;
					}
					updateCoinsForPurchase();
				} else {
					depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - userCoinSum) / coinValue);
					if (inventory[itemUserCardSlot] != null && inventory[itemUserCardSlot].hasTagCompound()
							&& inventory[itemUserCardSlot].getItem() == UniversalCoins.proxy.ender_card
							&& getUserAccountBalance() != -1
							&& getUserAccountBalance() + (depositAmount * coinValue) < Integer.MAX_VALUE) {
						creditUserAccount(depositAmount * coinValue);
					} else {
						userCoinSum += depositAmount * coinValue;
					}
				}
				inventory[slot].stackSize -= depositAmount;
				if (inventory[slot].stackSize == 0) {
					inventory[slot] = null;
				}
			}
		}
		if (slot == itemCardSlot) {
			updateCoinsForPurchase();
		}
		if (slot == itemUserCardSlot && inventory[itemUserCardSlot] != null
				&& inventory[itemUserCardSlot].getItem() instanceof ItemEnderCard && getUserAccountBalance() != -1
				&& getUserAccountBalance() + userCoinSum < Integer.MAX_VALUE) {
			creditUserAccount(userCoinSum);
			userCoinSum = 0;
		}
		checkSellingInventory(); // update inventory status
		hasInventorySpace();
		updateEntity();
	}

	public String getName() {
		return null;
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == itemTradeSlot) {
			inventory[itemTradeSlot] = stack.copy();
			return false;
		} else
			return true;
	}

	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new SPacketUpdateTileEntity(pos, 1, nbt);
	}

	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public void updateTE() {
		markDirty();
		worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
	}

	public void sendButtonMessage(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
	}

	public void sendServerUpdateMessage() {
		FMLLog.info("sending server update");
		UniversalCoins.snw.sendToServer(
				new UCVendorServerMessage(pos.getX(), pos.getY(), pos.getZ(), itemPrice, blockOwner, infiniteMode));
	}

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
			ironCoinBtnActive = tagCompound.getBoolean("CoinButtonActive");
		} catch (Throwable ex2) {
			ironCoinBtnActive = false;
		}
		try {
			goldCoinBtnActive = tagCompound.getBoolean("SmallStackButtonActive");
		} catch (Throwable ex2) {
			goldCoinBtnActive = false;
		}
		try {
			emeraldCoinBtnActive = tagCompound.getBoolean("LargeStackButtonActive");
		} catch (Throwable ex2) {
			emeraldCoinBtnActive = false;
		}
		try {
			diamondCoinBtnActive = tagCompound.getBoolean("SmallBagButtonActive");
		} catch (Throwable ex2) {
			diamondCoinBtnActive = false;
		}
		try {
			obsidianCoinBtnActive = tagCompound.getBoolean("LargeBagButtonActive");
		} catch (Throwable ex2) {
			obsidianCoinBtnActive = false;
		}
		try {
			uironCoinBtnActive = tagCompound.getBoolean("UserCoinButtonActive");
		} catch (Throwable ex2) {
			uironCoinBtnActive = false;
		}
		try {
			ugoldCoinBtnActive = tagCompound.getBoolean("UserSmallStackButtonActive");
		} catch (Throwable ex2) {
			ugoldCoinBtnActive = false;
		}
		try {
			uemeraldCoinBtnActive = tagCompound.getBoolean("UserLargeStackButtonActive");
		} catch (Throwable ex2) {
			uemeraldCoinBtnActive = false;
		}
		try {
			udiamondCoinBtnActive = tagCompound.getBoolean("UserSmallBagButtonActive");
		} catch (Throwable ex2) {
			udiamondCoinBtnActive = false;
		}
		try {
			uobsidianCoinBtnActive = tagCompound.getBoolean("UserLargeBagButtonActive");
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
		tagCompound.setBoolean("CoinButtonActive", ironCoinBtnActive);
		tagCompound.setBoolean("SmallStackButtonActive", goldCoinBtnActive);
		tagCompound.setBoolean("LargeStackButtonActive", emeraldCoinBtnActive);
		tagCompound.setBoolean("SmallBagButtonActive", diamondCoinBtnActive);
		tagCompound.setBoolean("LargeBagButtonActive", obsidianCoinBtnActive);
		tagCompound.setBoolean("UserCoinButtonActive", uironCoinBtnActive);
		tagCompound.setBoolean("UserSmallStackButtonActive", ugoldCoinBtnActive);
		tagCompound.setBoolean("UserLargeStackButtonActive", uemeraldCoinBtnActive);
		tagCompound.setBoolean("UserSmallBagButtonActive", udiamondCoinBtnActive);
		tagCompound.setBoolean("UserLargeBagButtonActive", uobsidianCoinBtnActive);
		tagCompound.setInteger("TextColor", textColor);
		tagCompound.setInteger("remoteX", remoteX);
		tagCompound.setInteger("remoteY", remoteY);
		tagCompound.setInteger("remoteZ", remoteZ);
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
		if (remoteX != 0 && remoteY != 0 && remoteZ != 0 && inventory[itemTradeSlot] != null) {
			for (int i = itemStorageSlot1; i <= itemStorageSlot9; i++) {
				if (inventory[i] == null) {
					loadRemoteChunk(remoteX, remoteY, remoteZ);
					TileEntity te = worldObj.getTileEntity(new BlockPos(remoteX, remoteY, remoteZ));
					if (te != null && te instanceof TileEntityChest) {
						TileEntityChest chest = (TileEntityChest) te;
						for (int j = 0; j < chest.getSizeInventory(); j++) {
							if (inventory[i] == null && chest.getStackInSlot(j) != null
									&& chest.getStackInSlot(j).getItem() == inventory[itemTradeSlot].getItem()) {
								inventory[i] = chest.getStackInSlot(j);
								chest.setInventorySlotContents(j, null);
								checkSellingInventory();
							}
						}
					}
				}
			}
		}
	}

	public long getUserAccountBalance() {
		if (inventory[itemUserCardSlot] != null && inventory[itemUserCardSlot].hasTagCompound() && !worldObj.isRemote) {
			String accountNumber = inventory[itemUserCardSlot].getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
		return -1;
	}

	public void debitUserAccount(int amount) {
		if (inventory[itemUserCardSlot] != null && inventory[itemUserCardSlot].hasTagCompound() && !worldObj.isRemote) {
			String accountNumber = inventory[itemUserCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().debitAccount(accountNumber, amount);
		}
	}

	public void creditUserAccount(int amount) {
		if (inventory[itemUserCardSlot] != null && inventory[itemUserCardSlot].hasTagCompound() && !worldObj.isRemote) {
			String accountNumber = inventory[itemUserCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().creditAccount(accountNumber, amount);
		}
	}

	public long getOwnerAccountBalance() {
		if (inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound() && !worldObj.isRemote) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		}
		return -1;
	}

	public void debitOwnerAccount(int amount) {
		if (inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound() && !worldObj.isRemote) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().debitAccount(accountNumber, amount);
		}
	}

	public void creditOwnerAccount(int amount) {
		if (inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound() && !worldObj.isRemote) {
			String accountNumber = inventory[itemCardSlot].getTagCompound().getString("Account");
			UniversalAccounts.getInstance().creditAccount(accountNumber, amount);
		}
	}

	private void loadRemoteChunk(int x, int y, int z) {
		Chunk ch = worldObj.getChunkFromChunkCoords(x, y);
		worldObj.getChunkProvider().provideChunk(ch.xPosition, ch.zPosition);
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
		updateEntity();
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.proxy.tradestation.getLocalizedName());
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		// TODO Auto-generated method stub
		return inventory[index];
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
}
