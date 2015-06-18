package universalcoins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalcoins.UniversalCoins;
import universalcoins.container.ContainerTradeStation;
import universalcoins.tile.TileTradeStation;

public class TradeStationGUI extends GuiContainer {

	private TileTradeStation tileEntity;
	private GuiButton buyButton, sellButton, coinModeButton, autoModeButton;
	private GuiCoinButton retrCoinButton, retrSStackButton, retrLStackButton, retrSBagButton, retrLBagButton;
	public static final int idBuyButton = 0;
	public static final int idSellButton = 1;
	public static final int idCoinButton = 2;
	private static final int idSStackButton = 3;
	private static final int idLStackButton = 4;
	public static final int idSBagButton = 5;
	public static final int idLBagButton = 6;
	public static final int idCoinModeButton = 7;
	public static final int idAutoModeButton = 8;

	public String[] autoLabels = { StatCollector.translateToLocal("tradestation.gui.autolabel.off"),
			StatCollector.translateToLocal("tradestation.gui.autolabel.buy"),
			StatCollector.translateToLocal("tradestation.gui.autolabel.sell") };

	public TradeStationGUI(InventoryPlayer inventoryPlayer, TileTradeStation parTileEntity) {
		super(new ContainerTradeStation(inventoryPlayer, parTileEntity));
		tileEntity = parTileEntity;
		xSize = 184;
		ySize = 200;
	}

	@Override
	public void initGui() {
		super.initGui();
		buyButton = new GuiSlimButton(idBuyButton, 50 + (width - xSize) / 2, 21 + (height - ySize) / 2, 48, 12,
				StatCollector.translateToLocal("general.button.buy"));
		sellButton = new GuiSlimButton(idSellButton, 50 + (width - xSize) / 2, 38 + (height - ySize) / 2, 48, 12,
				StatCollector.translateToLocal("general.button.sell"));
		retrCoinButton = new GuiCoinButton(idCoinButton, 88 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 0);
		retrSStackButton = new GuiCoinButton(idSStackButton, 106 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18,
				18, "", 1);
		retrLStackButton = new GuiCoinButton(idLStackButton, 124 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18,
				18, "", 2);
		retrSBagButton = new GuiCoinButton(idSBagButton, 142 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 3);
		retrLBagButton = new GuiCoinButton(idLBagButton, 160 + (width - xSize) / 2, 74 + (height - ySize) / 2, 18, 18,
				"", 4);
		coinModeButton = new GuiSlimButton(idCoinModeButton, 110 + (width - xSize) / 2, 98 + (height - ySize) / 2, 46,
				12, StatCollector.translateToLocal("general.button.coin"));
		buttonList.clear();
		if (UniversalCoins.tradeStationBuyEnabled)
			buttonList.add(buyButton);
		buttonList.add(sellButton);
		buttonList.add(retrCoinButton);
		buttonList.add(retrSStackButton);
		buttonList.add(retrLStackButton);
		buttonList.add(retrSBagButton);
		buttonList.add(retrLBagButton);
		buttonList.add(coinModeButton);

		// display only if auto buy/sell enabled?
		if (tileEntity.autoModeButtonActive) {
			autoModeButton = new GuiSlimButton(idAutoModeButton, 6 + (width - xSize) / 2, 84 + (height - ySize) / 2,
					28, 12, StatCollector.translateToLocal("tradestation.gui.button.mode"));
			buttonList.add(autoModeButton);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tileEntity.getName(), 6, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 6, ySize - 96 + 2, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.coinSum), 114, 57, 4210752);
		String priceInLocal = StatCollector.translateToLocal("general.label.price");
		int stringWidth = fontRendererObj.getStringWidth(priceInLocal);
		fontRendererObj.drawString(priceInLocal, 48 - stringWidth, 57, 4210752);
		if (tileEntity.itemPrice > 0) {
			if (sellButton.isMouseOver() || !UniversalCoins.tradeStationBuyEnabled) { // player
																						// hovering
																						// over
																						// sell
																						// button
																						// or
																						// buy
																						// button
																						// disabled
				int sellPrice = (int) (tileEntity.itemPrice * UniversalCoins.itemSellRatio);
				fontRendererObj.drawString(String.valueOf(sellPrice), 48, 57, 4210752);
			} else {
				fontRendererObj.drawString(String.valueOf(tileEntity.itemPrice), 48, 57, 4210752);
			}
		} else {
			fontRendererObj.drawString(StatCollector.translateToLocal("tradestation.gui.warning.noitem"), 48, 57,
					4210752);
		}
		// display only if auto buy/sell enabled
		if (tileEntity.autoModeButtonActive) {
			fontRendererObj
					.drawString(StatCollector.translateToLocal("tradestation.gui.label.autobuy"), 6, 74, 4210752);
			fontRendererObj.drawString(autoLabels[tileEntity.autoMode], 38, 87, 4210752);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {

		buyButton.enabled = tileEntity.buyButtonActive;
		sellButton.enabled = tileEntity.sellButtonActive;
		retrCoinButton.enabled = tileEntity.coinButtonActive;
		retrSStackButton.enabled = tileEntity.isSStackButtonActive;
		retrLStackButton.enabled = tileEntity.isLStackButtonActive;
		retrSBagButton.enabled = tileEntity.isSBagButtonActive;
		retrLBagButton.enabled = tileEntity.isLBagButtonActive;

		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/tradeStation.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		// draw auto mode box if auto buy/sell enabled
		if (tileEntity.autoModeButtonActive) {
			this.drawTexturedModalRect(x + 35, y + 83, 184, 0, 50, 15);
		}

		// draw highlight over currently selected coin type (coinMode)
		int xHighlight[] = { 0, 89, 107, 125, 143, 161 };
		if (tileEntity.coinMode > 0) {
			this.drawTexturedModalRect(x + xHighlight[tileEntity.coinMode], y + 94, 184, 15, 16, 2);
		}
	}

	protected void actionPerformed(GuiButton par1GuiButton) {
		tileEntity.sendPacket(par1GuiButton.id, isShiftKeyDown());
	}
}
