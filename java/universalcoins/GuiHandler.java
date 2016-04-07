package universalcoins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import universalcoins.container.ContainerSafe;
import universalcoins.container.ContainerTradeStation;
import universalcoins.container.ContainerVendor;
import universalcoins.container.ContainerVendorBuy;
import universalcoins.container.ContainerVendorSell;
import universalcoins.gui.SafeGUI;
import universalcoins.gui.TradeStationGUI;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.tileentity.TileSafe;
import universalcoins.tileentity.TileTradeStation;
import universalcoins.tileentity.TileVendor;

class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof TileTradeStation) {
			return new ContainerTradeStation(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new ContainerSafe(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
//			if (player.getHeldItemMainhand() != null
//					&& player.getHeldItemMainhand().getItem() == UniversalCoins.proxy.itemVendorWrench) {
//				return new ContainerVendorWrench(player.inventory, (TileVendor) tileEntity);
//			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getName())) {
				return new ContainerVendor(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new ContainerVendorSell(player.inventory, (TileVendor) tileEntity);
			} else
				return new ContainerVendorBuy(player.inventory, (TileVendor) tileEntity);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof TileTradeStation) {
			return new TradeStationGUI(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new SafeGUI(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
//			if (player.getHeldItemMainhand() != null
//					&& player.getHeldItemMainhand().getItem() == UniversalCoins.proxy.itemVendorWrench) {
//				return new VendorWrenchGUI(player.inventory, (TileVendor) tileEntity);
//			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getName())) {
				return new VendorGUI(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new VendorSellGUI(player.inventory, (TileVendor) tileEntity);
			} else
				return new VendorBuyGUI(player.inventory, (TileVendor) tileEntity);
		}
		return null;
	}
}
