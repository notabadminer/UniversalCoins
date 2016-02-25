package universalcoins.items;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.blocks.BlockUCStandingSign;
import universalcoins.blocks.BlockUCWallSign;
import universalcoins.tile.TileUCSign;

public class ItemUCSign extends ItemSign {

	public ItemUCSign() {
		this.maxStackSize = 16;
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (side == EnumFacing.DOWN) {
			return false;
		} else if (!worldIn.getBlockState(pos).getBlock().getMaterial().isSolid()) {
			return false;
		} else {
			pos = pos.offset(side);

			if (!playerIn.canPlayerEdit(pos, side, stack)) {
				return false;
			} else if (!Blocks.standing_sign.canPlaceBlockAt(worldIn, pos)) {
				return false;
			} else {
				if (side == EnumFacing.UP) {
					int i = MathHelper.floor_double((double) ((playerIn.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D)
							& 15;
					worldIn.setBlockState(pos, UniversalCoins.proxy.standing_ucsign.getDefaultState()
							.withProperty(BlockUCStandingSign.ROTATION, Integer.valueOf(i)), 3);
				} else {
					worldIn.setBlockState(pos, UniversalCoins.proxy.wall_ucsign.getDefaultState()
							.withProperty(BlockUCWallSign.FACING, side), 3);
				}

				--stack.stackSize;
				TileEntity tileentity = worldIn.getTileEntity(pos);

				if (tileentity instanceof TileUCSign) {
					((TileUCSign) tileentity).blockOwner = playerIn.getName();
					playerIn.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}

				return true;
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tagCompound = stack.getTagCompound();
			if (tagCompound.getString("BlockIcon") == "") {
				NBTTagList textureList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
				byte slot = tagCompound.getByte("Texture");
				ItemStack textureStack = ItemStack.loadItemStackFromNBT(tagCompound);
				ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
				String blockIcon = "";// imm.getItemModel(textureStack).getTexture().getIconName();
										// //TODO fix this
				if (blockIcon.startsWith("biomesoplenty")) {
					String[] iconInfo = blockIcon.split(":");
					String[] blockName = textureStack.getUnlocalizedName().split("\\.", 3);
					String woodType = blockName[2].replace("Plank", "");
					// hellbark does not follow the same naming convention
					if (woodType.contains("hell"))
						woodType = "hell_bark";
					blockIcon = iconInfo[0] + ":" + "plank_" + woodType;
					// bamboo needs a hack too
					if (blockIcon.contains("bamboo"))
						blockIcon = blockIcon.replace("plank_bambooThatching", "bamboothatching");
				}
				list.add(blockIcon);
			} else {
				list.add(tagCompound.getString("BlockIcon"));
			}
		}
	}
}
