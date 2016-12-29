package universalcoins.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

public class ItemBlockVendor extends ItemBlockWithMetadata {

	private String[] metaTypes = { "stone", "cobblestone", "stonebrick", "wood", "crafting_table", "gravel", "jukebox",
			"sandstone", "gold", "iron", "brick", "mossy_cobblestone", "obsidian", "diamond", "emerald", "lapis", };

	public ItemBlockVendor(Block block) {
		super(block, block);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getItemDamage() < 16)
			return this.getUnlocalizedName() + "." + metaTypes[stack.getItemDamage()];
		return this.getUnlocalizedName() + "." + metaTypes[0];
	}
}
