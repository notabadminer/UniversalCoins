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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
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

	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
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
	public boolean coinButtonActive = false;
	public boolean isSStackButtonActive = false;
	public boolean isLStackButtonActive = false;
	public boolean isSBagButtonActive = false;
	public boolean isLBagButtonActive = false;
	public boolean uCoinButtonActive = false;
	public boolean uSStackButtonActive = false;
	public boolean uLStackButtonActive = false;
	public boolean uSBagButtonActive = false;
	public boolean uLBagButtonActive = false;
	public boolean inUse = false;
	public String playerName = "";
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
		coinButtonActive = false;
		isSStackButtonActive = false;
		isLStackButtonActive = false;
		isSBagButtonActive = false;
		isLBagButtonActive = false;
		if (coinSum > 0) {
			coinButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemCoin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= 9) {
			isSStackButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemSmallCoinStack
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= 81) {
			isLStackButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemLargeCoinStack
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= 729) {
			isSBagButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemSmallCoinBag
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (coinSum >= 6561) {
			isLBagButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemLargeCoinBag
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
				: buttonClickedID - VendorSellGUI.idCoinButton);
		int multiplier = 1;
		for (int i = 0; i < absoluteButton; i++) {
			multiplier *= 9;
		}
		Item itemOnButton = coins[absoluteButton];
		if (coinField < multiplier
				|| (inventory[itemCoinOutputSlot] != null && inventory[itemCoinOutputSlot].getItem() != itemOnButton)
				|| (inventory[itemCoinOutputSlot] != null && inventory[itemCoinOutputSlot].stackSize == 64)) {
			return coinField;
		}
		if (shiftPressed) {
			if (inventory[itemCoinOutputSlot] == null) {
				int amount = coinField / multiplier;
				if (amount >= 64) {
					coinField -= multiplier * 64;
					inventory[itemCoinOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemCoinOutputSlot].stackSize = 64;
				} else {
					coinField -= multiplier * amount;
					inventory[itemCoinOutputSlot] = new ItemStack(itemOnButton);
					inventory[itemCoinOutputSlot].stackSize = amount;
				}
			} else {
				int amount = Math.min(coinField / multiplier,
						inventory[itemCoinOutputSlot].getMaxStackSize() - inventory[itemCoinOutputSlot].stackSize);
				inventory[itemCoinOutputSlot].stackSize += amount;
				coinField -= multiplier * amount;
			}
		} else {
			coinField -= multiplier;
			if (inventory[itemCoinOutputSlot] == null) {
				inventory[itemCoinOutputSlot] = new ItemStack(itemOnButton);
			} else {
				inventory[itemCoinOutputSlot].stackSize++;
			}
		}
		return coinField;
	}

	private void activateUserRetrieveButtons() {
		uCoinButtonActive = false;
		uSStackButtonActive = false;
		uLStackButtonActive = false;
		uSBagButtonActive = false;
		uLBagButtonActive = false;
		if (userCoinSum > 0) {
			uCoinButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemCoin
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= 9) {
			uSStackButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemSmallCoinStack
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= 81) {
			uLStackButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemLargeCoinStack
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= 729) {
			uSBagButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemSmallCoinBag
							&& inventory[itemCoinOutputSlot].stackSize != 64);
		}
		if (userCoinSum >= 6561) {
			uLBagButtonActive = inventory[itemCoinOutputSlot] == null
					|| (inventory[itemCoinOutputSlot].getItem() == UniversalCoins.proxy.itemLargeCoinBag
							&& inventory[itemCoinOutputSlot].stackSize != 64);
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
						&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard
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
								&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard
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
			if (inventory[itemTradeSlot].getMaxStackSize() * itemPrice
					/ inventory[itemTradeSlot].stackSize <= (useCard ? getUserAccountBalance() : userCoinSum)) {
				// buy as many as will fit in a stack
				amount = inventory[itemTradeSlot].getMaxStackSize() / inventory[itemTradeSlot].stackSize;
			} else {
				// buy as many as i have coins for.
				amount = (useCard ? getUserAccountBalance() : userCoinSum) / itemPrice;
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
				amount = (useCard ? getUserAccountBalance() : userCoinSum) / itemPrice; // buy
																						// as
																						// many
																						// as
																						// i
																						// can
																						// with
																						// available
																						// coins.
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
					&& inventory[itemUserCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard
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
			amount = Math.min(getOwnerAccountBalance() / (itemPrice * inventory[itemTradeSlot].stackSize), amount);
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
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = 0;
					if (slot == itemCoinInputSlot) {
						depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
						if (inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound()
								&& inventory[itemCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard
								&& getOwnerAccountBalance() != -1
								&& getOwnerAccountBalance() + (depositAmount * itemValue) < Integer.MAX_VALUE) {
							creditOwnerAccount(depositAmount * itemValue);
						} else {
							coinSum += depositAmount * itemValue;
						}
						updateCoinsForPurchase();
					} else {
						depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - userCoinSum) / itemValue);
						if (inventory[itemUserCardSlot] != null && inventory[itemUserCardSlot].hasTagCompound()
								&& inventory[itemUserCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard
								&& getUserAccountBalance() != -1
								&& getUserAccountBalance() + (depositAmount * itemValue) < Integer.MAX_VALUE) {
							creditUserAccount(depositAmount * itemValue);
						} else {
							userCoinSum += depositAmount * itemValue;
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
					&& getUserAccountBalance() != -1 && getUserAccountBalance() + userCoinSum < Integer.MAX_VALUE) {
				creditUserAccount(userCoinSum);
				userCoinSum = 0;
			}
			checkSellingInventory(); // update inventory status
			hasInventorySpace();
		}
		updateEntity();
	}

	private int getCoinType(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
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

	public void updateTE() {
		worldObj.markBlockForUpdate(pos);
	}

	public void sendButtonMessage(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
	}

	public void sendServerUpdateMessage() {
		UniversalCoins.snw.sendToServer(
				new UCVendorServerMessage(pos.getX(), pos.getY(), pos.getZ(), itemPrice, blockOwner, infiniteMode));
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
			coinButtonActive = tagCompound.getBoolean("CoinButtonActive");
		} catch (Throwable ex2) {
			coinButtonActive = false;
		}
		try {
			isSStackButtonActive = tagCompound.getBoolean("SmallStackButtonActive");
		} catch (Throwable ex2) {
			isSStackButtonActive = false;
		}
		try {
			isLStackButtonActive = tagCompound.getBoolean("LargeStackButtonActive");
		} catch (Throwable ex2) {
			isLStackButtonActive = false;
		}
		try {
			isSBagButtonActive = tagCompound.getBoolean("SmallBagButtonActive");
		} catch (Throwable ex2) {
			isSBagButtonActive = false;
		}
		try {
			isLBagButtonActive = tagCompound.getBoolean("LargeBagButtonActive");
		} catch (Throwable ex2) {
			isLBagButtonActive = false;
		}
		try {
			uCoinButtonActive = tagCompound.getBoolean("UserCoinButtonActive");
		} catch (Throwable ex2) {
			uCoinButtonActive = false;
		}
		try {
			uSStackButtonActive = tagCompound.getBoolean("UserSmallStackButtonActive");
		} catch (Throwable ex2) {
			uSStackButtonActive = false;
		}
		try {
			uLStackButtonActive = tagCompound.getBoolean("UserLargeStackButtonActive");
		} catch (Throwable ex2) {
			uLStackButtonActive = false;
		}
		try {
			uSBagButtonActive = tagCompound.getBoolean("UserSmallBagButtonActive");
		} catch (Throwable ex2) {
			uSBagButtonActive = false;
		}
		try {
			uLBagButtonActive = tagCompound.getBoolean("UserLargeBagButtonActive");
		} catch (Throwable ex2) {
			uLBagButtonActive = false;
		}
		try {
			inUse = tagCompound.getBoolean("InUse");
		} catch (Throwable ex2) {
			inUse = false;
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
		tagCompound.setBoolean("CoinButtonActive", coinButtonActive);
		tagCompound.setBoolean("SmallStackButtonActive", isSStackButtonActive);
		tagCompound.setBoolean("LargeStackButtonActive", isLStackButtonActive);
		tagCompound.setBoolean("SmallBagButtonActive", isSBagButtonActive);
		tagCompound.setBoolean("LargeBagButtonActive", isLBagButtonActive);
		tagCompound.setBoolean("UserCoinButtonActive", uCoinButtonActive);
		tagCompound.setBoolean("UserSmallStackButtonActive", uSStackButtonActive);
		tagCompound.setBoolean("UserLargeStackButtonActive", uLStackButtonActive);
		tagCompound.setBoolean("UserSmallBagButtonActive", uSBagButtonActive);
		tagCompound.setBoolean("UserLargeBagButtonActive", uLBagButtonActive);
		tagCompound.setBoolean("InUse", inUse);
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

	public int getUserAccountBalance() {
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

	public int getOwnerAccountBalance() {
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
		} else if (buttonId >= VendorGUI.idCoinButton && buttonId <= VendorGUI.idLBagButton) {
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
		} else if (buttonId >= VendorBuyGUI.idCoinButton && buttonId <= VendorBuyGUI.idLBagButton) {
			onRetrieveButtonsPressed(buttonId, shiftPressed);
		}
		updateEntity();
	}

	@Override
	public IChatComponent getDisplayName() {
		// TODO Auto-generated method stub
		return null;
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
