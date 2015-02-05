package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileSafe;
import universalcoins.tile.TileVendorBlock;

public class BlockSafe extends BlockContainer {
	
	private final String name = "blockSafe";
		
	public BlockSafe() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + ":" + name);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileSafe) {
			TileSafe tentity = (TileSafe) te;
			if (player.getCommandSenderEntity().getName().matches(tentity.blockOwner)) {
				tentity.updateAccountBalance();
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
	}
		
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote) return;
		TileEntity te = world.getTileEntity(pos);
		if (te != null ) ((TileSafe)world.getTileEntity(pos)).blockOwner = player.getName();
		/*
		int l = MathHelper.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

		switch (l) {
		case 0:
			world.setBlockMetadataWithNotify(pos.getX(), pos.getY(), pos.getZ(), 2, l);
			break;
		case 1:
			world.setBlockMetadataWithNotify(x, y, z, 5, l);
			break;
		case 2:
			world.setBlockMetadataWithNotify(x, y, z, 3, l);
			break;
		case 3:
			world.setBlockMetadataWithNotify(x, y, z, 4, l);
			break;
		}*/
	}
	
	@Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
		String ownerName = ((TileSafe)worldIn.getTileEntity(pos)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.harvestBlock(worldIn, player, pos, state, te);
		}
		if (player.getDisplayName().equals(ownerName) && !worldIn.isRemote) {
			super.harvestBlock(worldIn, player, pos, state, te);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileSafe();
	}
	
	@Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        world.setBlockToAir(pos);
        onBlockDestroyedByExplosion(world, pos, explosion);
        EntityItem entityItem = new EntityItem( world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this, 1));
		if (!world.isRemote) world.spawnEntityInWorld(entityItem);
    }
}
