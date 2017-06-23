package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tileentity.TileVendor;

public class VendorBlockRenderer extends TileEntitySpecialRenderer {

	RenderEntityItem renderer = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(),
			Minecraft.getMinecraft().getRenderItem());

	public VendorBlockRenderer() {
	}

	@Override
	public void renderTileEntityFast(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage,
			float partial, net.minecraft.client.renderer.BufferBuilder buffer) {
		TileVendor tVendor = (TileVendor) te;

		if (tVendor == null || tVendor.getBlockType() == null) {
			return;
		}

		ItemStack itemstack = tVendor.getSellItem();

		if (itemstack == null) {
			return;
		}
		EntityItem entity = new EntityItem(null, x, y, z, itemstack);

		entity.hoverStart = 0;

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y + 0.15F, (float) z + 0.5F);

		try {
			// render trade item
			renderer.doRender(entity, 0, 0, 0, 0, Minecraft.getMinecraft().player.ticksExisted);
		} catch (Throwable e) {
		}
		GlStateManager.popMatrix();
	}
}
