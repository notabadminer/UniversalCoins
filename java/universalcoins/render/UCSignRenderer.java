package universalcoins.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

@SideOnly(Side.CLIENT)
public class UCSignRenderer extends TileEntitySpecialRenderer<TileEntitySign> {
	private int counter = 0;
	private boolean showStick = false;
	private final ModelSign model = new ModelSign();

	public void renderTileEntityAt(TileUCSign tileEntity, double posX, double posY, double posZ, float partialTicks,
			int destroyStage) {
		TileUCSign te = null;
		float f3;
		if (tileEntity instanceof TileUCSign) {
			te = (TileUCSign) tileEntity;
		} else {
			return;
		}
		ResourceLocation blockTexture = new ResourceLocation("textures/blocks/planks_birch.png");
		/** The ModelSign instance for use in this renderer */
		final ModelSign model = new ModelSign();

		Block block = te.getBlockType();
		GlStateManager.pushMatrix();
		float f = 0.6666667F;

		if (block == UniversalCoins.proxy.standing_ucsign) {
			GlStateManager.translate((float) posX + 0.5F, (float) posY + 0.75F * f, (float) posZ + 0.5F);
			float f1 = (float) (te.getBlockMetadata() * 360) / 16.0F;
			GlStateManager.rotate(-f1, 0.0F, 1.0F, 0.0F);
			this.model.signStick.showModel = true;
		} else {
			int k = te.getBlockMetadata();
			float f2 = 0.0F;

			if (k == 2) {
				f2 = 180.0F;
			}

			if (k == 4) {
				f2 = 90.0F;
			}

			if (k == 5) {
				f2 = -90.0F;
			}

			GlStateManager.translate((float) posX + 0.5F, (float) posY + 0.75F * f, (float) posZ + 0.5F);
			GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
			this.model.signStick.showModel = false;
		}

		if (destroyStage >= 0) {
			this.bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(4.0F, 2.0F, 1.0F);
			GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		} else {
			this.bindTexture(blockTexture);
		}

		// GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.scale(f, -f, -f);
		this.model.renderSign();
		GlStateManager.popMatrix();
		FontRenderer fontrenderer = this.getFontRenderer();
		f3 = 0.016666668F * f;
		GlStateManager.translate(0.0F, 0.5F * f, 0.07F * f);
		GlStateManager.scale(f3, -f3, f3);
		GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
		GlStateManager.depthMask(false);

		int[] colors = { 0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA, 0x555555,
				0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF };

		for (int i = 0; i < te.signText.length; ++i) {
			String s = te.signText[i].getUnformattedText();
			int colorCode = 0;
			if (s.startsWith("§r"))
				s = s.substring(2);
			if (s.startsWith("&") && s.length() > 1 && String.valueOf(s.charAt(1)).matches("[0-9a-fA-F]+")) {
				colorCode = Integer.parseInt(s.substring(1, 2), 16);
				s = s.substring(2);
			}

			if (i == te.lineBeingEdited) {
				s = "> " + s + " <";
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, i * 10 - te.signText.length * 5,
						colors[colorCode]);
			} else {
				if (s.length() <= 16) {
					fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, i * 10 - te.signText.length * 5,
							colors[colorCode]);
				} else {
					// display a subset of string while scrolling through entire
					// string
					String subset = "";
					if (counter / 10 < s.length() - 8) {
						subset = s.substring(Math.min(counter / 10, s.length() - 15),
								Math.min(counter / 10 + 15, s.length()));
					} else {
						subset = s.substring(0, 15);
					}
					fontrenderer.drawString(subset, -fontrenderer.getStringWidth(subset) / 2,
							i * 10 - te.signText.length * 5, colors[colorCode]);
					counter++;
					if (counter / 10 > s.length() + 8)
						counter = 0;
				}
			}
		}
		GlStateManager.depthMask(true);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();

		if (destroyStage >= 0) {
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}

	@Override
	public void renderTileEntityAt(TileEntitySign te, double x, double y, double z, float partialTicks,
			int destroyStage) {
		this.renderTileEntityAt((TileUCSign) te, x, y, z, partialTicks, destroyStage);

	}
}
