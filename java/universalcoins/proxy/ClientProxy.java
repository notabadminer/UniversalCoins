package universalcoins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import universalcoins.UniversalCoins;
import universalcoins.render.ItemCardStationRenderer;
import universalcoins.render.ItemSignalRenderer;
import universalcoins.render.TileEntityCardStationRenderer;
import universalcoins.render.TileEntitySignalRenderer;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TileSignal;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		
		//Items
		mesher.register(itemCoin, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemCoin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSmallCoinStack, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemSmallCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLargeCoinStack, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemLargeCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSmallCoinBag, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemSmallCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLargeCoinBag, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemLargeCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemEnderCard, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemEnderCard.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemPackage, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemPackage.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSeller, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemSeller.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemUCCard, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemUCCard.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemVendorWrench, 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + itemVendorWrench.getUnlocalizedName().substring(5), "inventory"));
		
		//Blocks
		mesher.register(Item.getItemFromBlock(blockBandit), 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + blockBandit.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockBase), 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + blockBase.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockCardStation), 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + blockCardStation.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockPackager), 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + blockPackager.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockSafe), 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + blockSafe.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockSignal), 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + blockSignal.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockTradeStation), 0, new ModelResourceLocation(UniversalCoins.modid + 
				":" + blockTradeStation.getUnlocalizedName().substring(5), "inventory"));
		
		//ClientRegistry.bindTileEntitySpecialRenderer(TileVendor.class, new TileEntityVendorRenderer());
		//RenderingRegistry.registerBlockHandler(new BlockVendorRenderer(RenderingRegistry.getNextAvailableRenderId()));
		
		TileEntitySpecialRenderer render = new TileEntityCardStationRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileCardStation.class, render);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockCardStation), new ItemCardStationRenderer(render, new TileCardStation()));
        
        //TileEntitySpecialRenderer render2 = new VendorFrameRenderer();
		//ClientRegistry.bindTileEntitySpecialRenderer(TileVendorFrame.class, render2);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockVendorFrame), new ItemVendorFrameRenderer(render2, new TileVendorFrame()));
        
        //TileEntitySpecialRenderer render3 = new TileEntityUCSignRenderer();
		//ClientRegistry.bindTileEntitySpecialRenderer(TileUCSign.class, render3);
		
		TileEntitySpecialRenderer render4 = new TileEntitySignalRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileSignal.class, render4);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockSignal), new ItemSignalRenderer(render4, new TileSignal()));
	}
}
