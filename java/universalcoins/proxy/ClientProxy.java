package universalcoins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import universalcoins.UniversalCoins;
import universalcoins.blocks.BlockBase;
import universalcoins.blocks.BlockCardStation;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.blocks.BlockVendor;
import universalcoins.gui.HintGuiRenderer;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemLargeCoinBag;
import universalcoins.items.ItemLargeCoinStack;
import universalcoins.items.ItemSeller;
import universalcoins.items.ItemSmallCoinBag;
import universalcoins.items.ItemSmallCoinStack;
import universalcoins.items.ItemUCCard;
import universalcoins.items.ItemVendorWrench;
import universalcoins.render.CardStationRenderer;
import universalcoins.render.ItemCardStationRenderer;
import universalcoins.tile.TileCardStation;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
	    
		//items
	    renderItem.getItemModelMesher().register(itemCoin, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemCoin) itemCoin).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemEnderCard, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemEnderCard) itemEnderCard).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemLargeCoinBag, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemLargeCoinBag) itemLargeCoinBag).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemLargeCoinStack, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemLargeCoinStack) itemLargeCoinStack).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemSeller, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemSeller) itemSeller).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemSmallCoinBag, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemSmallCoinBag) itemSmallCoinBag).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemSmallCoinStack, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemSmallCoinStack) itemSmallCoinStack).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemUCCard, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemUCCard) itemUCCard).getName(), "inventory"));
	    renderItem.getItemModelMesher().register(itemVendorWrench, 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((ItemVendorWrench) itemVendorWrench).getName(), "inventory"));

	     //blocks
	     renderItem.getItemModelMesher().register(Item.getItemFromBlock(blockBase), 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((BlockBase) blockBase).getName(), "inventory"));
	     renderItem.getItemModelMesher().register(Item.getItemFromBlock(blockCardStation), 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((BlockCardStation) blockCardStation).getName(), "inventory"));
	     renderItem.getItemModelMesher().register(Item.getItemFromBlock(blockTradeStation), 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((BlockTradeStation) blockTradeStation).getName(), "inventory"));
	     renderItem.getItemModelMesher().register(Item.getItemFromBlock(blockVendor), 0, new ModelResourceLocation(UniversalCoins.MODID + ":" + ((BlockVendor) blockVendor).getName(), "inventory"));
	    		
		//ClientRegistry.bindTileEntitySpecialRenderer(TileVendor.class, new TileEntityVendorRenderer());
		//RenderingRegistry.registerBlockHandler(new BlockVendorRenderer(RenderingRegistry.getNextAvailableRenderId()));
		
		//register handler for GUI hints for vending blocks
		MinecraftForge.EVENT_BUS.register(HintGuiRenderer.instance);
				
		TileEntitySpecialRenderer render = new CardStationRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileCardStation.class, render);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(UniversalCoins.proxy.blockCardStation), new ItemCardStationRenderer(render, new TileCardStation()));
	}
}
