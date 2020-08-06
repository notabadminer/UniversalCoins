package universalcoins.command;

import java.text.DecimalFormat;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import universalcoins.UniversalCoins;
import universalcoins.util.UniversalAccounts;

public class UCBalance extends CommandBase implements ICommand {

	@Override
	public String getName() {
		return "balance";
	}

	@Override
	public String getUsage(ICommandSender var1) {
		return "/balance : returns player inventory and account coin balances";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayerMP) {
			int playerCoins = getPlayerCoins((EntityPlayerMP) sender);
			String uuid = ((EntityPlayerMP) sender).getPersistentID().toString();
			String playerAcct = UniversalAccounts.getInstance().getPlayerAccount(uuid);
			long accountBalance = UniversalAccounts.getInstance().getAccountBalance(playerAcct);
			DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");
			sender.sendMessage(new TextComponentString("Inventory: " + formatter.format(playerCoins)));
			//TODO change translation strings
			//player.sendMessage(new TextComponentTranslation("text.message"));
			if (accountBalance != -1) {
				sender.sendMessage(new TextComponentString("Account: " + formatter.format(accountBalance)));
			}
			
			// achievement stuff		
//			if (accountBalance > 1000) {
//				((EntityPlayerMP) sender).addStat(Achievements.achThousand, 1);
//			}
//			if (accountBalance > 1000000) {
//				((EntityPlayerMP) sender).addStat(Achievements.achMillion, 1);
//			}
//			if (accountBalance > 1000000000) {
//				((EntityPlayerMP) sender).addStat(Achievements.achBillion, 1);
//			}
//			if (accountBalance > 1000000000000) {
//				((EntityPlayerMP) sender).addStat(Achievements.achTrillion, 1);
//			}
//			if (accountBalance > 1000000000000000) {
//				((EntityPlayerMP) sender).addStat(Achievements.achQuadrillion, 1);
//			}
//			if (accountBalance > 1000000000000000000) {
//					((EntityPlayerMP) sender).addStat(Achievements.achQuintillion, 1);
//				}
//			if (playerCoins == Long.MAX_VALUE || accountBalance == Long.MAX_VALUE) {
//				((EntityPlayerMP) sender).addStat(Achievements.achMaxed, 1);
//			}
		}
	}

	private int getPlayerCoins(EntityPlayerMP player) {
		int coinsFound = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null) {
				switch (stack.getUnlocalizedName()) {
				case "item.universalcoins.iron_coin":
					coinsFound += stack.getCount() * UniversalCoins.coinValues[0];
					break;
				case "item.universalcoins.gold_coin":
					coinsFound += stack.getCount() * UniversalCoins.coinValues[1];
					break;
				case "item.universalcoins.emerald_coin":
					coinsFound += stack.getCount() * UniversalCoins.coinValues[2];
					break;
				case "item.universalcoins.diamond_coin":
					coinsFound += stack.getCount() * UniversalCoins.coinValues[3];
					break;
				case "item.universalcoins.obsidian_coin":
					coinsFound += stack.getCount() * UniversalCoins.coinValues[4];
					break;
				}
			}
		}
		return coinsFound;
	}
}
