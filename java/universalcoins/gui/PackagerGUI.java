package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalcoins.container.ContainerPackager;
import universalcoins.tile.TilePackager;

public class PackagerGUI extends GuiContainer {
	
	private TilePackager tEntity;
	private GuiButton smallButton, medButton, largeButton, packageButton, coinButton;
	public static final int idPackButton = 0;
	public static final int idCoinButton = 1;
	public static final int idSmallButton = 2;
	public static final int idMedButton = 3;
	public static final int idLargeButton = 4;
	private int[] defaultCoord = {8, 26};
	private int hideCoord = Integer.MAX_VALUE;
	private boolean buttonHover = false;
	
	public PackagerGUI(InventoryPlayer inventoryPlayer, TilePackager tileEntity) {
		super(new ContainerPackager(inventoryPlayer, tileEntity));
		tEntity = tileEntity;
		
		xSize = 176;
		ySize = 201;
	}

	@Override
	public void initGui() {
		super.initGui();
		smallButton = new GuiSlimButton(idSmallButton, 87 + (width - xSize) / 2, 20 + (height - ySize) / 2, 50, 12, StatCollector.translateToLocal("packager.button.small"));
		medButton = new GuiSlimButton(idMedButton, 87 + (width - xSize) / 2, 32 + (height - ySize) / 2, 50, 12, StatCollector.translateToLocal("packager.button.medium"));
		largeButton = new GuiSlimButton(idLargeButton, 87 + (width - xSize) / 2, 44 + (height - ySize) / 2, 50, 12, StatCollector.translateToLocal("packager.button.large"));

		packageButton = new GuiSlimButton(idPackButton, 123 + (width - xSize) / 2, 58 + (height - ySize) / 2, 46, 12, StatCollector.translateToLocal("general.button.buy"));
		coinButton = new GuiSlimButton(idCoinButton, 123 + (width - xSize) / 2, 91 + (height - ySize) / 2, 46, 12, StatCollector.translateToLocal("general.button.coin"));
		buttonList.clear();
		buttonList.add(smallButton);
		buttonList.add(medButton);
		buttonList.add(largeButton);
		buttonList.add(packageButton);
		buttonList.add(coinButton);

		//update slots to current mode
		updateSlots(tEntity.packageSize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tEntity.getName(), 8, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(
				StatCollector.translateToLocal("container.inventory"), 8,
				96, 4210752);
		
		//draw package price if button hover true
		buttonHover = false;
		if (smallButton.isMouseOver()) {
			String cost = "Cost: " + tEntity.packageCost[0];
			int stringWidth = fontRendererObj.getStringWidth(cost);
			fontRendererObj.drawString(cost, 144 - stringWidth, 78, 4210752);
			buttonHover = true;
		}
		if (medButton.isMouseOver()) {
			String cost = "Cost: " + tEntity.packageCost[1];
			int stringWidth = fontRendererObj.getStringWidth(cost);
			fontRendererObj.drawString(cost, 144 - stringWidth, 78, 4210752);
			buttonHover = true;
		}
		if (largeButton.isMouseOver()) {
			String cost = "Cost: " + tEntity.packageCost[2];
			int stringWidth = fontRendererObj.getStringWidth(cost);
			fontRendererObj.drawString(cost, 144 - stringWidth, 78, 4210752);
			buttonHover = true;
		}
		if (!buttonHover) {
			//draw coin sum right aligned
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			String coinSumString = String.valueOf(formatter.format(tEntity.coinSum));
			int stringWidth = fontRendererObj.getStringWidth(coinSumString);
			fontRendererObj.drawString(coinSumString, 144 - stringWidth, 78, 4210752);
		}
		
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		//disable if player has no money
		packageButton.enabled = tEntity.coinSum >= tEntity.packageCost[tEntity.packageSize] || tEntity.cardAvailable;
		coinButton.enabled = tEntity.coinSum > 0;

		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/packager.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		//draw highlight over currently selected package mode
		int yHighlight[] = {24, 36, 48};
		//check tileEntity for mode and hightlight proper mode
		this.drawTexturedModalRect(x + 80, y + yHighlight[tEntity.packageSize], 176, 0, 6, 6);
		
		//draw background for slots	
		if (tEntity.packageSize == 1) {
			this.drawTexturedModalRect(x + 25, y + 21 , 176, 6, 18, 36);
		}
		if (tEntity.packageSize == 2) {
			this.drawTexturedModalRect(x + 7, y + 21 , 176, 6, 36, 36);
		}
	}
	
	private void updateSlots(int mode) {
		if (mode == 0) {
			//hide slot 0-3
			Slot slot0 = super.inventorySlots.getSlot(0);
			slot0.xDisplayPosition = hideCoord;
			Slot slot1 = super.inventorySlots.getSlot(1);
			slot1.xDisplayPosition = hideCoord;
			Slot slot2 = super.inventorySlots.getSlot(2);
			slot2.xDisplayPosition = hideCoord;
			Slot slot3 = super.inventorySlots.getSlot(3);
			slot3.xDisplayPosition = hideCoord;
		}
		if (mode == 1) {
			//hide slot 0-1 , show 2-3
			Slot slot0 = super.inventorySlots.getSlot(0);
			slot0.xDisplayPosition = hideCoord;
			Slot slot1 = super.inventorySlots.getSlot(1);
			slot1.xDisplayPosition = hideCoord;
			Slot slot2 = super.inventorySlots.getSlot(2);
			slot2.xDisplayPosition = defaultCoord[1];
			Slot slot3 = super.inventorySlots.getSlot(3);
			slot3.xDisplayPosition = defaultCoord[1];
		}
		if (mode == 2) {
			//show slot 0-3
			Slot slot0 = super.inventorySlots.getSlot(0);
			slot0.xDisplayPosition = defaultCoord[0];
			Slot slot1 = super.inventorySlots.getSlot(1);
			slot1.xDisplayPosition = defaultCoord[0];
			Slot slot2 = super.inventorySlots.getSlot(2);
			slot2.xDisplayPosition = defaultCoord[1];
			Slot slot3 = super.inventorySlots.getSlot(3);
			slot3.xDisplayPosition = defaultCoord[1];
		}
	}
	
	protected void actionPerformed(GuiButton button) {
		if (button.id == 2) {
			updateSlots(0);
		}
		if (button.id == 3) {
			updateSlots(1);
		}
		if (button.id == 4) {
			updateSlots(2);
		}

		tEntity.sendPacket(button.id, isShiftKeyDown());
	}
}
