package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import universalcoins.container.ContainerSafe;
import universalcoins.tileentity.TileSafe;

public class SafeGUI extends GuiContainer {
	private TileSafe tEntity;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###");

	public SafeGUI(InventoryPlayer inventoryPlayer, TileSafe tileEntity) {
		super(new ContainerSafe(inventoryPlayer, tileEntity));
		tEntity = tileEntity;

		xSize = 176;
		ySize = 152;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/safe.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		fontRenderer.drawString(tEntity.getName(), 6, 5, 4210752);

		fontRenderer.drawString(I18n.format("container.inventory"), 6, 58, 4210752);

		// display player account balance
		String formattedBalance = formatter.format(tEntity.accountBalance);
		int balLength = fontRenderer.getStringWidth(formattedBalance);
		fontRenderer.drawString(formattedBalance, 154 - balLength, 22, 4210752);
	}
}