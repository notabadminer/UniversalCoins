package universalcoins.commands;

import java.text.DecimalFormat;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import universalcoins.UniversalCoins;
import universalcoins.util.UCWorldData;

public class UCBalance extends CommandBase {
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] {
			UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack,
			UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag,
			UniversalCoins.proxy.itemLargeCoinBag };

	@Override
	public String getName() {
		return StatCollector.translateToLocal("command.balance.name");
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return StatCollector.translateToLocal("command.balance.help");
	}
	
	@Override
	public boolean canCommandSenderUse(ICommandSender par1ICommandSender) {
        return true;
    }

	@Override
	public void execute(ICommandSender sender, String[] astring) {
		if (sender instanceof EntityPlayerMP) {
			int playerCoins = getPlayerCoins((EntityPlayerMP) sender);
			String playerAcct = getPlayerAccount((EntityPlayerMP) sender);
			String customAcct = getCustomAccount((EntityPlayerMP) sender);
			int accountBalance = getAccountBalance((EntityPlayerMP) sender, playerAcct);
			int custAccountBalance = getAccountBalance((EntityPlayerMP) sender, customAcct);
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal(
					"command.balance.result.inventory") + formatter.format(playerCoins)));
			if (accountBalance != -1) {
				sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal(
						"command.balance.result.account") + formatter.format(accountBalance)));
			}
			if (custAccountBalance != -1) {
				sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal(
						"command.balance.result.customaccount") + formatter.format(custAccountBalance)));
			}
		}		
	}

	private int getPlayerCoins(EntityPlayerMP player) {
		int coinsFound = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			for (int j = 0; j < coins.length; j++) {
				if (stack != null && stack.getItem() == coins[j]) {
					coinsFound += stack.stackSize * multiplier[j];
				}
			}
		}
		return coinsFound;
	}
	
	private String getCustomAccount(EntityPlayerMP player){
		String accountName = getWorldString(player, "¿" + player.getUniqueID().toString());
		return getWorldString(player, accountName);
	}
	
	private String getPlayerAccount(EntityPlayerMP player) {
		//returns an empty string if no account found
		return getWorldString(player, player.getUniqueID().toString());
	}
	
	private int getAccountBalance(EntityPlayerMP player, String accountNumber) {
		if (getWorldString(player, accountNumber) != "") {
			return getWorldInt(player, accountNumber);
		} else return -1;	
	}
	
	private int getWorldInt(EntityPlayerMP player, String tag) {
		UCWorldData wData = UCWorldData.get(player.worldObj);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getInteger(tag);
	}
	
	private String getWorldString(EntityPlayerMP player, String tag) {
		UCWorldData wData = UCWorldData.get(player.worldObj);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getString(tag);
	}
}
