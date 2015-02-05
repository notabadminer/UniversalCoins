package universalcoins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import universalcoins.inventory.ContainerVendorBuy;
import universalcoins.tile.TileVendor;

public class VendorBuyGUI extends GuiContainer{
	private TileVendor tileEntity;
	private GuiButton sellButton;
	private GuiCoinButton retrCoinButton, retrSStackButton, retrLStackButton, retrSBagButton, retrLBagButton;
	public static final int idSellButton = 8;
	public static final int idCoinButton = 10;
	private static final int idSStackButton = 11;
	private static final int idLStackButton = 12;
	public static final int idSBagButton = 13;
	public static final int idLBagButton = 14;
	
	boolean shiftPressed = false;

	public VendorBuyGUI(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		super(new ContainerVendorBuy(inventoryPlayer, tEntity));
		tileEntity = tEntity;
		
		xSize = 176;
		ySize = 198;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		sellButton = new GuiSlimButton(idSellButton, 126 + (width - xSize) / 2, 46 + (height - ySize) / 2, 42, 12, 
				StatCollector.translateToLocal("general.button.sell"));
		retrCoinButton = new GuiCoinButton(idCoinButton, 60 + (width - xSize) / 2, 82 + (height - ySize) / 2, 18, 18, "", 0);
		retrSStackButton = new GuiCoinButton(idSStackButton, 78 + (width - xSize) / 2, 82 + (height - ySize) / 2, 18, 18, "", 1);
		retrLStackButton = new GuiCoinButton(idLStackButton, 96 + (width - xSize) / 2, 82 + (height - ySize) / 2, 18, 18, "", 2);
		retrSBagButton = new GuiCoinButton(idSBagButton, 114 + (width - xSize) / 2, 82 + (height - ySize) / 2, 18, 18, "", 3);
		retrLBagButton = new GuiCoinButton(idLBagButton, 132 + (width - xSize) / 2, 82 + (height - ySize) / 2, 18, 18, "", 4);
		buttonList.clear();
		buttonList.add(sellButton);
		buttonList.add(retrCoinButton);
		buttonList.add(retrSStackButton);
		buttonList.add(retrLStackButton);
		buttonList.add(retrSBagButton);
		buttonList.add(retrLBagButton);
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor-buy.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		sellButton.enabled = tileEntity.sellButtonActive;
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
		fontRendererObj.drawString(tileEntity.getInventoryName(), 6, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 106, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.itemPrice), 48, 29, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.userCoinSum), 63, 69, 4210752);
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
