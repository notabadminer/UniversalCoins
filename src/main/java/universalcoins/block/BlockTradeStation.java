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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileTradeStation;

public class BlockTradeStation extends BlockProtected {

	public BlockTradeStation() {
		super(Material.IRON);
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000.0F);
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, player, stack);
		if (world.isRemote)
			return;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileTradeStation) {
			TileTradeStation tentity = (TileTradeStation) te;
			tentity.blockOwner = player.getName();
		}
		if (stack.hasDisplayName()) {
			((TileTradeStation) world.getTileEntity(pos)).setName(stack.getDisplayName());
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileTradeStation) {
			TileTradeStation tentity = (TileTradeStation) tileEntity;
			if (tentity.inUse) {
				if (!worldIn.isRemote) {
					playerIn.sendMessage(new TextComponentString(I18n.translateToLocal("chat.warning.inuse")));
				}
				return true;
			}
			if (tentity.publicAccess) {
				playerIn.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				tentity.playerName = playerIn.getName();
				tentity.inUse = true;
				return true;
			} else {
				if (tentity.blockOwner.matches(playerIn.getName())) {
					playerIn.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
					tentity.playerName = playerIn.getName();
					tentity.inUse = true;
					return true;
				}
				if (!worldIn.isRemote) {
					playerIn.sendMessage(new TextComponentString(I18n.translateToLocal("chat.warning.private")));
				}
			}
		}
		return false;
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
			int fortune) {
		TileEntity te = world.getTileEntity(pos);
		ItemStack stack = new ItemStack(UniversalCoins.Blocks.tradestation, 1);
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound tagCompound = new NBTTagCompound();
			te.writeToNBT(tag);
			tagCompound.setTag("BlockEntityTag", tag);
			stack.setTagCompound(tagCompound);
		}
		drops.add(stack);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTradeStation();
	}
}
