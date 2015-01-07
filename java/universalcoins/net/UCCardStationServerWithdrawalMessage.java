package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import universalcoins.tile.TileCardStation;

public class UCCardStationServerWithdrawalMessage  implements IMessage, IMessageHandler<UCCardStationServerWithdrawalMessage, IMessage> {
	private int x, y, z, withdrawalAmount;

    public UCCardStationServerWithdrawalMessage() {}

    public UCCardStationServerWithdrawalMessage(int x, int y, int z, int withdrawalAmount) { 
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
	public IMessage onMessage(UCCardStationServerWithdrawalMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;
		BlockPos pos = new BlockPos(message.x, message.y, message.z);

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileCardStation) {
			((TileCardStation) tileEntity).coinWithdrawalAmount = message.withdrawalAmount;
			}
			return null;
	}
}
