package universalcoins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import universalcoins.inventory.ContainerVendorSell;
import universalcoins.tile.TileVendor;

public class VendorSellGUI extends GuiContainer{
	private TileVendor tileEntity;
	private GuiButton buyButton;
	private GuiCoinButton retrCoinButton, retrSStackButton, retrLStackButton, retrSBagButton, retrLBagButton;
	public static final int idBuyButton = 9;
	public static final int idCoinButton = 10;
	private static final int idSStackButton = 11;
	private static final int idLStackButton = 12;
	public static final int idSBagButton = 13;
	public static final int idLBagButton = 14;
	
	boolean shiftPressed = false;

	public VendorSellGUI(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		super(new ContainerVendorSell(inventoryPlayer, tEntity));
		tileEntity = tEntity;
		
		xSize = 176;
		ySize = 189;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		buyButton = new GuiSlimButton(idBuyButton, 126 + (width - xSize) / 2, 42 + (height - ySize) / 2, 42, 12, 
				StatCollector.translateToLocal("general.button.buy"));
		retrCoinButton = new GuiCoinButton(idCoinButton, 56 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 0);
		retrSStackButton = new GuiCoinButton(idSStackButton, 74 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 1);
		retrLStackButton = new GuiCoinButton(idLStackButton, 92 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 2);
		retrSBagButton = new GuiCoinButton(idSBagButton, 110 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 3);
		retrLBagButton = new GuiCoinButton(idLBagButton, 128 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18, "", 4);
		buttonList.clear();
		buttonList.add(buyButton);
		buttonList.add(retrCoinButton);
		buttonList.add(retrSStackButton);
		buttonList.add(retrLStackButton);
		buttonList.add(retrSBagButton);
		buttonList.add(retrLBagButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor-sell.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		buyButton.enabled = tileEntity.buyButtonActive;
		retrCoinButton.enabled = tileEntity.uCoinButtonActive;
		retrSStackButton.enabled = tileEntity.uSStackButtonActive;
		retrLStackButton.enabled = tileEntity.uLStackButtonActive;
		retrSBagButton.enabled = tileEntity.uSBagButtonActive;
		retrLBagButton.enabled = tileEntity.uLBagButtonActive;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(StatCollector.translateToLocal("tile.blockVendor.name"), 8, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal(
				"container.inventory"), 8, 98, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.itemPrice), 59, 29, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.userCoinSum), 59, 62, 4210752);
	}
	
	protected void actionPerformed(GuiButton button) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			shiftPressed = true;
		}
		else {
			shiftPressed = false;
		}
		tileEntity.sendButtonMessage(button.id, shiftPressed);
	}
}
