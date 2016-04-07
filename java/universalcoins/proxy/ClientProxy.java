package universalcoins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import universalcoins.UniversalCoins;
import universalcoins.render.SignalRenderer;
import universalcoins.render.VendorBlockRenderer;
import universalcoins.render.VendorFrameRenderer;
import universalcoins.tileentity.TileSignal;
import universalcoins.tileentity.TileVendorBlock;
import universalcoins.tileentity.TileVendorFrame;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		// Items
		mesher.register(iron_coin, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + iron_coin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(gold_coin, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + gold_coin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(emerald_coin, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + emerald_coin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(diamond_coin, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + diamond_coin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(obsidian_coin, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + obsidian_coin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(uc_card, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + uc_card.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(ender_card, 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + ender_card.getUnlocalizedName().substring(5), "inventory"));

		// Blocks
		mesher.register(Item.getItemFromBlock(tradestation), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + tradestation.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(safe), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + safe.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(signalblock), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + signalblock.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(vendor), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + vendor.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(Item.getItemFromBlock(vendor_frame), 0, new ModelResourceLocation(
				UniversalCoins.MODID + ":" + vendor_frame.getUnlocalizedName().substring(5), "inventory"));
		
		//special
		ClientRegistry.bindTileEntitySpecialRenderer(TileSignal.class, new SignalRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorBlock.class, new VendorBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorFrame.class, new VendorFrameRenderer());

	}
}
