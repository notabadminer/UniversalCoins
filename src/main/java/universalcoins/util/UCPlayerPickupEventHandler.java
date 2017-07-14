package universalcoins.util;

import java.text.DecimalFormat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import universalcoins.UniversalCoins;

public class UCPlayerPickupEventHandler {

	private World world;
	private String accountNumber;

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		if (event.getItem().getItem().getItem() == UniversalCoins.Items.iron_coin
				|| event.getItem().getItem().getItem() == UniversalCoins.Items.gold_coin
				|| event.getItem().getItem().getItem() == UniversalCoins.Items.emerald_coin
				|| event.getItem().getItem().getItem() == UniversalCoins.Items.diamond_coin
				|| event.getItem().getItem().getItem() == UniversalCoins.Items.obsidian_coin) {
			// event.getEntityPlayer().addStat(Achievements.achCoin, 1);
			world = event.getEntityPlayer().world;
			EntityPlayer player = event.getEntityPlayer();
			NonNullList<ItemStack> inventory = player.inventory.mainInventory;
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			for (int i = 0; i < inventory.size(); i++) {
				if (inventory.get(i) != null && inventory.get(i).getItem() == UniversalCoins.Items.ender_card) {
					if (!inventory.get(i).hasTagCompound())
						return; // card has not been initialized. Nothing we can
								// do here
					accountNumber = inventory.get(i).getTagCompound().getString("Account");
					FMLLog.info("Account: " + accountNumber);
					long accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
					if (accountBalance == -1)
						return; // get out of here if the card is invalid
					if (event.getItem().getItem().getCount() == 0)
						return; // no need to notify on zero size stack
					int coinValue = 0;
					int depositAmount = 0;
					int stackSize = event.getItem().getItem().getCount();
					switch (event.getItem().getItem().getUnlocalizedName()) {
					case "item.universalcoins.iron_coin":
						coinValue = UniversalCoins.coinValues[0];
						break;
					case "item.universalcoins.gold_coin":
						coinValue = UniversalCoins.coinValues[1];
						break;
					case "item.universalcoins.emerald_coin":
						coinValue = UniversalCoins.coinValues[2];
						break;
					case "item.universalcoins.diamond_coin":
						coinValue = UniversalCoins.coinValues[3];
						break;
					case "item.universalcoins.obsidian_coin":
						coinValue = UniversalCoins.coinValues[4];
						break;
					}
					if (UniversalAccounts.getInstance().creditAccount(accountNumber, coinValue * stackSize, true)) {
						UniversalAccounts.getInstance().creditAccount(accountNumber, coinValue * stackSize, false);
						event.getItem().getItem().setCount(0);
						player.sendMessage(new TextComponentString(I18n.translateToLocal("item.card.deposit") + " "
								+ formatter.format(stackSize * coinValue) + " "
								+ I18n.translateToLocal("general.currency.single")));
						break; // no need to continue. We are done here
					}
				}
			}
		}
	}
}
