package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import universalcoins.tile.TileBandit;

public class UCBanditServerMessage  implements IMessage, IMessageHandler<UCBanditServerMessage, IMessage> {
	private int x, y, z, spinFee, fourMatch, fiveMatch;

    public UCBanditServerMessage() {}

    public UCBanditServerMessage(int x, int y, int z, int spinFee, int fourMatch, int fiveMatch) { 
    	this.x = x;
    	this.y = y;
    	this.z = z;
        this.spinFee = spinFee;
        this.fourMatch = fourMatch;
        this.fiveMatch = fiveMatch;
    }

    @Override
    public void toBytes(ByteBuf buf) { 
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(spinFee);
        buf.writeInt(fourMatch);
        buf.writeInt(fiveMatch);
    }

    @Override
    public void fromBytes(ByteBuf buf) { 
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.spinFee = buf.readInt();
        this.fourMatch = buf.readInt();
        this.fiveMatch = buf.readInt();
	}

	@Override
	public IMessage onMessage(UCBanditServerMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TileBandit) {
			((TileBandit) tileEntity).spinFee = message.spinFee;
			((TileBandit) tileEntity).fourMatchPayout = message.fourMatch;
			((TileBandit) tileEntity).fiveMatchPayout = message.fiveMatch;
			((TileBandit) tileEntity).inUseCleanup();
			((TileBandit) tileEntity).markDirty();
			}
			return null;
	}
}
