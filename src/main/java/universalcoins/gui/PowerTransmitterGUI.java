package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import universalcoins.container.ContainerPowerTransmitter;
import universalcoins.tileentity.TilePowerTransmitter;

public class PowerTransmitterGUI extends GuiContainer {
	private TilePowerTransmitter tEntity;
	private GuiButton coinButton, accessModeButton;
	public static final int idCoinButton = 0;
	public static final int idAccessModeButton = 1;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###");

	public PowerTransmitterGUI(InventoryPlayer inventoryPlayer, TilePowerTransmitter tileEntity) {
		super(new ContainerPowerTransmitter(inventoryPlayer, tileEntity));
		tEntity = tileEntity;

		xSize = 176;
		ySize = 157;
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
	public void initGui() {
		super.initGui();
		coinButton = new GuiSlimButton(idCoinButton, 123 + (width - xSize) / 2, 60 + (height - ySize) / 2, 46, 12,
				I18n.format("general.button.coin"));
		accessModeButton = new GuiSlimButton(idAccessModeButton, 122 + (width - xSize) / 2, 4 + (height - ySize) / 2,
				50, 12, I18n.format("general.label.public"));
		buttonList.clear();
		buttonList.add(coinButton);
		buttonList.add(accessModeButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/power_transmitter.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		if (tEntity.publicAccess) {
			accessModeButton.displayString = I18n.format("general.label.public");
		} else {
			accessModeButton.displayString = I18n.format("general.label.private");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		fontRenderer.drawString(tEntity.getName(), 6, 5, 4210752);

		fontRenderer.drawString(I18n.format("container.inventory"), 6, 63, 4210752);

		// display rf sold
		String formattedkfe = formatter.format(tEntity.kfeSold);
		int feLength = fontRenderer.getStringWidth(formattedkfe + " kFE");
		String overage = (tEntity.kfeSold == Integer.MAX_VALUE ? "+" : "");
		fontRenderer.drawString(formattedkfe + " kFE", 130 - feLength, 26, 4210752);

		// display coin balance
		String formattedBalance = formatter.format(tEntity.coinSum);
		int balLength = fontRenderer.getStringWidth(formattedBalance);
		fontRenderer.drawString(formattedBalance, 131 - balLength, 48, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		tEntity.sendButtonMessage(button.id, isShiftKeyDown());
	}
}