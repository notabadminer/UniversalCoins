package universalcoins;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

public class UCTab extends CreativeTabs {

	public UCTab(String label) {
		super(label);
	}

	@Override
	public Item getTabIconItem() {
		return Item.getItemFromBlock(UniversalCoins.proxy.tradestation);
	}

}
