package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileCardStation;

public class BlockCardStation extends BlockContainer {
	
	private final String name = "blockCardStation";
	
	public BlockCardStation() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000000.0F);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }
	
	@Override
	public boolean isOpaqueCube() {
	   return false;
	}
	
	@Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		//public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		if (((TileCardStation) tileEntity).inUse) {
			if (!worldIn.isRemote) { playerIn.addChatMessage(new ChatComponentText(((TileCardStation) tileEntity).player + " " + 
					StatCollector.translateToLocal("chat.cardstation.warning.inuse"))); }
			return true;
		} else {
			playerIn.openGui(UniversalCoins.instance, 0, worldIn, xCoord, yCoord, zCoord);
        	((TileCardStation) tileEntity).player = playerIn.getDisplayName().toString();
        	return true;
        }
	}
		
	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		//set block meta so we can use it later for rotation
		int rotation = MathHelper.floor_double((double)((placer.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		worldIn.setBlockState(pos, state); //TODO set state from rotation
		//.setBlockMetadataWithNotify(pos, rotation, 2);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileCardStation();
	}
}
