package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendorFrame;

public class UCTextureMessage  implements IMessage, IMessageHandler<UCTextureMessage, IMessage> {
	private int x, y, z;
	private String blockIcon;

    public UCTextureMessage() {}

    public UCTextureMessage(int x, int y, int z, String blockIcon) { 
    	this.x = x;
    	this.y = y;
    	this.z = z;
        this.blockIcon = blockIcon;
    }

    @Override
    public void toBytes(ByteBuf buf) { 
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, blockIcon);    }

    @Override
    public void fromBytes(ByteBuf buf) { 
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.blockIcon = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public IMessage onMessage(final UCTextureMessage message, final MessageContext ctx) {
		Runnable task = new Runnable() {
            @Override
            public void run() {
                processMessage(message, ctx);
            }
        };
        if(ctx.side == Side.CLIENT) {
            Minecraft.getMinecraft().addScheduledTask(task);
        }
        else if(ctx.side == Side.SERVER) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
            if(playerEntity == null) {
                FMLLog.warning("onMessage-server: Player is null");
                return null;
            }
            playerEntity.getServerForPlayer().addScheduledTask(task);
        }
        return null;
 }
		
private void processMessage(UCTextureMessage message, final MessageContext ctx) {	
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TileVendorFrame) {
			((TileVendorFrame) tileEntity).blockIcon = message.blockIcon;
		}
		if (tileEntity instanceof TileUCSign) {
			((TileUCSign) tileEntity).blockIcon = message.blockIcon;
		}
	}
}
