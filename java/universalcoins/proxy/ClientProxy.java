package universalcoins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import universalcoins.render.SignalRenderer;
import universalcoins.render.UCSignRenderer;
import universalcoins.render.VendorBlockRenderer;
import universalcoins.render.VendorFrameRenderer;
import universalcoins.tileentity.TileSignal;
import universalcoins.tileentity.TileUCSign;
import universalcoins.tileentity.TileVendorBlock;
import universalcoins.tileentity.TileVendorFrame;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		// Items
		mesher.register(iron_coin, 0, new ModelResourceLocation(iron_coin.getRegistryName(), "inventory"));
		mesher.register(gold_coin, 0, new ModelResourceLocation(gold_coin.getRegistryName(), "inventory"));
		mesher.register(emerald_coin, 0, new ModelResourceLocation(emerald_coin.getRegistryName(), "inventory"));
		mesher.register(diamond_coin, 0, new ModelResourceLocation(diamond_coin.getRegistryName(), "inventory"));
		mesher.register(obsidian_coin, 0, new ModelResourceLocation(obsidian_coin.getRegistryName(), "inventory"));
		mesher.register(uc_card, 0, new ModelResourceLocation(uc_card.getRegistryName(), "inventory"));
		mesher.register(ender_card, 0, new ModelResourceLocation(ender_card.getRegistryName(), "inventory"));
		mesher.register(uc_sign, 0, new ModelResourceLocation(uc_sign.getRegistryName(), "inventory"));
		mesher.register(vendor_wrench, 0, new ModelResourceLocation(vendor_wrench.getRegistryName(), "inventory"));
		mesher.register(catalog, 0, new ModelResourceLocation(catalog.getRegistryName(), "inventory"));
		mesher.register(link_card, 0, new ModelResourceLocation(link_card.getRegistryName(), "inventory"));
		mesher.register(uc_package, 0, new ModelResourceLocation(uc_package.getRegistryName(), "inventory"));

		// Blocks
		mesher.register(Item.getItemFromBlock(tradestation), 0,
				new ModelResourceLocation(tradestation.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(safe), 0, new ModelResourceLocation(safe.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(signalblock), 0,
				new ModelResourceLocation(signalblock.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(vendor), 0,
				new ModelResourceLocation(vendor.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(vendor_frame), 0,
				new ModelResourceLocation(vendor_frame.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(packager), 0,
				new ModelResourceLocation(packager.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(power_transmitter), 0,
				new ModelResourceLocation(power_transmitter.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(power_receiver), 0,
				new ModelResourceLocation(power_receiver.getRegistryName(), "inventory"));
		mesher.register(Item.getItemFromBlock(atm), 0, new ModelResourceLocation(atm.getRegistryName(), "inventory"));

		// special
		ClientRegistry.bindTileEntitySpecialRenderer(TileSignal.class, new SignalRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorBlock.class, new VendorBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorFrame.class, new VendorFrameRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileUCSign.class, new UCSignRenderer());
	}
}
