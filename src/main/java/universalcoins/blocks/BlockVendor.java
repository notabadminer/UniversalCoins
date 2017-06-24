package universalcoins.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileVendor;
import universalcoins.tileentity.TileVendorBlock;

public class BlockVendor extends BlockProtected {

	protected static final AxisAlignedBB DEFAULT_AABB = new AxisAlignedBB(0.0625f, 0.125f, 0.0625f, 0.9375f, 0.9375f,
			0.9375f);

	public BlockVendor() {
		super(Material.IRON);
		// setStepSound(stepSound.GLASS);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setHardness(0.3F);
		setResistance(6000.0F);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return DEFAULT_AABB;
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, player, stack);
		if (world.isRemote)
			return;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileVendor) {
			TileVendor tentity = (TileVendor) te;
			tentity.blockOwner = player.getName();
		}
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileVendor) {
			TileVendor tentity = (TileVendor) tileEntity;
			if (tentity.inUse) {
				if (!world.isRemote) {
					player.sendMessage(new TextComponentString(I18n.translateToLocal("chat.warning.inuse")));
				}
				return true;
			} else {
				tentity.updateEntity();
				tentity.playerName = player.getName();
				tentity.inUse = true;
				tentity.updateCoinsForPurchase();
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		return false;
	}

	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendorBlock();
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		java.util.List<ItemStack> ret = new java.util.ArrayList<ItemStack>();
		TileVendorBlock te = world.getTileEntity(pos) instanceof TileVendorBlock
				? (TileVendorBlock) world.getTileEntity(pos) : null;
		ItemStack stack = new ItemStack(UniversalCoins.Blocks.vendor, 1);
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound tagCompound = new NBTTagCompound();
			te.writeToNBT(tag);
			tagCompound.setTag("BlockEntityTag", tag);
			stack.setTagCompound(tagCompound);
		}
		ret.add(stack);
		return ret;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		if (willHarvest)
			return true; // If it will harvest, delay deletion of the block
							// until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te,
			ItemStack tool) {
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}
}
