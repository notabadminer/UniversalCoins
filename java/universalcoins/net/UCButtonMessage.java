package universalcoins.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalcoins.tile.TileATM;
import universalcoins.tile.TilePackager;
import universalcoins.tile.TilePowerReceiver;
import universalcoins.tile.TilePowerTransmitter;
import universalcoins.tile.TileUCSignal;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileVendor;

public class UCButtonMessage implements IMessage, IMessageHandler<UCButtonMessage, IMessage> {
	private int x, y, z, buttonId;
	private boolean shiftPressed;

	public UCButtonMessage() {
	}

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
			((TileTradeStation) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
		if (tileEntity instanceof TileVendor) {
			((TileVendor) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
		if (tileEntity instanceof TileATM) {
			((TileATM) tileEntity).onButtonPressed(message.buttonId);
		}
		if (tileEntity instanceof TileUCSignal) {
			((TileUCSignal) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
		if (tileEntity instanceof TilePackager) {
			((TilePackager) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
		if (tileEntity instanceof TilePowerTransmitter) {
			((TilePowerTransmitter) tileEntity).onButtonPressed(message.buttonId);
		}
		if (tileEntity instanceof TilePowerReceiver) {
			((TilePowerReceiver) tileEntity).onButtonPressed(message.buttonId);
		}
		return null;
	}
}
