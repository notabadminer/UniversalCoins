package com.notabadminer.universalcoins;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class UCTab extends CreativeTabs {

	public UCTab(String label) {
		super(label);
	}

	@Override
	public Item getTabIconItem() {
		return UniversalCoins.proxy.coin;
	}

}
