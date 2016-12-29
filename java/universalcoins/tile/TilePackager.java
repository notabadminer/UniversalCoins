package universalcoins.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCPackagerServerMessage;
import universalcoins.util.UniversalAccounts;

public class TilePackager extends TileEntity implements IInventory, ISidedInventory {

	private ItemStack[] inventory = new ItemStack[12];
	public static final int itemCardSlot = 0;
	public static final int itemCoinSlot = 1;
	public static final int itemPackageInputSlot = 2;
	public static final int itemOutputSlot = 3;
	public static final int[] itemPackageSlot = { 4, 5, 6, 7, 8, 9, 10, 11 };
	public long coinSum = 0;
	public boolean cardAvailable = false;
	public String customName = "";
	public String playerName = "";
	public String packageTarget = "";
	public boolean inUse = false;
	public int packageSize = 0;
	public int[] packageCost = { UniversalCoins.smallPackagePrice, UniversalCoins.medPackagePrice,
			UniversalCoins.largePackagePrice };

	public TilePackager() {
		super();
	}

	public void onButtonPressed(int buttonId, boolean doSend) {
		if (buttonId == 0) {
			if (doSend) {
				sendPackage(packageTarget);
				return;
			}
			checkCard();
			if (coinSum < packageCost[packageSize] && !cardAvailable)
				return;
			if (inventory[itemOutputSlot] == null) {

				NBTTagList itemList = new NBTTagList();
				NBTTagCompound tagCompound = new NBTTagCompound();
				for (int i = 4; i < itemPackageSlot.length + 4; i++) {
					ItemStack invStack = inventory[i];
					if (invStack != null && invStack.getItem() != UniversalCoins.proxy.uc_package) {
						NBTTagCompound tag = new NBTTagCompound();
						tag.setByte("Slot", (byte) i);
						invStack.writeToNBT(tag);
						itemList.appendTag(tag);
						inventory[i] = null;
					}
				}
				if (itemList.tagCount() > 0) {
					inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.uc_package);
					tagCompound.setTag("Inventory", itemList);
					inventory[itemOutputSlot].setTagCompound(tagCompound);
					if (cardAvailable) {
						String account = inventory[itemCardSlot].getTagCompound().getString("accountNumber");
						UniversalAccounts.getInstance().debitAccount(account, packageCost[packageSize]);
					} else {
						coinSum -= packageCost[packageSize];

					}
				}

			}
		}
		if (buttonId == 1) {
			fillOutputSlot();
		}
		if (buttonId == 2) {
			packageSize = 0;
			for (int i = 11; i > 7; i--) {
				ejectItem(i);
			}
		}
		if (buttonId == 3) {
			packageSize = 1;
			for (int i = 11; i > 9; i--) {
				ejectItem(i);
			}
		}
		if (buttonId == 4) {
			packageSize = 2;
		}
		if (buttonId == 5) {
			if (doSend) {
				// eject package if switching from send to buy
				ejectItem(itemPackageInputSlot);
			} else {
				// eject package inventory slots
				for (int i = 11; i > 3; i--) {
					ejectItem(i);
				} 
				if (inventory[itemOutputSlot] != null
						&& inventory[itemOutputSlot].getItem() == UniversalCoins.proxy.uc_package) {
					// move package if in output slot
					if (inventory[itemPackageInputSlot] != null)
						ejectItem(itemPackageInputSlot);
					inventory[itemPackageInputSlot] = inventory[itemOutputSlot];
					inventory[itemOutputSlot] = null;
				}
			}
		}
	}

	private void ejectItem(int slot) {
		if (inventory[slot] != null) {
			if (worldObj.getPlayerEntityByName(playerName).inventory.getFirstEmptyStack() != -1) {
				worldObj.getPlayerEntityByName(playerName).inventory.addItemStackToInventory(inventory[slot]);
			} else {
				// spawn in world
				EntityItem entityItem = new EntityItem(worldObj, xCoord, yCoord, zCoord, inventory[slot]);
				worldObj.spawnEntityInWorld(entityItem);
			}
			inventory[slot] = null;
		}
	}

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
		inUse = false;
		updateTE();
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : UniversalCoins.proxy.packager.getLocalizedName();
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

	public void checkCard() {
		if (!worldObj.isRemote && inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound()) {
			String account = inventory[itemCardSlot].getTagCompound().getString("Account");
			long accountBalance = UniversalAccounts.getInstance().getAccountBalance(account);
			if (accountBalance >= packageCost[packageSize]) {
				cardAvailable = true;
				return;
			}
		}
		cardAvailable = false;
	}

	public void playerLookup(String player, boolean tabPressed) {
		if (tabPressed) {
			String test[] = new String[1];
			test[0] = player;
			List match = CommandBase.getListOfStringsMatchingLastWord(test,
					MinecraftServer.getServer().getAllUsernames());
			if (match.size() > 0) {
				packageTarget = match.get(0).toString();
			}
		} else {
			EntityPlayerMP targetPlayer = MinecraftServer.getServer().getConfigurationManager().func_152612_a(player);
			if (targetPlayer != null) {
				packageTarget = player;
			} else {
				packageTarget = "";
			}
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, button, shiftPressed));
	}

	public void sendServerUpdateMessage(String packageTarget, boolean tabPressed) {
		UniversalCoins.snw.sendToServer(new UCPackagerServerMessage(xCoord, yCoord, zCoord, packageTarget, tabPressed));
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
		tagCompound.setLong("coinSum", coinSum);
		tagCompound.setBoolean("cardAvailable", cardAvailable);
		tagCompound.setString("customName", customName);
		tagCompound.setString("packageTarget", packageTarget);
		tagCompound.setBoolean("inUse", inUse);
		tagCompound.setInteger("packageSize", packageSize);
		tagCompound.setInteger("smallPrice", packageCost[0]);
		tagCompound.setInteger("medPrice", packageCost[1]);
		tagCompound.setInteger("largePrice", packageCost[2]);
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
			coinSum = tagCompound.getInteger("coinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			cardAvailable = tagCompound.getBoolean("cardAvailable");
		} catch (Throwable ex2) {
			cardAvailable = false;
		}
		try {
			customName = tagCompound.getString("customName");
		} catch (Throwable ex2) {
			customName = "";
		}
		try {
			packageTarget = tagCompound.getString("packageTarget");
		} catch (Throwable ex2) {
			packageTarget = "";
		}
		try {
			inUse = tagCompound.getBoolean("inUse");
		} catch (Throwable ex2) {
			inUse = false;
		}
		try {
			packageSize = tagCompound.getInteger("packageSize");
		} catch (Throwable ex2) {
			packageSize = 0;
		}
		try {
			packageCost[0] = tagCompound.getInteger("smallPrice");
		} catch (Throwable ex2) {
			packageCost[0] = UniversalCoins.smallPackagePrice;
		}
		try {
			packageCost[1] = tagCompound.getInteger("medPrice");
		} catch (Throwable ex2) {
			packageCost[1] = UniversalCoins.medPackagePrice;
		}
		try {
			packageCost[2] = tagCompound.getInteger("largePrice");
		} catch (Throwable ex2) {
			packageCost[2] = UniversalCoins.largePackagePrice;
		}
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
			if (slot == itemCardSlot) {
				checkCard();
			}
		}
		return stack;
	}

	public void fillOutputSlot() {
		if (inventory[itemOutputSlot] == null && coinSum > 0) {
			if (coinSum > UniversalCoins.coinValues[4]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.obsidian_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[4], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[4];
			} else if (coinSum > UniversalCoins.coinValues[3]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.diamond_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[3], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[3];
			} else if (coinSum > UniversalCoins.coinValues[2]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.emerald_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[2], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[2];
			} else if (coinSum > UniversalCoins.coinValues[1]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.gold_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[1], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[1];
			} else if (coinSum > UniversalCoins.coinValues[0]) {
				inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.iron_coin);
				inventory[itemOutputSlot].stackSize = (int) Math.min(coinSum / UniversalCoins.coinValues[0], 64);
				coinSum -= inventory[itemOutputSlot].stackSize * UniversalCoins.coinValues[0];
			}
		}
		if (coinSum <= 0) {
			coinSum = 0;
		}
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
			if (slot == itemCoinSlot) {
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
				long depositAmount = Math.min(stack.stackSize, (Long.MAX_VALUE - coinSum) / coinValue);
				inventory[slot].stackSize -= depositAmount;
				coinSum += depositAmount * coinValue;
				if (inventory[slot].stackSize == 0) {
					inventory[slot] = null;
				}
			}
			if (slot == itemCardSlot && !worldObj.isRemote) {
				checkCard();
			}
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		// return all slots so connections can go anywhere
		if (packageSize == 0)
			return new int[] { 4, 5, 6, 7, 8, 9, 10 };
		if (packageSize == 1)
			return new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		if (packageSize == 2)
			return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		return null;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3) {
		// first check if items inserted are coins. put them in the coin input
		// slot if they are.
		if (var1 == itemCoinSlot && (var2.getItem() == (UniversalCoins.proxy.iron_coin)
				|| var2.getItem() == (UniversalCoins.proxy.gold_coin)
				|| var2.getItem() == (UniversalCoins.proxy.emerald_coin)
				|| var2.getItem() == (UniversalCoins.proxy.diamond_coin)
				|| var2.getItem() == (UniversalCoins.proxy.obsidian_coin))) {
			return true;
			// put everything else in the item input slot
		} else if (var1 < itemPackageSlot.length) {
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

	public void sendPackage(String packageTarget) {
		if (worldObj.isRemote)
			return;
		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(packageTarget);
		if (player != null) {
			if (player.inventory.getFirstEmptyStack() != -1) {
				player.inventory.addItemStackToInventory(inventory[itemPackageInputSlot]);
			} else {
				Random rand = new Random();
				float rx = rand.nextFloat() * 0.8F + 0.1F;
				float ry = rand.nextFloat() * 0.8F + 0.1F;
				float rz = rand.nextFloat() * 0.8F + 0.1F;
				EntityItem entityItem = new EntityItem(worldObj, player.getPlayerCoordinates().posX + rx,
						player.getPlayerCoordinates().posY + ry, player.getPlayerCoordinates().posZ + rz,
						inventory[itemPackageInputSlot]);
				player.worldObj.spawnEntityInWorld(entityItem);
			}
			player.addChatMessage(
					new ChatComponentText("§2" + playerName + StatCollector.translateToLocal("packager.message.sent")));
			inventory[itemPackageInputSlot] = null;
		}
	}

}
