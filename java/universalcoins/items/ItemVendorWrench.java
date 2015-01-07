package universalcoins.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemVendorWrench extends Item {
	
	public ItemVendorWrench() {
		super();
		
		setFull3D();
		setMaxStackSize(1);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){
		this.itemIcon = par1IconRegister.registerIcon(UniversalCoins.modid + ":" + this.getUnlocalizedName().substring(5));
	}
	
	@Override
	  public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
	    return true;
	  }
}
