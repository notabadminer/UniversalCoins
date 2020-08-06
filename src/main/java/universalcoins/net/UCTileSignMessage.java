package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.tileentity.TileUCSign;

public class UCTileSignMessage implements IMessage, IMessageHandler<UCTileSignMessage, IMessage> {
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private ITextComponent signText0, signText1, signText2, signText3;
	private String blockOwner;

	public UCTileSignMessage() {
	}

	public UCTileSignMessage(int x, int y, int z, ITextComponent[] signText, String blockOwner) {
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
		this.signText0 = signText[0];
		this.signText1 = signText[1];
		this.signText2 = signText[2];
		this.signText3 = signText[3];
		this.blockOwner = blockOwner;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.xCoord = buf.readInt();
		this.yCoord = buf.readShort();
		this.zCoord = buf.readInt();
		this.signText0 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
		this.signText1 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
		this.signText2 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
		this.signText3 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
		this.blockOwner = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.xCoord);
		buf.writeShort(this.yCoord);
		buf.writeInt(this.zCoord);
		ByteBufUtils.writeUTF8String(buf, signText0.getUnformattedText());
		ByteBufUtils.writeUTF8String(buf, signText1.getUnformattedText());
		ByteBufUtils.writeUTF8String(buf, signText2.getUnformattedText());
		ByteBufUtils.writeUTF8String(buf, signText3.getUnformattedText());
		ByteBufUtils.writeUTF8String(buf, this.blockOwner);
	}

	@Override
	public IMessage onMessage(final UCTileSignMessage message, final MessageContext ctx) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				processMessage(message, ctx);
			}
		};
		if (ctx.side == Side.CLIENT) {
			Minecraft.getMinecraft().addScheduledTask(task);
		} else if (ctx.side == Side.SERVER) {
			EntityPlayerMP playerEntity = ctx.getServerHandler().player;
			if (playerEntity == null) {
				FMLLog.log.warn("onMessage-server: Player is null");
				return null;
			}
			playerEntity.getServerWorld().addScheduledTask(task);
		}
		return null;
	}

	private void processMessage(UCTileSignMessage message, final MessageContext ctx) {
		TileEntity tileEntity = FMLClientHandler.instance().getClient().world
				.getTileEntity(new BlockPos(message.xCoord, message.yCoord, message.zCoord));

		if (tileEntity != null && tileEntity instanceof TileUCSign) {
			TileUCSign tentity = (TileUCSign) tileEntity;
			tentity.signText[0] = message.signText0;
			tentity.signText[1] = message.signText1;
			tentity.signText[2] = message.signText2;
			tentity.signText[3] = message.signText3;
			tentity.blockOwner = message.blockOwner;
		}
	}
}