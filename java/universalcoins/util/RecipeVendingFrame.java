package universalcoins.util;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import universalcoins.UniversalCoins;

public class RecipeVendingFrame implements IRecipe {
	
	private ItemStack newStack;
	private Object[] recipeItems = {Items.stick, Items.gold_ingot, Items.stick, 
			Items.redstone , Blocks.planks, Items.redstone,
			Items.stick, Items.stick, Items.stick};
	
	@Override
	public boolean matches(InventoryCrafting var1, World var2) {
		this.newStack = null;
		for (int j = 0; j < var1.getSizeInventory(); j++) {
			if (var1.getStackInSlot(j) == null && recipeItems[j] != null) return false;
			if (var1.getStackInSlot(j) != null) {
				if (j == 4) {
					if (!isWoodPlank(var1.getStackInSlot(j))) return false;
				} else {
					if (var1.getStackInSlot(j).getItem() != recipeItems[j]) return false;
				}
			}
		}
		newStack = new ItemStack(UniversalCoins.proxy.blockVendorFrame);		
		ItemStack textureStack = var1.getStackInSlot(4);
		NBTTagList itemList = new NBTTagList();
		NBTTagCompound tag = new NBTTagCompound();
		if (textureStack != null) {
			tag.setByte("Texture", (byte) 0);
			textureStack.writeToNBT(tag);
		}
		tag.setTag("Inventory", itemList);
		this.newStack.setTagCompound(tag);
		return true;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		return newStack;
	}

	@Override
	public int getRecipeSize() {
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return newStack;
	}
	
	private boolean isWoodPlank (ItemStack stack) {
		for (ItemStack oreStack : OreDictionary.getOres("plankWood")) {
			if (OreDictionary.itemMatches(oreStack, stack, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inventoryCrafting) {
		ItemStack[] remainingItems = new ItemStack[inventoryCrafting.getSizeInventory()];
		for (int i = 0; i < remainingItems.length; ++i) {
			ItemStack itemstack = inventoryCrafting.getStackInSlot(i);
			remainingItems[i] = net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack);
		}
		return remainingItems;
	}

}
