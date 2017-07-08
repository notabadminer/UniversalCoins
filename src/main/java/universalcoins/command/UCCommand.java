package universalcoins.command;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import universalcoins.util.UCItemPricer;

public class UCCommand extends CommandBase implements ICommand {
	DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");

	@Override
	public String getName() {
		return "universalcoins";
	}

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "/universalcoins help";
	}

	@Override
	public List getAliases() {
		List aliases = new ArrayList();
		aliases.add("uc");
		return aliases;
	}

	// Method called when the command is typed in
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length <= 0) {
			throw new WrongUsageException(this.getUsage(sender));
		} else if (args[0].matches("help")) {
			sender.sendMessage(new TextComponentString("Usage: /uc <command option> <arguments>"));
			sender.sendMessage(new TextComponentString("Available command options:"));
			sender.sendMessage(new TextComponentString("/uc get : Get price of item held in main hand."));
			sender.sendMessage(new TextComponentString("/uc set <price> : Set price of item held in main hand."));
			sender.sendMessage(
					new TextComponentString("/uc reload : Reload pricelists. This will reset any unsaved changes."));
			sender.sendMessage(new TextComponentString(
					"/uc reset : Reset all prices to defaults. Will not override items not priced by default"));
			sender.sendMessage(new TextComponentString("/uc save : Save pricelists."));
			sender.sendMessage(new TextComponentString("/uc update : rerun auto-pricing"));
		} else if (args[0].matches("reload")) {
			UCItemPricer.getInstance().loadConfigs();
		} else if (args[0].matches("get")) {
			// get item price
			if (args.length == 1) {
				int price = -1;
				String stackName = "";
				ItemStack stack = getPlayerItem(sender);
				if (stack != null) {
					price = UCItemPricer.getInstance().getItemPrice(stack);
					stackName = getPlayerItem(sender).getDisplayName();
				} else {
					sender.sendMessage(new TextComponentString("§cError: no item found in hand."));
				}

				if (price == -1) {
					sender.sendMessage(new TextComponentString("§cNo price set for" + " " + stackName));
				} else
					sender.sendMessage(new TextComponentString("§a" + stackName + " = " + formatter.format(price)));
			}
		} else if (args[0].matches("set")) {
			// set item price
			if (args.length > 1) {
				boolean result = false;
				int price = -1;
				try {
					price = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sender.sendMessage(new TextComponentString("§cError: invalid price"));
					return;
				}
				ItemStack stack = getPlayerItem(sender);
				if (stack != null) {
					result = UCItemPricer.getInstance().setItemPrice(stack, price);
				}

				if (result == true) {
					sender.sendMessage(new TextComponentString("Price set to " + formatter.format(price)));
				} else {
					sender.sendMessage(new TextComponentString("§cFailed to set price."));
				}
			} else
				sender.sendMessage(new TextComponentString("Please specify price."));
		} else if (args[0].matches("reload")) {
			UCItemPricer.getInstance().loadConfigs();
			sender.sendMessage(new TextComponentString("§aAll changes since last save have been reset."));
		} else if (args[0].matches("reset")) {
			UCItemPricer.getInstance().resetDefaults();
			sender.sendMessage(new TextComponentString("§aPrice defaults reloaded."));
		} else if (args[0].matches("save")) {
			UCItemPricer.getInstance().savePriceLists();
			sender.sendMessage(new TextComponentString("§aChanges will be saved."));
		} else if (args[0].matches("update")) {
			UCItemPricer.getInstance().updatePriceLists();
			sender.sendMessage(new TextComponentString("§aPrices updated."));
		}
	}

	private ItemStack getPlayerItem(ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		if (player.getHeldItemMainhand() != null) {
			ItemStack stack = player.getHeldItemMainhand();
			return stack;
		}
		return null;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) {
			List<String> options = new ArrayList<String>();
			options.add("help");
			options.add("get");
			options.add("set");
			options.add("reload");
			options.add("reset");
			options.add("save");
			options.add("update");
			return getListOfStringsMatchingLastWord(args, options);
		}
		return null;
	}
}
