package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tileentity.TileVendorFrame;

public class VendorFrameRenderer extends TileEntitySpecialRenderer {

	public VendorFrameRenderer() {
	}

	@Override
	public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		// get trade item or block
		ItemStack itemstack = ((TileVendorFrame) te).getSellItem();

		// exit if nothing is set
		if (itemstack.isEmpty()) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();

		// adjust block rotation based on block meta
		int meta = 0;
		try {
			meta = te.getBlockMetadata();
		} catch (Throwable ex2) {
			// do nothing
		}
		float correction = 0.0F;
		if (meta == 2) {
			correction = 0.0F;
		}
		if (meta == 3) {
			correction = 180.0F;
		}
		if (meta == 4) {
			correction = 90.0F;
		}
		if (meta == 5) {
			correction = -90.0F;
		}
		GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GlStateManager.rotate(correction, 0F, 1F, 0F);
		GlStateManager.translate(-0.0F, -0.0F, -0.45F);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		GlStateManager.pushAttrib();
		RenderHelper.enableStandardItemLighting();
		Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popAttrib();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}
