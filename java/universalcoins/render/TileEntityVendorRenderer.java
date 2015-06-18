package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorFrame;

//author AUTOMATIC_MAIDEN

public class TileEntityVendorRenderer extends TileEntitySpecialRenderer {
	RenderItem renderer = Minecraft.getMinecraft().getRenderItem();

	public TileEntityVendorRenderer() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double posX,
			double posY, double posZ, float f, int p_180535_9_) {
		TileVendor machine = (TileVendor) tileentity;

		if (machine == null || machine.getBlockType() == null) {
			return;
		}

		ItemStack itemstack = machine.getSellItem();

		if (itemstack == null) {
			return;
		}

		EntityItem entity = new EntityItem(null, posX, posY, posZ, itemstack);
		entity.hoverStart = 0;

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
			entity.ticksExisted = Minecraft.getMinecraft().thePlayer.ticksExisted;
		}

		int i = (int) posX;
		int j = (int) posY;
		int k = (int) posZ;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) posX + 0.5F, (float) posY + 0.35F, (float) posZ + 0.5F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		// render trade item or block
		if (itemstack != null) {
			ItemStack visStack = itemstack.copy();
			visStack.stackSize = 1;
			EntityItem entityitem = new EntityItem(null, 0.0D, 0.0D, 0.0D, visStack);
			entityitem.hoverStart = 0.0F;
			renderer.renderItemModel(entityitem.getEntityItem());
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}
}
