package universalcoins.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import universalcoins.UniversalCoins;

public class UCGive extends CommandBase implements ICommand {

	@Override
	public String getCommandName() {
		return I18n.translateToLocal("command.givecoins.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return I18n.translateToLocal("command.givecoins.help");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 2) {
			EntityPlayer recipient = null;
			WorldServer[] ws = server.worldServers;
			for (WorldServer w : ws) {
				if (w.playerEntities.contains(w.getPlayerEntityByName(args[0]))) {
					recipient = (EntityPlayer) w.getPlayerEntityByName(args[0]);
				}
			}
			int coinsToSend = 0;
			if (recipient == null) {
				sender.addChatMessage(
						new TextComponentString("§c" + I18n.translateToLocal("command.givecoins.error.notfound")));
				return;
			}
			try {
				coinsToSend = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.addChatMessage(
						new TextComponentString("§c" + I18n.translateToLocal("command.givecoins.error.badentry")));
				return;
			}
			if (coinsToSend <= 0) {
				sender.addChatMessage(
						new TextComponentString("§c" + I18n.translateToLocal("command.send.error.badentry")));
				return;
			}
			// we made it through the exceptions, give the coins to the player
			givePlayerCoins(recipient, coinsToSend);
			DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");
			sender.addChatMessage(new TextComponentString(
					"Gave " + args[0] + " " + formatter.format(coinsToSend) + " " + I18n.translateToLocal(
							coinsToSend > 1 ? "general.currency.multiple" : "general.currency.single")));
			recipient.addChatMessage(
					new TextComponentString(sender.getName() + " " + I18n.translateToLocal("command.givecoins.result")
							+ " " + (coinsToSend) + " " + I18n.translateToLocal(
									coinsToSend > 1 ? "general.currency.multiple" : "general.currency.single")));
		} else
			sender.addChatMessage(
					new TextComponentString("§c" + I18n.translateToLocal("command.givecoins.error.noname")));
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
				// at this point, we're going to throw extra to the world since
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

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos pos) {
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
