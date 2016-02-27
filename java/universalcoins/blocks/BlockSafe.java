package universalcoins.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileSafe;

public class BlockSafe extends BlockRotatable {

	public BlockSafe() {
		super();
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileSafe) {
			TileSafe tentity = (TileSafe) tileEntity;
			if (player.getCommandSenderEntity().getName().matches(tentity.blockOwner)) {
				if (!world.isRemote)
					tentity.updateAccountBalance();
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, player.getHorizontalFacing().getOpposite()), 2);
		if (world.isRemote)
			return;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileSafe) {
			TileSafe tentity = (TileSafe) tileEntity;
			tentity.blockOwner = player.getCommandSenderEntity().getName();
			tentity.setSafeAccount(player.getCommandSenderEntity().getName());
		}
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		String ownerName = ((TileSafe) world.getTileEntity(pos)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, pos, player, willHarvest);
		}
		if (player.getDisplayName().equals(ownerName) && !world.isRemote) {
			super.removedByPlayer(world, pos, player, willHarvest);
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileSafe();
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		world.setBlockToAir(pos);
		onBlockDestroyedByExplosion(world, pos, explosion);
		EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this, 1));
		if (!world.isRemote)
			world.spawnEntityInWorld(entityItem);
	}
}
