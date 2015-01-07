package universalcoins.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalcoins.tile.TileVendor;

public class HintGui extends GuiScreen {
	private static Minecraft mc = Minecraft.getMinecraft();

	public void renderToHud() {
		if ((mc.inGameHasFocus || (mc.currentScreen != null && (mc.currentScreen instanceof GuiChat)))
				&& !mc.gameSettings.showDebugInfo) {
			ScaledResolution res = new ScaledResolution(
					HintGui.mc.gameSettings, HintGui.mc.displayWidth,
					HintGui.mc.displayHeight);
			FontRenderer fontRender = mc.fontRenderer;
			boolean warning = false;
			int width = res.getScaledWidth();
			int height = res.getScaledHeight();
			int w = 80;
			int h = 26;
			int centerYOff = -80;
			int cx = width / 2;
			int x = cx - w / 2;
			int y = height / 2 - h / 2 + centerYOff;
			World world = mc.theWorld;
			MovingObjectPosition mop = mc.objectMouseOver;
			if (mop == null) return; //null pointer error bugfix?
			TileEntity te = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
			if (te == null || !(te instanceof TileVendor)) {
				return;
			}
			TileVendor tileEntity = (TileVendor) te;
			ItemStack itemSelling = tileEntity.getSellItem();
			List<String> itemInfoStringList = new ArrayList<String>();
			itemInfoStringList.add(tileEntity.sellMode ? StatCollector.translateToLocal("hintgui.sellmode.sell"): 
				StatCollector.translateToLocal("hintgui.sellmode.buy"));
			if (itemSelling != null) {
				if (itemSelling.stackSize > 1) {
					itemInfoStringList.add(itemSelling.stackSize + " " + itemSelling.getDisplayName());
				} else { 
					itemInfoStringList.add(itemSelling.getDisplayName());
				}
				
				if (itemSelling.isItemEnchanted()) {
					NBTTagList tagList = itemSelling.getEnchantmentTagList();
					for (int i = 0; i < tagList.tagCount(); i++) {
						NBTTagCompound enchant = ((NBTTagList) tagList)
								.getCompoundTagAt(i);
						String eInfo = Enchantment.enchantmentsList[enchant
								.getInteger("id")].getTranslatedName(enchant
								.getInteger("lvl"));
						
						itemInfoStringList.add(eInfo);
					}
				}
				DecimalFormat formatter = new DecimalFormat("#,###,###,###");//TODO localization
				itemInfoStringList.add(StatCollector.translateToLocal("hintgui.price") + formatter.format(tileEntity.itemPrice));
				//add out of stock notification if not infinite and no stock found
				if (!tileEntity.infiniteSell && tileEntity.sellMode && tileEntity.ooStockWarning) {
					warning = true;
					itemInfoStringList.add(StatCollector.translateToLocal("hintgui.warning.stock"));
				}
				//add out of coins notification if buying and no funds available
				if (!tileEntity.sellMode && tileEntity.ooCoinsWarning) {
					warning = true;
					itemInfoStringList.add(StatCollector.translateToLocal("hintgui.warning.coins"));
				}
				//add inventory full notification
				if (!tileEntity.sellMode && tileEntity.inventoryFullWarning) {
					warning = true;
					itemInfoStringList.add(StatCollector.translateToLocal("hintgui.warning.inventoryfull"));
				}
				// reset height since we now have more lines
				h = (10 * itemInfoStringList.size() + 4);
				//reset width to longest string plus a bit
				String longestString = itemInfoStringList.get(0).toString();
				for (int i = 0; i < itemInfoStringList.size(); i++) {
					if (itemInfoStringList.get(i).toString().length() > longestString.length()) longestString = 
							itemInfoStringList.get(i).toString();
				}
				w = fontRender.getStringWidth(longestString) + 8;
				//set start point for x draw of background rectangle
				x = cx - w / 2;
			} else {
				return;
			}

			int color = 0xffffff;
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0f, 0.0f, -180.0f);
			drawGradientRect(x, y, x + w, y + h, 0xc0101010, 0xd0101010);
			for (int i = 0; i < itemInfoStringList.size(); i++) {
				if (warning && i == itemInfoStringList.size() - 1) {
					color = 0xef0000;
				} else color = 0xffffff;
				fontRender.drawString(itemInfoStringList.get(i), cx - 
						fontRender.getStringWidth(itemInfoStringList.get(i)) / 2 ,
						y + 4 + (10 * i), color);
			}
			GL11.glPopMatrix();
		}
	}

}
