package universalcoins.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.block.BlockUCStandingSign;
import universalcoins.block.BlockUCWallSign;
import universalcoins.tileentity.TileUCSign;

public class ItemUCSign extends ItemSign {

	public ItemUCSign() {
		this.maxStackSize = 16;
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing == EnumFacing.DOWN) {
			return EnumActionResult.FAIL;
		} else if (!worldIn.getBlockState(pos).getBlock().getMaterial(null).isSolid()) {
			return EnumActionResult.FAIL;
		} else {
			pos = pos.offset(facing);

			if (!player.canPlayerEdit(pos, facing, player.getActiveItemStack())) {
				return EnumActionResult.SUCCESS;
			} else if (!Blocks.STANDING_SIGN.canPlaceBlockAt(worldIn, pos)) {
				return EnumActionResult.SUCCESS;
			} else {
				if (facing == EnumFacing.UP) {
					int i = MathHelper.floor((double) ((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
					worldIn.setBlockState(pos, UniversalCoins.Blocks.standing_ucsign.getDefaultState()
							.withProperty(BlockUCStandingSign.ROTATION, Integer.valueOf(i)), 3);
				} else {
					worldIn.setBlockState(pos, UniversalCoins.Blocks.wall_ucsign.getDefaultState()
							.withProperty(BlockUCWallSign.FACING, facing), 3);
				}

				player.getActiveItemStack().shrink(1);
				TileEntity tileentity = worldIn.getTileEntity(pos);

				if (tileentity instanceof TileUCSign) {
					((TileUCSign) tileentity).blockOwner = player.getName();
					player.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}

				return EnumActionResult.SUCCESS;
			}
		}
	}
}
