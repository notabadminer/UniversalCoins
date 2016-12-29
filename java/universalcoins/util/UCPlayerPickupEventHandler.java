package universalcoins.util;

import java.text.DecimalFormat;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import universalcoins.Achievements;
import universalcoins.UniversalCoins;

public class UCPlayerPickupEventHandler {

	private World world;
	private String accountNumber;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		if (event.item.getEntityItem().getItem() == UniversalCoins.proxy.iron_coin
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.gold_coin
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.emerald_coin
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.diamond_coin
				|| event.item.getEntityItem().getItem() == UniversalCoins.proxy.obsidian_coin) {
			event.entityPlayer.addStat(Achievements.achCoin, 1);
			world = event.entityPlayer.worldObj;
			EntityPlayer player = event.entityPlayer;
			ItemStack[] inventory = player.inventory.mainInventory;
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] != null && inventory[i].getItem() == UniversalCoins.proxy.ender_card) {
					if (!inventory[i].hasTagCompound())
						return; // card has not been initialized. Nothing we can
								// do here
					accountNumber = inventory[i].getTagCompound().getString("Account");
					long accountBalance = getAccountBalance(accountNumber);
					if (accountBalance == -1)
						return; // get out of here if the card is invalid
					if (event.item.getEntityItem().stackSize == 0)
						return; // no need to notify on zero size stack
					int coinsFound = 0;
					switch (event.item.getEntityItem().getUnlocalizedName()) {
					case "item.iron_coin":
						coinsFound += event.item.getEntityItem().stackSize * UniversalCoins.coinValues[0];
						break;
					case "item.gold_coin":
						coinsFound += event.item.getEntityItem().stackSize * UniversalCoins.coinValues[1];
						break;
					case "item.emerald_coin":
						coinsFound += event.item.getEntityItem().stackSize * UniversalCoins.coinValues[2];
						break;
					case "item.diamond_coin":
						coinsFound += event.item.getEntityItem().stackSize * UniversalCoins.coinValues[3];
						break;
					case "item.obsidian_coin":
						coinsFound += event.item.getEntityItem().stackSize * UniversalCoins.coinValues[4];
						break;
					}
					long depositAmount = Math.min(Long.MAX_VALUE - accountBalance, coinsFound);
					if (depositAmount > 0) {
						creditAccount(accountNumber, depositAmount);
						player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.card.deposit")
								+ " " + formatter.format(depositAmount) + " " + StatCollector.translateToLocal(
										depositAmount > 1 ? "general.currency.multiple" : "general.currency.single")));
						event.item.getEntityItem().stackSize -= depositAmount;
					}
					if (event.item.getEntityItem().stackSize == 0) {
						event.setCanceled(true);
					}
					break; // no need to continue. We are done here
				}
			}
		}
	}

	private long getAccountBalance(String accountNumber) {
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	private void creditAccount(String accountNumber, long amount) {
		UniversalAccounts.getInstance().creditAccount(accountNumber, amount);
	}
}
