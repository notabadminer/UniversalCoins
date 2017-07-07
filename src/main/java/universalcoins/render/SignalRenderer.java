package universalcoins.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import universalcoins.tileentity.TileSignal;

public class SignalRenderer extends TileEntitySpecialRenderer {

	public SignalRenderer() {
	}

	public void render(TileEntity tileentity, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		TileSignal te = (TileSignal) tileentity;

		// adjust block rotation based on block meta
		int meta = tileentity.getBlockMetadata();
		if (meta == -1) { // fix for inventory crash on get block meta
			try {
				meta = tileentity.getBlockMetadata();
			} catch (Throwable ex2) {
				// do nothing
			}
		}

		BlockPos tPos = new BlockPos(tileentity.getPos().getX(), tileentity.getPos().getY() + 1,
				tileentity.getPos().getZ());
		int brightness = (int) this.getWorld().getCombinedLight(tPos, 0);
		GL11.glPushMatrix();
		float correction = 0.0F;
		if (meta == 2) {
			correction = 180.0F;
		}
		if (meta == 4) {
			correction = 90.0F;
		}
		if (meta == 5) {
			correction = -90.0F;
		}
		GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GlStateManager.rotate(-correction, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);

		// draw text on front face
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
				String s = time + " " + (time > 1 ? I18n.translateToLocal("signal.face.label.seconds")
						: I18n.translateToLocal("signal.face.label.second"));
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 16, b0);
			} else {
				int duration = te.duration;
				int coins = te.fee;
				String s = coins + " " + (coins > 1 ? I18n.translateToLocal("signal.face.label.coins")
						: I18n.translateToLocal("signal.face.label.coin"));
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 6, b0);
				String s2 = duration + " " + (duration > 1 ? I18n.translateToLocal("signal.face.label.seconds")
						: I18n.translateToLocal("signal.face.label.second"));
				fontrenderer.drawString(s2, -fontrenderer.getStringWidth(s2) / 2, 16, b0);
			}
			GL11.glDepthMask(true);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GL11.glPopMatrix();
	}
}
