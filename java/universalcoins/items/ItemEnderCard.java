package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.util.UniversalAccounts;

public class ItemEnderCard extends Item {

	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };

	public ItemEnderCard() {
		super();
		this.maxStackSize = 1;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if (stack.hasTagCompound()) {
			list.add(stack.getTagCompound().getString("Name"));
			list.add(stack.getTagCompound().getString("Account"));
		} else {
			list.add(StatCollector.translateToLocal("item.itemUCCard.warning"));
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (world.isRemote)
			return true;
		if (itemstack.getTagCompound() == null) {
			createNBT(itemstack, world, player);
		}
		int accountBalance = UniversalAccounts.getInstance().getAccountBalance(itemstack.getTagCompound().getString("Account"));
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		ItemStack[] inventory = player.inventory.mainInventory;
		String accountNumber = itemstack.getTagCompound().getString("Account");
		int coinsDeposited = 0;
		for (int i = 0; i < inventory.length; i++) {
			if (inventory[i] != null
					&& (inventory[i].getItem() == UniversalCoins.proxy.itemCoin
							|| inventory[i].getItem() == UniversalCoins.proxy.itemSmallCoinStack
							|| inventory[i].getItem() == UniversalCoins.proxy.itemLargeCoinStack
							|| inventory[i].getItem() == UniversalCoins.proxy.itemSmallCoinBag || inventory[i]
							.getItem() == UniversalCoins.proxy.itemLargeCoinBag)) {
				if (accountBalance == -1)
					return true; // get out of here if the card is invalid
				int coinType = getCoinType(inventory[i].getItem());
				if (coinType == -1)
					return true; // something went wrong
				int coinValue = multiplier[coinType];
				int depositAmount = Math.min(inventory[i].stackSize, (Integer.MAX_VALUE - accountBalance) / coinValue);
				UniversalAccounts.getInstance().creditAccount(accountNumber, depositAmount * coinValue);
				coinsDeposited += depositAmount * coinValue;
				inventory[i].stackSize -= depositAmount;
				if (inventory[i].stackSize == 0) {
					player.inventory.setInventorySlotContents(i, null);
					player.inventoryContainer.detectAndSendChanges();
				}
			}
		}
		if (coinsDeposited > 0) {
			player.addChatMessage(new ChatComponentText(StatCollector
					.translateToLocal("item.itemEnderCard.message.deposit")
					+ " "
					+ formatter.format(coinsDeposited)
					+ " " + StatCollector.translateToLocal("item.itemCoin.name")));
		}
		player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.itemEnderCard.balance") + " "
				+ formatter.format(UniversalAccounts.getInstance().getAccountBalance(accountNumber))));
		return true;
	}

	private void createNBT(ItemStack stack, World world, EntityPlayer entityPlayer) {
		String accountNumber = UniversalAccounts.getInstance().getOrCreatePlayerAccount(entityPlayer.getPersistentID().toString());
		stack.getTagCompound().setString("Name", entityPlayer.getName());
		stack.getTagCompound().setString("Owner", entityPlayer.getPersistentID().toString());
		stack.getTagCompound().setString("Account", accountNumber);
	}

	private int getCoinType(Item item) {
		final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin, UniversalCoins.proxy.itemSmallCoinStack,
				UniversalCoins.proxy.itemLargeCoinStack, UniversalCoins.proxy.itemSmallCoinBag,
				UniversalCoins.proxy.itemLargeCoinBag };
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
	}
}
