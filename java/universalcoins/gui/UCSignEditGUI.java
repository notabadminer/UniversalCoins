package universalcoins.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import universalcoins.tile.TileUCSign;

public class UCSignEditGUI extends GuiScreen {
	
	/** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Edit sign message:";
    /** Reference to the sign object. */
    private TileUCSign tileSign;
    /** Counts the number of screen updates. */
    private int updateCounter;
    /** The index of the line that is being edited. */
    private int editLine;
    private GuiButton doneBtn;

	public UCSignEditGUI(TileEntitySign tileEntity) {
		this.tileSign = (TileUCSign) tileEntity;
	}
	
	/**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.tileSign.setEditable(false);
        this.buttonList.clear();
        this.buttonList.add(this.doneBtn = new GuiButton(0, this.width / 2 - 50, this.height / 2 + 50, 100 , 19, "Done"));
    }
	
	public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
		tileSign.blockOwner = this.mc.thePlayer.getName();
		tileSign.sendServerUpdateMessage();
        this.tileSign.setEditable(true);
    }
	
	/**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        ++this.updateCounter;
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 0) {
                this.tileSign.markDirty();
                this.mc.displayGuiScreen((GuiScreen)null);
            }
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 200)
        {
            this.editLine = this.editLine - 1 & 3;
        }

        if (keyCode == 208 || keyCode == 28 || keyCode == 156)
        {
            this.editLine = this.editLine + 1 & 3;
        }

        String s = this.tileSign.signText[this.editLine].getUnformattedText();

        if (keyCode == 14 && s.length() > 0)
        {
            s = s.substring(0, s.length() - 1);
        }

        if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && this.fontRendererObj.getStringWidth(s + typedChar) <= 180)
        {
            s = s + typedChar;
        }

        this.tileSign.signText[this.editLine] = new ChatComponentText(s);

        if (keyCode == 1)
        {
            this.actionPerformed(this.doneBtn);
        }
    }

	
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 40, 16777215);
        final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/sign.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		int xSize = 100;
		int ySize = 50;
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		int[] lineOffset = { -20, -8, 4, 16};
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
        if (this.updateCounter / 6 % 2 == 0) {
            this.tileSign.lineBeingEdited = this.editLine;
        }
        
        //draw active line indicator
        if (tileSign.lineBeingEdited != -1) {
        	if (tileSign.signText[tileSign.lineBeingEdited].getUnformattedText().length() > 15) {
        		fontRendererObj.drawString("> ", (this.width - 90) / 2 - 8, this.height / 2 + lineOffset[tileSign.lineBeingEdited], 0x000000);
        		fontRendererObj.drawString(" <", (this.width + 	90) / 2, this.height / 2 + lineOffset[tileSign.lineBeingEdited], 0x000000);
        	} else {
        		int stringLength = fontRendererObj.getStringWidth(tileSign.signText[tileSign.lineBeingEdited].getUnformattedText());
        		fontRendererObj.drawString("> ", (this.width - stringLength) / 2 - 8, this.height / 2 + lineOffset[tileSign.lineBeingEdited], 0x000000);
        		fontRendererObj.drawString(" <", (this.width + stringLength) / 2, this.height / 2 + lineOffset[tileSign.lineBeingEdited], 0x000000);
        	}
        }
        //draw sign text
        String displayString;
        for (int i = 0; i < tileSign.signText.length; i++) {
        	if (tileSign.signText[i].getUnformattedText().length() > 15) {
        		displayString = tileSign.signText[i].getUnformattedText().substring(tileSign.signText[i].getUnformattedText().length() - 15);
        	} else {
        		displayString = tileSign.signText[i].getUnformattedText();
        	}
        	int stringLength = fontRendererObj.getStringWidth(displayString);
            fontRendererObj.drawString(displayString, (this.width - stringLength) / 2, this.height / 2 + lineOffset[i], 0x000000);
        }
          
        this.tileSign.lineBeingEdited = -1;
        
        super.drawScreen(par1, par2, par3);
    }
}
