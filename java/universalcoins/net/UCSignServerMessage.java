package universalcoins.net;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalcoins.tile.TileUCSign;

public class UCSignServerMessage implements IMessage, IMessageHandler<UCSignServerMessage, IMessage> {
	private int x, y, z;
	private String signText0, signText1, signText2, signText3;

	public UCSignServerMessage() {
	}

	public UCSignServerMessage(int x, int y, int z, String[] signText) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.signText0 = signText[0];
		this.signText1 = signText[1];
		this.signText2 = signText[2];
		this.signText3 = signText[3];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeUTF8String(buf, signText0);
		ByteBufUtils.writeUTF8String(buf, signText1);
		ByteBufUtils.writeUTF8String(buf, signText2);
		ByteBufUtils.writeUTF8String(buf, signText3);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.signText0 = ByteBufUtils.readUTF8String(buf);
		this.signText1 = ByteBufUtils.readUTF8String(buf);
		this.signText2 = ByteBufUtils.readUTF8String(buf);
		this.signText3 = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public IMessage onMessage(UCSignServerMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
		if (tileEntity != null && tileEntity instanceof TileUCSign) {
			TileUCSign tentity = (TileUCSign) tileEntity;
			tentity.signText[0] = message.signText0;
			tentity.signText[1] = message.signText1;
			tentity.signText[2] = message.signText2;
			tentity.signText[3] = message.signText3;
			tentity.updateSign();
			tentity.markDirty();
		}
		return null;
	}
}
