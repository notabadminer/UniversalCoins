package universalcoins.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileVendor;
import universalcoins.tileentity.TileVendorFrame;

public class BlockVendorFrame extends BlockProtected {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	protected static final AxisAlignedBB FRAME_NORTH_AABB = new AxisAlignedBB(0.12f, 0.12f, 0f, 0.88f, 0.88f, 0.07f);
	protected static final AxisAlignedBB FRAME_SOUTH_AABB = new AxisAlignedBB(0.12f, 0.12f, 0.93f, 0.88f, 0.88f, 1.00f);
	protected static final AxisAlignedBB FRAME_EAST_AABB = new AxisAlignedBB(0.93f, 0.12f, 0.12f, 1.0f, 0.88f, 0.88f);
	protected static final AxisAlignedBB FRAME_WEST_AABB = new AxisAlignedBB(0.07f, 0.12f, 0.12f, 0f, 0.88f, 0.88f);

	public BlockVendorFrame() {
		super(Material.WOOD);
		setHardness(1.0f);
		setResistance(6000.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
		case EAST:
			return FRAME_EAST_AABB;
		case WEST:
			return FRAME_WEST_AABB;
		case SOUTH:
			return FRAME_SOUTH_AABB;
		case NORTH:
		default:
			return FRAME_NORTH_AABB;
		}
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, player.getHorizontalFacing()), 2);
		super.onBlockPlacedBy(world, pos, state, player, stack);
		if (world.isRemote)
			return;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileVendorFrame) {
			TileVendorFrame tentity = (TileVendorFrame) te;
			tentity.blockOwner = player.getName();
		}
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileVendor) {
			TileVendor tentity = (TileVendor) tileEntity;
			if (tentity.inUse) {
				if (!worldIn.isRemote) {
					playerIn.sendMessage(new TextComponentString(I18n.translateToLocal("chat.warning.inuse")));
				}
				return true;
			} else {
				tentity.updateTE();
				tentity.playerName = playerIn.getName();
				tentity.inUse = true;
				playerIn.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		return false;
	}

	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendorFrame();
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
			int fortune) {
		TileEntity te = world.getTileEntity(pos);
		ItemStack stack = new ItemStack(UniversalCoins.Blocks.vendor_frame, 1);
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound tagCompound = new NBTTagCompound();
			te.writeToNBT(tag);
			tagCompound.setTag("BlockEntityTag", tag);
			stack.setTagCompound(tagCompound);
		}
		drops.add(stack);
	}
}
