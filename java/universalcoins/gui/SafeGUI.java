package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.inventory.ContainerSafe;
import universalcoins.tile.TileSafe;

public class SafeGUI extends GuiContainer{
	private TileSafe tEntity;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###");

	
	public SafeGUI(InventoryPlayer inventoryPlayer, TileSafe tileEntity) {
		super(new ContainerSafe(inventoryPlayer, tileEntity));
		tEntity = tileEntity;
		
		xSize = 176;
		ySize = 152;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
			final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/safe.png");
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			int x = (width - xSize) / 2;
			int y = (height - ySize) / 2;
			this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		fontRendererObj.drawString(tEntity.getInventoryName(), 6, 5, 4210752);

		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 6, 58, 4210752);
		
		//display player account balance
		String formattedBalance = formatter.format(tEntity.accountBalance);
		int balLength = fontRendererObj.getStringWidth(formattedBalance);
		fontRendererObj.drawString(formattedBalance, 131 - balLength, 35, 4210752);
	}
}