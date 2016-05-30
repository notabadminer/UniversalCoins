package universalcoins.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileProtected;
import universalcoins.tileentity.TileTradeStation;

public class BlockTradeStation extends BlockProtected {

	public BlockTradeStation() {
		super(Material.IRON);
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000.0F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileTradeStation) {
			if (((TileTradeStation) tileEntity).inUse) {
				if (!world.isRemote) {
					player.addChatMessage(new TextComponentString(I18n.translateToLocal("chat.warning.inuse")));
				}
				return true;
			} else {
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				((TileTradeStation) tileEntity).playerName = player.getName();
				((TileTradeStation) tileEntity).inUse = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTradeStation();
	}
}
