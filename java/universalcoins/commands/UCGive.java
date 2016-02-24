package universalcoins.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import universalcoins.UniversalCoins;

public class UCGive extends CommandBase {
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };

	@Override
	public String getCommandName() {
		return StatCollector.translateToLocal("command.givecoins.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return StatCollector.translateToLocal("command.givecoins.help");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] astring) {
		if (astring.length == 2) {
			EntityPlayer recipient = null;
			WorldServer[] ws = MinecraftServer.getServer().worldServers;
			for (WorldServer w : ws) {
				if (w.playerEntities.contains(w.getPlayerEntityByName(astring[0]))) {
					recipient = (EntityPlayer) w.getPlayerEntityByName(astring[0]);
				}
			}
			int coinsToSend = 0;
			if (recipient == null) {
				sender.addChatMessage(new ChatComponentText(
						"§c" + StatCollector.translateToLocal("command.givecoins.error.notfound")));
			}
			try {
				coinsToSend = Integer.parseInt(astring[1]);
			} catch (NumberFormatException e) {
				sender.addChatMessage(new ChatComponentText(
						"§c" + StatCollector.translateToLocal("command.givecoins.error.badentry")));
			}
			int change = givePlayerCoins(recipient, coinsToSend);
			sender.addChatMessage(new ChatComponentText("Gave " + astring[0] + " " + (coinsToSend - change) + " "
					+ StatCollector.translateToLocal("item.itemCoin.name")));
			recipient.addChatMessage(new ChatComponentText(
					sender.getName() + " " + StatCollector.translateToLocal("command.givecoins.result") + " "
							+ (coinsToSend - change) + " " + StatCollector.translateToLocal("item.itemCoin.name")));
		} else
			sender.addChatMessage(
					new ChatComponentText("§c" + StatCollector.translateToLocal("command.givecoins.error.noname")));
	}

	private int givePlayerCoins(EntityPlayer recipient, int coinsLeft) {
		while (coinsLeft > 0) {
			// use logarithm to find largest cointype for coins being sent
			int logVal = Math.min((int) (Math.log(coinsLeft) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinsLeft / Math.pow(9, logVal)), 64);
			// add a stack to the recipients inventory
			if (recipient.inventory.getFirstEmptyStack() != -1) {
				recipient.inventory.addItemStackToInventory(new ItemStack(coins[logVal], stackSize));
				coinsLeft -= (stackSize * Math.pow(9, logVal));
			} else {
				for (int i = 0; i < recipient.inventory.getSizeInventory(); i++) {
					ItemStack stack = recipient.inventory.getStackInSlot(i);
					for (int j = 0; j < coins.length; j++) {
						if (stack != null && stack.getItem() == coins[j]) {
							int amountToAdd = (int) Math.min(coinsLeft / Math.pow(9, j),
									stack.getMaxStackSize() - stack.stackSize);
							stack.stackSize += amountToAdd;
							recipient.inventory.setInventorySlotContents(i, stack);
							coinsLeft -= (amountToAdd * Math.pow(9, j));
						}
					}
				}
				return coinsLeft; // return change
			}
		}
		return 0;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) {
			List<String> players = new ArrayList<String>();
			for (EntityPlayer p : (List<EntityPlayer>) sender.getEntityWorld().playerEntities) {
				players.add(p.getName());
			}
			return getListOfStringsMatchingLastWord(args, players);
		}
		return null;
	}
}
