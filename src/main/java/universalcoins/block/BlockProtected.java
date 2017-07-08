package universalcoins.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileProtected;

public class BlockProtected extends BlockContainer {

	protected BlockProtected(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		if (UniversalCoins.blockProtection) {
			TileProtected tileEntity = (TileProtected) player.getEntityWorld().getTileEntity(pos);
			if (!player.capabilities.isCreativeMode && !tileEntity.blockOwner.equals(player.getName())) {
				return false;
			}
		}
		if (willHarvest)
			return true; // If it will harvest, delay deletion of the block
							// until after getDrops
		this.onBlockHarvested(world, pos, state, player);
		return world.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		if (world.isRemote)
			return;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileProtected) {
			TileProtected tentity = (TileProtected) te;
			tentity.inUse = false;
			tentity.blockOwner = player.getName();
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileProtected();
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		world.setBlockToAir(pos);
		onBlockDestroyedByExplosion(world, pos, explosion);
		EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this, 1));
		if (!world.isRemote)
			world.spawnEntity(entityItem);
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
			@Nullable TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, (TileEntity) null, stack);
		worldIn.setBlockToAir(pos);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (UniversalCoins.blockProtection) {
			String ownerName = ((TileProtected) world.getTileEntity(pos)).blockOwner;
			if (!player.capabilities.isCreativeMode && !player.getName().contentEquals(ownerName)) {
				this.setHardness(-1.0F);
			} else {
				this.setHardness(1.0F);
			}
		}
	}
}
