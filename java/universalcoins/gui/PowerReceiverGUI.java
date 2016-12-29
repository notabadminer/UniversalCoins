package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.inventory.ContainerPowerReceiver;
import universalcoins.tile.TilePowerReceiver;
import universalcoins.util.UniversalPower;

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

	@Override
	public void initGui() {
		super.initGui();
		coinButton = new GuiSlimButton(idCoinButton, 123 + (width - xSize) / 2, 89 + (height - ySize) / 2, 45, 12,
				StatCollector.translateToLocal("general.button.coin"));
		accessModeButton = new GuiSlimButton(idAccessModeButton, 122 + (width - xSize) / 2, 4 + (height - ySize) / 2,
				50, 12, StatCollector.translateToLocal("general.label.public"));
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
			accessModeButton.displayString = StatCollector.translateToLocal("general.label.public");
		} else {
			accessModeButton.displayString = StatCollector.translateToLocal("general.label.private");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		fontRendererObj.drawString(tEntity.getInventoryName(), 6, 5, 4210752);

		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 6, 92, 4210752);

		// display world rf level
		String formattedwrf = formatter.format(tEntity.wrfLevel);
		int wrfLength = fontRendererObj.getStringWidth(formattedwrf + " kRF");
		fontRendererObj.drawString(formattedwrf + " kRF", 162 - wrfLength, 23, 4210752);

		// display rf level
		fontRendererObj.drawString("Stored", 16, 41, 4210752);
		String formattedrf = formatter.format(tEntity.rfLevel);
		int rfLength = fontRendererObj.getStringWidth(formattedrf + " RF");
		fontRendererObj.drawString(formattedrf + " RF", 142 - rfLength, 41, 4210752);

		// display rf output
		fontRendererObj.drawString("Output", 18, 58, 4210752);
		String formattedrfOutput = formatter.format(tEntity.rfOutput);
		int rfOutputLength = fontRendererObj.getStringWidth(formattedrfOutput + " RF/t");
		fontRendererObj.drawString(formattedrfOutput + " RF/t", 142 - rfOutputLength, 59, 4210752);

		// display coin balance
		String formattedBalance = formatter.format(tEntity.coinSum);
		int balLength = fontRendererObj.getStringWidth(formattedBalance);
		fontRendererObj.drawString(formattedBalance, 142 - balLength, 76, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		tEntity.sendPacket(button.id, isShiftKeyDown());
	}
}