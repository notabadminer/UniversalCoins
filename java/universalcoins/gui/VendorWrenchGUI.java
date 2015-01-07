package universalcoins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.inventory.ContainerVendorWrench;
import universalcoins.tile.TileVendor;

public class VendorWrenchGUI extends GuiContainer {
	private TileVendor tileEntity;
	private GuiTextField blockOwnerField;
	private GuiButton infiniteButton, editButton, applyButton;
	public static final int idInfiniteButton = 0;
	public static final int idEditButton = 1;
	public static final int idApplyButton = 2;
	
	boolean infinite;


	public VendorWrenchGUI(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		super(new ContainerVendorWrench(inventoryPlayer, tEntity));
		tileEntity = tEntity;
		
		xSize = 166;
		ySize = 80;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		infinite = tileEntity.infiniteSell;
		infiniteButton = new GuiSlimButton(idInfiniteButton, 9 + (width - xSize) / 2, 54 + 
				(height - ySize) / 2, 148, 12, infinite ? StatCollector.translateToLocal("vending.wrench.infiniteon") :
					StatCollector.translateToLocal("vending.wrench.infiniteoff"));
		editButton = new GuiSlimButton(idEditButton, 68 + (width - xSize) / 2, 34 + (height - ySize) / 2, 40, 12, 
				StatCollector.translateToLocal("general.button.edit"));
		applyButton = new GuiSlimButton(idApplyButton, 110 + (width - xSize) / 2, 34 + (height - ySize) / 2, 40, 12, 
				StatCollector.translateToLocal("general.button.save"));
		buttonList.clear();
		buttonList.add(editButton);
		buttonList.add(applyButton);
		buttonList.add(infiniteButton);
		
		blockOwnerField = new GuiTextField(this.fontRendererObj, 12, 20, 138, 13);
		blockOwnerField.setFocused(false);
		blockOwnerField.setMaxStringLength(100);
		blockOwnerField.setText(tileEntity.blockOwner);
		blockOwnerField.setEnableBackgroundDrawing(true);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor-wrench.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(StatCollector.translateToLocal("vending.wrench.owner"), 6, 5, 4210752);
		blockOwnerField.drawTextBox();
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (blockOwnerField.isFocused()) {
			blockOwnerField.textboxKeyTyped(c, i);
		} else super.keyTyped(c, i);

	}
	
	protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
    }
	
	protected void actionPerformed(GuiButton button) {
		if (button.id == idEditButton) {
			blockOwnerField.setFocused(true);
		}
		if (button.id == idApplyButton) {
			String blockOwner = blockOwnerField.getText();
			try {
				tileEntity.blockOwner = blockOwner;
			} catch (Throwable ex2) {
				//fail silently?
			}
			blockOwnerField.setFocused(false);
			tileEntity.sendServerUpdateMessage();
		}
		if (button.id == idInfiniteButton) {
			infinite = !infinite;
			infiniteButton.displayString = (infinite ? StatCollector.translateToLocal("vending.wrench.infiniteon") :
				StatCollector.translateToLocal("vending.wrench.infiniteoff"));
			tileEntity.infiniteSell = infinite;
			tileEntity.sendServerUpdateMessage();
		}
	}
}
