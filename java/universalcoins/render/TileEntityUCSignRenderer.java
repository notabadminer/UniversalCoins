package universalcoins.render;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

@SideOnly(Side.CLIENT)
public class TileEntityUCSignRenderer extends TileEntitySpecialRenderer {
	private int counter = 0;
	private boolean showStick = false;

	public void renderTileEntityAt(TileUCSign tileEntity, double xCoord, double yCoord, double zCoord,
			float p_147512_8_) {
		ResourceLocation blockTexture = new ResourceLocation("textures/blocks/planks_birch.png");
		Block block = tileEntity.getBlockType();
		GL11.glPushMatrix();
		float f1 = 0.6666667F;
		float f3;

		if (tileEntity.blockIcon != null && tileEntity.blockIcon != "") {
			String[] tempIconName = tileEntity.blockIcon.split(":", 3);
			if (tempIconName.length == 1) {
				// if minecraft, set resourcelocation using last part
				blockTexture = (new ResourceLocation("textures/blocks/" + tempIconName[0] + ".png"));
			} else {
				// if mod use mod path
				blockTexture = (new ResourceLocation(tempIconName[0] + ":textures/blocks/" + tempIconName[1] + ".png"));
			}
		}

		if (block == UniversalCoins.proxy.standing_ucsign) {
			float f2 = (float) (tileEntity.getBlockMetadata() * 360) / 16.0F;
			GL11.glTranslatef((float) xCoord + 0.5F, (float) yCoord + 0.5F, (float) zCoord + 0.5F);
			GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

			GL11.glTranslatef(0.0F, 0.3F, 0.45F);
			showStick = true;
		} else {
			int j = tileEntity.getBlockMetadata();
			f3 = 0.0F;

			if (j == 2) {
				f3 = 180.0F;
			}

			if (j == 4) {
				f3 = 90.0F;
			}

			if (j == 5) {
				f3 = -90.0F;
			}

			GL11.glTranslatef((float) xCoord + 0.5F, (float) yCoord + 0.5F, (float) zCoord + 0.5F);
			GL11.glRotatef(-f3, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			showStick = false;
		}

		this.bindTexture(blockTexture);
		GL11.glPushMatrix();
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		// back
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(1.0F, 0.78125F, 0.02F, 0.0F, 0.9F);
		tessellator.addVertexWithUV(1.0F, 0.28125F, 0.02F, 0.0F, 0.2F);
		tessellator.addVertexWithUV(0.0F, 0.28125F, 0.02F, 1.0F, 0.2F);
		tessellator.addVertexWithUV(0.0F, 0.78125F, 0.02F, 1.0F, 0.9F);

		// top
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(0.0F, 0.78125F, 0.02F, 0.125, 0);
		tessellator.addVertexWithUV(0.0F, 0.78125F, 0.1F, 0.125, 0.0625);
		tessellator.addVertexWithUV(1.0F, 0.78125F, 0.1F, 0.875, 0.0625);
		tessellator.addVertexWithUV(1.0F, 0.78125F, 0.02F, 0.875, 0);

		// bottom
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertexWithUV(0.0F, 0.28125F, 0.1F, 0.0F, 0.82F);
		tessellator.addVertexWithUV(0.0F, 0.28125F, 0.02F, 0.0F, 0.9F);
		tessellator.addVertexWithUV(1.0F, 0.28125F, 0.02F, 1.0F, 0.9F);
		tessellator.addVertexWithUV(1.0F, 0.28125F, 0.1F, 1.0F, 0.82F);

		// left
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(0.0F, 0.78125F, 0.1F, 0, 0.9F);
		tessellator.addVertexWithUV(0.0F, 0.78125F, 0.02F, 0.125, 0.9F);
		tessellator.addVertexWithUV(0.0F, 0.28125F, 0.02F, 0, 0.2F);
		tessellator.addVertexWithUV(0.0F, 0.28125F, 0.1F, 0.125, 0.2F);

		// right
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(1.0F, 0.78125F, 0.02F, 0.875, 0.9F);
		tessellator.addVertexWithUV(1.0F, 0.78125F, 0.1F, 0.75, 0.9F);
		tessellator.addVertexWithUV(1.0F, 0.28125F, 0.1F, 0.75, 0.2F);
		tessellator.addVertexWithUV(1.0F, 0.28125F, 0.02F, 0.875, 0.2F);

		// front //LR, TB, FB //LR, TB
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(0.0F, 0.28125F, 0.1F, 0.0F, 0.2F); // BL
		tessellator.addVertexWithUV(1.0F, 0.28125F, 0.1F, 1.0F, 0.2F); // BR
		tessellator.addVertexWithUV(1.0F, 0.78125F, 0.1F, 1.0F, 0.9F); // TR
		tessellator.addVertexWithUV(0.0F, 0.78125F, 0.1F, 0.0F, 0.9F); // TL

		if (showStick) {
			// draw stick
			// front
			tessellator.setNormal(0.0F, 0.0F, 1.0F);
			tessellator.addVertexWithUV(0.45F, -0.35F, 0.1F, 0.62F, 0.1F); // BL
			tessellator.addVertexWithUV(0.55F, -0.35F, 0.1F, 0.62F, 0.0F); // BR
			tessellator.addVertexWithUV(0.55F, 0.28125F, 0.1F, 0.0F, 0.0F); // TR
			tessellator.addVertexWithUV(0.45F, 0.28125F, 0.1F, 0.0F, 0.1F); // TL

			// right
			tessellator.setNormal(1.0F, 0.0F, 0.0F);
			tessellator.addVertexWithUV(0.55F, -0.35F, 0.1F, 0.62F, 0.0F); // BL
			tessellator.addVertexWithUV(0.55F, -0.35F, 0.02F, 0.62F, 0.0F); // BR
			tessellator.addVertexWithUV(0.55F, 0.28125F, 0.02F, 0.0F, 0.0F); // TR
			tessellator.addVertexWithUV(0.55F, 0.28125F, 0.1F, 0.0F, 0.0F); // TL

			// left
			tessellator.setNormal(-1.0F, 0.0F, 0.0F);
			tessellator.addVertexWithUV(0.45F, -0.35F, 0.02F, 0.62F, 0.1F); // BL
			tessellator.addVertexWithUV(0.45F, -0.35F, 0.1F, 0.62F, 0.1F); // BR
			tessellator.addVertexWithUV(0.45F, 0.28125F, 0.1F, 0.0F, 0.1F); // TR
			tessellator.addVertexWithUV(0.45F, 0.28125F, 0.02F, 0.0F, 0.1F); // TL

			// back
			tessellator.setNormal(0.0F, 0.0F, -1.0F);
			tessellator.addVertexWithUV(0.55F, -0.35F, 0.02F, 0.62F, 0.0F); // BL
			tessellator.addVertexWithUV(0.45F, -0.35F, 0.02F, 0.62F, 0.1F); // BR
			tessellator.addVertexWithUV(0.45F, 0.28125F, 0.02F, 0.0F, 0.1F); // TR
			tessellator.addVertexWithUV(0.55F, 0.28125F, 0.02F, 0.0F, 0.0F); // TL
		}

		tessellator.draw();
		GL11.glPopMatrix();
		FontRenderer fontrenderer = this.func_147498_b();
		f3 = 0.016666668F * f1;
		GL11.glTranslatef(0.5F, 0.5F, 0.105F);
		GL11.glScalef(f3, -f3, f3);
		GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
		GL11.glDepthMask(false);
		int[] colors = { 0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA, 0x555555,
				0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF };

		for (int i = 0; i < tileEntity.signText.length; ++i) {
			String s = tileEntity.signText[i];
			int colorCode = 0;
			if (s.startsWith("&") && s.length() > 1 && String.valueOf(s.charAt(1)).matches("[0-9a-fA-F]+")) {
				colorCode = Integer.parseInt(s.substring(1, 2), 16);
				s = s.substring(2);
			}

			if (i == tileEntity.lineBeingEdited) {
				s = "> " + s + " <";
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, i * 10 - tileEntity.signText.length * 5,
						colors[colorCode]);
			} else {
				if (s.length() <= 16) {
					fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2,
							i * 10 - tileEntity.signText.length * 5, colors[colorCode]);
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
							i * 10 - tileEntity.signText.length * 5, colors[colorCode]);
					counter++;
					if (counter / 10 > s.length() + 8)
						counter = 0;
				}
			}
		}

		GL11.glDepthMask(true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}

	public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_,
			float p_147500_8_) {
		this.renderTileEntityAt((TileUCSign) p_147500_1_, p_147500_2_, p_147500_4_, p_147500_6_, p_147500_8_);
	}
}
