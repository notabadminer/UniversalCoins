package universalcoins.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCPackagerServerMessage;
import universalcoins.util.CoinUtils;
import universalcoins.util.UniversalAccounts;

public class TilePackager extends TileProtected implements IInventory {
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(12, ItemStack.EMPTY);
	public static final int[] itemPackageSlot = { 0, 1, 2, 3, 4, 5, 6, 7 };
	public static final int itemCardSlot = 8;
	public static final int itemCoinSlot = 9;
	public static final int itemOutputSlot = 10;
	public static final int itemPackageInputSlot = 11;
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

	public void onButtonPressed(int buttonId, boolean shiftPressed) {
		if (buttonId == 0) {
			if (shiftPressed) {
				sendPackage(packageTarget);
				return;
			}
			if (coinSum < packageCost[packageSize] && !cardAvailable) {
				return;
			}
			if (inventory.get(itemOutputSlot) == null) {

				NBTTagList itemList = new NBTTagList();
				NBTTagCompound tagCompound = new NBTTagCompound();
				for (int i = 0; i < itemPackageSlot.length; i++) {
					ItemStack invStack = inventory.get(i);
					if (invStack != null && invStack.getItem() != UniversalCoins.Items.uc_package) {
						NBTTagCompound tag = new NBTTagCompound();
						tag.setByte("Slot", (byte) i);
						invStack.writeToNBT(tag);
						itemList.appendTag(tag);
						inventory.set(i, ItemStack.EMPTY);
					}
				}
				if (itemList.tagCount() > 0) {
					inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.uc_package));
					tagCompound.setTag("Inventory", itemList);
					inventory.get(itemOutputSlot).setTagCompound(tagCompound);
					if (cardAvailable) {
						String account = inventory.get(itemCardSlot).getTagCompound().getString("accountNumber");
						UniversalAccounts.getInstance().debitAccount(account, packageCost[packageSize], false);
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
			for (int i = 0; i < 4; i++) {
				if (inventory.get(i) != null) {
					if (world.getPlayerEntityByName(playerName).inventory.getFirstEmptyStack() != -1) {
						world.getPlayerEntityByName(playerName).inventory.addItemStackToInventory(inventory.get(i));
					} else {
						// spawn in world
						EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(),
								inventory.get(i));
						world.spawnEntity(entityItem);
					}
					inventory.set(i, ItemStack.EMPTY);
				}
			}
		}
		if (buttonId == 3) {
			packageSize = 1;
			for (int i = 0; i < 2; i++) {
				if (inventory.get(i) != null) {
					if (world.getPlayerEntityByName(playerName).inventory.getFirstEmptyStack() != -1) {
						world.getPlayerEntityByName(playerName).inventory.addItemStackToInventory(inventory.get(i));
					} else {
						// spawn in world
						EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(),
								inventory.get(i));
						world.spawnEntity(entityItem);
					}
					inventory.set(i, ItemStack.EMPTY);
				}
			}
		}
		if (buttonId == 4) {
			packageSize = 2;
		}
	}

	public void inUseCleanup() {
		if (world.isRemote)
			return;
		inUse = false;
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : UniversalCoins.Blocks.packager.getLocalizedName();
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

	public void checkCard() {
		cardAvailable = false;
		if (inventory.get(itemCardSlot) != null && inventory.get(itemCardSlot).hasTagCompound() && !world.isRemote) {
			String account = inventory.get(itemCardSlot).getTagCompound().getString("Account");
			long accountBalance = UniversalAccounts.getInstance().getAccountBalance(account);
			if (accountBalance > packageCost[packageSize]) {
				cardAvailable = true;
			}
		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return world.getTileEntity(pos) == this
				&& entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), button, shiftPressed));
	}

	public void sendServerUpdateMessage(String packageTarget, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(
				new UCPackagerServerMessage(pos.getX(), pos.getY(), pos.getZ(), packageTarget, shiftPressed));
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
		tagCompound.setLong("coinSum", coinSum);
		tagCompound.setBoolean("cardAvailable", cardAvailable);
		tagCompound.setString("customName", customName);
		tagCompound.setBoolean("inUse", inUse);
		tagCompound.setInteger("packageSize", packageSize);
		tagCompound.setString("packageTarget", packageTarget);
		tagCompound.setInteger("smallPrice", packageCost[0]);
		tagCompound.setInteger("medPrice", packageCost[1]);
		tagCompound.setInteger("largePrice", packageCost[2]);

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
				inventory.get(slot).deserializeNBT(tag);
			}
		}
		try {
			coinSum = tagCompound.getLong("coinSum");
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
			packageTarget = tagCompound.getString("packageTarget");
		} catch (Throwable ex2) {
			packageTarget = "";
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
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i >= inventory.size()) {
			return null;
		}
		return inventory.get(i);
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.getCount() <= size) {
				inventory.set(slot, ItemStack.EMPTY);
			} else {
				stack = stack.splitStack(size);
				if (stack.getCount() == 0) {
					inventory.set(slot, ItemStack.EMPTY);
				}
			}
			if (slot == itemCardSlot) {
				checkCard();
			}
		}
		return stack;
	}

	public void fillOutputSlot() {
		inventory.set(itemOutputSlot, ItemStack.EMPTY);
		if (coinSum > UniversalCoins.coinValues[4]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.obsidian_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[4], 64));
			coinSum -= UniversalCoins.coinValues[4] * inventory.get(itemOutputSlot).getCount();
		} else if (coinSum > UniversalCoins.coinValues[3]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.diamond_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[3], 64));
			coinSum -= UniversalCoins.coinValues[3] * inventory.get(itemOutputSlot).getCount();
		} else if (coinSum > UniversalCoins.coinValues[2]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.emerald_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[2], 64));
			coinSum -= UniversalCoins.coinValues[2] * inventory.get(itemOutputSlot).getCount();
		} else if (coinSum > UniversalCoins.coinValues[1]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.gold_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[1], 64));
			coinSum -= UniversalCoins.coinValues[1] * inventory.get(itemOutputSlot).getCount();
		} else if (coinSum > UniversalCoins.coinValues[0]) {
			inventory.set(itemOutputSlot, new ItemStack(UniversalCoins.Items.iron_coin));
			inventory.get(itemOutputSlot).setCount((int) Math.min(coinSum / UniversalCoins.coinValues[0], 64));
			coinSum -= UniversalCoins.coinValues[0] * inventory.get(itemOutputSlot).getCount();
		}
	}

	// @Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (slot == itemCoinSlot) {
			int coinValue = 0;
			coinValue = CoinUtils.getCoinValue(stack);
			int depositAmount = (int) Math.min(stack.getCount(), (Long.MAX_VALUE - coinSum) / coinValue);
			inventory.get(slot).shrink(depositAmount);
			coinSum += depositAmount * coinValue;
		}
		if (inventory.get(slot).getCount() == 0) {
			inventory.set(slot, ItemStack.EMPTY);
		}
		if (slot == itemCardSlot && inventory.get(itemCardSlot).getItem() == UniversalCoins.Items.ender_card) {
			if (!world.isRemote && stack.hasTagCompound()) {
				String accountNumber = stack.getTagCompound().getString("Account");
				UniversalAccounts.getInstance().creditAccount(accountNumber, coinSum, false);
				coinSum = 0;
			}
		}
		if (slot == itemCardSlot && !world.isRemote) {
			checkCard();
		}
	}

	public void sendPackage(String packageTarget) {
		if (world.isRemote)
			return;
		EntityPlayer player = world.getPlayerEntityByName(packageTarget);
		if (player != null) {
			if (player.inventory.getFirstEmptyStack() != -1) {
				player.inventory.addItemStackToInventory(inventory.get(itemPackageInputSlot));
			} else {
				Random rand = new Random();
				float rx = rand.nextFloat() * 0.8F + 0.1F;
				float ry = rand.nextFloat() * 0.8F + 0.1F;
				float rz = rand.nextFloat() * 0.8F + 0.1F;
				EntityItem entityItem = new EntityItem(world, player.posX + rx, player.posY + ry, player.posZ + rz,
						inventory.get(itemPackageInputSlot));
				world.spawnEntity(entityItem);
			}
			player.sendMessage(
					new TextComponentString("�c" + playerName + I18n.translateToLocal("packager.message.sent")));
			inventory.set(itemPackageInputSlot, ItemStack.EMPTY);
		}
	}

	public void playerLookup(String player, boolean tabPressed) {
		if (tabPressed) {
			List<String> players = new ArrayList<String>();
			for (EntityPlayer p : (List<EntityPlayer>) world.playerEntities) {
				players.add(p.getDisplayName().getUnformattedText());
			}
			String test[] = new String[1];
			test[0] = player;
			List match = CommandBase.getListOfStringsMatchingLastWord(test, players);
			if (match.size() > 0) {
				packageTarget = match.get(0).toString();
			}
		} else {
			if (world.getPlayerEntityByName(player) != null) {
				packageTarget = player;
			} else {
				packageTarget = "";
			}
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(UniversalCoins.Blocks.packager.getLocalizedName());
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
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
}
