package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalcoins.container.ContainerBandit;
import universalcoins.tile.TileBandit;

public class BanditGUI extends GuiContainer {
	
	private TileBandit tEntity;
	private GuiButton spinButton, coinButton;
	public static final int idPullButton = 0;
	public static final int idCoinButton = 1;
	private int[] counter = {219, 219, 219, 219, 219};
	private boolean[] reelActive = {true, true, true, true, true};
	private boolean resultCheck = false;
	private int[] reelDrawPos = {13, 35, 57, 79, 101};
	
	public BanditGUI(InventoryPlayer inventoryPlayer, TileBandit tileEntity) {
		super(new ContainerBandit(inventoryPlayer, tileEntity));
		tEntity = tileEntity;
		
		xSize = 176;
		ySize = 201;
	}

	@Override
	public void initGui() {
		super.initGui();
		spinButton = new GuiSlimButton(idPullButton, 130 + (width - xSize) / 2, 30 + (height - ySize) / 2, 32, 12, StatCollector.translateToLocal("general.button.spin"));
		coinButton = new GuiSlimButton(idCoinButton, 130 + (width - xSize) / 2, 45 + (height - ySize) / 2, 32, 12, StatCollector.translateToLocal("general.button.coin"));
		buttonList.clear();
		buttonList.add(spinButton);
		buttonList.add(coinButton);
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
		//draw coin sum right aligned
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		String coinSumString = String.valueOf(formatter.format(tEntity.coinSum));
		int stringWidth = fontRendererObj.getStringWidth(coinSumString);
		fontRendererObj.drawString(coinSumString, 140 - stringWidth, 78, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		//disable if player has no money
		spinButton.enabled = tEntity.coinSum >= tEntity.spinFee || tEntity.cardAvailable;
		coinButton.enabled = tEntity.coinSum > 0;

		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/bandit.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		//draw reels
		for (int i = 0; i < reelDrawPos.length; i++) {
			this.drawTexturedModalRect(x + reelDrawPos[i], y + 26, 176, 0 + counter[i], 21, 36);
			if (reelActive[i]) {
				if (counter[i] == tEntity.reelPos[i]) { 
					reelActive[i] = false;
					continue;
				}
				counter[i]-=2;
				if (counter[i] < 0) counter[i] = 216;
			}
		}
		
		//do we need to check the results?
		if (!reelActive[0] && !reelActive[1] && !reelActive[2] && !reelActive[3] && resultCheck) {
			tEntity.sendPacket(2, isShiftKeyDown());
			tEntity.checkMatch();
			resultCheck = false;
		}
	}
	
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			resultCheck = true;
			for (int i = 0; i < reelActive.length; i++) {
				reelActive[i] = true;
				counter[i]-=2;
				tEntity.playSound(0);
			}
		}
		tEntity.sendPacket(button.id, isShiftKeyDown());
	}
}
