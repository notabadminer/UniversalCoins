package universalcoins.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileCardStation;

public class BlockCardStation extends BlockContainer {

	private final String name = "blockCardStation";
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockCardStation() {
		super(new Material(MapColor.stoneColor));
		//this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000000.0F);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + ":" + name);
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos,
			EnumFacing side) {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (((TileCardStation) tileEntity).inUse) {
			if (!world.isRemote) { player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse"))); }
			return true;
		} else {
        	player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        	((TileCardStation) tileEntity).playerName = player.getDisplayName().toString();
        	((TileCardStation) tileEntity).playerUID = player.getUniqueID().toString();
        	return true;
        }
	}
		
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote) return;
		int rotation = MathHelper.floor_double((double)((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		//world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileCardStation();
	}
	
	@Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        world.setBlockToAir(pos);
        onBlockDestroyedByExplosion(world, pos, explosion);
        EntityItem entityItem = new EntityItem( world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this, 1));
		if (!world.isRemote) world.spawnEntityInWorld(entityItem);
    }
}
