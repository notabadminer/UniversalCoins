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

public class ATMWithdrawalMessage implements IMessage, IMessageHandler<ATMWithdrawalMessage, IMessage> {
	private int x, y, z, withdrawalAmount;

	public ATMWithdrawalMessage() {
	}

	public ATMWithdrawalMessage(int x, int y, int z, int withdrawalAmount) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.withdrawalAmount = withdrawalAmount;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(withdrawalAmount);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.withdrawalAmount = buf.readInt();
	}

	@Override
	public IMessage onMessage(final ATMWithdrawalMessage message, final MessageContext ctx) {
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

	private void processMessage(ATMWithdrawalMessage message, final MessageContext ctx) {
		World world = ctx.getServerHandler().player.world;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TileATM) {
			((TileATM) tileEntity).startCoinWithdrawal(message.withdrawalAmount);
		}
	}
}
