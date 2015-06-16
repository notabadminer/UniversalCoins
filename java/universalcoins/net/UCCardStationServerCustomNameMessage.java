package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import universalcoins.tile.TileCardStation;

public class UCCardStationServerCustomNameMessage  implements IMessage, IMessageHandler<UCCardStationServerCustomNameMessage, IMessage> {
	private int x, y, z;
	private String groupName;

    public UCCardStationServerCustomNameMessage() {}

    public UCCardStationServerCustomNameMessage(int x, int y, int z, String stringGroupName) { 
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
        ByteBufUtils.writeUTF8String(buf, groupName);    }

    @Override
    public void fromBytes(ByteBuf buf) { 
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.groupName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public IMessage onMessage(UCCardStationServerCustomNameMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TileCardStation) {
			((TileCardStation) tileEntity).customAccountName = message.groupName;
			}
			return null;
	}
}
