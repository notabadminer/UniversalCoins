package universalcoins.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import universalcoins.UniversalCoins;

public class TileEntityCardStationRenderer extends TileEntitySpecialRenderer {

	public TileEntityCardStationRenderer() {
	}

	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		ResourceLocation textures = (new ResourceLocation(UniversalCoins.MODID,
				"textures/blocks/atm.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);

		// adjust block rotation based on block meta
		int meta = tileentity.blockMetadata;
		if (meta == -1) { // fix for inventory crash on get block meta
			try {
				meta = tileentity.getBlockMetadata();
			} catch (Throwable ex2) {
				// do nothing
			}
		}

		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glRotatef(meta * -90F, 0F, 1F, 0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F); // top

		// back
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addVertexWithUV(0, 0, 0, 0, 0);
		tessellator.addVertexWithUV(0, 1, 0, 0, 1);
		tessellator.addVertexWithUV(1, 1, 0, 1, 1);
		tessellator.addVertexWithUV(1, 0, 0, 1, 0);

		// bottom
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertexWithUV(0, 0, 1, 0, 0);
		tessellator.addVertexWithUV(0, 0, 0, 0, 1);
		tessellator.addVertexWithUV(1, 0, 0, 1, 1);
		tessellator.addVertexWithUV(1, 0, 1, 1, 0);

		// left
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addVertexWithUV(0, 0, 1, 0, 0);
		tessellator.addVertexWithUV(0, 1, 1, 0, 1);
		tessellator.addVertexWithUV(0, 1, 0, 1, 1);
		tessellator.addVertexWithUV(0, 0, 0, 1, 0);

		// right
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(1, 1, 1, 0, 0);
		tessellator.addVertexWithUV(1, 0, 1, 0, 1);
		tessellator.addVertexWithUV(1, 0, 0, 1, 1);
		tessellator.addVertexWithUV(1, 1, 0, 1, 0);

		// top
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(1, 1, 1, 0, 0);
		tessellator.addVertexWithUV(1, 1, 0, 0, 1);
		tessellator.addVertexWithUV(0, 1, 0, 1, 1);
		tessellator.addVertexWithUV(0, 1, 1, 1, 0);

		// front top
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.1, 1, 1, 0.1, 0);
		tessellator.addVertexWithUV(0.1, 0.9, 1, 0.1, 0.1);
		tessellator.addVertexWithUV(0.9, 0.9, 1, 0.9, 0.1);
		tessellator.addVertexWithUV(0.9, 1, 1, 0.9, 0);

		// front left
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0, 0, 1, 0, 0);
		tessellator.addVertexWithUV(0.1, 0, 1, 0.1, 0);
		tessellator.addVertexWithUV(0.1, 1, 1, 0.1, 1);
		tessellator.addVertexWithUV(0, 1, 1, 0, 1);

		// front right
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.9, 0, 1, 0.9, 0);
		tessellator.addVertexWithUV(1, 0, 1, 1, 0);
		tessellator.addVertexWithUV(1, 1, 1, 1, 1);
		tessellator.addVertexWithUV(0.9, 1, 1, 0.9, 1);

		// front bottom
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.1, 0.1, 1, 0.1, 0.9);
		tessellator.addVertexWithUV(0.1, 0, 1, 0.1, 1);
		tessellator.addVertexWithUV(0.9, 0, 1, 0.9, 1);
		tessellator.addVertexWithUV(0.9, 0.1, 1, 0.9, 0.9);

		// inside left
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.1, 0.9, 1, 0.1, 0.1);
		tessellator.addVertexWithUV(0.1, 0.1, 1, 0.1, 0.9);
		tessellator.addVertexWithUV(0.1, 0.1, 0.7, 0.4, 0.9);
		tessellator.addVertexWithUV(0.1, 0.9, 0.7, 0.4, 0.1);

		// inside right
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addVertexWithUV(0.9, 0.9, 0.7, 0.1, 0.1);
		tessellator.addVertexWithUV(0.9, 0.1, 0.7, 0.1, 0.9);
		tessellator.addVertexWithUV(0.9, 0.1, 1, 0.4, 0.9);
		tessellator.addVertexWithUV(0.9, 0.9, 1, 0.4, 0.1);

		// inside top
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertexWithUV(0.1, 0.9, 1, 0.1, 0.9);
		tessellator.addVertexWithUV(0.1, 0.9, 0.7, 0.1, 0.1);
		tessellator.addVertexWithUV(0.9, 0.9, 0.7, 0.9, 0.1);
		tessellator.addVertexWithUV(0.9, 0.9, 1, 0.9, 0.9);

		tessellator.draw();

		textures = (new ResourceLocation(UniversalCoins.MODID, "textures/blocks/atm_face.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);

		tessellator.startDrawingQuads();

		// inside face
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.1, 0.9, 0.7, 0, 0);
		tessellator.addVertexWithUV(0.1, 0.1, 0.7, 0, 1);
		tessellator.addVertexWithUV(0.9, 0.1, 0.7, 1, 1);
		tessellator.addVertexWithUV(0.9, 0.9, 0.7, 1, 0);

		// inside bottom
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(0.1, 0.4, 0.7, 0, 0.55);
		tessellator.addVertexWithUV(0.1, 0.1, 1, 0, 1);
		tessellator.addVertexWithUV(0.9, 0.1, 1, 1, 1);
		tessellator.addVertexWithUV(0.9, 0.4, 0.7, 1, 0.55);

		tessellator.draw();
		GL11.glPopMatrix();
	}
}
