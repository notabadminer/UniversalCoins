package universalcoins.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;

public class ItemVendorWrench extends Item {
	
	private final String name = "itemVendorWrench";
	
	public ItemVendorWrench() {
		super();
		
		setFull3D();
		setMaxStackSize(1);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		GameRegistry.registerItem(this, name);
		setUnlocalizedName(UniversalCoins.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}
}
