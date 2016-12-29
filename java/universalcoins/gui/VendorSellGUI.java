package universalcoins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.inventory.ContainerVendorSell;
import universalcoins.tile.TileVendor;

public class VendorSellGUI extends GuiContainer {
	private TileVendor tileEntity;
	private GuiButton buyButton;
	private GuiCoinButton retrIronCoinButton, retrGoldCoinButton, retrEmeraldCoinButton, retrDiamondCoinButton, retrObsidianCoinButton;
	public static final int idBuyButton = 9;
	public static final int idIronCoinButton = 10;
	private static final int idGoldCoinButton = 11;
	private static final int idEmeraldCoinButton = 12;
	public static final int idDiamondCoinButton = 13;
	public static final int idObsidianCoinButton = 14;

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
		retrIronCoinButton = new GuiCoinButton(idIronCoinButton, 56 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 0);
		retrGoldCoinButton = new GuiCoinButton(idGoldCoinButton, 74 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18,
				18, "", 1);
		retrEmeraldCoinButton = new GuiCoinButton(idEmeraldCoinButton, 92 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18,
				18, "", 2);
		retrDiamondCoinButton = new GuiCoinButton(idDiamondCoinButton, 110 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 3);
		retrObsidianCoinButton = new GuiCoinButton(idObsidianCoinButton, 128 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 4);
		buttonList.clear();
		buttonList.add(buyButton);
		buttonList.add(retrIronCoinButton);
		buttonList.add(retrGoldCoinButton);
		buttonList.add(retrEmeraldCoinButton);
		buttonList.add(retrDiamondCoinButton);
		buttonList.add(retrObsidianCoinButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor_sell.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		buyButton.enabled = tileEntity.buyButtonActive;
		retrIronCoinButton.enabled = tileEntity.uIronCoinBtnActive;
		retrGoldCoinButton.enabled = tileEntity.uGoldCoinBtnActive;
		retrEmeraldCoinButton.enabled = tileEntity.uEmeraldCoinBtnActive;
		retrDiamondCoinButton.enabled = tileEntity.uDiamondCoinBtnActive;
		retrObsidianCoinButton.enabled = tileEntity.uObsidianCoinBtnActive;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tileEntity.getInventoryName(), 6, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 98, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.itemPrice), 59, 29, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.userCoinSum), 59, 62, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		tileEntity.sendButtonMessage(button.id, isShiftKeyDown());
	}
}
