package universalcoins;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalcoins.gui.ATMGUI;
import universalcoins.gui.PackagerGUI;
import universalcoins.gui.PowerReceiverGUI;
import universalcoins.gui.PowerTransmitterGUI;
import universalcoins.gui.SafeGUI;
import universalcoins.gui.SignalGUI;
import universalcoins.gui.TradeStationGUI;
import universalcoins.gui.UCSignEditGUI;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.gui.VendorWrenchGUI;
import universalcoins.inventory.ContainerATM;
import universalcoins.inventory.ContainerPackager;
import universalcoins.inventory.ContainerPowerReceiver;
import universalcoins.inventory.ContainerPowerTransmitter;
import universalcoins.inventory.ContainerSafe;
import universalcoins.inventory.ContainerSignal;
import universalcoins.inventory.ContainerTradeStation;
import universalcoins.inventory.ContainerVendor;
import universalcoins.inventory.ContainerVendorBuy;
import universalcoins.inventory.ContainerVendorSell;
import universalcoins.inventory.ContainerVendorWrench;
import universalcoins.tile.TileATM;
import universalcoins.tile.TilePackager;
import universalcoins.tile.TilePowerReceiver;
import universalcoins.tile.TilePowerTransmitter;
import universalcoins.tile.TileSafe;
import universalcoins.tile.TileUCSignal;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendor;

class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileTradeStation) {
			return new ContainerTradeStation(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
			if (player.getHeldItem() != null && player.getHeldItem().getItem() == UniversalCoins.proxy.vendor_wrench) {
				return new ContainerVendorWrench(player.inventory, (TileVendor) tileEntity);
			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getDisplayName())) {
				return new ContainerVendor(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new ContainerVendorSell(player.inventory, (TileVendor) tileEntity);
			} else
				return new ContainerVendorBuy(player.inventory, (TileVendor) tileEntity);
		}
		if (tileEntity instanceof TileATM) {
			return new ContainerATM(player.inventory, (TileATM) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new ContainerSafe(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileUCSignal) {
			return new ContainerSignal(player.inventory, (TileUCSignal) tileEntity);
		}
		if (tileEntity instanceof TilePackager) {
			return new ContainerPackager(player.inventory, (TilePackager) tileEntity);
		}
		if (tileEntity instanceof TilePowerTransmitter) {
			return new ContainerPowerTransmitter(player.inventory, (TilePowerTransmitter) tileEntity);
		}
		if (tileEntity instanceof TilePowerReceiver) {
			return new ContainerPowerReceiver(player.inventory, (TilePowerReceiver) tileEntity);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileTradeStation) {
			return new TradeStationGUI(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
			if (player.getHeldItem() != null && player.getHeldItem().getItem() == UniversalCoins.proxy.vendor_wrench) {
				return new VendorWrenchGUI(player.inventory, (TileVendor) tileEntity);
			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getDisplayName())) {
				return new VendorGUI(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new VendorSellGUI(player.inventory, (TileVendor) tileEntity);
			} else
				return new VendorBuyGUI(player.inventory, (TileVendor) tileEntity);
		}
		if (tileEntity instanceof TileATM) {
			return new ATMGUI(player.inventory, (TileATM) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new SafeGUI(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileUCSignal) {
			return new SignalGUI(player.inventory, (TileUCSignal) tileEntity);
		}
		if (tileEntity instanceof TileUCSign) {
			return new UCSignEditGUI((TileUCSign) tileEntity);
		}
		if (tileEntity instanceof TilePackager) {
			return new PackagerGUI(player.inventory, (TilePackager) tileEntity);
		}
		if (tileEntity instanceof TilePowerTransmitter) {
			return new PowerTransmitterGUI(player.inventory, (TilePowerTransmitter) tileEntity);
		}
		if (tileEntity instanceof TilePowerReceiver) {
			return new PowerReceiverGUI(player.inventory, (TilePowerReceiver) tileEntity);
		}
		return null;
	}
}
