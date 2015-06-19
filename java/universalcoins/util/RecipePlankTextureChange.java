package universalcoins.util;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import universalcoins.UniversalCoins;

public class RecipePlankTextureChange implements IRecipe {

	private ItemStack newStack;
	private ItemStack plankStack;

	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		this.newStack = null;
		boolean hasItem = false;
		boolean hasPlank = false;
		for (int j = 0; j < inventorycrafting.getSizeInventory(); j++) {
			if (inventorycrafting.getStackInSlot(j) != null && !hasPlank && 
					isWoodPlank(inventorycrafting.getStackInSlot(j))) {
					hasPlank = true;
					plankStack = inventorycrafting.getStackInSlot(j);
					continue;
			}
			if (inventorycrafting.getStackInSlot(j) != null && !hasItem && 
					(inventorycrafting.getStackInSlot(j).getItem() == UniversalCoins.proxy.itemUCSign ||
					Block.getBlockFromItem(inventorycrafting.getStackInSlot(j).getItem()) == UniversalCoins.proxy.blockVendorFrame)) {
				hasItem = true;
				newStack = inventorycrafting.getStackInSlot(j).copy();
				continue;
			}
			if (inventorycrafting.getStackInSlot(j) != null) {
				return false;
			}
		}
		
		if (!hasPlank || !hasItem)
			return false;
		else
			return true;
	}

	private boolean isWoodPlank(ItemStack stack) {
		for (ItemStack oreStack : OreDictionary.getOres("plankWood")) {
			if (OreDictionary.itemMatches(oreStack, stack, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		NBTTagList itemList = new NBTTagList();
		NBTTagCompound tag = new NBTTagCompound();
		if (plankStack != null) {
			tag.setByte("Texture", (byte) 0);
			plankStack.writeToNBT(tag);
		}
		tag.setTag("Inventory", itemList);
		this.newStack.setTagCompound(tag);
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
