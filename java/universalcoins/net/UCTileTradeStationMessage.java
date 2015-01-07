package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tile.TileTradeStation;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class UCTileTradeStationMessage implements IMessage, IMessageHandler<UCTileTradeStationMessage, IMessage> {
public int x, y, z, coinSum, itemPrice;
public String customName;
private boolean buyButtonActive, sellButtonActive, coinButtonActive, 
isSStackButtonActive, isLStackButtonActive, isSBagButtonActive, isLBagButtonActive;;

    public UCTileTradeStationMessage()
    {
    }

    public UCTileTradeStationMessage(TileTradeStation tileEntity)
    {
        this.x = tileEntity.xCoord;
        this.y = tileEntity.yCoord;
        this.z = tileEntity.zCoord;
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
    }

	@Override
	public IMessage onMessage(UCTileTradeStationMessage message, MessageContext ctx) {
		TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
				.getTileEntity(message.x, message.y, message.z);

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
		}
		return null;
	}
}