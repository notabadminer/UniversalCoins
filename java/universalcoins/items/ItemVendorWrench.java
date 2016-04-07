package universalcoins.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
	public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos,
			EntityPlayer player) {
		return true;
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			world.getMinecraftServer()
					.addChatMessage(new TextComponentString("Wrench used by " + player.getDisplayName() + " in World "
							+ world.provider.getDimension() + " at " + hitX + " " + hitY + " " + hitZ));
		}
		return EnumActionResult.SUCCESS;
	}
}
