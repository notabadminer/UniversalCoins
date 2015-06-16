package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalcoins.UniversalCoins;
import universalcoins.tile.TileSignal;

public class TileEntitySignalRenderer extends TileEntitySpecialRenderer {

	public TileEntitySignalRenderer() {
	}

	public void renderTileEntityAt(TileEntity tileentity, double posX,
			double posY, double posZ, float p_180535_8_, int p_180535_9_) {

		ResourceLocation textures = (new ResourceLocation(UniversalCoins.modid, "textures/blocks/blockSignal.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		TileSignal te = (TileSignal)tileentity;
		
		// adjust block rotation based on block meta
		int meta = tileentity.getBlockMetadata();
		if (meta == -1) { // fix for inventory crash on get block meta
			try {
				meta = tileentity.getBlockMetadata();
			} catch (Throwable ex2) {
				// do nothing
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) posX + 0.5F,(float) posY + 0.5F,(float) posZ + 0.5F);
		GL11.glRotatef(meta * -90F, 0F, 1F, 0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		worldrenderer.startDrawingQuads();
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F); //top

		
		// back
		worldrenderer.setNormal(0.0F, 0.0F, -1.0F);
		worldrenderer.addVertexWithUV(1, 1, 0, 0, 0);
		worldrenderer.addVertexWithUV(1, 0, 0, 0, 1);
		worldrenderer.addVertexWithUV(0, 0, 0, 1, 1);
		worldrenderer.addVertexWithUV(0, 1, 0, 1, 0);

		// left
		worldrenderer.setNormal(0.0F, 0.0F, -1.0F);
		worldrenderer.addVertexWithUV(0, 1, 0, 0, 0);
		worldrenderer.addVertexWithUV(0, 0, 0, 0, 1);
		worldrenderer.addVertexWithUV(0, 0, 1, 1, 1);
		worldrenderer.addVertexWithUV(0, 1, 1, 1, 0);

		// right
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(1, 1, 1, 0, 0);
		worldrenderer.addVertexWithUV(1, 0, 1, 0, 1);
		worldrenderer.addVertexWithUV(1, 0, 0, 1, 1);
		worldrenderer.addVertexWithUV(1, 1, 0, 1, 0);
		
		worldrenderer.startDrawingQuads();
		
		textures = (new ResourceLocation(UniversalCoins.modid, "textures/blocks/blockSignal_top.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		worldrenderer.startDrawingQuads();

		// top
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
		worldrenderer.addVertexWithUV(1, 1, 1, 0, 0);
		worldrenderer.addVertexWithUV(1, 1, 0, 0, 1);
		worldrenderer.addVertexWithUV(0, 1, 0, 1, 1);
		worldrenderer.addVertexWithUV(0, 1, 1, 1, 0);
		
		// bottom
		worldrenderer.setNormal(0.0F, -1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0, 0, 1, 0, 0);
		worldrenderer.addVertexWithUV(0, 0, 0, 0, 1);
		worldrenderer.addVertexWithUV(1, 0, 0, 1, 1);
		worldrenderer.addVertexWithUV(1, 0, 1, 1, 0);
		
		worldrenderer.startDrawingQuads();
		
		textures = (new ResourceLocation(UniversalCoins.modid, "textures/blocks/blockSignal_face.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		worldrenderer.startDrawingQuads();
		
		// front face
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0, 1, 1, 0, 0);
		worldrenderer.addVertexWithUV(0, 0, 1, 0, 1);
		worldrenderer.addVertexWithUV(1, 0, 1, 1, 1);
		worldrenderer.addVertexWithUV(1, 1, 1, 1, 0);
		
		worldrenderer.startDrawingQuads();
		
		//draw text on front face
		FontRenderer fontrenderer = this.getFontRenderer();
		byte b0 = 0;
		
		if (fontrenderer != null) {
			float f1 = 0.6666667F;
	        float f3 = 0.016666668F * f1;
	        GL11.glTranslatef(0.5F, 1F, 1.0F + f3);
	        GL11.glScalef(0.01F, -f3, f3);
	        GL11.glDepthMask(false);
	        if (te.secondsLeft > 0) {
	        	int time = te.secondsLeft;
	        	String s = time + " " + (time > 1 ? StatCollector.translateToLocal("signal.face.label.seconds")
						: StatCollector.translateToLocal("signal.face.label.second"));
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 16, b0);
	        } else {
				int duration = te.duration;
		        int coins = te.fee;
				String s = coins + " " + (coins > 1 ? StatCollector.translateToLocal("signal.face.label.coins")
						: StatCollector.translateToLocal("signal.face.label.coin"));
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 6, b0);
				String s2 = duration + " " + (duration > 1 ? StatCollector.translateToLocal("signal.face.label.seconds")
						: StatCollector.translateToLocal("signal.face.label.second"));
				fontrenderer.drawString(s2, -fontrenderer.getStringWidth(s2) / 2, 16, b0);
	        }
			GL11.glDepthMask(true);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
        
		GL11.glPopMatrix();
	}
}
