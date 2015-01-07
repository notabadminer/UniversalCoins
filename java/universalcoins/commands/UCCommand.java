package universalcoins.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import universalcoins.UniversalCoins;
import universalcoins.util.UCItemPricer;

public class UCCommand extends CommandBase {

	private boolean firstChange = true;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] {
			UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack,
			UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag,
			UniversalCoins.proxy.itemLargeCoinBag };

	@Override
	public String getCommandName() {
		return StatCollector.translateToLocal("command.uccommand.name");
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return StatCollector.translateToLocal("command.uccommand.help");
	}

	// Method called when the command is typed in
	@Override
	public void processCommand(ICommandSender sender, String[] astring) {
		if (astring.length <= 0) {
			throw new WrongUsageException(this.getCommandUsage(sender));
		} else if (astring[0].matches(StatCollector.translateToLocal("command.uccommand.option.help.name"))) {
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.usage")));
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.commandheader")));
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.get.help")));
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.set.help")));
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.reload.help")));
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.reset.help")));
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.save.help")));
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.usage.hint")));
		} else if (astring[0].matches(StatCollector.translateToLocal("command.uccommand.option.reload.name"))) {
			UCItemPricer.loadConfigs();
		} else if (astring[0].matches(StatCollector.translateToLocal("command.uccommand.option.get.name"))) {
			// get item price
			if (astring.length > 1) {
				int price = -1;
				if (astring[1].matches(StatCollector.translateToLocal("command.uccommand.option.set.itemheld"))) {
					ItemStack stack = getPlayerItem(sender);
					if (stack != null) {
						price = UCItemPricer.getItemPrice(stack);
					}
				} else {
					price = UCItemPricer.getItemPrice(astring[1]);
				}
				String stackName = getPlayerItem(sender).getUnlocalizedName();
				if (price == -1) {
					sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal(
							"command.uccommand.warning.pricenotset") + " " + stackName));
				} else
					sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal(
							"command.uccommand.warning.pricefound") + " " + stackName + ": " + price));
			} else
				sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.warning.noitem")));
		} else if (astring[0].matches(StatCollector.translateToLocal("command.uccommand.option.set.name"))) {
			// set item price
			if (astring.length > 2) {
				boolean result = false;
				int price = -1;
				try {
					price = Integer.parseInt(astring[2]);
				} catch (NumberFormatException e) {
					sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.set.price.invalid")));
					return;
				}
				if (astring[1].matches(StatCollector.translateToLocal("command.uccommand.option.set.itemheld"))) {
					ItemStack stack = getPlayerItem(sender);
					if (stack != null) {
						result = UCItemPricer.setItemPrice(stack, price);
					}
				} else {
					result = UCItemPricer.setItemPrice(astring[1], price);
				}
				if (result == true) {
					sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.set.price")
							+ " " + price));
					if (firstChange) {
						sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.set.price.firstuse.one")));
						sender.addChatMessage(new ChatComponentText(
								StatCollector.translateToLocal("command.uccommand.option.set.price.firstuse.two")));
						sender.addChatMessage(new ChatComponentText(
								StatCollector.translateToLocal("command.uccommand.option.set.price.firstuse.three")));
						firstChange = false;
					}
				} else {
					sender.addChatMessage(new ChatComponentText(
							StatCollector.translateToLocal("command.uccommand.option.set.price.fail.one")));
				}
			} else
				sender.addChatMessage(new ChatComponentText(
						StatCollector.translateToLocal("command.uccommand.option.set.price.error")));
		} else if (astring[0].matches(StatCollector.translateToLocal("command.uccommand.option.reload"))) {
			UCItemPricer.loadConfigs();
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.reload.confirm")));
		} else if (astring[0].matches(StatCollector.translateToLocal("command.uccommand.option.reset.name"))) {
			UCItemPricer.resetDefaults();
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.reset.confirm")));
		} else if (astring[0].matches(StatCollector.translateToLocal("command.uccommand.option.save.name"))) {
			UCItemPricer.updatePriceLists();
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.uccommand.option.save.confirm")));
		}
	}

	private ItemStack getPlayerItem(ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		if (player.getHeldItem() != null) {
			ItemStack stack = player.getHeldItem();
			return stack;
		}
		return null;
	}

	private int getCoinMultiplier(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return multiplier[i];
			}
		}
		return -1;
	}

	private int givePlayerCoins(EntityPlayer recipient, int coinsLeft) {
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

	private int getPlayerCoins(EntityPlayer player) {
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
