package universalcoins.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;

public class ItemVendorWrench extends Item {

	public ItemVendorWrench() {
		super();

		setFull3D();
		setMaxStackSize(1);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister
				.registerIcon(UniversalCoins.MODID + ":" + this.getUnlocalizedName().substring(5));
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_,
			float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		if (!world.isRemote) {
			MinecraftServer.getServer().addChatMessage(new ChatComponentText("Wrench used by " + player.getDisplayName()
					+ " in World " + world.provider.dimensionId + " at " + x + " " + y + " " + z));
		}
		return false;
	}
}
