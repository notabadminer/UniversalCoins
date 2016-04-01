package universalcoins.util;

import java.text.DecimalFormat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import universalcoins.Achievements;
import universalcoins.UniversalCoins;

public class UCPlayerPickupEventHandler {

	private World world;
	private String accountNumber;

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		if (event.getItem().getEntityItem().getItem() == UniversalCoins.proxy.iron_coin
				|| event.getItem().getEntityItem().getItem() == UniversalCoins.proxy.gold_coin
				|| event.getItem().getEntityItem().getItem() == UniversalCoins.proxy.emerald_coin
				|| event.getItem().getEntityItem().getItem() == UniversalCoins.proxy.diamond_coin
				|| event.getItem().getEntityItem().getItem() == UniversalCoins.proxy.obsidian_coin) {
			event.getEntityPlayer().addStat(Achievements.achCoin, 1);
			world = event.getEntityPlayer().worldObj;
			EntityPlayer player = event.getEntityPlayer();
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
					if (event.getItem().getEntityItem().stackSize == 0)
						return; // no need to notify on zero size stack
					int coinsFound = 0;
					switch (event.getItem().getEntityItem().getUnlocalizedName()) {
					case "item.iron_coin":
						coinsFound += event.getItem().getEntityItem().stackSize * UniversalCoins.coinValues[0];
						break;
					case "item.gold_coin":
						coinsFound += event.getItem().getEntityItem().stackSize * UniversalCoins.coinValues[1];
						break;
					case "item.emerald_coin":
						coinsFound += event.getItem().getEntityItem().stackSize * UniversalCoins.coinValues[2];
						break;
					case "item.diamond_coin":
						coinsFound += event.getItem().getEntityItem().stackSize * UniversalCoins.coinValues[3];
						break;
					case "item.obsidian_coin":
						coinsFound += event.getItem().getEntityItem().stackSize * UniversalCoins.coinValues[4];
						break;
					}
					long depositAmount = Math.min(Long.MAX_VALUE - accountBalance, coinsFound);
					if (depositAmount > 0) {
						creditAccount(accountNumber, depositAmount);
						player.addChatMessage(new TextComponentString(I18n
								.translateToLocal("item.card.deposit") + " "
								+ formatter.format(depositAmount) + " " + I18n.translateToLocal(
										depositAmount > 1 ? "general.currency.multiple" : "general.currency.single")));
						event.getItem().getEntityItem().stackSize -= depositAmount;
					}
					if (event.getItem().getEntityItem().stackSize == 0) {
						event.setCanceled(true);
					}
					break; // no need to continue. We are done here
				}
			}
		}
	}

	private long getAccountBalance(String accountNumber) {
		return UniversalAccounts.getInstance(world).getAccountBalance(accountNumber);
	}

	private void creditAccount(String accountNumber, long amount) {
		UniversalAccounts.getInstance(world).creditAccount(accountNumber, amount);
	}
}
