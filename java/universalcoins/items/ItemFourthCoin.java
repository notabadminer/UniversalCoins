package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import universalcoins.UniversalCoins;

public class ItemFourthCoin extends ItemCoin {

	public ItemFourthCoin() {
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		DecimalFormat formatter = new DecimalFormat("###,###,###,###,###");
		list.add(formatter.format(stack.stackSize * UniversalCoins.coinValues[3]) + " "
				+ I18n.translateToLocal("general.currency.multiple"));
	}
}
