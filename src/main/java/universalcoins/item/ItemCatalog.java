package universalcoins.item;

import net.minecraft.item.Item;
import universalcoins.UniversalCoins;

public class ItemCatalog extends Item {

	public ItemCatalog() {
		super();
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
		this.setMaxStackSize(1);
	}
}
