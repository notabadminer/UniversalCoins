package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import universalcoins.tile.TileVendor;

public class VendorBlockRenderer extends TileEntitySpecialRenderer {
	RenderItem renderer = Minecraft.getMinecraft().getRenderItem();

	public VendorBlockRenderer() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double posX,
			double posY, double posZ, float f, int p_180535_9_) {
		TileVendor tVendor = (TileVendor) tileentity;

		if (tVendor == null || tVendor.getBlockType() == null) {
			return;
		}

		ItemStack itemstack = tVendor.getSellItem();

		if (itemstack == null) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) posX + 0.5F, (float) posY + 0.35F, (float) posZ + 0.5F);
		GlStateManager.scale(0.9F, 0.9F, 0.9F);

		// render trade item or block
		if (itemstack != null) {
			renderer.renderItemModel(itemstack);
		}
		GlStateManager.popMatrix();
	}
}
