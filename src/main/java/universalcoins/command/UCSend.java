package universalcoins.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import universalcoins.UniversalCoins;

public class UCSend extends UCCommandBase implements ICommand {

	@Override
	public String getName() {
		return "send";
	}

	@Override
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<String>();
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
					sender.sendMessage(new TextComponentString("§cError: player not found"));
					return;
				}
				int requestedSendAmount = 0;
				try {
					requestedSendAmount = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sender.sendMessage(new TextComponentString("§cError: invalid number format"));
					return;
				}
				if (requestedSendAmount <= 0) {
					sender.sendMessage(new TextComponentString("§cError: specify an amount greater than zero"));
					return;
				}
				// get coins from player inventory
				int coinsFound = 0;
				EntityPlayerMP player = (EntityPlayerMP) sender;
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (stack != null) {
						switch (stack.getUnlocalizedName()) {
						case "item.universalcoins.iron_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[0];
							player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							break;
						case "item.universalcoins.gold_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[1];
							player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							break;
						case "item.universalcoins.emerald_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[2];
							player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							break;
						case "item.universalcoins.diamond_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[3];
							player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							break;
						case "item.universalcoins.obsidian_coin":
							coinsFound += stack.getCount() * UniversalCoins.coinValues[4];
							player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							break;
						}
					}
				}
				// if sender is short, cancel this transaction and return coins.
				if (coinsFound < requestedSendAmount) {
					sender.sendMessage(new TextComponentString("§cError: insufficent coins."));
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
				sender.sendMessage(new TextComponentString("§cPlease specify recipient and amount."));
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
