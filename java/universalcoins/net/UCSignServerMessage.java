package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.tileentity.TileUCSign;

public class UCSignServerMessage implements IMessage, IMessageHandler<UCSignServerMessage, IMessage> {
	private int x, y, z;
	private ITextComponent signText0, signText1, signText2, signText3;

	public UCSignServerMessage() {
	}

	public UCSignServerMessage(int x, int y, int z, ITextComponent[] signText) {
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
		ByteBufUtils.writeUTF8String(buf, signText0.getFormattedText());
		ByteBufUtils.writeUTF8String(buf, signText1.getFormattedText());
		ByteBufUtils.writeUTF8String(buf, signText2.getFormattedText());
		ByteBufUtils.writeUTF8String(buf, signText3.getFormattedText());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.signText0 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
		this.signText1 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
		this.signText2 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
		this.signText3 = new TextComponentString(ByteBufUtils.readUTF8String(buf));
	}

	@Override
	public IMessage onMessage(final UCSignServerMessage message, final MessageContext ctx) {
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
			playerEntity.getServerWorld().addScheduledTask(task);
		}
		return null;
	}

	private void processMessage(UCSignServerMessage message, final MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity != null && tileEntity instanceof TileUCSign) {
			TileUCSign tentity = (TileUCSign) tileEntity;
			tentity.signText[0] = message.signText0;
			tentity.signText[1] = message.signText1;
			tentity.signText[2] = message.signText2;
			tentity.signText[3] = message.signText3;
			tentity.updateSign();
			tentity.markDirty();
		}
	}
}
