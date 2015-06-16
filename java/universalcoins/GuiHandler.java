package universalcoins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import universalcoins.container.ContainerBandit;
import universalcoins.container.ContainerCardStation;
import universalcoins.container.ContainerPackager;
import universalcoins.container.ContainerSafe;
import universalcoins.container.ContainerSignal;
import universalcoins.container.ContainerTradeStation;
import universalcoins.gui.BanditConfigGUI;
import universalcoins.gui.BanditGUI;
import universalcoins.gui.CardStationGUI;
import universalcoins.gui.PackagerGUI;
import universalcoins.gui.SafeGUI;
import universalcoins.gui.SignalGUI;
import universalcoins.gui.TradeStationGUI;
import universalcoins.tile.TileBandit;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TilePackager;
import universalcoins.tile.TileSafe;
import universalcoins.tile.TileSignal;
import universalcoins.tile.TileTradeStation;

class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if(tileEntity instanceof TileTradeStation){
                return new ContainerTradeStation(player.inventory, (TileTradeStation) tileEntity);
        }
        if (tileEntity instanceof TileCardStation) {
            return new ContainerCardStation(player.inventory, (TileCardStation) tileEntity);
        }
        if (tileEntity instanceof TileSafe) {
            return new ContainerSafe(player.inventory, (TileSafe) tileEntity);
        }
        if (tileEntity instanceof TileBandit) {
        	if(player.getHeldItem() != null && player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
        		return null;
        	} else {
        		return new ContainerBandit(player.inventory, (TileBandit) tileEntity);
        	}
        }
        if (tileEntity instanceof TileSignal) {
            return new ContainerSignal(player.inventory, (TileSignal) tileEntity);
        }
        if (tileEntity instanceof TilePackager) {
            return new ContainerPackager(player.inventory, (TilePackager) tileEntity);
        }
        return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if(tileEntity instanceof TileTradeStation){
                return new TradeStationGUI(player.inventory, (TileTradeStation) tileEntity);
        }
        if (tileEntity instanceof TileCardStation) {
            return new CardStationGUI(player.inventory, (TileCardStation) tileEntity);
        }
        if (tileEntity instanceof TileSafe) {
            return new SafeGUI(player.inventory, (TileSafe) tileEntity);
        }
        if (tileEntity instanceof TileBandit) {
        	if (player.getHeldItem() != null && player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
        		return new BanditConfigGUI((TileBandit) tileEntity);
        	} else {
                return new BanditGUI(player.inventory, (TileBandit) tileEntity);
        	}
        }
        if (tileEntity instanceof TileSignal) {
            return new SignalGUI(player.inventory, (TileSignal) tileEntity);
        }
        if (tileEntity instanceof TilePackager) {
            return new PackagerGUI(player.inventory, (TilePackager) tileEntity);
        }
        return null;
		}	
}
