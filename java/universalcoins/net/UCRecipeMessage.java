package universalcoins.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import universalcoins.UniversalCoins;
import universalcoins.util.UCRecipeHelper;


public class UCRecipeMessage implements IMessage, IMessageHandler<UCRecipeMessage, IMessage> {
private boolean recipesEnabled, vendorRecipesEnabled, vendorFrameRecipesEnabled, atmRecipeEnabled, enderCardRecipeEnabled;;

    public UCRecipeMessage()
    {
        this.recipesEnabled = UniversalCoins.recipesEnabled;
        this.vendorRecipesEnabled = UniversalCoins.vendorRecipesEnabled;
        this.vendorFrameRecipesEnabled = UniversalCoins.vendorFrameRecipesEnabled;
        this.atmRecipeEnabled = UniversalCoins.atmRecipeEnabled;
        this.enderCardRecipeEnabled = UniversalCoins.enderCardRecipeEnabled;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
    	this.recipesEnabled = buf.readBoolean();
        this.vendorRecipesEnabled = buf.readBoolean();
        this.vendorFrameRecipesEnabled = buf.readBoolean();
        this.atmRecipeEnabled = buf.readBoolean();
        this.enderCardRecipeEnabled = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeBoolean(recipesEnabled);
        buf.writeBoolean(vendorRecipesEnabled);
        buf.writeBoolean(vendorFrameRecipesEnabled);
        buf.writeBoolean(atmRecipeEnabled);
        buf.writeBoolean(enderCardRecipeEnabled);
    }

	@Override
	public IMessage onMessage(UCRecipeMessage message, MessageContext ctx) {
		UCRecipeHelper.addCoinRecipes();
		if (message.recipesEnabled) {
			UCRecipeHelper.addTradeStationRecipe();
		}
		if (message.vendorRecipesEnabled){
			UCRecipeHelper.addVendingBlockRecipes();
		}
		if (message.vendorFrameRecipesEnabled){
			UCRecipeHelper.addVendingFrameRecipes();
		}
		if (message.atmRecipeEnabled){
			UCRecipeHelper.addCardStationRecipes();
		}
		if (message.enderCardRecipeEnabled){
			UCRecipeHelper.addEnderCardRecipes();
			UCRecipeHelper.addBlockSafeRecipe();
		}
			
		
		return null;
	}
}