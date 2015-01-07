package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalcoins.tile.TileVendor;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class UCVendorServerMessage  implements IMessage, IMessageHandler<UCVendorServerMessage, IMessage> {
	private int x, y, z, itemPrice;
	private String blockOwner;
	private boolean infinite;

    public UCVendorServerMessage() {}

    public UCVendorServerMessage(int x, int y, int z, int price, String blockOwner, boolean infinite) { 
    	this.x = x;
    	this.y = y;
    	this.z = z;
        this.itemPrice = price;
        this.blockOwner = blockOwner;
        this.infinite = infinite;
    }

    @Override
    public void toBytes(ByteBuf buf) { 
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(itemPrice);
        ByteBufUtils.writeUTF8String(buf, blockOwner);
        buf.writeBoolean(infinite);
    }

    @Override
    public void fromBytes(ByteBuf buf) { 
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.itemPrice = buf.readInt();
        this.blockOwner = ByteBufUtils.readUTF8String(buf);
        this.infinite = buf.readBoolean();
	}

	@Override
	public IMessage onMessage(UCVendorServerMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
		if (tileEntity instanceof TileVendor) {
			((TileVendor) tileEntity).itemPrice = message.itemPrice;
			((TileVendor) tileEntity).blockOwner = message.blockOwner;
			((TileVendor) tileEntity).infiniteSell = message.infinite;
			}
			return null;
	}
}
