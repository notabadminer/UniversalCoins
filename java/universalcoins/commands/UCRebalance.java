package universalcoins.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import universalcoins.UniversalCoins;

public class UCRebalance extends CommandBase {
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] {
			UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack,
			UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag,
			UniversalCoins.proxy.itemLargeCoinBag };

	@Override
	public String getCommandName() {
		return StatCollector.translateToLocal("command.rebalance.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return StatCollector.translateToLocal("command.rebalance.help");
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
        return true;
    }

	@Override
	public void processCommand(ICommandSender sender, String[] astring) {
		if (sender instanceof EntityPlayerMP) {
			if (astring.length == 0) {
				// get coins from player inventory
				int coinTotal = 0;
				EntityPlayerMP player = (EntityPlayerMP) sender;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					for (int j = 0; j < coins.length; j++) {
						if (stack != null && stack.getItem() == coins[j]) {
							coinTotal += stack.stackSize * multiplier[j];
							player.inventory.setInventorySlotContents(i, null);
						}
					}
				}
				// give sender back change
				int leftOvers = givePlayerCoins(player, coinTotal);
				if (leftOvers > 0) {
					//TODO spawn coins in world
				}
			}
		}
	}

	private int getCoinMultiplier(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return multiplier[i];
			}
		}
		return -1;
	}

	private int givePlayerCoins(EntityPlayerMP recipient, int coinsLeft) {
		while (coinsLeft > 0) {
			// use logarithm to find largest cointype for coins being sent
			int logVal = Math.min((int) (Math.log(coinsLeft) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinsLeft / Math.pow(9, logVal)), 64);
			// add a stack to the recipients inventory
			Boolean coinsAdded = recipient.inventory.addItemStackToInventory(new ItemStack(coins[logVal], stackSize));
			if (coinsAdded) {
				coinsLeft -= (stackSize * Math.pow(9, logVal));
			} else {
				return coinsLeft; // return change
			}
		}
		return 0;
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
