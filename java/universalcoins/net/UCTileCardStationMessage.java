package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tile.TileCardStation;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class UCTileCardStationMessage implements IMessage, IMessageHandler<UCTileCardStationMessage, IMessage> {
private int x, y, z, coinWithdrawalAmount, accountBalance;
private boolean inUse, depositCoins, withdrawCoins;
private String player, accountNumber, cardOwner, groupAccountName, groupAccountNumber;

    public UCTileCardStationMessage() { }

    public UCTileCardStationMessage(TileCardStation tileEntity) {
        this.x = tileEntity.xCoord;
        this.y = tileEntity.yCoord;
        this.z = tileEntity.zCoord;
        this.coinWithdrawalAmount = tileEntity.coinWithdrawalAmount;
        this.accountBalance = tileEntity.accountBalance;
        this.inUse = tileEntity.inUse;
        this.depositCoins = tileEntity.depositCoins;
        this.withdrawCoins = tileEntity.withdrawCoins;
        this.player = tileEntity.player;
        this.accountNumber = tileEntity.accountNumber;
        this.cardOwner = tileEntity.cardOwner;
        this.groupAccountName = tileEntity.customAccountName;
        this.groupAccountNumber = tileEntity.customAccountNumber;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.coinWithdrawalAmount = buf.readInt();
        this.accountBalance = buf.readInt();
        this.inUse = buf.readBoolean();
        this.depositCoins = buf.readBoolean();
        this.withdrawCoins = buf.readBoolean();
        this.player = ByteBufUtils.readUTF8String(buf);
        this.accountNumber = ByteBufUtils.readUTF8String(buf);
        this.cardOwner = ByteBufUtils.readUTF8String(buf);
        this.groupAccountName = ByteBufUtils.readUTF8String(buf);
        this.groupAccountNumber = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(coinWithdrawalAmount);
        buf.writeInt(accountBalance);
        buf.writeBoolean(inUse);
        buf.writeBoolean(depositCoins);
        buf.writeBoolean(withdrawCoins);
        ByteBufUtils.writeUTF8String(buf, player);
        ByteBufUtils.writeUTF8String(buf, accountNumber);
        ByteBufUtils.writeUTF8String(buf, cardOwner);
        ByteBufUtils.writeUTF8String(buf, groupAccountName);
        ByteBufUtils.writeUTF8String(buf, groupAccountNumber);
    }

	@Override
	public IMessage onMessage(UCTileCardStationMessage message, MessageContext ctx) {
		TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
				.getTileEntity(message.x, message.y, message.z);

		if (tileEntity instanceof TileCardStation) {
			//FMLLog.info("UC: received TE packet");
			((TileCardStation) tileEntity).coinWithdrawalAmount = message.coinWithdrawalAmount;
			((TileCardStation) tileEntity).accountBalance = message.accountBalance;
			((TileCardStation) tileEntity).inUse = message.inUse;
			((TileCardStation) tileEntity).depositCoins = message.depositCoins;
			((TileCardStation) tileEntity).withdrawCoins = message.withdrawCoins;
			((TileCardStation) tileEntity).player = message.player;
			((TileCardStation) tileEntity).accountNumber = message.accountNumber;
			((TileCardStation) tileEntity).cardOwner = message.cardOwner;
			((TileCardStation) tileEntity).customAccountName = message.groupAccountName;
			((TileCardStation) tileEntity).customAccountNumber = message.groupAccountNumber;
		}
		return null;
	}
}