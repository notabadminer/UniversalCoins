package universalcoins.util;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import universalcoins.UniversalCoins;
import cpw.mods.fml.common.registry.GameRegistry;

public class UCRecipeHelper {
	
	private static ItemStack oneSeller = new ItemStack(UniversalCoins.proxy.itemSeller);
	private static ItemStack oneCoin = new ItemStack(UniversalCoins.proxy.itemCoin);
	private static ItemStack oneSStack = new ItemStack(UniversalCoins.proxy.itemSmallCoinStack);
	private static ItemStack oneLStack = new ItemStack(UniversalCoins.proxy.itemLargeCoinStack);
	private static ItemStack oneSSack = new ItemStack(UniversalCoins.proxy.itemSmallCoinBag);
	private static ItemStack oneLSack = new ItemStack(UniversalCoins.proxy.itemLargeCoinBag);
	
	
	public static void addCoinRecipes(){
		
		
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemCoin, 9), new Object[]{
			oneSStack
		});
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemSmallCoinStack, 9), new Object[]{
			oneLStack
		});
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemLargeCoinStack, 9), new Object[]{
			oneSSack
		});
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemSmallCoinBag, 9), new Object[]{
			oneLSack
		});
		
		GameRegistry.addShapelessRecipe(oneSStack, new Object[]{
				oneCoin, oneCoin, oneCoin, oneCoin, oneCoin, oneCoin, oneCoin, oneCoin, oneCoin
		});
		GameRegistry.addShapelessRecipe(oneLStack, new Object[]{
				oneSStack, oneSStack, oneSStack, oneSStack, oneSStack, oneSStack,oneSStack, oneSStack, oneSStack
		});
		GameRegistry.addShapelessRecipe(oneSSack, new Object[]{
				oneLStack, oneLStack, oneLStack, oneLStack, oneLStack, oneLStack,oneLStack, oneLStack, oneLStack
		});
		GameRegistry.addShapelessRecipe(oneLSack, new Object[]{
				oneSSack, oneSSack, oneSSack, oneSSack, oneSSack, oneSSack, oneSSack, oneSSack, oneSSack
		});
	}

	public static void addTradeStationRecipe() {
		GameRegistry.addShapedRecipe(oneSeller, new Object[]{
			"LGE",
			"PPP",
			'L', Items.leather, 'G', Items.gold_ingot, 'E', Items.ender_pearl, 'P', Items.paper
		});
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockTradeStation), new Object[]{
			"IGI",
			"ICI",
			"III",
			'I', Items.iron_ingot, 'G', Items.gold_ingot, 'C', UniversalCoins.proxy.itemSeller
		});
	}
	
	public static void addVendingBlockRecipes() {
		for(int i=0; i < Vending.supports.length; i++){
			GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockVendor,1,i), new Object[]{
				"XXX",
				"XGX",
				"*R*", 'X', Blocks.glass, 'G', Items.gold_ingot, 'R', Items.redstone, '*', Vending.reagents[i]
				});
		}
	}
	
	public static void addCardStationRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockCardStation), new Object[]{
			"III",
			"ICI",
			"III",
			'I', Items.iron_ingot,'C', UniversalCoins.proxy.itemSmallCoinBag
		});
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockBase), new Object[]{
			"III",
			"ICI",
			"III",
			'I', Items.iron_ingot,'C', UniversalCoins.proxy.itemCoin
		});
	}
	
	public static void addEnderCardRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemEnderCard), new Object[]{
				UniversalCoins.proxy.itemUCCard, Items.ender_pearl
		});
	}
}