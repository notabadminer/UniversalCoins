package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import universalcoins.container.ContainerPowerReceiver;
import universalcoins.tileentity.TilePowerReceiver;

public class PowerReceiverGUI extends GuiContainer {
	private TilePowerReceiver tEntity;
	private GuiButton coinButton, accessModeButton;
	public static final int idCoinButton = 0;
	public static final int idAccessModeButton = 1;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###");

	public PowerReceiverGUI(InventoryPlayer inventoryPlayer, TilePowerReceiver tileEntity) {
		super(new ContainerPowerReceiver(inventoryPlayer, tileEntity));
		tEntity = tileEntity;

		xSize = 176;
		ySize = 184;
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
		coinButton = new GuiSlimButton(idCoinButton, 123 + (width - xSize) / 2, 89 + (height - ySize) / 2, 46, 12,
				I18n.format("general.button.coin"));
		accessModeButton = new GuiSlimButton(idAccessModeButton, 122 + (width - xSize) / 2, 4 + (height - ySize) / 2,
				50, 12, I18n.format("general.label.public"));
		buttonList.clear();
		buttonList.add(coinButton);
		buttonList.add(accessModeButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/power_receiver.png");
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
		fontRenderer.drawString(I18n.format("container.inventory"), 6, 92, 4210752);

		// display world fe level

		String formattedwfe = formatter.format(tEntity.wfeLevel);
		int wfeLength = fontRenderer.getStringWidth(formattedwfe + " kFE");
		fontRenderer.drawString(formattedwfe + " kFE", 162 - wfeLength, 23, 4210752);

		// display fe level
		fontRenderer.drawString("Stored", 16, 41, 4210752);
		String formattedfe = formatter.format(tEntity.feLevel);
		int feLength = fontRenderer.getStringWidth(formattedfe + " FE");
		fontRenderer.drawString(formattedfe + " FE", 142 - feLength, 41, 4210752);

		// display fe output
		fontRenderer.drawString("Output", 18, 58, 4210752);
		String formattedfeOutput = formatter.format(tEntity.feOutput);
		int feOutputLength = fontRenderer.getStringWidth(formattedfeOutput + " FE/t");
		fontRenderer.drawString(formattedfeOutput + " FE/t", 142 - feOutputLength, 59, 4210752);
		// display coin balance
		String formattedBalance = formatter.format(tEntity.coinSum);
		int balLength = fontRenderer.getStringWidth(formattedBalance);
		fontRenderer.drawString(formattedBalance, 142 - balLength, 76, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		tEntity.sendPacket(button.id, isShiftKeyDown());
	}
}