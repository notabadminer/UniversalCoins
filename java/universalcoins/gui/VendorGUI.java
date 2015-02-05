package universalcoins.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import universalcoins.inventory.ContainerVendor;
import universalcoins.tile.TileVendor;

public class VendorGUI extends GuiContainer{
	private TileVendor tileEntity;
	private GuiTextField itemPriceField;
	private GuiButton modeButton, updateButton, setButton;
	private GuiCoinButton retrCoinButton, retrSStackButton, retrLStackButton, retrSBagButton, retrLBagButton;
	public static final int idModeButton = 0;
	public static final int idUpdateButton = 1;
	public static final int idSetButton = 2;
	public static final int idCoinButton = 3;
	private static final int idSStackButton = 4;
	private static final int idLStackButton = 5;
	public static final int idSBagButton = 6;
	public static final int idLBagButton = 7;
	private boolean textActive = false;
	private boolean shiftPressed = false;

	public VendorGUI(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		super(new ContainerVendor(inventoryPlayer, tEntity));
		tileEntity = tEntity;
		
		xSize = 176;
		ySize = 200;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		modeButton = new GuiSlimButton(idModeButton, 8 + (width - xSize) / 2, 35 + (height - ySize) / 2, 62, 12, 
				StatCollector.translateToLocal("vending.gui.button.mode.sell"));
		updateButton = new GuiSlimButton(idUpdateButton, 79 + (width - xSize) / 2, 35 + (height - ySize) / 2, 44, 12, 
				StatCollector.translateToLocal("general.button.edit"));
		setButton = new GuiSlimButton(idSetButton, 124 + (width - xSize) / 2, 35 + (height - ySize) / 2, 44, 12, 
				StatCollector.translateToLocal("general.button.save"));
		retrCoinButton = new GuiCoinButton(idCoinButton, 56 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 0);
		retrSStackButton = new GuiCoinButton(idSStackButton, 74 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 1);
		retrLStackButton = new GuiCoinButton(idLStackButton, 92 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 2);
		retrSBagButton = new GuiCoinButton(idSBagButton, 110 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 3);
		retrLBagButton = new GuiCoinButton(idLBagButton, 128 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 4);
		buttonList.clear();
		buttonList.add(modeButton);
		buttonList.add(updateButton);
		buttonList.add(setButton);
		buttonList.add(retrCoinButton);
		buttonList.add(retrSStackButton);
		buttonList.add(retrLStackButton);
		buttonList.add(retrSBagButton);
		buttonList.add(retrLBagButton);
		
		itemPriceField = new GuiTextField(0, this.fontRendererObj, 82, 21, 86, 15);
		itemPriceField.setFocused(false);
		itemPriceField.setMaxStringLength(10);
		itemPriceField.setEnableBackgroundDrawing(false);
		//itemPriceField.setTextColor(4210752);
	}

	
	protected void keyTyped(char c, int i) {
		if (itemPriceField.isFocused()) {
			itemPriceField.textboxKeyTyped(c, i);
		} else
			try {
				super.keyTyped(c, i);
			} catch (IOException e) {
				// do nothing
			}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		retrCoinButton.enabled = tileEntity.coinButtonActive;
		retrSStackButton.enabled = tileEntity.isSStackButtonActive;
		retrLStackButton.enabled = tileEntity.isLStackButtonActive;
		retrSBagButton.enabled = tileEntity.isSBagButtonActive;
		retrLBagButton.enabled = tileEntity.isLBagButtonActive;
		
		modeButton.displayString = (tileEntity.sellMode ? StatCollector.translateToLocal("vending.gui.button.mode.sell") :
			StatCollector.translateToLocal("vending.gui.button.mode.buy"));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tileEntity.getInventoryName(), 6, 5, 4210752);
		String priceInLocal = StatCollector.translateToLocal("vending.gui.price");
		int stringWidth = fontRendererObj.getStringWidth(priceInLocal);
		fontRendererObj.drawString(priceInLocal, 78 - stringWidth, 22, 4210752);
		//draw itemprice
		if (!textActive) {
		String iSum = String.valueOf(tileEntity.itemPrice);
		stringWidth = fontRendererObj.getStringWidth(iSum);
		fontRendererObj.drawString(iSum, 82, 22, 4210752);
		} else itemPriceField.drawTextBox();
		//draw coinsum
		String cSum = String.valueOf(tileEntity.coinSum);
		stringWidth = fontRendererObj.getStringWidth(cSum);
		fontRendererObj.drawString(cSum, 145 - stringWidth, 60, 4210752);		
	}
	
	protected void actionPerformed(GuiButton button) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			shiftPressed = true;
		}
		else {
			shiftPressed = false;
		}		
		if (button.id == idUpdateButton) {
			itemPriceField.setText(String.valueOf(tileEntity.itemPrice));
			textActive = true;
			itemPriceField.setFocused(true);
		} else if (button.id == idSetButton) {
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
		tileEntity.sendButtonMessage(button.id, shiftPressed);
	}
}
