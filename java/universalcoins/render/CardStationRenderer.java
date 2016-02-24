package universalcoins.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import universalcoins.UniversalCoins;

public class CardStationRenderer extends TileEntitySpecialRenderer {

	public CardStationRenderer() {
	}

	public void renderTileEntityAt(TileEntity tileEntity, double posX, double posY, double posZ, float p_180535_8_,
			int p_180535_9_) {

		ResourceLocation textures = (new ResourceLocation(UniversalCoins.modid,
				"textures/blocks/blockCardStation.png"));
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
		int brightness = (int) this.getWorld().getCombinedLight(tileEntity.getPos(), 0);
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
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		// right - working 4th
		worldrenderer.normal(0.0F, 0.0F, 1.0F);
		worldrenderer.pos(1, 0, 0).tex(0, 0).endVertex();
		worldrenderer.pos(1, 1, 0).tex(0, 1).endVertex();
		worldrenderer.pos(1, 1, 1).tex(1, 1).endVertex();
		worldrenderer.pos(1, 0, 1).tex(1, 0).endVertex();

		// front - working 2nd
		worldrenderer.normal(0.0F, 0.0F, 1.0F);
		worldrenderer.pos(1, 0, 1).tex(0, 0).endVertex();
		worldrenderer.pos(1, 1, 1).tex(0, 1).endVertex();
		worldrenderer.pos(0, 1, 1).tex(1, 1).endVertex();
		worldrenderer.pos(0, 0, 1).tex(1, 0).endVertex();

		// back - working 2nd, 4th
		worldrenderer.normal(0.0F, 0.0F, -1.0F);
		worldrenderer.pos(0, 0, 0).tex(0, 0).endVertex();
		worldrenderer.pos(0, 1, 0).tex(0, 1).endVertex();
		worldrenderer.pos(1, 1, 0).tex(1, 1).endVertex();
		worldrenderer.pos(1, 0, 0).tex(1, 0).endVertex();

		// bottom - working 2nd, 4th
		worldrenderer.normal(0.0F, -1.0F, 0.0F);
		worldrenderer.pos(1, 0, 0).tex(0, 0).endVertex();
		worldrenderer.pos(1, 0, 1).tex(0, 1).endVertex();
		worldrenderer.pos(0, 0, 1).tex(1, 1).endVertex();
		worldrenderer.pos(0, 0, 0).tex(1, 0).endVertex();

		// top - working 2nd, 4th
		worldrenderer.normal(0.0F, 1.0F, 0.0F);
		worldrenderer.pos(1, 1, 1).tex(0, 0).endVertex();
		worldrenderer.pos(1, 1, 0).tex(0, 1).endVertex();
		worldrenderer.pos(0, 1, 0).tex(1, 1).endVertex();
		worldrenderer.pos(0, 1, 1).tex(1, 0).endVertex();

		// left working 6th, 2nd, 4th
		worldrenderer.normal(0.0F, 0.0F, -1.0F);
		worldrenderer.pos(0, 0, 1).tex(0, 0).endVertex();
		worldrenderer.pos(0, 1, 1).tex(0, 1).endVertex();
		worldrenderer.pos(0, 1, 0).tex(1, 1).endVertex();
		worldrenderer.pos(0, 0, 0).tex(1, 0).endVertex();

		// // front top
		// worldrenderer.normal(0.0F, 0.0F, 1.0F);
		// worldrenderer.pos(0.1, 1, 1);
		// worldrenderer.tex(0.1, 0);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 0.9, 1);
		// worldrenderer.tex(0.1, 0.1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0.9, 1);
		// worldrenderer.tex(0.9, 0.1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 1, 1);
		// worldrenderer.tex(0.9, 0);
		// worldrenderer.endVertex();

		// // front left
		// worldrenderer.normal(0.0F, 0.0F, 1.0F);
		// worldrenderer.pos(0, 0, 1);
		// worldrenderer.tex(0, 0);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 0, 1);
		// worldrenderer.tex(0.1, 0);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 1, 1);
		// worldrenderer.tex(0.1, 1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0, 1, 1);
		// worldrenderer.tex(0, 1);
		// worldrenderer.endVertex();
		//
		// // front right
		// worldrenderer.normal(0.0F, 0.0F, 1.0F);
		// worldrenderer.pos(0.9, 0, 1);
		// worldrenderer.tex(0.9, 0);
		// worldrenderer.endVertex();
		// worldrenderer.pos(1, 0, 1);
		// worldrenderer.tex(1, 0);
		// worldrenderer.endVertex();
		// worldrenderer.pos(1, 1, 1);
		// worldrenderer.tex(1, 1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 1, 1);
		// worldrenderer.tex(0.9, 1);
		// worldrenderer.endVertex();
		//
		// // front bottom
		// worldrenderer.normal(0.0F, 0.0F, 1.0F);
		// worldrenderer.pos(0.1, 0.1, 1);
		// worldrenderer.tex(0.1, 0.9);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 0, 1);
		// worldrenderer.tex(0.1, 1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0, 1);
		// worldrenderer.tex(0.9, 1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0.1, 1);
		// worldrenderer.tex(0.9, 0.9);
		// worldrenderer.endVertex();
		//
		// // inside left
		// worldrenderer.normal(0.0F, 0.0F, 1.0F);
		// worldrenderer.pos(0.1, 0.9, 1);
		// worldrenderer.tex(0.1, 0.1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 0.1, 1);
		// worldrenderer.tex(0.1, 0.9);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 0.1, 0.7);
		// worldrenderer.tex(0.4, 0.9);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 0.9, 0.7);
		// worldrenderer.tex(0.4, 0.1);
		// worldrenderer.endVertex();
		//
		// // inside right
		// worldrenderer.normal(0.0F, 0.0F, -1.0F);
		// worldrenderer.pos(0.9, 0.9, 0.7);
		// worldrenderer.tex(0.1, 0.1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0.1, 0.7);
		// worldrenderer.tex(0.1, 0.9);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0.1, 1);
		// worldrenderer.tex(0.4, 0.9);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0.9, 1);
		// worldrenderer.tex(0.4, 0.1);
		// worldrenderer.endVertex();
		//
		// // inside top
		// worldrenderer.normal(0.0F, -1.0F, 0.0F);
		// worldrenderer.pos(0.1, 0.9, 0.1);
		// worldrenderer.tex(0.1, 0.9);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.1, 0.9, 0.7);
		// worldrenderer.tex(0.1, 0.1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0.9, 0.7);
		// worldrenderer.tex(0.9, 0.1);
		// worldrenderer.endVertex();
		// worldrenderer.pos(0.9, 0.9, 1);
		// worldrenderer.tex(0.9, 0.9);
		// worldrenderer.endVertex();

		tessellator.draw();

		// textures = (new ResourceLocation(UniversalCoins.modid,
		// "textures/blocks/blockCardStationFace.png"));
		// Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		//
		// worldrenderer.begin(GL11.GL_QUADS,
		// DefaultVertexFormats.POSITION_TEX);

		// // inside face lr bt bf
		// worldrenderer.normal(0.0F, 0.0F, 1.0F);
		// worldrenderer.pos(0.9, 0.1, 0.7).tex(0, 0).endVertex();
		// worldrenderer.pos(0.9, 0.9, 0.7).tex(0, 1).endVertex();
		// worldrenderer.pos(0.1, 0.9, 0.7).tex(1, 1).endVertex();
		// worldrenderer.pos(0.1, 0.1, 0.7).tex(1, 0).endVertex();

		// // inside bottom - working
		// worldrenderer.normal(0.0F, 1.0F, 0.0F);
		// worldrenderer.pos(0.1, 0.4, 0.7).tex(0, 0.55).endVertex();
		// worldrenderer.pos(0.1, 0.1, 1).tex(0, 1).endVertex();
		// worldrenderer.pos(0.9, 0.1, 1).tex(1, 1).endVertex();
		// worldrenderer.pos(0.9, 0.4, 0.7).tex(1, 0.55).endVertex();
		//
		// tessellator.draw();
		GL11.glPopMatrix();
	}
}
