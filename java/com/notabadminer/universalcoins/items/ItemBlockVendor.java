package com.notabadminer.universalcoins.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockVendor extends ItemBlock {

	public ItemBlockVendor(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public int getMetadata(int meta){
	return meta;
	}
}
