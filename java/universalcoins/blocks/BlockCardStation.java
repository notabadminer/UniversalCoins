package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileCardStation;

public class BlockCardStation extends BlockContainer {
	
	IIcon blockIcon;
	
	public BlockCardStation() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setBlockTextureName("universalcoins:blockTradeStation1"); //fixes missing texture on block break
		setResistance(6000000.0F);
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        return false;
    }
	
	@Override
	public boolean isOpaqueCube() {
	   return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (((TileCardStation) tileEntity).inUse) {
			if (!world.isRemote) { player.addChatMessage(new ChatComponentText(((TileCardStation) tileEntity).player + " " + 
					StatCollector.translateToLocal("chat.cardstation.warning.inuse"))); }
			return true;
		} else {
        	player.openGui(UniversalCoins.instance, 0, world, x, y, z);
        	((TileCardStation) tileEntity).player = player.getDisplayName();
        	return true;
        }
	}
		
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		//set block meta so we can use it later for rotation
		int rotation = MathHelper.floor_double((double)((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileCardStation();
	}
}
