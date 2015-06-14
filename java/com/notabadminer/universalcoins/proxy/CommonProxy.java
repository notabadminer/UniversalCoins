package com.notabadminer.universalcoins.proxy;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.notabadminer.universalcoins.items.ItemCoin;
import com.notabadminer.universalcoins.items.ItemLargeCoinBag;
import com.notabadminer.universalcoins.items.ItemLargeCoinStack;
import com.notabadminer.universalcoins.items.ItemPackage;
import com.notabadminer.universalcoins.items.ItemSeller;
import com.notabadminer.universalcoins.items.ItemSmallCoinBag;
import com.notabadminer.universalcoins.items.ItemSmallCoinStack;
import com.notabadminer.universalcoins.items.ItemUCard;
import com.notabadminer.universalcoins.items.ItemVendorWrench;


public class CommonProxy {
	
	//items
    public static Item coin = new ItemCoin().setUnlocalizedName("coin");
    public static Item smallCoinStack = new ItemSmallCoinStack().setUnlocalizedName("smallCoinStack");
    public static Item largeCoinStack = new ItemLargeCoinStack().setUnlocalizedName("largeCoinStack");
    public static Item smallCoinBag = new ItemSmallCoinBag().setUnlocalizedName("smallCoinBag");
    public static Item largeCoinBag = new ItemLargeCoinBag().setUnlocalizedName("largeCoinBag");
    public static Item uPackage = new ItemPackage().setUnlocalizedName("package");
    public static Item seller = new ItemSeller().setUnlocalizedName("seller");
    public static Item vendorWrench = new ItemVendorWrench().setUnlocalizedName("vendorWrench");
    public static Item universalCard = new ItemUCard().setUnlocalizedName("universalCard");
    
    //blocks
 
	
	public void registerRenderers()
	{
		//blank since we don't do anything on the server
	}
	
	public static void registerBlocks()
	{
        //GameRegistry.registerBlock(block, block.getUnlocalizedName().substring(5));
	}

	public static void registerItems()
	{		
		GameRegistry.registerItem(coin, coin.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(smallCoinStack, smallCoinStack.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(largeCoinStack, largeCoinStack.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(smallCoinBag, smallCoinBag.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(largeCoinBag, largeCoinBag.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(uPackage, uPackage.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(seller, seller.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(vendorWrench, vendorWrench.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(universalCard, universalCard.getUnlocalizedName().substring(5));
	}
}
