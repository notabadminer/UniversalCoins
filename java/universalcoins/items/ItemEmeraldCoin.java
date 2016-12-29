package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import universalcoins.UniversalCoins;

public class ItemEmeraldCoin extends ItemCoin {

	public ItemEmeraldCoin() {
		super();
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister
				.registerIcon(UniversalCoins.MODID + ":" + this.getUnlocalizedName().substring(5));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		DecimalFormat formatter = new DecimalFormat("###,###,###,###,###");
		list.add(formatter.format(stack.stackSize * UniversalCoins.coinValues[2]) + " "
				+ StatCollector.translateToLocal("general.currency.multiple"));
	}
}
