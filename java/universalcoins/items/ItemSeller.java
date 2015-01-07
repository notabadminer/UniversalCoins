package universalcoins.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import universalcoins.UniversalCoins;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSeller extends Item {

	public ItemSeller() {
		super();
		this.setUnlocalizedName("itemSeller");
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
		this.setMaxStackSize(1);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){
		this.itemIcon = par1IconRegister.registerIcon(UniversalCoins.modid + ":" + this.getUnlocalizedName().substring(5));
	}
	
}
