package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tile.TileVendorFrame;

public class VendorFrameRenderer extends TileEntitySpecialRenderer {
	RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

	public VendorFrameRenderer() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double posX, double posY, double posZ, float p_180535_8_,
			int p_180535_9_) {
		GlStateManager.pushMatrix();

		// render trade item or block
		ItemStack itemstack = ((TileVendorFrame) tileentity).getSellItem();

		// adjust block rotation based on block meta
		int meta = 0;
		try {
			meta = tileentity.getBlockMetadata();
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
		GlStateManager.translate((float) posX + 0.5F, (float) posY + 0.5F, (float) posZ + 0.5F);
		GlStateManager.rotate(correction, 0F, 1F, 0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);

		if (itemstack != null) {
			ItemStack visStack = itemstack.copy();
			visStack.stackSize = 1;
			GlStateManager.translate(0.5F, 0.5F, 0.0635F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			EntityItem entityitem = new EntityItem(null, 0, 0, 0, visStack);
			entityitem.hoverStart = 0.0F;
			GlStateManager.pushAttrib();
			RenderHelper.enableStandardItemLighting();
			this.itemRenderer.renderItem(entityitem.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popAttrib();
		}
		GlStateManager.popMatrix();
	}
}
