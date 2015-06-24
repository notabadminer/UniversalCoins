package universalcoins.net;

import io.netty.buffer.ByteBuf;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.UniversalCoins;

public class UCRecipeMessage implements IMessage, IMessageHandler<UCRecipeMessage, IMessage> {
private boolean tradeStationRecipesEnabled, vendorRecipesEnabled, vendorFrameRecipesEnabled, atmRecipeEnabled, 
enderCardRecipeEnabled, banditRecipeEnabled, signalRecipeEnabled, linkCardRecipeEnabled, packagerRecipeEnabled;

    public UCRecipeMessage()
    {
        this.tradeStationRecipesEnabled = UniversalCoins.tradeStationRecipesEnabled;
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
    public void fromBytes(ByteBuf buf)
    {
    	this.tradeStationRecipesEnabled = buf.readBoolean();
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
    public void toBytes(ByteBuf buf)
    {
        buf.writeBoolean(tradeStationRecipesEnabled);
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
		if (!message.tradeStationRecipesEnabled) {
			removeRecipe(new ItemStack(UniversalCoins.proxy.blockTradeStation));
			removeRecipe(new ItemStack(UniversalCoins.proxy.itemSeller));
		}
		if (!message.vendorRecipesEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.blockVendor));
		}
		if (!message.vendorFrameRecipesEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.blockVendorFrame));
		}
		if (!message.atmRecipeEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.blockCardStation));
		}
		if (!message.enderCardRecipeEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.itemEnderCard));
		}
		if (!message.banditRecipeEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.blockBandit));
		}
		if (!message.signalRecipeEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.blockSignal));
		}
		if (!message.linkCardRecipeEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.itemLinkCard));
		}
		if (!message.packagerRecipeEnabled){
			removeRecipe(new ItemStack(UniversalCoins.proxy.blockPackager));
		}
	}
	
	private void removeRecipe(ItemStack stack) {
		List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
		Iterator<IRecipe> recipeIterator = recipeList.iterator();
		
		while (recipeIterator.hasNext()) {
			ItemStack recipeStack = recipeIterator.next().getRecipeOutput();
			if (recipeStack != null) {
				if (recipeStack.areItemStacksEqual(recipeStack, stack)) {
					recipeIterator.remove();
				}
			}			
		}
	}
}