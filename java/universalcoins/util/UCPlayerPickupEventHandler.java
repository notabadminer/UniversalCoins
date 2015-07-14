package universalcoins.util;

import java.text.DecimalFormat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import universalcoins.UniversalCoins;

public class UCPlayerPickupEventHandler {

	private World world;
	private String accountNumber;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		if (event.item.getEntityItem().getItem() == UniversalCoins.proxy.itemCoin
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.itemSmallCoinStack
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.itemLargeCoinStack
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.itemSmallCoinBag
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.itemLargeCoinBag) {
			world = event.entityPlayer.worldObj;
			EntityPlayer player = event.entityPlayer;
			ItemStack[] inventory = player.inventory.mainInventory;
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] != null && inventory[i].getItem() == UniversalCoins.proxy.itemEnderCard) {
					if (!inventory[i].hasTagCompound())
						return; // card has not been initialized. Nothing we can
								// do here
					accountNumber = inventory[i].getTagCompound().getString("Account");
					int accountBalance = getAccountBalance(accountNumber);
					if (accountBalance == -1)
						return; // get out of here if the card is invalid
					if (event.item.getEntityItem().stackSize == 0)
						return; // no need to notify on zero size stack
					int coinType = getCoinType(event.item.getEntityItem().getItem());
					if (coinType == -1)
						return; // something went wrong
					int coinValue = multiplier[coinType];
					int depositAmount = Math.min(event.item.getEntityItem().stackSize,
							(Integer.MAX_VALUE - accountBalance) / coinValue);
					creditAccount(accountNumber, depositAmount * coinValue);
					player.addChatMessage(new ChatComponentText(StatCollector
							.translateToLocal("item.itemEnderCard.message.deposit")
							+ " "
							+ formatter.format(depositAmount * coinValue)
							+ " "
							+ StatCollector.translateToLocal("item.itemCoin.name")));
					event.item.getEntityItem().stackSize -= depositAmount;
					if (event.item.getEntityItem().stackSize == 0) {
						event.setCanceled(true);
					}
					break; // no need to continue. We are done here
				}
			}
		}
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

	private int getAccountBalance(String accountNumber) {
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private void creditAccount(String accountNumber, int amount) {
		UniversalAccounts.getInstance().creditAccount(accountNumber, amount);
	}
}
