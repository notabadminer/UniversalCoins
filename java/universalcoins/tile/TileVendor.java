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
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.items.ItemEnderCard;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCVendorServerMessage;
import universalcoins.util.UniversalAccounts;

public class TileVendor extends TileEntity implements IInventory, ISidedInventory {

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

	public String blockOwner = "";
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
	public boolean uIronCoinBtnActive = false;
	public boolean uGoldCoinBtnActive = false;
	public boolean uEmeraldCoinBtnActive = false;
	public boolean uDiamondCoinBtnActive = false;
	public boolean uObsidianCoinBtnActive = false;
	public boolean inUse = false;
	public String playerName = "";
	public String blockIcon = "planks_birch";
	public int textColor = 0x0;
	private int remoteX = 0;
	private int remoteY = 0;
	private int remoteZ = 0;

	@Override
	public void updateEntity() {
		super.updateEntity();
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
		if (inventory[itemOutputSlot] != null) {
			if (inventory[itemOutputSlot].getMaxStackSize() == inventory[itemOutputSlot].stackSize) {
				buyButtonActive = false;
				return;
			}
		}
		if ((userCoinSum >= itemPrice && coinSum + itemPrice < Integer.MAX_VALUE && (!ooStockWarning || infiniteMode))
				|| (getUserAccountBalance() > itemPrice && (!ooStockWarning || infiniteMode))) {
			buyButtonActive = true;
		} else
			buyButtonActive = false;
	}

	private void activateSellButton() {
		if (inventory[itemSellSlot] != null && inventory[itemTradeSlot] != null
				&& inventory[itemTradeSlot].getItem() == inventory[itemSellSlot].getItem()
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
			ironCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.iron_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[1]) {
			goldCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.gold_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[2]) {
			emeraldCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.emerald_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[3]) {
			diamondCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.diamond_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= UniversalCoins.coinValues[4]) {
			obsidianCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.obsidian_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
	}

	private void activateUserRetrieveButtons() {
		uIronCoinBtnActive = false;
		uGoldCoinBtnActive = false;
		uEmeraldCoinBtnActive = false;
		uDiamondCoinBtnActive = false;
		uObsidianCoinBtnActive = false;
		if (userCoinSum > 0) {
			uIronCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.iron_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[1]) {
			uGoldCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.gold_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[2]) {
			uEmeraldCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.emerald_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[3]) {
			uDiamondCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.diamond_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= UniversalCoins.coinValues[4]) {
			uObsidianCoinBtnActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.obsidian_coin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
	}

	public void onRetrieveButtonsPressed(int buttonClickedID, boolean shiftPressed) {
		if (buttonClickedID <= VendorGUI.idLBagButton) {
			// get owner coins
			coinSum = retrieveCoins(coinSum, buttonClickedID, shiftPressed);
			updateCoinsForPurchase();
		} else {
			// get buyer coins
			userCoinSum = retrieveCoins(userCoinSum, buttonClickedID, shiftPressed);
		}
	}

	public int retrieveCoins(int coinField, int buttonClickedID, boolean shiftPressed) {
		int absoluteButton = (buttonClickedID <= VendorGUI.idLBagButton ? buttonClickedID - VendorGUI.idCoinButton
				: buttonClickedID - VendorSellGUI.idIronCoinButton);
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
		if (coinField < UniversalCoins.coinValues[absoluteButton]
				|| (inventory[itemCoinOutputSlot] != null && inventory[itemCoinOutputSlot].getItem() != itemOnButton)
				|| (inventory[itemCoinOutputSlot] != null && inventory[itemCoinOutputSlot].stackSize == 64)) {
			return coinField;
		}
		if (shiftPressed) {
			if (inventory[itemCoinOutputSlot] == null) {
				int amount = (int) (coinField / UniversalCoins.coinValues[absoluteButton]);
				if (amount >= 64) {
					coinField -= UniversalCoins.coinValues[absoluteButton] * 64;
					inventory[itemCoinOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemCoinOutputSlot].stackSize = 64;
				} else {
					coinField -= UniversalCoins.coinValues[absoluteButton] * amount;
					inventory[itemCoinOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemCoinOutputSlot].stackSize = amount;
				}
			} else {
				int amount = (int) Math.min(coinField / UniversalCoins.coinValues[absoluteButton],
						inventory[itemCoinOutputSlot].getMaxStackSize() - inventory[itemCoinOutputSlot].stackSize);
				inventory[itemCoinOutputSlot].stackSize += amount;
				coinField -= UniversalCoins.coinValues[absoluteButton] * amount;
			}
		} else {
			coinField -= UniversalCoins.coinValues[absoluteButton];
			if (inventory[itemCoinOutputSlot] == null) {
				inventory[itemCoinOutputSlot] = new ItemStack(itemOnButton);
			} else {
				inventory[itemCoinOutputSlot].stackSize++;
			}
		}
		return coinField;
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
				inventory[itemOutputSlot] = new ItemStack(inventory[itemTradeSlot].getItem());
				inventory[itemOutputSlot].setItemDamage(inventory[itemTradeSlot].getItemDamage());
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
			coinSum = 0;
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
						inventory[itemOutputSlot] = new ItemStack(inventory[itemTradeSlot].getItem());
						inventory[itemOutputSlot].setItemDamage(inventory[itemTradeSlot].getItemDamage());
						inventory[itemOutputSlot].stackSize = 0;
					}
					int thisSale = Math.min(inventory[i].stackSize, totalSale);
					inventory[itemOutputSlot].stackSize += thisSale;
					inventory[i].stackSize -= thisSale;
					totalSale -= thisSale;
					if (useCard && inventory[itemUserCardSlot] != null) {
						debitUserAccount(itemPrice * thisSale / inventory[itemTradeSlot].stackSize);
					} else {
						userCoinSum -= itemPrice * thisSale / inventory[itemTradeSlot].stackSize;
					}
					if (inventory[itemCardSlot] != null
							&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.ender_card
							&& getOwnerAccountBalance() != -1
							&& getOwnerAccountBalance() + (itemPrice * thisSale) < Long.MAX_VALUE) {
						creditOwnerAccount(itemPrice * thisSale / inventory[itemTradeSlot].stackSize);
					} else {
						// if (Integer.MAX_VALUE - coinSum < itemPrice *
						// thisSale)//TODO fix overflow
						coinSum += itemPrice * thisSale / inventory[itemTradeSlot].stackSize;
					}
				}
				// cleanup empty stacks
				if (inventory[i] == null || inventory[i].stackSize == 0) {
					inventory[i] = null;
				}
			}
		}
		checkSellingInventory(); // we sold things. Make sure we still have some
									// left
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
			if (useCard && getUserAccountBalance() > Integer.MAX_VALUE) {
				amount = Integer.MAX_VALUE / itemPrice;
			} else {
				amount = (int) ((useCard ? getUserAccountBalance() : coinSum) / itemPrice);
			}
			if (amount > inventory[itemTradeSlot].getMaxStackSize()) {
				amount = inventory[itemTradeSlot].getMaxStackSize();
			}
		} else if (inventory[itemOutputSlot].getItem() == inventory[itemTradeSlot].getItem()
				&& inventory[itemOutputSlot].getItemDamage() == inventory[itemTradeSlot].getItemDamage()
				&& ItemStack.areItemStackTagsEqual(inventory[itemOutputSlot], inventory[itemTradeSlot])
				&& inventory[itemOutputSlot].stackSize < inventory[itemTradeSlot].getMaxStackSize()
						- inventory[itemTradeSlot].stackSize) {
			if (useCard && getUserAccountBalance() > Integer.MAX_VALUE) {
				amount = Integer.MAX_VALUE / itemPrice;
			} else {
				amount = (int) ((useCard ? getUserAccountBalance() : coinSum) / itemPrice);
			}
			if (amount > (inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize)) {
				amount = (inventory[itemOutputSlot].getMaxStackSize() - inventory[itemOutputSlot].stackSize);
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
		if (useCard && getOwnerAccountBalance() > Integer.MAX_VALUE) {
			amount = Math.min(Integer.MAX_VALUE / (itemPrice * inventory[itemTradeSlot].stackSize), amount);
		} else {
			amount = (int) Math.min(
					((useCard ? getOwnerAccountBalance() : coinSum) / (itemPrice * inventory[itemTradeSlot].stackSize)),
					amount);
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
								+ (itemPrice * thisSale / inventory[itemTradeSlot].stackSize) < Long.MAX_VALUE) {
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
		this.ooStockWarning = true; // if we reach this point, we are OOS.
		updateSigns();
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
				inventory[slot] = null;
			} else {
				stack = stack.splitStack(size);
				if (stack.stackSize == 0) {
					inventory[slot] = null;
				}
			}
		}
		if (slot < itemStorageSlot9) { // update inventory status
			checkSellingInventory();
			hasInventorySpace();
		}
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
				if (coinValue > 0) {
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
						if (inventory[itemUserCardSlot] != null
								&& inventory[itemUserCardSlot].getItem() == UniversalCoins.proxy.ender_card
								&& inventory[itemUserCardSlot].hasTagCompound() && getUserAccountBalance() != -1
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
			if (slot == itemUserCardSlot && inventory[itemUserCardSlot].getItem() instanceof ItemEnderCard
					&& getUserAccountBalance() != -1 && getUserAccountBalance() + userCoinSum < Long.MAX_VALUE) {
				creditUserAccount(userCoinSum);
				userCoinSum = 0;
			}
			checkSellingInventory(); // update inventory status
			hasInventorySpace();
		}
	}

	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == itemTradeSlot) {
			inventory[itemTradeSlot] = stack.copy();
			return false;
		} else
			return true;
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

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void sendButtonMessage(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, button, shiftPressed));
	}

	public void sendServerUpdateMessage() {
		UniversalCoins.snw
				.sendToServer(new UCVendorServerMessage(xCoord, yCoord, zCoord, itemPrice, blockOwner, infiniteMode));
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
			blockOwner = tagCompound.getString("BlockOwner");
		} catch (Throwable ex2) {
			blockOwner = null;
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
			uIronCoinBtnActive = tagCompound.getBoolean("uIronCoinBtnActive");
		} catch (Throwable ex2) {
			uIronCoinBtnActive = false;
		}
		try {
			uGoldCoinBtnActive = tagCompound.getBoolean("uGoldCoinBtnActive");
		} catch (Throwable ex2) {
			uGoldCoinBtnActive = false;
		}
		try {
			uEmeraldCoinBtnActive = tagCompound.getBoolean("uEmeraldCoinBtnActive");
		} catch (Throwable ex2) {
			uEmeraldCoinBtnActive = false;
		}
		try {
			uDiamondCoinBtnActive = tagCompound.getBoolean("uDiamondCoinBtnActive");
		} catch (Throwable ex2) {
			uDiamondCoinBtnActive = false;
		}
		try {
			uObsidianCoinBtnActive = tagCompound.getBoolean("uObsidianCoinBtnActive");
		} catch (Throwable ex2) {
			uObsidianCoinBtnActive = false;
		}
		try {
			inUse = tagCompound.getBoolean("InUse");
		} catch (Throwable ex2) {
			inUse = false;
		}
		try {
			blockIcon = tagCompound.getString("BlockIcon");
		} catch (Throwable ex2) {
			blockIcon = "";
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
		tagCompound.setString("BlockOwner", blockOwner);
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
		tagCompound.setBoolean("uIronCoinBtnActive", uIronCoinBtnActive);
		tagCompound.setBoolean("uGoldCoinBtnActive", uGoldCoinBtnActive);
		tagCompound.setBoolean("uEmeraldCoinBtnActive", uEmeraldCoinBtnActive);
		tagCompound.setBoolean("uDiamondCoinBtnActive", uDiamondCoinBtnActive);
		tagCompound.setBoolean("uObsidianCoinBtnActive", uObsidianCoinBtnActive);
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setString("BlockIcon", blockIcon);
		tagCompound.setInteger("TextColor", textColor);
		tagCompound.setInteger("remoteX", remoteX);
		tagCompound.setInteger("remoteY", remoteY);
		tagCompound.setInteger("remoteZ", remoteZ);
	}

	public void updateSigns() {
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 11 };
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3) {
		// put everything in the item storage slots
		if (var1 >= itemStorageSlot1 && var1 <= itemStorageSlot9) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3) {
		// allow pulling items from output slot only
		if (var1 == itemCoinOutputSlot) {
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
					loadRemoteChunk(xCoord, yCoord, zCoord);
					TileEntity te = worldObj.getTileEntity(remoteX, remoteY, remoteZ);
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

	private void loadRemoteChunk(int x, int y, int z) {
		Chunk ch = worldObj.getChunkFromBlockCoords(x, y);
		worldObj.getChunkProvider().loadChunk(ch.xPosition, ch.zPosition);
	}

	public void onButtonPressed(int buttonId, boolean shiftPressed) {
		if (buttonId == VendorGUI.idModeButton) {
			onModeButtonPressed();
		}
		if (buttonId < VendorGUI.idCoinButton) {
			// do nothing here
		} else if (buttonId <= VendorGUI.idLBagButton) {
			onRetrieveButtonsPressed(buttonId, shiftPressed);
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
		} else if (buttonId <= VendorSellGUI.idObsidianCoinButton) {
			onRetrieveButtonsPressed(buttonId, shiftPressed);
		}
		if (buttonId == VendorGUI.idtcmButton) {
			if (textColor > 0)
				textColor--;
		}
		if (buttonId == VendorGUI.idtcpButton) {
			if (textColor < 15)
				textColor++;
		}
	}

	private long getOwnerAccountBalance() {
		if (worldObj.isRemote)
			return 0;
		if (inventory[itemCardSlot] == null || !inventory[itemCardSlot].hasTagCompound())
			return 0;
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private void creditOwnerAccount(long i) {
		if (worldObj.isRemote || !inventory[itemCardSlot].hasTagCompound())
			return;
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		UniversalAccounts.getInstance().creditAccount(accountNumber, i);
	}

	private void debitOwnerAccount(long i) {
		if (worldObj.isRemote || !inventory[itemCardSlot].hasTagCompound())
			return;
		String accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
		UniversalAccounts.getInstance().debitAccount(accountNumber, i);
	}

	private void debitUserAccount(long i) {
		if (worldObj.isRemote || !inventory[itemUserCardSlot].hasTagCompound())
			return;
		String accountNumber = inventory[itemUserCardSlot].stackTagCompound.getString("Account");
		UniversalAccounts.getInstance().debitAccount(accountNumber, i);
	}

	private long getUserAccountBalance() {
		if (worldObj.isRemote)
			return 0;
		if (inventory[itemUserCardSlot] == null || !inventory[itemUserCardSlot].hasTagCompound())
			return 0;
		String accountNumber = inventory[itemUserCardSlot].stackTagCompound.getString("Account");
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private void creditUserAccount(int i) {
		if (worldObj.isRemote || !inventory[itemUserCardSlot].hasTagCompound())
			return;
		String accountNumber = inventory[itemUserCardSlot].stackTagCompound.getString("Account");
		UniversalAccounts.getInstance().creditAccount(accountNumber, i);
	}

}
