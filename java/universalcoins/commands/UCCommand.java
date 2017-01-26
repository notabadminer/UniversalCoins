package universalcoins.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import universalcoins.util.UCItemPricer;

public class UCCommand extends CommandBase {

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
	public void processCommand(ICommandSender sender, String[] astring) {
		if (astring.length <= 0) {
			throw new WrongUsageException(this.getCommandUsage(sender));
		} else if (astring[0].matches("command.uccommand.option.help.name")) {
			sender.addChatMessage(new ChatComponentText("Usage: /universalcoins <command option> <arguments>"));
			sender.addChatMessage(new ChatComponentText("Available command options:"));
			sender.addChatMessage(new ChatComponentText("/universalcoins price : Get price of item held in hand."));
			sender.addChatMessage(new ChatComponentText("/universalcoins set <price> : Set price of item held in hand."));
			sender.addChatMessage(
					new ChatComponentText("/universalcoins reload : Reload pricelists. This will reset any unsaved changes."));
			sender.addChatMessage(new ChatComponentText(
					"/universalcoins reset : Reset all prices to defaults. Will not override items not priced by default"));
			sender.addChatMessage(new ChatComponentText("/universalcoins save : Save pricelists."));
			sender.addChatMessage(new ChatComponentText("/universalcoins update : rerun auto-pricing"));
		} else if (astring[0].matches("reload")) {
			UCItemPricer.getInstance().loadConfigs();
		} else if (astring[0].matches("price")) {
			// get item price
			int price = -1;
			String stackName = "";
			ItemStack stack = getPlayerItem(sender);
			if (stack != null) {
				price = UCItemPricer.getInstance().getItemPrice(stack);
				stackName = getPlayerItem(sender).getDisplayName();
			} else {
				sender.addChatMessage(new ChatComponentText("§cError: no item found in hand."));
				return;
			}
			if (price == -1) {
				sender.addChatMessage(new ChatComponentText("§cNo price set for" + " " + stackName));
			} else
				sender.addChatMessage(new ChatComponentText("§a" + stackName + " = " + formatter.format(price)));
		} else if (astring[0].matches("set")) {
			// set item price
			if (astring.length > 1) {
				boolean result = false;
				int price = -1;
				try {
					price = Integer.parseInt(astring[1]);
				} catch (NumberFormatException e) {
					sender.addChatMessage(new ChatComponentText("§cError: invalid price"));
					return;
				}
				ItemStack stack = getPlayerItem(sender);
				if (stack != null) {
					result = UCItemPricer.getInstance().setItemPrice(stack, price);
				}
				if (result == true) {
					sender.addChatMessage(new ChatComponentText("Price set to " + formatter.format(price)));
				} else {
					sender.addChatMessage(new ChatComponentText("§cFailed to set price."));
				}
			} else {
				sender.addChatMessage(new ChatComponentText("Please specify price."));
			}

		} else if (astring[0].matches("reload")) {
			UCItemPricer.getInstance().loadConfigs();
			sender.addChatMessage(new ChatComponentText("§aAll changes since last save have been reset."));
		} else if (astring[0].matches("reset")) {
			if (UCItemPricer.getInstance().resetDefaults()) {
				sender.addChatMessage(new ChatComponentText("§aPrice defaults reloaded."));
			} else {
				sender.addChatMessage(new ChatComponentText("§cFailed to load defaults."));
			}
		} else if (astring[0].matches("save")) {
			if (UCItemPricer.getInstance().savePriceLists()) {
				sender.addChatMessage(new ChatComponentText("§aChanges saved successfully."));
			} else {
				sender.addChatMessage(new ChatComponentText("§cFailed to save changes."));
			}
		} else if (astring[0].matches("update")) {
			UCItemPricer.getInstance().updatePriceLists();
			sender.addChatMessage(new ChatComponentText("§aPrices updated."));
		}
	}

	private ItemStack getPlayerItem(ICommandSender sender) {
		EntityPlayer player = (EntityPlayer) sender;
		if (player.getHeldItem() != null) {
			ItemStack stack = player.getHeldItem();
			return stack;
		}
		return null;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 1) {
			List<String> options = new ArrayList<String>();
			options.add("help");
			options.add("price");
			options.add("set");
			options.add("reload");
			options.add("reset");
			options.add("save");
			options.add("update");
			return getListOfStringsFromIterableMatchingLastWord(args, options);
		}
		return null;
	}
}
