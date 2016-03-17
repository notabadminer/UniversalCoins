package universalcoins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import universalcoins.container.ContainerCardStation;
import universalcoins.container.ContainerPackager;
import universalcoins.container.ContainerPowerBase;
import universalcoins.container.ContainerPowerReceiver;
import universalcoins.container.ContainerSafe;
import universalcoins.container.ContainerSignal;
import universalcoins.container.ContainerTradeStation;
import universalcoins.container.ContainerVendor;
import universalcoins.container.ContainerVendorBuy;
import universalcoins.container.ContainerVendorSell;
import universalcoins.container.ContainerVendorWrench;
import universalcoins.gui.CardStationGUI;
import universalcoins.gui.PackagerGUI;
import universalcoins.gui.PowerBaseGUI;
import universalcoins.gui.PowerReceiverGUI;
import universalcoins.gui.SafeGUI;
import universalcoins.gui.SignalGUI;
import universalcoins.gui.TradeStationGUI;
import universalcoins.gui.UCSignEditGUI;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.gui.VendorWrenchGUI;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TilePackager;
import universalcoins.tile.TilePowerBase;
import universalcoins.tile.TilePowerReceiver;
import universalcoins.tile.TileSafe;
import universalcoins.tile.TileSignal;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendor;

class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof TileTradeStation) {
			return new ContainerTradeStation(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileCardStation) {
			return new ContainerCardStation(player.inventory, (TileCardStation) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new ContainerSafe(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileSignal) {
			return new ContainerSignal(player.inventory, (TileSignal) tileEntity);
		}
		if (tileEntity instanceof TilePackager) {
			return new ContainerPackager(player.inventory, (TilePackager) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
			if (player.getHeldItem() != null
					&& player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
				return new ContainerVendorWrench(player.inventory, (TileVendor) tileEntity);
			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getName())) {
				return new ContainerVendor(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new ContainerVendorSell(player.inventory, (TileVendor) tileEntity);
			} else
				return new ContainerVendorBuy(player.inventory, (TileVendor) tileEntity);
		}
		if (tileEntity instanceof TilePowerBase) {
			return new ContainerPowerBase(player.inventory, (TilePowerBase) tileEntity);
		}
		if (tileEntity instanceof TilePowerReceiver) {
			return new ContainerPowerReceiver(player.inventory, (TilePowerReceiver) tileEntity);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof TileTradeStation) {
			return new TradeStationGUI(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileCardStation) {
			return new CardStationGUI(player.inventory, (TileCardStation) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new SafeGUI(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileSignal) {
			return new SignalGUI(player.inventory, (TileSignal) tileEntity);
		}
		if (tileEntity instanceof TilePackager) {
			return new PackagerGUI(player.inventory, (TilePackager) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
			if (player.getHeldItem() != null
					&& player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
				return new VendorWrenchGUI(player.inventory, (TileVendor) tileEntity);
			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getName())) {
				return new VendorGUI(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new VendorSellGUI(player.inventory, (TileVendor) tileEntity);
			} else
				return new VendorBuyGUI(player.inventory, (TileVendor) tileEntity);
		}
		if (tileEntity instanceof TileUCSign) {
			return new UCSignEditGUI((TileUCSign) tileEntity);
		}
		if (tileEntity instanceof TilePowerBase) {
			return new PowerBaseGUI(player.inventory, (TilePowerBase) tileEntity);
		}
		if (tileEntity instanceof TilePowerReceiver) {
			return new PowerReceiverGUI(player.inventory, (TilePowerReceiver) tileEntity);
		}
		return null;
	}
}
