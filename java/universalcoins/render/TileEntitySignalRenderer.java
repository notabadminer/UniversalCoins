package universalcoins.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSignal;

public class TileEntitySignalRenderer extends TileEntitySpecialRenderer {

	public TileEntitySignalRenderer() {
	}

	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		ResourceLocation textures = (new ResourceLocation(UniversalCoins.MODID, "textures/blocks/signalblock.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		TileUCSignal te = (TileUCSignal) tileentity;

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
		tessellator.addVertexWithUV(1, 1, 0, 0, 0);
		tessellator.addVertexWithUV(1, 0, 0, 0, 1);
		tessellator.addVertexWithUV(0, 0, 0, 1, 1);
		tessellator.addVertexWithUV(0, 1, 0, 1, 0);

		// left
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addVertexWithUV(0, 1, 0, 0, 0);
		tessellator.addVertexWithUV(0, 0, 0, 0, 1);
		tessellator.addVertexWithUV(0, 0, 1, 1, 1);
		tessellator.addVertexWithUV(0, 1, 1, 1, 0);

		// right
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(1, 1, 1, 0, 0);
		tessellator.addVertexWithUV(1, 0, 1, 0, 1);
		tessellator.addVertexWithUV(1, 0, 0, 1, 1);
		tessellator.addVertexWithUV(1, 1, 0, 1, 0);

		tessellator.draw();

		textures = (new ResourceLocation(UniversalCoins.MODID, "textures/blocks/signalblock_top.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);

		tessellator.startDrawingQuads();

		// top
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(1, 1, 1, 0, 0);
		tessellator.addVertexWithUV(1, 1, 0, 0, 1);
		tessellator.addVertexWithUV(0, 1, 0, 1, 1);
		tessellator.addVertexWithUV(0, 1, 1, 1, 0);

		// bottom
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertexWithUV(0, 0, 1, 0, 0);
		tessellator.addVertexWithUV(0, 0, 0, 0, 1);
		tessellator.addVertexWithUV(1, 0, 0, 1, 1);
		tessellator.addVertexWithUV(1, 0, 1, 1, 0);

		tessellator.draw();

		textures = (new ResourceLocation(UniversalCoins.MODID, "textures/blocks/signalblock_face.png"));
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);

		tessellator.startDrawingQuads();

		// front face
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0, 1, 1, 0, 0);
		tessellator.addVertexWithUV(0, 0, 1, 0, 1);
		tessellator.addVertexWithUV(1, 0, 1, 1, 1);
		tessellator.addVertexWithUV(1, 1, 1, 1, 0);

		tessellator.draw();

		// draw text on front face
		FontRenderer fontrenderer = this.func_147498_b();
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
