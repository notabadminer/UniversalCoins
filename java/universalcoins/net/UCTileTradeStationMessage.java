package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import universalcoins.tile.TileTradeStation;

public class UCTileTradeStationMessage implements IMessage, IMessageHandler<UCTileTradeStationMessage, IMessage> {
public int x, y, z, coinSum, itemPrice;
public String customName;
private boolean buyButtonActive, sellButtonActive, coinButtonActive, inUse, 
isSStackButtonActive, isLStackButtonActive, isSBagButtonActive, isLBagButtonActive;;

    public UCTileTradeStationMessage()
    {
    }

    public UCTileTradeStationMessage(TileTradeStation tileEntity)
    {
    	this.x = tileEntity.getPos().getX();
        this.y = tileEntity.getPos().getY();
        this.z = tileEntity.getPos().getZ();
        this.coinSum = tileEntity.coinSum;
        this.itemPrice = tileEntity.itemPrice;
        this.customName = tileEntity.getInventoryName();
        this.buyButtonActive = tileEntity.buyButtonActive;
        this.sellButtonActive = tileEntity.sellButtonActive;
        this.coinButtonActive = tileEntity.coinButtonActive;
        this.isSStackButtonActive = tileEntity.isSStackButtonActive;
        this.isLStackButtonActive = tileEntity.isLStackButtonActive;
        this.isSBagButtonActive = tileEntity.isSBagButtonActive;
        this.isLBagButtonActive = tileEntity.isLBagButtonActive;
        this.inUse = tileEntity.inUse;

    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.coinSum = buf.readInt();
        this.itemPrice = buf.readInt();
        this.customName = ByteBufUtils.readUTF8String(buf);
        this.buyButtonActive = buf.readBoolean();
        this.sellButtonActive = buf.readBoolean();
        this.coinButtonActive = buf.readBoolean();
        this.isSStackButtonActive = buf.readBoolean();
        this.isLStackButtonActive = buf.readBoolean();
        this.isSBagButtonActive = buf.readBoolean();
        this.isLBagButtonActive = buf.readBoolean();
        this.inUse = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(coinSum);
        buf.writeInt(itemPrice);
        ByteBufUtils.writeUTF8String(buf, customName);
        buf.writeBoolean(buyButtonActive);
        buf.writeBoolean(sellButtonActive);
        buf.writeBoolean(coinButtonActive);
        buf.writeBoolean(isSStackButtonActive);
        buf.writeBoolean(isLStackButtonActive);
        buf.writeBoolean(isSBagButtonActive);
        buf.writeBoolean(isLBagButtonActive);
        buf.writeBoolean(inUse);
    }

	@Override
	public IMessage onMessage(UCTileTradeStationMessage message, MessageContext ctx) {
		BlockPos pos = new BlockPos(message.x, message.y, message.z);
		TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
				.getTileEntity(pos);

		if (tileEntity instanceof TileTradeStation) {
			//FMLLog.info("UC: received TE packet");
			((TileTradeStation) tileEntity).coinSum = message.coinSum;
			((TileTradeStation) tileEntity).itemPrice = message.itemPrice;
			((TileTradeStation) tileEntity).setInventoryName(message.customName);
			((TileTradeStation) tileEntity).buyButtonActive = message.buyButtonActive;
			((TileTradeStation) tileEntity).sellButtonActive = message.sellButtonActive;
			((TileTradeStation) tileEntity).coinButtonActive = message.coinButtonActive;
			((TileTradeStation) tileEntity).isSStackButtonActive = message.isSStackButtonActive;
			((TileTradeStation) tileEntity).isLStackButtonActive = message.isLStackButtonActive;
			((TileTradeStation) tileEntity).isSBagButtonActive = message.isSBagButtonActive;
			((TileTradeStation) tileEntity).isLBagButtonActive = message.isLBagButtonActive;
			((TileTradeStation) tileEntity).inUse = message.inUse;
		}
		return null;
	}
}