package universalcoins.command;

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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import universalcoins.UniversalCoins;

public class UCGive extends UCCommandBase implements ICommand {

	@Override
	public String getName() {
		return "givecoins";
	}

	@Override
	public String getUsage(ICommandSender var1) {
		return "/givecoins <player> <amount>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 2) {
			EntityPlayer recipient = null;
			WorldServer[] ws = server.worlds;
			for (WorldServer w : ws) {
				if (w.playerEntities.contains(w.getPlayerEntityByName(args[0]))) {
					recipient = (EntityPlayer) w.getPlayerEntityByName(args[0]);
				}
			}
			int coinsToSend = 0;
			if (recipient == null) {
				sender.sendMessage(new TextComponentString("§cPlayer not found!"));
				return;
			}
			try {
				coinsToSend = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(new TextComponentString("§cInvalid coin amount!"));
				return;
			}
			if (coinsToSend <= 0) {
				sender.sendMessage(new TextComponentString("§cInvalid coin amount!"));
				return;
			}
			// we made it through the exceptions, give the coins to the player
			givePlayerCoins(recipient, coinsToSend);
			DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");
			sender.sendMessage(
					new TextComponentString("Gave " + args[0] + " " + formatter.format(coinsToSend) + " Coin."));
			recipient.sendMessage(new TextComponentString(sender.getName() + " gave you " + (coinsToSend) + " coin."));
		} else
			sender.sendMessage(new TextComponentString("§cError: Please specify recipient and amount."));
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
