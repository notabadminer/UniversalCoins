package universalcoins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import universalcoins.UniversalCoins;
import universalcoins.render.InventoryVendorRenderer;
import universalcoins.render.CardStationRenderer;
import universalcoins.render.SignalRenderer;
import universalcoins.render.UCSignRenderer;
import universalcoins.render.VendorBlockRenderer;
import universalcoins.render.VendorFrameRenderer;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TileSignal;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendorBlock;
import universalcoins.tile.TileVendorFrame;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		// Items
		mesher.register(itemCoin, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemCoin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSmallCoinStack, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemSmallCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLargeCoinStack, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemLargeCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSmallCoinBag, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemSmallCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLargeCoinBag, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemLargeCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemEnderCard, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemEnderCard.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemPackage, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemPackage.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSeller, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemSeller.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemUCCard, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemUCCard.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemVendorWrench, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemVendorWrench.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemUCSign, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemUCSign.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLinkCard, 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ itemLinkCard.getUnlocalizedName().substring(5), "inventory"));

		// Blocks
		mesher.register(Item.getItemFromBlock(blockBandit), 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ blockBandit.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockBase), 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ blockBase.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockCardStation), 0, new ModelResourceLocation(UniversalCoins.modid
				+ ":" + blockCardStation.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockPackager), 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ blockPackager.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockSafe), 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ blockSafe.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockSignal), 0, new ModelResourceLocation(UniversalCoins.modid + ":"
				+ blockSignal.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockTradeStation), 0, new ModelResourceLocation(UniversalCoins.modid
				+ ":" + blockTradeStation.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockVendor), 0, new ModelResourceLocation(UniversalCoins.modid
				+ ":" + blockVendor.getUnlocalizedName().substring(5), "inventory"));

		
		ClientRegistry.bindTileEntitySpecialRenderer(TileCardStation.class, new CardStationRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileUCSign.class, new UCSignRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileSignal.class, new SignalRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorFrame.class,new VendorFrameRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorBlock.class,new VendorBlockRenderer());
		
		//inventory render helpers for TESR
		TileEntityItemStackRenderer.instance = new InventoryVendorRenderer();
		mesher.getModelManager().getBlockModelShapes().registerBuiltInBlocks(blockVendorFrame);
		mesher.register(Item.getItemFromBlock(blockVendorFrame),0,new ModelResourceLocation(UniversalCoins.modid
				+ ":" + blockVendorFrame.getUnlocalizedName().substring(5),"inventory"));
		
		//this is to stop model definition errors for signs
		mesher.getModelManager().getBlockModelShapes().registerBuiltInBlocks(wall_ucsign);
		mesher.getModelManager().getBlockModelShapes().registerBuiltInBlocks(standing_ucsign);
	}
}
