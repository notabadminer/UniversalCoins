package universalcoins.items;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;

public class ItemSeller extends Item {
	
	private final String name = "itemSeller";

	public ItemSeller() {
		super();
		this.setUnlocalizedName("itemSeller");
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
		this.setMaxStackSize(1);
		GameRegistry.registerItem(this, name);
		setUnlocalizedName(UniversalCoins.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}
}
