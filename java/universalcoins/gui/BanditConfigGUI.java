package universalcoins.gui;

import java.io.IOException;
import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalcoins.tile.TileBandit;

public class BanditConfigGUI extends GuiScreen {
	private TileBandit tileBandit;
	private GuiButton costEdit, fourMatchEdit, fiveMatchEdit, editButton;
	private int xSize = 175;
	private int ySize = 121;
	private int x = 0;
	private int y = 0;
	private DecimalFormat formatter = new DecimalFormat("#,###,###,###");
	private String payoutPercentage = "0";
	private GuiTextField textField;
	private String catDisplay = "Fee";
	private int spinFee, fourMatch, fiveMatch;
	private int guiMode = 0;

	public BanditConfigGUI(TileBandit tileEntity) {
		this.tileBandit = (TileBandit) tileEntity;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		x = (width - xSize) / 2;
		y = (height - ySize) / 2;
		costEdit = new GuiSlimButton(0, x + 13, y + 52, 12, 12, StatCollector.translateToLocal(""));
		fourMatchEdit = new GuiSlimButton(1, x + 25, y + 52, 12, 12, StatCollector.translateToLocal(""));
		fiveMatchEdit = new GuiSlimButton(2, x + 37, y + 52, 12, 12, StatCollector.translateToLocal(""));
		editButton = new GuiSlimButton(3, x + 87, y + 52, 73, 12, StatCollector.translateToLocal(StatCollector
				.translateToLocal("general.button.edit")));
		this.buttonList.clear();
		buttonList.add(costEdit);
		buttonList.add(fourMatchEdit);
		buttonList.add(fiveMatchEdit);
		buttonList.add(editButton);

		textField = new GuiTextField(0, this.fontRendererObj, x + 88, y + 36, 70, 14);
		textField.setFocused(false);
		textField.setMaxStringLength(10);
		textField.setText(String.valueOf(tileBandit.spinFee));
		textField.setEnableBackgroundDrawing(false);

		spinFee = tileBandit.spinFee;
		fourMatch = tileBandit.fourMatchPayout;
		fiveMatch = tileBandit.fiveMatchPayout;

		updatePayoutPercentage();
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (textField.isFocused() && !isShiftKeyDown()) {
			if (i == 14 || (i > 1 && i < 12)) {
				textField.textboxKeyTyped(c, i);
			}
		}
		try {
			super.keyTyped(c, i);
		} catch (IOException e) {
		}
	}

	public void onGuiClosed() {
		tileBandit.spinFee = spinFee;
		tileBandit.fourMatchPayout = fourMatch;
		tileBandit.fiveMatchPayout = fiveMatch;
		tileBandit.sendServerUpdateMessage();
	}

	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 0) {
				guiMode = 0;
				catDisplay = StatCollector.translateToLocal("bandit.label.fee");
				textField.setText(String.valueOf(spinFee));
			}
			if (button.id == 1) {
				guiMode = 1;
				catDisplay = StatCollector.translateToLocal("bandit.label.four");
				textField.setText(String.valueOf(fourMatch));
			}
			if (button.id == 2) {
				guiMode = 2;
				catDisplay = StatCollector.translateToLocal("bandit.label.five");
				textField.setText(String.valueOf(fiveMatch));
			}
			if (button.id == 3) {
				if (!textField.isFocused()) {
					textField.setFocused(true);
				} else if (!textField.getText().isEmpty()) {
					if (guiMode == 0)
						spinFee = Integer.valueOf(textField.getText());
					if (guiMode == 1)
						fourMatch = Integer.valueOf(textField.getText());
					if (guiMode == 2)
						fiveMatch = Integer.valueOf(textField.getText());
					textField.setFocused(false);
				}
			}
			if (button.id == 4) {
				this.mc.displayGuiScreen((GuiScreen) null);
			}
			updatePayoutPercentage();
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/banditConfig.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		// change button text depending on edit mode
		editButton.displayString = textField.isFocused() ? StatCollector.translateToLocal("general.button.save")
				: StatCollector.translateToLocal("general.button.edit");

		fontRendererObj.drawString(tileBandit.getName(), x + 6, y + 5, 4210752);
		fontRendererObj.drawString(catDisplay, x + 15, y + 40, 4210752);
		if (!textField.getText().isEmpty()) {
			String cost = String.valueOf(formatter.format(Integer.parseInt(textField.getText())));
			int stringWidth = fontRendererObj.getStringWidth(cost);
			fontRendererObj.drawString(cost, x + 156 - stringWidth, y + 40, 4210752);
		}

		String label = StatCollector.translateToLocal("bandit.label.payout");
		int stringWidth = fontRendererObj.getStringWidth(label);
		fontRendererObj.drawString(label, x + 98 - stringWidth, y + 98, 4210752);
		String percent = payoutPercentage + "%";
		stringWidth = fontRendererObj.getStringWidth(percent);
		fontRendererObj.drawString(percent, x + 156 - stringWidth, y + 98, 4210752);

		super.drawScreen(par1, par2, par3);
	}

	private void updatePayoutPercentage() {
		payoutPercentage = String.format("%.2f", ((450 * (double) fourMatch + 10 * (double) fiveMatch)
				/ (100000 * spinFee) * 100));
	}
}