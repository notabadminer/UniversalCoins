package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.tileentity.TileATM;

public class ATMCustomNameMessage implements IMessage, IMessageHandler<ATMCustomNameMessage, IMessage> {
	private int x, y, z;
	private String groupName;

	public ATMCustomNameMessage() {
	}

	public ATMCustomNameMessage(int x, int y, int z, String stringGroupName) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.groupName = stringGroupName;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeUTF8String(buf, groupName);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.groupName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public IMessage onMessage(final ATMCustomNameMessage message, final MessageContext ctx) {
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

	private void processMessage(ATMCustomNameMessage message, final MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TileATM) {
			((TileATM) tileEntity).customAccountName = message.groupName;
		}
	}
}
