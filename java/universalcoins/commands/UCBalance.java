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
			long accountBalance = UniversalAccounts.getInstance().getAccountBalance(playerAcct);
			DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");
			sender.addChatMessage(
					new ChatComponentText(StatCollector.translateToLocal("command.balance.result.inventory")
							+ formatter.format(playerCoins)));
			if (accountBalance != -1) {
				sender.addChatMessage(
						new ChatComponentText(StatCollector.translateToLocal("command.balance.result.account")
								+ formatter.format(accountBalance)));
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
			if (stack != null) {
				switch (stack.getUnlocalizedName()) {
				case "item.iron_coin":
					coinsFound += stack.stackSize * UniversalCoins.coinValues[0];
					break;
				case "item.gold_coin":
					coinsFound += stack.stackSize * UniversalCoins.coinValues[1];
					break;
				case "item.emerald_coin":
					coinsFound += stack.stackSize * UniversalCoins.coinValues[2];
					break;
				case "item.diamond_coin":
					coinsFound += stack.stackSize * UniversalCoins.coinValues[3];
					break;
				case "item.obsidian_coin":
					coinsFound += stack.stackSize * UniversalCoins.coinValues[4];
					break;
				}
			}
		}
		return coinsFound;
	}
}
