package universalcoins.item;

import java.text.DecimalFormat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import universalcoins.util.CoinUtils;
import universalcoins.util.UniversalAccounts;

public class ItemEnderCard extends ItemUCCard {

	@SuppressWarnings("deprecation")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return EnumActionResult.SUCCESS;
		if (player.getActiveItemStack().getTagCompound() == null) {
			createNBT(player.getActiveItemStack(), worldIn, player);
		}
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		NonNullList<ItemStack> inventory = player.inventory.mainInventory;
		String accountNumber = player.getActiveItemStack().getTagCompound().getString("Account");
		long accountBalance = UniversalAccounts.getInstance().getAccountBalance(accountNumber);
		int coinValue = 0;
		int depositAmount = 0;
		int coinsDeposited = 0;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack instack = player.inventory.getStackInSlot(i);
			coinValue = CoinUtils.getCoinValue(instack);
			if (accountBalance == -1)
				// get out of here if the card is invalid
				return EnumActionResult.SUCCESS;
			if (coinValue == 0)
				continue;
			if (Long.MAX_VALUE - accountBalance > coinValue * instack.getCount()) {
				depositAmount = instack.getCount();
			} else {
				depositAmount = (int) (Long.MAX_VALUE - accountBalance) / coinValue;
			}
			UniversalAccounts.getInstance().creditAccount(accountNumber, coinValue * depositAmount, false);
			coinsDeposited += coinValue * depositAmount;
			inventory.get(i).shrink(depositAmount);
			if (inventory.get(i).getCount() == 0) {
				player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
				player.inventoryContainer.detectAndSendChanges();
			}
		}
		if (coinsDeposited > 0) {
			player.sendMessage(new TextComponentString(I18n.translateToLocal("item.card.deposit") + " "
					+ formatter.format(coinsDeposited) + " " + I18n.translateToLocal(
							coinsDeposited > 1 ? "general.currency.multiple" : "general.currency.single")));
		}
		player.sendMessage(new TextComponentString(I18n.translateToLocal("item.card.balance") + " "
				+ formatter.format(UniversalAccounts.getInstance().getAccountBalance(accountNumber))));
		return EnumActionResult.SUCCESS;
	}

}