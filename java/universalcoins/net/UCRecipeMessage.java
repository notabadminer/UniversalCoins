package universalcoins.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.UniversalCoins;
import universalcoins.util.UCRecipeHelper;

public class UCRecipeMessage implements IMessage, IMessageHandler<UCRecipeMessage, IMessage> {
	private boolean recipesEnabled, vendorRecipesEnabled, vendorFrameRecipesEnabled, atmRecipeEnabled,
			enderCardRecipeEnabled, banditRecipeEnabled, signalRecipeEnabled, linkCardRecipeEnabled,
			packagerRecipeEnabled;

	public UCRecipeMessage() {
		this.recipesEnabled = UniversalCoins.recipesEnabled;
		this.vendorRecipesEnabled = UniversalCoins.vendorRecipesEnabled;
		this.vendorFrameRecipesEnabled = UniversalCoins.vendorFrameRecipesEnabled;
		this.atmRecipeEnabled = UniversalCoins.atmRecipeEnabled;
		this.enderCardRecipeEnabled = UniversalCoins.enderCardRecipeEnabled;
		this.banditRecipeEnabled = UniversalCoins.banditRecipeEnabled;
		this.signalRecipeEnabled = UniversalCoins.signalRecipeEnabled;
		this.linkCardRecipeEnabled = UniversalCoins.linkCardRecipeEnabled;
		this.packagerRecipeEnabled = UniversalCoins.packagerRecipeEnabled;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.recipesEnabled = buf.readBoolean();
		this.vendorRecipesEnabled = buf.readBoolean();
		this.vendorFrameRecipesEnabled = buf.readBoolean();
		this.atmRecipeEnabled = buf.readBoolean();
		this.enderCardRecipeEnabled = buf.readBoolean();
		this.banditRecipeEnabled = buf.readBoolean();
		this.signalRecipeEnabled = buf.readBoolean();
		this.linkCardRecipeEnabled = buf.readBoolean();
		this.packagerRecipeEnabled = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(recipesEnabled);
		buf.writeBoolean(vendorRecipesEnabled);
		buf.writeBoolean(vendorFrameRecipesEnabled);
		buf.writeBoolean(atmRecipeEnabled);
		buf.writeBoolean(enderCardRecipeEnabled);
		buf.writeBoolean(banditRecipeEnabled);
		buf.writeBoolean(signalRecipeEnabled);
		buf.writeBoolean(linkCardRecipeEnabled);
		buf.writeBoolean(packagerRecipeEnabled);
	}

	@Override
	public IMessage onMessage(final UCRecipeMessage message, final MessageContext ctx) {
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
		
	private void processMessage(UCRecipeMessage message, final MessageContext ctx) {
		UCRecipeHelper.addCoinRecipes();
		if (message.recipesEnabled) {
			UCRecipeHelper.addTradeStationRecipe();
		}
		if (message.vendorRecipesEnabled) {
			UCRecipeHelper.addVendingBlockRecipes();
		}
		if (message.vendorFrameRecipesEnabled) {
			UCRecipeHelper.addVendingFrameRecipes();
		}
		if (message.atmRecipeEnabled) {
			UCRecipeHelper.addCardStationRecipes();
		}
		if (message.enderCardRecipeEnabled) {
			UCRecipeHelper.addEnderCardRecipes();
			UCRecipeHelper.addBlockSafeRecipe();
		}
		if (message.banditRecipeEnabled) {
			UCRecipeHelper.addBanditRecipes();
		}
		if (message.signalRecipeEnabled) {
			UCRecipeHelper.addSignalRecipes();
		}
		if (message.linkCardRecipeEnabled) {
			UCRecipeHelper.addLinkCardRecipes();
		}
		if (message.packagerRecipeEnabled) {
			UCRecipeHelper.addPackagerRecipes();
		}
		UCRecipeHelper.addSignRecipes();
		UCRecipeHelper.addPlankTextureRecipes();
	}
}