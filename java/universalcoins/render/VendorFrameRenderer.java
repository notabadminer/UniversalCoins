package universalcoins.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import universalcoins.tile.TileVendorFrame;

public class VendorFrameRenderer extends TileEntitySpecialRenderer {

	RenderItem renderer = new RenderItem();
	public String blockIcon = "";

	public VendorFrameRenderer() {
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
		// default texture
		ResourceLocation blockTexture = (new ResourceLocation("textures/blocks/planks_birch.png"));
		// change texture based on plank type
		if (((TileVendorFrame) te).blockIcon != "") {
			blockIcon = (((TileVendorFrame) te).blockIcon);
		}

		if (blockIcon != null && blockIcon != "") {
			String[] tempIconName = blockIcon.split(":", 3); // split string
			if (tempIconName.length == 1) {
				// if minecraft, set resourcelocation using last part
				blockTexture = (new ResourceLocation("textures/blocks/" + tempIconName[0] + ".png"));
			} else {
				// if mod use mod path
				blockTexture = (new ResourceLocation(tempIconName[0] + ":textures/blocks/" + tempIconName[1] + ".png"));
			}
		}

		this.bindTexture(blockTexture);

		// adjust block rotation based on block meta
		int meta = te.blockMetadata;
		if (meta == -1) { // fix for inventory crash on get block meta
			try {
				meta = te.getBlockMetadata();
			} catch (Throwable ex2) {
				// do nothing
			}
		}

		// render block
		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glRotatef(meta * -90F, 0F, 1F, 0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);

		// back
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(0.875, 0.875, 0, 0.125, 0.125);
		tessellator.addVertexWithUV(0.875, 0.125, 0, 0.125, 0.875);
		tessellator.addVertexWithUV(0.125, 0.125, 0, 0.875, 0.875);
		tessellator.addVertexWithUV(0.125, 0.875, 0, 0.875, 0.125);

		// top
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(0.125, 0.875, 0, 0.125, 0);
		tessellator.addVertexWithUV(0.125, 0.875, 0.0625, 0.125, 0.0625);
		tessellator.addVertexWithUV(0.875, 0.875, 0.0625, 0.875, 0.0625);
		tessellator.addVertexWithUV(0.875, 0.875, 0, 0.875, 0);

		// bottom
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertexWithUV(0.125, 0.125, 0.0625, 0.875, 0.9375);
		tessellator.addVertexWithUV(0.125, 0.125, 0, 0.875, 0.875);
		tessellator.addVertexWithUV(0.875, 0.125, 0, 0.125, 0.875);
		tessellator.addVertexWithUV(0.875, 0.125, 0.0625, 0.125, 0.9375);

		// left
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(0.125, 0.875, 0.0625, 0, 0.125);
		tessellator.addVertexWithUV(0.125, 0.875, 0, 0.125, 0.125);
		tessellator.addVertexWithUV(0.125, 0.125, 0, 0, 0.875);
		tessellator.addVertexWithUV(0.125, 0.125, 0.0625, 0.125, 0.875);

		// right
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(0.875, 0.875, 0, 0.875, 0.125);
		tessellator.addVertexWithUV(0.875, 0.875, 0.0625, 0.75, 0.125);
		tessellator.addVertexWithUV(0.875, 0.125, 0.0625, 0.75, 0.875);
		tessellator.addVertexWithUV(0.875, 0.125, 0, 0.875, 0.875);

		// front center
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.1875, 0.8125, 0.03125, 0.8125, 0.1875);
		tessellator.addVertexWithUV(0.1875, 0.1875, 0.03125, 0.8125, 0.8125);
		tessellator.addVertexWithUV(0.8125, 0.1875, 0.03125, 0.1875, 0.8125);
		tessellator.addVertexWithUV(0.8125, 0.8125, 0.03125, 0.1875, 0.1875);

		// front left
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.125, 0.875, 0.0625, 0.1875, 0.125);
		tessellator.addVertexWithUV(0.125, 0.125, 0.0625, 0.1875, 0.875);
		tessellator.addVertexWithUV(0.1875, 0.125, 0.0625, 0.125, 0.875);
		tessellator.addVertexWithUV(0.1875, 0.875, 0.0625, 0.125, 0.125);

		// front right
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.8125, 0.875, 0.0625, 0.875, 0.125);
		tessellator.addVertexWithUV(0.8125, 0.125, 0.0625, 0.875, 0.875);
		tessellator.addVertexWithUV(0.875, 0.125, 0.0625, 0.8125, 0.875);
		tessellator.addVertexWithUV(0.875, 0.875, 0.0625, 0.8125, 0.125);

		// front top
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.1875, 0.875, 0.0625, 0.1875, 0.125);
		tessellator.addVertexWithUV(0.1875, 0.8125, 0.0625, 0.1875, 0.1875);
		tessellator.addVertexWithUV(0.8125, 0.8125, 0.0625, 0.8125, 0.1875);
		tessellator.addVertexWithUV(0.8125, 0.875, 0.0625, 0.8125, 0.125);

		// front bottom
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.1875, 0.1875, 0.0625, 0.8125, 0.8125);
		tessellator.addVertexWithUV(0.1875, 0.125, 0.0625, 0.8125, 0.875);
		tessellator.addVertexWithUV(0.8125, 0.125, 0.0625, 0.1875, 0.875);
		tessellator.addVertexWithUV(0.8125, 0.1875, 0.0625, 0.1875, 0.8125);

		// inside left
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(0.1875, 0.8125, 0.0625, 0.8125, 0.1875);
		tessellator.addVertexWithUV(0.1875, 0.1875, 0.0625, 0.8125, 0.8125);
		tessellator.addVertexWithUV(0.1875, 0.1875, 0.03125, 0.8125, 0.8125);
		tessellator.addVertexWithUV(0.1875, 0.8125, 0.03125, 0.8125, 0.1875);

		// inside right
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(0.8125, 0.1875, 0.0625, 0.8125, 0.1875);
		tessellator.addVertexWithUV(0.8125, 0.8125, 0.0625, 0.8125, 0.8125);
		tessellator.addVertexWithUV(0.8125, 0.8125, 0.03125, 0.8125, 0.8125);
		tessellator.addVertexWithUV(0.8125, 0.1875, 0.03125, 0.8125, 0.1875);

		// inside top
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertexWithUV(0.1875, 0.8125, 0.0625, 0.1875, 0.1875);
		tessellator.addVertexWithUV(0.1875, 0.8125, 0.03125, 0.1875, 0.21875);
		tessellator.addVertexWithUV(0.8125, 0.8125, 0.03125, 0.8125, 0.21875);
		tessellator.addVertexWithUV(0.8125, 0.8125, 0.0625, 0.8125, 0.1875);

		// inside bottom
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(0.1875, 0.1875, 0.03125, 0.8125, 0.78035);
		tessellator.addVertexWithUV(0.1875, 0.1875, 0.0625, 0.8125, 0.8125);
		tessellator.addVertexWithUV(0.8125, 0.1875, 0.0625, 0.1875, 0.8125);
		tessellator.addVertexWithUV(0.8125, 0.1875, 0.03125, 0.1875, 0.78035);

		tessellator.draw();

		// render trade item or block
		ItemStack itemstack = ((TileVendorFrame) te).getSellItem();
		if (itemstack != null) {
			ItemStack visStack = itemstack.copy();
			visStack.stackSize = 1;
			EntityItem entityitem = new EntityItem(null, 0.0D, 0.0D, 0.0D, visStack);
			entityitem.hoverStart = 0.0F;
			renderer.renderInFrame = true;
			RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.5D, 0.32D, 0.0635D, 0F, 0F);
			renderer.renderInFrame = false;
		}
		GL11.glPopMatrix();
	}
}
