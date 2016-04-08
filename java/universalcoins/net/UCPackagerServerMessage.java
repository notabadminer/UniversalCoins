package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import universalcoins.tileentity.TilePackager;

public class UCPackagerServerMessage implements IMessage, IMessageHandler<UCPackagerServerMessage, IMessage> {
	private int x, y, z;
	private String packageTarget;
	private boolean tabPressed;

	public UCPackagerServerMessage() {
	}

	public UCPackagerServerMessage(int x, int y, int z, String packageTarget, boolean tabPressed) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.packageTarget = packageTarget;
		this.tabPressed = tabPressed;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeUTF8String(buf, packageTarget);
		buf.writeBoolean(tabPressed);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.packageTarget = ByteBufUtils.readUTF8String(buf);
		this.tabPressed = buf.readBoolean();
	}

	@Override
	public IMessage onMessage(UCPackagerServerMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TilePackager) {
			((TilePackager) tileEntity).playerLookup(message.packageTarget, message.tabPressed);
		}
		return null;
	}
}
