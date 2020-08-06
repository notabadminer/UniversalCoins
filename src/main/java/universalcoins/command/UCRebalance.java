package universalcoins.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import universalcoins.UniversalCoins;

public class UCRebalance extends UCCommandBase implements ICommand {

	@Override
	public String getName() {
		return "rebalance";
	}

	@Override
	public String getUsage(ICommandSender var1) {
		return "/rebalance : consolidates inventory coins into largest values possible";
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
				// give coins back to player
				givePlayerCoins(player, coinsFound);
			}
		}
	}
}
