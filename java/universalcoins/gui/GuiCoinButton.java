package universalcoins.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class GuiCoinButton extends GuiButton {
	protected ResourceLocation buttonTexture = new ResourceLocation("universalcoins", "textures/gui/buttons.png");
	protected int index;

	public GuiCoinButton(int id, int xPos, int yPos, int width, int height, String displayString, int index) {
		super(id, xPos, yPos, width, height, displayString);
		this.index = index;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			FontRenderer fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(buttonTexture);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y
					&& mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = this.getHoverState(this.hovered);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, 0 + index * this.width, 0 + k * this.height,
					this.width, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
			int l = 14737632;

			if (packedFGColour != 0) {
				l = packedFGColour;
			} else if (!this.enabled) {
				l = 10526880;
			} else if (this.hovered) {
				l = 16777120;
			}

			this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2,
					this.y + (this.height - 8) / 2, l);
		}
	}

}
