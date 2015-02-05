package universalcoins.commands;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
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
	public String getName() {
		return StatCollector.translateToLocal("command.rebalance.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return StatCollector.translateToLocal("command.rebalance.help");
	}
	
	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {
        return true;
    }

	@Override
	public void execute(ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayerMP) {
			if (args.length == 0) {
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
