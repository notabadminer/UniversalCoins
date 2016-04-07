package universalcoins.commands;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;

public class UCRebalance extends CommandBase implements ICommand {

	@Override
	public String getCommandName() {
		return I18n.translateToLocal("command.rebalance.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return I18n.translateToLocal("command.rebalance.help");
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayerMP) {
			if (args.length == 0) {
				// get coins from player inventory
				int coinsFound = 0;
				EntityPlayerMP player = (EntityPlayerMP) sender;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (stack != null) {
						switch (stack.getUnlocalizedName()) {
						case "item.iron_coin":
							coinsFound += stack.stackSize * UniversalCoins.coinValues[0];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.gold_coin":
							coinsFound += stack.stackSize * UniversalCoins.coinValues[1];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.emerald_coin":
							coinsFound += stack.stackSize * UniversalCoins.coinValues[2];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.diamond_coin":
							coinsFound += stack.stackSize * UniversalCoins.coinValues[3];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.obsidian_coin":
							coinsFound += stack.stackSize * UniversalCoins.coinValues[4];
							player.inventory.setInventorySlotContents(i, null);
							break;
						}
					}
				}
				// give coins back to player
				givePlayerCoins(player, coinsFound);
			}
		}
	}

	private void givePlayerCoins(EntityPlayer recipient, int coinsLeft) {
		ItemStack stack = null;
		while (coinsLeft > 0) {
			if (coinsLeft > UniversalCoins.coinValues[4]) {
				stack = new ItemStack(UniversalCoins.proxy.obsidian_coin, 1);
				stack.stackSize = (int) Math.floor(coinsLeft / UniversalCoins.coinValues[4]);
				coinsLeft -= stack.stackSize * UniversalCoins.coinValues[4];
			} else if (coinsLeft > UniversalCoins.coinValues[3]) {
				stack = new ItemStack(UniversalCoins.proxy.diamond_coin, 1);
				stack.stackSize = (int) Math.floor(coinsLeft / UniversalCoins.coinValues[3]);
				coinsLeft -= stack.stackSize * UniversalCoins.coinValues[3];
			} else if (coinsLeft > UniversalCoins.coinValues[2]) {
				stack = new ItemStack(UniversalCoins.proxy.emerald_coin, 1);
				stack.stackSize = (int) Math.floor(coinsLeft / UniversalCoins.coinValues[2]);
				coinsLeft -= stack.stackSize * UniversalCoins.coinValues[2];
			} else if (coinsLeft > UniversalCoins.coinValues[1]) {
				stack = new ItemStack(UniversalCoins.proxy.gold_coin, 1);
				stack.stackSize = (int) Math.floor(coinsLeft / UniversalCoins.coinValues[1]);
				coinsLeft -= stack.stackSize * UniversalCoins.coinValues[1];
			} else if (coinsLeft > UniversalCoins.coinValues[0]) {
				stack = new ItemStack(UniversalCoins.proxy.iron_coin, 1);
				stack.stackSize = (int) Math.floor(coinsLeft / UniversalCoins.coinValues[0]);
				coinsLeft -= stack.stackSize * UniversalCoins.coinValues[0];
			}

			if (stack == null)
				return;

			// add a stack to the recipients inventory
			if (recipient.inventory.getFirstEmptyStack() != -1) {
				recipient.inventory.addItemStackToInventory(stack);
			} else {
				for (int i = 0; i < recipient.inventory.getSizeInventory(); i++) {
					ItemStack istack = recipient.inventory.getStackInSlot(i);
					if (istack != null && istack.getItem() == stack.getItem()) {
						int amountToAdd = (int) Math.min(stack.stackSize, istack.getMaxStackSize() - istack.stackSize);
						istack.stackSize += amountToAdd;
						stack.stackSize -= amountToAdd;
					}
				}
				if (stack.stackSize > 0) {
					// at this point, we're going to throw extra to the world
					// since
					// the player inventory must be full.
					World world = ((EntityPlayerMP) recipient).worldObj;
					Random rand = new Random();
					float rx = rand.nextFloat() * 0.8F + 0.1F;
					float ry = rand.nextFloat() * 0.8F + 0.1F;
					float rz = rand.nextFloat() * 0.8F + 0.1F;
					EntityItem entityItem = new EntityItem(world, ((EntityPlayerMP) recipient).posX + rx,
							((EntityPlayerMP) recipient).posY + ry, ((EntityPlayerMP) recipient).posZ + rz, stack);
					world.spawnEntityInWorld(entityItem);
				}
			}
		}
	}
}
