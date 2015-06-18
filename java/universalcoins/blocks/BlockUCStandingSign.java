package universalcoins.blocks;

import java.util.Random;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

public class BlockUCStandingSign extends BlockStandingSign {

	private Class signEntityClass;
	private boolean isStanding;

	public BlockUCStandingSign(Class tileEntity) {
		super();
		this.isStanding = true;
		this.signEntityClass = tileEntity;
		float f = 0.25F;
		float f1 = 1.0F;
		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
	}
	
	@Override
	public int getRenderType() {
        return 2;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.getSelectedBoundingBox(worldIn, pos);
    }

	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_) {
		try {
			return (TileUCSign) this.signEntityClass.newInstance();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileUCSign) {
			TileUCSign tentity = (TileUCSign) tileEntity;
			if (player.getCommandSenderEntity().getName().matches(tentity.blockOwner)) {
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		String ownerName = ((TileUCSign) world.getTileEntity(pos)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, pos, player, willHarvest);
			return false;
		}
		if (player.getDisplayName().equals(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, pos.getX(), pos.getY(), pos.getZ());
			EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			world.spawnEntityInWorld(entityItem);
			super.removedByPlayer(world, pos, player, willHarvest);
		}
		return false;
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(UniversalCoins.proxy.itemUCSign);
		TileEntity tentity = world.getTileEntity(new BlockPos(x, y, z));
		if (tentity instanceof TileUCSign) {
			TileUCSign te = (TileUCSign) tentity;
			NBTTagCompound tagCompound = new NBTTagCompound();
			tagCompound.setString("BlockIcon", te.blockIcon);
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return UniversalCoins.proxy.itemUCSign;
	}
}
