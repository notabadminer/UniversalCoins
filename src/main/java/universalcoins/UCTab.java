package universalcoins;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class UCTab extends CreativeTabs {

	public UCTab(String label) {
		super(label);
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(UniversalCoins.proxy.tradestation);
	}

}
