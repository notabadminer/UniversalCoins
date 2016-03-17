package universalcoins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import universalcoins.UniversalCoins;
import universalcoins.render.SignalRenderer;
import universalcoins.render.UCSignRenderer;
import universalcoins.render.VendorBlockRenderer;
import universalcoins.render.VendorFrameRenderer;
import universalcoins.tile.TileSignal;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendorBlock;
import universalcoins.tile.TileVendorFrame;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		// Items
		mesher.register(itemCoin, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemCoin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSmallCoinStack, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemSmallCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLargeCoinStack, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemLargeCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSmallCoinBag, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemSmallCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLargeCoinBag, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemLargeCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemEnderCard, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemEnderCard.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemPackage, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemPackage.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemSeller, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemSeller.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemUCCard, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemUCCard.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemVendorWrench, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemVendorWrench.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemUCSign, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemUCSign.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(itemLinkCard, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + itemLinkCard.getUnlocalizedName().substring(5), "inventory"));

		// Blocks
		mesher.register(Item.getItemFromBlock(blockBase), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockBase.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockCardStation), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockCardStation.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockPackager), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockPackager.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockSafe), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockSafe.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockSignal), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockSignal.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockTradeStation), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockTradeStation.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockVendor), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockVendor.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockVendorFrame), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockVendorFrame.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockPowerBase), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockPowerBase.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(blockPowerReceiver), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + blockPowerReceiver.getUnlocalizedName().substring(5), "inventory"));

		// this is to stop model definition errors for signs //TODO make this work
		//mesher.getModelManager().getBlockModelShapes().registerBuiltInBlocks(wall_ucsign);
		//mesher.getModelManager().getBlockModelShapes().registerBuiltInBlocks(standing_ucsign);

		ClientRegistry.bindTileEntitySpecialRenderer(TileUCSign.class, new UCSignRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileSignal.class, new SignalRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorBlock.class, new VendorBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorFrame.class, new VendorFrameRenderer());
	}
}
