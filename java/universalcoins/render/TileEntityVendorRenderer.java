package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import universalcoins.tile.TileVendor;

//author AUTOMATIC_MAIDEN

public class TileEntityVendorRenderer extends TileEntitySpecialRenderer {
	RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
	RenderItem renderer = Minecraft.getMinecraft().getRenderItem();

	public TileEntityVendorRenderer() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double posX,
			double posZ, double p_180535_6_, float f, int p_180535_9_) {
		TileVendor machine = (TileVendor) tileentity;

		if (machine == null || machine.getBlockType() == null) {
			return;
		}

		ItemStack itemstack = machine.getSellItem();

		if (itemstack == null) {
			return;
		}

		//EntityItem entity = new EntityItem(null, posX, y, posZ, itemstack);
		//entity.hoverStart = 0;

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
			//entity.ticksExisted = Minecraft.getMinecraft().thePlayer.ticksExisted;
		}

		int i = (int) posX;
		//int j = (int) y;
		int k = (int) posZ;
		//int meta = worldObj.getBlockMetadata(i, j, k);
		GL11.glPushMatrix();
		//GL11.glTranslatef((float) posX + 0.5F, (float) y + 0.35F, (float) posZ + 0.5F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		try {
			//renderer.doRender(entity, 0, 0, 0, 0, f);
		} catch (Throwable e) {
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}
}
