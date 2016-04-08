package universalcoins.tileentity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;

public class TileVendorFrame extends TileVendor {

	String signText[] = { "", "", "", "" };

	@Override
	public String getName() {
		return I18n.translateToLocal("tile.vendor_frame.name");
	}

	public void updateSigns() {

		if (inventory[itemTradeSlot] != null) {

			signText[0] = sellMode ? "&" + Integer.toHexString(textColor) + "Selling"
					: "&" + Integer.toHexString(textColor) + "Buying";
			// add out of stock notification if not infinite and no stock found
			if (!infiniteMode && sellMode && ooStockWarning) {
				signText[0] = "&" + Integer.toHexString(textColor) + (I18n.translateToLocal("sign.warning.stock"));
			}
			// add out of coins notification if buying and no funds available
			if (!sellMode && ooCoinsWarning && !infiniteMode) {
				signText[0] = "&" + Integer.toHexString(textColor) + (I18n.translateToLocal("sign.warning.coins"));
			}
			// add inventory full notification
			if (!sellMode && inventoryFullWarning) {
				signText[0] = "&" + Integer.toHexString(textColor)
						+ (I18n.translateToLocal("sign.warning.inventoryfull"));
			}
			if (inventory[itemTradeSlot].stackSize > 1) {
				signText[1] = "&" + Integer.toHexString(textColor) + inventory[itemTradeSlot].stackSize + " "
						+ inventory[itemTradeSlot].getDisplayName();
			} else {
				signText[1] = "&" + Integer.toHexString(textColor) + inventory[itemTradeSlot].getDisplayName();
			}
			if (inventory[itemTradeSlot].isItemEnchanted()) {
				signText[2] = "&" + Integer.toHexString(textColor);
				NBTTagList tagList = inventory[itemTradeSlot].getEnchantmentTagList();
				for (int i = 0; i < tagList.tagCount(); i++) {
					NBTTagCompound enchant = ((NBTTagList) tagList).getCompoundTagAt(i);
					// signText[2] =
					// signText[2].concat(Enchantment.enchantmentsBookList[enchant.getInteger("id")]
					// .getTranslatedName(enchant.getInteger("lvl")) + ", ");
				}
			} else
				signText[2] = "";
			if (inventory[itemTradeSlot].getItem() == UniversalCoins.proxy.uc_package) {
				if (inventory[itemTradeSlot].getTagCompound() != null) {
					signText[2] = "&" + Integer.toHexString(textColor);
					NBTTagList tagList = inventory[itemTradeSlot].getTagCompound().getTagList("Inventory",
							Constants.NBT.TAG_COMPOUND);
					for (int i = 0; i < tagList.tagCount(); i++) {
						NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
						byte slot = tag.getByte("Slot");
						int itemCount = ItemStack.loadItemStackFromNBT(tag).stackSize;
						String itemName = ItemStack.loadItemStackFromNBT(tag).getDisplayName();
						signText[2] += itemCount + ":" + itemName + " ";
					}
				}
			}
			signText[3] = "&" + Integer.toHexString(textColor) + "Price: " + itemPrice;

			// find and update all signs
			TileEntity te;
			te = super.worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));
			if (te != null && te instanceof TileUCSign) {
				TileUCSign tesign = (TileUCSign) te;
				for (int i = 0; i < 4; i++) {
					tesign.signText[i] = new TextComponentString(this.signText[i]);
				}
				tesign.updateSign();
				tesign.markDirty();
			}
			te = super.worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
			if (te != null && te instanceof TileUCSign) {
				TileUCSign tesign = (TileUCSign) te;
				for (int i = 0; i < 4; i++) {
					tesign.signText[i] = new TextComponentString(this.signText[i]);
				}
				tesign.updateSign();
				tesign.markDirty();
			}
		}
	}
}
