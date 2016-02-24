package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tile.TileVendor;

public class VendorBlockRenderer extends TileEntitySpecialRenderer {

	RenderEntityItem renderer = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(),
			Minecraft.getMinecraft().getRenderItem());

	public VendorBlockRenderer() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double posX, double posY, double posZ, float f,
			int p_180535_9_) {
		TileVendor tVendor = (TileVendor) tileentity;

		if (tVendor == null || tVendor.getBlockType() == null) {
			return;
		}

		ItemStack itemstack = tVendor.getSellItem();

		if (itemstack == null) {
			return;
		}
		EntityItem entity = new EntityItem(null, posX, posY, posZ, itemstack);

		entity.hoverStart = 0;

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) posX + 0.5F, (float) posY + 0.15F, (float) posZ + 0.5F);

		try {
			// render trade item
			renderer.doRender(entity, 0, 0, 0, 0, Minecraft.getMinecraft().thePlayer.ticksExisted);
		} catch (Throwable e) {
		}
		GlStateManager.popMatrix();
	}
}
