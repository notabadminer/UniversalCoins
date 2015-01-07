package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;

public class ItemLargeCoinStack extends Item {
	
	private final String name = "itemLargeCoinStack";

	public ItemLargeCoinStack() {
		super();
		this.setUnlocalizedName("itemLargeCoinStack");
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
		GameRegistry.registerItem(this, name);
		setUnlocalizedName(UniversalCoins.MODID + "_" + name);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		DecimalFormat formatter = new DecimalFormat("###,###,###");
		list.add(formatter.format(stack.stackSize * 81) + " Coins");
	}
	
	public String getName() {
		return name;
	}
}
