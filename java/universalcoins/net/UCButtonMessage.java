package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.tileentity.TileATM;
import universalcoins.tileentity.TilePackager;
import universalcoins.tileentity.TileSignal;
import universalcoins.tileentity.TileTradeStation;
import universalcoins.tileentity.TileVendor;

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

	public IMessage onMessage(final UCButtonMessage message, final MessageContext ctx) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				processMessage(message, ctx);
			}
		};
		if (ctx.side == Side.CLIENT) {
			Minecraft.getMinecraft().addScheduledTask(task);
		} else if (ctx.side == Side.SERVER) {
			EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
			if (playerEntity == null) {
				FMLLog.warning("onMessage-server: Player is null");
				return null;
			}
			playerEntity.getServerForPlayer().addScheduledTask(task);
		}
		return null;
	}

	private void processMessage(UCButtonMessage message, final MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TileTradeStation) {
			((TileTradeStation) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
		if (tileEntity instanceof TileATM) {
			((TileATM) tileEntity).onButtonPressed(message.buttonId);
		}
		if (tileEntity instanceof TileSignal) {
			((TileSignal) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
		if (tileEntity instanceof TilePackager) {
			((TilePackager) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
		if (tileEntity instanceof TileVendor) {
			((TileVendor) tileEntity).onButtonPressed(message.buttonId, message.shiftPressed);
		}
	}
}
