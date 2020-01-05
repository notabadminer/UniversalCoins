package universalcoins.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
			if (stack.hasDisplayName()) {
				tentity.setName(stack.getDisplayName());
			}
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
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTradeStation();
	}

	@Override
	public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
			IBlockState state, int fortune) {
		TileTradeStation te = world.getTileEntity(pos) instanceof TileTradeStation
				? (TileTradeStation) world.getTileEntity(pos)
				: null;
		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			te.writeToNBT(tag);
			stack.setTagInfo("BlockEntityTag", tag);
		}
		drops.add(stack);
	}
}
