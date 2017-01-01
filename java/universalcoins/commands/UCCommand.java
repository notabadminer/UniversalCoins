package universalcoins.commands;

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
import net.minecraft.util.text.translation.I18n;
import universalcoins.util.UCItemPricer;

public class UCCommand extends CommandBase implements ICommand {

	private boolean firstChange = true;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");

	@Override
	public String getCommandName() {
		return "universalcoins";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/universalcoins help";
	}

	@Override
	public List getCommandAliases() {
		List aliases = new ArrayList();
		aliases.add("uc");
		return aliases;
	}

	// Method called when the command is typed in
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length <= 0) {
			throw new WrongUsageException(this.getCommandUsage(sender));
		} else if (args[0].matches("help")) {
			sender.addChatMessage(new TextComponentString("Usage: /uc <command option> <arguments>"));
			sender.addChatMessage(new TextComponentString("Available command options:"));
			sender.addChatMessage(new TextComponentString("/uc get : Get price of item held in main hand."));
			sender.addChatMessage(new TextComponentString("/uc set <price> : Set price of item held in main hand."));
			sender.addChatMessage(
					new TextComponentString("/uc reload : Reload pricelists. This will reset any unsaved changes."));
			sender.addChatMessage(new TextComponentString(
					"/uc reset : Reset all prices to defaults. Will not override items not priced by default"));
			sender.addChatMessage(new TextComponentString("/uc save : Save pricelists."));
			sender.addChatMessage(new TextComponentString("/uc update : rerun auto-pricing"));
		} else if (args[0].matches("reload")) {
			UCItemPricer.getInstance().loadConfigs();
		} else if (args[0].matches("get")) {
			// get item price
			if (args.length > 1) {
				int price = -1;
				String stackName = "";
				ItemStack stack = getPlayerItem(sender);
				if (stack != null) {
					price = UCItemPricer.getInstance().getItemPrice(stack);
					stackName = getPlayerItem(sender).getDisplayName();
				} else {
					sender.addChatMessage(new TextComponentString("�cError: no item found in hand."));
				}

				if (price == -1) {
					sender.addChatMessage(new TextComponentString("�cNo price set for" + " " + stackName));
				} else
					sender.addChatMessage(
							new TextComponentString("�a" + stackName + " = " + formatter.format(price)));
			}
		} else if (args[0].matches("set")) {
			// set item price
			if (args.length > 1) {
				boolean result = false;
				int price = -1;
				try {
					price = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sender.addChatMessage(new TextComponentString("�cError: invalid price"));
					return;
				}
				ItemStack stack = getPlayerItem(sender);
				if (stack != null) {
					result = UCItemPricer.getInstance().setItemPrice(stack, price);
				}

				if (result == true) {
					sender.addChatMessage(new TextComponentString("Price set to " + formatter.format(price)));
					if (firstChange) {
						sender.addChatMessage(new TextComponentString("Changes will not be saved"));
						sender.addChatMessage(new TextComponentString("Run \"/uc save\" to save changes"));
						sender.addChatMessage(new TextComponentString("Run \"/uc reload\" to undo changes"));
						firstChange = false;
					}
				} else {
					sender.addChatMessage(new TextComponentString("�cFailed to set price."));
				}
			} else
				sender.addChatMessage(new TextComponentString("Please specify price."));
		} else if (args[0].matches("reload")) {
			UCItemPricer.getInstance().loadConfigs();
			sender.addChatMessage(new TextComponentString("�aAll changes since last save have been reset."));
		} else if (args[0].matches("reset")) {
			UCItemPricer.getInstance().resetDefaults();
			sender.addChatMessage(new TextComponentString("�aPrice defaults reloaded."));
		} else if (args[0].matches("save")) {
			UCItemPricer.getInstance().savePriceLists();
			sender.addChatMessage(new TextComponentString("�aChanges will be saved."));
		} else if (args[0].matches("update")) {
			UCItemPricer.getInstance().updatePriceLists();
			sender.addChatMessage(new TextComponentString("�aPrices updated."));
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
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos pos) {
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
