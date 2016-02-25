package universalcoins.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;

public class ItemVendorWrench extends Item {

	public ItemVendorWrench() {
		super();
		setFull3D();
		setMaxStackSize(1);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@Override
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			MinecraftServer.getServer().addChatMessage(new ChatComponentText("Wrench used by " + player.getDisplayName()
					+ " in World " + world.provider.getDimensionId() + " at " + hitX + " " + hitY + " " + hitZ));
		}
		return false;
	}
}
