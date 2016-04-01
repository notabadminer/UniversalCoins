package universalcoins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import universalcoins.container.ContainerTradeStation;
import universalcoins.gui.TradeStationGUI;
import universalcoins.tileentity.TileTradeStation;

class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof TileTradeStation) {
			return new ContainerTradeStation(player.inventory, (TileTradeStation) tileEntity);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof TileTradeStation) {
			return new TradeStationGUI(player.inventory, (TileTradeStation) tileEntity);
		}
		return null;
	}
}
