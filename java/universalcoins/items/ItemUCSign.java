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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.blocks.BlockUCStandingSign;
import universalcoins.blocks.BlockUCWallSign;
import universalcoins.tileentity.TileUCSign;

public class ItemUCSign extends ItemSign {

	public ItemUCSign() {
		this.maxStackSize = 16;
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing == EnumFacing.DOWN) {
			return EnumActionResult.FAIL;
		} else if (!worldIn.getBlockState(pos).getBlock().getMaterial(null).isSolid()) {
			return EnumActionResult.FAIL;
		} else {
			pos = pos.offset(facing);

			if (!playerIn.canPlayerEdit(pos, facing, stack)) {
				return EnumActionResult.SUCCESS;
			} else if (!Blocks.STANDING_SIGN.canPlaceBlockAt(worldIn, pos)) {
				return EnumActionResult.SUCCESS;
			} else {
				if (facing == EnumFacing.UP) {
					int i = MathHelper.floor_double((double) ((playerIn.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D)
							& 15;
					worldIn.setBlockState(pos, UniversalCoins.proxy.standing_ucsign.getDefaultState()
							.withProperty(BlockUCStandingSign.ROTATION, Integer.valueOf(i)), 3);
				} else {
					worldIn.setBlockState(pos, UniversalCoins.proxy.wall_ucsign.getDefaultState()
							.withProperty(BlockUCWallSign.FACING, facing), 3);
				}

				--stack.stackSize;
				TileEntity tileentity = worldIn.getTileEntity(pos);

				if (tileentity instanceof TileUCSign) {
					((TileUCSign) tileentity).blockOwner = playerIn.getName();
					playerIn.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}

				return EnumActionResult.SUCCESS;
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
