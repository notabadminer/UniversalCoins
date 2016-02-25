package universalcoins.commands;

import java.text.DecimalFormat;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import universalcoins.Achievements;
import universalcoins.UniversalCoins;
import universalcoins.util.UniversalAccounts;

public class UCBalance extends CommandBase {
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };

	@Override
	public String getCommandName() {
		return StatCollector.translateToLocal("command.balance.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return StatCollector.translateToLocal("command.balance.help");
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] astring) {
		if (sender instanceof EntityPlayerMP) {
			int playerCoins = getPlayerCoins((EntityPlayerMP) sender);
			String uuid = ((EntityPlayerMP) sender).getPersistentID().toString();
			String playerAcct = UniversalAccounts.getInstance().getPlayerAccount(uuid);
			String customAcct = UniversalAccounts.getInstance().getCustomAccount(uuid);
			int accountBalance = UniversalAccounts.getInstance().getAccountBalance(playerAcct);
			int custAccountBalance = UniversalAccounts.getInstance().getAccountBalance(customAcct);
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			sender.addChatMessage(
					new ChatComponentText(StatCollector.translateToLocal("command.balance.result.inventory")
							+ formatter.format(playerCoins)));
			if (accountBalance != -1) {
				sender.addChatMessage(
						new ChatComponentText(StatCollector.translateToLocal("command.balance.result.account")
								+ formatter.format(accountBalance)));
			}
			if (custAccountBalance != -1) {
				sender.addChatMessage(
						new ChatComponentText(StatCollector.translateToLocal("command.balance.result.customaccount")
								+ formatter.format(custAccountBalance)));
			}
			// achievement stuff
			if (playerCoins > 1000 || accountBalance > 1000) {
				((EntityPlayerMP) sender).addStat(Achievements.achThousand, 1);
			}
			if (playerCoins > 1000000 || accountBalance > 1000000) {
				((EntityPlayerMP) sender).addStat(Achievements.achMillion, 1);
			}
			if (playerCoins > 1000000000 || accountBalance > 1000000000) {
				((EntityPlayerMP) sender).addStat(Achievements.achBillion, 1);
			}
			if (playerCoins == Integer.MAX_VALUE || accountBalance == Integer.MAX_VALUE) {
				((EntityPlayerMP) sender).addStat(Achievements.achMaxed, 1);
			}
		}
	}

	private int getPlayerCoins(EntityPlayerMP player) {
		int coinsFound = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			for (int j = 0; j < coins.length; j++) {
				if (stack != null && stack.getItem() == coins[j]) {
					coinsFound += stack.stackSize * multiplier[j];
				}
			}
		}
		return coinsFound;
	}
}
