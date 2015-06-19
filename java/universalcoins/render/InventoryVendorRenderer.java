package universalcoins.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendorFrame;

public class InventoryVendorRenderer extends TileEntityItemStackRenderer {
	
	private TileVendorFrame teVendorFrame = new TileVendorFrame();

	@Override
	public void renderByItem(ItemStack itemStack) {
		Block block = Block.getBlockFromItem(itemStack.getItem());

		if (block == UniversalCoins.proxy.blockVendorFrame) {
			GlStateManager.rotate(45, 0, -1, 0);
			GlStateManager.scale(1.7F, 1.7F, 1.7F);
			GlStateManager.translate(-0.1F, -0.2F, 0.0F);
			TileEntityRendererDispatcher.instance.renderTileEntityAt(this.teVendorFrame, 0.0D, 0.0D, 0.0D, 0.0F);
		} else {
			super.renderByItem(itemStack);
		}
	}
}
