package universalcoins.commands;

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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import universalcoins.UniversalCoins;

public class UCSend extends CommandBase implements ICommand {

	@Override
	public String getName() {
		return "send";
	}

	@Override
	public List getAliases() {
		List aliases = new ArrayList();
		aliases.add("pay");
		return aliases;
	}

	@Override
	public String getUsage(ICommandSender var1) {
		return "/send <playername> <amount> : send another player coins from your inventory";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayerMP) {
			if (args.length == 2) {
				// check for player
				EntityPlayerMP recipient = null;
				WorldServer[] ws = server.worlds;
				for (WorldServer w : ws) {
					if (w.playerEntities.contains(w.getPlayerEntityByName(args[0]))) {
						recipient = (EntityPlayerMP) w.getPlayerEntityByName(args[0]);
					}
				}
				if (recipient == null) {
					sender.sendMessage(new TextComponentString("�cError: player not found"));
					return;
				}
				int requestedSendAmount = 0;
				try {
					requestedSendAmount = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sender.sendMessage(new TextComponentString("�cError: invalid number format"));
					return;
				}
				if (requestedSendAmount <= 0) {
					sender.sendMessage(new TextComponentString("�cError: specify an amount greater than zero"));
					return;
				}
				// get coins from player inventory
				int coinsFound = 0;
				EntityPlayerMP player = (EntityPlayerMP) sender;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (stack != null) {
						switch (stack.getUnlocalizedName()) {
						case "item.iron_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[0];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.gold_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[1];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.emerald_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[2];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.diamond_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[3];
							player.inventory.setInventorySlotContents(i, null);
							break;
						case "item.obsidian_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[4];
							player.inventory.setInventorySlotContents(i, null);
							break;
						}
					}
				}
				// if sender is short, cancel this transaction and return coins.
				if (coinsFound < requestedSendAmount) {
					sender.sendMessage(new TextComponentString("�cError: insufficent coins."));
					givePlayerCoins((EntityPlayerMP) sender, coinsFound);
					return;
				}
				// subtract coins to send from player coins
				coinsFound -= requestedSendAmount;
				// send coins to recipient
				givePlayerCoins(recipient, requestedSendAmount);
				sender.sendMessage(new TextComponentString(requestedSendAmount + " coin sent to " + " " + args[0]));
				recipient.sendMessage(
						new TextComponentString(requestedSendAmount + " coin sent to you from " + sender.getName()));
				// give sender back change
				givePlayerCoins(player, coinsFound);
			} else
				sender.sendMessage(new TextComponentString("�cPlease specify recipient and amount."));
		}
	}

	private void givePlayerCoins(EntityPlayer recipient, int coinsLeft) {
		ItemStack stack = null;
		while (coinsLeft > 0) {
			if (coinsLeft > UniversalCoins.coinValues[4]) {
				stack = new ItemStack(UniversalCoins.proxy.obsidian_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[4]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[4];
			} else if (coinsLeft > UniversalCoins.coinValues[3]) {
				stack = new ItemStack(UniversalCoins.proxy.diamond_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[3]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[3];
			} else if (coinsLeft > UniversalCoins.coinValues[2]) {
				stack = new ItemStack(UniversalCoins.proxy.emerald_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[2]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[2];
			} else if (coinsLeft > UniversalCoins.coinValues[1]) {
				stack = new ItemStack(UniversalCoins.proxy.gold_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[1]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[1];
			} else if (coinsLeft > UniversalCoins.coinValues[0]) {
				stack = new ItemStack(UniversalCoins.proxy.iron_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[0]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[0];
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
						int amountToAdd = (int) Math.min(stack.getCount(),
								istack.getMaxStackSize() - istack.getCount());
						istack.setCount(istack.getCount() + amountToAdd);
						stack.setCount(stack.getCount() - amountToAdd);
					}
				}
				// at this point, we're going to throw extra to the world since
				// the player inventory must be full.
				World world = ((EntityPlayerMP) recipient).world;
				Random rand = new Random();
				float rx = rand.nextFloat() * 0.8F + 0.1F;
				float ry = rand.nextFloat() * 0.8F + 0.1F;
				float rz = rand.nextFloat() * 0.8F + 0.1F;
				EntityItem entityItem = new EntityItem(world, ((EntityPlayerMP) recipient).posX + rx,
						((EntityPlayerMP) recipient).posY + ry, ((EntityPlayerMP) recipient).posZ + rz, stack);
				world.spawnEntity(entityItem);
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
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
