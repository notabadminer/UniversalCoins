package com.notabadminer.universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.notabadminer.universalcoins.UniversalCoins;

public class ItemLargeCoinBag extends Item{

	public ItemLargeCoinBag() {
		super();
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		DecimalFormat formatter = new DecimalFormat("###,###,###");
		list.add(formatter.format(stack.stackSize * 6561) + " Coins");
	}
}
