package universalcoins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import universalcoins.container.ContainerVendorSell;
import universalcoins.tileentity.TileVendor;

public class VendorSellGUI extends GuiContainer {
	private TileVendor tileEntity;
	private GuiButton buyButton;
	private GuiCoinButton retrIronCoinBtn, retrGoldCoinBtn, retrEmeraldCoinBtn, retrDiamondCoinBtn, retrObsidianCoinBtn;
	public static final int idBuyButton = 11;
	public static final int idIronCoinBtn = 12;
	private static final int idGoldCoinBtn = 13;
	private static final int idEmeraldCoinBtn = 14;
	public static final int idDiamondCoinBtn = 15;
	public static final int idObsidianCoinBtn = 16;

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
				I18n.translateToLocal("general.button.buy"));
		retrIronCoinBtn = new GuiCoinButton(idIronCoinBtn, 56 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 0);
		retrGoldCoinBtn = new GuiCoinButton(idGoldCoinBtn, 74 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18,
				18, "", 1);
		retrEmeraldCoinBtn = new GuiCoinButton(idEmeraldCoinBtn, 92 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18,
				18, "", 2);
		retrDiamondCoinBtn = new GuiCoinButton(idDiamondCoinBtn, 110 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 3);
		retrObsidianCoinBtn = new GuiCoinButton(idObsidianCoinBtn, 128 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 4);
		buttonList.clear();
		buttonList.add(buyButton);
		buttonList.add(retrIronCoinBtn);
		buttonList.add(retrGoldCoinBtn);
		buttonList.add(retrEmeraldCoinBtn);
		buttonList.add(retrDiamondCoinBtn);
		buttonList.add(retrObsidianCoinBtn);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor-sell.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		buyButton.enabled = tileEntity.buyButtonActive;
		retrIronCoinBtn.enabled = tileEntity.uironCoinBtnActive;
		retrGoldCoinBtn.enabled = tileEntity.ugoldCoinBtnActive;
		retrEmeraldCoinBtn.enabled = tileEntity.uemeraldCoinBtnActive;
		retrDiamondCoinBtn.enabled = tileEntity.udiamondCoinBtnActive;
		retrObsidianCoinBtn.enabled = tileEntity.uobsidianCoinBtnActive;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tileEntity.getName(), 6, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(I18n.translateToLocal("container.inventory"), 8, 98, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.itemPrice), 59, 29, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.userCoinSum), 59, 62, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		tileEntity.sendButtonMessage(button.id, isShiftKeyDown());
	}
}
