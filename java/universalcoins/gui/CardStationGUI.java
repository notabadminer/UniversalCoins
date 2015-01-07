package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import universalcoins.inventory.ContainerCardStation;
import universalcoins.tile.TileCardStation;

public class CardStationGUI extends GuiContainer{
	private GuiButton buttonOne, buttonTwo, buttonThree, buttonFour;
	private GuiTextField textField;
	private TileCardStation tEntity;
	public static final int idButtonOne = 0;
	public static final int idButtonTwo = 1;
	public static final int idButtonThree = 2;
	public static final int idButtonFour = 3;
	
	public int menuState = 0;
	private static final String[] menuStateName = new String[] { "welcome",	"auth", "main", "additional", 
		"balance", "deposit", "withdraw", "newcard", "transferaccount", "customaccount", "takecard", 
		"takecoins", "insufficient", "invalid", "badcard", "unauthorized", "customaccountoptions","newaccount"};
	int barProgress = 0;
	
	boolean shiftPressed = false;

	public CardStationGUI(InventoryPlayer inventoryPlayer, TileCardStation tileEntity) {
		super(new ContainerCardStation(inventoryPlayer, tileEntity));
		tEntity = tileEntity;
		
		xSize = 176;
		ySize = 201;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (menuState == 6 || menuState == 9) {
			textField.setFocused(true);
			textField.textboxKeyTyped(c, i);
			textField.setFocused(false);
		} else super.keyTyped(c, i);

	}
	
	@Override
	public void initGui() {
		super.initGui();
		buttonOne = new GuiSlimButton(idButtonOne, 8 + (width - xSize) / 2, 30 + (height - ySize) / 2, 18, 12, "");
		buttonTwo = new GuiSlimButton(idButtonTwo, 8 + (width - xSize) / 2, 50 + (height - ySize) / 2, 18, 12, "");
		buttonThree = new GuiSlimButton(idButtonThree, 8 + (width - xSize) / 2, 70 + (height - ySize) / 2, 18, 12, "");
		buttonFour = new GuiSlimButton(idButtonFour, 8 + (width - xSize) / 2, 90 + (height - ySize) / 2, 18, 12, "");
		buttonList.clear();
		buttonList.add(buttonOne);
		buttonList.add(buttonTwo);
		buttonList.add(buttonThree);
		buttonList.add(buttonFour);
		
		textField = new GuiTextField(this.fontRendererObj, 1, 1, 100, 13);
		textField.setFocused(false);
		textField.setMaxStringLength(9);
		textField.setText("0");
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/cardStation.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		if (menuState == 0 && super.inventorySlots.getSlot(0).getStack() != null && tEntity.accountBalance >= 0) {
			menuState = 2;
		}
		if (menuState == 1) {
			//state 1 is auth - run eye scan
			barProgress++;
			this.drawTexturedModalRect(x + 151, y + 19, 176, 0, 18, 18);
			this.drawTexturedModalRect(x + 34, y + 43, 0, 201, Math.min(barProgress, 104), 5);
			if (barProgress > 105) {
				String authString = StatCollector.translateToLocal("cardstation.auth.access");
				if (authString.startsWith("C:")) authString = authString.substring(2);
				int stringLength = fontRendererObj.getStringWidth(authString);
				int cx = width / 2 - stringLength / 2;
				fontRendererObj.drawString(authString, cx, y + 52, 4210752);
			}
			if (barProgress > 120) {
				if (!tEntity.accountNumber.matches("none")) {
					String authString = StatCollector.translateToLocal("cardstation.auth.success");
					if (authString.startsWith("C:")) authString = authString.substring(2);
					int stringLength = fontRendererObj.getStringWidth(authString);
					int cx = width / 2 - stringLength / 2;
					fontRendererObj.drawString(authString, cx, y + 72, 4210752);
					if (barProgress > 160) {menuState = 2;barProgress = 0;}
				} else {
					String authString = StatCollector.translateToLocal("cardstation.auth.fail");
					if (authString.startsWith("C:")) authString = authString.substring(2);
					int stringLength = fontRendererObj.getStringWidth(authString);
					int cx = width / 2 - stringLength / 2;
					fontRendererObj.drawString(authString, cx, y + 72, 4210752);
					if (barProgress > 160) {menuState = 17;barProgress = 0;}
				}
			}
		}
		if (menuState == 2 && tEntity.accountBalance == -1) {
			tEntity.sendButtonMessage(6, false); //message to destroy card
			menuState = 14;
		}
		
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");//TODO localization
		if (menuState == 4 || menuState == 5) {
			fontRendererObj.drawString(formatter.format(tEntity.accountBalance), x + 34, y + 52, 4210752);
		}
		if (menuState == 6) {
			//display account balance
			fontRendererObj.drawString(formatter.format(tEntity.accountBalance), x + 34, y + 32, 4210752);
			fontRendererObj.drawString(textField.getText(), x + 34, y + 52, 4210752);
		}
		if (menuState == 9) {
			//display text field for custom name entry
			if (textField.getText() == "0") { //textfield has not been initialized. do it now
				textField.setMaxStringLength(20);
				textField.setText(tEntity.customAccountName);
			}
			fontRendererObj.drawString(textField.getText(), x + 34, y + 42, 4210752);
		}
		if (menuState == 14) {
			barProgress++;
			if (barProgress > 100) {
				menuState = 0;
				barProgress = 0;
			}
		}
		if (menuState == 15) {
			barProgress++;
			if (barProgress > 100) {
				menuState = 2;
				barProgress = 0;
			}
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tEntity.getInventoryName(), 6, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 6, ySize - 96 + 2, 4210752);
		drawMenu(menuState);
	}
	
	protected void actionPerformed(GuiButton button) {
		//We are not going to send button IDs to the server
		//instead, we are going to use function IDs to send
		//a packet to the server to do things
		int functionID = 0;
		switch (menuState) {
			case 0:
				//welcome
				//we run function 5 here to get the player account info
				if (button.id == idButtonOne){functionID = 5;menuState = 1;}
				if (button.id == idButtonTwo){functionID = 5;menuState = 1;}
				if (button.id == idButtonThree){functionID = 5;menuState = 1;}
				if (button.id == idButtonFour){functionID = 5;menuState = 1;}
				barProgress = 0; //always set scan progress to zero so scan will take place next time
				break;
			case 1:
				//authentication
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){}
				break;
			case 2:
				//main menu
				if (button.id == idButtonOne){menuState = 4;}
				if (button.id == idButtonTwo){functionID = 3;menuState = 5;}
				if (button.id == idButtonThree){textField.setText("0");menuState = 6;}
				if (button.id == idButtonFour){menuState = 3;}
				break;
			case 3:
				//additional menu
				if (button.id == idButtonOne){
					if (!tEntity.cardOwner.contentEquals(tEntity.player)) {
					menuState = 15;
				} else menuState = 7;}
				if (button.id == idButtonTwo){
					if (!tEntity.cardOwner.contentEquals(tEntity.player)) {
					menuState = 15;
				} else menuState = 8;}
				if (button.id == idButtonThree){
					if (!tEntity.customAccountName.contentEquals("none")) {
						menuState = 16;
					} else if (!tEntity.cardOwner.contentEquals(tEntity.player)) {
					menuState = 15;
				} else menuState = 9;}
				if (button.id == idButtonFour){}
				break;
			case 4:
				//balance
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){menuState = 2;}
				break;
			case 5:
				//deposit
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){menuState = 2;}
				break;
			case 6:
				//withdraw
				int coinWithdrawalAmount = 0;
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){//Output coins;
					try { coinWithdrawalAmount = Integer.parseInt(textField.getText());
					} catch (NumberFormatException ex) {
		                menuState = 13;
		            } catch (Throwable ex2) {
		                menuState = 13;
		            }
					if (coinWithdrawalAmount > tEntity.accountBalance) {
						menuState = 12;
					} else if (coinWithdrawalAmount <= 0) {
						menuState = 13;
					} else { 
						//send message to server with withdrawal amount
						tEntity.sendServerUpdatePacket(coinWithdrawalAmount);
						functionID = 4;
						menuState = 11;} }
				if (button.id == idButtonFour){menuState = 2;}
				break;
			case 7:
				//new card
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){					
					functionID = 1;menuState = 10;}
				if (button.id == idButtonFour){menuState = 0;}
				break;
			case 8:
				//transfer account
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){functionID = 2;menuState = 10;}
				if (button.id == idButtonFour){menuState = 2;}
				break;
			case 9:
				//customaccount
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){
					if (!tEntity.customAccountName.contentEquals("none")) {
						String customName = textField.getText();
						tEntity.sendServerUpdatePacket(customName);
						functionID = 9;menuState = 10;
					} else {
						String customName = textField.getText();
						tEntity.sendServerUpdatePacket(customName);
						functionID = 7;menuState = 10;}
					}
				if (button.id == idButtonFour){menuState = 3;}
				break;
			case 10:
				//take card
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){menuState = 0;}
				break;
			case 11:
				//take coins
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){menuState = 0;}
				break;
			case 12:
				//Insufficient funds
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){menuState = 6;}
				break;
			case 13:
				//Invalid input
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){menuState = 6;}
				break;
			case 14:
				//Bad card - account not found
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){}
				break;
			case 15:
				//unauthorized access - card does not belong to player
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){}
				break;
			case 16:
				//custom account options
				if (button.id == idButtonOne){
					functionID = 7;
					menuState = 10;
				}
				if (button.id == idButtonTwo){
					menuState = 9;
				}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){menuState = 2;}
				break;
			case 17:
				//new account
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){					
					functionID = 1;menuState = 10;}
				if (button.id == idButtonFour){menuState = 0;}
				break;
			default:
				//we should never get here
				if (button.id == idButtonOne){}
				if (button.id == idButtonTwo){}
				if (button.id == idButtonThree){}
				if (button.id == idButtonFour){}				
				break;
		}
		//We include the shift boolean for compatibility with the button message only. It does nothing
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			shiftPressed = true;
		}
		else {
			shiftPressed = false;
		}
		//function1 - new card
		//function2 - transfer account
		//function3 - deposit
		//function4 - withdraw
		//function5 - get account info
		//function6 - destroy invalid card - called from drawGuiContainerBackgroundLayer
		//function7 - new custom account
		//function8 - new custom card
		//function9 - transfer custom account
		//we use function IDs as we don't have specific functions for each button
		tEntity.sendButtonMessage(functionID, shiftPressed);
	}
	
	private void drawMenu(int state) {
		int lineCoords[] = {22, 32, 42, 52, 62, 72, 82, 92};
		String[] endString = {".one", ".two", ".three", ".four", ".five", ".six", ".seven", ".eight"};
		String menuString;
		for(int x = 0; x < 8; x++) {
			menuString = StatCollector.translateToLocal("cardstation." + menuStateName[state] + endString[x]);
			if (menuString.startsWith("C:")) { //center text
				menuString = menuString.substring(2);//strip centering flag
				int stringLength = fontRendererObj.getStringWidth(menuString);
				int cx = xSize / 2 - stringLength / 2;
				fontRendererObj.drawString(menuString, cx, lineCoords[x], 4210752);
			} else { //draw normally
				fontRendererObj.drawString(menuString, 34, lineCoords[x], 4210752);
			}
		}
	}
}
