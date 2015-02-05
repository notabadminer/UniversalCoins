package universalcoins.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import universalcoins.UniversalCoins;

public class UCSend extends CommandBase {
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] {
			UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack,
			UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag,
			UniversalCoins.proxy.itemLargeCoinBag };

	@Override
	public String getName() {
		return StatCollector.translateToLocal("command.send.name");
	}
	
	@Override
	public List getAliases() {
		List aliases = new ArrayList();
		aliases.add("pay");
        return aliases;
    }


	@Override
	public String getCommandUsage(ICommandSender var1) {
		return StatCollector.translateToLocal("command.send.help");
	}
	
	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {
        return true;
    }

	@Override
	public void execute(ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayerMP) {
			if (args.length == 2) {
				// check for player
				EntityPlayerMP recipient = null;
				WorldServer[] ws = MinecraftServer.getServer().worldServers;
				for (WorldServer w : ws) {
					if (w.playerEntities.contains(w.getPlayerEntityByName(args[0]))) { 
						recipient = (EntityPlayerMP) w.getPlayerEntityByName(args[0]);
					}
				}
				if (recipient == null) {
					sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.send.error.notfound")));
					return;
				}
				int requestedSendAmount = 0;
				try {
					requestedSendAmount = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.send.error.badentry")));
					return;
				}
				//TODO get sender account, check balance, get coins
				if (getPlayerCoins((EntityPlayerMP) sender) < requestedSendAmount) {
					sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.send.error.insufficient")));
					return;
				}
				// get coins from player inventory
				int coinsFromSender = 0;
				EntityPlayerMP player = (EntityPlayerMP) sender;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					for (int j = 0; j < coins.length; j++) {
						if (stack != null && stack.getItem() == coins[j]
								&& coinsFromSender < requestedSendAmount) {
							coinsFromSender += stack.stackSize * multiplier[j];
							player.inventory.setInventorySlotContents(i, null);
						}
					}
				}
				// subtract coins to send from player coins
				coinsFromSender -= requestedSendAmount;
				//TODO find recipient account, deposit coins
				// send coins to recipient
				int coinChange = givePlayerCoins(recipient, requestedSendAmount);
				sender.addChatMessage(new ChatComponentText((requestedSendAmount - coinChange) + " " + 
						StatCollector.translateToLocal("command.send.result.sender") + " " + args[0]));
				recipient.addChatMessage(new ChatComponentText((requestedSendAmount - coinChange) + " " + 
						StatCollector.translateToLocal("command.send.result.receiver") + " " + sender.getName()));
				// add change back to sender coins
				coinsFromSender += coinChange;
				// give sender back change
				int leftOvers = givePlayerCoins(player, coinsFromSender);
				//if we have coins that won't fit in inventory, dump them to the world
				if (leftOvers > 0) {
					World world = ((EntityPlayerMP) sender).worldObj;
					Random rand = new Random();
					while (leftOvers > 0) {
						float rx = rand.nextFloat() * 0.8F + 0.1F;
						float ry = rand.nextFloat() * 0.8F + 0.1F;
						float rz = rand.nextFloat() * 0.8F + 0.1F;
						int logVal = Math.min((int) (Math.log(leftOvers) / Math.log(9)), 4);
						int stackSize = Math.min((int) (leftOvers / Math.pow(9, logVal)), 64);
						EntityItem entityItem = new EntityItem( world, sender.getCommandSenderEntity().posX + rx, 
								sender.getCommandSenderEntity().posY + ry, sender.getCommandSenderEntity().posZ + rz, 
								new ItemStack(coins[logVal], stackSize));
						world.spawnEntityInWorld(entityItem);
						leftOvers -= Math.pow(9, logVal) * stackSize;
					}
				}
			} else
				sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("command.send.error.incomplete")));
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
							int amountToAdd = (int) Math.min( coinsLeft / Math.pow(9, j), stack.getMaxStackSize() - stack.stackSize);
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
