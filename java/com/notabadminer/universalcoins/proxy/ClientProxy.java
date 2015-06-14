package com.notabadminer.universalcoins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;

import com.notabadminer.universalcoins.UniversalCoins;

public class ClientProxy extends CommonProxy {

	public void registerRenderers() {
		
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		mesher.register(coin,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + coin.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(smallCoinStack,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + smallCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(largeCoinStack,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + largeCoinStack.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(smallCoinBag,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + smallCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(largeCoinBag,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + largeCoinBag.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(uPackage,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + uPackage.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(seller,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + seller.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(vendorWrench,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + vendorWrench.getUnlocalizedName().substring(5), "inventory"));
		mesher.register(universalCard,	0, new ModelResourceLocation(UniversalCoins.MODID + 
				":" + universalCard.getUnlocalizedName().substring(5), "inventory"));
		

		// Item item = Item.getItemFromBlock(block);
		// Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item,
		// 0, new ModelResourceLocation(UniversalCoins.MODID +
		// ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

}
