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
import universalcoins.UniversalCoins;
import universalcoins.util.UCStaticItemPricer;

public class UCCommand extends CommandBase implements ICommand {

	private boolean firstChange = true;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");

	@Override
	public String getCommandName() {
		return I18n.translateToLocal("command.uccommand.name");
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return I18n.translateToLocal("command.uccommand.help");
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
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.help.name"))) {
			sender.addChatMessage(new TextComponentString(I18n.translateToLocal("command.uccommand.usage")));
			sender.addChatMessage(new TextComponentString(I18n.translateToLocal("command.uccommand.commandheader")));
			sender.addChatMessage(new TextComponentString(I18n.translateToLocal("command.uccommand.option.get.help")));
			sender.addChatMessage(new TextComponentString(I18n.translateToLocal("command.uccommand.option.set.help")));
			sender.addChatMessage(
					new TextComponentString(I18n.translateToLocal("command.uccommand.option.reload.help")));
			sender.addChatMessage(
					new TextComponentString(I18n.translateToLocal("command.uccommand.option.reset.help")));
			sender.addChatMessage(new TextComponentString(I18n.translateToLocal("command.uccommand.option.save.help")));
			sender.addChatMessage(
					new TextComponentString(I18n.translateToLocal("command.uccommand.option.update.help")));
			sender.addChatMessage(new TextComponentString(I18n.translateToLocal("command.uccommand.usage.hint")));
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.reload.name"))) {
			UniversalCoins.itemPricer.loadConfigs();
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.get.name"))) {
			// get item price
			if (args.length > 1) {
				int price = -1;
				String stackName = "";
				if (args[1].matches(I18n.translateToLocal("command.uccommand.option.set.itemheld"))) {
					ItemStack stack = getPlayerItem(sender);
					if (stack != null) {
						price = UniversalCoins.itemPricer.getItemPrice(stack);
						stackName = getPlayerItem(sender).getDisplayName();
					}
				}
				if (price == -1) {
					sender.addChatMessage(new TextComponentString(
							"�c" + I18n.translateToLocal("command.uccommand.warning.pricenotset") + " " + stackName));
				} else
					sender.addChatMessage(
							new TextComponentString("�a" + I18n.translateToLocal("command.uccommand.warning.pricefound")
									+ " " + stackName + ": " + formatter.format(price)));
			} else
				sender.addChatMessage(
						new TextComponentString("�c" + I18n.translateToLocal("command.uccommand.warning.noitem")));
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.set.name"))) {
			// set item price
			if (args.length > 2) {
				boolean result = false;
				int price = -1;
				try {
					price = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					sender.addChatMessage(new TextComponentString(
							"�c" + I18n.translateToLocal("command.uccommand.option.set.price.invalid")));
					return;
				}
				if (args[1].matches(I18n.translateToLocal("command.uccommand.option.set.itemheld"))) {
					ItemStack stack = getPlayerItem(sender);
					if (stack != null) {
						result = UniversalCoins.itemPricer.setItemPrice(stack, price);
					}
				}
				if (result == true) {
					sender.addChatMessage(new TextComponentString(
							I18n.translateToLocal("command.uccommand.option.set.price") + " " + formatter.format(price)));
					if (firstChange) {
						sender.addChatMessage(new TextComponentString(
								I18n.translateToLocal("command.uccommand.option.set.price.firstuse.one")));
						sender.addChatMessage(new TextComponentString(
								I18n.translateToLocal("command.uccommand.option.set.price.firstuse.two")));
						sender.addChatMessage(new TextComponentString(
								I18n.translateToLocal("command.uccommand.option.set.price.firstuse.three")));
						firstChange = false;
					}
				} else {
					sender.addChatMessage(new TextComponentString(
							"�c" + I18n.translateToLocal("command.uccommand.option.set.price.fail.one")));
				}
			} else
				sender.addChatMessage(new TextComponentString(
						"�c" + I18n.translateToLocal("command.uccommand.option.set.price.error")));
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.reload"))) {
			UniversalCoins.itemPricer.loadConfigs();
			sender.addChatMessage(
					new TextComponentString("�a" + I18n.translateToLocal("command.uccommand.option.reload.confirm")));
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.reset.name"))) {
			UniversalCoins.itemPricer.resetDefaults();
			sender.addChatMessage(
					new TextComponentString("�a" + I18n.translateToLocal("command.uccommand.option.reset.confirm")));
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.save.name"))) {
			UniversalCoins.itemPricer.savePriceLists();
			sender.addChatMessage(
					new TextComponentString("�a" + I18n.translateToLocal("command.uccommand.option.save.confirm")));
		} else if (args[0].matches(I18n.translateToLocal("command.uccommand.option.update.name"))) {
			UniversalCoins.itemPricer.updatePriceLists();
			sender.addChatMessage(
					new TextComponentString("�a" + I18n.translateToLocal("command.uccommand.option.update.confirm")));
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
			options.add(I18n.translateToLocal("command.uccommand.option.help.name"));
			options.add(I18n.translateToLocal("command.uccommand.option.get.name"));
			options.add(I18n.translateToLocal("command.uccommand.option.set.name"));
			options.add(I18n.translateToLocal("command.uccommand.option.reload.name"));
			options.add(I18n.translateToLocal("command.uccommand.option.reset.name"));
			options.add(I18n.translateToLocal("command.uccommand.option.save.name"));
			options.add(I18n.translateToLocal("command.uccommand.option.update.name"));
			return getListOfStringsMatchingLastWord(args, options);
		}
		if (args.length == 2) {
			if (args[0].matches(I18n.translateToLocal("command.uccommand.option.get.name"))
					|| args[0].matches(I18n.translateToLocal("command.uccommand.option.set.name"))) {
				List<String> options = new ArrayList<String>();
				options.add(I18n.translateToLocal("command.uccommand.option.set.itemheld"));
				for (String item : UniversalCoins.itemPricer.getPricedItems()) {
					options.add(item);
				}
				return getListOfStringsMatchingLastWord(args, options);
			}
		}
		return null;
	}
}
