package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.tileentity.TileVendor;
import universalcoins.tileentity.TileVendorBlock;
import universalcoins.tileentity.TileVendorFrame;

public class UCVendorServerMessage implements IMessage, IMessageHandler<UCVendorServerMessage, IMessage> {
	private int x, y, z, itemPrice;
	private String blockOwner;
	private boolean infinite;

	public UCVendorServerMessage() {
	}

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
	public IMessage onMessage(final UCVendorServerMessage message, final MessageContext ctx) {
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
				return null;
			}
			playerEntity.getServerWorld().addScheduledTask(task);
		}
		return null;
	}

	private void processMessage(UCVendorServerMessage message, final MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
		if (tileEntity instanceof TileVendor) {
			TileVendor tEntity = (TileVendor) tileEntity;
			tEntity.itemPrice = message.itemPrice;
			tEntity.blockOwner = message.blockOwner;
			tEntity.infiniteMode = message.infinite;
		}
		if (tileEntity instanceof TileVendorFrame) {
			((TileVendorFrame) tileEntity).updateSigns();
		}
		if (tileEntity instanceof TileVendorBlock) {
			((TileVendorBlock) tileEntity).updateSigns();
		}
	}
}
