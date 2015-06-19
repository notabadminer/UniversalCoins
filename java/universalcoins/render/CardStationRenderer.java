package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import universalcoins.UniversalCoins;

public class CardStationRenderer extends TileEntitySpecialRenderer {

	public CardStationRenderer() {
	}

	public void renderTileEntityAt(TileEntity tileEntity, double posX,
			double posY, double posZ, float p_180535_8_, int p_180535_9_) {

		ResourceLocation textures = (new ResourceLocation(UniversalCoins.modid, "textures/blocks/blockCardStation.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		// adjust block rotation based on block meta
		int meta = tileEntity.getBlockMetadata();
		if (meta == -1) { // fix for inventory crash on get block meta
			try {
				meta = tileEntity.getBlockMetadata();
			} catch (Throwable ex2) {
				// do nothing
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		BlockPos tPos = new BlockPos(tileEntity.getPos().getX(), tileEntity.getPos().getY() + 1, tileEntity.getPos().getZ());
		int brightness = (int) this.getWorld().getCombinedLight(tPos, 0);
		GL11.glPushMatrix();
		float f3 = 0.0F;
		if (meta == 2) {
			f3 = 180.0F;
		}
		if (meta == 4) {
			f3 = 90.0F;
		}
		if (meta == 5) {
			f3 = -90.0F;
		}
		GlStateManager.translate((float) posX + 0.5F, (float) posY + 0.5F, (float) posZ + 0.5F);
		GlStateManager.rotate(-f3, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		worldrenderer.startDrawingQuads();
		worldrenderer.setBrightness(brightness);
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F); //top

		
		// back
		worldrenderer.setNormal(0.0F, 0.0F, -1.0F);
		worldrenderer.addVertexWithUV(0, 0, 0, 0, 0);
		worldrenderer.addVertexWithUV(0, 1, 0, 0, 1);
		worldrenderer.addVertexWithUV(1, 1, 0, 1, 1);
		worldrenderer.addVertexWithUV(1, 0, 0, 1, 0);

		// bottom
		worldrenderer.setNormal(0.0F, -1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0, 0, 1, 0, 0);
		worldrenderer.addVertexWithUV(0, 0, 0, 0, 1);
		worldrenderer.addVertexWithUV(1, 0, 0, 1, 1);
		worldrenderer.addVertexWithUV(1, 0, 1, 1, 0);

		// left
		worldrenderer.setNormal(0.0F, 0.0F, -1.0F);
		worldrenderer.addVertexWithUV(0, 0, 1, 0, 0);
		worldrenderer.addVertexWithUV(0, 1, 1, 0, 1);
		worldrenderer.addVertexWithUV(0, 1, 0, 1, 1);
		worldrenderer.addVertexWithUV(0, 0, 0, 1, 0);

		// right
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(1, 1, 1, 0, 0);
		worldrenderer.addVertexWithUV(1, 0, 1, 0, 1);
		worldrenderer.addVertexWithUV(1, 0, 0, 1, 1);
		worldrenderer.addVertexWithUV(1, 1, 0, 1, 0);

		// top
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
		worldrenderer.addVertexWithUV(1, 1, 1, 0, 0);
		worldrenderer.addVertexWithUV(1, 1, 0, 0, 1);
		worldrenderer.addVertexWithUV(0, 1, 0, 1, 1);
		worldrenderer.addVertexWithUV(0, 1, 1, 1, 0);
		
		// front top
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.1, 1, 1, 0.1, 0);
		worldrenderer.addVertexWithUV(0.1, 0.9, 1, 0.1, 0.1);
		worldrenderer.addVertexWithUV(0.9, 0.9, 1, 0.9, 0.1);
		worldrenderer.addVertexWithUV(0.9, 1, 1, 0.9, 0);

		// front left
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0, 0, 1, 0, 0);
		worldrenderer.addVertexWithUV(0.1, 0, 1, 0.1, 0);
		worldrenderer.addVertexWithUV(0.1, 1, 1, 0.1, 1);
		worldrenderer.addVertexWithUV(0, 1, 1, 0, 1);
		
		// front right
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.9, 0, 1, 0.9, 0);
		worldrenderer.addVertexWithUV(1, 0, 1, 1, 0);
		worldrenderer.addVertexWithUV(1, 1, 1, 1, 1);
		worldrenderer.addVertexWithUV(0.9, 1, 1, 0.9, 1);
		
		// front bottom
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.1, 0.1, 1, 0.1, 0.9);
		worldrenderer.addVertexWithUV(0.1, 0, 1, 0.1, 1);
		worldrenderer.addVertexWithUV(0.9, 0, 1, 0.9, 1);
		worldrenderer.addVertexWithUV(0.9, 0.1, 1, 0.9, 0.9);
		
		// inside left
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.1, 0.9, 1, 0.1, 0.1);
		worldrenderer.addVertexWithUV(0.1, 0.1, 1, 0.1, 0.9);
		worldrenderer.addVertexWithUV(0.1, 0.1, 0.7, 0.4, 0.9);
		worldrenderer.addVertexWithUV(0.1, 0.9, 0.7, 0.4, 0.1);
		
		// inside right
		worldrenderer.setNormal(0.0F, 0.0F, -1.0F);
		worldrenderer.addVertexWithUV(0.9, 0.9, 0.7, 0.1, 0.1);
		worldrenderer.addVertexWithUV(0.9, 0.1, 0.7, 0.1, 0.9);
		worldrenderer.addVertexWithUV(0.9, 0.1, 1, 0.4, 0.9);
		worldrenderer.addVertexWithUV(0.9, 0.9, 1, 0.4, 0.1);
		
		// inside top
		worldrenderer.setNormal(0.0F, -1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.1, 0.9, 1, 0.1, 0.9);
		worldrenderer.addVertexWithUV(0.1, 0.9, 0.7, 0.1, 0.1);
		worldrenderer.addVertexWithUV(0.9, 0.9, 0.7, 0.9, 0.1);
		worldrenderer.addVertexWithUV(0.9, 0.9, 1, 0.9, 0.9);
		
		tessellator.draw();
		
		textures = (new ResourceLocation(UniversalCoins.modid, "textures/blocks/blockCardStationFace.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		worldrenderer.startDrawingQuads();
		worldrenderer.setBrightness(brightness);
				
		// inside face
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.1, 0.9, 0.7, 0, 0);
		worldrenderer.addVertexWithUV(0.1, 0.1, 0.7, 0, 1);
		worldrenderer.addVertexWithUV(0.9, 0.1, 0.7, 1, 1);
		worldrenderer.addVertexWithUV(0.9, 0.9, 0.7, 1, 0);

		// inside bottom
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.1, 0.4, 0.7, 0, 0.55);
		worldrenderer.addVertexWithUV(0.1, 0.1, 1, 0, 1);
		worldrenderer.addVertexWithUV(0.9, 0.1, 1, 1, 1);
		worldrenderer.addVertexWithUV(0.9, 0.4, 0.7, 1, 0.55);
		
		tessellator.draw();
		GL11.glPopMatrix();
	}
}
