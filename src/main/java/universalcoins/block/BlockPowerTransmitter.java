package universalcoins.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TilePowerTransmitter;

public class BlockPowerTransmitter extends BlockProtected {

	public BlockPowerTransmitter() {
		super(Material.IRON);
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, player, stack);
		if (world.isRemote)
			return;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TilePowerTransmitter) {
			TilePowerTransmitter tentity = (TilePowerTransmitter) te;
			tentity.blockOwner = player.getName();
		}
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te != null && te instanceof TilePowerTransmitter) {
			TilePowerTransmitter tentity = (TilePowerTransmitter) te;
			if (tentity.publicAccess || playerIn.getName().matches(tentity.blockOwner)) {
				tentity.blockOwner = playerIn.getName();
				playerIn.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
			if (!worldIn.isRemote) {
				playerIn.sendMessage(new TextComponentTranslation("chat.warning.private"));
			}
		}
		return false;
	}

	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TilePowerTransmitter();
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		java.util.List<ItemStack> ret = new java.util.ArrayList<ItemStack>();
		TilePowerTransmitter te = world.getTileEntity(pos) instanceof TilePowerTransmitter
				? (TilePowerTransmitter) world.getTileEntity(pos) : null;
		ItemStack stack = new ItemStack(UniversalCoins.Blocks.power_transmitter, 1);
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
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te,
			ItemStack tool) {
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}
}