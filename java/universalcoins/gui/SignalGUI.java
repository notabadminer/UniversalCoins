package universalcoins.gui;

import java.text.DecimalFormat;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import universalcoins.container.ContainerSignal;
import universalcoins.tileentity.TileSignal;

public class SignalGUI extends GuiContainer {

	private TileSignal tEntity;
	private GuiButton coinButton, durationMinusButton, durationPlusButton, coinMinusButton, coinPlusButton;
	public static final int idCoinButton = 0;
	public static final int idDurMinusButton = 1;
	public static final int idDurPlusButton = 2;
	public static final int idCoinMinusButton = 3;
	public static final int idCoinPlusButton = 4;

	public SignalGUI(InventoryPlayer inventoryPlayer, TileSignal tileEntity) {
		super(new ContainerSignal(inventoryPlayer, tileEntity));
		tEntity = tileEntity;

		xSize = 176;
		ySize = 201;
	}

	@Override
	public void initGui() {
		super.initGui();
		coinButton = new GuiSlimButton(idCoinButton, 111 + (width - xSize) / 2, 92 + (height - ySize) / 2, 32, 12,
				I18n.format("general.button.coin"));
		durationMinusButton = new GuiSlimButton(idDurMinusButton, 95 + (width - xSize) / 2, 48 + (height - ySize) / 2,
				12, 12, "-");
		durationPlusButton = new GuiSlimButton(idDurPlusButton, 144 + (width - xSize) / 2, 48 + (height - ySize) / 2,
				12, 12, "+");
		coinMinusButton = new GuiSlimButton(idCoinMinusButton, 95 + (width - xSize) / 2, 26 + (height - ySize) / 2, 12,
				12, "-");
		coinPlusButton = new GuiSlimButton(idCoinPlusButton, 144 + (width - xSize) / 2, 26 + (height - ySize) / 2, 12,
				12, "+");
		buttonList.clear();
		buttonList.add(coinButton);
		buttonList.add(durationMinusButton);
		buttonList.add(durationPlusButton);
		buttonList.add(coinMinusButton);
		buttonList.add(coinPlusButton);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");

		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tEntity.getName(), 8, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(I18n.format("container.inventory"), 8, 96, 4210752);
		String feeLabel = I18n.format("signal.label.fee");
		int stringWidth = fontRendererObj.getStringWidth(feeLabel);
		fontRendererObj.drawString(feeLabel, 92 - stringWidth, 28, 4210752);
		// draw fee right aligned
		String fee = String.valueOf(formatter.format(tEntity.fee));
		stringWidth = fontRendererObj.getStringWidth(fee);
		fontRendererObj.drawString(fee, 138 - stringWidth, 28, 4210752);
		String durationLabel = I18n.format("signal.label.duration");
		stringWidth = fontRendererObj.getStringWidth(durationLabel);
		fontRendererObj.drawString(durationLabel, 92 - stringWidth, 50, 4210752);
		// draw signal duration right aligned
		String duration = String.valueOf(formatter.format(tEntity.duration));
		stringWidth = fontRendererObj.getStringWidth(duration);
		fontRendererObj.drawString(duration, 138 - stringWidth, 50, 4210752);
		// draw coin sum right aligned
		String coinSumString = String.valueOf(formatter.format(tEntity.coinSum));
		stringWidth = fontRendererObj.getStringWidth(coinSumString);
		fontRendererObj.drawString(coinSumString, 138 - stringWidth, 78, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		// disable if no coins left
		coinButton.enabled = tEntity.coinSum > 0;

		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/signal.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	protected void actionPerformed(GuiButton button) {
		tEntity.sendPacket(button.id, isShiftKeyDown());
	}
}
