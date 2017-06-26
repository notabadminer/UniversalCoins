package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tileentity.TileVendor;

public class VendorBlockRenderer extends TileEntitySpecialRenderer {

	public VendorBlockRenderer() {
	}

	@Override
	public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		TileVendor tVendor = (TileVendor) te;

		if (tVendor == null || tVendor.getBlockType() == null) {
			return;
		}

		ItemStack itemstack = tVendor.getSellItem();

		if (itemstack.isEmpty()) {
			return;
		}
		EntityItem entity = new EntityItem(null, x, y, z, itemstack);

		entity.hoverStart = 0;

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y + 0.15F, (float) z + 0.5F);

		try {
			// render trade item
			Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F,
					Minecraft.getMinecraft().player.ticksExisted, false);
		} catch (Throwable e) {
		}
		GlStateManager.popMatrix();
	}
}
