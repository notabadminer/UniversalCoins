package universalcoins.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tile.TileVendor;

//author AUTOMATIC_MAIDEN

public class TileEntityVendorRenderer extends TileEntitySpecialRenderer {
	RenderItem renderer = new RenderItem();

	public TileEntityVendorRenderer() {
		renderer.setRenderManager(RenderManager.instance);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		TileVendor machine = (TileVendor) tileentity;

		if (machine == null || machine.getBlockType() == null) {
			return;
		}

		ItemStack itemstack = machine.getSellItem();

		if (itemstack == null) {
			return;
		}

		EntityItem entity = new EntityItem(null, x, y, z, itemstack);
		entity.hoverStart = 0;

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
			entity.age = Minecraft.getMinecraft().thePlayer.ticksExisted;
		}

		int i = (int) x;
		int j = (int) y;
		int k = (int) z;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.35F, (float) z + 0.5F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		try {
			// render trade item
			renderer.doRender(entity, 0, 0, 0, 0, f);
		} catch (Throwable e) {
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}
}
