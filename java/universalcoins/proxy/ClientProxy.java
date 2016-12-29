package universalcoins.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import universalcoins.UniversalCoins;
import universalcoins.render.BlockVendorRenderer;
import universalcoins.render.ItemCardStationRenderer;
import universalcoins.render.ItemSignalRenderer;
import universalcoins.render.ItemVendorFrameRenderer;
import universalcoins.render.TileEntityCardStationRenderer;
import universalcoins.render.TileEntitySignalRenderer;
import universalcoins.render.TileEntityUCSignRenderer;
import universalcoins.render.TileEntityVendorRenderer;
import universalcoins.render.VendorFrameRenderer;
import universalcoins.tile.TileATM;
import universalcoins.tile.TileUCSignal;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorFrame;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendor.class, new TileEntityVendorRenderer());
		RenderingRegistry.registerBlockHandler(new BlockVendorRenderer(RenderingRegistry.getNextAvailableRenderId()));

		TileEntitySpecialRenderer render = new TileEntityCardStationRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileATM.class, render);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(UniversalCoins.proxy.atm),
				new ItemCardStationRenderer(render, new TileATM()));

		TileEntitySpecialRenderer render2 = new VendorFrameRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorFrame.class, render2);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(UniversalCoins.proxy.vendor_frame),
				new ItemVendorFrameRenderer(render2, new TileVendorFrame()));

		TileEntitySpecialRenderer render3 = new TileEntityUCSignRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileUCSign.class, render3);

		TileEntitySpecialRenderer render4 = new TileEntitySignalRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileUCSignal.class, render4);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(UniversalCoins.proxy.signal_block),
				new ItemSignalRenderer(render4, new TileUCSignal()));
	}
}
