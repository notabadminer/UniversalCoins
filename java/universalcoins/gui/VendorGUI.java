package universalcoins.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import universalcoins.container.ContainerVendor;
import universalcoins.tileentity.TileVendor;

public class VendorGUI extends GuiContainer {
	private TileVendor tileEntity;
	private GuiTextField itemPriceField;
	private GuiButton modeButton, updateButton, setButton, tColorMinButton, tColorPlusButton;
	private GuiCoinButton retrIronCoinBtn, retrGoldCoinBtn, retrEmeraldCoinBtn, retrDiamondCoinBtn, retrObsidianCoinBtn;
	public static final int idModeButton = 0;
	public static final int idUpdateButton = 1;
	public static final int idSetButton = 2;
	public static final int idIronCoinBtn = 3;
	private static final int idGoldCoinBtn = 4;
	private static final int idEmeraldCoinBtn = 5;
	public static final int idDiamondCoinBtn = 6;
	public static final int idObsidianCoinBtn = 7;
	public static final int idtcmButton = 8;
	public static final int idtcpButton = 9;
	private boolean textActive = false;
	private int x;
	private int y;

	public VendorGUI(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		super(new ContainerVendor(inventoryPlayer, tEntity));
		tileEntity = tEntity;

		xSize = 176;
		ySize = 200;

	}

	@Override
	public void initGui() {
		super.initGui();
		x = (width - xSize) / 2;
		y = (height - ySize) / 2;
		modeButton = new GuiSlimButton(idModeButton, 8 + x, 35 + y, 62, 12,
				I18n.format("vending.gui.button.mode.sell"));
		updateButton = new GuiSlimButton(idUpdateButton, 79 + x, 35 + y, 44, 12,
				I18n.format("general.button.edit"));
		setButton = new GuiSlimButton(idSetButton, 124 + x, 35 + y, 44, 12,
				I18n.format("general.button.save"));
		retrIronCoinBtn = new GuiCoinButton(idIronCoinBtn, 56 + x, 74 + y, 18, 18, "", 0);
		retrGoldCoinBtn = new GuiCoinButton(idGoldCoinBtn, 74 + x, 74 + y, 18, 18, "", 1);
		retrEmeraldCoinBtn = new GuiCoinButton(idEmeraldCoinBtn, 92 + x, 74 + y, 18, 18, "", 2);
		retrDiamondCoinBtn = new GuiCoinButton(idDiamondCoinBtn, 110 + x, 74 + y, 18, 18, "", 3);
		retrObsidianCoinBtn = new GuiCoinButton(idObsidianCoinBtn, 128 + x, 74 + y, 18, 18, "", 4);
		tColorMinButton = new GuiSlimButton(idtcmButton, 7 + x, 78 + y, 12, 12, "-");
		tColorPlusButton = new GuiSlimButton(idtcpButton, 33 + x, 78 + y, 12, 12, "+");

		buttonList.clear();
		buttonList.add(modeButton);
		buttonList.add(updateButton);
		buttonList.add(setButton);
		buttonList.add(retrIronCoinBtn);
		buttonList.add(retrGoldCoinBtn);
		buttonList.add(retrEmeraldCoinBtn);
		buttonList.add(retrDiamondCoinBtn);
		buttonList.add(retrObsidianCoinBtn);
		buttonList.add(tColorMinButton);
		buttonList.add(tColorPlusButton);

		itemPriceField = new GuiTextField(0, this.fontRendererObj, 82, 21, 86, 15);
		itemPriceField.setFocused(false);
		itemPriceField.setMaxStringLength(10);
		itemPriceField.setEnableBackgroundDrawing(false);
	}

	protected void keyTyped(char c, int i) {
		if (itemPriceField.isFocused()) {
			if (i == 14 || (i > 1 && i < 12)) {
				itemPriceField.textboxKeyTyped(c, i);
			}
		} else
			try {
				super.keyTyped(c, i);
			} catch (IOException e) {
			}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		retrIronCoinBtn.enabled = tileEntity.ironCoinBtnActive;
		retrGoldCoinBtn.enabled = tileEntity.goldCoinBtnActive;
		retrEmeraldCoinBtn.enabled = tileEntity.emeraldCoinBtnActive;
		retrDiamondCoinBtn.enabled = tileEntity.diamondCoinBtnActive;
		retrObsidianCoinBtn.enabled = tileEntity.obsidianCoinBtnActive;

		modeButton.displayString = (tileEntity.sellMode ? I18n.format("general.button.sell")
				: I18n.format("general.button.buy"));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tileEntity.getName(), 6, 5, 4210752);
		String priceInLocal = I18n.format("general.label.price");
		int stringWidth = fontRendererObj.getStringWidth(priceInLocal);
		fontRendererObj.drawString(priceInLocal, 78 - stringWidth, 22, 4210752);
		// draw itemprice
		if (!textActive) {
			String iSum = String.valueOf(tileEntity.itemPrice);
			stringWidth = fontRendererObj.getStringWidth(iSum);
			fontRendererObj.drawString(iSum, 82, 22, 4210752);
		} else
			itemPriceField.drawTextBox();
		// draw coinsum
		String cSum = String.valueOf(tileEntity.coinSum);
		stringWidth = fontRendererObj.getStringWidth(cSum);
		fontRendererObj.drawString(cSum, 145 - stringWidth, 60, 4210752);
		// draw sign text color
		fontRendererObj.drawString(Integer.toHexString(tileEntity.textColor), 23, 81, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		if (button.id == idUpdateButton) {
			itemPriceField.setText(String.valueOf(tileEntity.itemPrice));
			textActive = true;
			itemPriceField.setFocused(true);
		}
		if (button.id == idSetButton) {
			String price = itemPriceField.getText();

			try {
				tileEntity.itemPrice = Integer.parseInt(price);
			} catch (NumberFormatException ex) {
				// iPrice is a non-numeric string, do nothing
			} catch (Throwable ex2) {
				// fail silently?
			}

			textActive = false;
			itemPriceField.setFocused(false);
			tileEntity.sendServerUpdateMessage();
		}
		tileEntity.sendButtonMessage(button.id, isShiftKeyDown());
	}
}
