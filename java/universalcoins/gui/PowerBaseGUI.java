package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.container.ContainerPowerBase;
import universalcoins.tile.TilePowerBase;

public class PowerBaseGUI extends GuiContainer {
	private TilePowerBase tEntity;
	private GuiButton coinButton;
	public static final int idCoinButton = 0;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###");

	public PowerBaseGUI(InventoryPlayer inventoryPlayer, TilePowerBase tileEntity) {
		super(new ContainerPowerBase(inventoryPlayer, tileEntity));
		tEntity = tileEntity;

		xSize = 176;
		ySize = 152;
	}

	@Override
	public void initGui() {
		super.initGui();
		coinButton = new GuiSlimButton(idCoinButton, 123 + (width - xSize) / 2, 55 + (height - ySize) / 2, 46, 12,
				StatCollector.translateToLocal("general.button.coin"));
		buttonList.clear();
		buttonList.add(coinButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/powerBase.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		fontRendererObj.drawString(tEntity.getName(), 6, 5, 4210752);

		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 6, 58, 4210752);

		// display rf sold
		String formattedkrf = formatter.format(tEntity.krfSold);
		int rfLength = fontRendererObj.getStringWidth(formattedkrf + " kRF");
		fontRendererObj.drawString(formattedkrf + " kRF", 131 - rfLength, 21, 4210752);

		// display coin balance
		String formattedBalance = formatter.format(tEntity.coinSum);
		int balLength = fontRendererObj.getStringWidth(formattedBalance);
		fontRendererObj.drawString(formattedBalance, 131 - balLength, 43, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		tEntity.sendPacket(button.id, isShiftKeyDown());
	}
}