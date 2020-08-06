package universalcoins.item;

import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;

@SuppressWarnings("deprecation")
public class ItemCoin extends Item {

	public ItemCoin() {
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		DecimalFormat formatter = new DecimalFormat("###,###,###,###,###");
		tooltip.add(formatter.format(stack.getCount() * UniversalCoins.coinValues[0]) + " "
				+ (stack.getCount() * UniversalCoins.coinValues[0] > 1
						? I18n.translateToLocal("general.currency.multiple")
						: I18n.translateToLocal("general.currency.single")));
	}
}
