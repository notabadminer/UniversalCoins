package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import universalcoins.tile.TileCardStation;

public class UCTileCardStationMessage implements IMessage, IMessageHandler<UCTileCardStationMessage, IMessage> {
private int x, y, z, coinWithdrawalAmount, accountBalance;
private boolean inUse, depositCoins, withdrawCoins,accountError;
private String playerName, playerUID, accountNumber, cardOwner, groupAccountName, groupAccountNumber;

    public UCTileCardStationMessage() { }

    public UCTileCardStationMessage(TileCardStation tileEntity) {
        this.x = tileEntity.getPos().getX();
        this.y = tileEntity.getPos().getY();
        this.z = tileEntity.getPos().getZ();
        this.coinWithdrawalAmount = tileEntity.coinWithdrawalAmount;
        this.accountBalance = tileEntity.accountBalance;
        this.inUse = tileEntity.inUse;
        this.depositCoins = tileEntity.depositCoins;
        this.withdrawCoins = tileEntity.withdrawCoins;
        this.accountError = tileEntity.accountError;
        this.playerName = tileEntity.playerName;
        this.playerUID = tileEntity.playerUID;
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
        this.accountError = buf.readBoolean();
        this.playerName = ByteBufUtils.readUTF8String(buf);
        this.playerUID = ByteBufUtils.readUTF8String(buf);
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
        buf.writeBoolean(accountError);
        ByteBufUtils.writeUTF8String(buf, playerName);
        ByteBufUtils.writeUTF8String(buf, playerUID);
        ByteBufUtils.writeUTF8String(buf, accountNumber);
        ByteBufUtils.writeUTF8String(buf, cardOwner);
        ByteBufUtils.writeUTF8String(buf, groupAccountName);
        ByteBufUtils.writeUTF8String(buf, groupAccountNumber);
    }

	@Override
	public IMessage onMessage(UCTileCardStationMessage message, MessageContext ctx) {
		TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld
				.getTileEntity(new BlockPos(message.x, message.y, message.z));

		if (tileEntity instanceof TileCardStation) {
			//FMLLog.info("UC: received TE packet");
			((TileCardStation) tileEntity).coinWithdrawalAmount = message.coinWithdrawalAmount;
			((TileCardStation) tileEntity).accountBalance = message.accountBalance;
			((TileCardStation) tileEntity).inUse = message.inUse;
			((TileCardStation) tileEntity).depositCoins = message.depositCoins;
			((TileCardStation) tileEntity).withdrawCoins = message.withdrawCoins;
			((TileCardStation) tileEntity).accountError = message.accountError;
			((TileCardStation) tileEntity).playerName = message.playerName;
			((TileCardStation) tileEntity).playerUID = message.playerUID;
			((TileCardStation) tileEntity).accountNumber = message.accountNumber;
			((TileCardStation) tileEntity).cardOwner = message.cardOwner;
			((TileCardStation) tileEntity).customAccountName = message.groupAccountName;
			((TileCardStation) tileEntity).customAccountNumber = message.groupAccountNumber;
		}
		return null;
	}
}