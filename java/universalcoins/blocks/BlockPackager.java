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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TilePackager;

public class BlockPackager extends BlockContainer {
		
	public BlockPackager() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TilePackager) {
			 if (((TilePackager)tileEntity).inUse) {
				 if (!world.isRemote) { player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse"))); }
				 return true;
			 } else {
				 player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				 ((TilePackager) tileEntity).playerName = player.getName();
				 ((TilePackager) tileEntity).inUse = true;
				 return true;
			 }
		}
		return false;
	}
		
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote) return;
		int l = MathHelper.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

		switch (l) {
		case 0:
			//world.setBlockMetadataWithNotify(x, y, z, 2, l);
			break;
		case 1:
			//world.setBlockMetadataWithNotify(x, y, z, 5, l);
			break;
		case 2:
			//world.setBlockMetadataWithNotify(x, y, z, 3, l);
			break;
		case 3:
			//world.setBlockMetadataWithNotify(x, y, z, 4, l);
			break;
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TilePackager();
	}
	
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        world.setBlockToAir(pos);
        onBlockDestroyedByExplosion(world, pos, explosion);
        EntityItem entityItem = new EntityItem( world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this, 1));
		if (!world.isRemote) world.spawnEntityInWorld(entityItem);
    }
}