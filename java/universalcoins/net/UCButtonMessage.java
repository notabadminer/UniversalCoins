package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalcoins.gui.TradeStationGUI;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileVendor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class UCButtonMessage implements IMessage, IMessageHandler<UCButtonMessage, IMessage> {
	private int x, y, z, buttonId;
	private boolean shiftPressed;

    public UCButtonMessage() {}

    public UCButtonMessage(int x, int y, int z, int button, boolean shift) { 
    	this.x = x;
    	this.y = y;
    	this.z = z;
        this.buttonId = button;
        this.shiftPressed = shift;
    }

    @Override
    public void toBytes(ByteBuf buf) { 
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(buttonId);
        buf.writeBoolean(shiftPressed);
    }

    @Override
    public void fromBytes(ByteBuf buf) { 
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.buttonId = buf.readInt();
        this.shiftPressed = buf.readBoolean();
	}

	@Override
	public IMessage onMessage(UCButtonMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
		if (tileEntity instanceof TileTradeStation) {
			if (message.buttonId == TradeStationGUI.idBuyButton) {
				if (message.shiftPressed) {
					((TileTradeStation) tileEntity).onBuyMaxPressed();
				} else {
					((TileTradeStation) tileEntity).onBuyPressed();
				}
			} else if (message.buttonId == TradeStationGUI.idSellButton) {
				if (message.shiftPressed) {
					((TileTradeStation) tileEntity).onSellMaxPressed();
				} else {
					((TileTradeStation) tileEntity).onSellPressed();
				}
			} else if (message.buttonId == TradeStationGUI.idAutoModeButton) {
				((TileTradeStation) tileEntity).onAutoModeButtonPressed();
			} else if (message.buttonId == TradeStationGUI.idCoinModeButton) {
				((TileTradeStation) tileEntity).onCoinModeButtonPressed();
			} else if (message.buttonId <= TradeStationGUI.idLBagButton) {
				((TileTradeStation) tileEntity).onRetrieveButtonsPressed(
						message.buttonId, message.shiftPressed);
			}

			NBTTagCompound data = new NBTTagCompound();
			tileEntity.writeToNBT(data);
		}
		if (tileEntity instanceof TileVendor) {
			if (message.buttonId == VendorGUI.idModeButton) {
				((TileVendor) tileEntity).onModeButtonPressed();
			}
			if (message.buttonId < VendorGUI.idCoinButton) {
				//do nothing here
			} else if (message.buttonId <= VendorGUI.idLBagButton) {
				((TileVendor) tileEntity).onRetrieveButtonsPressed(
						message.buttonId, message.shiftPressed);
			} else if (message.buttonId == VendorBuyGUI.idSellButton) {
				if (message.shiftPressed) {
					((TileVendor) tileEntity).onSellMaxPressed();
				} else {
					((TileVendor) tileEntity).onSellPressed();
				} 
			} else if (message.buttonId == VendorSellGUI.idBuyButton) {
				if (message.shiftPressed) {
					((TileVendor) tileEntity).onBuyMaxPressed();
				} else {
					((TileVendor) tileEntity).onBuyPressed();
				}
			} else if (message.buttonId <= VendorSellGUI.idLBagButton) {
				((TileVendor) tileEntity).onRetrieveButtonsPressed(
						message.buttonId, message.shiftPressed);
			}
		}
		if (tileEntity instanceof TileCardStation) {
			((TileCardStation) tileEntity).onButtonPressed(message.buttonId);
		}
		return null;
	}
}
