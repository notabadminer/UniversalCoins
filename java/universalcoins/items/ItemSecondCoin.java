package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import universalcoins.UniversalCoins;

public class ItemSecondCoin extends ItemCoin {

	public ItemSecondCoin() {
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		DecimalFormat formatter = new DecimalFormat("###,###,###,###,###");
		list.add(formatter.format(stack.stackSize * UniversalCoins.coinValues[1]) + " "
				+ I18n.translateToLocal("general.currency.multiple"));
	}
}
