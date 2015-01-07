package universalcoins.proxy;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import universalcoins.UniversalCoins;
import universalcoins.gui.HintGuiRenderer;
import universalcoins.render.BlockVendorRenderer;
import universalcoins.render.CardStationRenderer;
import universalcoins.render.ItemCardStationRenderer;
import universalcoins.render.TileEntityVendorRenderer;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TileVendor;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendor.class, new TileEntityVendorRenderer());
		RenderingRegistry.registerBlockHandler(new BlockVendorRenderer(RenderingRegistry.getNextAvailableRenderId()));
		//register handler for GUI hints for vending blocks
		MinecraftForge.EVENT_BUS.register(HintGuiRenderer.instance);
				
		TileEntitySpecialRenderer render = new CardStationRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileCardStation.class, render);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(UniversalCoins.proxy.blockCardStation), new ItemCardStationRenderer(render, new TileCardStation()));
	}
}
