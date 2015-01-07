package universalcoins.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import universalcoins.UniversalCoins;

public class UCGive extends CommandBase {
	private static final Item[] coins = new Item[] {
			UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack,
			UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag,
			UniversalCoins.proxy.itemLargeCoinBag };

	@Override
	public String getName() {
		return StatCollector.translateToLocal("command.givecoins.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return StatCollector.translateToLocal("command.givecoins.help");
	}

	@Override
	public void execute(ICommandSender sender, String[] args) {
		if (args.length == 2) {
			EntityPlayer recipient = null;
			WorldServer[] ws= MinecraftServer.getServer().worldServers;
			for(WorldServer w : ws) {
				if(w.playerEntities.contains(w.getPlayerEntityByName(args[0]))) {
					recipient = (EntityPlayer) w.getPlayerEntityByName(args[0]);
				}
			}
			int coinsToSend = 0;
			if (recipient == null) {
				sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.givecoins.error.notfound")));
				return;
			}
			try {
				coinsToSend = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.givecoins.error.badentry")));
				return;
			}
			givePlayerCoins(recipient, coinsToSend);
			sender.addChatMessage(new ChatComponentText("Gave " + args[0] + " " + args[1] + 
					" " + StatCollector.translateToLocal("item.itemCoin.name")));
			recipient.addChatMessage(new ChatComponentText( sender.getCommandSenderEntity().getName() + " " +
					StatCollector.translateToLocal("command.givecoins.result") + " " + args[1] + 
					" " + StatCollector.translateToLocal("item.itemCoin.name")));
		} else
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.givecoins.error.noname")));
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
}
